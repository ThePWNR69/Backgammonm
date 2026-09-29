# Approved gameplay layout — v1.16.0

Master coordinate system: **1672 × 941**.

The complete gameplay composition is scaled uniformly. Individual visual elements must not be independently stretched to fill a device.

## Top HUD
- Menu: x108 y22 w88 h86
- Player 1 plaque: x210 y22 w450 h86
- Status plaque: x670 y18 w342 h92
- Player 2 plaque: x1022 y22 w432 h86
- Settings: x1464 y22 w88 h86

## Board
- x104 y118 w1442 h615
- The clean board artwork contains no checker sprites.
- Checkers, highlights and animations remain live renderer layers.

## Bottom deck
- Deck: x108 y752 w1435 h126
- Dice 1: x223 y786 w80 h78
- Dice 2: x331 y786 w81 h78
- Main action hit target: x571 y780 w451 h87
- Undo hit target: x1090 y782 w197 h82
- Hint hit target: x1309 y782 w195 h82

## Text rule
All player names, scores, turn/status messages and button labels are live Android text using system sans-serif fonts. They must never be baked into the theme artwork.
