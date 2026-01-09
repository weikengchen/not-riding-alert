package com.chenweikeng.nra;

import com.chenweikeng.nra.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import com.chenweikeng.nra.command.SetSoundCommand;
import com.chenweikeng.nra.command.ToggleAlertCommand;
import com.chenweikeng.nra.command.ToggleBlindWhenRidingCommand;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotRidingAlertClient implements ClientModInitializer {
    public static final String MOD_ID = "not-riding-alert";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static ModConfig config;
    private int tickCounter = 0;
    private static final int CHECK_INTERVAL = 200; // Check every 200 ticks (10 seconds)
    private static boolean isRiding = false; // Cached riding state
    
    @Override
    public void onInitializeClient() {
        config = ModConfig.load();
        LOGGER.info("Not Riding Alert client initialized");
        
        // Register tick event to update cached riding state and check periodically (for sound alert)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                isRiding = false;
                return;
            }
            
            // Update cached riding state every tick
            isRiding = client.player.hasVehicle();
            
            tickCounter++;
            if (tickCounter >= CHECK_INTERVAL) {
                tickCounter = 0;
                checkRiding(client);
            }
        });
        
        // Register commands
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            SetSoundCommand.register(dispatcher);
            ToggleAlertCommand.register(dispatcher);
            ToggleBlindWhenRidingCommand.register(dispatcher);
        });
    }
    
    private void checkRiding(MinecraftClient client) {
        if (client.player == null) {
            return;
        }
        
        // Check if feature is enabled
        if (!config.isEnabled()) {
            return;
        }
        
        // Play sound when player is NOT riding (keep playing every check)
        if (!isRiding) {
            playSound(client);
        }
    }
    
    
    private void playSound(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        
        try {
            String soundId = config.getSoundId();
            Identifier soundIdentifier = Identifier.of(soundId);
            
            // Try to get the sound event from the registry
            SoundEvent soundEvent = null;
            try {
                // Try to get from sound registry
                var registry = client.world.getRegistryManager().getOrThrow(net.minecraft.registry.RegistryKeys.SOUND_EVENT);
                soundEvent = registry.get(soundIdentifier);
            } catch (Exception e) {
                // If not found, try common sound events
            }
            
            // Fallback to default if not found
            if (soundEvent == null) {
                soundEvent = SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
            }
            
            // Play sound at player's position
            client.world.playSound(
                client.player,
                client.player.getX(),
                client.player.getY(),
                client.player.getZ(),
                soundEvent,
                SoundCategory.MASTER,
                1.0f,
                1.0f
            );
        } catch (Exception e) {
            // Fallback to default sound
            if (client.player != null && client.world != null) {
                SoundEvent fallbackSound = SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
                client.world.playSound(
                    client.player,
                    client.player.getX(),
                    client.player.getY(),
                    client.player.getZ(),
                    fallbackSound,
                    SoundCategory.MASTER,
                    1.0f,
                    1.0f
                );
            }
        }
    }
    
    public static ModConfig getConfig() {
        return config;
    }
    
    public static boolean isRiding() {
        return isRiding;
    }
}

