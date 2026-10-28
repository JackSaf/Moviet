# Moviet

A Kotlin Android app for discovering movies and series with [OMDb](https://www.omdbapi.com/), exploring their details, and keeping a local collection of favorites.

## Features

- Keyword search with pagination, result counts, and loading, empty, error, and retry states.
- Movie details with posters, plot, cast, ratings, and available metadata.
- Favorites stored locally with Room, including removal with Undo.
- Adaptive phone and two-pane layouts, shared poster transitions, and independent Search and Favorites navigation stacks.
- Native and interstitial AdMob test ads through an independent advertising library.

## Setup

### Prerequisites

- Android Studio with support for Android Gradle Plugin 9.4.
- Android SDK Platform 37, installed through **Tools → SDK Manager**.
- An emulator or Android device running Android 9 (API 28) or newer.
- Internet access for dependency downloads, movie searches, details, posters, and test ads.

Use Android Studio's bundled JDK to start Gradle. The checked-in Gradle JVM configuration selects JDK 25 and includes toolchain download URLs; the first sync may download that JDK. Gradle itself is supplied by the Wrapper, so a separate Gradle installation is unnecessary.

### Configure the OMDb API key

1. Request a key from the [OMDb API key page](https://www.omdbapi.com/apikey.aspx). The free account currently allows 1,000 requests per day.
2. Check the email from OMDb and follow its activation instructions before using the key.
3. In the repository root, beside `settings.gradle.kts`, copy `secrets.properties.example` to a new file named `secrets.properties`.

   Windows PowerShell:

   ```powershell
   Copy-Item secrets.properties.example secrets.properties
   ```

   macOS/Linux:

   ```sh
   cp secrets.properties.example secrets.properties
   ```

4. Open `secrets.properties` and paste only the key after `omdb.apiKey=`, without quotes:

   ```properties
   omdb.apiKey=YOUR_OMDB_API_KEY
   ```

5. Sync Gradle, then build and run the app. Rebuild and reinstall whenever you change the key: it is read at build time.

`secrets.properties` is ignored by Git. Keep the real key out of `secrets.properties.example` and source files. The key is embedded in the APK through `BuildConfig`; it is not a server-side secret.

For CI or terminal builds, the key can also come from the `OMDB_API_KEY` environment variable or the `omdb.apiKey` Gradle property. Resolution order is Gradle property → environment variable → `secrets.properties`. An existing property or environment variable therefore overrides the file.

### Run the app

1. Open the repository root in Android Studio and let Gradle sync finish.
2. Start an emulator from **Device Manager**, or connect a device with USB debugging enabled.
3. Select the **app** run configuration and the target device, then click **Run**.
4. Search for a title such as `Batman`, open a result, and tap **Save ticket** to add it to Favorites.

No backend, account sign-in, or AdMob credentials are needed. Builds and automated tests work without an OMDb key; live search and details require an activated key. Saved favorites remain available locally, while opening their full details requires a network request.

### Troubleshooting

- **Search or details fail:** check the key and its activation, the device's internet connection, and the OMDb daily request limit. Confirm that `secrets.properties` is at the repository root, check for overriding environment/Gradle properties, and rebuild after editing it.
- **SDK Platform 37 is missing:** install it in Android Studio's SDK Manager and sync again. Android Studio creates the machine-specific `local.properties` file for the SDK path.
- **Gradle cannot find Java in a terminal:** set `JAVA_HOME` to your Android Studio installation's `jbr` directory. In the IDE, check **Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK**.

## Build and verification

From the repository root, run the JVM tests, lint for both modules, and a debug build:

```powershell
.\gradlew.bat :SDK:testDebugUnitTest :app:testDebugUnitTest :SDK:lintDebug :app:lintDebug :app:assembleDebug --no-daemon
```

On macOS/Linux, replace `.\gradlew.bat` with `./gradlew` in these commands. Terminal builds require `JAVA_HOME` and an Android SDK path, supplied by Android Studio's generated `local.properties` or your SDK environment configuration.

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

Build a release APK with R8 and resource shrinking:

```powershell
.\gradlew.bat :app:assembleRelease --no-daemon
```

The release APK is written to `app/build/outputs/apk/release/app-release.apk`. Release builds currently use the local debug signing key for device testing. Distribution requires a dedicated signing configuration.

JVM tests cover API mapping and failures, credential scoping, paging, ViewModel state and cancellation, favorites behavior, navigation policies, dependency injection, and ad foreground policies. Android integration tests cover Room migration/persistence and Compose navigation state restoration, using an isolated test Application.

Build the Android test APK without a device:

```powershell
.\gradlew.bat :app:assembleDebugAndroidTest --no-daemon
```

With a single emulator or device connected, execute the Android tests:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest --no-daemon
```

The integration suite has been compiled but has not yet been executed on a device. Live ad callbacks and system navigation gestures also require device verification.

## Advertising

The project uses [Google's demo ad IDs](https://developers.google.com/admob/android/test-ads), so no AdMob account setup is needed to run it.

- For searches with at least three movies, native ads appear at zero-based positions 1 and 3 among the movie/ad rows, below the result heading.
- An interstitial is attempted once per launch session: launch, leave the app, then return once. Further returns in that session show no interstitial. Closing the app's last Activity or starting a new process resets the in-memory count; rotation does not. Simply backgrounding and reopening the app continues the current session.
- The SDK preloads one interstitial during the initial visit. If it is unavailable or expired at the first return, that session's opportunity is skipped. There are no delayed display timers or saved ad counters. To repeat the scenario, fully close and relaunch the app; saved favorites are retained.

Before distributing an app with production ads, replace the sample application ID in `SDK/src/main/AndroidManifest.xml` and the test unit IDs in `AdMobInterstitialController.kt` and `AdMobNativeAdStore.kt`, and implement the appropriate consent flow before ad initialization.

## Project structure

| Location | Responsibility |
| --- | --- |
| `app/.../app` | Application startup, dependency injection, and Navigation 3 host |
| `app/.../core` | Shared models, networking, database, design system, and presentation utilities |
| `app/.../search`, `details`, `favorites` | Feature data, domain, and presentation code |
| `SDK` | Provider-neutral `AdsService` interface and AdMob implementation; independent of `app` |
| `app/schemas` | Versioned Room database schemas |
| `gradle/libs.versions.toml` | Pinned dependency and plugin versions |

Compose screens receive immutable state and dispatch actions to entry-scoped ViewModels. Repositories own data access; API DTOs and Room entities are mapped to domain models. Room is the source of truth for favorites, and Paging owns search pagination. Koin wires dependencies, while advertising implementation details stay inside `SDK`.

Built with Kotlin, Compose Material 3, Navigation 3, Room, Paging, Koin, Retrofit/OkHttp, Kotlin Serialization, and Coil.
