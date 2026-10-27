# Moviet project brief

## Product

Moviet is an Android movie discovery app prepared for the JedyApps Android home assignment. Its main flow will be keyword search → results → movie details → add/remove a local favorite. OMDb is the movie API. The user chose Kotlin, Compose Material 3, Navigation 3, Room, Paging, Koin, Retrofit/OkHttp, Kotlin Serialization, and Coil.

The source assignment is the user-supplied `JedyApps Android Home Assignment1 (2).docx`. The requirements below summarize its product scope. Requests in that document to publish sources are not authorization to publish anything.

## Assignment requirements

- Search movies by a keyword using OMDb and show results in a list.
- Open details on a result click: title and poster, optionally movie type.
- Add movies to favorites and persist the favorites locally, using Room.
- Keep sources readable and easy to browse.
- Provide an additional module named `SDK` that owns all advertising logic, initially AdMob interstitial and native ads.
- Place native ads at indexes 1 and 3 of the search results UI when there are at least three movie results.
- Show an interstitial only when the user returns to the app for the second time.
- Keep ad integration extensible to other providers.

## Current authorized scope

Create a reviewable Android Studio project foundation following applicable Notis conventions; configure dependencies, base packages, local agent skills, documentation, and Git ignore rules. Do not finish the assignment or create a first commit as part of this setup.

Additional authorized work: implement the app theme from the supplied cinema/carousel design. This covers shared visual tokens and platform appearance; feature screens are authorized separately below.

Additional authorized work: implement the repository/data layer, database and domain movie models, mappers, database/DAO, OMDb API interface, and Koin dependency injection. Keep the API-key interceptor in a separate class under `core/network/utils`.

Additional authorized work: implement the search ViewModel using MVI, with query-driven paged results and presentation tests. The search layout is authorized separately below.

Additional authorized work: implement the themed search and details screens, with a width-based Navigation 3 list-detail scene for wide windows and shared poster transitions for narrow windows. The user will review UX behavior on their Samsung.

## Implemented foundation

