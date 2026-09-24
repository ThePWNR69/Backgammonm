
## Build hotfix
- Fixed `CustomiseActivity` Android compilation failure caused by the invalid `Button.setTextAllCaps(false)` call.
- Replaced it with the supported `Button.setAllCaps(false)` API.
- Re-ran a full Java source compile against Android API stubs after the fix.

# Patch Notes — v0.9 Rendering Foundation

## Rendering architecture
- Added a permanent, invisible `BoardMap` shared by every board cosmetic.
- Board cosmetics no longer own checker coordinates, hitboxes or movement geometry.
- All 24 points, the bar, bear-off tray, stack anchors and animation endpoints now resolve from one canonical map.
- Board scaling preserves the locked **1.86:1** master aspect ratio on every screen.

## Production board standard
- Locked production board art to **1860 × 1000 px**.
- Added `MASTER_BOARD_TEMPLATE.png` and `MASTER_BOARD_MAP.json` under `production_templates/`.
- Runtime validates production board dimensions before using them.
- Replaced the incorrect Classic Walnut full-board artwork.
- Rebuilt **all three boards** from the same master geometry so triangle placement is identical:
  - Classic Walnut
  - Aegean Marble
  - Midnight Teal

## Visual overhaul
- Classic Walnut rebuilt with deeper walnut framing, inset dark playing surface, recessed trays, brass hardware and stronger edge depth.
- Aegean Marble rebuilt using the same geometry with white marble, navy playing surface and gold details.
- Midnight Teal rebuilt with a dark lacquer/gunmetal frame, teal field and restrained metallic accents.
- Replaced the old surrounding wood filler with a darker premium tabletop / leather-mat background designed to support the board rather than compete with it.
- Reduced gameplay chrome slightly to give the board more available height.
- Refined the main emerald/gold action button and compact dark controls.

## Checker consistency
- Standardised all existing checker source files to a **512 × 512** transparent canvas.
- Normalised visible checker artwork to the same target bounds.
- Added runtime transparent-padding trimming so future checker artwork cannot appear smaller simply because its source file has extra padding.
- All checker sets now use one renderer-controlled physical diameter and identical stack spacing.

## Customise preview fix
- Replaced the old picker flow with a responsive full-screen **Customise** screen.
- Board previews are now rendered by `CosmeticPreviewView` using the exact same `BoardMap`, `BoardGeometry` and static board renderer as gameplay.
- Every board preview uses the same standard starting position; checker stacks are no longer hand-positioned.
- Checker previews use the same runtime visible-bounds normalisation as gameplay, so Ivory & Walnut, Marble & Bronze and Obsidian & Gold display at the same physical diameter.
- Equipping a board/checker/animation writes to the same saved loadout and updates gameplay when returning.

## Alignment and QA
- Added **Show Board Map** to the in-game menu.
- The overlay displays all 24 canonical point anchors plus field/bar/bear-off bounds for checking future skins.
- Added `DESIGN_STANDARDS.md` so new boards and checker sets inherit the same production rules without those requirements needing to be re-specified.

## Existing gameplay retained
- Opening roll
- Vs AI / local 2-player
- dice roll animation
- smooth checker animation
- Undo / End Turn flow
- blue selected checker indicator
- green single-die destinations
- gold two-dice combined destination indicator
- orange capture indicator
- cosmetic board/checker/movement selection

## Developer note
This release intentionally concentrates on eliminating rendering/alignment problems before expanding the ruleset and match-setup system further. The board skin can now be replaced without changing any gameplay map data.

## GitHub compact packaging revision
- Repacked the same v0.9 test build to **80 total files** so it can be uploaded through GitHub's browser upload flow without hitting the 100-file limit.
- Removed obsolete per-board `frame.webp`, `field.webp`, `bar.webp` and duplicate `preview.webp` assets.
- Each board now ships only its canonical `board.webp`, matching the intended invisible-BoardMap + static-board-image architecture.
- No gameplay rules, checker mapping or AI behaviour were intentionally changed by this packaging revision.
