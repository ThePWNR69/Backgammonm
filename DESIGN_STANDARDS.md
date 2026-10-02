# Backgammon Legacy — locked production standards (v1.6.5)

These are architecture rules, not per-theme preferences. Future boards/checkers must follow them automatically.

## 1. One permanent invisible BoardMap

Gameplay geometry lives in `rendering/BoardMap.java` and is projected by `BoardGeometry.java`.

The map owns:
- all 24 point locations
- stack anchors
- bar anchors
- bear-off tray anchors
- touch/hit regions
- movement/animation start and end positions

Board artwork never supplies gameplay coordinates.

## 2. Master board artwork

Production board size: **2048 × 977 px** (aspect ~**2.096:1**).

Every board image must retain the same:
- 24 triangle positions
- point bases and tips
- centre bar
- left/right trays
- playing-field rectangle
- overall aspect ratio

Only materials, colours, ornamentation and lighting may change.

Runtime protection: `StaticBoardRenderer` accepts a production board only when it matches the master dimensions exactly. An invalid board falls back to the debug renderer rather than silently misaligning gameplay.

### Canonical pixel regions
- Field envelope: x **236–1840**, y **58–916**
- Left tray: x **78–219**
- Right tray: x **1849–1976**
- Bar: x **996–1085**
- Point height: measured per triangle from its base to its apex (approximately **358–392 px**)

## 3. Layering

Runtime stack:
1. premium tabletop/background artwork
2. selected static board image
3. invisible BoardMap (logic/hitboxes)
4. checkers
5. dice
6. move indicators
7. movement/dice animations

Board art contains no live checker/dice state.

## 4. Checker sizing

Source checker canvas: **512 × 512 px**.
Target visible checker content: **448 px diameter**, centred.

On-screen checker size comes only from `BoardGeometry`. Theme artwork cannot make one set larger/smaller.
`AssetTextureRepository.visibleBounds()` trims source transparent padding at runtime before fitting artwork to the same render square.

## 5. Stack standard

- Up to five physical checker positions are shown on a point.
- Five visible checkers finish at approximately the point tip.
- 6+ uses a count badge instead of extending into the centre.

## 6. Shared previews

Gameplay, Store and Customise use the same board image, BoardMap and checker renderer. No hand-positioned preview pieces and no alternate preview geometry.

## 7. Visual target

The approved premium gameplay reference is the production target for Classic Walnut:
- deep walnut case with physical bevel/depth
- black leather playing surface
- cream + walnut points
- polished ivory/walnut checkers
- brass/gold trim
- dark emerald luxury tabletop
- burgundy leather player panels and bottom controls with restrained antique-gold edging
- faceted brushed-gold turn/status plaque
- dark secondary Undo/Hint buttons with gold trim
- compact UI so the board remains the focus

## 8. New board workflow

1. Start from the 2048×977 canonical template/regions above.
2. Never move any gameplay geometry.
3. Redesign only the materials/art treatment.
4. Export to `app/src/main/assets/cosmetics/boards/<id>/board.webp`.
5. Register the theme in `CosmeticCatalog`.
6. Verify with in-game **Show Board Map** before release.

## 9. New checker workflow

1. Use a 512×512 transparent canvas.
2. Centre the visible disc to the standard bounds.
3. Export top-down `light.png` and `dark.png` (transparent 512×512 source canvas).
4. When the set supports bear-off presentation, also export `light_side.png` and `dark_side.png` as transparent horizontal edge-view assets.
5. Register all supplied assets plus LIGHT / DARK gameplay-side metadata in `CosmeticCatalog`.
6. Confirm top-down art renders the same diameter as every existing set and side art fits the common bear-off tray slot geometry.

## 10. GitHub browser upload limit

The distributable source package must remain below **100 files**. Reuse assets and render previews from production assets rather than adding duplicate preview files.

## Triangle-centred checker placement

Checker X positions are **not** a generic column centre. Each production point has measured base-left, base-right and apex coordinates. At every checker slot Y, the renderer interpolates both sloping triangle edges and places the checker at their exact midpoint. The same rule applies to landing previews, move indicators and animation endpoints. Future board artwork must preserve these triangle vertices.
## 11. App-wide premium UI system

The approved VS Bot Setup and gameplay HUD are the visual source of truth for non-board screens. All screens should use the same design language:
- dark emerald tabletop/background
- walnut/brown framed panels
- cream/gold serif headings
- emerald primary controls with gold borders
- dark secondary controls with gold borders
- consistent rounded back button, tabs, cards and score badges
- restrained decorative elements; the board/content remains the focus
- no low-contrast grey/green body text when warm cream/gold is clearer

Gameplay player nameplates show only the checker icon, player name and rounds won. Do not reintroduce OFF/BAR/colour/direction text into the nameplates.

Setup screens use the same two-panel pattern: settings on the left, rules/summary on the right, with a single strong emerald START MATCH action. Layouts must remain compact enough for landscape phones and must not clip at the bottom.


## 12. Gameplay HUD proportions

