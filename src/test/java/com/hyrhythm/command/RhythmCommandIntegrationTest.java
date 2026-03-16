package com.hyrhythm.command;

import com.hyrhythm.bootstrap.registries.CoreBootstrapRegistry;
import com.hyrhythm.bootstrap.registries.RhythmBootstrapRegistry;
import com.hyrhythm.dependency.DependencyLoader;
import com.hyrhythm.session.interfaces.RhythmSessionAccess;
import com.hyrhythm.session.model.RhythmSessionPhase;
import com.hyrhythm.support.CommandTestSender;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RhythmCommandIntegrationTest implements RhythmSessionAccess {
    private CommandTestSender sender;
    private DependencyLoader loader;
    private Path tempDataDirectory;

    @BeforeEach
    void setUp() throws Exception {
        tempDataDirectory = Files.createTempDirectory("hyrhythm-command-test");
        System.setProperty("hyrhythm.test.dataDir", tempDataDirectory.toString());

        loader = DependencyLoader.getFallbackDependencyLoader();
        loader.resetInstances();
        new CoreBootstrapRegistry().register(loader, null);
        new RhythmBootstrapRegistry().register(loader, null);
        loader.loadQueuedDependenciesInOrder();

        sender = new CommandTestSender(UUID.randomUUID(), "RhythmTester");
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("hyrhythm.test.dataDir");
        loader.resetInstances();
    }

    @Test
    void subcommandsDriveSelfTestSessionFlow() {
        RhythmDebugCommand debugCommand = loader.requireInstance(RhythmDebugCommand.class);
        RhythmPlayersCommand playersCommand = loader.requireInstance(RhythmPlayersCommand.class);
        RhythmJoinCommand joinCommand = loader.requireInstance(RhythmJoinCommand.class);
        RhythmSongsCommand songsCommand = loader.requireInstance(RhythmSongsCommand.class);
        RhythmTestCommand testCommand = loader.requireInstance(RhythmTestCommand.class);
        RhythmStartCommand startCommand = loader.requireInstance(RhythmStartCommand.class);
        RhythmInputCommand inputCommand = loader.requireInstance(RhythmInputCommand.class);
        RhythmAdvanceCommand advanceCommand = loader.requireInstance(RhythmAdvanceCommand.class);
        RhythmStateCommand stateCommand = loader.requireInstance(RhythmStateCommand.class);
        RhythmStopCommand stopCommand = loader.requireInstance(RhythmStopCommand.class);

        debugCommand.executeAsync(new CommandContext(debugCommand, sender, "debug on")).join();
        playersCommand.executeAsync(new CommandContext(playersCommand, sender, "players")).join();
        songsCommand.executeAsync(new CommandContext(songsCommand, sender, "songs")).join();
        joinCommand.executeAsync(new CommandContext(joinCommand, sender, "join")).join();
        testCommand.executeAsync(new CommandContext(testCommand, sender, "test")).join();
        startCommand.executeAsync(new CommandContext(startCommand, sender, "start")).join();
        assertEquals(RhythmSessionPhase.PLAYING, findRhythmSession(sender.getUuid()).orElseThrow().phase());
        inputCommand.executeAsync(new CommandContext(inputCommand, sender, "input down 1 1000")).join();
        inputCommand.executeAsync(new CommandContext(inputCommand, sender, "input down 2 1500")).join();
        inputCommand.executeAsync(new CommandContext(inputCommand, sender, "input down 3 2000")).join();
        advanceCommand.executeAsync(new CommandContext(advanceCommand, sender, "advance 2600")).join();
        inputCommand.executeAsync(new CommandContext(inputCommand, sender, "input down 4 3000")).join();
        inputCommand.executeAsync(new CommandContext(inputCommand, sender, "input down 1 3500")).join();
        inputCommand.executeAsync(new CommandContext(inputCommand, sender, "input down 2 4500")).join();
        inputCommand.executeAsync(new CommandContext(inputCommand, sender, "input down 4 5500")).join();
        inputCommand.executeAsync(new CommandContext(inputCommand, sender, "input down 3 6500")).join();
        inputCommand.executeAsync(new CommandContext(inputCommand, sender, "input down 1 7500")).join();
        inputCommand.executeAsync(new CommandContext(inputCommand, sender, "input down 4 8500")).join();
        inputCommand.executeAsync(new CommandContext(inputCommand, sender, "input down 2 9000")).join();
        inputCommand.executeAsync(new CommandContext(inputCommand, sender, "input down 3 9500")).join();
        inputCommand.executeAsync(new CommandContext(inputCommand, sender, "input down 2 10500")).join();
        inputCommand.executeAsync(new CommandContext(inputCommand, sender, "input down 4 11500")).join();
        inputCommand.executeAsync(new CommandContext(inputCommand, sender, "input down 1 12500")).join();
        inputCommand.executeAsync(new CommandContext(inputCommand, sender, "input down 3 13500")).join();
        inputCommand.executeAsync(new CommandContext(inputCommand, sender, "input down 2 14500")).join();
        inputCommand.executeAsync(new CommandContext(inputCommand, sender, "input down 4 15500")).join();
        inputCommand.executeAsync(new CommandContext(inputCommand, sender, "input down 1 16500")).join();
        inputCommand.executeAsync(new CommandContext(inputCommand, sender, "input down 3 17500")).join();
        inputCommand.executeAsync(new CommandContext(inputCommand, sender, "input down 2 18500")).join();
        inputCommand.executeAsync(new CommandContext(inputCommand, sender, "input down 4 19500")).join();
        inputCommand.executeAsync(new CommandContext(inputCommand, sender, "input down 1 20000")).join();
        assertEquals(RhythmSessionPhase.ENDED, findRhythmSession(sender.getUuid()).orElseThrow().phase());
        stateCommand.executeAsync(new CommandContext(stateCommand, sender, "state")).join();
        stopCommand.executeAsync(new CommandContext(stopCommand, sender, "stop")).join();

        assertEquals(RhythmSessionPhase.ENDED, findRhythmSession(sender.getUuid()).orElseThrow().phase());
        assertTrue(sender.messages().stream().anyMatch(message -> message.contains("No online players are available")));
        assertTrue(sender.messages().stream().anyMatch(message -> message.contains("HyRhythm Debug Track")));
        assertTrue(sender.messages().stream().anyMatch(message -> message.contains("debug/test-4k")));
        assertTrue(sender.messages().stream().anyMatch(message -> message.contains("result=PERFECT")));
        assertTrue(sender.messages().stream().anyMatch(message -> message.contains("Advanced gameplay:")));
        assertTrue(sender.messages().stream().anyMatch(message -> message.contains("Stopped rhythm session")));
    }

    @Test
    void keybindsCommandUpdatesPersistentLaneKeys() {
        RhythmKeybindsCommand keybindsCommand = loader.requireInstance(RhythmKeybindsCommand.class);

        keybindsCommand.executeAsync(new CommandContext(keybindsCommand, sender, "keybinds")).join();
        keybindsCommand.executeAsync(new CommandContext(keybindsCommand, sender, "keybinds set 1 L")).join();
        keybindsCommand.executeAsync(new CommandContext(keybindsCommand, sender, "keybinds show")).join();

        assertTrue(sender.messages().stream().anyMatch(message -> message.contains("1=D 2=F 3=J 4=K")));
        assertTrue(sender.messages().stream().anyMatch(message -> message.contains("1=L 2=F 3=J 4=K")));
    }

    @Test
    void importCommandLoadsExternalSongFromConfiguredSongsFolder() throws Exception {
        RhythmImportCommand importCommand = loader.requireInstance(RhythmImportCommand.class);
        RhythmSongsCommand songsCommand = loader.requireInstance(RhythmSongsCommand.class);

        Path songsDirectory = tempDataDirectory.resolve("rhythm").resolve("songs").resolve("tp-na-ame");
        Files.createDirectories(songsDirectory);
        Files.writeString(songsDirectory.resolve("tp-na-ame-hard.osu"), """
            osu file format v14

            [General]
            AudioFilename: tp-na-ame.ogg
            Mode: 3

            [Metadata]
            Title: tp na ame
            Artist: ZERATch
            Creator: mapper
            Version: Hard

            [Difficulty]
            CircleSize: 4
            OverallDifficulty: 7

            [TimingPoints]
            0,500,4,1,0,100,1,0

            [HitObjects]
            64,192,1000,1,0,0:0:0:0:
            192,192,1500,1,0,0:0:0:0:
            320,192,2000,1,0,0:0:0:0:
            448,192,2500,1,0,0:0:0:0:
            """);

        importCommand.executeAsync(new CommandContext(importCommand, sender, "import")).join();
        songsCommand.executeAsync(new CommandContext(songsCommand, sender, "songs")).join();

        assertTrue(sender.messages().stream().anyMatch(message -> message.contains("Imported 1 chart")));
        assertTrue(sender.messages().stream().anyMatch(message -> message.contains("Songs folder: " + tempDataDirectory.resolve("rhythm").resolve("songs"))));
        assertTrue(sender.messages().stream().anyMatch(message -> message.contains("tp na ame by ZERATch")));
        assertTrue(sender.messages().stream().anyMatch(message -> message.contains("zeratch-tp-na-ame/hard")));
    }

    @Test
    void targetedOfflineKeybindCommandsFailCleanlyWithoutThrowing() {
        RhythmKeybindsCommand keybindsCommand = loader.requireInstance(RhythmKeybindsCommand.class);

        keybindsCommand.executeAsync(new CommandContext(keybindsCommand, sender, "keybinds MissingPlayer")).join();
        keybindsCommand.executeAsync(new CommandContext(keybindsCommand, sender, "keybinds show MissingPlayer")).join();
        keybindsCommand.executeAsync(new CommandContext(keybindsCommand, sender, "keybinds set 1 Use MissingPlayer")).join();
        keybindsCommand.executeAsync(new CommandContext(keybindsCommand, sender, "keybinds reset MissingPlayer")).join();

        assertEquals(4, sender.messages().stream().filter(message -> message.contains("MissingPlayer")).count());
    }

    @Test
    void uiCommandRequiresPlayerSender() {
        RhythmUiCommand uiCommand = loader.requireInstance(RhythmUiCommand.class);

        uiCommand.executeAsync(new CommandContext(uiCommand, sender, "ui")).join();

        assertTrue(sender.messages().stream().anyMatch(message -> message.contains("only be used by an in-world player")));
    }

    @Test
    void targetedUiCommandFailsCleanlyWhenTargetPlayerIsOffline() {
        RhythmUiCommand uiCommand = loader.requireInstance(RhythmUiCommand.class);

        uiCommand.executeAsync(new CommandContext(uiCommand, sender, "ui MissingPlayer")).join();

        assertTrue(sender.messages().stream().anyMatch(message -> message.contains("MissingPlayer")));
        assertTrue(findRhythmSession(sender.getUuid()).isEmpty());
    }

    @Test
    void targetedOfflineCommandsFailCleanlyWithoutThrowing() {
        RhythmJoinCommand joinCommand = loader.requireInstance(RhythmJoinCommand.class);
        RhythmTestCommand testCommand = loader.requireInstance(RhythmTestCommand.class);
        RhythmStateCommand stateCommand = loader.requireInstance(RhythmStateCommand.class);
        RhythmStartCommand startCommand = loader.requireInstance(RhythmStartCommand.class);
        RhythmStopCommand stopCommand = loader.requireInstance(RhythmStopCommand.class);

        joinCommand.executeAsync(new CommandContext(joinCommand, sender, "join MissingPlayer")).join();
        testCommand.executeAsync(new CommandContext(testCommand, sender, "test MissingPlayer")).join();
        stateCommand.executeAsync(new CommandContext(stateCommand, sender, "state MissingPlayer")).join();
        startCommand.executeAsync(new CommandContext(startCommand, sender, "start MissingPlayer")).join();
        stopCommand.executeAsync(new CommandContext(stopCommand, sender, "stop MissingPlayer")).join();

        assertEquals(5, sender.messages().stream().filter(message -> message.contains("MissingPlayer")).count());
        assertTrue(findRhythmSession(sender.getUuid()).isEmpty());
    }

    @Test
    void inputCommandReportsReadySessionWhenGameplayHasNotStartedYet() {
        RhythmTestCommand testCommand = loader.requireInstance(RhythmTestCommand.class);
        RhythmInputCommand inputCommand = loader.requireInstance(RhythmInputCommand.class);

        testCommand.executeAsync(new CommandContext(testCommand, sender, "test")).join();
        inputCommand.executeAsync(new CommandContext(inputCommand, sender, "input down 1 1000")).join();

        assertTrue(sender.messages().stream().anyMatch(message -> message.contains("Current session: session=")));
        assertTrue(sender.messages().stream().anyMatch(message -> message.contains("phase=READY")));
    }

    @Test
    void rootCommandShowsUsageForConsoleWithoutCreatingSession() {
        RhythmCommandRouter router = loader.requireInstance(RhythmCommandRouter.class);

        router.executeAsync(new CommandContext(router, sender, "rhythm")).join();

        assertTrue(router.getAliases().contains("rhy"));
        assertTrue(sender.messages().stream().anyMatch(message -> message.contains("Usage: /rhythm")));
        assertTrue(sender.messages().stream().anyMatch(message -> message.contains("Subcommands:")));
        assertTrue(sender.messages().stream().anyMatch(message -> message.contains("import:")));
        assertTrue(sender.messages().stream().anyMatch(message -> message.contains("keybinds:")));
        assertTrue(findRhythmSession(sender.getUuid()).isEmpty());
    }

    @Test
    void unknownRootSubcommandShowsErrorAndHelp() {
        RhythmCommandRouter router = loader.requireInstance(RhythmCommandRouter.class);

        router.executeAsync(new CommandContext(router, sender, "rhythm nope")).join();

        assertTrue(sender.messages().stream().anyMatch(message -> message.contains("Unknown rhythm subcommand 'nope'.")));
        assertTrue(sender.messages().stream().anyMatch(message -> message.contains("Usage: /rhythm")));
        assertTrue(sender.messages().stream().anyMatch(message -> message.contains("Subcommands:")));
    }
}
