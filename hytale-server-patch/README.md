# Hytale Server Patch Module

This subproject overlays focused patched classes and resources onto a copy of
the upstream Hytale server jar.

## What lives here

- `decompiled-src/` contains decompiled Hytale reference sources.
- `src/main/java/` contains patched classes.
- `src/main/resources/` contains patched resources.

## Reference source workflow

Generate or refresh the reference source tree and build a local source jar:

```bash
scripts/decompile-hytale-server.sh --build-sources
```

The script obtains the stable upstream server reference through Gradle,
decompiles `com/hypixel/**` into `hytale-server-patch/decompiled-src`, and
writes the optional source jar under `build/reference-sources/`.

## Patch workflow

Copy a class from the reference tree into the patch source root:

```bash
scripts/copy-hytale-class-for-patch.sh com.hypixel.hytale.server.core.ui.Anchor
```

Edit the copied file, then build the patched server export:

```bash
./gradlew --no-daemon :hytale-server-patch:patchedServerJar
```

The patched export is written to:

```text
hytale-server-patch/build/distributions/Server-2026.02.19-1a311a592-patched.jar
```
