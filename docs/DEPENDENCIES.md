# Dependency baseline

Stable releases checked on 2026-10-26 against Google Maven, Maven Central, the Gradle Plugin Portal and Gradle's current-release API. These are exact pinned versions, not moving ranges. Pre-release versions were excluded. Versions in Notis were used as a comparison, not assumed current.

| Component | Version |
| --- | --- |
| Gradle Wrapper | 9.8.2 |
| Android Gradle Plugin | 9.4.1 |
| Kotlin / Compose compiler / Serialization plugin | 2.4.21 |
| KSP | 2.3.12 |
| Compile / target Android API | 37 |
| Minimum Android API | 28 |
| Java source / bytecode target | 17 |
| Core KTX | 1.19.1 |
| Activity Compose | 1.13.0 |
| Lifecycle / ViewModel Navigation 3 | 2.11.0 |
| Compose BOM | 2026.09.00 |
| Navigation 3 | 1.2.0 |
| Room | 2.8.5 |
| Paging | 3.5.3 |
| Koin BOM | 4.2.2 |
| Koin compiler plugin | 1.2.1 |
| Retrofit / Kotlin Serialization converter | 3.0.0 |
| OkHttp | 5.5.1 |
| Kotlin Serialization runtime | 1.12.0 |
| Kotlin Coroutines | 1.11.0 |
| Kotlin Coroutines Test (local JVM tests only) | 1.11.0 |
| Coil / OkHttp fetcher | 3.6.3 |
| JUnit 4 (local JVM tests only) | 4.13.2 |

Compose UI and Material 3 versions are managed by the stable Compose BOM. Koin library versions are managed by its BOM. The build uses the Android Studio JBR locally; the bytecode target is intentionally independent of the installed JBR's version. Gradle pins its own distribution checksum. SDK Build Tools are selected by AGP rather than unnecessarily pinned in each module.

AGP 9.4 supports API 37, requires Gradle 9.6+ and JDK 17+. This project uses the newest stable Gradle 9.8.2. It uses AGP's built-in Kotlin and modern Android DSL. The root buildscript pins Kotlin Gradle Plugin 2.4.21 through its classpath, following the [documented higher-KGP override](https://developer.android.com/build/releases/agp-9-0-0-release-notes#kotlin-gradle-plugin). No external Kotlin Android plugin or legacy DSL opt-out is required.

The Koin compiler plugin 1.2.1 release verifies Kotlin through 2.4.20. Kotlin 2.4.21 is newer; retain it as requested and validate the actual compilation. Do not claim an upstream-verified combination unless Koin updates its compatibility list. Any actual build limitation belongs in the verification record below.

## Sources for future version checks

