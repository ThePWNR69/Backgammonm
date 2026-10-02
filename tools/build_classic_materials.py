#!/usr/bin/env python3
"""Rebuild the Classic Burgundy material library from the approved realism reference.

This tool extracts *material samples only*. It never extracts or defines gameplay geometry.
Triangles and checker positions remain owned exclusively by BoardMap.
"""
from pathlib import Path
from PIL import Image, ImageOps, ImageEnhance, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / 'design/reference/CLASSIC_BURGUNDY_REALISM_TARGET.png'
OUT = ROOT / 'app/src/main/assets/cosmetics/boards/classic_burgundy/materials'
OUT.mkdir(parents=True, exist_ok=True)

BOXES = {
    'wood': (340,108,690,138),
    'field_black': (365,397,700,452),
    'tray_burgundy': (165,240,218,575),
    'point_cream': (351,190,407,315),
    'point_burgundy': (447,180,505,315),
}

def seamless_tile(crop, size=256):
    a = crop
    b = ImageOps.mirror(a)
    c = ImageOps.flip(a)
    d = ImageOps.mirror(c)
    w, h = a.size
    canvas = Image.new('RGB', (w * 2, h * 2))
    canvas.paste(a, (0, 0)); canvas.paste(b, (w, 0))
    canvas.paste(c, (0, h)); canvas.paste(d, (w, h))
    return canvas.resize((size, size), Image.Resampling.LANCZOS).filter(ImageFilter.GaussianBlur(.22))

src = Image.open(SRC).convert('RGB')
for name, box in BOXES.items():
    tile = seamless_tile(src.crop(box))
    if name == 'wood':
        tile = ImageEnhance.Color(ImageEnhance.Contrast(tile).enhance(1.08)).enhance(1.06)
    elif name in ('point_cream', 'point_burgundy'):
        tile = ImageEnhance.Contrast(tile).enhance(1.05)
    else:
        tile = ImageEnhance.Contrast(tile).enhance(1.06)
    tile.save(OUT / f'{name}.webp', 'WEBP', quality=96, method=6)
    print('wrote', OUT / f'{name}.webp')
