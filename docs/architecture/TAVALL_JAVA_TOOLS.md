# HyRhythm Tavall Java Tools Contract

HyRhythm is a Tavall-owned Java consumer. Tavall DI is the universal first-party composition/lifecycle baseline, including the Tavall-owned Hytale server patch project. External Hytale host/plugin discovery remains an external platform boundary.

## Current migration targets

HyRhythm currently contains first-party infrastructure that predates the canonical Tavall Java Tools platform:

- `com.hyrhythm.dependency.DependencyLoader` / `DependencyLoaderAccess` -> Tavall DI.
- `com.hyrhythm.bootstrap.registries.*` -> Tavall DI for object lifecycle plus Tavall Registry where keyed catalog semantics are genuinely required.
- `com.hyrhythm.logging.RhythmRuntimeLogger` and logging access wrappers -> Tavall Logging, preserving any Hytale logger bridge only as the platform adapter.
- `com.hyrhythm.ui.RhythmGameplayUiScheduler` -> Tavall Scheduler. This migration begins on the current platform-adoption branch.
- reusable reflection/scanning -> Tavall Reflection.
- generic async/coordination -> Tavall Concurrency.
- generic typed in-process events -> Tavall EventBus when needed.
- bounded/expiring state -> Tavall Cache when needed.
- database persistence -> Tavall Database if introduced.

## ServiceLoader boundary

The `src/serviceLoaderTest` source set currently tests HyRhythm's own dependency/bootstrap isolation. That is first-party migration debt, not a permanent exception. Those tests should be replaced by Tavall DI/Registry lifecycle tests as the custom loader/registry stack is removed.

If Hytale itself requires ServiceLoader or another discovery mechanism at an external host/plugin seam, that platform contract may remain. Once a Tavall-owned object enters HyRhythm, Tavall DI owns its first-party composition/lifecycle.

## Scheduler migration

`RhythmGameplayUiScheduler` preserves its small product-facing API but now delegates delayed/repeating execution to `org.tavall.scheduler.CustomScheduler`, removing the local `ScheduledExecutorService`, custom thread factory, and shutdown implementation.

## Naming/ownership cleanup

Existing `*Manager` classes are legacy product debt and are not templates for new code. This platform migration does not rename unrelated gameplay classes merely to create diff noise, but touched infrastructure should converge on the current Tavall Service/Handler/Orchestrator/Registry/Runtime naming rules.

Exact Java 25 tests, Hytale plugin startup, UI timing acceptance, and local client-bot smoke are required before promotion.

Tavall Java tools are declared as exact source dependencies in `.tavallci` and composed by Tavall CI. Maven Local and GitHub Packages are not internal dependency authorities; CodeMC remains only for the external Hytale server artifact.