The live match screen remains board-first. On extra-wide phones the physical board must be
given a rectangle that already matches the production **2048:977** aspect ratio; do not
stretch a board-sized View and rely on internal letterboxing.

Top name/status plates and bottom controls should remain compact and share a common control
height. Horizontal spacing can still derive from the approved 1672-wide reference. Vertical
spacing is allowed to compress on short/wide phones so the board gets the maximum practical
height.

## 13. Uniform board scaling (v1.6.4)

For gameplay:

- determine the safe landscape window,
- reserve compact top HUD and bottom-control bands,
- calculate the remaining board slot height,
- size the board with one uniform scale using `BoardMap.MASTER_ASPECT`,
- centre the board horizontally,
- never independently scale board width and height.

HUD text/icons may use their own UI scale, but the board art, point map, checker anchors,
hitboxes and animation endpoints must all share the final board rectangle.

## 14. Symmetric master board art (v1.6.4)

Production board size remains **2048 × 977 px**. The v1.6.4 master geometry is horizontally
rebalanced around the centre bar. All production board themes use that same rebalance, and
`BoardMap` stores the matching post-rebalance coordinates.

Do not introduce a board skin based on the pre-v1.6.4 horizontal geometry. A new board must
be authored/aligned to the current production template and validated with **Show Board Map**.

## 15. Gameplay UI polish rules

- Opening dice may be temporarily enlarged and centred for the opening-roll presentation.
- Normal-turn dice return to the standard right-centre board position.
- Opening-roll result text should appear briefly before AI movement begins.
- Main action, Undo and Menu use one control height and one corner family.
- Player plates contain checker identity, player name and match score only.
- Bear-off trays may use a restrained `OFF` label; avoid adding dense metadata to the board.
- The live gameplay background should remain visually quiet enough that the board is the hero.

## 15. Five-checker point coverage (v1.6.5)

Checker-to-point geometry is now a locked renderer invariant:

- The gameplay checker diameter is **90% of the narrowest visible triangle base width**.
- A five-checker stack begins flush with the triangle base and spans the complete triangle height.
- The fifth checker reaches and slightly covers the apex so the triangle tip cannot remain visible after scaling or anti-aliasing.
- The required centre-to-centre pitch is calculated independently for each measured point from its real base-to-apex height.
- The stack remains centred on the measured midpoint between the two triangle edges at each checker Y position.
- 1-4 checkers use the same pitch; 6+ continues to use the count-badge treatment until a later high-stack design is approved.

Reference design relationship: a **100 px** triangle base uses a **90 px** checker. For a **278 px** point height, five 90 px checkers use approximately **47.56 px centre-to-centre pitch**, including the small apex-cover guard.

Do not tune checker size or stack spacing independently per cosmetic theme.


## 16. v1.10.0 approved gameplay-layout override

For the **live match screen only**, sections 12–13's older uniform-aspect gameplay-board rule are superseded by `APPROVED_GAMEPLAY_LAYOUT.md` and `design/reference/CLASSIC_BURGUNDY_APPROVED.png`.

The approved match screen is a single **510 × 280 logical composition**. The board occupies `0,44,510,178` inside that composition and the canonical 2048×977 `BoardMap` is projected independently on X/Y into that exact rectangle. This intentional presentation projection is what makes the runtime screen match the approved wide/short concept while preserving one map for artwork, anchors, hit regions and animation endpoints.

The board source assets themselves remain 2048×977 and Store/Customise previews may still show the canonical source aspect. Do not change the live gameplay rectangle without replacing the approved reference and updating `APPROVED_GAMEPLAY_LAYOUT.md` in the same release.

Classic Burgundy UI chrome is stored as raster theme resources at 4× logical size in `res/drawable-nodpi/premium_*`. Button state selectors must use Normal / Pressed / Disabled assets rather than substituting generic Android button styling.

## 17. v1.11.0 slim-chrome gameplay override

v1.11.0 supersedes the v1.10.0 live-match rectangles in section 16 after in-game review showed the 42-unit top HUD and 56-unit bottom deck visually compressing the board.

The overall logical frame remains **510 × 280**, but the live match now uses:
- top controls: **30** logical units high,
- live board: `0,32,510,207`,
- bottom control deck: **39** logical units high.

The board must remain the dominant visual element. Do not increase HUD/control height to solve text or icon problems; refine typography/assets within the locked chrome instead.

Classic Burgundy chrome uses thin double antique-gold edges, restrained leather texture, compact faceted score/turn treatments and no glow. Normal / Pressed / Disabled button states remain mandatory.


## 18. v1.12.0 detailed premium chrome lock

v1.12.0 keeps every v1.11 runtime rectangle unchanged. The screenshot at `design/reference/V1_12_SIZE_REFERENCE.jpg` is the accepted size/proportion reference; future polish work must not make the top or bottom chrome taller unless a later explicit design decision replaces this lock.

