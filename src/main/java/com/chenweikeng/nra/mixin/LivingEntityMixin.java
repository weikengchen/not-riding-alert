package com.chenweikeng.nra.mixin;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.chenweikeng.nra.config.FullbrightMode;
import com.chenweikeng.nra.config.ModConfig;
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
      cancellable = true)
  private void injectBlindnessWhenRiding(
      Holder<MobEffect> effect, CallbackInfoReturnable<Boolean> cir) {
    if (!NotRidingAlertClient.isImagineFunServer()) {
      return;
    }
    LivingEntity entity = (LivingEntity) (Object) this;
    if (entity instanceof net.minecraft.client.player.LocalPlayer player) {
      boolean isRiding = NotRidingAlertClient.isRiding(player);
      if (ModConfig.getInstance().blindWhenRiding && effect == MobEffects.BLINDNESS && isRiding) {
        cir.setReturnValue(true);
      } else if (effect == MobEffects.NIGHT_VISION) {
        FullbrightMode mode = ModConfig.getInstance().fullbrightMode;
        boolean shouldHaveFullbright =
            switch (mode) {
              case NONE -> false;
              case ONLY_WHEN_RIDING -> isRiding;
              case ONLY_WHEN_NOT_RIDING -> !isRiding;
              case ALWAYS -> true;
            };
        if (shouldHaveFullbright) {
          cir.setReturnValue(true);
        }
      }
    }
  }

  @Inject(
      method =
          "getEffect(Lnet/minecraft/core/Holder;)Lnet/minecraft/world/effect/MobEffectInstance;",
      at = @At("RETURN"),
      cancellable = true)
  private void injectBlindnessInstanceWhenRiding(
      Holder<MobEffect> effect, CallbackInfoReturnable<MobEffectInstance> cir) {
    if (!NotRidingAlertClient.isImagineFunServer()) {
      return;
    }
    LivingEntity entity = (LivingEntity) (Object) this;
    if (entity instanceof net.minecraft.client.player.LocalPlayer player) {
      boolean isRiding = NotRidingAlertClient.isRiding(player);
      if (ModConfig.getInstance().blindWhenRiding && effect == MobEffects.BLINDNESS && isRiding) {
        if (cir.getReturnValue() == null) {
          cir.setReturnValue(new MobEffectInstance(MobEffects.BLINDNESS, -1));
        }
      } else if (effect == MobEffects.NIGHT_VISION) {
        FullbrightMode mode = ModConfig.getInstance().fullbrightMode;
        boolean shouldHaveFullbright =
            switch (mode) {
              case NONE -> false;
              case ONLY_WHEN_RIDING -> isRiding;
              case ONLY_WHEN_NOT_RIDING -> !isRiding;
              case ALWAYS -> true;
            };
        if (shouldHaveFullbright && cir.getReturnValue() == null) {
          cir.setReturnValue(new MobEffectInstance(MobEffects.NIGHT_VISION, -1));
        }
      }
    }
  }
}
