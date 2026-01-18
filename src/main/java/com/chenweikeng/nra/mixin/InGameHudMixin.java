package com.chenweikeng.nra.mixin;

import com.chenweikeng.nra.ride.CurrentRideHolder;
import com.chenweikeng.nra.ride.LastRideHolder;
import com.chenweikeng.nra.ride.RideName;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow
    private static Comparator<?> SCOREBOARD_ENTRY_COMPARATOR;

    @Inject(
        at = @At("HEAD"),
        method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"
    )
    private void onRenderScoreboardSidebar(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
        Scoreboard scoreboard = objective.getScoreboard();

        @SuppressWarnings("unchecked")
        Comparator<Object> comparator = (Comparator<Object>) SCOREBOARD_ENTRY_COMPARATOR;

        final String[] prefix8 = { null };
        final String[] prefix9 = { null };
        scoreboard.getScoreboardEntries(objective).stream()
            .filter(score -> !score.hidden())
            .sorted(comparator)
            .limit(15L)
            .forEach(scoreboardEntry -> {
                Team team = scoreboard.getScoreHolderTeam(scoreboardEntry.owner());
                if (team == null) return;
                String name = team.getName();
                Text prefixText = team.getPrefix();
                String prefixStr = prefixText != null ? prefixText.getString() : "null";
                if ("§8".equals(name)) prefix8[0] = prefixStr;
                else if ("§9".equals(name)) prefix9[0] = prefixStr;
            });

        // Only analyze when §8 prefix contains "Current Ride"
        if (prefix8[0] == null) {
            CurrentRideHolder.setCurrentRide(null);
            return;
        }
        if (!prefix8[0].contains("Current Ride")) {
            CurrentRideHolder.setCurrentRide(null);
            return;
        }
        if (prefix9[0] == null) {
            CurrentRideHolder.setCurrentRide(null);
            return;
        }

        RideName resolved = RideName.fromTruncatedString(prefix9[0]);
        CurrentRideHolder.setCurrentRide(resolved);
        LastRideHolder.setLastRide(resolved);
    }
}