package com.hyrhythm.command;

import com.hyrhythm.player.interfaces.RhythmPlayerTargetAccess;
import com.hyrhythm.session.interfaces.RhythmSessionAccess;
import com.hyrhythm.ui.interfaces.RhythmUiAccess;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;

public final class RhythmUiCommand extends AbstractRhythmCommand implements RhythmSessionAccess, RhythmUiAccess, RhythmPlayerTargetAccess {
    public RhythmUiCommand() {
        super("ui", "Open the rhythm song selection UI.", true);
    }

    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext context) {
        String targetLookup = optionalSingleArgument(context);
        if ("".equals(targetLookup)) {
            sendError(context, "Usage: /rhythm ui [player]");
            return completed();
        }

        try {
            Player player = resolveUiPlayer(context, targetLookup);
            openRhythmSongSelection(player);
            var session = findRhythmSession(player.getUuid())
                .orElseThrow(() -> new IllegalStateException("Rhythm session was not created while opening the selection UI."));
            sendSuccess(
                context,
                "Opening rhythm song selection UI for " + player.getDisplayName()
                    + " in session " + session.sessionId() + ". Select a chart, then use /rhythm start."
            );
            logCommandUsage(
                context,
                "ui_requested",
                new LinkedHashMap<>() {{
                    put("targetPlayer", player.getDisplayName());
                    put("targetPlayerId", player.getUuid());
                    put("sessionId", session.sessionId());
                    put("phase", session.phase());
                    put("chartId", session.chartId() == null ? "none" : session.chartId());
                }}
            );
        } catch (IllegalStateException exception) {
            sendError(context, exception.getMessage());
            logRhythmWarn(
                "command",
                "ui_open_failed",
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

    private Player resolveUiPlayer(CommandContext context, String targetLookup) {
        if (targetLookup == null || targetLookup.isBlank()) {
            if (context.sender() instanceof Player player) {
                return player;
            }
            throw new IllegalStateException("This command can only be used by an in-world player, or by targeting an online player: /rhythm ui <player>.");
        }

        return findRhythmOnlinePlayer(targetLookup)
            .map(value -> value.player())
            .filter(player -> player.getWorld() != null)
            .orElseThrow(() -> new IllegalStateException("Player '" + targetLookup + "' is not online and ready for rhythm UI commands."));
    }
}
