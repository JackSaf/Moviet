# Moviet project conventions

These conventions adapt Notis to this project. They are maintained here; `AGENTS.md` and the local project skill route new tasks to this file. Explicit user instructions take precedence.

## Package ownership

Keep one application module for the current assignment. Group code by feature under `app/src/main/java/com/jacks/moviet`:

```text
app/                      Application, Activity, app DI, root navigation
core/database/            Room database, entities, DAOs, database DI
core/network/             Shared Retrofit/OkHttp configuration and network DI
core/designsystem/        Shared Compose theme, components, tokens
core/presentation/util/   UiText, event observer, sharing policy
search/                   data, domain, presentation/components, di
details/                  data, domain, presentation/components, di
favorites/                data, domain, presentation/components, di
```

`SDK/src/main/java/com/jacks/moviet/sdk` owns advertising. Dependency direction is `app → SDK`; SDK must not import app classes. Do not introduce extra Gradle modules or abstractions without an actual need. Empty `.gitkeep` packages are reserved for upcoming work; replace their marker when adding code.

Keep DTOs and serialization details in `data`, business models/contracts in `domain`, and UI state/mappers in `presentation`. Shared movie models can move into `core/domain` once multiple features use them. Do not expose Room entities or Retrofit DTOs directly as screen state. Prefer constructor injection and focused files; avoid a global `Constants.kt`.

## Initial data and state lifetime

- Do not fetch data, start observation, or launch jobs in a screen ViewModel `init` block.
- Derive public state from cold repository flows where possible. Trigger initial suspend work with `.onStart { ... }`, then expose state with `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(StateFlowPolicy.STOP_TIMEOUT_MILLIS), initialValue)`.
- `STOP_TIMEOUT_MILLIS` is 5,000 ms. Resolve it from the shared policy rather than repeating a literal.
- `onStart` runs again when the upstream is subscribed again. Define an explicit refresh policy (`refreshIfNeeded`, cache age, query change); do not assume it runs once per ViewModel.
- Prefer a directly composed cold flow over launching a second collector from `onStart`. If a separate long-lived collector is necessary, hold its `Job` and guard against duplicate active collectors. Decide whether it should stop with subscribers or live until ViewModel clearing.
- Use `flatMapLatest` when input changes invalidate previous work. Apply `distinctUntilChanged` and a named debounce duration to a query stream where appropriate. Preserve coroutine cancellation; do not turn `CancellationException` into a UI error.
- Paging streams use `cachedIn(viewModelScope)` after query-dependent transformations. Do not recreate a Pager in a composable or on every recomposition.
- The current SearchViewModel has static scaffold state and no data source. It deliberately performs no startup work; do not add a dummy fetch just to follow a pattern.

