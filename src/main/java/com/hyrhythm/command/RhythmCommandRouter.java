package com.hyrhythm.command;

import com.hyrhythm.session.interfaces.RhythmSessionAccess;
import com.hyrhythm.ui.interfaces.RhythmUiAccess;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class RhythmCommandRouter extends AbstractRhythmCommand implements RhythmUiAccess, RhythmSessionAccess {
    private static final LinkedHashMap<String, String> HELP_SUBCOMMANDS = new LinkedHashMap<>() {{
        put("ui", "Open the rhythm song selection UI.");
        put("players", "List online players available for targeted rhythm commands.");
        put("songs", "List currently registered rhythm songs.");
        put("import", "Import .osu charts from the plugin songs folder.");
        put("join", "Join or create a rhythm match.");
        put("gameplay", "Open the rhythm gameplay UI for an active session.");
        put("start", "Start the active rhythm match.");
        put("stop", "Stop the active rhythm match.");
        put("input", "Submit a debug lane input into active gameplay.");
        put("advance", "Advance active gameplay time for debug playback.");
        put("debug", "Toggle rhythm debug logging.");
        put("state", "Show current rhythm session state.");
        put("test", "Create a self-test rhythm session.");
        put("keybinds", "View or update lane keybinds.");
    }};
    public RhythmCommandRouter(
        RhythmUiCommand uiCommand,
        RhythmPlayersCommand playersCommand,
        RhythmSongsCommand songsCommand,
        RhythmImportCommand importCommand,
        RhythmJoinCommand joinCommand,
        RhythmGameplayCommand gameplayCommand,
        RhythmStartCommand startCommand,
        RhythmStopCommand stopCommand,
        RhythmInputCommand inputCommand,
        RhythmAdvanceCommand advanceCommand,
        RhythmDebugCommand debugCommand,
        RhythmStateCommand stateCommand,
        RhythmTestCommand testCommand,
        RhythmKeybindsCommand keybindsCommand
    ) {
        super("rhythm", "Rhythm gameplay commands.", true);
        addAliases("rhy");
        addSubCommand(uiCommand);
        addSubCommand(playersCommand);
        addSubCommand(songsCommand);
        addSubCommand(importCommand);
        addSubCommand(joinCommand);
        addSubCommand(gameplayCommand);
        addSubCommand(startCommand);
        addSubCommand(stopCommand);
        addSubCommand(inputCommand);
        addSubCommand(advanceCommand);
        addSubCommand(debugCommand);
        addSubCommand(stateCommand);
        addSubCommand(testCommand);
        addSubCommand(keybindsCommand);
    }

    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext context) {
        List<String> trailing = trailingTokens(context);
        String requestedSubcommand = trailing.isEmpty() ? null : trailing.getFirst();
        boolean validSubcommand = requestedSubcommand == null
            || HELP_SUBCOMMANDS.containsKey(requestedSubcommand.toLowerCase(Locale.ROOT));

        if (!validSubcommand) {
            sendError(context, "Unknown rhythm subcommand '" + requestedSubcommand + "'.");
        }

        sendInfo(context, "Usage: /rhythm <" + String.join("|", HELP_SUBCOMMANDS.keySet()) + ">");
        sendInfo(context, "Subcommands:");
        for (Map.Entry<String, String> subcommand : HELP_SUBCOMMANDS.entrySet()) {
            sendInfo(context, " - " + subcommand.getKey() + ": " + subcommand.getValue());
        }

        if (context.sender() instanceof Player) {
            sendInfo(context, "Tip: /rhythm ui opens the song selection UI.");
            sendInfo(context, "Tip: /rhythm gameplay reopens the gameplay UI for an active session.");
        }

        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("aliases", getAliases());
        fields.put("requestedSubcommand", requestedSubcommand == null ? "none" : requestedSubcommand);
        fields.put("validSubcommand", validSubcommand);
        logCommandUsage(context, "usage_shown", fields);
        return completed();
    }
}
