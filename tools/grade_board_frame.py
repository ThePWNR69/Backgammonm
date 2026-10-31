#!/usr/bin/env python3
"""One-time colour grade of the Classic Burgundy board frame: deeper, less orange walnut.

The v1.28 frame art is a saturated orange burl; design/reference/CLASSIC_BURGUNDY_REALISM_TARGET.png
shows a darker, browner walnut. This keeps every bevel, shadow and trim line (luminance structure is
preserved) and only moves wood-hued pixels toward the target tone. Brass plates, the compass inlay
and gold trim (hue >= ~34 deg or bright) and the burgundy trays (red hue) are protected by colour. Geometry is untouched, so
BoardMap alignment is unaffected.

Run once on the v1.28 board; it refuses to run twice (checks mean wood saturation).
"""
from pathlib import Path
import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
PATH = ROOT / 'app/src/main/assets/cosmetics/boards/classic_burgundy/board.webp'

SAT_SCALE = 0.86
VAL_SCALE = 0.78
HUE_SHIFT = 2.0   # degrees, slightly browner

im = Image.open(PATH).convert('RGB')
W, H = im.size
assert (W, H) == (2048, 977), 'board must be the 2048x977 production template'
rgb = np.asarray(im, np.float32) / 255.0
mx, mn = rgb.max(-1), rgb.min(-1)
v = mx
d = mx - mn
s = np.where(mx > 1e-6, d / np.maximum(mx, 1e-6), 0)
r, g, b = rgb[..., 0], rgb[..., 1], rgb[..., 2]
hue = np.zeros_like(v)
m = d > 1e-6
rc = np.where(m, (mx - r) / np.maximum(d, 1e-6), 0)
gc = np.where(m, (mx - g) / np.maximum(d, 1e-6), 0)
bc = np.where(m, (mx - b) / np.maximum(d, 1e-6), 0)
hue = np.where(mx == r, bc - gc, np.where(mx == g, 2.0 + rc - bc, 4.0 + gc - rc))
hue = (hue / 6.0) % 1.0
hdeg = hue * 360.0


def ramp(x, a, b_):
    t = np.clip((x - a) / (b_ - a), 0, 1)
    return t * t * (3 - 2 * t)


# weight: wood hue band 6..31 deg full, fading to nothing by 39 deg (brass/gold) and below 3 deg (red trays)
w = ramp(hdeg, 3, 8) * (1 - ramp(hdeg, 31, 39))
w *= ramp(s, 0.35, 0.55)           # only clearly saturated wood
w *= (1 - ramp(v, 0.60, 0.80))     # bright brass/specular/trim stays

# sanity guard so the grade is not applied twice
sel = w > 0.6
if sel.sum() == 0 or s[sel].mean() < 0.70:
    raise SystemExit('Frame already graded (or no wood found) - nothing to do.')

hn = (hdeg + HUE_SHIFT * w) % 360 / 360.0
sn = s * (1 - w * (1 - SAT_SCALE))
vn = v * (1 - w * (1 - VAL_SCALE))
hi = (hn * 6.0).astype(np.int32) % 6
f = hn * 6.0 - np.floor(hn * 6.0)
p = vn * (1 - sn)
q = vn * (1 - f * sn)
t = vn * (1 - (1 - f) * sn)
out = np.zeros_like(rgb)
for k, (rr, gg, bb) in enumerate([(vn, t, p), (q, vn, p), (p, vn, t), (p, q, vn), (t, p, vn), (vn, p, q)]):
    sel_k = hi == k
    out[..., 0][sel_k], out[..., 1][sel_k], out[..., 2][sel_k] = rr[sel_k], gg[sel_k], bb[sel_k]
Image.fromarray(np.uint8(np.clip(out * 255 + 0.5, 0, 255)), 'RGB').save(PATH, quality=95, method=6)
print(f'Graded {int((w > 0.5).sum()):,} wood pixels in {PATH.name}')
