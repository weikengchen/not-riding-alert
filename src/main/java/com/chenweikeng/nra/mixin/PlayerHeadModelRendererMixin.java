package com.chenweikeng.nra.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.chenweikeng.nra.playerheads.PlayerHeadRenderer;

import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.model.special.PlayerHeadModelRenderer;
import net.minecraft.client.texture.PlayerSkinCache;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.Identifier;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo.TextureAsset;

@Mixin(PlayerHeadModelRenderer.class)
public abstract class PlayerHeadModelRendererMixin {
    
    @Inject(
        method = "render(Lnet/minecraft/client/texture/PlayerSkinCache$Entry;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IIZI)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onRender(
        PlayerSkinCache.Entry entry,
        ItemDisplayContext itemDisplayContext,
        MatrixStack matrixStack,
        OrderedRenderCommandQueue orderedRenderCommandQueue,
        int i,
        int j,
        boolean bl,
        int k,
        CallbackInfo ci
    ) {
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
