# Approved gameplay layout — v1.10.0+

`design/reference/CLASSIC_BURGUNDY_APPROVED.png` is the visual source of truth for the live match screen.
Do not reinterpret the composition on future UI passes. New themes reskin this layout; they do not move it.

## Reference frame

The runtime HUD uses a single **510 × 280** logical frame and scales it uniformly to the safe landscape window.
It is centred on extra-wide displays instead of allowing individual pieces to drift apart.

### Locked rectangles (x, y, width, height)

- Menu: `0, 0, 42, 42`
- Player 1: `46, 0, 144, 42`
- Turn/status plaque: `194, 0, 132, 42`
- Player 2 / AI: `330, 0, 138, 42`
- Settings: `472, 0, 38, 42`
- Live board: `0, 44, 510, 178`
- Bottom control deck: `0, 224, 510, 56`
- Die 1: `34, 233, 38, 38`
- Die 2: `82, 233, 38, 38`
- Roll / End Turn: `162, 229, 178, 46`
- Undo: `364, 228, 60, 48`
- Hint: `430, 228, 64, 48`

## Visual rules

- Classic Burgundy is the baseline theme: warm walnut case, ivory/burgundy points, burgundy leather HUD, restrained gold trim.
- The centre turn plaque is gold/cream and visually distinct from the player panels.
- Menu and Settings are compact square plaques, not detached circular controls.
- The bottom controls form one integrated deck. Dice live on the left; the main action is central; Undo and Hint live on the right.
- `ROLL` changes to `END TURN` without changing the button rectangle.
- Hint Off uses the same reserved rectangle; surrounding controls never move.
- Buttons have Normal / Pressed / Disabled states. Pressed state looks mechanically depressed: face shifts down, bottom shadow collapses, bevel darkens. No glow.
- The right bear-off tray remains a two-direction edge-on progress stack: one side grows downward, the other upward, meeting toward the centre.
- Top identity uses player/AI icons rather than checker sprites. Checker cosmetics stay on the board.
- Do not add full-board ornamental borders. Decorative gold is limited to the top/bottom UI and restrained board hardware.

## Board projection

The canonical board art and BoardMap remain **2048 × 977**. Gameplay projects that same map into the approved wide/short board rectangle. X and Y are projected independently, so the artwork, 24 points, hit regions, checker anchors, bar, bear-off tray and animation endpoints remain aligned to the same BoardMap.

Checker sprites are never stretched: they remain circular and use the locked 90%-of-point-base sizing rule. Store/Customise previews may use the canonical source-art aspect; gameplay uses the approved match-screen projection.
