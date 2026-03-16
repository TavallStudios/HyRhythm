package com.hyrhythm.command;

import com.hyrhythm.player.interfaces.RhythmPlayerTargetAccess;
import com.hyrhythm.session.interfaces.RhythmSessionAccess;
import com.hyrhythm.ui.interfaces.RhythmUiAccess;
import com.hyrhythm.session.model.RhythmSessionSnapshot;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.command.system.CommandContext;

import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;

public final class RhythmStartCommand extends AbstractRhythmCommand implements RhythmSessionAccess, RhythmUiAccess, RhythmPlayerTargetAccess {
    public RhythmStartCommand() {
        super("start", "Start the active rhythm match.", true);
    }

    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext context) {
        try {
            String targetLookup = optionalSingleArgument(context);
            if ("".equals(targetLookup)) {
                sendError(context, "Usage: /rhythm start [player]");
                return completed();
            }

            var target = resolveTarget(context, targetLookup);
            RhythmSessionSnapshot session = startRhythmSession(
                target.playerId(),
                target.playerName()
            );

            String successMessage = "Started rhythm session " + session.sessionId()
                + " with chart " + session.chartId() + " for " + target.playerName() + ".";
            Player player = target.player();
            if (player != null) {
                try {
                    openRhythmGameplay(player);
                    successMessage += " Opening gameplay UI.";
                } catch (IllegalStateException exception) {
                    successMessage += " Gameplay UI could not open: " + exception.getMessage()
                        + " Use /rhythm input <down|up> <lane> <songTimeMs> as fallback.";
                    logRhythmWarn(
                        "ui",
                        "gameplay_ui_start_open_failed",
                        new LinkedHashMap<>() {{
                            put("sessionId", session.sessionId());
                            put("chartId", session.chartId());
                            put("targetPlayer", target.playerName());
                            put("targetPlayerId", target.playerId());
                            put("reason", exception.getMessage());
                        }}
                    );
                }
            } else {
                successMessage += " Use /rhythm input <down|up> <lane> <songTimeMs> or the gameplay UI when available.";
            }
            sendSuccess(
                context,
                successMessage
            );
            logCommandUsage(
                context,
                "start_completed",
                new LinkedHashMap<>() {{
                    put("targetPlayer", target.playerName());
                    put("targetPlayerId", target.playerId());
                    put("sessionId", session.sessionId());
                    put("chartId", session.chartId());
                    put("phase", session.phase());
                }}
            );
        } catch (IllegalStateException exception) {
            sendError(context, exception.getMessage());
            logRhythmWarn(
                "command",
                "start_failed",
                new LinkedHashMap<>() {{
                    put("command", "rhythm");
                    put("subcommand", getName());
                    put("sender", context.sender().getDisplayName());
                    put("senderId", context.sender().getUuid());
                    put("reason", exception.getMessage());
                }}
            );
        }
        return completed();
    }

    private com.hyrhythm.player.model.RhythmPlayerTarget resolveTarget(CommandContext context, String targetLookup) {
        if (targetLookup == null || targetLookup.isBlank()) {
            return senderTarget(context);
        }
        return findRhythmOnlinePlayer(targetLookup)
            .orElseThrow(() -> new IllegalStateException("Player '" + targetLookup + "' is not online."));
    }
}