Classic Burgundy detail requirements:
- layered antique-gold outer rim plus darker inner hairline,
- subtle leather grain and restrained stitched seams,
- faceted recessed score bays,
- small metal rivet/stud details used sparingly,
- a brushed-metal centre turn plaque with a subtle inset title plate,
- recessed framed dice wells in the bottom deck,
- visual separators between dice / primary action / utility control zones,
- Normal / Pressed / Disabled button assets with shadow/compression changes,
- no neon glow or oversized ornamentation.

The board remains the hero. Additional detail should come from material treatment, depth, fine trim, typography and icon polish rather than increasing chrome size.

## Checker side metadata (v1.18+)
Every checker theme must define a paired LIGHT and DARK gameplay side. The art does not need to be literally light/dark coloured. Match Setup Auto opponent selection reads the user's equipped `checker_side` and assigns the opposite tagged side. New checker sets must preserve this metadata convention.

## Match Setup visual lock — v1.19.0
- Keep the v1.18 setup geometry and sliding-selector layout unchanged unless explicitly requested.
- Approved finish reference: `design/reference/MATCH_SETUP_3D_FINISH_APPROVED.png`.
- Setup chrome uses layered 3D assets: burgundy leather panels, emerald selector rails, multi-stage gold bevels, subtle texture, recessed shadows, specular highlights, and dimensional medallions.
- All option labels, selected values, rules text, and Start Match text remain live Android text; they are never baked into the art.
- Game Version changes the Match Rules content dynamically.
- VS Bot uses `Bot colour: Auto / Light / Dark`; 2 Player uses `Player 2 colour: Auto / Light / Dark`.
- Auto must use checker metadata `LIGHT` / `DARK` pairing, never image colour inference.

## v1.20 geometry + compositing lock
- Checker anchors must be measured from the current production board artwork, not inherited from an older board skin.
- `BoardMap` v1.20 coordinates are measured from `classic_burgundy/board.webp` at 2048 x 977.
- Light/dark checker art for the Classic set uses clean transparent PNG sprites; no baked rectangular background or rough cut-out halo is allowed.
- Player plaques contain only chrome and score bays. Player/AI identity icons are live Android `ImageView`s so they cannot be clipped by a bitmap crop.
- Menu and settings buttons use separate complete assets; neither may reuse a partially cropped source image.
- The centre status plaque must have complete left and right ends with transparent breathing room.

## v1.21.0 Gameplay polish lock
The approved gameplay finish is defined by `design/reference/APPROVED_GAMEPLAY_POLISH_REFERENCE.png` and the alternate approved reference beside it. Future gameplay themes must preserve the locked BoardMap and live text/dynamic pieces while matching the same level of material depth: polished walnut, dark leather, burgundy leather, layered brass/gold bevels, soft integrated shadows, complete anti-aliased edges, and clean checker/dice sprites. UI assets must not contain dynamic names, scores, turn text, dice values, or checker positions.

## v1.22 geometry source-of-truth lock
- The 24 playable point triangles are runtime geometry, not trusted baked board artwork.
- BoardMap is the single source for point edges, checker centres, hit regions and animation endpoints.
- Classic Burgundy board art supplies physical frame/tray/bar material underneath this geometry layer.
- Bottom-deck dice views must be derived from the actual well rectangles in the deck asset; never hand-offset by eye.


### Realistic material standard (v1.24+)
- Realism is achieved with tileable material textures clipped into the locked BoardMap, not by regenerating full board artwork with new triangle geometry.
- Point textures must never define their own triangle edges. `BoardMap` defines all edges.
- Texture improvements may change grain, leather pores, wood figure, gloss, shadows and highlights only.
- The Classic Burgundy baseline uses approved-reference material samples for black leather, cream leather, burgundy leather and walnut.


## v1.25.0 tabletop + checker asset lock
- The default tabletop is a clean dark emerald cloth/felt image with subtle texture only. No plants, dice bowls, cases, props, or decorative objects are baked into the background.
- Background art is independent from board/checker cosmetics so a future Background cosmetic category can replace it without touching BoardMap.
- The starting checker set is **Classic Wood**: pale maple LIGHT and dark walnut DARK.
- Each checker theme may carry independent top-down and side-view artwork. Top-down art is used on points/bar; side-view art is used for borne-off pieces in the right end-zone tray.
- `Auto / Light / Dark` opponent selection continues to use `lightSideTag` / `darkSideTag`; it never infers gameplay side from pixel colour.

## v1.26 starter simplicity + checker parity lock
- The default Classic Burgundy starter board uses simple cream/burgundy points. Do not add repeating motifs, diamonds, embossed symbols or decorative cut-outs to starter points.
- Starter points may use a restrained gradient and single seam only. Premium themes can add richer material detail later without changing BoardMap geometry.
- Classic Wood LIGHT and DARK top-down sprites must share the same canvas size and visible bounds; only wood colour/grain changes.
- Classic Wood side-view bear-off sprites must likewise share identical geometry.
- Bottom action/utility button frames are measured from the actual wells in `premium_bottom_deck.webp`; do not hand-offset them independently.
