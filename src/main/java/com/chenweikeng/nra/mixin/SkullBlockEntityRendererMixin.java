package com.chenweikeng.nra.mixin;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.chenweikeng.nra.playerheads.PlayerHeadRenderer;

import net.minecraft.block.SkullBlock;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.render.block.entity.state.SkullBlockEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.PlayerSkinCache;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;

// Code from Camzanno
@Mixin(SkullBlockEntityRenderer.class)
public abstract class SkullBlockEntityRendererMixin {
    @Shadow
    private PlayerSkinCache skinCache;
    
    @Unique
    private static final Set<String> imaginefun$loggedStaticRenderCallers = new HashSet<>();
    
    @Unique
    private static final Set<Class<?>> imaginefun$loggedTextureSpecClasses = new HashSet<>();
    
    @Unique
    private static final Set<String> imaginefun$loggedReflectionErrors = new HashSet<>();
    
    @Inject(
        method = "render(Lnet/minecraft/client/render/block/entity/state/SkullBlockEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onRender(
		SkullBlockEntityRenderState skullBlockEntityRenderState,
		MatrixStack matrixStack,
		OrderedRenderCommandQueue orderedRenderCommandQueue,
		CameraRenderState cameraRenderState,
        CallbackInfo ci
    ) {
        if(skullBlockEntityRenderState.skullType != SkullBlock.Type.PLAYER) return;        
        
        RenderLayer renderLayer = skullBlockEntityRenderState.renderLayer;
        if(renderLayer == null) return;

        RenderLayerAccessor playerSkinCacheEntryAccessor = (RenderLayerAccessor) renderLayer;
        RenderSetup renderSetup = playerSkinCacheEntryAccessor.getRenderSetup();
        if (renderSetup == null) return;

        Identifier texture = getTextureLocation(renderSetup, "Sampler0");
        if (texture == null) return;

        boolean success = PlayerHeadRenderer.render(
            texture,
            matrixStack,
            skullBlockEntityRenderState.facing,
            skullBlockEntityRenderState.lightmapCoordinates,
            skullBlockEntityRenderState.yaw
        );

        if(success) ci.cancel();
    }

    @Unique
    private Identifier getTextureLocation(RenderSetup renderSetup, String textureKey) {
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
}
