package com.hyrhythm.bootstrap.registries;

import com.hyrhythm.HyRhythmPlugin;
import com.hyrhythm.command.RhythmCommandRouter;
import com.hyrhythm.command.RhythmDebugCommand;
import com.hyrhythm.command.RhythmGameplayCommand;
import com.hyrhythm.command.RhythmImportCommand;
import com.hyrhythm.command.RhythmJoinCommand;
import com.hyrhythm.command.RhythmKeybindsCommand;
import com.hyrhythm.command.RhythmPlayersCommand;
import com.hyrhythm.command.RhythmSongsCommand;
import com.hyrhythm.command.RhythmStartCommand;
import com.hyrhythm.command.RhythmStateCommand;
import com.hyrhythm.command.RhythmStopCommand;
import com.hyrhythm.command.RhythmTestCommand;
import com.hyrhythm.command.RhythmInputCommand;
import com.hyrhythm.command.RhythmAdvanceCommand;
import com.hyrhythm.command.RhythmUiCommand;
import com.hyrhythm.content.OsuManiaChartImportService;
import com.hyrhythm.content.RhythmSongLibrary;
import com.hyrhythm.content.interfaces.RhythmChartImportService;
import com.hyrhythm.content.interfaces.RhythmSongLibraryService;
import com.hyrhythm.dependency.DependencyLoader;
import com.hyrhythm.gameplay.RhythmGameplayManager;
import com.hyrhythm.gameplay.RhythmLaneInputRouter;
import com.hyrhythm.gameplay.interfaces.RhythmGameplayService;
import com.hyrhythm.logging.interfaces.RhythmLoggingService;
import com.hyrhythm.player.RhythmOnlinePlayerDirectory;
import com.hyrhythm.player.RhythmPlayerDisconnectService;
import com.hyrhythm.player.interfaces.RhythmPlayerTargetService;
import com.hyrhythm.session.RhythmSessionManager;
import com.hyrhythm.session.interfaces.RhythmSessionService;
import com.hyrhythm.settings.RhythmPlayerSettingsService;
import com.hyrhythm.settings.RhythmPlayerSettingsStore;
import com.hyrhythm.settings.RhythmStoragePaths;
import com.hyrhythm.settings.interfaces.RhythmSettingsService;
import com.hyrhythm.ui.RhythmSongSelectionPageFactory;
import com.hyrhythm.ui.RhythmGameplayPageFactory;
import com.hyrhythm.ui.RhythmGameplayUiScheduler;
import com.hyrhythm.ui.RhythmKeybindsPageFactory;
import com.hyrhythm.ui.RhythmUiManager;
import com.hyrhythm.ui.interfaces.RhythmUiService;

import java.nio.file.Path;

