# Hytale UI Runtime Pipeline Report

## Scope
This report maps the `.ui` pipeline using the code that is present in this workspace:

- `hytale-server-patch/decompiled-src`
- `hytale-server-patch/src/main/java`
- the main `HyRhythm` plugin sources and assets

The patch module contains the server-side page transport and typed command builders. It does not contain the full client renderer. Where a conclusion depends on missing client code, that is called out explicitly as an inference.

## Executive Summary
- `.ui` documents are not parsed on the server in the available sources. The server sends either a document path or inline markup text to the client through `CustomPage` and `CustomHud` packets.
- The built-in element factory or registry that instantiates `Group`, `Sprite`, `Label`, `TextButton`, and similar element types is not present in `hytale-server-patch`. That logic is client-side.
- The server-side typed property layer that is visible here is the command/value codec stack: `UICommandBuilder`, `Value`, `ValueCodec`, and builder codecs like `Anchor.CODEC`.
- The current HyRhythm gameplay UI does not use a client-local lane runtime. It pre-generates a large `.ui` document full of note sprites and then drives note motion from the server every `16ms` by sending `Set` commands for `.Visible` and `.Anchor`.
- A real client-understood `BeatmapLane` element is not viable under the current constraint that the client cannot be edited. The smallest feasible extension in this repo is server-side or build-time expansion into existing built-ins such as `Group` and `Sprite`, while keeping the current `.ui` transport unchanged.

## Proven File/Class Map
- `hytale-server-patch/decompiled-src/com/hypixel/hytale/server/core/entity/entities/player/pages/CustomUIPage.java`
  Server abstraction for a custom page. Builds command/event arrays and sends `CustomPage`.
- `hytale-server-patch/decompiled-src/com/hypixel/hytale/server/core/entity/entities/player/pages/InteractiveCustomUIPage.java`
  Decodes event payloads back from the client through a `BuilderCodec<T>`.
- `hytale-server-patch/decompiled-src/com/hypixel/hytale/server/core/entity/entities/player/pages/PageManager.java`
  Opens pages, updates pages, waits for client acknowledgements, and routes `Dismiss` or `Data` events.
- `hytale-server-patch/decompiled-src/com/hypixel/hytale/server/core/io/handlers/game/GamePacketHandler.java`
  Receives packet `219` (`CustomPageEvent`) and forwards it into `PageManager`.
- `hytale-server-patch/decompiled-src/com/hypixel/hytale/protocol/packets/interface_/CustomPage.java`
  To-client packet `218`. Carries page key, lifetime, command array, and event binding array.
- `hytale-server-patch/decompiled-src/com/hypixel/hytale/protocol/packets/interface_/CustomUICommand.java`
  Per-command wire payload for `Append`, `AppendInline`, `InsertBefore`, `Set`, `Clear`, and `Remove`.
- `hytale-server-patch/decompiled-src/com/hypixel/hytale/protocol/packets/interface_/CustomUIEventBinding.java`
  Per-binding wire payload for click, value change, focus, drag, tab, and other UI events.
- `hytale-server-patch/decompiled-src/com/hypixel/hytale/server/core/ui/builder/UICommandBuilder.java`
  Server-side builder that turns Java values into `CustomUICommand`.
- `hytale-server-patch/decompiled-src/com/hypixel/hytale/server/core/ui/builder/UIEventBuilder.java`
  Server-side builder that turns event metadata into `CustomUIEventBinding`.
- `hytale-server-patch/decompiled-src/com/hypixel/hytale/server/core/ui/Anchor.java`
  Typed `Anchor` object with a `BuilderCodec<Anchor>`.
- `hytale-server-patch/decompiled-src/com/hypixel/hytale/server/core/ui/Value.java`
  Represents either a direct value or a reference into another `.ui` document.
- `hytale-server-patch/decompiled-src/com/hypixel/hytale/server/core/ui/ValueCodec.java`
  Encodes `Value<T>` either as a direct BSON value or as a `$Document` + `@Value` reference.
- `hytale-server-patch/decompiled-src/com/hypixel/hytale/server/core/entity/entities/player/hud/CustomUIHud.java`
  HUD equivalent of `CustomUIPage`.
