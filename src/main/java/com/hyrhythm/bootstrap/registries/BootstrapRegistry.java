package com.hyrhythm.bootstrap.registries;

import com.hyrhythm.HyRhythmPlugin;
import com.hyrhythm.dependency.DependencyLoader;

public interface BootstrapRegistry {
    void register(DependencyLoader loader, HyRhythmPlugin plugin);
}