public final class RhythmBootstrapRegistry implements BootstrapRegistry {
    @Override
    public void register(DependencyLoader loader, HyRhythmPlugin plugin) {
        loader.queueDependency(RhythmStoragePaths.class, () -> new RhythmStoragePaths(resolveDataDirectory(plugin)));
        loader.queueDependency(
            RhythmPlayerSettingsStore.class,
            () -> new RhythmPlayerSettingsStore(loader.requireInstance(RhythmStoragePaths.class))
        );
        loader.queueDependency(
            RhythmPlayerSettingsService.class,
            () -> new RhythmPlayerSettingsService(loader.requireInstance(RhythmPlayerSettingsStore.class))
        );
        loader.queueDependency(
            RhythmSettingsService.class,
            () -> loader.requireInstance(RhythmPlayerSettingsService.class)
        );
        loader.queueDependency(OsuManiaChartImportService.class, OsuManiaChartImportService::new);
        loader.queueDependency(
            RhythmChartImportService.class,
            () -> loader.requireInstance(OsuManiaChartImportService.class)
        );
        loader.queueDependency(
            RhythmSongLibrary.class,
            () -> new RhythmSongLibrary(
                loader.requireInstance(RhythmChartImportService.class),
                loader.requireInstance(RhythmStoragePaths.class)
            )
        );
        loader.queueDependency(
            RhythmSongLibraryService.class,
            () -> loader.requireInstance(RhythmSongLibrary.class)
        );
        loader.queueDependency(RhythmSessionManager.class, RhythmSessionManager::new);
        loader.queueDependency(RhythmSessionService.class, () -> loader.requireInstance(RhythmSessionManager.class));
        loader.queueDependency(RhythmGameplayManager.class, RhythmGameplayManager::new);
        loader.queueDependency(RhythmGameplayService.class, () -> loader.requireInstance(RhythmGameplayManager.class));
        loader.queueDependency(RhythmLaneInputRouter.class, RhythmLaneInputRouter::new);
        loader.queueDependency(
            RhythmSongSelectionPageFactory.class,
            () -> new RhythmSongSelectionPageFactory(
                loader.requireInstance(RhythmSessionService.class),
                loader.requireInstance(RhythmLoggingService.class)
            )
        );
        loader.queueDependency(RhythmGameplayUiScheduler.class, RhythmGameplayUiScheduler::new);
        loader.queueDependency(
            RhythmGameplayPageFactory.class,
            () -> new RhythmGameplayPageFactory(
                loader.requireInstance(RhythmGameplayService.class),
                loader.requireInstance(RhythmSessionService.class),
                loader.requireInstance(RhythmGameplayUiScheduler.class),
                loader.requireInstance(RhythmLoggingService.class)
            )
        );
        loader.queueDependency(
            RhythmKeybindsPageFactory.class,
            () -> new RhythmKeybindsPageFactory(
                loader.requireInstance(RhythmSettingsService.class),
                loader.requireInstance(RhythmLoggingService.class)
            )
        );
        loader.queueDependency(RhythmPlayerDisconnectService.class, RhythmPlayerDisconnectService::new);
        loader.queueDependency(RhythmPlayerTargetService.class, RhythmOnlinePlayerDirectory::new);
        loader.queueDependency(
            RhythmUiManager.class,
            () -> new RhythmUiManager(
                loader.requireInstance(RhythmSongLibraryService.class),
                loader.requireInstance(RhythmSessionService.class),
                loader.requireInstance(RhythmSettingsService.class),
                loader.requireInstance(RhythmSongSelectionPageFactory.class),
                loader.requireInstance(RhythmGameplayPageFactory.class),
                loader.requireInstance(RhythmKeybindsPageFactory.class),
                loader.requireInstance(RhythmLoggingService.class)
            )
        );
        loader.queueDependency(RhythmUiService.class, () -> loader.requireInstance(RhythmUiManager.class));

        loader.queueDependency(RhythmUiCommand.class, RhythmUiCommand::new);
        loader.queueDependency(RhythmPlayersCommand.class, RhythmPlayersCommand::new);
        loader.queueDependency(RhythmSongsCommand.class, RhythmSongsCommand::new);
        loader.queueDependency(RhythmImportCommand.class, RhythmImportCommand::new);
        loader.queueDependency(RhythmJoinCommand.class, RhythmJoinCommand::new);
        loader.queueDependency(RhythmGameplayCommand.class, RhythmGameplayCommand::new);
        loader.queueDependency(RhythmStartCommand.class, RhythmStartCommand::new);
        loader.queueDependency(RhythmStopCommand.class, RhythmStopCommand::new);
        loader.queueDependency(RhythmInputCommand.class, RhythmInputCommand::new);
        loader.queueDependency(RhythmAdvanceCommand.class, RhythmAdvanceCommand::new);
        loader.queueDependency(RhythmDebugCommand.class, RhythmDebugCommand::new);
        loader.queueDependency(RhythmStateCommand.class, RhythmStateCommand::new);
        loader.queueDependency(RhythmTestCommand.class, RhythmTestCommand::new);
        loader.queueDependency(RhythmKeybindsCommand.class, RhythmKeybindsCommand::new);
        loader.queueDependency(
            RhythmCommandRouter.class,
            () -> new RhythmCommandRouter(
                loader.requireInstance(RhythmUiCommand.class),
                loader.requireInstance(RhythmPlayersCommand.class),
                loader.requireInstance(RhythmSongsCommand.class),
                loader.requireInstance(RhythmImportCommand.class),
                loader.requireInstance(RhythmJoinCommand.class),
                loader.requireInstance(RhythmGameplayCommand.class),
                loader.requireInstance(RhythmStartCommand.class),
                loader.requireInstance(RhythmStopCommand.class),
                loader.requireInstance(RhythmInputCommand.class),
                loader.requireInstance(RhythmAdvanceCommand.class),
                loader.requireInstance(RhythmDebugCommand.class),
                loader.requireInstance(RhythmStateCommand.class),
                loader.requireInstance(RhythmTestCommand.class),
                loader.requireInstance(RhythmKeybindsCommand.class)
            )
        );
    }

    private static Path resolveDataDirectory(HyRhythmPlugin plugin) {
        String override = System.getProperty("hyrhythm.test.dataDir");
        if (override != null && !override.isBlank()) {
            return Path.of(override);
        }

        if (plugin != null) {
            Path pluginDataDirectory = plugin.getDataDirectory().toAbsolutePath().normalize();
            Path modsDirectory = pluginDataDirectory.getParent();
            Path serverDirectory = modsDirectory == null ? null : modsDirectory.getParent();
            if (serverDirectory != null) {
                return serverDirectory
                    .resolve("plugin-data")
                    .resolve(pluginDataDirectory.getFileName().toString());
            }
            return pluginDataDirectory;
        }
        return Path.of(System.getProperty("java.io.tmpdir"), "hyrhythm-test-runtime");
    }
}
