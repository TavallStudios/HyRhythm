package com.hyrhythm.protocolbot;

import com.hypixel.hytale.protocol.Asset;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.ModelTransform;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.TeleportAck;
import com.hypixel.hytale.protocol.packets.connection.ClientType;
import com.hypixel.hytale.protocol.packets.connection.Connect;
import com.hypixel.hytale.protocol.packets.connection.Disconnect;
import com.hypixel.hytale.protocol.packets.connection.Ping;
import com.hypixel.hytale.protocol.packets.connection.Pong;
import com.hypixel.hytale.protocol.packets.connection.PongType;
import com.hypixel.hytale.protocol.packets.entities.EntityUpdates;
import com.hypixel.hytale.protocol.packets.interface_.ChatMessage;
import com.hypixel.hytale.protocol.packets.interface_.CustomPage;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageEvent;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageEventType;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommand;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommandType;
import com.hypixel.hytale.protocol.packets.interface_.ServerMessage;
import com.hypixel.hytale.protocol.packets.interface_.SetPage;
import com.hypixel.hytale.protocol.packets.player.ClientMovement;
import com.hypixel.hytale.protocol.packets.player.ClientReady;
import com.hypixel.hytale.protocol.packets.player.ClientTeleport;
import com.hypixel.hytale.protocol.packets.player.JoinWorld;
import com.hypixel.hytale.protocol.packets.player.SetClientId;
import com.hypixel.hytale.protocol.packets.setup.PlayerOptions;
import com.hypixel.hytale.protocol.packets.setup.RequestAssets;
import com.hypixel.hytale.protocol.packets.setup.ViewRadius;
import com.hypixel.hytale.protocol.packets.setup.WorldSettings;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class RhythmProtocolBotMain {
    private static final String SELECTION_PAGE_KEY = "com.hyrhythm.ui.RhythmSongSelectionPage";
    private static final String GAMEPLAY_PAGE_KEY = "com.hyrhythm.ui.RhythmGameplayPage";
    private static final String RESPAWN_PAGE_KEY = "com.hypixel.hytale.server.core.entity.entities.player.pages.RespawnPage";

    public static void main(String[] args) throws Exception {
        BotConfig config = BotConfig.parse(args);
        BotRunResult result = new RhythmProtocolBot(config).run();
        System.out.println("[bot] success=" + result.success());
        System.out.println("[bot] finalState=" + result.finalStateMessage());
        System.out.println("[bot] messages=" + result.serverMessages().size());
        System.out.println("[bot] pages=" + result.pageKeys());
        if (!result.success()) {
            if (result.failureMessage() != null && !result.failureMessage().isBlank()) {
                System.err.println("[bot] failure=" + result.failureMessage());
            }
            System.exit(1);
        }
    }

    private record BotConfig(
        String host,
        int port,
        String playerName,
        Duration timeout,
        GameplayInputMode inputMode,
        GameplayScenario gameplayScenario,
        boolean assumeOperator,
        boolean traceUiPackets,
        boolean listenOnly,
        long uiPacketLookaheadMs
    ) {
        private static BotConfig parse(String[] args) {
            String host = "127.0.0.1";
            int port = 5520;
            String playerName = "HyRhythmBot";
            Duration timeout = Duration.ofSeconds(20L);
            GameplayInputMode inputMode = GameplayInputMode.CommandInput;
            GameplayScenario gameplayScenario = GameplayScenario.PlayChart;
            boolean assumeOperator = false;
            boolean traceUiPackets = false;
            boolean listenOnly = false;
            long uiPacketLookaheadMs = 40L;

            for (int index = 0; index < args.length; index++) {
                String argument = args[index];
                switch (argument) {
                    case "--host" -> host = requireValue(argument, args, ++index);
                    case "--port" -> port = Integer.parseInt(requireValue(argument, args, ++index));
                    case "--name" -> playerName = requireValue(argument, args, ++index);
                    case "--timeout-seconds" -> timeout = Duration.ofSeconds(Long.parseLong(requireValue(argument, args, ++index)));
                    case "--input-mode" -> inputMode = GameplayInputMode.parse(requireValue(argument, args, ++index));
                    case "--gameplay-scenario" -> gameplayScenario = GameplayScenario.parse(requireValue(argument, args, ++index));
                    case "--assume-op" -> assumeOperator = true;
                    case "--trace-ui" -> traceUiPackets = true;
                    case "--listen-only" -> listenOnly = true;
                    case "--ui-lookahead-ms" -> uiPacketLookaheadMs = Math.max(0L, Long.parseLong(requireValue(argument, args, ++index)));
                    case "-h", "--help" -> {
                        System.out.println("""
                            Usage: RhythmProtocolBotMain [--host <host>] [--port <port>] [--name <player>] [--timeout-seconds <seconds>] [--input-mode <command-input|ui-packet>] [--gameplay-scenario <play-chart|click-stop|click-close>] [--assume-op] [--trace-ui] [--listen-only] [--ui-lookahead-ms <millis>]
                            """.trim());
                        System.exit(0);
                    }
                    default -> throw new IllegalArgumentException("Unknown argument: " + argument);
                }
            }

            return new BotConfig(host, port, playerName, timeout, inputMode, gameplayScenario, assumeOperator, traceUiPackets, listenOnly, uiPacketLookaheadMs);
        }

        private static String requireValue(String flag, String[] args, int index) {
            if (index >= args.length) {
                throw new IllegalArgumentException("Missing value for " + flag);
            }
            return args[index];
        }
    }

    private enum GameplayInputMode {
        CommandInput("command-input"),
        UiPacket("ui-packet");

        private final String cliValue;

        GameplayInputMode(String cliValue) {
            this.cliValue = cliValue;
        }

        private static GameplayInputMode parse(String rawValue) {
            for (GameplayInputMode value : values()) {
                if (value.cliValue.equalsIgnoreCase(rawValue)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown input mode: " + rawValue);
        }
    }

    private enum GameplayScenario {
        PlayChart("play-chart"),
        ClickStop("click-stop"),
        ClickClose("click-close");

        private final String cliValue;

        GameplayScenario(String cliValue) {
            this.cliValue = cliValue;
        }

        private static GameplayScenario parse(String rawValue) {
            for (GameplayScenario value : values()) {
                if (value.cliValue.equalsIgnoreCase(rawValue)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown gameplay scenario: " + rawValue);
        }
    }

    private record PlannedInput(int lane, String key, boolean down, long songTimeMillis) {
        private String asUiLaneEventJson(String captureValue) {
            return "{\"Action\":\"CaptureKey\",\"@CaptureValue\":\"" + captureValue + "\"}";
        }

        private String asCommand(long effectiveSongTimeMillis) {
            return "/rhythm input " + (down ? "down" : "up") + " " + lane + " " + effectiveSongTimeMillis;
        }
    }

    private record BotRunResult(
        boolean success,
        String finalStateMessage,
        String failureMessage,
        List<String> serverMessages,
        List<String> pageKeys
    ) {
    }

    private static final class RhythmProtocolBot extends SimpleChannelInboundHandler<Packet> {
        private static final int PROTOCOL_CRC = -1356075132;
        private static final int PROTOCOL_BUILD = 20;
        private static final Asset[] EMPTY_ASSETS = new Asset[0];
        private static final List<PlannedInput> GAMEPLAY_PACKET_INPUTS = List.of(
            new PlannedInput(1, "D", true, 1000L),
            new PlannedInput(2, "F", true, 1500L),
            new PlannedInput(3, "J", true, 2000L),
            new PlannedInput(4, "K", true, 3000L),
            new PlannedInput(1, "D", true, 3500L),
            new PlannedInput(2, "F", true, 4500L),
            new PlannedInput(4, "K", true, 5500L),
            new PlannedInput(3, "J", true, 6500L),
            new PlannedInput(1, "D", true, 7500L),
            new PlannedInput(4, "K", true, 8500L),
            new PlannedInput(2, "F", true, 9000L),
            new PlannedInput(3, "J", true, 9500L),
            new PlannedInput(2, "F", true, 10500L),
            new PlannedInput(4, "K", true, 11500L),
            new PlannedInput(1, "D", true, 12500L),
            new PlannedInput(3, "J", true, 13500L),
            new PlannedInput(2, "F", true, 14500L),
            new PlannedInput(4, "K", true, 15500L),
            new PlannedInput(1, "D", true, 16500L),
            new PlannedInput(3, "J", true, 17500L),
            new PlannedInput(2, "F", true, 18500L),
            new PlannedInput(4, "K", true, 19500L),
            new PlannedInput(1, "D", true, 20000L)
        );
        private static final List<PlannedInput> GAMEPLAY_COMMAND_INPUTS = List.of(
            new PlannedInput(1, "D", true, 1000L),
            new PlannedInput(2, "F", true, 1500L),
            new PlannedInput(3, "J", true, 2000L),
            new PlannedInput(3, "J", false, 2600L),
            new PlannedInput(4, "K", true, 3000L),
            new PlannedInput(1, "D", true, 3500L),
            new PlannedInput(2, "F", true, 4500L),
            new PlannedInput(4, "K", true, 5500L),
            new PlannedInput(3, "J", true, 6500L),
            new PlannedInput(1, "D", true, 7500L),
            new PlannedInput(4, "K", true, 8500L),
            new PlannedInput(2, "F", true, 9000L),
            new PlannedInput(3, "J", true, 9500L),
            new PlannedInput(2, "F", true, 10500L),
            new PlannedInput(4, "K", true, 11500L),
            new PlannedInput(1, "D", true, 12500L),
            new PlannedInput(3, "J", true, 13500L),
            new PlannedInput(2, "F", true, 14500L),
            new PlannedInput(4, "K", true, 15500L),
            new PlannedInput(1, "D", true, 16500L),
            new PlannedInput(3, "J", true, 17500L),
            new PlannedInput(2, "F", true, 18500L),
            new PlannedInput(4, "K", true, 19500L),
            new PlannedInput(1, "D", true, 20000L)
        );

        private static final AtomicInteger SCHEDULER_THREAD_COUNTER = new AtomicInteger(1);

        private final BotConfig config;
        private final CompletableFuture<BotRunResult> completion = new CompletableFuture<>();
        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, runnable -> {
            Thread thread = new Thread(runnable, "hyrhythm-protocol-bot-" + SCHEDULER_THREAD_COUNTER.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        });
        private final EventLoopGroup workerGroup = new NioEventLoopGroup(1);
        private final List<String> pageKeys = new CopyOnWriteArrayList<>();
        private final List<String> serverMessages = new CopyOnWriteArrayList<>();
        private final AtomicInteger customPageAcknowledgmentsOutstanding = new AtomicInteger();

        private volatile SocketChannel channel;
        private volatile int clientId = -1;
        private volatile boolean selfOpRequestScheduled;
        private volatile boolean selfOpResponsePending;
        private volatile boolean worldJoinConfirmed;
        private volatile boolean postOpCommandSequenceScheduled;
        private volatile boolean selectionConfirmed;
        private volatile boolean startCommandSent;
        private volatile boolean startCommandScheduled;
        private volatile boolean gameplayInputsScheduled;
        private volatile boolean gameplayPageSeen;
        private volatile boolean gameplayRequestSent;
        private volatile boolean gameplayCloseDismissed;
        private volatile boolean stateCommandScheduled;
        private volatile boolean endedSeen;
        private volatile long lastCustomPageReceivedAtMillis;
        private volatile boolean chartCompleteSeen;
        private volatile int selectionStage;
        private volatile int selfOpAttempts;
        private volatile int startCommandAttempts;
        private volatile int nextGameplayInputIndex;
        private volatile long latestGameplayClockMillis = -1L;
        private volatile boolean customPageVisible;
        private final StringBuilder gameplayCaptureBuffer = new StringBuilder();
        private volatile ScheduledFuture<?> movementHeartbeat;
        private volatile Position currentPosition;
        private volatile Direction currentLookOrientation;
        private volatile Direction currentBodyOrientation;

        private RhythmProtocolBot(BotConfig config) {
            this.config = Objects.requireNonNull(config, "config");
        }

        private BotRunResult run() throws Exception {
            scheduler.schedule(
                () -> fail("Timed out after " + config.timeout().toSeconds() + "s waiting for protocol bot validation."),
                config.timeout().toMillis(),
                TimeUnit.MILLISECONDS
            );

            try {
                Bootstrap bootstrap = new Bootstrap()
                    .group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            channel = socketChannel;
                            socketChannel.pipeline().addLast("packetDecoder", new ClientPacketDecoder());
                            socketChannel.pipeline().addLast("packetEncoder", new ClientPacketEncoder());
                            socketChannel.pipeline().addLast("handler", RhythmProtocolBot.this);
                        }
                    });

                bootstrap.connect(new InetSocketAddress(config.host(), config.port())).sync();
                return completion.get(config.timeout().toMillis() + 2_000L, TimeUnit.MILLISECONDS);
            } catch (ExecutionException exception) {
                Throwable cause = exception.getCause() == null ? exception : exception.getCause();
                throw new RuntimeException(cause.getMessage(), cause);
            } finally {
                ScheduledFuture<?> heartbeat = movementHeartbeat;
                if (heartbeat != null) {
                    heartbeat.cancel(true);
                }
                if (channel != null) {
                    channel.close().awaitUninterruptibly(1, TimeUnit.SECONDS);
                }
                scheduler.shutdownNow();
                workerGroup.shutdownGracefully().awaitUninterruptibly(2, TimeUnit.SECONDS);
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext context) {
            UUID playerUuid = UUID.nameUUIDFromBytes(("hyrhythm:" + config.playerName()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            trace("connected " + config.playerName() + " -> " + config.host() + ":" + config.port());
            sendPacket(
                context,
                new Connect(
                    PROTOCOL_CRC,
                    PROTOCOL_BUILD,
                    "hyrhythm-bot",
                    ClientType.Game,
                    playerUuid,
                    config.playerName(),
                    null,
                    "en",
                    null,
                    null
                ),
                "connect"
            );
        }

        @Override
        protected void channelRead0(ChannelHandlerContext context, Packet packet) {
            if (packet instanceof WorldSettings) {
                trace("received WorldSettings");
                context.write(new RequestAssets(EMPTY_ASSETS));
                context.write(new ViewRadius(6));
                sendPacket(context, new PlayerOptions((PlayerSkin) null), "player-options");
                return;
            }

            if (packet instanceof SetClientId setClientId) {
                clientId = setClientId.clientId;
                trace("assigned clientId=" + clientId);
                return;
            }

            if (packet instanceof JoinWorld) {
                trace("received JoinWorld");
                sendPacket(context, new ClientReady(true, clientId != -1), "client-ready");
                startHeartbeatIfNeeded();
                scheduleRhythmEntrypoint();
                return;
            }

            if (packet instanceof Ping ping) {
                handlePing(context, ping);
                return;
            }

            if (packet instanceof ClientTeleport teleport) {
                updateTransform(teleport.modelTransform);
                ClientMovement movement = createMovementPacket();
                movement.teleportAck = new TeleportAck(teleport.teleportId);
                context.writeAndFlush(movement);
                startHeartbeatIfNeeded();
                scheduleRhythmEntrypoint();
                return;
            }

            if (packet instanceof EntityUpdates entityUpdates) {
                trace("entity update packet received updates=" + (entityUpdates.updates == null ? 0 : entityUpdates.updates.length));
                startHeartbeatIfNeeded();
                scheduleRhythmEntrypoint();
                return;
            }

            if (packet instanceof SetPage setPage) {
                String pageName = String.valueOf(setPage.page);
                trace("set page=" + pageName);
                if (customPageVisible && "None".equalsIgnoreCase(pageName)) {
                    customPageVisible = false;
                    sendPacket(context, new CustomPageEvent(CustomPageEventType.Acknowledge, null), "set-page-ack");
                    if (gameplayPageSeen && gameplayRequestSent && config.gameplayScenario() == GameplayScenario.ClickClose) {
                        gameplayCloseDismissed = true;
                        trace("gameplay close dismissed");
                        scheduleStatePoll("close-dismissed", 100L);
                    }
                }
                if ("None".equalsIgnoreCase(pageName) && selectionReadyForGameplay() && !gameplayPageSeen) {
                    scheduleStartCommand("page-dismissed", 50L);
                }
                return;
            }

            if (packet instanceof CustomPage customPage) {
                handleCustomPage(context, customPage);
                return;
            }

            if (packet instanceof ServerMessage serverMessage) {
                handleServerMessage(serverMessage);
                return;
            }

            if (packet instanceof Disconnect disconnect) {
                fail("Disconnected: " + disconnect.reason + " (" + disconnect.type + ")");
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext context) {
            if (!completion.isDone()) {
                fail("Channel closed before rhythm bot validation finished.");
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
            fail("Protocol bot exception: " + cause.getMessage());
        }

        private void handlePing(ChannelHandlerContext context, Ping ping) {
            context.write(new Pong(ping.id, ping.time, PongType.Raw, (short) 0));
            context.write(new Pong(ping.id, ping.time, PongType.Direct, (short) 0));
            sendPacket(context, new Pong(ping.id, ping.time, PongType.Tick, (short) 0), "ping-response");
        }

        private void handleCustomPage(ChannelHandlerContext context, CustomPage customPage) {
            pageKeys.add(customPage.key);
            customPageVisible = true;
            int pendingAcks = customPageAcknowledgmentsOutstanding.incrementAndGet();
            trace(
                "custom page=" + customPage.key
                    + " initial=" + customPage.isInitial
                    + " commands=" + commandCount(customPage)
                    + " pendingAcks=" + pendingAcks
            );

            lastCustomPageReceivedAtMillis = System.currentTimeMillis();

            List<Object> packets = new ArrayList<>();
            packets.add(new CustomPageEvent(CustomPageEventType.Acknowledge, null));
            customPageAcknowledgmentsOutstanding.decrementAndGet();

            if (config.traceUiPackets()) {
                traceCustomPagePayload(customPage);
            }

            if (SELECTION_PAGE_KEY.equals(customPage.key)) {
                handleSelectionPage(packets);
            } else if (GAMEPLAY_PAGE_KEY.equals(customPage.key)) {
                handleGameplayPage(customPage, packets);
            } else if (RESPAWN_PAGE_KEY.equals(customPage.key)) {
                handleRespawnPage(packets);
            }

            for (Object packet : packets) {
                context.write(packet);
            }
            context.flush();
        }

        private void handleSelectionPage(List<Object> packets) {
            if (config.listenOnly()) {
                return;
            }
            if (selectionStage == 0) {
                selectionStage = 1;
                trace("selection: preview song");
                packets.add(new CustomPageEvent(CustomPageEventType.Data, "{\"Song\":\"debug-song\"}"));
                return;
            }
            if (selectionStage == 1) {
                selectionStage = 2;
                trace("selection: preview chart");
                packets.add(new CustomPageEvent(CustomPageEventType.Data, "{\"Chart\":\"debug/test-4k\"}"));
                return;
            }
            if (selectionStage == 2) {
                selectionStage = 3;
                trace("selection: confirm chart");
                packets.add(new CustomPageEvent(CustomPageEventType.Data, "{\"Action\":\"Confirm\"}"));
            }
        }

        private void handleGameplayPage(CustomPage customPage, List<Object> packets) {
            if (config.listenOnly()) {
                return;
            }
            long clockMillis = extractClockMillis(customPage);
            if (clockMillis >= 0L) {
                latestGameplayClockMillis = clockMillis;
            }
            gameplayPageSeen = true;
            armGameplayScenario();
        }

        private void handleRespawnPage(List<Object> packets) {
            if (config.listenOnly()) {
                return;
            }
            trace("respawn: requesting respawn");
            packets.add(new CustomPageEvent(CustomPageEventType.Data, "{\"Action\":\"Respawn\"}"));
        }

        private void handleServerMessage(ServerMessage serverMessage) {
            String message = plainText(serverMessage.message);
            if (message.isBlank()) {
                return;
            }

            serverMessages.add(message);
            trace("server: " + message);

            if (message.contains("server.commands.op.self.added")) {
                selfOpResponsePending = false;
                schedulePostOpCommandSequence();
                return;
            }
            if (message.contains("server.general.playerJoinedWorld")) {
                worldJoinConfirmed = true;
                scheduleRhythmEntrypoint();
                return;
            }
            if (message.contains("server.commands.op.self.removed")) {
                selfOpResponsePending = false;
                trace("operator privileges removed by toggle, retrying self-op");
                scheduleSelfOpRequest(100L, true);
                return;
            }

            if (message.contains("Selected chart debug/test-4k. Use /rhythm start.")) {
                selectionConfirmed = true;
                trace("selection confirmed by server message");
                scheduleStartCommand("chart-selected-message", 100L);
            }

            if (config.gameplayScenario() == GameplayScenario.ClickStop) {
                if (message.contains("phase=ENDED") && message.contains("chart=debug/test-4k")) {
                    if (message.contains("finish=ui_stop")) {
                        succeed(message);
                    } else {
                        fail("Gameplay stop did not produce ui_stop state: " + message);
                    }
                }
                return;
            }

            if (config.gameplayScenario() == GameplayScenario.ClickClose) {
                if (message.contains("phase=ENDED") && message.contains("chart=debug/test-4k")) {
                    fail("Gameplay close unexpectedly ended the session: " + message);
                    return;
                }
                if (gameplayCloseDismissed
                    && message.contains("phase=PLAYING")
                    && message.contains("chart=debug/test-4k")
                    && (message.contains("gameplay=active") || message.contains("gameplay=idle"))) {
                    succeed(message);
                }
                return;
            }

            if (message.contains("phase=ENDED") && message.contains("chart=debug/test-4k")) {
                endedSeen = true;
            }
            if (message.contains("finish=chart_complete")) {
                chartCompleteSeen = true;
            }
            boolean authoritativeCompletionSeen = message.contains("score=4090")
                && message.contains("maxCombo=13");
            if (endedSeen && chartCompleteSeen && config.inputMode() == GameplayInputMode.UiPacket) {
                if (message.contains("score=") && message.contains("maxCombo=")) {
                    if (authoritativeCompletionSeen) {
                        succeed(message);
                    } else {
                        fail("UI-packet gameplay did not reach authoritative completion: " + message);
                    }
                    return;
                }
            }

            if (endedSeen && chartCompleteSeen && authoritativeCompletionSeen) {
                succeed(message);
            }
        }

        private void scheduleRhythmEntrypoint() {
            if (!worldJoinConfirmed || config.listenOnly()) {
                return;
            }
            if (config.assumeOperator()) {
                schedulePostOpCommandSequence();
                return;
            }
            scheduleSelfOpRequest(150L, false);
        }

        private void scheduleSelfOpRequest(long delayMillis, boolean force) {
            if (!force && (selfOpRequestScheduled || selfOpResponsePending || postOpCommandSequenceScheduled)) {
                return;
            }
            if (selfOpAttempts >= 3) {
                fail("Failed to establish operator permissions for the protocol bot after " + selfOpAttempts + " attempts.");
                return;
            }
            selfOpRequestScheduled = true;
            trace("queueing /op self attempt=" + (selfOpAttempts + 1));
            scheduler.schedule(() -> {
                selfOpAttempts++;
                selfOpRequestScheduled = false;
                selfOpResponsePending = true;
                trace("firing /op self attempt=" + selfOpAttempts);
                sendChat("/op self");
            }, delayMillis, TimeUnit.MILLISECONDS);
        }

        private void schedulePostOpCommandSequence() {
            if (postOpCommandSequenceScheduled) {
                return;
            }
            postOpCommandSequenceScheduled = true;
            trace("queueing /rhythm debug on + /rhythm ui");
            scheduler.schedule(() -> {
                trace("firing /rhythm debug on");
                sendChat("/rhythm debug on");
            }, 150L, TimeUnit.MILLISECONDS);
            scheduler.schedule(() -> {
                trace("firing /rhythm ui");
                sendChat("/rhythm ui");
            }, 450L, TimeUnit.MILLISECONDS);
        }

        private void armGameplayScenario() {
            if (gameplayInputsScheduled) {
                return;
            }
            gameplayInputsScheduled = true;
            switch (config.gameplayScenario()) {
                case PlayChart -> {
                    trace("gameplay: arming " + config.inputMode().cliValue + " inputs");
                    if (config.inputMode() == GameplayInputMode.CommandInput) {
                        scheduleGameplayCommandInputs();
                    } else {
                        scheduleGameplayUiPackets();
                    }
                }
                case ClickStop -> {
                    trace("gameplay: queueing stop interaction");
                    scheduleGameplayRequest("Stop", 150L);
                }
                case ClickClose -> {
                    trace("gameplay: queueing close interaction");
                    scheduleGameplayRequest("Close", 150L);
                }
            }
            scheduleStatePolls();
        }

        private void scheduleGameplayCommandInputs() {
            for (PlannedInput input : GAMEPLAY_COMMAND_INPUTS) {
                long delayMillis = Math.max(100L, input.songTimeMillis() - 100L);
                scheduler.schedule(() -> sendGameplayCommand(input), delayMillis, TimeUnit.MILLISECONDS);
            }
        }

        private void scheduleGameplayUiPackets() {
            gameplayCaptureBuffer.setLength(0);
            for (PlannedInput input : GAMEPLAY_PACKET_INPUTS) {
                if (!input.down()) {
                    continue;
                }
                long delayMillis = Math.max(150L, input.songTimeMillis());
                scheduler.schedule(() -> sendGameplayUiPacket(input), delayMillis, TimeUnit.MILLISECONDS);
            }
        }

        private void scheduleGameplayRequest(String request, long delayMillis) {
            if (gameplayRequestSent) {
                return;
            }
            scheduler.schedule(() -> sendGameplayRequest(request), delayMillis, TimeUnit.MILLISECONDS);
        }

        private void sendGameplayRequest(String request) {
            if (gameplayRequestSent || completion.isDone()) {
                return;
            }
            if (channel == null || !channel.isActive()) {
                fail("Cannot send gameplay request because the bot channel is closed: " + request);
                return;
            }
            gameplayRequestSent = true;
            String payload = "{\"Request\":\"" + request + "\"}";
            trace("gameplay request> " + payload);
            sendPacket(channel.writeAndFlush(new CustomPageEvent(CustomPageEventType.Data, payload)), "gameplay-request-" + request.toLowerCase(Locale.ROOT));
            scheduleStatePoll("post-" + request.toLowerCase(Locale.ROOT), 250L);
        }

        private void sendGameplayCommand(PlannedInput input) {
            long effectiveSongTimeMillis = input.songTimeMillis();
            if (latestGameplayClockMillis >= 0L) {
                effectiveSongTimeMillis = Math.max(effectiveSongTimeMillis, latestGameplayClockMillis);
            }
            String command = input.asCommand(effectiveSongTimeMillis);
            trace("gameplay command> " + command);
            sendChat(command);
        }

        private void sendGameplayUiPacket(PlannedInput input) {
            if (channel == null || !channel.isActive()) {
                fail("Cannot send gameplay UI packet because the bot channel is closed: " + input.key());
                return;
            }
            String payload = gameplayCapturePayload(input);
            trace("gameplay packet> " + payload);
            sendPacket(channel.writeAndFlush(new CustomPageEvent(CustomPageEventType.Data, payload)), "gameplay-ui-packet");
        }

        private void scheduleStatePolls() {
            if (stateCommandScheduled) {
                return;
            }
            stateCommandScheduled = true;
            scheduleStatePoll("early", 200L);
            scheduleStatePoll("mid", 700L);
            scheduleStatePoll("late", 3_800L);
            scheduleStatePoll("final", 4_500L);
            scheduleStatePoll("extended", 6_000L);
        }

        private void scheduleStatePoll(String label, long delayMillis) {
            scheduler.schedule(() -> {
                if (!completion.isDone()) {
                    trace("firing " + label + " /rhythm state poll");
                    sendChat("/rhythm state");
                }
            }, delayMillis, TimeUnit.MILLISECONDS);
        }

        private void startHeartbeatIfNeeded() {
            if (movementHeartbeat != null) {
                return;
            }
            trace("starting movement heartbeat");
            movementHeartbeat = scheduler.scheduleAtFixedRate(this::sendHeartbeatMovement, 250L, 250L, TimeUnit.MILLISECONDS);
        }

        private void sendHeartbeatMovement() {
            if (channel == null || !channel.isActive()) {
                return;
            }
            channel.writeAndFlush(createMovementPacket());
        }

        private boolean selectionReadyForGameplay() {
            return selectionConfirmed || selectionStage >= 3;
        }

        private void scheduleStartCommand(String reason, long delayMillis) {
            if (config.listenOnly() || gameplayPageSeen || startCommandScheduled || !selectionReadyForGameplay()) {
                return;
            }
            if (startCommandAttempts >= 2) {
                return;
            }
            startCommandScheduled = true;
            trace("queueing /rhythm start reason=" + reason + " attempt=" + (startCommandAttempts + 1));
            scheduler.schedule(() -> dispatchStartCommand(reason), delayMillis, TimeUnit.MILLISECONDS);
        }

        private void dispatchStartCommand(String reason) {
            startCommandScheduled = false;
            if (config.listenOnly() || gameplayPageSeen || !selectionReadyForGameplay()) {
                return;
            }
            if (startCommandAttempts >= 2) {
                return;
            }
            startCommandAttempts++;
            startCommandSent = true;
            trace("dispatching /rhythm start reason=" + reason + " attempt=" + startCommandAttempts);
            sendChat("/rhythm start");
            if (startCommandAttempts < 2) {
                scheduler.schedule(() -> {
                    if (!completion.isDone() && !gameplayPageSeen) {
                        scheduleStartCommand("retry-after-" + reason, 0L);
                    }
                }, 800L, TimeUnit.MILLISECONDS);
            }
        }

        private void sendChat(String command) {
            if (channel == null || !channel.isActive()) {
                fail("Cannot send chat command because the bot channel is closed: " + command);
                return;
            }
            trace("chat> " + command);
            sendPacket(channel.writeAndFlush(new ChatMessage(command)), "chat:" + command);
        }

        private ClientMovement createMovementPacket() {
            ClientMovement movement = new ClientMovement();
            movement.absolutePosition = currentPosition == null ? new Position() : new Position(currentPosition);
            movement.lookOrientation = currentLookOrientation == null ? new Direction() : new Direction(currentLookOrientation);
            movement.bodyOrientation = currentBodyOrientation == null ? new Direction() : new Direction(currentBodyOrientation);
            movement.movementStates = new MovementStates();
            if (movement.bodyOrientation != null) {
                movement.bodyOrientation.pitch = 0.0f;
            }
            return movement;
        }

        private void updateTransform(ModelTransform transform) {
            if (transform == null) {
                return;
            }
            if (transform.position != null) {
                currentPosition = new Position(transform.position);
            }
            if (transform.lookOrientation != null) {
                currentLookOrientation = new Direction(transform.lookOrientation);
            }
            if (transform.bodyOrientation != null) {
                currentBodyOrientation = new Direction(transform.bodyOrientation);
            }
        }

        private int commandCount(CustomPage customPage) {
            return customPage.commands == null ? 0 : customPage.commands.length;
        }

        private void succeed(String finalStateMessage) {
            if (completion.isDone()) {
                return;
            }
            completion.complete(
                new BotRunResult(
                    true,
                    finalStateMessage,
                    null,
                    List.copyOf(serverMessages),
                    List.copyOf(pageKeys)
                )
            );
        }

        private void fail(String message) {
            if (completion.isDone()) {
                return;
            }
            completion.complete(
                new BotRunResult(
                    false,
                    "",
                    message,
                    List.copyOf(serverMessages),
                    List.copyOf(pageKeys)
                )
            );
        }

        private void trace(String message) {
            System.out.println("[bot] " + message);
        }

        private String gameplayCapturePayload(PlannedInput input) {
            gameplayCaptureBuffer.append(input.key());
            return input.asUiLaneEventJson(gameplayCaptureBuffer.toString());
        }

        private long extractClockMillis(CustomPage customPage) {
            if (customPage.commands == null) {
                return -1L;
            }
            for (CustomUICommand command : customPage.commands) {
                if (command == null || command.selector == null || !"#ClockValue.Text".equals(command.selector)) {
                    continue;
                }
                Long parsed = parseClockValue(command.text);
                if (parsed != null) {
                    return parsed;
                }
                parsed = parseClockValue(command.data);
                if (parsed != null) {
                    return parsed;
                }
            }
            return -1L;
        }

        private Long parseClockValue(String rawValue) {
            if (rawValue == null || rawValue.isBlank()) {
                return null;
            }
            String digits = rawValue.replaceAll("[^0-9]", "");
            if (digits.isBlank()) {
                return null;
            }
            return Long.parseLong(digits);
        }

        private void sendPacket(ChannelHandlerContext context, Packet packet, String description) {
            sendPacket(context.writeAndFlush(packet), description);
        }

        private void sendPacket(ChannelFuture future, String description) {
            future.addListener(result -> {
                if (!result.isSuccess()) {
                    fail("Failed to send " + description + ": " + result.cause().getMessage());
                } else {
                    trace("sent " + description);
                }
            });
        }

        private void traceCustomPagePayload(CustomPage customPage) {
            if (customPage.commands == null) {
                return;
            }
            for (CustomUICommand command : customPage.commands) {
                if (command == null) {
                    continue;
                }
                StringBuilder line = new StringBuilder("ui-command ")
                    .append(customPage.key)
                    .append(' ')
                    .append(command.type);
                if (command.selector != null) {
                    line.append(' ').append(command.selector);
                }
                if (command.data != null && !command.data.isBlank()) {
                    line.append(" data=").append(command.data);
                }
                if (command.text != null && !command.text.isBlank()) {
                    line.append(" text=").append(command.text);
                }
                trace(line.toString());
            }
        }

        private static String plainText(FormattedMessage message) {
            if (message == null) {
                return "";
            }
            StringBuilder builder = new StringBuilder();
            appendMessage(builder, message);
            return builder.toString().trim();
        }

        private static void appendMessage(StringBuilder builder, FormattedMessage message) {
            if (message.rawText != null && !message.rawText.isBlank()) {
                if (!builder.isEmpty()) {
                    builder.append(' ');
                }
                builder.append(message.rawText);
            } else if (message.messageId != null && !message.messageId.isBlank()) {
                if (!builder.isEmpty()) {
                    builder.append(' ');
                }
                builder.append(message.messageId);
            }

            if (message.children != null) {
                for (FormattedMessage child : message.children) {
                    if (child != null) {
                        appendMessage(builder, child);
                    }
                }
            }
        }
    }
}
