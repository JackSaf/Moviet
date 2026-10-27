# Moviet launcher layers — approved concept 05

- foreground.png: 1254×1254 PNG, RGBA; isolated amber/cream folded-film M with transparent surroundings and perforations. Padding is included.
- background.png: 1254×1254 opaque PNG; full-bleed near-black charcoal gradient, no rounded corners.
- preview.png: 512×512 composite for visual inspection, not an import layer.

Generated with the built-in image_gen tool on 2026-10-27 using design/icon-concepts/05-soft-film-monogram-amber.png as the reference. Preview rendered with System.Drawing. Alpha was checked at the canvas corner and a film perforation; composite visually inspected on the actual background.

## Import in Android Studio

1. Right-click app/src/main/res → New → Image Asset.
2. Select Launcher Icons (Adaptive and Legacy).
3. Foreground Layer → Image → foreground.png. Set Trim to No and Resize to 100% to retain the prepared transparent margins.
4. Background Layer → Image → background.png, Resize 100%.
5. Review Circle, Squircle and Square previews, then Next → Finish.
6. Ensure the application's android:icon references the generated @mipmap/ic_launcher resource; the existing scaffold may still point to @drawable/ic_launcher.

Sources: [Android Studio Image Asset](https://developer.android.com/studio/write/create-app-icons), [Android adaptive-icon layers and safe area](https://developer.android.com/develop/ui/compose/system/icon_design_adaptive).

## Final foreground prompt

Use case: identity-preserve. Production asset for Android adaptive launcher foreground. Reference is the approved Moviet icon: reproduce ONLY its large amber-orange and warm cream folded film-ribbon M, with the exact same three holes on each leg, rounded shoulders, continuous soft central curve and matte dimensional shading. RECREATE the isolated artwork with impeccably clean, smooth antialiased edges; do not use crude background color-keying. Genuinely transparent RGBA background and transparent holes. No yellow fringe, no neon yellow edge, no outline, no stray pixels, no matte halo, no dirt, no background shadow. Amber #FFB54F to #F59B34 and cream #FFE1AE; avoid saturated lemon yellow. No letters or numerals, no tile, no border, no other instances. Square image. Extremely important: the M must be centered and relatively SMALL on the square canvas, occupying precisely 56% of canvas WIDTH and approximately 33% of canvas HEIGHT, leaving 22% transparent margin on BOTH left and right and about 33% above and below. This padding is mandatory for Android launcher masking. Do not zoom the symbol to fill the image. Deliver a final clean isolated film M with soft folds and alpha transparency.

## Background prompt

Use case: precise-object-edit. Asset type: production Android adaptive launcher icon BACKGROUND PNG layer. Use the near-black charcoal surface inside the LARGE icon tile on the left of the reference as the only color/style reference. Produce one square 1024x1024 background, full bleed edge to edge: extremely subtle smooth charcoal gradient, very slightly lighter around upper center (#14191D) fading gently to near-black (#080A0C) toward lower edges. Matte smooth appearance, virtually flat. Remove all letters, ribbons, logos, film holes, labels and presentation layout. Absolutely no symbol, no icon tile silhouette, no rounded corners, no outline, no rim, no drop shadow, no bevel, no texture, no vignette border. Every pixel opaque. The whole square is the background layer; Android will apply its own mask.

