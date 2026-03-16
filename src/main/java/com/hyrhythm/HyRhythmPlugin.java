package com.hyrhythm;

import com.hyrhythm.bootstrap.PluginBootstrap;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class HyRhythmPlugin extends JavaPlugin {
    private PluginBootstrap pluginBootstrap;

    public HyRhythmPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        this.pluginBootstrap = new PluginBootstrap(this);
        this.pluginBootstrap.setup();
    }

    @Override
    protected void start() {
        if (pluginBootstrap == null) {
            throw new IllegalStateException("pluginBootstrap");
        }

        this.pluginBootstrap.start();
    }

    @Override
    protected void shutdown() {
        if (pluginBootstrap != null) {
            this.pluginBootstrap.shutdown();
        }
    }

    public void registerRhythmCommand(AbstractAsyncCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command");
        }

        getCommandRegistry().registerCommand(command);
    }

    public void registerPlayerReadyListener(Consumer<PlayerReadyEvent> playerReadyListener) {
        if (playerReadyListener == null) {
            throw new IllegalArgumentException("playerReadyListener");
        }

        getEventRegistry().registerGlobal(PlayerReadyEvent.class, playerReadyListener::accept);
    }

    public void registerPlayerDisconnectListener(Consumer<PlayerDisconnectEvent> playerDisconnectListener) {
        if (playerDisconnectListener == null) {
            throw new IllegalArgumentException("playerDisconnectListener");
        }

        getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, playerDisconnectListener::accept);
    }

    public void registerPlayerInteractListener(Consumer<PlayerInteractEvent> playerInteractListener) {
        if (playerInteractListener == null) {
            throw new IllegalArgumentException("playerInteractListener");
        }

        getEventRegistry().registerGlobal(PlayerInteractEvent.class, playerInteractListener::accept);
    }

    public void logInfo(String message) {
        if (message == null) {
            throw new IllegalArgumentException("message");
        }

        getLogger().atInfo().log(message);
    }

    public void logWarning(String message) {
        if (message == null) {
            throw new IllegalArgumentException("message");
        }

        getLogger().atWarning().log(message);
    }

    public void logSevere(String message, Throwable throwable) {
        if (message == null) {
            throw new IllegalArgumentException("message");
        }
        if (throwable == null) {
            throw new IllegalArgumentException("throwable");
        }

        getLogger().atSevere().withCause(throwable).log(message);
    }
}
