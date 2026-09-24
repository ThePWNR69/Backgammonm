# Backgammon Android v0.9 — Rendering Foundation

v0.9 replaces theme-specific board geometry with one permanent invisible board map and a locked production-art pipeline.

## Core concept

```text
premium screen background
        ↓
static board skin (1860 × 1000)
        ↓
INVISIBLE BOARD MAP (permanent)
        ↓
checkers / dice / highlights / animations
```

All three included boards use identical point, bar and tray geometry. Board art changes appearance only.

See:
- `PATCH_NOTES_v0.9.md`
- `DESIGN_STANDARDS.md`
- `production_templates/MASTER_BOARD_TEMPLATE.png`
- `production_templates/MASTER_BOARD_MAP.json`
- `production_templates/MASTER_CHECKER_TEMPLATE.png`

## Alignment debug
Open `☰` → **Show Board Map** to overlay the canonical 24 point anchors over the selected board.

## Build
Run the included GitHub Actions workflow. Artifact: `Backgammon-v0.9-APK`.

## GitHub web-upload package
This compact v0.9 package removes redundant legacy board layers and duplicate preview images.
It is designed to stay comfortably below GitHub's 100-file browser upload limit while retaining the same v0.9 gameplay/rendering behaviour.
