# Theme-aligned catalog exploration

Design review, 2026-10-27. These are mockup proposals, not implemented Android screens or a final layout selection.

## Direction

Remove the oversized first-result poster and its scroll-collapse transition. Search results have equal importance; the first API result is not an editorial recommendation. The search field and type filters remain available above ordinary scrolling results. Larger artwork belongs on the details screen.

The reviewed alternatives are **Compact list**, **Poster grid**, and the previously accepted **Saved tickets** treatment. The compact list is recommended for the assignment's list requirement, readable long titles, and result density. The grid is a visual alternative, not an additional mandatory app mode. No layout toggle is required by this proposal.

## Theme alignment

Use the existing Compose theme as the source of truth:

- Screen/navigation: `background` / `surface`, `#080B0D`.
- Search and ticket/card surfaces: `surfaceContainer`, `#141A1E`.
- Raised feedback: `surfaceContainerHigh`, `#1E252A`.
- Selected filters, bookmarks and ticket stubs: `primary`, `#FFB74D`; filled-control content: `onPrimary`, `#241705`.
- Main text: `onSurface`, `#F2F2F2`; secondary text: `onSurfaceVariant`, `#B7BFC7`.
- Input outlines: `outline`, `#78828B`; card edges/dividers: `outlineVariant`, `#384148`.
- App heading: 32/40 bold; screen/movie heading: 28/36 bold; list titles: 16/24 semibold; grid titles: 14/20; metadata: 12/16.
- Search/cards: 16dp corners; small posters: 8dp; filter chips: full rounding. Use the existing spacing scale and at least 48dp action targets.

The browser mockup approximates Android's sans-serif typography; it is not a rendered Compose screen. Poster illustrations are schematic, not the API's actual movie artwork.

## Data and interaction

Use the saved `design/api-samples` responses; no additional API checks were made. Search cards show title, year, type, poster and the local favorite action. Type chips replace unsupported genre filters. Ratings, runtime and plot are reserved for details when available. Search starts with an empty query in the real app; the mockup opens on the saved Batman result set to expose the layout.

Saved tickets retain the perforated amber stub, with a normal labeled bookmark action and undo feedback. The ticket body opens details. A details navigation sketch allows moving through the originating list and returning to its scroll position; it does not establish a requirement for a pager on the search screen.

The labeled ad slot illustrates surface treatment only. Final placement at indexes 1 and 3 still depends on the unresolved index semantics documented in `docs/PROJECT.md`; this mockup is not a complete ad-placement specification.

## Review checks

Checked in the browser preview: exact background token, list/grid rendering, type filtering, save/remove with undo, opening a saved movie, next-film navigation, and no horizontal overflow in the ticket layout at a 320px viewport. No Android app code changed and no device testing was performed in this design pass.
