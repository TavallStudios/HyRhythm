package com.hyrhythm.content.model;

import java.util.Objects;

public record RhythmChartMetadata(
    String title,
    String titleUnicode,
    String artist,
    String artistUnicode,
    String creator,
    String difficultyName,
    String audioFileName,
    String sourceName
) {
    public RhythmChartMetadata {
        title = Objects.requireNonNull(title, "title");
        titleUnicode = titleUnicode == null || titleUnicode.isBlank() ? title : titleUnicode;
        artist = Objects.requireNonNull(artist, "artist");
        artistUnicode = artistUnicode == null || artistUnicode.isBlank() ? artist : artistUnicode;
        creator = creator == null || creator.isBlank() ? "unknown" : creator;
        difficultyName = Objects.requireNonNull(difficultyName, "difficultyName");
        audioFileName = Objects.requireNonNull(audioFileName, "audioFileName");
        sourceName = Objects.requireNonNull(sourceName, "sourceName");
    }
}
