#!/usr/bin/env python3
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
errors = []

# XML sanity for every resource XML, not just the gameplay layout.
for xml in (ROOT / 'app/src/main/res').rglob('*.xml'):
    try:
        ET.parse(xml)
    except Exception as exc:
        errors.append(f'{xml.relative_to(ROOT)} invalid: {exc}')

layout = ROOT / 'app/src/main/res/layout/activity_main.xml'
xml_text = layout.read_text()
java = (ROOT / 'app/src/main/java/com/george/backgammon/ui/GameActivity.java').read_text()
required_layout_tokens = [
    'final float REF_W = 510f;',
    'final float REF_H = 280f;',
    'setFrameRef(boardView, 0f, 44f, 510f, 178f, scale);',
    'setFrameRef(bottomControlBar, 0f, 224f, 510f, 56f, scale);',
    'setFrameRef(mainActionButton, 162f, 229f, 178f, 46f, scale);',
    'setFrameRef(undoButton, 364f, 228f, 60f, 48f, scale);',
    'setFrameRef(hintButton, 430f, 228f, 64f, 48f, scale);',
]
for token in required_layout_tokens:
    if token not in java:
        errors.append(f'Approved layout token missing: {token}')

for view_id in ['menuButton','settingsButton','playerOnePanel','statusPanel','playerTwoPanel','boardView',
                'bottomControlBar','dieOneView','dieTwoView','mainActionButton','undoButton','hintButton']:
    if f'@+id/{view_id}' not in xml_text:
        errors.append(f'activity_main.xml missing {view_id}')

if '@drawable/tabletop' not in xml_text:
    errors.append('Gameplay root is not using the approved emerald tabletop')

# Canonical BoardMap remains untouched.
board_map = (ROOT / 'app/src/main/java/com/george/backgammon/rendering/BoardMap.java').read_text()
for token in ['MASTER_WIDTH_PX = 2048', 'MASTER_HEIGHT_PX = 977']:
    if token not in board_map:
        errors.append(f'BoardMap master geometry changed unexpectedly: {token}')

board_geometry = (ROOT / 'app/src/main/java/com/george/backgammon/rendering/BoardGeometry.java').read_text()
if 'void computeStretched(float width, float height)' not in board_geometry:
    errors.append('Approved gameplay BoardMap projection is missing')

board_view = (ROOT / 'app/src/main/java/com/george/backgammon/rendering/BackgammonBoardView.java').read_text()
if 'Dice are rendered exclusively in the fixed bottom control deck in v1.10.0.' not in board_view:
    errors.append('Board renderer may be drawing dice over the approved board area')

reference = ROOT / 'design/reference/CLASSIC_BURGUNDY_APPROVED.png'
if not reference.exists():
    errors.append('Approved Classic Burgundy reference image is missing')

premium_assets = [
    'premium_player_left.webp', 'premium_player_right.webp', 'premium_status.webp',
    'premium_bottom_deck.webp', 'premium_primary_normal.webp', 'premium_primary_pressed.webp',
    'premium_primary_disabled.webp', 'premium_secondary_normal.webp',
    'premium_secondary_pressed.webp', 'premium_secondary_disabled.webp',
    'premium_square_normal.webp', 'premium_square_pressed.webp'
]
asset_dir = ROOT / 'app/src/main/res/drawable-nodpi'
for name in premium_assets:
    if not (asset_dir / name).exists():
        errors.append(f'Missing premium theme asset: {name}')

# Pixel dimensions are deliberately 4x the logical gameplay rectangles.
try:
    from PIL import Image
    expected_sizes = {
        'premium_player_left.webp': (576, 168),
        'premium_player_right.webp': (552, 168),
        'premium_status.webp': (528, 168),
        'premium_bottom_deck.webp': (2040, 224),
        'premium_primary_normal.webp': (712, 184),
        'premium_primary_pressed.webp': (712, 184),
        'premium_primary_disabled.webp': (712, 184),
        'premium_secondary_normal.webp': (256, 192),
        'premium_secondary_pressed.webp': (256, 192),
        'premium_secondary_disabled.webp': (256, 192),
        'premium_square_normal.webp': (168, 168),
        'premium_square_pressed.webp': (168, 168),
    }
    for name, size in expected_sizes.items():
        path = asset_dir / name
        if path.exists() and Image.open(path).size != size:
            errors.append(f'{name} has wrong pixel size: {Image.open(path).size}, expected {size}')
except ImportError:
    pass

catalog = (ROOT / 'app/src/main/java/com/george/backgammon/cosmetics/UiThemeCatalog.java').read_text()
for token in ['R.drawable.premium_player_left', 'R.drawable.premium_player_right',
              'R.drawable.premium_status', 'R.drawable.premium_bottom_deck',
              'R.drawable.premium_primary_button_selector',
              'R.drawable.premium_secondary_button_selector',
              'R.drawable.premium_square_button_selector']:
    if token not in catalog:
        errors.append(f'Classic Burgundy theme is not wired to {token}')

build = (ROOT / 'app/build.gradle.kts').read_text()
if 'versionName = "1.10.0"' not in build or 'versionCode = 28' not in build:
    errors.append('App version is not v1.10.0 / code 28')

workflow = (ROOT / '.github/workflows/build-apk.yml').read_text()
if 'Backgammon-Legacy-v1.10.0.apk' not in workflow:
    errors.append('GitHub workflow does not output Backgammon-Legacy-v1.10.0.apk')

if errors:
    print('\n'.join('ERROR: ' + e for e in errors))
    sys.exit(1)
print('Project validation passed: approved v1.10.0 gameplay composition and premium theme wiring are intact.')
