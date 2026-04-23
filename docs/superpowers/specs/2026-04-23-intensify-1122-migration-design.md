# Intensify 1.12.2 Migration Design

**Date:** 2026-04-23

**Goal:** Fully migrate `Intensify` from its current `1.16.4`-style implementation to a native `1.12.2` Forge 3 codebase while preserving the existing gameplay surface, and repair `AttributesLib` `1.12.2` integration issues that block that goal.

## Problem Statement

`Intensify` is still structurally a `1.16.4` mod even though the target runtime is now `1.12.2`. The current source still depends on multiple post-1.12.2 platform systems, including:

- `DeferredRegister` and `RegistryObject`
- `mods.toml`
- `ForgeConfigSpec`
- `GlobalLootModifier`
- `LazyOptional`
- `AbstractFurnaceTileEntity`
- Brigadier command registration
- modern text component APIs
- data-generator-oriented resource workflows

That means the current project is not blocked by a few missing renames. It needs a platform-level rehost onto Forge `1.12.2` conventions, with business logic then adapted onto that foundation.

The migration also depends on the in-workspace `AttributesLib` `1.12.2` branch. That branch is already mid-port, so `Intensify` migration must treat `AttributesLib` as a collaborating dependency whose missing or incorrect `1.12.2` behavior can be fixed as part of the same effort.

## Scope

This migration targets full feature parity of the current `1.16.4` gameplay behavior, not just a minimal bootable port.

### Must Preserve

- all intensify stone items and their distinct behavior
- furnace-based intensify workflows
- configuration-driven attribute modification rules
- loot and drop behavior for mining, fishing, and mob kills
- tooltip text and item feedback
- first-login advancement/capability behavior
- command-driven runtime tuning for configured multipliers
- persistent world/player data used by the mod

### May Change Internally

- registration implementation details
- config backend and file format when `1.16.4` APIs have no `1.12.2` equivalent
- command implementation details
- loot injection mechanism
- mixin targets and injection shapes
- resource-generation workflow

The player-facing outcome should remain equivalent even when the underlying `1.12.2` implementation differs.

## Chosen Approach

Rebuild the mod around a native `1.12.2` platform shell first, then migrate gameplay modules onto that shell in bounded slices.

This avoids trying to “polyfill” `1.16.4` patterns into `1.12.2`, which would create a fragile hybrid codebase. The migration will instead separate the work into:

1. **Platform layer migration**
   Replace the loading, registration, config, command, loot, capability, furnace, and resource integration points with `1.12.2`-native equivalents.
2. **Gameplay logic adaptation**
   Preserve and adapt the existing intensify logic so the visible behavior matches the modern version as closely as `1.12.2` allows.
3. **Dependency alignment**
   Repair `AttributesLib` `1.12.2` wherever missing attributes, tooltip support, or registration mismatches prevent `Intensify` from behaving correctly.

## Architecture

### 1. Build and Load Layer

`Intensify` will be converted from ForgeGradle `6` conventions back to ForgeGradle `3` and Forge `1.12.2` metadata/layout conventions.

This includes:

- replacing `mods.toml` with `mcmod.info`
- rewriting `build.gradle` and `gradle.properties` to `1.12.2`/FG3 expectations
- preserving mixin usage, but switching startup, manifest, and refmap configuration to `1.12.2`-compatible wiring
- removing or isolating runtime-irrelevant data generator setup from the active mod build
- keeping resource processing aligned with `1.12.2` packaging rules

The result should be a dev environment that compiles, launches, and loads resources the same way as the already-migrated `AttributesLib` branch.

### 2. Mod Entry and Registration Layer

The current `FMLJavaModLoadingContext` + event bus + `DeferredRegister` model will be replaced with the `1.12.2` lifecycle and registry event model.

Design rules:

- keep existing registry classes when they still provide useful boundaries
- replace delayed registration internals with static instances plus `RegistryEvent.Register<T>` handlers
- register items, recipe serializers/factories, loot conditions, and other forge registries through `1.12.2` events or the appropriate legacy hooks
- keep the main mod class responsible for high-level initialization only

`AttributesLib` dependency resolution will also be updated to a `1.12.2` artifact and verified against the attribute ids `Intensify` expects to consume.

### 3. Configuration and Command Layer

`ForgeConfigSpec` and Brigadier do not exist in usable `1.12.2` form for this mod’s current design, so both systems will be replaced rather than shimmed.

Configuration design:

- migrate global config values such as upgrade and attribute multipliers to `1.12.2` config loading
- preserve semantic meaning of existing keys wherever practical
- allow `probability` and template-based config content to be loaded from `1.12.2`-appropriate file formats
- retain safe defaults so missing or invalid entries do not corrupt gameplay state

Command design:

