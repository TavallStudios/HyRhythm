package com.hyrhythm.content.interfaces;

import com.hyrhythm.content.model.RhythmChart;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public interface RhythmChartImportService {
    RhythmChart importOsu(String sourceName, InputStream inputStream);

    List<RhythmChart> importOsz(Path archivePath);
}
