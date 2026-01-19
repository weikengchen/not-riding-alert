package com.chenweikeng.nra.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.chenweikeng.nra.playerheads.PlayerHeadRenderer;

import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.RenderSetup;

// Code from Camzanno
@Mixin(SkullBlockEntityRenderer.class)
public abstract class SkullBlockEntityRendererMixin {
    @Unique
    private static Identifier getTextureLocation(RenderSetup renderSetup, String textureKey) {
        Map<String, ?> textures = ((RenderSetupAccessor) (Object) renderSetup).getTextures();
        Object textureSpec = textures.get(textureKey);
        if (textureSpec == null) {
            return null;
        }
        
        // TextureSpec is a record, so we can use reflection to get the location
        try {
            java.lang.reflect.Method locationMethod = textureSpec.getClass().getMethod("comp_5228");
            return (Identifier) locationMethod.invoke(textureSpec);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Inject(
        method = "render(Lnet/minecraft/util/math/Direction;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/client/render/block/entity/SkullBlockEntityModel;Lnet/minecraft/client/render/RenderLayer;ILnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void onStaticRender(
        net.minecraft.util.math.Direction facing,
        float yaw,
        float poweredTicks,
        MatrixStack matrices,
        net.minecraft.client.render.command.OrderedRenderCommandQueue queue,
        int light,
        net.minecraft.client.render.block.entity.SkullBlockEntityModel model,
        net.minecraft.client.render.RenderLayer renderLayer,
        int outlineColor,
        net.minecraft.client.render.command.ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay,
        CallbackInfo ci
    ) {
        if(renderLayer == null) return;

        RenderLayerAccessor playerSkinCacheEntryAccessor = (RenderLayerAccessor) renderLayer;
        RenderSetup renderSetup = playerSkinCacheEntryAccessor.getRenderSetup();
        if (renderSetup == null) return;

        Identifier texture = getTextureLocation(renderSetup, "Sampler0");
        if (texture == null) return;

        boolean success = PlayerHeadRenderer.render(
            texture,
            matrices,
            facing,
            light,
            yaw
        );

        if(success) ci.cancel();
    }
}