- reimplement the current `intensify` command tree using `CommandBase`-style `1.12.2` commands
- preserve current subcommand semantics and argument meaning
- keep in-game command feedback aligned with current translated messages

### 4. Gameplay and Persistence Layer

The gameplay layer should preserve the current rules, but all platform seams will be adapted to `1.12.2` types and lifecycle behavior.

This covers:

- intensify stone item behavior
- furnace interaction and recipe execution
- attribute application and item mutation
- loot/drop probability evaluation
- capability storage and retrieval
- world or chunk persistence for replaced block tracking
- first-login progression handling
- tooltip mutation and user feedback

Where the existing code already expresses pure logic, it should be preserved with minimal changes. Where it directly references `1.16.4` runtime types, adapters or local rewrites should convert it to `1.12.2` equivalents.

### 5. Resources and Localization Layer

Only resources that `1.12.2` actually consumes at runtime will remain in the active mod package.

The migration will:

- normalize language resources to `.lang`
- keep or rebuild recipe, loot table, model, and advancement resources in `1.12.2`-compatible form
- reuse generated data only when its structure already matches `1.12.2` runtime expectations
- discard or quarantine `1.16.4`-only generated assets that would confuse maintenance

## Module Boundaries

The migration will keep these responsibilities separated:

- **Platform shell:** build files, metadata, mixin wiring, mod bootstrap
- **Registration:** items, recipes, commands, capabilities, loot hooks
- **Config ingestion:** file loading, defaults, validation, translation into runtime config objects
- **Gameplay logic:** enhancement systems, item mutation, probability logic, furnace flow
- **Persistence:** player capability data, world/chunk data, NBT compatibility
- **Presentation:** tooltip text, translation keys, user-facing feedback
- **Dependency bridge:** `AttributesLib`-specific attribute lookup and compatibility fixes

This separation matters because the port risk is concentrated at the platform boundaries, while the gameplay logic should remain as stable as possible.

## Error Handling

The `1.12.2` port must fail visibly in logs while protecting saves and item data from corruption.

### Rules

- unknown attribute ids from config must be logged with enough context to locate the bad entry, then skipped
- invalid or missing template/config entries must degrade safely rather than crash late during gameplay
- furnace, tile entity, or NBT mismatches must resolve to “do nothing” behavior instead of writing broken state
- first-login and saved-data reads must tolerate absent fields and initialize defaults
- loot/drop hooks must ignore invalid contexts rather than duplicating or corrupting drops

This keeps the mod debuggable without making world recovery dependent on perfect config content.

## Verification Strategy

Verification must prove platform migration and gameplay parity, not just compilation.

### 1. Build Verification

- `AttributesLib` `1.12.2` compiles and can be consumed as the active dependency
- `Intensify` compiles cleanly against that dependency
- no required runtime path still depends on `1.16.4`-only infrastructure

### 2. Logic Verification

Preserve or add JVM-runnable tests for code that does not require the game runtime, especially:

- enhancement probability logic
- attribute calculation logic
- config/template parsing
- conversion helpers
- any regression-prone pure functions discovered during the port

### 3. Runtime Integration Verification

Use the dev environment to verify:

- mod initialization
- registry completion
- config loading
- command execution
- capability attachment and persistence
- tooltip generation
- loot/drop hooks
- furnace intensify flow

### 4. Gameplay Acceptance

At minimum, acceptance must cover:

- all four stone items are registered and visible
- furnace-based enhancement executes correctly
- item attribute/NBT mutation reflects expected intensify outcome
- mining, fishing, and kill drops still produce stones correctly
- commands update multipliers and affect behavior
- first-login progression still fires once
- English and Chinese localization render usable text

## AttributesLib Collaboration Plan

`Intensify` will treat `AttributesLib` as part of the migration surface, not a fixed black box.

When migration exposes `AttributesLib` issues, acceptable fixes include:

- restoring missing `1.12.2` attribute registration
- aligning attribute resource identifiers with `Intensify` config lookups
- fixing tooltip or client display behavior needed to validate intensify outcomes
- correcting build, mixin, or resource issues that prevent `Intensify` from launching or reading attributes correctly

Only fixes that directly support `Intensify` migration or verification are in scope.

## Non-Goals

- preserving the exact `1.16.4` internal architecture
- keeping `1.16.4` data generator pipelines active in the `1.12.2` runtime build
- unrelated refactoring outside migration-critical files
- cosmetic redesign not required for `1.12.2` compatibility

## Success Criteria

The migration is complete when all of the following are true:

1. `Intensify` builds and launches as a native `1.12.2` mod.
2. No blocking `1.16.4`-only infrastructure remains in the active runtime path.
3. Core intensify gameplay features behave equivalently to the source version.
4. `AttributesLib` exposes the attribute and client behavior needed for `Intensify` to function and be validated.
5. Config, persistence, tooltip, command, drop, and furnace flows each have verification evidence.
