package com.hyrhythm.command;

import com.hyrhythm.debug.interfaces.RhythmDebugAccess;
import com.hyrhythm.logging.interfaces.RhythmLoggingAccess;
import com.hyrhythm.player.model.RhythmPlayerTarget;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

abstract class AbstractRhythmCommand extends AbstractAsyncCommand implements RhythmLoggingAccess, RhythmDebugAccess {
    protected AbstractRhythmCommand(String name, String description, boolean allowsExtraArguments) {
        super(name, description);
        setAllowsExtraArguments(allowsExtraArguments);
    }

    protected static CompletableFuture<Void> completed() {
        return CompletableFuture.completedFuture(null);
    }

    protected void sendInfo(CommandContext context, String message) {
        context.sendMessage(Message.raw(message).color("yellow"));
    }

    protected void sendSuccess(CommandContext context, String message) {
        context.sendMessage(Message.raw(message).color("green"));
    }

    protected void sendError(CommandContext context, String message) {
        context.sendMessage(Message.raw(message).color("red"));
    }

    protected String optionalSingleArgument(CommandContext context) {
        List<String> tokens = trailingTokens(context);
        if (tokens.isEmpty()) {
            return null;
        }
        return tokens.size() == 1 ? tokens.getFirst() : "";
    }

    protected RhythmPlayerTarget senderTarget(CommandContext context) {
        return new RhythmPlayerTarget(
            context.sender().getUuid(),
            context.sender().getDisplayName(),
            context.sender() instanceof Player player ? player : null
        );
    }

    protected List<String> trailingTokens(CommandContext context) {
        String input = context.getInputString();
        if (input == null || input.isBlank()) {
            return List.of();
        }

        String[] rawTokens = input.trim().split("\\s+");
        int skipIndex = -1;
        for (int index = 0; index < rawTokens.length; index++) {
            if (getName().equalsIgnoreCase(rawTokens[index])) {
                skipIndex = index;
            }
        }

        List<String> tokens = new ArrayList<>();
        for (int index = skipIndex + 1; index < rawTokens.length; index++) {
            tokens.add(rawTokens[index]);
        }
        if (skipIndex >= 0) {
            return tokens;
        }

        for (String rawToken : rawTokens) {
            tokens.add(rawToken);
        }
        return tokens;
    }

    protected void logCommandUsage(CommandContext context, String event, Map<String, ?> extraFields) {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("command", "rhythm");
        fields.put("subcommand", getName());
        fields.put("sender", context.sender().getDisplayName());
        fields.put("senderId", context.sender().getUuid());
        fields.put("input", context.getInputString());
        if (extraFields != null) {
            fields.putAll(extraFields);
        }
        logRhythmDebug("command", event, fields);
    }
}