- `HyRhythm/src/main/java/com/hyrhythm/ui/RhythmUiManager.java`
  Project entry point that opens custom pages.
- `HyRhythm/src/main/java/com/hyrhythm/ui/RhythmGameplayPage.java`
  Current gameplay runtime that preloads beatmap `.ui` and sends ongoing `.Anchor` / `.Visible` updates.
- `HyRhythm/src/main/java/com/hyrhythm/ui/RhythmChartUiDocumentGenerator.java`
  Current workaround generator that expands a chart into `Group` + many `Sprite` nodes.
- `HyRhythm/src/main/java/com/hyrhythm/ui/RhythmGameplayUiScheduler.java`
  Single-threaded scheduler driving gameplay UI refreshes every `16ms`.
- `HyRhythm/src/main/java/com/hyrhythm/content/RhythmSongLibrary.java`
  Copies built-in UI assets, writes generated beatmap `.ui` files into the generated asset pack, and registers the pack.
- `HyRhythm/src/main/java/com/hyrhythm/ui/RhythmGameplayUiValidation.java`
  Validates the generated beatmap `.ui` structure and required selectors.
- `HyRhythm/src/main/resources/Common/UI/Custom/Pages/InteractiveGameplayUI.ui`
  Current gameplay shell document.

## Current Pipeline
### 1. Page open
- `RhythmUiManager.openGameplay(...)` creates a `RhythmGameplayPage` and calls `player.getPageManager().openCustomPage(...)`.
- `PageManager.openCustomPage(...)` calls `page.build(...)`, wraps the generated commands and bindings into `CustomPage`, and sends packet `218`.

### 2. Server command emission
- `UICommandBuilder.append("Pages/InteractiveGameplayUI.ui")` queues a document-path append.
- `UICommandBuilder.append(selector, documentPath)` queues a child document append under an existing selector.
- `UICommandBuilder.appendInline(selector, document)` queues inline markup text.
- Example inline usage exists in upstream server code:
  - `WarpListPage`: `Label { Text: %server.customUI.warpListPage.noWarps; Style: (Alignment: Center); }`
  - `CommandListPage`: `Group { LayoutMode: Left; Anchor: (Bottom: 0); }`

### 3. Wire format
- `CustomPage` carries:
  - `key`
  - `isInitial`
  - `clear`
  - `lifetime`
  - `commands[]`
  - `eventBindings[]`
- Each `CustomUICommand` carries:
  - `type`
  - `selector`
  - `data`
  - `text`
- This means the server wire layer is already generic enough for new element names or new properties. It does not encode `Group`, `Sprite`, or `BeatmapLane` as protocol enums.

### 4. Client acknowledgement and event return
- `PageManager.updateCustomPage(...)` increments `customPageRequiredAcknowledgments`.
- `GamePacketHandler.handle(CustomPageEvent)` forwards packet `219` back into `PageManager`.
- `PageManager.handleEvent(...)`:
  - decrements the acknowledgement counter on `Acknowledge`
  - drops `Data` events while acknowledgements are still pending
  - forwards `Data` to the active page once the client has acknowledged

### 5. Event decode on the server
- `InteractiveCustomUIPage.handleDataEvent(...)` decodes returned JSON with a `BuilderCodec<T>`.
- `UIEventBuilder` serializes `EventData` with `MapCodec.STRING_HASH_MAP_CODEC`.
- Current HyRhythm gameplay events are simple string maps such as `{"Action":"LaneTap","Lane":"1"}`.

## Where `.ui` Files or Inline UI Markup Are Parsed
### Proven locally
- The server does not parse `.ui` documents in the available sources.
- The server only sends:
  - a document path, for example `Pages/InteractiveGameplayUI.ui`
  - or an inline markup string, for example `Label { ... }`

### Evidence
- `UICommandBuilder.append(...)` stores the document path directly in `CustomUICommand.text`.
- `UICommandBuilder.appendInline(...)` stores the raw markup directly in `CustomUICommand.text`.
- No parser, AST, tokenizer, or element-construction code for `.ui` exists in `hytale-server-patch`.

### Conclusion
- `.ui` parsing is client-side and is not available in this repo.

