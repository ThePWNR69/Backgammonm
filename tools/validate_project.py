#!/usr/bin/env python3
from pathlib import Path
import sys, xml.etree.ElementTree as ET
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
errors=[]

# XML sanity
for xml in (ROOT/'app/src/main/res').rglob('*.xml'):
    try: ET.parse(xml)
    except Exception as e: errors.append(f'{xml.relative_to(ROOT)} invalid: {e}')

setup=(ROOT/'app/src/main/java/com/george/backgammon/ui/MatchSetupActivity.java').read_text()
for token in [
    'v1.19 premium pre-match setup.',
    'R.drawable.setup_panel',
    'R.drawable.setup_selector',
    'R.drawable.setup_selector_pressed',
    'R.drawable.setup_start_button_selector',
    'R.drawable.setup_back_button_selector',
    'new String[]{"Auto", "Light", "Dark"}',
    'updateRules();',
    'CheckerTheme.SIDE_LIGHT',
    'CheckerTheme.SIDE_DARK',
    '.translationX(distance)',
]:
    if token not in setup: errors.append('Missing setup token: '+token)

# 3D setup assets
xhdpi=ROOT/'app/src/main/res/drawable-xxhdpi'
for name in [
    'setup_panel.9.png','setup_selector.9.png','setup_selector_pressed.9.png',
    'setup_start_normal.9.png','setup_start_pressed.9.png',
    'setup_icon_badge.png','setup_back_normal.png','setup_back_pressed.png']:
    p=xhdpi/name
    if not p.exists():
        errors.append('Missing setup asset: '+name)
        continue
    try:
        im=Image.open(p).convert('RGBA')
        if name.endswith('.9.png'):
            # NinePatch needs visible black stretch markers on top and left borders.
            top=[im.getpixel((x,0)) for x in range(im.width)]
            left=[im.getpixel((0,y)) for y in range(im.height)]
            if not any(px[:3]==(0,0,0) and px[3]>200 for px in top): errors.append(name+' missing top stretch marker')
            if not any(px[:3]==(0,0,0) and px[3]>200 for px in left): errors.append(name+' missing left stretch marker')
    except Exception as e:
        errors.append(f'{name} invalid image: {e}')

# Selector / button XML wiring
for name in ['setup_back_button_selector.xml','setup_start_button_selector.xml','setup_selector_arrow.xml','setup_divider.xml','setup_separator_vertical.xml']:
    if not (ROOT/'app/src/main/res/drawable'/name).exists(): errors.append('Missing drawable '+name)

# Checker metadata stays scalable for Auto / Light / Dark.
checker=(ROOT/'app/src/main/java/com/george/backgammon/cosmetics/CheckerTheme.java').read_text()
for token in ['SIDE_LIGHT = "LIGHT"','SIDE_DARK = "DARK"','lightSideTag','darkSideTag']:
    if token not in checker: errors.append('Checker metadata missing '+token)

# Version / workflow
build=(ROOT/'app/build.gradle.kts').read_text(); wf=(ROOT/'.github/workflows/build-apk.yml').read_text()
if 'versionCode = 37' not in build or 'versionName = "1.19.0"' not in build: errors.append('Version is not 1.19.0 / code 37')
if 'Backgammon-Legacy-v1.19.0.apk' not in wf or 'Backgammon-Legacy-v1.19.0-APK' not in wf: errors.append('GitHub APK naming not v1.19.0')

if errors:
    print('\n'.join('ERROR: '+e for e in errors)); sys.exit(1)
print('Project validation passed: v1.19.0 polished 3D setup UI + sliding selectors + dynamic rules + Auto opponent side.')
