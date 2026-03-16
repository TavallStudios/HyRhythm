package com.hyrhythm.command;

import com.hyrhythm.gameplay.interfaces.RhythmGameplayAccess;
import com.hyrhythm.player.interfaces.RhythmPlayerTargetAccess;
import com.hyrhythm.session.interfaces.RhythmSessionAccess;
import com.hyrhythm.settings.interfaces.RhythmSettingsAccess;
import com.hypixel.hytale.server.core.command.system.CommandContext;

import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;

public final class RhythmStateCommand extends AbstractRhythmCommand implements
    RhythmSessionAccess,
    RhythmSettingsAccess,
    RhythmGameplayAccess,
    RhythmPlayerTargetAccess {
    public RhythmStateCommand() {
        super("state", "Show current rhythm session state.", true);
    }

    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext context) {
        try {
            String targetLookup = optionalSingleArgument(context);
            if ("".equals(targetLookup)) {
                sendError(context, "Usage: /rhythm state [player]");
                return completed();
            }

            var target = resolveTarget(context, targetLookup);
            String settings = describeRhythmSettings(target.playerId(), target.playerName());
            String session = findRhythmSession(target.playerId())
                .map(value -> value.summary())
                .orElse("session=none phase=NONE chart=none");
            String gameplay = findActiveRhythmGameplay(target.playerId())
                .map(value -> value.summary())
                .orElseGet(() -> findLastRhythmGameplay(target.playerId())
                    .map(value -> value.summary())
                    .orElse("gameplay=none"));

            sendInfo(
                context,
                "target=" + target.playerName()
                    + " | debug=" + (isRhythmDebugEnabled() ? "on" : "off")
                    + " | " + settings + " | " + session + " | " + gameplay
            );
            logCommandUsage(
                context,
                "state_requested",
                new LinkedHashMap<>() {{
                    put("targetPlayer", target.playerName());
                    put("targetPlayerId", target.playerId());
                    put("debug", isRhythmDebugEnabled());
                    put("session", session);
                    put("gameplay", gameplay);
                }}
            );
        } catch (IllegalStateException exception) {
            sendError(context, exception.getMessage());
            logRhythmWarn(
                "command",
                "state_failed",
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
