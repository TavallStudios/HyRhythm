# HyRhythm

Maven-based Hytale Java plugin scaffold.

## Build

```bash
mvn package
```

The build targets Java 25 and resolves the Hytale server API from CodeMC using:

- `com.hypixel.hytale:Server:2026.02.19-1a311a592`

The plugin jar is written to `target/HyRhythm.jar`.

## Hytale Server Patch Module

The repo also includes `hytale-server-patch/`, a separate module for working on
patches against the upstream Hytale server jar without trying to reconstruct the
entire upstream build.

Generate reference sources from the upstream `Server` jar and install them as a
local `-sources` artifact for IDE debugging:

```bash
scripts/decompile-hytale-server.sh --install-sources
```

Copy a decompiled class into the patch source root:

```bash
scripts/copy-hytale-class-for-patch.sh com.hypixel.hytale.server.core.ui.Anchor
```

Build a patched export jar that overlays the compiled patch classes onto the
original server jar:

```bash
mvn -f hytale-server-patch/pom.xml package
```

The patched server export is written to:

```text
hytale-server-patch/target/Server-2026.02.19-1a311a592-patched.jar
```

## Local Deploy And Smoke

```bash
scripts/hyrhythm-local-smoke.sh
```

This script:

- builds `target/HyRhythm.jar`
- deploys it to `/srv/hytale/Server/mods/HyRhythm.jar`
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

To run the packet-level client validation against the local server:

```bash
scripts/hyrhythm-client-bot-smoke.sh
```

This restarts Hytale in TCP/insecure mode with `--allow-op`, connects a real
protocol bot, drives `/rhythm ui` -> song selection -> chart confirm ->
`/rhythm start`, and completes the debug chart through authoritative
`/rhythm input` commands while the gameplay UI is open. The bot-input mode is
configurable:

```bash
HYRHYTHM_BOT_INPUT_MODE=ui-packet scripts/hyrhythm-client-bot-smoke.sh
```

`ui-packet` keeps the experimental raw `CustomPageEvent` gameplay-input path
available for diagnostics, while `command-input` is the stable smoke path. If a
UI update is invalid enough to disconnect the client, the bot is the one that
gets dropped instead of your real account.

## Local-PC Tunnel Diagnostics

Local-PC port forwarding, reverse-tunnel setup, and Windows-side bridge scripts
are no longer managed from this repository. That workflow now lives in
AgentTaskManager and on the local machine itself.

## Files

- `pom.xml` contains the Maven build and Hytale repository configuration.
- `src/main/resources/manifest.json` is the plugin descriptor Hytale loads.
- `src/main/java/com/hyrhythm/HyRhythmPlugin.java` is the plugin entry point.

## Next steps

Edit the metadata in `manifest.json` and add your plugin logic in `HyRhythmPlugin`.
