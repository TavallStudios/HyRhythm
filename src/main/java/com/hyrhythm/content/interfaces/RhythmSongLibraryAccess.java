package com.hyrhythm.content.interfaces;

import com.hyrhythm.content.model.RhythmChart;
import com.hyrhythm.content.model.RhythmSong;
import com.hyrhythm.content.model.RhythmSongImportResult;
import com.hyrhythm.dependency.CoreDependencyAccess;
import com.hyrhythm.dependency.DependencyLoaderAccess;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface RhythmSongLibraryAccess extends CoreDependencyAccess {
    default RhythmSongLibraryService getRhythmSongLibraryService() {
        return DependencyLoaderAccess.requireInstance(RhythmSongLibraryService.class, "RhythmSongLibraryService");
    }

    default void loadRhythmSongLibrary() {
        getRhythmSongLibraryService().loadBuiltInSongs();
    }

    default RhythmSongImportResult importRhythmSongsFromConfiguredDirectory() {
        return getRhythmSongLibraryService().importSongsFromConfiguredDirectory();
    }

    default Path rhythmSongsDirectory() {
        return getRhythmSongLibraryService().songsDirectory();
    }

    default List<RhythmSong> listRhythmSongs() {
        return getRhythmSongLibraryService().listSongs();
    }

    default Optional<RhythmChart> findRhythmChartById(String chartId) {
        return getRhythmSongLibraryService().findChartById(chartId);
    }

    default Optional<String> findRhythmSoundEventIdBySongId(String songId) {
        return getRhythmSongLibraryService().findSoundEventIdBySongId(songId);
    }

    default RhythmChart requireDebugRhythmChart() {
        return getRhythmSongLibraryService().requireDebugChart();
    }
}
