# Brand source assets

These live in the repo because until October 2026 they did not exist anywhere
except as uploads inside Google Play Console, and both apps consequently
shipped Android Studio and Kotlin Multiplatform template icons by accident.

## Master

- **TrueNorth Logo 2026.png** - 512x512, the real icon. Everything else is
  generated from this: the iOS `app-icon-1024.png`, the Android launcher
  mipmaps, and the adaptive-icon foreground (keyed out of the red background
  using the green channel, since the artwork is pure white on #B6061A).

Background red is **#B6061A**, which is slightly deeper than the in-app
`CanadaRed` (#D80621). Use #B6061A for anything that has to sit flush against
the icon.

## Not masters - downscaled copies pulled back off the Play listing

- **TrueNorth Feature Graphic.png** - 512x250. Play requires **1024x500**, so
  this is exactly half size and cannot be re-uploaded.
- **Android Screenshot1-6.png** - 230x512 thumbnails. Play requires at least
  320px on the short edge.

Kept only for reference. If either is ever needed again, the full-size
originals have to come from Play Console, or be remade.
