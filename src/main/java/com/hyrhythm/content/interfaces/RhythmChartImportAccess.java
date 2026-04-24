package com.hyrhythm.content.interfaces;

import com.hyrhythm.content.model.RhythmChart;
import com.hyrhythm.dependency.CoreDependencyAccess;
import com.hyrhythm.dependency.DependencyLoaderAccess;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public interface RhythmChartImportAccess extends CoreDependencyAccess {
    default RhythmChartImportService getRhythmChartImportService() {
        return DependencyLoaderAccess.requireInstance(RhythmChartImportService.class, "RhythmChartImportService");
    }

    default RhythmChart importRhythmOsu(String sourceName, InputStream inputStream) {
        return getRhythmChartImportService().importOsu(sourceName, inputStream);
    }

    default List<RhythmChart> importRhythmOsz(Path archivePath) {
        return getRhythmChartImportService().importOsz(archivePath);
    }
}
