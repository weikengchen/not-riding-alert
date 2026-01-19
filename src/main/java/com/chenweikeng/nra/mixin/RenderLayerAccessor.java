package com.chenweikeng.nra.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;

@Mixin(RenderLayer.class)
public interface RenderLayerAccessor {
    /**
     * Accessor to get the renderSetup field from PlayerSkinCache.Entry.
     * Usage: ((PlayerSkinCacheEntryAccessor) entry).getRenderSetup()
     */
    @Accessor("renderSetup")
    RenderSetup getRenderSetup();
}
