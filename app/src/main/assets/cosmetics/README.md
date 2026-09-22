# Cosmetic artwork folder

The renderer looks for optional artwork under this folder. If an expected file is missing, the game falls back to the procedural fallback drawing, so artists can replace pieces gradually.

## Board theme layers

A board theme may provide these transparent or opaque WebP/PNG layers:

- `boards/<theme>/frame.webp` — full outer-board rectangle, including trays if desired.
- `boards/<theme>/field.webp` — playing-field rectangle only.
- `boards/<theme>/points.webp` — transparent overlay for all 24 points/details.
- `boards/<theme>/bar.webp` — centre bar artwork.

The paths are declared in `CosmeticCatalog`/`BoardTheme`.

## Checker artwork

- `checkers/<theme>/light.webp`
- `checkers/<theme>/dark.webp`

Use a square transparent image. The game scales it to the physical checker radius and adds the movement/selection effects separately.

## Dice artwork

- `dice/<theme>/die_1.webp` through `die_6.webp`

Use square transparent images. Rotation and dice-roll motion are applied by the renderer.

This separation is intentional: a visual set can be changed without changing the rules engine, AI, or turn logic.

- `checkers/obsidian_gold/light.webp` + `dark.webp` are optional future production artwork; v0.7 has a procedural fallback with gold detailing.
