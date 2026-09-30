# v1.18.0 — Match Setup Redesign

- Replaced every Android dropdown/Spinner on Match Setup with an in-place left/right sliding selector.
- Selector value transition: 85 ms slide-out + 125 ms slide-in.
- Match Rules panel now updates immediately when Game Version changes.
- VS Bot uses `Bot colour: Auto / Light / Dark`.
- Local 2-player uses `Player 2 colour: Auto / Light / Dark`.
- Auto reads the user's equipped checker side from Customisation and assigns the opponent the opposite gameplay side.
- CheckerTheme now stores explicit LIGHT/DARK side metadata independently of the artwork's literal colours.
- Current checker sets have explicit side metadata; future checker cosmetics should follow the same convention.
- Added Light/Dark checker-side preference to the Checkers tab in Customisation.
- Tawla 31 setup fixes Match Length to `31 Points` and displays Tawla-specific rules.
- VS Bot labels the movement selector `Your direction`; local two-player retains `Player 1 direction`.
- Difficulty remains VS Bot-only.
- Hints remain independently configurable.
- Preserves v1.17 gameplay composition calibration: trimmed 860-unit runtime master frame, ~9% larger presentation on extra-wide phones, unchanged 1442×615 board rectangle.
- Rules engine, AI, XP/Gold reward logic and BoardMap are otherwise unchanged.
