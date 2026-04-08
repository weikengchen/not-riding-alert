package com.chenweikeng.nra.mixin;

import com.chenweikeng.nra.GameState;
import com.chenweikeng.nra.ServerState;
import com.chenweikeng.nra.config.FullbrightMode;
import com.chenweikeng.nra.config.ModConfig;
import com.chenweikeng.nra.handler.FireworkViewingHandler;
import net.minecraft.client.ClientClockManager;
import net.minecraft.core.Holder;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientClockManager.class)
public class ClientClockManagerMixin {
  private static final long NOON = 6000L;
  private static final long SUNSET_START = 12000L;
  private static final long NIGHT = 13000L;
  private static final long DAY_LENGTH = 24000L;

  @Inject(method = "getTotalTicks", at = @At("RETURN"), cancellable = true)
  private void onGetTotalTicks(Holder<WorldClock> definition, CallbackInfoReturnable<Long> cir) {
    if (!ServerState.isImagineFunServer()) {
      return;
    }
    if (!definition.is(WorldClocks.OVERWORLD)) {
      return;
    }

    long ticks = cir.getReturnValue();
    long phase = ticks % DAY_LENGTH;
    long dayBase = (ticks / DAY_LENGTH) * DAY_LENGTH;

    if (FireworkViewingHandler.getInstance().isViewingFirework()) {
      if (phase < NIGHT) {
        cir.setReturnValue(dayBase + NIGHT);
      }
      return;
    }

    boolean isRiding = GameState.getInstance().isRiding();
    FullbrightMode mode = ModConfig.currentSetting.fullbrightMode;
    boolean shouldApplyFullbright =
        switch (mode) {
          case NONE -> false;
          case ONLY_WHEN_RIDING -> isRiding;
          case ONLY_WHEN_NOT_RIDING -> !isRiding;
          case ALWAYS -> true;
        };

    if (shouldApplyFullbright && phase >= SUNSET_START) {
      cir.setReturnValue(dayBase + NOON);
    }
  }
}