## Element Factory / Registry For `Group` and `Sprite`
### Proven locally
- `Group`, `Sprite`, `Label`, `TextButton`, and similar names appear only inside `.ui` assets and inline strings in this workspace.
- No local class in `hytale-server-patch` instantiates those element types.

### Conclusion
- The built-in element factory or registry is client-side and missing from the patch overlay sources.

### Inference
- Because the first token of each declaration is a textual type name, the client almost certainly has a registry or lookup table mapping names like `Group` and `Sprite` to concrete element classes.
- If client patching ever becomes possible, that registry is the smallest place to inject a new element name `BeatmapLane`.

## Property Binding / Codec Layer
### Proven locally
- `UICommandBuilder` has a typed `CODEC_MAP` for server-side `Set` operations.
- `Anchor`, `Area`, `DropdownEntryInfo`, `ItemGridSlot`, `LocalizableString`, `PatchStyle`, and `ItemStack` are supported in the visible server-side codec map.
- `Anchor` uses `BuilderCodec<Anchor>` with keys:
  - `Left`
  - `Right`
  - `Top`
  - `Bottom`
  - `Height`
  - `Full`
  - `Horizontal`
  - `Vertical`
  - `Width`
  - `MinWidth`
  - `MaxWidth`
- `ValueCodec<T>` supports either:
  - a direct encoded value
  - or a cross-document reference via `$Document` and `@Value`

### What is not present locally
- No client-side property binder for `.ui` element declarations.
- No local evidence of sprite frame metadata parsing, autoplay flags, sprite animation frame codecs, or a sprite frame-step controller in the UI runtime.

### Conclusion
- The visible typed property layer is only the server-side mutation codec.
- The client-side property binder for element declarations, including any `Sprite`-specific frame metadata, is not present in this repo.

## Runtime Update / Tick Path
### Built-in `Sprite` autoplay / frame stepping
- Not found in the available sources.
- No client runtime classes are present that show a per-element `tick` or an animation-frame advance path for UI sprites.

### Current HyRhythm runtime path
- `RhythmGameplayPage.build(...)` queues the gameplay shell and starts runtime scheduling.
- `RhythmGameplayPage.queueDeferredGameplayPreload()` schedules the chart document append after the shell.
- `RhythmGameplayPage.preloadGameplayRuntimeUi(...)` appends the generated chart `.ui` document under `#BeatmapHost`.
- `RhythmGameplayPage.startRefreshLoop()` schedules `refreshFromClock()` using `RhythmGameplayUiScheduler.scheduleAtFixedRate(..., 16ms)`.
- `RhythmGameplayPage.refreshFromClock()` advances gameplay state and calls `pushSnapshotUpdate(snapshot, "refresh_tick")`.
- `RhythmGameplayPage.applySnapshot(...)` computes note visibility and note top positions.
- `RhythmGameplayPage.applySnapshot(...)` then emits:
  - `Set selector.Visible`
  - `SetObject selector.Anchor`

### Conclusion
- The current HyRhythm scroll path is server-driven and packet-heavy.
- Without client changes, there is no visible path to replace this with a client-local `BeatmapLane` tick.
- Any deployable plan in this repo has to stay on the current server-driven path, or reduce traffic within that path, while still rendering only built-in client-supported elements.

## Current HyRhythm Beatmap Asset Pipeline
### Current shell document
- `InteractiveGameplayUI.ui` defines:
  - `#GameplayRoot`
  - `#BeatmapHost`
  - four lane columns
  - receptor sprites
  - status labels and buttons

### Current generated beatmap document
- `RhythmChartUiDocumentGenerator.generateBeatmapDocument(...)` emits:
  - `Group #GameplayBeatmapRoot`
  - `Group #GameplayBeatmapLane1`
  - `Group #GameplayBeatmapLane2`
  - `Group #GameplayBeatmapLane3`
  - `Group #GameplayBeatmapLane4`
  - one `Sprite` per note

### Current validation
- `RhythmGameplayUiValidation.validateGeneratedBeatmapUiDocument(...)` verifies:
  - the root selector exists
  - lane groups exist
  - every expected note selector exists
  - textures are correct
  - notes are initially hidden