- [Gradle current stable release API](https://services.gradle.org/versions/current)
- [AGP metadata](https://dl.google.com/dl/android/maven2/com/android/tools/build/gradle/maven-metadata.xml) and [AGP 9.4 compatibility](https://developer.android.com/build/releases/agp-9-4-0-release-notes)
- [Kotlin plugin metadata](https://repo.maven.apache.org/maven2/org/jetbrains/kotlin/kotlin-gradle-plugin/maven-metadata.xml)
- [Compose BOM metadata](https://dl.google.com/dl/android/maven2/androidx/compose/compose-bom/maven-metadata.xml)
- [Room metadata](https://dl.google.com/dl/android/maven2/androidx/room/room-runtime/maven-metadata.xml)
- [Paging metadata](https://dl.google.com/dl/android/maven2/androidx/paging/paging-runtime/maven-metadata.xml)
- [Navigation 3 metadata](https://dl.google.com/dl/android/maven2/androidx/navigation3/navigation3-runtime/maven-metadata.xml)
- [Koin BOM metadata](https://repo.maven.apache.org/maven2/io/insert-koin/koin-bom/maven-metadata.xml) and [compiler plugin releases](https://github.com/InsertKoinIO/koin-compiler-plugin/releases)
- [KSP metadata](https://repo.maven.apache.org/maven2/com/google/devtools/ksp/symbol-processing-gradle-plugin/maven-metadata.xml)
- [Retrofit metadata](https://repo.maven.apache.org/maven2/com/squareup/retrofit2/retrofit/maven-metadata.xml)
- [OkHttp metadata](https://repo.maven.apache.org/maven2/com/squareup/okhttp3/okhttp/maven-metadata.xml)
- [Serialization metadata](https://repo.maven.apache.org/maven2/org/jetbrains/kotlinx/kotlinx-serialization-json/maven-metadata.xml)
- [Coroutines metadata](https://repo.maven.apache.org/maven2/org/jetbrains/kotlinx/kotlinx-coroutines-android/maven-metadata.xml)
- [Coil metadata](https://repo.maven.apache.org/maven2/io/coil-kt/coil3/coil-compose/maven-metadata.xml)
- [JUnit 4 latest release](https://github.com/junit-team/junit4/releases/latest): 4.13.2 checked on 2026-10-27. Uses the standard Android local-unit-test runner; no additional JUnit Platform plugin is required.

## Verification record

On 2026-10-27, search/details UI added explicit Compose Foundation and Animation dependencies managed by the existing stable Compose BOM; no versions changed. The implementation uses the installed Navigation 3 Scenes and shared-element APIs, following [Google's list-detail recipe](https://github.com/android/nav3-recipes/tree/main/app/src/main/java/com/example/nav3recipes/scenes/listdetail) and [shared-elements guidance](https://developer.android.com/develop/ui/compose/animation/shared-elements/navigation), without adding Material Adaptive pre-release artifacts. `:app:testDebugUnitTest :app:assembleDebug :app:lintDebug :app:assembleRelease --no-daemon` passed: all 47 JVM tests passed, debug lint reports `No issues found.`, and R8/resource shrinking completed successfully. Runtime Koin resolution includes the new details ViewModel. Known Koin/Kotlin compatibility and Gradle deprecation notices remain. An earlier APK was checked on the user's Samsung for live search, posters, details, saving and rotation; the user took over UX review before the final landscape/keyboard/inset refinements were installed. Android Studio CLI preview rendering returned an unknown error; the debug preview fixtures compile, but rendering success is not claimed. Changes remain uncommitted.

On 2026-10-26 the project compiled debug and release variants using AGP built-in Kotlin 2.4.21; the release passed R8 minification/resource shrinking. The Gradle Wrapper JAR SHA-256 matches Gradle's published checksum (`3b8a25775a69158b5ad2b1d17a80a88dc7b40a352a73eb27d06c612a7ce68e98`). Room exported the version 1 schema. All seven project skills passed the skill-creator validator.

The final build/lint command `:app:assembleDebug :app:assembleRelease :app:lintDebug :app:lintRelease --no-daemon` completed successfully. After the final manifest update, both lint analysis/report tasks were rerun explicitly to refresh stale incremental reports. Debug and release reports both state `No issues found.` No device behavior or completed assignment functionality is implied by successful compilation; there are no automated feature tests in this foundation.

Known toolchain notices: Koin compiler 1.2.1 warns that Kotlin 2.4.21 is newer than its verified 2.4.20 adapter, although both variants compile successfully. Upstream plugins still emit a Gradle deprecation summary. None of these notices is suppressed by the project. The Cyrillic-path override used in the original checkout was removed when the project moved to `C:\Users\jacks\AndroidStudioProjects\Moviet` on 2026-10-27.

On 2026-10-27, after moving the checkout to AndroidStudioProjects and removing the path-check override, `:app:assembleDebug :app:lintDebug --no-daemon` completed successfully from the new directory. The debug lint report states `No issues found.` Git remains uncommitted.

On 2026-10-27, after implementing the repositories, OMDb contract/mappers and separate API-key interceptor, `:app:testDebugUnitTest :app:assembleDebug :app:lintDebug :app:assembleRelease --no-daemon` completed successfully. All 17 JVM tests passed, debug lint reports `No issues found.`, and the release passed R8/resource shrinking. The runtime Koin test resolves all three repository interfaces and the Retrofit API, and checks that OkHttp uses the registered interceptor. Room schema version 1 is unchanged. The first combined run encountered an internal Kotlin/FIR lint crash while analyzing a test fixture; the final rerun after fixture cleanup passed with all lint checks enabled. Tests use local HTTP fixtures and a fake DAO; no live OMDb requests or device persistence checks were performed. The previously documented Koin compatibility warning and upstream Gradle deprecation notices remain. Git remains uncommitted.

On 2026-10-27, after implementing the MVI search ViewModel and presentation PagingSource, `:app:testDebugUnitTest :app:assembleDebug :app:lintDebug --no-daemon` completed successfully. All 31 JVM tests passed, including 14 search tests for debounce, cancellation, normalization, clearing, cache reuse, typed errors/retry, navigation events and paging boundaries/refresh. The Koin runtime test also resolves the repository-injected SearchViewModel. Debug lint reports `No issues found.` Coroutines Test reuses the existing stable Coroutines 1.11.0 catalog pin; its published Maven POM and resolved artifact were checked. No live API or device checks were performed. The existing Koin compatibility warning and Gradle deprecation notices remain. Changes remain uncommitted.

On 2026-10-27, after moving Pager/PagingSource ownership into the repository and simplifying the ViewModel to observe debounced queries, `:app:testDebugUnitTest :app:assembleDebug :app:lintDebug --no-daemon` completed successfully. All 33 JVM tests passed and debug lint reports `No issues found.` Coverage includes retaining results during typing, avoiding a reload when edits settle back to the active query, immediate clearing, cancellation when the debounced query changes, and the production repository's lazy paged flow. No dependencies changed. The existing Koin compatibility and Gradle deprecation notices remain. Changes remain uncommitted.
