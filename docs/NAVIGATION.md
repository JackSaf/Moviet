# Adaptive search and details

The app uses a single saveable Navigation 3 back stack. Search is the root; a selected movie adds `NavigationRoute.Details(imdbId)`. Selecting another result replaces that details route, so Back returns directly to the results. Navigation entries retain their saveable state and own their Koin ViewModel stores.

## Window policy

`NavigationRoot` reads `LocalWindowInfo.current.containerSize.width`, converts pixels to dp with `LocalDensity`, and compares the result with the named 600dp breakpoint. This follows available app-window width, including resizing and split-screen, rather than checking orientation or identifying a tablet.

- Below 600dp: Navigation 3's default single-pane scene displays search or details.
- At least 600dp: the custom `ListDetailScene.Strategy` pairs adjacent list/details entries, with 40% of the width for results and 60% for details. A search-only stack shows a detail placeholder.
- An unrelated destination, such as Favorites, uses its own single-pane scene.

The list scene key stays stable when the selected details entry changes. The scene animates detail selection internally. `previousEntries` describes the actual stack after Back, including the wide search scene with its placeholder. The scene is a data class so equality includes its entries and back state.

Each pane owns its screen insets. Pane boundaries recalculate window insets before composing screen Scaffolds, so a physical cutout at the left edge does not add another left inset inside the right pane. Lists receive Scaffold padding through `contentPadding`. The search header paints its own opaque surface so scrolled results cannot show through it. IME visibility hides bottom navigation while the search content respects safe drawing/keyboard insets.

## Motion and data lifetime

`SharedTransitionLayout` wraps `NavDisplay`. Narrow layouts pass its scope plus `LocalNavAnimatedContentScope` into poster components, matching images by IMDb ID. The selected result is retained as an in-memory preview so the destination poster exists during the initial transition while full details load; only the stable IMDb ID is serialized in the route. After process recreation, a missing preview falls back to loading the route's details normally.

Shared poster matching is disabled when both panes are visible. Wider details use a small poster beside the title/metadata to leave useful room in short landscape windows. Global forward, backward and predictive-back transforms are provided through the Navigation 3 transition APIs.

Search's existing cached Paging stream survives navigating away and back. `rememberLazyListState` is owned by the saveable search entry. The details ViewModel performs no work during construction; collecting state after `LoadMovie` starts the request. Successful responses are reused after subscriber restarts; Retry clears that cached result. Failed loads may be attempted again when collection restarts. No new requests depend on orientation.

## Sources

The scene is adapted from Google's Apache-licensed [Navigation 3 list-detail recipe](https://github.com/android/nav3-recipes/tree/main/app/src/main/java/com/example/nav3recipes/scenes/listdetail), with a placeholder state and pane insets added for Moviet. Relevant official guidance:

- [Create custom layouts using Scenes](https://developer.android.com/guide/navigation/navigation-3/scenes)
- [Navigation with shared elements](https://developer.android.com/develop/ui/compose/animation/shared-elements/navigation)
- [Animate between destinations](https://developer.android.com/guide/navigation/navigation-3/animate-destinations)
- [Support different display sizes](https://developer.android.com/develop/ui/compose/layouts/adaptive/support-different-display-sizes)

The existing stable Navigation 3 and Compose BOM versions are retained. Foundation and Animation are now explicit BOM-managed dependencies; no Material Adaptive pre-release dependency was introduced.