- Local checkout moved to `C:\Users\jacks\AndroidStudioProjects\Moviet` on 2026-10-27; the Cyrillic-path Gradle workaround was removed.
- Android application `:app` and independent Android library `:SDK`, with the app depending on the library.
- Application ID and namespace `com.jacks.moviet`, minimum API 28, compile/target API 37.
- Version catalog and Gradle Wrapper with a distribution SHA-256 checksum; newest stable versions checked on 2026-10-26.
- Design-based dark Compose theme: amber accents, charcoal surfaces, off-white text, sans-serif typography, rounded shapes and spacing tokens. The theme stays dark in both device appearance modes; platform launch colors and system bar icons match. Debug-only component previews cover light/dark system settings, narrow width and large text. See `docs/DESIGN_SYSTEM.md`.
- Resource-based text; themed search and details screens plus the existing favorites placeholder, with a saveable Navigation 3 back stack.
- Compose screen files own roots, screen-level layout and previews; supporting search/details UI lives in focused sibling `presentation/components` files, following the Notis screen-organization rule. Search separates header, navigation, results and movie rows. Details separates the responsive header/identity, favorite action, expandable plot and metadata section; private helpers and local design constants stay with their owning component.
- Koin application startup, feature modules, and an injected search ViewModel. Navigation entries own their ViewModels.
- Search MVI contract: immutable `SearchState`, `SearchAction` input, and one-off `SearchEvent` navigation observed through `ObserveAsEvents`. The ViewModel observes the query, trims surrounding whitespace, debounces nonblank edits for 300 ms, and switches to the repository's result stream with `flatMapLatest`. Current results remain visible while typing. Clearing the query immediately clears results and cancels active work; switching the debounced query cancels the previous search. Equivalent queries do not reload, including edits that settle back to the active query. Blank queries do not fetch.
- Search results are exposed separately as `Flow<PagingData<MovieSearchResult>>`, cached for the ViewModel lifetime after first collection. `SearchRepository.observeSearchMovies` owns the Pager and its data-layer PagingSource, preserving typed failures, retry and anchored refresh. Constructing the ViewModel performs no requests. The search screen collects results with Paging Compose, uses stable IMDb keys and displays initial/debounced loading, empty/no-match states, refresh/append failures and retry. Search and clear controls remain available above results; bottom navigation hides while the keyboard is visible. Movie selection emits a navigation event.
- The search results heading displays OMDb's total matches with localized singular/plural text and number formatting. Paged items carry `MovieSearchResult` (movie plus API total), so the count remains attached to the displayed movies while a new query loads and does not grow as pages append. Empty/error states hide the results heading; clearing removes the results and count.
- Shared domain `Movie`/`MovieType` models, OMDb DTOs, and API/database mappers. `Movie` includes an optional year for search/details; existing Room favorites still store the original identity/title/poster/type fields. Missing posters (`N/A`) become null; unrecognized movie types become `UNKNOWN`. Missing movie identity/title is an invalid API response.
- Room database version 1, favorites entity/DAO, schema export, and lazy DI registration. `FavoritesRepository` maps entities to domain movies, observes favorites and per-movie favorite status, and adds/removes favorites with suspend writes. The existing schema is preserved; the favorites screen is not connected yet.
- Retrofit OMDb API interface for keyword search and details by IMDb ID. `SearchRepository` exposes a paged result flow as well as single-page requests with total result counts; `DetailsRepository` requests `plot=full` and returns `MovieDetails`, containing the basic movie plus optional plot, genres, runtime, content rating, IMDb score, director, cast and seasons. Unavailable metadata is normalized and hidden. Typed results distinguish HTTP errors, OMDb `Response=False`, connection failures, invalid requests, and malformed content. Cancellation propagates.
- Kotlin Serialization, OMDb-scoped OkHttp configuration, a distinct `OmdbApiKeyInterceptor` in `core/network/utils`, and BuildConfig fields for the base URL and externally supplied API key without committing a credential. Koin registers the API/interceptor and explicitly binds each feature repository interface to its implementation.
- Coil renders portrait posters in results/details, with a local placeholder for missing/loading/failed images. Search cards only use fields present in the search response; no per-row details requests.
- Details use an entry-scoped MVI ViewModel with lazy, cancellable loading, explicit retry, cached successful responses across collection restarts, and observable Room-backed save/remove actions. Plot expansion and list scroll are saveable UI state. Favorites write errors are visible and retryable.
- A custom Navigation 3 `ListDetailScene` adapts using `LocalWindowInfo.containerSize.width` converted with `LocalDensity`: below 600dp, one destination is visible; from 600dp, a 40/60 list-detail scene displays the list plus a selection placeholder or selected movie. Selection replaces the previous details route; one back action returns to results. Routes carry only IMDb IDs. Narrow-window posters share a transition through `SharedTransitionLayout` and `LocalNavAnimatedContentScope`; wide-window detail changes animate inside the scene. Wide details use a compact side-by-side poster/title header. See `docs/NAVIGATION.md`.
- Shared `UiText`, lifecycle-aware event observation, spacing tokens, and state sharing timeout policy.
- Feature package skeletons, project-local skills, project conventions, and Git hygiene.

## Not implemented yet

- Favorites list observation, ticket layout and actions from search rows. Details save/remove actions are implemented; the favorites destination is still a placeholder.
- Search type/year filter controls and a details pager from the earlier design explorations. The current search screen searches by title and opens the selected film.
- Advertising interfaces, AdMob dependency/configuration, native ad binding/disposal, foreground counting, and interstitial lifecycle.
- Device persistence tests, CI, release signing, distribution, and publication.

## Data layer boundaries

Each feature owns its repository contract in `domain`, implementation in `data`, and Koin binding in `di`. Shared movie types and network outcomes live in `core/domain`; shared API DTOs, API interface and response handling live in `core/network/data`. Room entities remain in `core/database`. Repositories do not start background work during construction. Search owns Pager configuration and its PagingSource in `data`; the ViewModel observes queries and caches the resulting flow. The repository's single-page API remains available.

