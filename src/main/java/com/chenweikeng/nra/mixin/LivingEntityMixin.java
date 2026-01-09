package com.chenweikeng.nra.mixin;

import com.chenweikeng.nra.NotRidingAlertClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(
        method = "hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z",
        at = @At("RETURN"),
        cancellable = true
    )
    private void injectBlindnessWhenRiding(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<Boolean> cir) {
        if (NotRidingAlertClient.getConfig().isBlindWhenRiding() 
            && effect == StatusEffects.BLINDNESS 
            && NotRidingAlertClient.isRiding()) {
            cir.setReturnValue(true);
        }
    }
    
    @Inject(
        method = "getStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Lnet/minecraft/entity/effect/StatusEffectInstance;",
        at = @At("RETURN"),
        cancellable = true
    )
    private void injectBlindnessInstanceWhenRiding(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<StatusEffectInstance> cir) {
        if (NotRidingAlertClient.getConfig().isBlindWhenRiding() 
            && effect == StatusEffects.BLINDNESS 
            && NotRidingAlertClient.isRiding()) {
            if (cir.getReturnValue() == null) {
                cir.setReturnValue(new StatusEffectInstance(
                    StatusEffects.BLINDNESS,
                    -1
                ));
            }
        }
    }
}

