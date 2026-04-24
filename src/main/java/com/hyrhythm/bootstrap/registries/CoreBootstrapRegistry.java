package com.hyrhythm.bootstrap.registries;

import com.hyrhythm.HyRhythmPlugin;
import com.hyrhythm.debug.RhythmDebugState;
import com.hyrhythm.dependency.DependencyLoader;
import com.hyrhythm.logging.RhythmRuntimeLogger;
import com.hyrhythm.logging.interfaces.RhythmLoggingService;

public final class CoreBootstrapRegistry implements BootstrapRegistry {
    @Override
    public void register(DependencyLoader loader, HyRhythmPlugin plugin) {
        if (plugin != null) {
            loader.registerImportantInstance(HyRhythmPlugin.class, plugin);
        }
        loader.queueDependency(RhythmDebugState.class, RhythmDebugState::new);
        loader.queueDependency(
            RhythmRuntimeLogger.class,
            () -> new RhythmRuntimeLogger(plugin, loader.requireInstance(RhythmDebugState.class))
        );
        loader.queueDependency(
            RhythmLoggingService.class,
            () -> loader.requireInstance(RhythmRuntimeLogger.class)
        );
    }
}