Typical future state pipeline (adapt to the feature's types; this is not an existing repository API):

```kotlin
val state = repository.observeMovies()
    .map { movies -> SearchState(/* map content here */) }
    .onStart { repository.refreshIfNeeded() }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(StateFlowPolicy.STOP_TIMEOUT_MILLIS),
        initialValue = SearchState(),
    )
```

Map expected failures to typed results and UI state; keep refresh policies cancellation-safe. Use `collectAsStateWithLifecycle()` in screen roots. Observe one-off ViewModel event flows with the shared `ObserveAsEvents`, not a separate screen-level collector. Durable outcomes should remain observable as state; a one-off event is not a durable result store.

## Compose and presentation

- Split `*ScreenRoot` (ViewModel, lifecycle collection, navigation/event wiring) from `*Screen` (state and callbacks). Keep supporting UI under sibling `presentation/components` when it is substantial or reused.
- In composable declarations and call sites, put `modifier` first whenever present. Stateless previews should not need Koin, a database, a network connection, or an Activity.
- All app-authored user-visible strings belong in Android resources. Messages exposed by a ViewModel use `UiText.StringResource`; external text can use `UiText.Dynamic`. Resolve text in the UI, never by keeping a Context in a ViewModel. User input and movie titles remain strings.
- Name meaningful limits, durations, storage keys, status values and conversion factors at their owner. Obvious index identities, empty values, SQL, serialization annotations and explicit preview fixtures may contain literals.
- Reuse design-system spacing, color, typography and animation tokens. Keep component-specific named values close to the component. Prefer available space, weights, aspect ratios and min/max bounds over fixed card/panel dimensions.
- For window-level adaptive decisions use `LocalWindowInfo.current.containerSize.width` converted with `LocalDensity`; use a named 600.dp breakpoint unless the design says otherwise. Do not use orientation or `BoxWithConstraints` for this purpose.
- Keep composables shallow. Remove layout wrappers with no behavior and extract coherent sections rather than splitting every tiny function into a file.
- Enable edge-to-edge in activities, handle system bars and IME, and apply Scaffold padding once. Pass padding to a lazy list's `contentPadding` when the content should scroll behind bars. Check dark theme, large fonts and narrow windows when changing layouts.

## Navigation and DI

Use Navigation 3: serializable `NavKey` routes, `rememberNavBackStack`, `NavDisplay`, `entryProvider`, saveable-state and ViewModel-store entry decorators. Pass stable movie IDs to details, not DTO/entity objects. Do not add Navigation 2, Hilt or a custom shared ViewModel store without a demonstrated requirement.

Koin starts once from `MovietApplication`. Keep feature registrations in each feature's `di` package and compose modules in `app/di`. Use the installed Koin compiler DSL (`org.koin.plugin.module.dsl.viewModel`, `single`, etc.) for constructor registrations, and provider lambdas for builders/external factories. Explicitly bind implementations to their interfaces. Resolve screen ViewModels with `koinViewModel()` inside a navigation entry. No Koin KSP processor; KSP currently belongs to Room only.

## Room, Paging and network

- DAOs expose reactive reads as Flow and writes as suspend functions. Keep database operations off the main thread; do not enable `allowMainThreadQueries`.
- Commit Room JSON schemas. Increment the DB version and provide a migration when a persisted schema changes; do not silently enable destructive migration.
- Use IMDb IDs as movie identity and lazy list keys. Normalize OMDb values such as `N/A` at the data boundary. Model HTTP errors and OMDb `Response=False` separately; the latter may use HTTP 200.
- Keep OMDb authentication in the API client, use HTTPS, and keep API credentials out of Git and request logs. The injected OkHttp client is specifically for OMDb; do not reuse its API-key interceptor for poster hosts.
- Use Retrofit + Kotlin Serialization for the API and Coil for posters. Do not add redundant networking/image stacks.
- Paging integration is pending. Document OMDb page numbering and end-of-pagination behavior from its API before implementing PagingSource. Add ads as presentation list items, not database entities or fake movies.

## Build and verification

Use pinned newest stable versions, not `+`, snapshots or previews. Check upstream compatibility before changing Kotlin, AGP, Gradle, KSP or the Koin compiler plugin. Record checks in `docs/DEPENDENCIES.md`. Use AGP's built-in Kotlin and modern Android DSL; the root buildscript explicitly pins the newer Kotlin Gradle plugin classpath. Do not bring the obsolete Kotlin Android plugin and legacy DSL flags over from Notis.

Always run Gradle with `--no-daemon`. For foundation/build changes run `:app:assembleDebug :app:lintDebug`. Validate release shrinking when release configuration or reflection/serialization behavior changes. Run relevant behavior tests when they exist; do not claim an empty test task verifies behavior. Device tests require a selected device; do not install or launch on an arbitrary personal device without task context.

The local Windows checkout lives at `C:\Users\jacks\AndroidStudioProjects\Moviet`. This path uses ASCII characters, so no `android.overridePathCheck` workaround is needed. Keep machine-specific SDK and JDK paths out of shared build configuration. The project has no native/CMake component.

Keep `local.properties`, credentials, keystores, caches, build output and IDE state ignored. Keep shared instructions, project skills, Wrapper and Room schemas tracked. Do not stage, commit, add a remote or publish until requested.
