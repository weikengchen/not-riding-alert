package com.chenweikeng.nra.mixin;

import com.chenweikeng.nra.ride.RideCountManager;
import com.chenweikeng.nra.ride.RideName;
import com.chenweikeng.nra.strategy.StrategyHudRenderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    private static final Pattern TOTAL_RIDES_PATTERN = Pattern.compile("Total rides:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    
    @Inject(at = @At("HEAD"), method = "onInventory")
    public void onInventory(InventoryS2CPacket inventoryS2CPacket, CallbackInfo ci) {
        var player = MinecraftClient.getInstance().player;
        if (player != null) {
            List<ItemStack> itemStacks = inventoryS2CPacket.contents();

            // Check if we have at least 4 items (indices 0-3)
            if (itemStacks.size() < 4) {
                return;
            }

            // Check if 2nd item (index 1) has CUSTOM_NAME "Disneyland"
            Text secondItemName = itemStacks.get(1).getComponents().get(DataComponentTypes.CUSTOM_NAME);
            if (secondItemName == null || !"Disneyland".equals(secondItemName.getString())) {
                return;
            }

            // Check if 3rd item (index 2) has CUSTOM_NAME "DCA"
            Text thirdItemName = itemStacks.get(2).getComponents().get(DataComponentTypes.CUSTOM_NAME);
            if (thirdItemName == null || !"DCA".equals(thirdItemName.getString())) {
                return;
            }

            // Check if 4th item (index 3) has CUSTOM_NAME "Retro"
            Text fourthItemName = itemStacks.get(3).getComponents().get(DataComponentTypes.CUSTOM_NAME);
            if (fourthItemName == null || !"Retro".equals(fourthItemName.getString())) {
                return;
            }

            // All conditions met, now process items
            RideCountManager countManager = RideCountManager.getInstance();
            for (ItemStack itemStack : itemStacks) {
                // Get CUSTOM_NAME
                Text customName = itemStack.getComponents().get(DataComponentTypes.CUSTOM_NAME);
                
                // Skip if CUSTOM_NAME is null
                if (customName == null) {
                    continue;
                }
                
                String nameString = customName.getString();
                
                // Skip if CUSTOM_NAME is "Overall"
                if ("Overall".equals(nameString)) {
                    continue;
                }

                // Get LORE
                LoreComponent itemLore = itemStack.getComponents().get(DataComponentTypes.LORE);
                if (itemLore != null) {
                    List<Text> lines = itemLore.lines();
                    // Check if we have at least 2 lines (indices 0 and 1)
                    if (lines.size() >= 2) {
                        String secondLine = lines.get(1).getString();
                        // Try to extract number from "Total rides: [NUM]"
                        Matcher matcher = TOTAL_RIDES_PATTERN.matcher(secondLine);
                        if (matcher.find()) {
                            try {
                                int count = Integer.parseInt(matcher.group(1));
                                RideName ride = RideName.fromString(nameString);
                                countManager.updateRideCount(ride, count);
                            } catch (NumberFormatException e) {
                                // Ignore invalid numbers
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Inject(at = @At("HEAD"), method = "onScreenHandlerSlotUpdate")
    public void onScreenHandlerSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
        var player = MinecraftClient.getInstance().player;
        if (player != null) {
            // ScreenHandlerSlotUpdateS2CPacket contains a single ItemStack (stack field)
            ItemStack itemStack = packet.getStack();
            if (itemStack == null || itemStack.isEmpty()) {
                return;
            }
            
            // Get CUSTOM_NAME
            Text customName = itemStack.getComponents().get(DataComponentTypes.CUSTOM_NAME);
            
            // Skip if CUSTOM_NAME is null
            if (customName == null) {
                return;
            }
            
            String nameString = customName.getString();
            
            // Skip if CUSTOM_NAME is "Overall"
            if ("Overall".equals(nameString)) {
                return;
            }
            
            // Get LORE
            LoreComponent itemLore = itemStack.getComponents().get(DataComponentTypes.LORE);
            if (itemLore != null) {
                List<Text> lines = itemLore.lines();
                // Check if we have more than 2 lines
                if (lines.size() > 2) {
                    // Check if 2nd line (index 1) matches the total rides pattern
                    String secondLine = lines.get(1).getString();
                    Matcher matcher = TOTAL_RIDES_PATTERN.matcher(secondLine);
                    if (matcher.find()) {
                        try {
                            int count = Integer.parseInt(matcher.group(1));
                            RideName ride = RideName.fromString(nameString);
                            if (ride == RideName.UNKNOWN) {
                                StrategyHudRenderer.setError("Unknown ride detected (name: " + nameString + ")");
                            }
                            RideCountManager countManager = RideCountManager.getInstance();
                            countManager.updateRideCount(ride, count);
                        } catch (NumberFormatException e) {
                            // Ignore invalid numbers
                        }
                    }
                }
            }
        }
    }

}
