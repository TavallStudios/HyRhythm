package com.hyrhythm.command;

import com.hyrhythm.player.interfaces.RhythmPlayerTargetAccess;
import com.hyrhythm.session.interfaces.RhythmSessionAccess;
import com.hyrhythm.session.model.RhythmSessionSnapshot;
import com.hypixel.hytale.server.core.command.system.CommandContext;

import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;

public final class RhythmJoinCommand extends AbstractRhythmCommand implements RhythmSessionAccess, RhythmPlayerTargetAccess {
    public RhythmJoinCommand() {
        super("join", "Join or create a rhythm match.", true);
    }

    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext context) {
        try {
            String targetLookup = optionalSingleArgument(context);
            if ("".equals(targetLookup)) {
                sendError(context, "Usage: /rhythm join [player]");
                return completed();
            }

            var target = resolveTarget(context, targetLookup);
            RhythmSessionSnapshot session = joinOrCreateRhythmSession(
                target.playerId(),
                target.playerName()
            );
            sendSuccess(
                context,
                "Joined rhythm session " + session.sessionId() + " for " + target.playerName() + ". Next: /rhythm ui or /rhythm test."
            );
            logCommandUsage(
                context,
                "join_completed",
                new LinkedHashMap<>() {{
                    put("targetPlayer", target.playerName());
                    put("targetPlayerId", target.playerId());
                    put("sessionId", session.sessionId());
                    put("phase", session.phase());
                }}
            );
        } catch (IllegalStateException exception) {
            sendError(context, exception.getMessage());
            logRhythmWarn(
                "command",
                "join_failed",
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
