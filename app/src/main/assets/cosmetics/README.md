# Cosmetic artwork folder

The renderer looks for optional artwork under this folder. Missing files fall back to the procedural renderer.

## Board artwork
A board theme can now provide either:

- `boards/<theme>/board_base.webp` — a complete, empty, production-rendered board aligned to `BoardGeometry`; or
- the older layered approach:
  - `frame.webp`
  - `field.webp`
  - `points.webp`
  - `bar.webp`

`Classic Walnut` in v0.8 is the first theme using `board_base.webp`. Checkers, dice, highlights and animation remain dynamic above that artwork.

## Checker artwork
- `checkers/<theme>/light.webp`
- `checkers/<theme>/dark.webp`

Use a square transparent image. Selection/ghost/shadow/movement effects are separate.

## Dice artwork
- `dice/<theme>/die_1.webp` through `die_6.webp`

Use square transparent images. Rotation and dice-roll motion are handled by the renderer.
