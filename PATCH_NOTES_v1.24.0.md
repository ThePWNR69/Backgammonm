# Backgammon Legacy v1.24.0 — Realistic Material Rendering

## Visual quality
- Keeps the v1.23 point-grid geometry completely locked.
- Adds a material-texture layer to BoardTheme so realism can improve without moving gameplay geometry.
- Classic Burgundy now uses real material samples derived from the approved finished-product reference for:
  - black leather playing field
  - cream leather points
  - burgundy leather points
  - burgundy side trays
  - walnut material library for future frame refinement
- Textures are clipped to the canonical BoardMap at runtime; triangles, checker anchors, touch zones and animations still share one geometry source.
- Adds directional lighting and two-stage leather edge treatment to each point for a more dimensional, physical-board finish.
- Side trays are redrawn as recessed leather wells with inset shadows and restrained brass trim.
- Existing board frame, hinge artwork and locked dice placement remain unchanged.

## Architecture
- `BoardTheme` now supports optional realistic material assets.
- Existing board themes remain backward compatible through the original constructor.
- Future themes can use the same material system without changing `BoardMap`.

## Gameplay
No rule, AI, XP/Gold, hint, checker-placement, point-grid, dice-placement, Tawla 31 or Mahbouseh logic changed in this release.
