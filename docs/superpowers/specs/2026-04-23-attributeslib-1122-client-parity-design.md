# AttributesLib 1.12.2 Client Parity Design

**Date:** 2026-04-23

**Goal:** Restore `AttributesLib` 1.12.2 client behavior so the mod both launches through `runClient` and exposes the same user-facing attribute inspection features as `AttributesLib1`, adapted to 1.12.2 APIs.

## Problem Statement

The current `1.12.2-cur` branch can be compiled and, after recent startup fixes, can launch a client, but two major client-facing systems remain functionally incomplete:

1. Item attribute tooltip rewriting is missing.
2. The inventory attribute panel is still stubbed.

The missing behavior is not a subtle regression. The current branch contains placeholder implementations for:

- `client/AttributesGui`
- `client/ModifierSource`
- `client/ModifierSourceType`
- `client/AttributeModifierComponent`

The current branch also lacks the `ItemStackMixin` and `ItemTooltipEvent` rewrite path used by `AttributesLib1` to replace vanilla attribute tooltip sections with enriched formatting and grouping.

## Design Constraints

- The reference behavior is `AttributesLib1`, but the implementation must use 1.12.2 client APIs.
- Behavior parity matters more than code-structure parity.
- Existing 1.12.2 fixes already in progress must be preserved.
- Existing config switches must become functional again:
  - `ALConfig.enableAttributesGui`
  - `ALConfig.enablePotionTooltips`
  - `ALConfig.hiddenAttributes`

## Chosen Approach

Implement feature parity with native 1.12.2 rendering and event hooks instead of trying to mechanically preserve 1.16.4 internals.

This means:

- Port the tooltip-rewrite pipeline into the existing 1.12.2 `AttributesLibClient`.
- Reintroduce an `ItemStackMixin` for tooltip markers using the 1.12.2 `ItemStack#getTooltip` signature.
- Replace the current GUI stubs with a 1.12.2 `GuiButton`/inventory-screen compatible attribute panel that preserves the same user-visible behavior:
  - inventory toggle button
  - scrollable attribute list
  - hide-unchanged toggle
  - attribute detail tooltip
  - modifier source attribution for items and effects

## Architecture

### 1. Tooltip Rewrite Pipeline

`ItemStackMixin` will inject two marker lines into the vanilla tooltip list around the modifier section. `AttributesLibClient` will detect those markers during `ItemTooltipEvent`, remove the vanilla section, and insert enriched attribute lines in its place.

The rewritten tooltip path will preserve the reference semantics from `AttributesLib1`:

- slot-grouped modifier headers
- dual-hand grouping
- skipped modifier support through `GatherSkippedAttributeTooltipsEvent`
- base-value merging for attributes with base UUIDs
- advanced-tooltip debug data
- potion description augmentation through the existing potion tooltip hook

### 2. Inventory Attribute Panel

The 1.12.2 branch will replace the current GUI placeholders with a real inventory-side widget system built on 1.12.2 GUI primitives.

The panel will:

- attach only to inventory-like screens where the reference feature belongs
- maintain the same visibility state semantics as the reference implementation
- filter hidden attributes using `ALConfig.hiddenAttributes`
- optionally hide unchanged attributes
- render attribute values and per-attribute details with modifier breakdowns
- display modifier provenance for equipment and active effects

The 1.12.2 implementation does not need to be pixel-identical to 1.16.4, but it must provide the same information and interaction affordances.

### 3. Modifier Source Modeling

The existing stub classes will be replaced with 1.12.2-compatible implementations that restore:

- equipment source extraction
- potion/effect source extraction
- source ordering for tooltip/detail display
- source rendering helpers usable by the inventory attribute panel

These classes should stay focused on data/source rendering responsibilities rather than absorbing screen lifecycle logic.

## File-Level Scope

Expected primary touch points:

- `src/main/java/dev/shadowsoffire/attributeslib/client/AttributesLibClient.java`
- `src/main/java/dev/shadowsoffire/attributeslib/client/AttributesGui.java`
- `src/main/java/dev/shadowsoffire/attributeslib/client/ModifierSource.java`
- `src/main/java/dev/shadowsoffire/attributeslib/client/ModifierSourceType.java`
- `src/main/java/dev/shadowsoffire/attributeslib/client/AttributeModifierComponent.java`
- `src/main/java/dev/shadowsoffire/attributeslib/mixin/ItemStackMixin.java`
- `src/main/resources/attributeslib.mixins.json`

Supporting updates may be required in helper classes if 1.12.2 formatting or rendering gaps are exposed during the port.

## Verification Plan

The work is complete when all of the following are true:

1. `./gradlew --no-daemon compileJava` passes.
2. `./gradlew --no-daemon runClient --stacktrace` launches and stays running without startup crash.
3. Opening inventory in the client exposes the attribute panel when enabled in config.
4. Attribute panel data is scrollable and respects hidden/hide-unchanged filters.
5. Item attribute tooltips no longer show the unmodified vanilla modifier block when the rewrite path is active.
6. Potion and effect tooltip enhancements still work after the port.

## Non-Goals

- Pixel-perfect recreation of the 1.16.4 GUI layout.
- Refactoring unrelated common-side systems.
- Cleaning non-blocking Forge startup warnings unless they directly block parity work.
