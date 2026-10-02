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


# v1.23 geometry lock uses field/bar boundaries as the point-grid source of truth.
boardmap=(ROOT/'app/src/main/java/com/george/backgammon/rendering/BoardMap.java').read_text()
for token in ['FIELD_LEFT_PX = 232.09f','FIELD_RIGHT_PX = 1836.44f','buildBaseLeftPx()','buildBaseRightPx()','buildApexXPx()','fill(419.0f)','fill(544.0f)']:
    if token not in boardmap: errors.append('v1.23 BoardMap calibration missing '+token)


# v1.21 clean checker sprites and live identity icons.
for rel in ['app/src/main/assets/cosmetics/checkers/ivory_walnut/light.png','app/src/main/assets/cosmetics/checkers/ivory_walnut/dark.png']:
    pth=ROOT/rel
    if not pth.exists(): errors.append('Missing clean checker asset '+rel)
    else:
        try:
            im=Image.open(pth).convert('RGBA')
            if im.size != (512,512): errors.append(rel+' must be 512x512')
            if im.getpixel((0,0))[3] != 0: errors.append(rel+' corner must be transparent')
        except Exception as e: errors.append(rel+' invalid: '+str(e))
# v1.26 starter checker pair must share exactly the same visible geometry.
try:
    light_im=Image.open(ROOT/'app/src/main/assets/cosmetics/checkers/ivory_walnut/light.png').convert('RGBA')
    dark_im=Image.open(ROOT/'app/src/main/assets/cosmetics/checkers/ivory_walnut/dark.png').convert('RGBA')
    if light_im.getbbox() != dark_im.getbbox(): errors.append('Classic Wood light/dark top-down visible bounds differ')
    light_side_im=Image.open(ROOT/'app/src/main/assets/cosmetics/checkers/ivory_walnut/light_side.png').convert('RGBA')
    dark_side_im=Image.open(ROOT/'app/src/main/assets/cosmetics/checkers/ivory_walnut/dark_side.png').convert('RGBA')
    if light_side_im.getbbox() != dark_side_im.getbbox(): errors.append('Classic Wood light/dark side visible bounds differ')
except Exception as e:
    errors.append('Classic Wood geometry validation failed: '+str(e))

layout=(ROOT/'app/src/main/res/layout/activity_main.xml').read_text()
if layout.count('android:visibility="visible"') < 2: errors.append('Gameplay identity icons are not live/visible')


# v1.25 checker side art + clean tabletop.
for rel in [
    'app/src/main/assets/cosmetics/checkers/ivory_walnut/light_side.png',
    'app/src/main/assets/cosmetics/checkers/ivory_walnut/dark_side.png']:
    pth=ROOT/rel
    if not pth.exists(): errors.append('Missing checker side asset '+rel)
    else:
        try:
            im=Image.open(pth).convert('RGBA')
            if im.width < 256 or im.height < 64: errors.append(rel+' side asset too small')
            if im.getpixel((0,0))[3] != 0: errors.append(rel+' corner must be transparent')
        except Exception as e: errors.append(rel+' invalid: '+str(e))
for token in ['lightSideAsset','darkSideAsset']:
    if token not in checker: errors.append('Checker side metadata missing '+token)
view=(ROOT/'app/src/main/java/com/george/backgammon/rendering/BackgammonBoardView.java').read_text()
for token in ['loadout.checkers.lightSideAsset','loadout.checkers.darkSideAsset']:
    if token not in view: errors.append('Bear-off side renderer missing '+token)
tabletop=ROOT/'app/src/main/res/drawable/tabletop.webp'
if not tabletop.exists(): errors.append('Missing emerald tabletop background')
else:
    try:
        im=Image.open(tabletop)
        if im.width < 1600 or im.height < 900: errors.append('Tabletop background resolution too low')
    except Exception as e: errors.append('Tabletop invalid: '+str(e))

# v1.24 realistic material pipeline.
boardtheme=(ROOT/'app/src/main/java/com/george/backgammon/cosmetics/BoardTheme.java').read_text()
renderer=(ROOT/'app/src/main/java/com/george/backgammon/rendering/StaticBoardRenderer.java').read_text()
for token in ['woodTextureAsset','fieldTextureAsset','trayTextureAsset','lightPointTextureAsset','darkPointTextureAsset']:
    if token not in boardtheme: errors.append('BoardTheme material metadata missing '+token)
for token in ['drawRealisticTrayMaterial','setRepeatingTexture','theme.fieldTextureAsset','theme.lightPointTextureAsset','theme.darkPointTextureAsset']:
    if token not in renderer: errors.append('Realistic material renderer missing '+token)
for rel in [
    'app/src/main/assets/cosmetics/boards/classic_burgundy/materials/wood.webp',
    'app/src/main/assets/cosmetics/boards/classic_burgundy/materials/field_black.webp',
    'app/src/main/assets/cosmetics/boards/classic_burgundy/materials/tray_burgundy.webp',
    'app/src/main/assets/cosmetics/boards/classic_burgundy/materials/point_cream.webp',
    'app/src/main/assets/cosmetics/boards/classic_burgundy/materials/point_burgundy.webp']:
    pth=ROOT/rel
    if not pth.exists(): errors.append('Missing realistic material asset '+rel)
    else:
        try:
            im=Image.open(pth)
            if im.width < 128 or im.height < 128: errors.append(rel+' material texture too small')
        except Exception as e: errors.append(rel+' invalid: '+str(e))

# v1.26 simple starter-board / control-well lock.
catalog=(ROOT/'app/src/main/java/com/george/backgammon/cosmetics/CosmeticCatalog.java').read_text()
if '"boards/classic_burgundy/materials/tray_burgundy.webp",\n            null,\n            null);' not in catalog:
    errors.append('Classic Burgundy starter points must not use repeating point textures')
game_activity=(ROOT/'app/src/main/java/com/george/backgammon/ui/GameActivity.java').read_text()
for token in ['568.46f, 746.76f, 458.88f, 101.28f','1096.73f, 746.76f, 192.38f, 101.28f','1304.88f, 746.76f, 199.48f, 101.28f']:
    if token not in game_activity: errors.append('Bottom control well alignment missing '+token)

# Version / workflow
build=(ROOT/'app/build.gradle.kts').read_text(); wf=(ROOT/'.github/workflows/build-apk.yml').read_text()
if 'versionCode = 44' not in build or 'versionName = "1.26.0"' not in build: errors.append('Version is not 1.26.0 / code 44')
if 'Backgammon-Legacy-v1.26.0.apk' not in wf or 'Backgammon-Legacy-v1.26.0-APK' not in wf: errors.append('GitHub APK naming not v1.26.0')

if errors:
    print('\n'.join('ERROR: '+e for e in errors)); sys.exit(1)
print('Project validation passed: v1.26.0 uniform Classic Wood checkers + simple starter points + snapped bottom controls + v1.23 geometry lock.')
