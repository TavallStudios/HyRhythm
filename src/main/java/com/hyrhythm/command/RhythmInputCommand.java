package com.hyrhythm.command;

import com.hyrhythm.gameplay.interfaces.RhythmGameplayAccess;
import com.hyrhythm.gameplay.model.RhythmLaneInputAction;
import com.hyrhythm.session.interfaces.RhythmSessionAccess;
import com.hypixel.hytale.server.core.command.system.CommandContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class RhythmInputCommand extends AbstractRhythmCommand implements RhythmGameplayAccess, RhythmSessionAccess {
    public RhythmInputCommand() {
        super("input", "Submit a debug lane input into active gameplay.", true);
    }

    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext context) {
        List<String> tokens = trailingTokens(context);
        if (tokens.size() != 3) {
            sendError(context, "Usage: /rhythm input <down|up> <lane> <songTimeMs>");
            return completed();
        }

        try {
            RhythmLaneInputAction action = RhythmLaneInputAction.parse(tokens.getFirst());
            int lane = Integer.parseInt(tokens.get(1));
            long songTimeMillis = Long.parseLong(tokens.get(2));

            var result = submitRhythmLaneInput(
                context.sender().getUuid(),
                context.sender().getDisplayName(),
                action,
                lane,
                songTimeMillis
            );
            if (result.judgment() == null) {
                sendInfo(context, "Input applied with no judgment. " + result.debugSummary());
            } else {
                sendSuccess(context, result.debugSummary());
            }

            logCommandUsage(
                context,
                "input_completed",
                new LinkedHashMap<>() {{
                    put("action", action);
                    put("lane", lane);
                    put("songTimeMs", songTimeMillis);
                    put("result", result.judgment() == null ? "none" : result.judgment().type());
                    put("debugSummary", result.debugSummary());
                }}
            );
        } catch (IllegalArgumentException | IllegalStateException exception) {
            String failureMessage = describeInputFailure(context, exception.getMessage());
            sendError(context, failureMessage);
            logRhythmWarn(
                "command",
                "input_failed",
                new LinkedHashMap<>() {{
                    put("command", "rhythm");
                    put("subcommand", getName());
                    put("sender", context.sender().getDisplayName());
                    put("senderId", context.sender().getUuid());
                    put("reason", failureMessage);
                }}
            );
        }
        return completed();
    }

    private String describeInputFailure(CommandContext context, String message) {
        if (!"No active gameplay session. Use /rhythm start first.".equals(message)) {
            return message;
        }

        return findRhythmSession(context.sender().getUuid())
            .map(session -> message + " Current session: " + session.summary())
            .orElse(message);
    }
}
