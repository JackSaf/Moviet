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

Additional authorized work: implement the app theme from the supplied cinema/carousel design. This covers shared visual tokens and platform appearance; the carousel and search UI remain future feature work.

Additional authorized work: implement the repository/data layer, database and domain movie models, mappers, database/DAO, OMDb API interface, and Koin dependency injection. Keep the API-key interceptor in a separate class under `core/network/utils`.

## Implemented foundation

- Local checkout moved to `C:\Users\jacks\AndroidStudioProjects\Moviet` on 2026-10-27; the Cyrillic-path Gradle workaround was removed.
- Android application `:app` and independent Android library `:SDK`, with the app depending on the library.
- Application ID and namespace `com.jacks.moviet`, minimum API 28, compile/target API 37.
- Version catalog and Gradle Wrapper with a distribution SHA-256 checksum; newest stable versions checked on 2026-10-26.
- Design-based dark Compose theme: amber accents, charcoal surfaces, off-white text, sans-serif typography, rounded shapes and spacing tokens. The theme stays dark in both device appearance modes; platform launch colors and system bar icons match. Debug-only component previews cover light/dark system settings, narrow width and large text. See `docs/DESIGN_SYSTEM.md`.
- Resource-based text; a minimal home screen and favorites placeholder with a working Navigation 3 back stack.
- Koin application startup, feature modules, and an injected search ViewModel. Navigation entries own their ViewModels.
- Shared domain `Movie`/`MovieType` models, OMDb DTOs, and API/database mappers. Missing posters (`N/A`) become null; unrecognized movie types become `UNKNOWN`. Missing movie identity/title is an invalid API response.
- Room database version 1, favorites entity/DAO, schema export, and lazy DI registration. `FavoritesRepository` maps entities to domain movies, observes favorites and per-movie favorite status, and adds/removes favorites with suspend writes. The existing schema is preserved; the favorites screen is not connected yet.
- Retrofit OMDb API interface for keyword search and details by IMDb ID. `SearchRepository` fetches one page and exposes the total result count; `DetailsRepository` fetches a domain movie. Typed results distinguish HTTP errors, OMDb `Response=False`, connection failures, invalid requests, and malformed content. Cancellation propagates.
- Kotlin Serialization, OMDb-scoped OkHttp configuration, a distinct `OmdbApiKeyInterceptor` in `core/network/utils`, and BuildConfig fields for the base URL and externally supplied API key without committing a credential. Koin registers the API/interceptor and explicitly binds each feature repository interface to its implementation.
- Paging and Coil dependencies are connected; their feature integrations are still pending.
- Shared `UiText`, lifecycle-aware event observation, spacing tokens, and state sharing timeout policy.
- Feature package skeletons, project-local skills, project conventions, and Git hygiene.

## Not implemented yet

- Search input, query debounce/cancellation, PagingSource and paginated UI results, errors/retry and loading states. Repository requests exist; no screen starts them yet.
- Details screen and poster rendering.
- Favorites list observation in UI and add/remove UI actions.
- Advertising interfaces, AdMob dependency/configuration, native ad binding/disposal, foreground counting, and interstitial lifecycle.
- Device persistence tests, CI, release signing, distribution, and publication.

## Data layer boundaries

Each feature owns its repository contract in `domain`, implementation in `data`, and Koin binding in `di`. Shared movie types and network outcomes live in `core/domain`; shared API DTOs, API interface and response handling live in `core/network/data`. Room entities remain in `core/database`. Repositories do not start background work during construction, and screens continue to use their existing scaffold state.

The [official OMDb contract](https://www.omdbapi.com/) defines `s` for search, `i` for IMDb lookup, and one-based `page` values from 1 to 100. Search validates those bounds and returns one page; PagingSource/end-of-pagination policy is future work. No API key is needed for the local fixture tests.

JVM tests cover Retrofit query encoding/JSON mapping, unavailable values, invalid payloads, HTTP versus OMDb failures, cancellation, repository favorite observation/writes with a fake DAO, interceptor authentication, and runtime Koin repository/API resolution. They do not verify real Room persistence or live OMDb access.

## Decisions to resolve when implementing ads

The assignment does not define whether indexes 1 and 3 are final zero-based UI positions or positions relative to the movie-only list. It also does not define whether “second return” is counted per process, session, or install, or whether cold start counts. Clarify those points before implementing and testing ad placement/foreground policy. Configuration changes must not be mistaken for app returns.

No ad SDK is initialized by this scaffold. Choose a provider-neutral public API in `SDK` when the ad feature is implemented; keep AdMob types, IDs, load/show lifecycle and native-ad disposal inside that module. Use test ad IDs during development.

## Suggested implementation order

1. Search PagingSource, query-driven ViewModel and list UI using the implemented repository.
2. Details and poster loading, then favorites UI using the implemented repositories.
3. Clarify ad behavior, implement SDK provider boundary and AdMob adapter.
4. Test search pagination, real favorites persistence, navigation restoration and ad policy; prepare the final submission only after review.