### Result
- The project is currently expanding chart data into `.ui` structure ahead of time because there is no client-local chart runtime element available.

## Smallest Viable Extension Point For `BeatmapLane`
### Proven constraint
- The server transport is already opaque and flexible enough. It transmits raw document paths or markup strings and generic property sets.
- The missing piece is entirely client-side: parser acceptance, element lookup, typed property binding, asset loading, and local tick behavior for a new element token.
- Because the client cannot be edited, a real client-understood `BeatmapLane` cannot be delivered from this repo alone.

### Smallest feasible extension in the current boundary
- Keep the existing `.ui` system unchanged and stay within client-supported built-ins such as `Group` and `Sprite`.
- Treat `BeatmapLane` as a server-side or build-time concept only, not as a runtime element name emitted to the client.
- Expand lane data into existing `.ui` structure before the document reaches the client:
  - at asset generation time in `RhythmChartUiDocumentGenerator`
  - or just before append in `RhythmGameplayPage.preloadGameplayRuntimeUi(...)`
- Keep `Anchor` as the only typed layout property used for note placement, because it already has a proven server codec.

### Recommended injection points under the no-client-edit constraint
- `HyRhythm/src/main/java/com/hyrhythm/ui/RhythmChartUiDocumentGenerator.java`
  Best place to express lane intent and expand it into `Group` + `Sprite`.
- `HyRhythm/src/main/java/com/hyrhythm/ui/RhythmGameplayPage.java`
  Best place to swap the currently generated subtree, preload chart-specific assets, and keep runtime updates isolated to gameplay.
- `HyRhythm/src/main/java/com/hyrhythm/content/RhythmSongLibrary.java`
  Best place to stage chart JSON and generated `.ui` documents into the asset pack.
- `HyRhythm/src/main/java/com/hyrhythm/ui/RhythmGameplayUiValidation.java`
  Best place to validate the expanded built-in subtree so invalid generated UI fails before runtime.

### Why this is the smallest deployable cut
- No change to `CustomPage`, `CustomUICommand`, or `UICommandBuilder` is required.
- No change to server event transport is required.
- No client patch is required.
- No new `.ui` DSL is required.
- The tradeoff is that note motion remains server-driven unless future client capabilities are discovered that are not visible in this repo.

## Recommended BeatmapLane Prototype Contract
The originally requested shape remains useful as a design target, but it is not deployable on the current client because the client cannot be taught a new element token from this repo:

```ui
BeatmapLane #Lane1 {
  ChartPath: "Charts/test.json";
  ScrollSpeed: 1.0;
  ReceptorY: 860;
  Anchor: (Left: 560, Top: 120, Width: 120, Height: 840);
}
```

Concrete prototype artifacts are included in this patch module:

- `hytale-server-patch/docs/prototypes/BeatmapLanePrototype.ui`
- `hytale-server-patch/docs/prototypes/Charts/test.json`

Those files should be treated as non-runnable specification artifacts unless client support becomes available.

### Recommended prototype semantics
- `ChartPath`
  Points to a JSON asset inside the current asset pack, for example `Common/UI/Custom/Charts/test.json`.
- `ScrollSpeed`
  Multiplies the local scroll rate without requiring server updates.
- `ReceptorY`
  Fixed receptor line in local element space.
- `Anchor`
  Uses the existing layout system with no custom layout DSL.

### Recommended prototype JSON shape
Keep the first prototype lane-local to avoid adding more parser surface:

```json
{
  "LaneTexturePath": "../Pages/RhythmGameplayNoteLeft.png",
  "ApproachWindowMs": 3000,
  "BaseNoteHeight": 32,
  "Notes": [
    { "Id": "n1", "StartTimeMs": 640, "EndTimeMs": 640, "Hold": false },
    { "Id": "n2", "StartTimeMs": 1280, "EndTimeMs": 1760, "Hold": true }
  ]
}
```

That keeps the custom-property surface to the exact prototype request and avoids adding a `Lane` property in phase one.

### Deployable fallback contract in the current repo
- Keep the chart JSON.
- Do not emit `BeatmapLane` into shipped `.ui`.
- Continue expanding each lane into:
  - a `Group` container per lane
  - one `Sprite` per note
  - optional additional built-in children for hold-body visuals if needed
