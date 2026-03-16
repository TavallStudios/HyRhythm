package com.hyrhythm.command;

import com.hyrhythm.content.interfaces.RhythmSongLibraryAccess;
import com.hypixel.hytale.server.core.command.system.CommandContext;

import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;

public final class RhythmSongsCommand extends AbstractRhythmCommand implements RhythmSongLibraryAccess {
    public RhythmSongsCommand() {
        super("songs", "List currently registered rhythm songs.", false);
    }

    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext context) {
        var songs = listRhythmSongs();
        if (songs.isEmpty()) {
            sendInfo(context, "No songs are registered in the rhythm library.");
            logCommandUsage(context, "songs_requested", new LinkedHashMap<>() {{
                put("songCount", 0);
            }});
            return completed();
        }

        StringBuilder builder = new StringBuilder("Songs:");
        for (var song : songs) {
            builder
                .append(' ')
                .append(song.title())
                .append(" by ")
                .append(song.artist());
            if (song.charts().isEmpty()) {
                builder.append(" [no charts]");
                continue;
            }
            builder.append(" [");
            for (int index = 0; index < song.charts().size(); index++) {
                var chart = song.charts().get(index);
                if (index > 0) {
                    builder.append(", ");
                }
                builder
                    .append(chart.metadata().difficultyName())
                    .append(" -> ")
                    .append(chart.chartId());
            }
            builder.append(']');
        }
        sendInfo(context, builder.toString());
        logCommandUsage(context, "songs_requested", new LinkedHashMap<>() {{
            put("songCount", songs.size());
        }});
        return completed();
    }
}
