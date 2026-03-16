package com.hyrhythm.content.interfaces;

import com.hyrhythm.content.model.RhythmChart;
import com.hyrhythm.content.model.RhythmSong;
import com.hyrhythm.content.model.RhythmSongImportResult;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface RhythmSongLibraryService {
    void loadBuiltInSongs();

    RhythmSongImportResult importSongsFromConfiguredDirectory();

    Path songsDirectory();

    List<RhythmSong> listSongs();

    Optional<RhythmChart> findChartById(String chartId);

    Optional<String> findSoundEventIdBySongId(String songId);

    RhythmChart requireDebugChart();
}
