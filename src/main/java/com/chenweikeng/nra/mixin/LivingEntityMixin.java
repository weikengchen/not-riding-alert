package com.chenweikeng.nra.mixin;

import com.chenweikeng.nra.NotRidingAlertClient;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(
        method = "hasEffect(Lnet/minecraft/core/Holder;)Z",
        at = @At("RETURN"),
        cancellable = true
    )
    private void injectBlindnessWhenRiding(Holder<MobEffect> effect, CallbackInfoReturnable<Boolean> cir) {
        if (NotRidingAlertClient.getConfig().isBlindWhenRiding() 
            && effect == MobEffects.BLINDNESS 
            && NotRidingAlertClient.isRiding()) {
            cir.setReturnValue(true);
        }
    }
    
    @Inject(
        method = "getEffect(Lnet/minecraft/core/Holder;)Lnet/minecraft/world/effect/MobEffectInstance;",
        at = @At("RETURN"),
        cancellable = true
    )
    private void injectBlindnessInstanceWhenRiding(Holder<MobEffect> effect, CallbackInfoReturnable<MobEffectInstance> cir) {
        if (NotRidingAlertClient.getConfig().isBlindWhenRiding() 
            && effect == MobEffects.BLINDNESS 
            && NotRidingAlertClient.isRiding()) {
            if (cir.getReturnValue() == null) {
                cir.setReturnValue(new MobEffectInstance(
                    MobEffects.BLINDNESS,
                    -1
                ));
            }
        }
    }
}

