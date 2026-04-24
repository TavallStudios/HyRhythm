# Hytale Server Patch Module

This module is a patch-overlay project for the upstream Hytale server jar.

## What lives here

- `decompiled-src/` contains decompiled reference sources for the Hytale packages extracted from the upstream server jar.
- `src/main/java/` is where patched classes should live.
- `src/main/resources/` is where patched resources should live.

## Reference source workflow

Generate or refresh the reference source tree:

```bash
scripts/decompile-hytale-server.sh --install-sources
```

That script decompiles `com/hypixel/**` from the upstream `Server` jar into
`hytale-server-patch/decompiled-src` and optionally installs a matching
`-sources` jar into the local Maven cache for IDE source attachment.

## Patch workflow

Copy a class from the reference tree into the patch source root:

```bash
scripts/copy-hytale-class-for-patch.sh com.hypixel.hytale.server.core.ui.Anchor
```

Edit the copied file under `src/main/java`, then build the patched server jar:

```bash
mvn -f hytale-server-patch/pom.xml package
```

The patched export is written to:

```text
hytale-server-patch/target/Server-2026.02.19-1a311a592-patched.jar
```
