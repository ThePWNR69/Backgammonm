#!/usr/bin/env python3
from pathlib import Path
import sys, xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
errors=[]
# XML sanity
for xml in (ROOT/'app/src/main/res').rglob('*.xml'):
    try: ET.parse(xml)
    except Exception as e: errors.append(f'{xml.relative_to(ROOT)} invalid: {e}')
java=(ROOT/'app/src/main/java/com/george/backgammon/ui/GameActivity.java').read_text()
layout=(ROOT/'app/src/main/res/layout/activity_main.xml').read_text()
for token in [
    'final float REF_W = 1672f;','final float REF_H = 941f;',
    'setFrameRef(menuButton,       108f, 22f,  88f,  86f, scale);',
    'setFrameRef(playerOnePanel,   210f, 22f, 450f,  86f, scale);',
    'setFrameRef(statusPanel,      670f, 18f, 342f,  92f, scale);',
    'setFrameRef(playerTwoPanel,  1022f, 22f, 432f,  86f, scale);',
    'setFrameRef(settingsButton,  1464f, 22f,  88f,  86f, scale);',
    'setFrameRef(boardView,        104f, 118f, 1442f, 615f, scale);',
    'setFrameRef(bottomControlBar, 108f, 752f, 1435f, 126f, scale);',
]:
    if token not in java: errors.append('Missing layout token: '+token)
for token in ['android:text=""','android:text="↶  UNDO"','android:text="✦  HINT"','android:fontFamily="sans-serif-medium"']:
    if token not in layout: errors.append('Live-text layout token missing: '+token)
# Layer assets
asset_dir=ROOT/'app/src/main/res/drawable-nodpi'
for name in [
    'premium_player_left.webp','premium_player_right.webp','premium_status.webp','premium_bottom_deck.webp',
    'premium_square_normal.webp','premium_square_pressed.webp','premium_settings_normal.webp','premium_settings_pressed.webp',
    'premium_primary_pressed.webp','premium_primary_disabled.webp','premium_secondary_pressed.webp','premium_secondary_disabled.webp',
    'premium_hint_pressed.webp','premium_hint_disabled.webp','die_1.webp','die_2.webp','die_3.webp','die_4.webp','die_5.webp','die_6.webp']:
    if not (asset_dir/name).exists(): errors.append('Missing layered asset: '+name)
board=ROOT/'app/src/main/assets/cosmetics/boards/classic_burgundy/board.webp'
if not board.exists(): errors.append('Missing Classic Burgundy board')
try:
    from PIL import Image
    if board.exists() and Image.open(board).size!=(2048,977): errors.append('Board must remain 2048x977 for BoardMap projection')
except Exception as e: errors.append('Image validation failed: '+str(e))
# Theme wiring
cat=(ROOT/'app/src/main/java/com/george/backgammon/cosmetics/UiThemeCatalog.java').read_text()
for token in ['premium_hint_button_selector','premium_settings_button_selector','premium_square_button_selector']:
    if token not in cat: errors.append('Theme not wired to '+token)
# Version / workflow
build=(ROOT/'app/build.gradle.kts').read_text(); wf=(ROOT/'.github/workflows/build-apk.yml').read_text()
if 'versionCode = 34' not in build or 'versionName = "1.16.0"' not in build: errors.append('Version is not 1.16.0 / code 34')
if 'Backgammon-Legacy-v1.16.0.apk' not in wf: errors.append('GitHub APK filename not v1.16.0')
# Design references
for name in ['CLASSIC_BURGUNDY_APPROVED.png','CLASSIC_BURGUNDY_LAYERED_ASSETS.png','CLASSIC_BURGUNDY_TEXT_SPEC.png']:
    if not (ROOT/'design/reference'/name).exists(): errors.append('Missing design reference '+name)
if errors:
    print('\n'.join('ERROR: '+e for e in errors)); sys.exit(1)
print('Project validation passed: v1.16.0 layered image + live text UI is wired.')
