# Backgammon Legacy v1.29.0 — Graphics Fix (match the approved render)

Gameplay, rules, AI, progression, setup screen and the locked v1.23 BoardMap are unchanged.

## Root causes found

1. **Dull, grey-looking points (the main "looks nothing like the render" problem).**
   `StaticBoardRenderer.drawTriangleExact()` ended each point with a seam stroke whose colour had alpha 145.
   The next point's gradient fill reused the same shared `Paint`, and an Android `Paint`'s alpha multiplies
   its shader output, so 23 of the 24 points were drawn at about 57% opacity over the black field
   (cream became grey-beige, burgundy became dim maroon). The first point was ~86%, which is why the
   top-left point looked brighter than the rest. The field and tray fills had the same leak from the
   gold tray line colour (alpha 220). Colour tuning in earlier releases could never fix this.
   Fix: reset the Paint to opaque white before every shader fill (3 one-line changes).
2. **Olive box behind the centre status text.** The inner title window of `premium_status.webp` was only
   ~28% opaque, so the green felt showed through, and longer messages spilled past it.
   Fix: that window is now an opaque, wider, recessed brushed-gold plate (`tools/fix_status_plaque.py`).

## Visual changes toward `design/reference/CLASSIC_BURGUNDY_REALISM_TARGET.png`

- Default checkers are now glossy **ivory** and **dark chocolate** discs with a gold rim and soft gloss
  (`tools/build_gold_ring_checkers.py`). Same ids/paths (`checkers_ivory_walnut`), identical light/dark
  geometry, new matching edge-on bear-off sprites. Display name is now **Classic Ivory**.
- Point colours re-sampled from the render: cream `#EED2A8`, crimson `#781B21`.
- Board frame walnut graded deeper and browner (`tools/grade_board_frame.py`); brass plates, compass inlay,
  gold trim and burgundy trays are untouched. Geometry is not changed.

GitHub APK: `Backgammon-Legacy-v1.29.0.apk`
