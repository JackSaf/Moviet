# Moviet

Android movie app for the JedyApps home assignment. Search OMDb, browse movie details, and save favorites locally with Room.

Implemented: paged keyword search, movie details, persistent favorites, loading/error/retry states, adaptive list-detail navigation, and AdMob test ads through the independent `SDK` module. Search and Favorites retain their own selected movie and navigation state when switching tabs.

## Run

1. Open the repository in Android Studio and install Android SDK Platform 37. The minimum device/emulator version is API 28.
2. Use Android Studio's bundled JDK for Gradle. The checked-in Gradle JVM configuration selects JDK 25 and includes toolchain download URLs.
3. Copy `secrets.properties.example` to `secrets.properties` and set your OMDb API key:

   ```properties
   omdb.apiKey=YOUR_OMDB_API_KEY
   ```

4. Sync Gradle and run the `app` configuration.

The key can also be supplied through the `OMDB_API_KEY` environment variable or the `omdb.apiKey` Gradle property. Priority: Gradle property, environment variable, then `secrets.properties`. The local secrets file is ignored by Git. Builds and fixture-based unit tests work without a key; live searches and details require one. The key is embedded in the APK through BuildConfig.

AdMob currently uses Google's sample app ID and demo interstitial/native ad units, so it does not serve production ads. Before publishing, replace those IDs in `SDK` with registered IDs and add the appropriate consent flow before SDK initialization. The app calls the provider-neutral `AdsService` interface; the `SDK` module owns AdMob initialization, loading, display, and native-ad disposal. When a search has at least three movies, native slots occupy zero-based visible indexes 1 and 3. The interstitial is attempted once on the second foreground return after the initial launch, across process restarts; rotation does not count. If it has not loaded by that return, it is skipped rather than interrupting the user later.

## Build and test

Windows PowerShell:

```powershell
.\gradlew.bat :SDK:testDebugUnitTest :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --no-daemon
```

Linux/macOS:

```sh
sh ./gradlew :SDK:testDebugUnitTest :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --no-daemon
```

The Gradle Wrapper is included. Debug APK output: `app/build/outputs/apk/debug/app-debug.apk`.

JVM tests cover API mapping and failures, paging, ViewModel state and cancellation, favorites repository behavior, dependency injection, navigation, ad-slot mapping, and the second-return trigger. Real-device Room persistence and live ad rendering are not covered by these tests.

## Structure

- `app`: application setup/navigation, shared `core` code, and `search`, `details`, `favorites` feature packages with data, domain, and presentation layers.
- `SDK`: independent Android library with the provider-neutral ad interface and AdMob implementation; no dependency on `app`.
- `app/schemas`: versioned Room database schemas.

Built with Kotlin, Compose Material 3, Navigation 3, Paging, Room, Koin, Retrofit/OkHttp, Kotlin Serialization, and Coil. Dependency versions are pinned in `gradle/libs.versions.toml`.
