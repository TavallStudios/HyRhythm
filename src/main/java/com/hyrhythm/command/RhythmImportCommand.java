package com.hyrhythm.command;

import com.hyrhythm.content.interfaces.RhythmSongLibraryAccess;
import com.hyrhythm.content.model.RhythmSongImportResult;
import com.hypixel.hytale.server.core.command.system.CommandContext;

import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;

public final class RhythmImportCommand extends AbstractRhythmCommand implements RhythmSongLibraryAccess {
    public RhythmImportCommand() {
        super("import", "Import .osu/.osz charts from the plugin songs folder.", false);
    }

    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext context) {
        RhythmSongImportResult result = importRhythmSongsFromConfiguredDirectory();
        if (result.discoveredSourceCount() == 0) {
            sendInfo(
                context,
                "No .osu or .osz files found in " + result.songsDirectory() + "."
            );
        } else if (result.importedAnything()) {
            sendSuccess(
                context,
                "Imported "
                    + result.importedChartCount()
                    + " chart(s) from "
                    + result.discoveredSourceCount()
                    + " source file(s) in "
                    + result.songsDirectory()
                    + "."
            );
        } else {
            sendError(
                context,
                "Import completed with no registered charts. Check logs and files in " + result.songsDirectory() + "."
            );
        }
        sendInfo(context, "Songs folder: " + result.songsDirectory());
        logCommandUsage(
            context,
            "songs_import_requested",
            new LinkedHashMap<>() {{
                put("songsDirectory", result.songsDirectory());
                put("discoveredSourceCount", result.discoveredSourceCount());
                put("importedChartCount", result.importedChartCount());
                put("importedSongCount", result.importedSongCount());
                put("failedSourceCount", result.failedSourceCount());
            }}
        );
        return completed();
    }
}
