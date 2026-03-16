package com.hyrhythm.command;

import com.hypixel.hytale.server.core.command.system.CommandContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class RhythmDebugCommand extends AbstractRhythmCommand {
    public RhythmDebugCommand() {
        super("debug", "Toggle rhythm debug logging.", true);
    }

    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext context) {
        List<String> tokens = trailingTokens(context);
        if (tokens.isEmpty()) {
            sendInfo(context, "Rhythm debug is " + (isRhythmDebugEnabled() ? "on" : "off") + ".");
            logCommandUsage(context, "debug_status", new LinkedHashMap<>() {{
                put("enabled", isRhythmDebugEnabled());
            }});
            return completed();
        }

        String mode = tokens.getFirst().toLowerCase();
        if (!"on".equals(mode) && !"off".equals(mode)) {
            sendError(context, "Usage: /rhythm debug <on|off>");
            return completed();
        }

        boolean enabled = "on".equals(mode);
        setRhythmDebugEnabled(enabled);
        sendSuccess(context, "Rhythm debug set to " + mode + ".");
        logRhythmInfo(
            "command",
            "debug_toggle",
            new LinkedHashMap<>() {{
                put("command", "rhythm");
                put("subcommand", getName());
                put("sender", context.sender().getDisplayName());
                put("senderId", context.sender().getUuid());
                put("enabled", enabled);
            }}
        );
        return completed();
    }
}
