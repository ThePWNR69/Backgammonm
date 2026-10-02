# v1.22.0 — Geometry-Locked Board & Dice Wells

- Classic Burgundy no longer trusts baked triangle artwork for gameplay alignment.
- The production bitmap supplies frame, trays and centre-bar materials; the two playable fields and all 24 points are redrawn at runtime from the canonical BoardMap.
- Checker centres, hit regions and painted triangle edges therefore share the same geometry source.
- Dice positions are calculated from the exact dice-well rectangles in premium_bottom_deck.webp instead of hand-tuned offsets.
- No rules, AI, progression, rewards or setup-screen behaviour changed.
