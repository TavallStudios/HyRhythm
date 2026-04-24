package com.hyrhythm.command;

import com.hyrhythm.player.interfaces.RhythmPlayerTargetAccess;
import com.hyrhythm.player.model.RhythmPlayerTarget;
import com.hyrhythm.settings.interfaces.RhythmSettingsAccess;
import com.hyrhythm.settings.model.RhythmPlayerSettings;
import com.hyrhythm.ui.interfaces.RhythmUiAccess;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class RhythmKeybindsCommand extends AbstractRhythmCommand implements RhythmSettingsAccess, RhythmPlayerTargetAccess, RhythmUiAccess {
    public RhythmKeybindsCommand() {
        super("keybinds", "Open the keybind rebind menu or update saved lane keys.", true);
    }

    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext context) {
        try {
            List<String> tokens = trailingTokens(context);
            if (tokens.isEmpty()) {
                if (context.sender() instanceof Player player && player.getWorld() != null) {
                    openRhythmKeybinds(player);
                    sendSuccess(context, "Opening keybind rebind menu for " + player.getDisplayName() + ".");
                    logCommandUsage(
                        context,
                        "keybinds_ui_requested",
                        new LinkedHashMap<>() {{
                            put("targetPlayer", player.getDisplayName());
                            put("targetPlayerId", player.getUuid());
                        }}
                    );
                    return completed();
                }
                RhythmPlayerTarget target = senderTarget(context);
                showKeybinds(context, target);
                return completed();
            }

            String action = tokens.getFirst().toLowerCase();
            if ("show".equals(action)) {
                if (tokens.size() > 2) {
                    sendError(context, usage());
                    return completed();
                }
                RhythmPlayerTarget target = resolveTarget(context, tokens.size() == 2 ? tokens.get(1) : null);
                showKeybinds(context, target);
                return completed();
            }

            if ("reset".equals(action)) {
                if (tokens.size() > 2) {
                    sendError(context, usage());
                    return completed();
                }
                RhythmPlayerTarget target = resolveTarget(context, tokens.size() == 2 ? tokens.get(1) : null);
                RhythmPlayerSettings settings = resetRhythmLaneKeys(
                    target.playerId(),
                    target.playerName()
                );
                sendSuccess(context, "Lane keys reset for " + target.playerName() + " to " + settings.laneKeys().toDisplayString() + ".");
                logCommandUsage(
                    context,
                    "keybinds_reset",
                    new LinkedHashMap<>() {{
                        put("targetPlayer", target.playerName());
                        put("targetPlayerId", target.playerId());
                        put("laneKeys", settings.laneKeys().toDisplayString());
                    }}
                );
                return completed();
            }

            if ("resetinputs".equals(action)) {
                if (tokens.size() > 2) {
                    sendError(context, usage());
                    return completed();
                }
                RhythmPlayerTarget target = resolveTarget(context, tokens.size() == 2 ? tokens.get(1) : null);
                RhythmPlayerSettings settings = resetRhythmKeybinds(
                    target.playerId(),
                    target.playerName()
                );
                sendSuccess(context, "Input bindings reset for " + target.playerName() + " to " + settings.keybinds().toDisplayString() + ".");
                logCommandUsage(
                    context,
                    "keybinds_input_reset",
                    new LinkedHashMap<>() {{
                        put("targetPlayer", target.playerName());
                        put("targetPlayerId", target.playerId());
                        put("binds", settings.keybinds().toDisplayString());
                    }}
                );
                return completed();
            }

            if (tokens.size() == 1) {
                RhythmPlayerTarget target = resolveTarget(context, tokens.getFirst());
                showKeybinds(context, target);
                return completed();
            }

            if ("set".equals(action) && (tokens.size() == 3 || tokens.size() == 4)) {
                RhythmPlayerTarget target = resolveTarget(context, tokens.size() == 4 ? tokens.get(3) : null);
                int lane = Integer.parseInt(tokens.get(1));
                String key = tokens.get(2);
                RhythmPlayerSettings settings = updateRhythmLaneKey(
                    target.playerId(),
                    target.playerName(),
                    lane,
                    key
                );
                sendSuccess(context, "Updated lane keys for " + target.playerName() + ": " + settings.laneKeys().toDisplayString() + ".");
                logCommandUsage(
                    context,
                    "keybinds_updated",
                    new LinkedHashMap<>() {{
                        put("targetPlayer", target.playerName());
                        put("targetPlayerId", target.playerId());
                        put("lane", lane);
                        put("key", settings.laneKeys().keyForLane(lane));
                    }}
                );
                return completed();
            }

            sendError(context, usage());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            sendError(context, exception.getMessage());
            logRhythmWarn(
                "command",
                "keybind_update_failed",
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

    private void showKeybinds(CommandContext context, RhythmPlayerTarget target) {
        RhythmPlayerSettings settings = getOrCreateRhythmPlayerSettings(target.playerId(), target.playerName());
        sendInfo(
            context,
            "Lane keys for " + target.playerName() + ": " + settings.laneKeys().toDisplayString()
                + " | inputs=" + settings.keybinds().toDisplayString()
        );
        logCommandUsage(
            context,
            "keybinds_shown",
            new LinkedHashMap<>() {{
                put("targetPlayer", target.playerName());
                put("targetPlayerId", target.playerId());
            }}
        );
    }

    private RhythmPlayerTarget resolveTarget(CommandContext context, String targetLookup) {
        if (targetLookup == null || targetLookup.isBlank()) {
            return senderTarget(context);
        }
        return findRhythmOnlinePlayer(targetLookup)
            .orElseThrow(() -> new IllegalStateException("Player '" + targetLookup + "' is not online."));
    }

    private static String usage() {
        return "Usage: /rhythm keybinds [show [player]|set <lane> <key> [player]|reset [player]|resetinputs [player]]"
            + " | use /rhythm keybinds in-game to open the rebind menu.";
    }
}
