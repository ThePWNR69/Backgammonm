# Approved gameplay layout — v1.14.0

The exact visual direction is `design/reference/CLASSIC_BURGUNDY_FINAL_APPROVED.png`.
This is now the implementation target, not an inspiration image.

## Reference frame
The match screen uses one 510 × 280 logical frame, uniformly scaled and centred in the safe landscape window.

### Locked rectangles (x, y, width, height)
- Menu: `0, 0, 30, 30`
- Player 1: `34, 0, 146, 30`
- Turn/status: `184, 0, 142, 30`
- Player 2 / AI: `330, 0, 146, 30`
- Settings: `480, 0, 30, 30`
- Live board: `2, 31, 506, 213`
- Bottom control deck: `0, 245, 510, 35`
- Die 1: `33, 250, 24, 24`
- Die 2: `66, 250, 24, 24`
- Roll / End Turn: `164, 248, 174, 29`
- Undo: `378, 248, 58, 29`
- Hint: `440, 248, 58, 29`

## Locked visual rules
- Board frame is straight-on and symmetrical: no perspective/skew in the wood rails.
- Board has rounded transparent corners and is grounded into the emerald tabletop by the renderer shadow.
- Player score bays are sized to the live score fields and stay inside each player plaque.
- Top UI is burgundy leather with restrained double antique-gold edging.
- The turn plaque is faceted brushed gold.
- Bottom controls are one integrated burgundy deck with recessed dice wells, a burgundy main action button and dark secondary buttons.
- Buttons retain Normal / Pressed / Disabled states and never glow.
- BoardMap, checker anchors, hit regions and rules geometry are unchanged.
