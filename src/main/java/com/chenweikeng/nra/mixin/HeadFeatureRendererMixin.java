package com.chenweikeng.nra.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.chenweikeng.nra.playerheads.PlayerHeadRenderer;

import net.minecraft.block.SkullBlock;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.texture.PlayerSkinCache;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo.TextureAsset;

@Mixin(HeadFeatureRenderer.class)
public abstract class HeadFeatureRendererMixin {
    @Shadow
	private PlayerSkinCache skinCache;
    
    @Inject(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/client/render/entity/state/LivingEntityRenderState;FF)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onRender(
        MatrixStack matrixStack,
        OrderedRenderCommandQueue orderedRenderCommandQueue,
        int i,
        LivingEntityRenderState livingEntityRenderState,
        float f,
        float g,
        CallbackInfo ci
    ) {
        if(livingEntityRenderState.wearingSkullType != SkullBlock.Type.PLAYER) return;

        PlayerSkinCache.Entry entry = skinCache.get(livingEntityRenderState.wearingSkullProfile);
        if(entry == null) return;

        SkinTextures skin = entry.getTextures();
        if(skin == null) return;

        TextureAsset bodyTexture = skin.body();
        if(bodyTexture == null) return;

        Identifier skinTexture = bodyTexture.texturePath();
        if(skinTexture == null) return;
        
        boolean success = PlayerHeadRenderer.render(
            skinTexture,
            matrixStack,
            null,
            i,
            180.0F
        );
        if(success) ci.cancel();
    }
}
