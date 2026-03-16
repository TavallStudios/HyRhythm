package com.hyrhythm.content.model;

import java.util.List;
import java.util.Objects;

public record RhythmSong(
    String songId,
    String title,
    String artist,
    List<RhythmChart> charts
) {
    public RhythmSong {
        songId = Objects.requireNonNull(songId, "songId");
        title = Objects.requireNonNull(title, "title");
        artist = Objects.requireNonNull(artist, "artist");
        charts = List.copyOf(charts);
    }
}
