# HyRhythm

Hytale Java plugin built with Gradle Kotlin DSL and Java 25.

## Build

```bash
./gradlew --no-daemon clean check stageDistribution
```

The build resolves `com.hypixel.hytale:Server:2026.02.19-1a311a592` from the
CodeMC Hytale repository as a provided platform dependency. The deployable thin
plugin is staged at:

```text
distribution/mods/HyRhythm.jar
```

Dependency locking is enabled. Refresh committed lock state deliberately with
`./gradlew --write-locks` when dependency declarations change.

## Hytale Server Patch Module

The `hytale-server-patch/` subproject supports focused patches against the
upstream Hytale server jar without reconstructing the upstream build.

Generate reference sources and a local source-attachment jar:

```bash
scripts/decompile-hytale-server.sh --build-sources
```

Copy a decompiled class into the patch source root:

```bash
scripts/copy-hytale-class-for-patch.sh com.hypixel.hytale.server.core.ui.Anchor
```

Build a patched export that overlays the compiled patch classes onto a copy of
the original server jar:

```bash
./gradlew --no-daemon :hytale-server-patch:patchedServerJar
```

The patched server export is written to:

```text
hytale-server-patch/build/distributions/Server-2026.02.19-1a311a592-patched.jar
```

This overlay is an intentional server-patch distribution. The ordinary
HyRhythm plugin remains a thin jar and does not embed Hytale platform classes.

## Local Deploy And Smoke

```bash
scripts/hyrhythm-local-smoke.sh
```

This script:

- builds `distribution/mods/HyRhythm.jar`
- preserves the currently deployed plugin as a timestamped rollback copy
- deploys the new jar to `/srv/hytale/Server/mods/HyRhythm.jar`
- starts the local Hytale server in a tmux session
- runs the command-driven playable loop smoke test
- stops the tmux-backed server unless `--keep-running` is supplied

Low-level tmux-backed server control is available through:

```bash
scripts/hytale-local-server.sh start
scripts/hytale-local-server.sh send "rhythm debug on"
scripts/hytale-local-server.sh capture 200
scripts/hytale-local-server.sh stop
```

To run packet-level client validation against the local server:

```bash
scripts/hyrhythm-client-bot-smoke.sh
```

This restarts Hytale in TCP/insecure mode with `--allow-op`, connects a real
protocol bot, drives `/rhythm ui` through chart confirmation and gameplay, and
checks the authoritative final state. The bot-input mode is configurable:

```bash
HYRHYTHM_BOT_INPUT_MODE=ui-packet scripts/hyrhythm-client-bot-smoke.sh
```

`ui-packet` preserves the experimental raw `CustomPageEvent` input path for
diagnostics. `command-input` is the stable smoke path.

## Local-PC Tunnel Diagnostics

Local-PC port forwarding, reverse-tunnel setup, and Windows-side bridge scripts
are managed by AgentTaskManager and the local machine, not this repository.

## Important Files

- `build.gradle.kts` defines the Java 25 build, publication, verification, and distributions.
- `src/main/resources/manifest.json` is the filtered plugin descriptor Hytale loads.
- `src/main/java/com/hyrhythm/HyRhythmPlugin.java` is the plugin entry point.
