package com.hyrhythm.command;

import com.hyrhythm.player.interfaces.RhythmPlayerTargetAccess;
import com.hyrhythm.player.model.RhythmPlayerTarget;
import com.hyrhythm.session.interfaces.RhythmSessionAccess;
import com.hyrhythm.session.model.RhythmSessionSnapshot;
import com.hyrhythm.ui.interfaces.RhythmUiAccess;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;

public final class RhythmGameplayCommand extends AbstractRhythmCommand implements
    RhythmSessionAccess,
    RhythmUiAccess,
    RhythmPlayerTargetAccess {
    public RhythmGameplayCommand() {
        super("gameplay", "Open the rhythm gameplay UI for an active session.", true);
        addAliases("play");
    }

    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext context) {
        try {
            String targetLookup = optionalSingleArgument(context);
            if ("".equals(targetLookup)) {
                sendError(context, "Usage: /rhythm gameplay [player]");
                return completed();
            }

            RhythmPlayerTarget target = resolveTarget(context, targetLookup);
            Player player = target.player();
            if (player == null) {
                throw new IllegalStateException("Player '" + target.playerName() + "' is not online and ready for gameplay UI.");
            }

            openRhythmGameplay(player);
            RhythmSessionSnapshot session = findRhythmSession(player.getUuid()).orElse(null);
            sendSuccess(
                context,
                session == null
                    ? "Opening rhythm gameplay UI for " + target.playerName() + "."
                    : "Opening rhythm gameplay UI for " + target.playerName() + " in session " + session.sessionId() + "."
            );
            logCommandUsage(
                context,
                "gameplay_ui_requested",
                new LinkedHashMap<>() {{
                    put("targetPlayer", target.playerName());
                    put("targetPlayerId", target.playerId());
                    put("sessionId", session == null ? "none" : session.sessionId());
                    put("phase", session == null ? "unknown" : session.phase());
                    put("chartId", session == null || session.chartId() == null ? "none" : session.chartId());
                }}
            );
        } catch (IllegalStateException exception) {
            sendError(context, exception.getMessage());
            logRhythmWarn(
                "command",
                "gameplay_ui_failed",
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

    private RhythmPlayerTarget resolveTarget(CommandContext context, String targetLookup) {
        if (targetLookup == null || targetLookup.isBlank()) {
            return senderTarget(context);
        }
        return findRhythmOnlinePlayer(targetLookup)
            .orElseThrow(() -> new IllegalStateException("Player '" + targetLookup + "' is not online."));
    }
}
