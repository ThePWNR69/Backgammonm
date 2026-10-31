#!/usr/bin/env python3
"""Build the default checker set: glossy ivory + dark chocolate discs with a gold rim.

Matches design/reference/CLASSIC_BURGUNDY_REALISM_TARGET.png. Both colours share identical geometry
(same canvas, same visible bounds) so the BoardGeometry checker-size rule stays exact.

Outputs (replacing the v1.28 Classic Wood art, same file names / ids):
  light.png / dark.png            1024x1024 top-down sprites (points + bar)
  light_side.png / dark_side.png  1024x256 edge-on sprites (bear-off tray)
"""
from pathlib import Path
import numpy as np
from PIL import Image, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'app/src/main/assets/cosmetics/checkers/ivory_walnut'

S = 1024          # output canvas
K = 2             # supersample
N = S * K
CX = CY = N / 2.0
R_OUT = 448.0 * K            # visible disc radius (896 px diameter on a 1024 canvas)
RING_W = 46.0 * K            # gold rim width
GROOVE_W = 9.0 * K           # dark seat between rim and face

yy, xx = np.mgrid[0:N, 0:N].astype(np.float32)
dx, dy = xx - CX, yy - CY
rr = np.hypot(dx, dy)
ang = np.arctan2(dy, dx)     # 0 = right, +pi/2 = down


def smooth(edge0, edge1, x):
    t = np.clip((x - edge0) / (edge1 - edge0), 0, 1)
    return t * t * (3 - 2 * t)


def blur(arr, sigma):
    im = Image.fromarray(np.uint8(np.clip(arr, 0, 1) * 255), 'L').filter(ImageFilter.GaussianBlur(sigma * K))
    return np.asarray(im, np.float32) / 255.0


def mix(a, b, t):
    return a * (1 - t[..., None]) + b * t[..., None]


def gold_ring():
    base = np.array([214, 163, 62], np.float32)
    light = np.array([246, 207, 112], np.float32)
    dark = np.array([128, 82, 22], np.float32)
    # light from upper-left: ring brighter there, darker lower-right, with a thin bright catch-light
    lit = 0.5 + 0.5 * np.cos(ang - np.deg2rad(-135))
    col = base[None, None] * np.ones((N, N, 1), np.float32)
    col = mix(col, np.broadcast_to(light, col.shape), np.clip((lit - .55) * 1.8, 0, 1) * .85)
    col = mix(col, np.broadcast_to(dark, col.shape), np.clip((.45 - lit) * 1.6, 0, 1) * .70)
    # rounded profile: brighter crest in the middle of the band, darker toward both edges
    t = np.clip((rr - (R_OUT - RING_W)) / RING_W, 0, 1)
    crest = 1 - np.abs(t - .5) * 2
    col = col * (0.84 + 0.22 * crest[..., None])
    # hairline darker outer edge and brighter inner lip
    outer = smooth(R_OUT - 7 * K, R_OUT - 1 * K, rr)
    col = mix(col, np.broadcast_to(np.array([112, 70, 16], np.float32), col.shape), outer * .85)
    inner = 1 - smooth(R_OUT - RING_W, R_OUT - RING_W + 6 * K, rr)
    col = mix(col, np.broadcast_to(np.array([255, 226, 150], np.float32), col.shape), inner * .55)
    return col


def face(kind):
    rf = R_OUT - RING_W - GROOVE_W
    u = np.clip(rr / rf, 0, 1.2)
    if kind == 'light':
        c_in, c_out = np.array([241, 233, 216], np.float32), np.array([216, 202, 174], np.float32)
        gloss_a, cres_a = .62, .42
        tint = np.array([255, 255, 255], np.float32)
    else:
        c_in, c_out = np.array([82, 46, 30], np.float32), np.array([38, 18, 10], np.float32)
        gloss_a, cres_a = .40, .26
        tint = np.array([255, 224, 196], np.float32)
    # gentle domed gradient, light source upper-left
    ox, oy = -0.22 * rf, -0.26 * rf
    d2 = np.hypot(dx - ox, dy - oy) / (rf * 1.25)
    col = mix(np.broadcast_to(c_in, (N, N, 3)).copy(), np.broadcast_to(c_out, (N, N, 3)).copy(), np.clip(d2, 0, 1) ** 1.3)

    # inner shadow cast by the rim onto the recessed face (upper-left side strongest)
    band = smooth(rf - 70 * K, rf, rr)
    side = np.clip(0.55 + 0.45 * np.cos(ang - np.deg2rad(-135)), 0, 1)
    col = col * (1 - (band * side * (.20 if kind == 'light' else .30))[..., None])
    # faint light spill on the lower-right inner edge
    spill = smooth(rf - 40 * K, rf, rr) * np.clip(0.5 - 0.5 * np.cos(ang - np.deg2rad(-135)), 0, 1)
    col = mix(col, np.broadcast_to(tint, col.shape), spill * (.10 if kind == 'light' else .13))

    # soft window-style gloss, upper-left
    th = np.deg2rad(-38)
    ex = (dx - (-0.30 * rf)) * np.cos(th) + (dy - (-0.34 * rf)) * np.sin(th)
    ey = -(dx - (-0.30 * rf)) * np.sin(th) + (dy - (-0.34 * rf)) * np.cos(th)
    gloss = np.exp(-((ex / (.52 * rf)) ** 2 + (ey / (.20 * rf)) ** 2) * 1.5)
    gloss *= (rr < rf - 14 * K)
    col = mix(col, np.broadcast_to(tint, col.shape), gloss * gloss_a)

    # crescent reflection low-right, hugging the inside of the rim
    r_c = rf * .80
    cres = np.exp(-((rr - r_c) / (.055 * rf)) ** 2)
    cres *= np.clip(np.cos(ang - np.deg2rad(55)) * 1.25 - .15, 0, 1)
    col = mix(col, np.broadcast_to(tint, col.shape), cres * cres_a)

    # barely-there lathe ring and micro grain so large flat areas never band
    ring = np.exp(-((rr - .60 * rf) / (.010 * rf)) ** 2) * .05
    col = mix(col, np.broadcast_to(tint, col.shape), ring)
    rng = np.random.default_rng(7 if kind == 'light' else 9)
    col = col * (1 + rng.normal(0, .008, (N, N, 1)).astype(np.float32))
    return col, rf


