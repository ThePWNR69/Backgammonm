# Backgammon Android — v0.5 Smooth Motion

v0.5 is a rendering-performance pass focused on making checker and dice animation visibly smoother on real Android phones.

## What changed

- Removed the v0.4 forced software-rendering layer; the board now uses Android's normal hardware-accelerated rendering path.
- Explicitly keeps application hardware acceleration enabled.
- The expensive static board (wood frame, leather field, points, centre bar and off tray) is rasterized only when the View size changes instead of being rebuilt every animation frame.
- Ivory/walnut checker artwork is pre-rendered into reusable sprites, so a frame no longer creates dozens of radial gradients and shadows.
- Dice faces are also pre-rendered into reusable sprites; rolling now mostly rotates/translates cached images.
- Per-frame glow effects no longer rely on software shadow layers; layered translucent strokes reproduce the glow on the GPU path.
- Movement uses `postInvalidateOnAnimation()` and a smoother path interpolator.
- On Android 15 / API 35+, the board requests the system's HIGH frame-rate category while dice or checker animation is active, then releases the request when motion ends.
- Optional **Show FPS** item in the in-game menu displays approximate draw FPS and whether the board is on GPU or CPU rendering. This is for testing and can stay off normally.
- Gameplay, AI, opening roll, Undo, End Turn, stack display and move highlighting from v0.4 are retained.

## Performance target

The goal is to keep rendering below the frame deadline: about 16.7 ms for 60 Hz, 11.1 ms for 90 Hz and 8.3 ms for 120 Hz. Actual refresh rate still depends on the phone, Android's scheduler, power mode and thermal state.

## Build

Upload/replace the files in your GitHub repository and run **Actions → Build Android APK**. The artifact is named **Backgammon-v0.5-APK**.
