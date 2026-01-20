package com.chenweikeng.nra.playerheads;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

public class PlayerHeadRenderer {
    private static final int MARKER_R = 2;
    private static final int MARKER_G = 1;
    private static final int MARKER_B = 3;
    private static final int MARKER_X = 63;
    private static final int MARKER_Y = 0;

    private static final int CONTROL_X = 63;
    private static final int CONTROL_Y = 1;

    private static final String NEW_NAMESPACE = "processed_images";
    private static final Map<Identifier, Identifier> processedTextures = new HashMap<>();

    public static boolean render(
        Identifier skinTexture,
        MatrixStack matrixStack, 
        Direction direction,
        int light,
        float yaw
    ) {
        AbstractTexture texture = MinecraftClient.getInstance().getTextureManager().getTexture(skinTexture);

        NativeImage image = null;
        if (texture instanceof NativeImageBackedTexture nativeImageBackedTexture) {
            image = nativeImageBackedTexture.getImage();
        } else {
            image = getTextureImageViaReflection(texture);
        }
        
        if(image == null) return false;

        int pixel = image.getColorArgb(MARKER_X, MARKER_Y);
        int r = (pixel >> 16) & 0xFF;
        int g = (pixel >> 8) & 0xFF;
        int b = (pixel >> 0) & 0xFF;

        if (r != MARKER_R || g != MARKER_G || b != MARKER_B) {
            return false;
        }

        int controlPixel = image.getColorArgb(CONTROL_X, CONTROL_Y);
        int controlR = (controlPixel >> 16) & 0xFF;
        int controlG = (controlPixel >> 8) & 0xFF;
        int controlB = (controlPixel >> 0) & 0xFF;
        int controlA = (controlPixel >> 24) & 0xFF;

        renderCustomSkull(direction, light, yaw, matrixStack, skinTexture,
            image,
            controlA,
            controlR,
            controlG,
            controlB
        );
        return true;
    }

    public static Identifier getNewImage(Identifier original, NativeImage input) {
        if(processedTextures.containsKey(original)) {
            return processedTextures.get(original);
        }

        NativeImage output = new NativeImage(
            input.getWidth(),
            input.getHeight(),
            true // ensure RGBA
        );

        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                int rgba = input.getColorArgb(x, y);

                int r = (rgba >> 16) & 0xFF;
                int g = (rgba >> 8) & 0xFF;
                int b = (rgba >> 0) & 0xFF;

                int a = (r == 2 && g == 1 && b == 3) ? 0 : 255;

                int newRGBA =
                        (a << 24) |
                        (r << 16) |
                        (g << 8) |
                        b;
                output.setColorArgb(x, y, newRGBA);
            }
        }

        Identifier newId = Identifier.of(
            NEW_NAMESPACE,
            original.getNamespace() + "/" + original.getPath()
        );

        NativeImageBackedTexture texture = new NativeImageBackedTexture(newId::toString, output);
        MinecraftClient.getInstance().getTextureManager().registerTexture(newId, texture);
        processedTextures.put(original, newId);
        return newId;
    }

    /**
     * Renders a custom skull with the given texture and scale.
     * 
     * @param skullBlockEntityRenderState The render state for the skull block entity
     * @param matrixStack The matrix stack for transformations
     * @param skinTexture The texture identifier for the skin
     * @param scale The scale factor for the skull (based on alpha channel of marker pixel)
     */
    public static void renderCustomSkull(
        Direction direction,
        int light,
        float yaw,
        MatrixStack matrixStack,
        Identifier skinTexture,
        NativeImage image,
        int controlA,
        int controlR,
        int controlG,
        int controlB
    ) {
        matrixStack.push();

        if (direction == null) {
            matrixStack.translate(0.5F, 0.0F, 0.5F);
        } else {
            matrixStack.translate(0.5F - direction.getOffsetX() * 0.25F, 0.25F, 0.5F - direction.getOffsetZ() * 0.25F);
        }
		matrixStack.scale(-1.0F, -1.0F, 1.0F);
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
        matrixStack.translate(0.0F, 0.0F, -0.25F);

        int overlay = OverlayTexture.DEFAULT_UV;
        
        float scaleX = 0;
        float scaleY = 0;

        if(controlB != 0) {
            scaleX = (float)controlA / 64.0f;
            scaleY = (float)controlA / 64.0f;
        } else {
            scaleX = (float)controlR / 16.0F - 0.0625F;
            scaleY = (float)controlG / 16.0F - 0.0625F;
        }

        Identifier actualIdentifier = null;
        if(controlB != 0) {
            actualIdentifier = getNewImage(skinTexture, image);
        } else {
            actualIdentifier = skinTexture;
        }

        // UV coordinates - skip the leftmost pixel column (contains control data)
        float minU = 0.0F;
        float maxU = 63.0F / 64.0F; // stop one column short

        float z = 0.0f;

        Immediate bufferSource = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderLayers.entityCutoutNoCull(actualIdentifier));

        Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
        MatrixStack.Entry pose = matrixStack.peek();

        vertexConsumer.vertex(matrix4f, -scaleX, -scaleY, z)
            .color(255, 255, 255, 255)
            .texture(minU, 0)
            .overlay(overlay)
            .light(light)
            .normal(pose, 0, 0, -1);
        
        vertexConsumer.vertex(matrix4f, scaleX, -scaleY, z)
            .color(255, 255, 255, 255)
            .texture(maxU, 0)
            .overlay(overlay)
            .light(light)
            .normal(pose, 0, 0, -1);
        
        vertexConsumer.vertex(matrix4f, scaleX, scaleY, z)
            .color(255, 255, 255, 255)
            .texture(maxU, 1)
            .overlay(overlay)
            .light(light)
            .normal(pose, 0, 0, -1);
        
        vertexConsumer.vertex(matrix4f, -scaleX, scaleY, z)
            .color(255, 255, 255, 255)
            .texture(minU, 1)
            .overlay(overlay)
            .light(light)
            .normal(pose, 0, 0, -1);

        matrixStack.pop();
    }

    private static NativeImage getTextureImageViaReflection(AbstractTexture texture) {
        try {
            Class<?> clazz = texture.getClass();
            while (clazz != null && clazz != Object.class) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.getType() == NativeImage.class) {
                        field.setAccessible(true);
                        NativeImage img = (NativeImage) field.get(texture);
                        if (img != null) {
                            return img;
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }
        } catch (Exception e) {
            // Ignore reflection errors
        }
        return null;
    }
}
