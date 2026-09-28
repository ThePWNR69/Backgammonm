# Backgammon Legacy v1.6.5 — Checker Geometry Lock

## Purpose

v1.6.5 begins the geometry-first rebuild of the gameplay presentation. Instead of visually guessing checker size and spacing, the renderer now follows the approved point/checker relationship as a mathematical rule.

## Locked checker geometry

- Checker diameter is now **90% of the narrowest triangle base width**.
- A five-checker stack spans the complete measured triangle height from base to apex.
- The fifth checker includes a very small apex-cover guard so the painted triangle tip cannot remain visible because of scaling or anti-aliasing.
- Spacing is calculated from each point's measured height rather than a generic visual spacing clamp.
- Checker X remains calculated from the midpoint of the actual triangle edges at that checker's Y coordinate.

### Approved reference relationship

For the design reference discussed during the geometry pass:

- Triangle base: **100 px**
- Triangle height: **278 px**
- Checker diameter: **90 px**
- Five-checker centre pitch with the apex-cover guard: approximately **47.56 px**

The Android renderer applies the same proportions to the actual production board dimensions, so it remains resolution-independent.

## High stacks

The existing 6+ count badge remains in place temporarily. A dedicated 6+ visual treatment will be designed separately.

## Version

Version code: **24**  
Version name: **1.6.5**  
GitHub Actions artifact: `Backgammon-Legacy-v1.6.5-APK`