- Treat the `BeatmapLane` block above as documentation for a future authoring abstraction, not as runtime markup for the current client.

## Prototype Implementation Plan
### Phase 1: lock the boundary
- Keep the current `.ui` system unchanged.
- Keep the prototype `BeatmapLane` markup only as a design artifact in docs.
- Record explicitly that no runtime path exists for a new element token without client work.

### Phase 2: move toward a server-side `BeatmapLane` concept
- Keep chart JSON as the source asset.
- Refactor `RhythmChartUiDocumentGenerator` so lane rendering is produced from a narrow lane model, for example a `BeatmapLaneSpec` in Java, even though the emitted markup remains only `Group` + `Sprite`.
- Keep parser and property changes at zero by reusing existing `.ui` syntax and server codecs only.

### Phase 3: reduce packet cost without client edits
- Keep the shell document and generated beatmap subtree append path.
- Constrain runtime mutation to active notes only.
- Precompute immutable note geometry where possible so each tick sends only the minimum changing `Anchor.Top` and `Visible` state.
- Consider lowering update frequency or lane-window culling if visual testing shows the current `16ms` path is unnecessarily expensive.
- Keep gameplay input events, stop/close events, and status labels on the server.

### Phase 4: validation and isolation
- Retain `RhythmGameplayUiValidation` for shell-document validation.
- Extend validation toward chart JSON plus generated subtree consistency.
- Keep the chart-to-asset export deterministic so content diffs stay reviewable.
- Keep the lane-generation abstraction isolated so a future renderer swap can replace the expander without touching gameplay rules.

## What Can Stay Unchanged
- `CustomPage` and `CustomPageEvent`
- `UICommandBuilder`
- `UIEventBuilder`
- `PageManager`
- server-side event decode path
- existing `.ui` assets outside gameplay

## What Must Change To Get A Real BeatmapLane Runtime
- Client parser or element-registry lookup
- Client property binder for the new element
- Client UI tick path hookup
- Client asset load path for chart JSON

Because those are all client-side concerns, they are currently out of scope.

## NoesisGUI Migration Risks
- A Hytale-specific custom element would increase renderer coupling, but that risk is currently avoided because no client patch is allowed.
- Current generated `Group` + `Sprite` documents are mechanically simpler to translate than engine-specific custom elements.
- The safest long-term boundary remains the data contract, not the renderer contract.

### Recommended mitigation
- Keep chart JSON renderer-agnostic.
- If a future authoring abstraction is added, keep its property names small and semantic:
  - `ChartPath`
  - `ScrollSpeed`
  - `ReceptorY`
  - `Anchor`
- Keep gameplay state and judgment logic outside the UI element.
- Treat any future `BeatmapLane` abstraction as a thin render adapter over chart data, not as the source of truth for rhythm gameplay.
- Avoid wire-protocol changes. If the transport remains `CustomPage` + generic events, swapping `.ui` for Noesis later only replaces the client render layer and asset authoring layer.

### Net effect of the new constraint
- The no-client-edit boundary actually lowers Noesis migration risk.
- If the project stays with generated built-ins now, later migration can target:
  - chart JSON
  - lane expansion logic
  - gameplay state
- It does not have to preserve a Hytale-only client element implementation that cannot be carried forward.

## Final Recommendation
- Do not patch the server packet layer.
- Do not add a new DSL.
- Do not attempt to ship a real `BeatmapLane` runtime element while the client is off-limits.
- Keep using the existing HyRhythm shell document and built-in elements only.
- If a `BeatmapLane` concept is still valuable, implement it only as a server-side or build-time expansion layer that emits `Group` + `Sprite`.
- Focus near-term work on reducing server-driven UI traffic and isolating lane generation behind a stable chart-data contract.

That is the smallest change set that satisfies:
- no client modifications
- no new `.ui` DSL
- minimal parser/property changes
- minimal migration damage if the renderer later moves to NoesisGUI

It does not satisfy:
- a real custom client-understood `BeatmapLane` element
- local client note scroll with zero per-tick server position updates

Those two goals require client work that is outside the allowed change surface.
