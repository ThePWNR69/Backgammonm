# Approved gameplay layout — v1.11.0+

The v1.11.0 runtime composition responds to the approved Classic Burgundy direction plus the in-game review that the v1.10 top and bottom chrome were too tall and visually compressed the board.

Visual references:
- `design/reference/CLASSIC_BURGUNDY_APPROVED.png` — material/style direction.
- `design/reference/V1_11_RUNTIME_COMPOSITION.png` — current runtime proportions.

Future themes may reskin this composition but must not move gameplay elements without a deliberate layout revision.

## Reference frame

The match screen uses one **510 × 280** logical frame, uniformly scaled and centred inside the safe landscape window.

### Locked rectangles (x, y, width, height)

- Menu: `0, 0, 30, 30`
- Player 1: `34, 0, 146, 30`
- Turn/status plaque: `184, 0, 142, 30`
- Player 2 / AI: `330, 0, 146, 30`
- Settings: `480, 0, 30, 30`
- Live board: `0, 32, 510, 207`
- Bottom control deck: `0, 241, 510, 39`
- Die 1: `34, 247, 27, 27`
- Die 2: `67, 247, 27, 27`
- Roll / End Turn: `164, 245, 174, 31`
- Undo: `378, 245, 58, 31`
- Hint: `440, 245, 58, 31`

The live board therefore occupies about **74% of the composition height**, up from about 64% in v1.10.0. The top chrome is reduced from 42 to 30 logical units and the bottom deck from 56 to 39.

## Visual rules

- Classic Burgundy remains the baseline: warm walnut case, black leather field, ivory/burgundy points, burgundy leather chrome and restrained antique-gold trim.
- Chrome is **slim and architectural**, not a large banner. The board is the visual focus.
- Player plaques have a recessed dark score bay and a restrained double gold edge.
- The centre turn plaque is a compact faceted gold plaque.
- Menu and Settings use the same 30-unit control height as the top HUD.
- Bottom controls form one integrated 39-unit deck. Dice remain left, Roll/End Turn remains centred, Undo/Hint remain right.
- Undo and Hint are compact single-line controls. Hint Off reserves the same rectangle so nothing shifts.
- Buttons retain Normal / Pressed / Disabled artwork. Pressed state visually sinks by collapsing the highlight/shadow and darkening the face. No glow.
- Decorative gold is limited and thin. Avoid oversized outlines, neon effects or full-board ornamental framing.
- The right bear-off tray remains the edge-on two-direction progress stack.

## Board projection

The canonical board art and BoardMap remain **2048 × 977**. The live screen projects that BoardMap into the v1.11 rectangle above. The BoardMap source coordinates, touch regions, checker anchors, bar/tray anchors and animation endpoints are unchanged.

Checker sprites remain circular and keep the locked 90%-of-point-base sizing rule. This release changes presentation space only; it does not change game/rules geometry.
