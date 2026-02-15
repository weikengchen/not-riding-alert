package com.chenweikeng.nra.util;

import com.chenweikeng.nra.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class SoundHelper {
  public static void playConfiguredSound(Minecraft client) {
    if (client.player == null || client.level == null) {
      return;
    }

    try {
      String soundId = ModConfig.getInstance().soundId;
      Identifier soundIdentifier = Identifier.parse(soundId);

      SoundEvent soundEvent = null;
      try {
        var registry = client.level.registryAccess().lookupOrThrow(Registries.SOUND_EVENT);
        soundEvent = registry.getValue(soundIdentifier);
      } catch (Exception e) {
        // If not found, use default
      }

      if (soundEvent == null) {
        soundEvent = SoundEvents.EXPERIENCE_ORB_PICKUP;
      }

      client.level.playSound(
          client.player,
          client.player.getX(),
          client.player.getY(),
          client.player.getZ(),
          soundEvent,
          SoundSource.MASTER,
          1.0f,
          1.0f);
    } catch (Exception e) {
      if (client.player != null && client.level != null) {
        client.level.playSound(
            client.player,
            client.player.getX(),
            client.player.getY(),
            client.player.getZ(),
            SoundEvents.EXPERIENCE_ORB_PICKUP,
            SoundSource.MASTER,
            1.0f,
            1.0f);
      }
    }
  }
}
