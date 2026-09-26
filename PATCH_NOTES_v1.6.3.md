# Backgammon Legacy v1.6.3 — Wide-Screen Reference Match

This update continues the gameplay visual correction started in v1.6.2. It specifically fixes the remaining wide-screen regression seen on the 1730×799 validation phone.

## What was wrong in v1.6.2

v1.6.2 treated the approved 1672×941 composition as a single uniformly scaled canvas. On a much wider phone, height became the limiting axis, so the whole composition was reduced and centred. That made the board and HUD too narrow even though their proportions inside the virtual canvas were correct.

## New reference projection

The approved reference coordinates are now projected directly into the safe landscape app window:

- X positions and widths scale from available screen width.
- Y positions and heights scale from available screen height.
- Circular/details such as checker icons and score circles use the smaller UI scale so they remain round.
- Text sizes also use the smaller UI scale so type is not horizontally stretched.
- Horizontal padding can use the wider screen space.

The canonical reference rectangles remain:

- Player 1 plate: x101 y14 w493 h80
- Centre status: x613 y14 w424 h80
- Player 2 plate: x1053 y14 w495 h80
- Board: x100 y104 w1448 h691
- Undo: x445 y811 w151 h80
- Main action: x611 y811 w432 h80
- Menu: x1060 y811 w151 h80

## 1730×799 validation target

With no display-cutout inset, the new projection produces approximately:

- Player 1 plate: x105–615, y12–80
- Centre status: x634–1073, y12–80
- Player 2 plate: x1090–1602, y12–80
- Board: x103–1601, y88–675
- Undo: x460–616, y689–757
- Main action: x632–1079, y689–757
- Menu: x1097–1253, y689–757

The board therefore occupies about **86.6% of the available screen width**, matching the approved reference instead of being reduced to roughly 71% by uniform fitting.

## Gameplay geometry preserved

No changes were made to:

- backgammon rules
- AI behaviour
- dice randomness
- checker starting positions
- BoardMap point definitions
- legal-move hit testing
- movement/dice animations
- cosmetics, progression or match scoring

`BackgammonBoardView` simply receives the new displayed rectangle, and `BoardGeometry` continues mapping the permanent board coordinates into that rectangle.

## Build

Version code: **22**  
Version name: **1.6.3**  
GitHub Actions artifact: `Backgammon-Legacy-v1.6.3-APK`
