# Moviet

An Android movie app built for the JedyApps home assignment. Search with OMDb, explore movie details, and save favorites locally.

## Features

- Movie search with pagination and error/retry states.
- Details with posters, plot, cast, and ratings.
- Persistent favorites with Undo on removal.
- Adaptive phone and two-pane layouts.
- Native and interstitial test ads in an independent `SDK` module.

## Run

Requires Android Studio compatible with AGP 9.4, Android SDK 37, and a device or emulator running Android 9+ (API 28).

1. Open the project in Android Studio and let Gradle sync.
2. Add your activated [OMDb API key](https://www.omdbapi.com/apikey.aspx) to the root `local.properties` file created by Android Studio:

   ```properties
   omdb.apiKey=YOUR_OMDB_API_KEY
   ```

3. Run `app`.

`local.properties` is Git-ignored. Rebuild after changing the key. Live search and details require internet access; saved favorites are stored locally. No AdMob credentials are needed.

## Implementation

Kotlin, Compose Material 3, Navigation 3, Room, Paging, Koin, Retrofit/OkHttp, Kotlin Serialization, and Coil.

The `app` module is organized by feature (`search`, `details`, `favorites`) with shared code in `core`. Screens use immutable state and ViewModel actions; repositories own data access. The independent `SDK` module exposes a provider-neutral advertising interface and owns the AdMob implementation.

## Advertising

Google demo ad IDs are included. Native ads occupy zero-based positions 1 and 3 when a search has at least three movies. An interstitial is attempted on the first return after launch, once per launch session; unavailable ads are skipped. Fully close and relaunch the app to repeat it.

Production ad IDs, consent handling, and release signing are not configured; release builds use debug signing.

To integrate another provider, implement `AdsProvider` in `SDK` and replace the `AdMobAdsService` binding with `single<AdsService> { ProviderAdsService(YourAdsProvider()) }`. The shared service owns the return policy, interstitial cache, expiry and stale-callback rejection. The adapter owns initialization, native rendering/cleanup and vendor-specific interstitial handles. Screens and ViewModels stay unchanged. This is source-level extensibility: a new vendor still needs its dependency, credentials, manifest configuration and integration testing. Remove AdMob's dependency and manifest metadata when removing that adapter.
