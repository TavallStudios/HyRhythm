package com.hyrhythm.bootstrap;

import com.hyrhythm.HyRhythmPlugin;
import com.hyrhythm.bootstrap.registries.BootstrapRegistry;
import com.hyrhythm.bootstrap.registries.CoreBootstrapRegistry;
import com.hyrhythm.bootstrap.registries.RhythmBootstrapRegistry;
import com.hyrhythm.command.RhythmCommandRouter;
import com.hyrhythm.content.interfaces.RhythmSongLibraryService;
import com.hyrhythm.dependency.DependencyLoader;
import com.hyrhythm.gameplay.RhythmLaneInputRouter;
import com.hyrhythm.logging.interfaces.RhythmLoggingAccess;
import com.hyrhythm.player.RhythmPlayerDisconnectService;

import java.util.Map;
import java.util.Objects;

public final class PluginBootstrap implements RhythmLoggingAccess {
    private static final BootstrapRegistry[] DEPENDENCY_REGISTRIES = new BootstrapRegistry[] {
        new CoreBootstrapRegistry(),
        new RhythmBootstrapRegistry()
    };

    private static PluginBootstrap activePluginBootstrap;

    private final HyRhythmPlugin plugin;
    private final DependencyLoader dependencyLoader = new DependencyLoader();

    public PluginBootstrap(HyRhythmPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        activePluginBootstrap = this;
    }

    public void setup() {
        activePluginBootstrap = this;
        getDependencyLoader().resetInstances();
        queueDependenciesInOrder();
        getDependencyLoader().loadQueuedDependenciesInOrder();
        runPostRegistrationHooks();
    }

    public void start() {
        registerCommands();
        registerPlayerLifecycleListeners();
        logRhythmInfo(
            "bootstrap",
            "plugin_start",
            Map.of("plugin", plugin.getName(), "version", plugin.getManifest().getVersion())
        );
    }

    public void shutdown() {
        runShutdown();
        logRhythmInfo("bootstrap", "plugin_shutdown", Map.of("plugin", plugin.getName()));
        getDependencyLoader().resetInstances();
        if (activePluginBootstrap == this) {
            activePluginBootstrap = null;
        }
    }

    public static PluginBootstrap getActivePluginBootstrap() {
        return activePluginBootstrap;
    }

    public DependencyLoader getDependencyLoader() {
        return dependencyLoader;
    }

    private void queueDependenciesInOrder() {
        DependencyLoader loader = getDependencyLoader();
        for (BootstrapRegistry registry : DEPENDENCY_REGISTRIES) {
            registry.register(loader, plugin);
        }
    }

    private void runPostRegistrationHooks() {
        getDependencyLoader().requireInstance(RhythmSongLibraryService.class).loadBuiltInSongs();
        logRhythmInfo(
            "bootstrap",
            "dependency_loader_ready",
            Map.of("registryCount", DEPENDENCY_REGISTRIES.length)
        );
    }

    private void registerCommands() {
        RhythmCommandRouter rhythmCommandRouter = getDependencyLoader().requireInstance(RhythmCommandRouter.class);
        plugin.registerRhythmCommand(rhythmCommandRouter);
        logRhythmInfo(
            "bootstrap",
            "command_registered",
            Map.of("command", rhythmCommandRouter.getName(), "aliases", rhythmCommandRouter.getAliases())
        );
    }

    private void registerPlayerLifecycleListeners() {
        RhythmPlayerDisconnectService disconnectService = getDependencyLoader().requireInstance(RhythmPlayerDisconnectService.class);
        RhythmLaneInputRouter laneInputRouter = getDependencyLoader().requireInstance(RhythmLaneInputRouter.class);
        plugin.registerPlayerDisconnectListener(event -> disconnectService.handleDisconnect(
            event.getPlayerRef().getUuid(),
            event.getPlayerRef().getUsername(),
            String.valueOf(event.getDisconnectReason())
        ));
        plugin.registerPlayerInteractListener(laneInputRouter::handlePlayerInteraction);
        logRhythmInfo("bootstrap", "player_disconnect_listener_registered", Map.of("plugin", plugin.getName()));
        logRhythmInfo("bootstrap", "player_interact_listener_registered", Map.of("plugin", plugin.getName()));
    }

    private void runShutdown() {
        for (Object instance : getDependencyLoader().getAllInstances()) {
            if (!(instance instanceof AutoCloseable closeable)) {
                continue;
            }
            try {
                closeable.close();
            } catch (Exception exception) {
                logRhythmWarn(
                    "bootstrap",
                    "runtime_close_failed",
                    Map.of("type", instance.getClass().getName(), "reason", exception.getMessage())
                );
            }
        }
        logRhythmInfo("bootstrap", "runtime_shutdown", Map.of("plugin", plugin.getName()));
    }
}
