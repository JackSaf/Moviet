---
name: moviet-room-paging
description: Implement or debug Room favorites persistence and Paging flows in Moviet, including DAO queries, schema migrations, query cancellation and Compose list states. Use when a task touches these data paths, not for unrelated UI changes.
---

# Room and Paging in Moviet

Read `docs/PROJECT.md` and the data/state sections of `docs/PROJECT_CONVENTIONS.md`. Inspect the version catalog, `core/database` and the feature being changed. The scaffold already exports a version 1 favorites schema and registers the database/DAO, but it has no favorite repository or search PagingSource yet.

Keep Room entities in `core/database`, map them at the feature data boundary, use IMDb IDs as identity, expose observable reads as cold Flow, and make writes suspend. A schema change requires a version decision, exported JSON and a migration that preserves favorites. Do not use destructive migration to make a test pass.

For OMDb paging, establish the actual page numbering, page size and error envelope from official API documentation. Handle HTTP 200 with `Response=False`, empty results and `N/A` values. Preserve cancellation and calculate refresh/end keys from the API contract. Use a query-driven `flatMapLatest` stream and `cachedIn(viewModelScope)`; keep debouncing, normalization and retry policies named and feature-owned. A network-only search does not require RemoteMediator merely because Room is installed.

In Compose use Paging load states, stable movie keys and retry callbacks. Keep movie rows and future ad slots as distinct presentation items; ad positions must not corrupt API page keys or movie counts. Resolve the ad-index ambiguity in the brief before implementing placements.

Use `android docs search` for current Room/Paging API guidance; official references include [Room](https://developer.android.com/training/data-storage/room), [migrations](https://developer.android.com/training/data-storage/room/migrating-db-versions), and [Paging](https://developer.android.com/topic/libraries/architecture/paging/v3-overview). Add meaningful tests for paging boundaries/cancellation or persistence/migration when implementing these behaviors. Run the affected checks and build with `--no-daemon`.
