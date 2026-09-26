# Backgammon Legacy v1.6.4 — Gameplay UI Polish

This update is a focused visual correction pass based on the v1.6.3 phone build.
Gameplay rules, dice randomness, AI logic and match scoring are unchanged.

## Board frame symmetry

The production board art had a subtle horizontal imbalance around the centre bar. The
right-side wood/tray area could therefore read thinner than the left even though the
bitmap itself was being scaled uniformly.

v1.6.4 rebalances all three production board skins around the centre bar while keeping
the master canvas at **2048 × 977**. `BoardMap` point, tray, field and bar coordinates
were transformed by the same mapping, so checker anchors, hit testing and animations
continue to match the visible artwork.

## Board aspect ratio is now locked at the gameplay-layout level

The live board is no longer assigned a wide independently-scaled rectangle and then
letterboxed internally. `GameActivity` now gives the `BackgammonBoardView` an actual
rectangle with the production board's **2048:977** aspect ratio.

The board is centred and made as large as the available vertical space permits. This:

- prevents wood/triangle stretching,
- makes the board materially larger on extra-wide phones,
- keeps equal visual treatment on the left and right,
- keeps BoardMap scaling one-to-one with the displayed board bounds.

## Slimmer gameplay HUD

The top player/status plates and bottom controls are shorter than v1.6.3, returning more
screen height to the board. Horizontal placement still follows the approved 1672-wide
reference composition.

The three bottom controls now share the same height and corner geometry. The main action
remains emerald so it is still visually dominant without using a different silhouette.

## Opening-roll presentation

- The initial action is simplified to **Roll**.
- Opening dice are temporarily enlarged and placed at the centre of the playing field.
- After the dice settle, the centre status briefly shows both opening values and who goes first.
- Ties show the two equal values before returning to **Roll Again**.
- AI play waits until the opening result has been shown.

## Checker spacing

Checker diameter was reduced by about 7.5% relative to point-base width. Five-checker
stacks now leave more of the triangle visible and use slightly cleaner spacing while
retaining the existing 6+ stack badge behaviour.

## Bear-off counters

The right tray now includes a subtle **OFF** label with each counter so the two zero/count
values are easier to understand at a glance.

## Background cleanup

The live match screen now uses the clean dark-green gameplay gradient rather than the
prop-heavy tabletop image. This removes the partially cropped decorative objects at the
screen edges. Menu, Store, Customise and Profile retain their existing premium tabletop
artwork.

## Preserved

No changes were made to:

- standard backgammon rules,
- legal move generation,
- higher-die / maximum-dice rules,
- AI evaluation or difficulty behaviour,
- dice randomness,
- match scoring,
- move/undo mechanics,
- cosmetic ownership/unlock logic.

## Build

Version code: **23**  
Version name: **1.6.4**  
GitHub Actions artifact: `Backgammon-Legacy-v1.6.4-APK`
