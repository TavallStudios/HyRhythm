package com.hyrhythm.command;

import com.hyrhythm.gameplay.interfaces.RhythmGameplayAccess;
import com.hypixel.hytale.server.core.command.system.CommandContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class RhythmAdvanceCommand extends AbstractRhythmCommand implements RhythmGameplayAccess {
    public RhythmAdvanceCommand() {
        super("advance", "Advance active gameplay time for debug playback.", true);
    }

    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext context) {
        List<String> tokens = trailingTokens(context);
        if (tokens.size() != 1) {
            sendError(context, "Usage: /rhythm advance <songTimeMs>");
            return completed();
        }

        try {
            long songTimeMillis = Long.parseLong(tokens.getFirst());
            var snapshot = advanceRhythmGameplay(
                context.sender().getUuid(),
                context.sender().getDisplayName(),
                songTimeMillis
            );
            sendSuccess(context, "Advanced gameplay: " + snapshot.summary());
            logCommandUsage(
                context,
                "advance_completed",
                new LinkedHashMap<>() {{
                    put("songTimeMs", songTimeMillis);
                    put("gameplay", snapshot.summary());
                }}
            );
        } catch (IllegalArgumentException | IllegalStateException exception) {
            sendError(context, exception.getMessage());
            logRhythmWarn(
                "command",
                "advance_failed",
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
}