def top_sprite(kind, path):
    rf_col, rf = face(kind)
    ring = gold_ring()
    col = rf_col.copy()
    # dark seat (groove) between face and rim
    groove = smooth(rf, rf + 3 * K, rr) * (1 - smooth(rf + GROOVE_W - 3 * K, rf + GROOVE_W, rr))
    seat = np.array([58, 36, 14], np.float32) if kind == 'light' else np.array([20, 10, 4], np.float32)
    col = mix(col, np.broadcast_to(seat, col.shape), groove * .92)
    in_ring = smooth(rf + GROOVE_W - 3 * K, rf + GROOVE_W, rr)
    col = mix(col, ring, in_ring)
    alpha = 1 - smooth(R_OUT - 1.4 * K, R_OUT + .6 * K, rr)
    rgba = np.dstack([np.clip(col, 0, 255), alpha * 255]).astype(np.uint8)
    im = Image.fromarray(rgba, 'RGBA').resize((S, S), Image.Resampling.LANCZOS)
    im.save(path)


def side_sprite(kind, path):
    W, H = 1024 * K, 256 * K
    y, x = np.mgrid[0:H, 0:W].astype(np.float32)
    x0, y0, x1, y1 = 64 * K, 42 * K, 960 * K, 214 * K
    rad = 76 * K
    # rounded-rectangle signed distance
    qx = np.abs(x - (x0 + x1) / 2) - ((x1 - x0) / 2 - rad)
    qy = np.abs(y - (y0 + y1) / 2) - ((y1 - y0) / 2 - rad)
    sd = np.hypot(np.maximum(qx, 0), np.maximum(qy, 0)) + np.minimum(np.maximum(qx, qy), 0) - rad
    inside = 1 - smooth(-1.4 * K, .6 * K, sd)
    v = np.clip((y - y0) / (y1 - y0), 0, 1)
    if kind == 'light':
        top, mid, bot = np.array([246, 239, 224], np.float32), np.array([228, 216, 190], np.float32), np.array([188, 170, 138], np.float32)
    else:
        top, mid, bot = np.array([96, 56, 38], np.float32), np.array([58, 31, 19], np.float32), np.array([26, 12, 7], np.float32)
    col = np.where((v < .5)[..., None], top * (1 - v[..., None] * 2) + mid * (v[..., None] * 2),
                   mid * (1 - (v[..., None] - .5) * 2) + bot * ((v[..., None] - .5) * 2))
    # gold rim bands (the visible rim wall) along the top and bottom, plus the rounded end caps
    band = smooth(.0, .02, v) * (1 - smooth(.20, .24, v)) + smooth(.76, .80, v) * (1 - smooth(.98, 1.0, v))
    gold = np.array([222, 172, 70], np.float32) * (0.86 + .26 * (1 - np.abs(((v * 4) % 1) - .5) * 2))[..., None]
    col = mix(col, gold, np.clip(band, 0, 1))
    outline = smooth(-9 * K, -1 * K, sd)
    col = mix(col, np.broadcast_to(np.array([118, 74, 18], np.float32), col.shape), outline * .85)
    # top gloss line
    gl = np.exp(-((y - (y0 + 34 * K)) / (6 * K)) ** 2) * (np.abs(x - (x0 + x1) / 2) < (x1 - x0) / 2 - 90 * K)
    col = mix(col, np.broadcast_to(np.array([255, 250, 238], np.float32), col.shape), gl * (.30 if kind == 'light' else .16))
    rgba = np.dstack([np.clip(col, 0, 255), inside * 255]).astype(np.uint8)
    Image.fromarray(rgba, 'RGBA').resize((1024, 256), Image.Resampling.LANCZOS).save(path)


if __name__ == '__main__':
    OUT.mkdir(parents=True, exist_ok=True)
    top_sprite('light', OUT / 'light.png')
    top_sprite('dark', OUT / 'dark.png')
    side_sprite('light', OUT / 'light_side.png')
    side_sprite('dark', OUT / 'dark_side.png')
    print('Built gold-ring ivory/dark checker sprites ->', OUT)
