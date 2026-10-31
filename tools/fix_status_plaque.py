#!/usr/bin/env python3
"""Replace the see-through title window of premium_status.webp with an opaque recessed gold plate.

v1.28's plaque had its inner title window at only ~28% opacity, so the green felt showed through as an
olive box behind the live status text, and the longer messages ("ROLL TO DECIDE FIRST") spilled past it.
This repaints a wider plate (lighter gold than the surrounding bevel, thin inner shadow, fine brushed
grain, dark hairline rim) so the whole text-safe area sits on solid gold. Everything outside the plate
is untouched. Live text colour (#2A180C) stays readable on it.

Run once on the v1.28 asset; it refuses to run twice.
"""
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw
import scipy.ndimage as ndi

ROOT = Path(__file__).resolve().parents[1]
PATH = ROOT / 'app/src/main/res/drawable-nodpi/premium_status.webp'
SS = 4          # supersample for a clean rounded edge
SIDE_PAD = 125  # how far the new plate extends beyond the old window, each side (asset px)
RIM_PAD = 5     # also cover the old window's dark rim above/below it

im = Image.open(PATH).convert('RGBA')
a = np.array(im)
h, w = a.shape[:2]

# locate the old window (connected low-alpha region containing the plaque centre)
lab, _ = ndi.label(a[..., 3] <= 110)
cl = lab[h // 2, w // 2]
if cl == 0:
    raise SystemExit('No transparent title window found (already fixed?) - nothing to do.')
old = ndi.binary_fill_holes(lab == cl)
ys, xs = np.nonzero(old)
y0, y1, x0, x1 = ys.min(), ys.max(), xs.min(), xs.max()
x0, x1 = x0 - SIDE_PAD, x1 + SIDE_PAD
y0, y1 = y0 - RIM_PAD, y1 + RIM_PAD

# anti-aliased rounded-rectangle coverage for the new plate
big = Image.new('L', (w * SS, h * SS), 0)
ImageDraw.Draw(big).rounded_rectangle((x0 * SS, y0 * SS, (x1 + 1) * SS, (y1 + 1) * SS), radius=30 * SS, fill=255)
cov = np.asarray(big.resize((w, h), Image.Resampling.LANCZOS), np.float32) / 255.0
inside = cov > 0.5

v = np.clip((np.arange(h, dtype=np.float32) - y0) / max(1, (y1 - y0)), 0, 1)[:, None]
top = np.array([240, 214, 150], np.float32)
bot = np.array([214, 178, 104], np.float32)
plate = np.broadcast_to(top[None, None] * (1 - v[..., None]) + bot[None, None] * v[..., None], (h, w, 3)).copy()

rng = np.random.default_rng(3)
brush = ndi.gaussian_filter(rng.normal(0, 1, (h, w)).astype(np.float32), (0.6, 22))
brush /= (np.abs(brush).max() + 1e-6)
plate *= (1 + 0.035 * brush)[..., None]

dist = ndi.distance_transform_edt(inside).astype(np.float32)
shadow = np.clip(1 - dist / 14.0, 0, 1) * np.clip((0.70 - v) / 0.55, 0, 1)
plate *= (1 - 0.22 * shadow)[..., None]
lip = np.clip(1 - dist / 4.0, 0, 1) * np.clip((v - 0.45) / 0.45, 0, 1)
plate = plate * (1 - 0.35 * lip[..., None]) + np.array([255, 240, 190], np.float32) * (0.35 * lip[..., None])

# dark hairline rim around the recess
rim = np.clip(1 - np.abs(dist - 1.6) / 1.6, 0, 1) * inside
plate = plate * (1 - 0.85 * rim[..., None]) + np.array([122, 82, 34], np.float32) * (0.85 * rim[..., None])

out = a.astype(np.float32)
c = cov[..., None]
out[..., :3] = out[..., :3] * (1 - c) + plate * c
out[..., 3] = np.maximum(out[..., 3], cov * 255.0)
# the old olive fill is fully replaced inside the plate
out[inside, 3] = 255
Image.fromarray(np.uint8(np.clip(out + 0.5, 0, 255)), 'RGBA').save(PATH, lossless=True, quality=100, method=6)
print(f'Repainted title plate {x1 - x0 + 1}x{y1 - y0 + 1}px in {PATH.name}')
