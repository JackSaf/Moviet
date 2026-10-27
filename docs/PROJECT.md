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

## Implemented foundation

- Local checkout moved to `C:\Users\jacks\AndroidStudioProjects\Moviet` on 2026-10-27; the Cyrillic-path Gradle workaround was removed.
- Android application `:app` and independent Android library `:SDK`, with the app depending on the library.
- Application ID and namespace `com.jacks.moviet`, minimum API 28, compile/target API 37.
- Version catalog and Gradle Wrapper with a distribution SHA-256 checksum; newest stable versions checked on 2026-10-26.
- Compose light/dark theme and resource-based text; a minimal home screen and favorites placeholder with a working Navigation 3 back stack.
- Koin application startup, feature modules, and an injected search ViewModel. Navigation entries own their ViewModels.
- Room database version 1, favorites entity/DAO, schema export, and lazy DI registration. This plumbing is not yet connected to the favorites screen.
- Retrofit, Kotlin Serialization, OMDb-scoped OkHttp configuration, and BuildConfig fields for the base URL and externally supplied API key without committing a credential. No API service or requests yet.
- Paging and Coil dependencies are connected; their feature integrations are still pending.
- Shared `UiText`, lifecycle-aware event observation, spacing tokens, and state sharing timeout policy.
- Feature package skeletons, project-local skills, project conventions, and Git hygiene.

## Not implemented yet

- Search input, query debounce/cancellation, OMDb DTOs and service, repository, paginated results, errors/retry and loading states.
- Details screen and poster rendering.
- Favorites domain/repository mapping, list observation in UI, and add/remove actions.
- Advertising interfaces, AdMob dependency/configuration, native ad binding/disposal, foreground counting, and interstitial lifecycle.
- Automated behavior tests, CI, release signing, distribution, and publication.

## Decisions to resolve when implementing ads

The assignment does not define whether indexes 1 and 3 are final zero-based UI positions or positions relative to the movie-only list. It also does not define whether “second return” is counted per process, session, or install, or whether cold start counts. Clarify those points before implementing and testing ad placement/foreground policy. Configuration changes must not be mistaken for app returns.

No ad SDK is initialized by this scaffold. Choose a provider-neutral public API in `SDK` when the ad feature is implemented; keep AdMob types, IDs, load/show lifecycle and native-ad disposal inside that module. Use test ad IDs during development.

## Suggested implementation order

1. OMDb API contract, typed results/errors, and movie domain model.
2. Search repository, PagingSource, query-driven ViewModel and list UI.
3. Details and poster loading, then favorites repository and Room-backed UI.
4. Clarify ad behavior, implement SDK provider boundary and AdMob adapter.
5. Test search pagination/cancellation, favorites persistence, navigation restoration and ad policy; prepare the final submission only after review.