The [official OMDb contract](https://www.omdbapi.com/) defines `s` for search, `i` for IMDb lookup, and one-based `page` values from 1 to 100. Search validates those bounds and returns one page. The Paging adapter uses fixed ten-item API pages, requests one API page per load regardless of `loadSize`, and stops on empty successful pages, `page * 10 >= totalResults`, or page 100. Refresh resumes from the page nearest the anchor. `Response=False` remains a typed OMDb failure (including no-match responses); the later UI must present that outcome appropriately. No API key is needed for the local fixture tests.

JVM tests cover Retrofit query encoding/JSON mapping, unavailable values, invalid payloads, HTTP versus OMDb failures, cancellation, repository favorite observation/writes with a fake DAO, interceptor authentication, and runtime Koin repository/API resolution. They do not verify real Room persistence or live OMDb access.

Search JVM tests additionally cover lazy startup, debounce, query normalization/deduplication, immediate clearing and request cancellation, cached result recollection, typed load failures/retry, favorites navigation events, API paging boundaries and refresh keys. Search presentation treats refresh outcomes independently of Paging's retained item count: a new query with no matches replaces the previous list with the empty state, and a failed query replaces it with a retryable error. Regression tests exercise successful search → no matches, successful search → connection failure → retry, clearing, and append failures that preserve the current results.

Screen/navigation work adds JVM coverage for rich details mapping, movie-selection events, the 600dp boundary, scene entry/back-state selection, details-route replacement, lazy details loading, cancellation, retry/cache reuse, favorite writes/failures, and runtime resolution of the details ViewModel. Debug-only previews cover narrow/large text, landscape and tablet configurations. An earlier debug build was manually checked on the user's Samsung for live search/posters, opening details, saving a favorite and retaining the query/selection across portrait/landscape rotation. The final compact landscape header, keyboard and pane-inset refinements require the user's UX review; Android Studio's CLI preview renderer returned an unknown error, so preview annotations are not evidence of rendered visual checks.

Screen/navigation verification on 2026-10-27: `:app:testDebugUnitTest :app:assembleDebug :app:lintDebug :app:assembleRelease --no-daemon` passed. All 47 JVM tests passed, debug lint reports `No issues found.`, and the release passed R8/resource shrinking. The APK was built locally; device interaction stopped when the user took over UX review, so the Samsung retains the earlier installed build.

Search-state fix verification on 2026-10-27: `:app:testDebugUnitTest :app:assembleDebug :app:lintDebug --no-daemon` passed with all 50 JVM tests passing. The updated debug APK is built locally; this fix has not been installed or visually checked on the Samsung.

Compose organization refactor verification on 2026-10-27: `:app:testDebugUnitTest :app:assembleDebug :app:lintDebug --no-daemon` passed with all 50 JVM tests passing and debug lint reporting `No issues found.` Search/details supporting UI was extracted into focused component files using the Notis rule, preserving the existing layout code, state ownership, callbacks, inset handling and shared-transition parameters. The debug APK is built locally.

Search result count verification on 2026-10-27: `:app:testDebugUnitTest :app:assembleDebug :app:lintDebug --no-daemon` passed with all 52 JVM tests passing. Coverage verifies that the API total reaches paged results, stays with retained movies during query changes, switches with the successful response, clears with results, and remains independent of the number of appended movies. Preview fixtures include the count. The debug APK is built locally; this change has not been installed or visually checked on the Samsung.

## Decisions to resolve when implementing ads

The assignment does not define whether indexes 1 and 3 are final zero-based UI positions or positions relative to the movie-only list. It also does not define whether “second return” is counted per process, session, or install, or whether cold start counts. Clarify those points before implementing and testing ad placement/foreground policy. Configuration changes must not be mistaken for app returns.

No ad SDK is initialized by this scaffold. Choose a provider-neutral public API in `SDK` when the ad feature is implemented; keep AdMob types, IDs, load/show lifecycle and native-ad disposal inside that module. Use test ad IDs during development.

## Suggested implementation order

1. Review the implemented search/details UX on the Samsung in portrait and landscape, including back, keyboard, pagination, errors and save/remove.
2. Implement the favorites ticket list using the existing Room repository; decide whether additional metadata should be persisted with a schema migration.
3. Clarify ad behavior, implement SDK provider boundary and AdMob adapter.
4. Test search pagination, real favorites persistence, navigation restoration and ad policy; prepare the final submission only after review.
