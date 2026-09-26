# Backgammon Legacy — locked production standards (v1.6.3)

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
- dark brown player panels and emerald/gold status/action controls
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
3. Export `light.webp` and `dark.webp`.
4. Register the set in `CosmeticCatalog`.
5. Confirm it renders the same diameter as every existing set.

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

For wide landscape phones, keep the live match screen board-first and follow the canonical reference rectangles below. In the 1672 × 941 master composition:
- top name/status plates occupy `y=14..94`
- board occupies `y=104..795`
- bottom controls occupy `y=811..891`
- checker icons and round-score badges must remain fully inside the player nameplates
- centre status uses small symmetrical gold ornaments, never extra gameplay metadata
- bottom controls remain centred and compact

## 13. Canonical gameplay reference projection (v1.6.3)

The approved match-screen concept is a **1672 × 941 reference coordinate system**. Do not return to independent LinearLayout weight sizing for the major gameplay regions.

Canonical rectangles:
- Player 1 plate: `101,14,493,80`
- Status plate: `613,14,424,80`
- Player 2 plate: `1053,14,495,80`
- Board: `100,104,1448,691`
- Undo: `445,811,151,80`
- Main action: `611,811,432,80`
- Menu: `1060,811,151,80`

The reference is projected into the **full safe landscape window**, not uniformly fitted into a centred 16:9 island:

- `scaleX = availableWidth / 1672`
- `scaleY = availableHeight / 941`
- X positions and widths use `scaleX`
- Y positions and heights use `scaleY`
- typography, checker icons and circular score badges use `min(scaleX, scaleY)` so they do not distort
- horizontal padding/gaps may use `scaleX`

This is required on wide phones such as the 1730 × 799 validation device. A uniform `min(widthScale, heightScale)` fit makes the board too narrow and is no longer the production rule.

The BoardMap continues to scale only from the final displayed board rectangle, so gameplay geometry remains independent of device resolution. Score badges must remain fully within the nameplate. Centre status text must remain complete; shrink text within bounds instead of ellipsizing normal status messages.
