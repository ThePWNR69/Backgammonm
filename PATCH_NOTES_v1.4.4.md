# Backgammon Legacy v1.4.4 — Triangle-Centred Checker Mapping

This update changes checker placement from an assumed column centre to the **actual geometry of each painted triangle**.

## Checker alignment

- Every one of the 24 points now stores its measured base-left, base-right and apex coordinates from the production board artwork.
- For each checker slot, the game calculates the left and right triangle edges at that checker's exact Y position.
- The checker centre is placed at the exact midpoint between those two edges.
- This allows naturally leaning/asymmetric point artwork while keeping every checker visually centred.
- Top stacks grow down the point and bottom stacks grow up the point using the same edge-midpoint rule.
- Main checker rendering, landing previews, legal-move indicators, invalid feedback and animation endpoints now all share this geometry.

## Checker sizing

- The standard checker diameter is now approximately 80% of the narrowest triangle base.
- Five visible checker positions are fitted between the triangle base and apex.
- All checker cosmetic sets still use the same renderer-controlled diameter.

## Board Map diagnostic

`Menu → Show Board Map` now displays:

- the two measured sloping edges of every point,
- a cyan midpoint line from the middle of the point base to its apex,
- the numbered point marker centred on that midpoint path.

This makes alignment errors immediately visible when future board artwork is added.

## Touch mapping

Point taps now choose the nearest real triangle midpoint at the tap height rather than relying on a generic equal-width grid.

## Rendering consistency

The procedural/fallback board renderer also uses the exact same measured triangle vertices, so the visible points and gameplay map cannot diverge.

## Preserved systems

All v1.4.3 gameplay, AI, opening roll, Undo, move highlights, Match Setup, Store, Customise, Profile, cosmetics and Backgammon Legacy branding are retained.

GitHub Actions artifact: **`Backgammon-Legacy-v1.4.4-APK`**.
