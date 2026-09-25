# Backgammon Legacy v1.4.3 — Board Integration & Map Alignment

This is a focused visual/alignment hotfix built directly on v1.4.2.

## Board integration
- Added a soft ambient/contact shadow beneath the physical board so it reads as an object sitting on the tabletop rather than a rectangular image pasted over it.
- Removed the hard rectangular matte at board-image corners by giving production board skins a softly anti-aliased transparent outer mask.
- Added a subtle pool of light and edge vignette to the emerald tabletop so the board and surrounding environment share the same lighting.
- The shadow is generated in the cached static-board render, so it does not add per-frame animation work.

## Invisible BoardMap correction
- Re-measured the actual six point bases on each half of the approved Classic Walnut artwork.
- The left and right six-point grids are now stored independently instead of deriving all 12 point anchors from one continuous field width.
- Corrected the small horizontal drift that was most visible on the left half of the board.
- Checker centres, landing previews and touch hitboxes now use the exact per-half column width.

## Board Map developer overlay
- The numbered Board Map circles now appear at the geometric centre of each triangular point.
- Previously the diagnostic overlay showed the first checker landing slot at the point base. That was useful internally but visually made the map marker look off-centre on the triangle.
- Actual checker landing slots still begin correctly at the base of each point.

## Preserved
- Backgammon Legacy branding and app icon
- Main menu, Store, Customise, Profile and Match Setup
- AI and opening roll
- Existing rules/Undo/End Turn flow
- Move highlighting and combined two-dice move indicator
- Fixed checker sizing and cosmetic system

GitHub Actions artifact: **`Backgammon-Legacy-v1.4.3-APK`**.
