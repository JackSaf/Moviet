# Moviet visual theme

The supplied cinema/carousel image is the visual reference. The theme takes its colors from the phone UI: near-black backgrounds, charcoal inputs and cards, off-white headings, muted metadata, and amber actions, ratings and selection. The surrounding landscape, presentation headings, movie posters and Russian annotations are not app UI assets or instructions.

Only a dark design was supplied. `MovietTheme` deliberately stays dark regardless of device appearance; it does not use dynamic wallpaper colors. A light palette is not yet designed. Search and details now use the shared theme, including the search field, result rows, bottom navigation, posters and save action. The favorites ticket list and thumbnail navigation remain future feature work.

## Color roles

Use `MaterialTheme.colorScheme` instead of repeating color literals in features.

| Role | Value | Use |
| --- | --- | --- |
| `background`, `surface` | `#080B0D` | Screen and navigation backgrounds |
| `surfaceContainer` | `#141A1E` | Search fields and neutral cards |
| `surfaceContainerHigh` | `#1E252A` | Raised panels |
| `primary` | `#FFB74D` | Actions, active navigation, rating stars, selection borders |
| `onPrimary` | `#241705` | Text/icons on filled amber controls |
| `onSurface` | `#F2F2F2` | Headings and main text |
| `onSurfaceVariant` | `#B7BFC7` | Metadata, supporting text and inactive icons |
| `outline` | `#78828B` | Input/control outlines |
| `outlineVariant` | `#384148` | Subtle card borders and dividers |
| `error` | `#FFB4AB` | Error messages |

Every Material 3 color role is specified, including inverse and fixed roles. Elevated surfaces retain neutral charcoal colors through a transparent `surfaceTint`. Use `scrim` with an appropriate opacity/gradient for text over posters; artwork varies, so check readability per component.

Material components have their own default role mappings. For the reference's filled amber genre selection, set `FilterChipDefaults.filterChipColors(selectedContainerColor = colorScheme.primary, selectedLabelColor = colorScheme.onPrimary)` and use `CircleShape`. Fields/cards can use `MaterialTheme.shapes.large`. Match the outlined input's container color to `surfaceContainer` when implementing the actual search component. Theme tokens alone do not recreate all component styling.

## Typography and geometry

`MaterialTheme.typography` supplies the full scalable type scale. Use `headlineLarge` for the 32sp bold app heading, `headlineMedium` for 28sp movie titles, `bodyMedium` for 14sp summaries, and `bodySmall` for 12sp metadata. The source contains no font asset or font specification; Android's sans serif approximates the reference and supports Latin/Cyrillic without downloads.

`MaterialTheme.shapes` provides 4, 8, 12, 16 and 24dp corner radii. Use `Spacing` for the 4, 8, 12, 16, 24, 32 and 48dp spacing scale. Keep touch targets at Material defaults even when visible icons or chips look smaller in the reference.

The XML launch theme and launcher mark share the Compose background and amber colors through `res/values/colors.xml`. Keep those two XML values aligned with `Color.kt`. The Activity explicitly requests light system bar icons because the app stays dark even on a light-system device. Screen Scaffolds retain ownership of content insets.

## Review

Open `app/src/debug/java/com/jacks/moviet/core/designsystem/theme/MovietThemePreview.kt` in Android Studio's Compose preview. It has four configurations: dark system, light system, 320dp narrow window, and 320dp with 1.5× text. The fixture and its strings are debug-only; no sample movies or controls are added to production screens. These are preview configurations, not automated screenshot tests.

Verification on 2026-10-27: `:app:assembleDebug :app:lintDebug --no-daemon` passed. Main text/background contrast is 17.63:1, supporting text/container contrast is 9.44:1, and dark text/amber button contrast is 10.11:1. Preview configurations compile; rendered preview and device appearance have not been visually verified.
