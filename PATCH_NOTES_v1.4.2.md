# Backgammon Legacy v1.4.2 — Approved Reference Match

This release replaces the previous premium-board approximation with a gameplay presentation built directly from the approved visual reference.

## Gameplay visual rebuild
- Rebuilt **Classic Walnut** from the approved premium board reference rather than procedurally approximating it.
- Removed the reference checkers/state from the board artwork so the board remains a true static skin.
- Preserved the reference walnut case, black leather field, recessed trays, gold/brass trim, central bar and medallion.
- Re-measured the permanent invisible `BoardMap` against this exact artwork.
- Rebuilt **Aegean Marble** and **Midnight Teal** from the same canonical board pixels so all three themes use identical geometry.
- Replaced the default Ivory/Walnut checker artwork with isolated checker artwork matching the approved reference.

## Scale / alignment
- New canonical board resolution: **2048 × 977**.
- Exactly 24 points, six per quadrant.
- All point anchors, bar anchors, off-tray anchors and touch areas are independent of the board image.
- Reduced checker diameter and stack spacing to match the proportions in the approved reference more closely.
- Five visible checkers still fit within the point height; 6+ continues to use the count badge.

## Gameplay layout
- Increased the gameplay board allocation and reduced wasted vertical UI space.
- Refined player panels, turn panel and bottom controls to match the approved dark-brown / emerald / gold visual language.
- Improved player subtitle spacing to avoid the clipped/overlapping appearance seen in v1.4.1.
- Rebuilt the background as a restrained dark emerald tabletop with decorative detail only at the outer edges.

## Preserved systems
- Backgammon Legacy branding and launcher icon.
- Main Menu, Store, Customise and Profile.
- Vs Bot and 2-Player Match Setup.
- Opening roll, AI, Undo, End Turn and dice animation.
- Green single-die moves, gold `2` combined-dice moves, orange captures and blue selection.
- Shared invisible-board renderer across gameplay and cosmetic previews.

## Build
GitHub Actions artifact: **`Backgammon-Legacy-v1.4.2-APK`**.
