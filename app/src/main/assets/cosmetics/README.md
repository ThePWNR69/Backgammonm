# Cosmetic asset layout — v0.9 compact

This package is intentionally kept under GitHub's 100-file browser upload limit.

## Boards
Each board uses one finished image only:

- `boards/classic_walnut/board.webp`
- `boards/greek_marble/board.webp`
- `boards/modern_teal/board.webp`

Every board image must use the locked 1860×1000 master template and the same point geometry.
The invisible `BoardMap` controls checker positions, hitboxes, bar, bear-off and movement; artwork never controls game logic.

## Checkers
Each checker set uses exactly two assets:

- `light.webp`
- `dark.webp`

All checker images use the same source canvas and are normalised by the renderer to the same displayed diameter.

## Dice
The current ivory dice still use six face assets. A later content expansion can consolidate these into an atlas if extra file-count headroom is needed.

Gameplay, Customise and Store previews must use the same board/checker renderer and BoardMap.
