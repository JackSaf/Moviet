---
name: moviet-koin
description: Configure or debug Moviet dependency injection using its Koin compiler DSL, feature modules, application startup and Navigation 3 ViewModel scopes. Use for DI graph or compiler-plugin changes, not for unrelated feature logic.
---

# Koin in Moviet

Read `docs/PROJECT_CONVENTIONS.md` and `docs/DEPENDENCIES.md`; inspect `MovietApplication`, `app/di/AppModule.kt`, the feature DI file and the version catalog.

Start Koin once in the Application with `androidContext`; compose feature modules through the app module. Prefer constructor injection. Register constructor-resolved types with the installed compiler DSL, e.g. `org.koin.plugin.module.dsl.viewModel`, and use provider lambdas for Room/Retrofit builders. Do not confuse compiler DSL imports with older runtime `singleOf`/`viewModelOf` examples. Explicitly bind an implementation to its interface when consumers request that interface.

Resolve screen ViewModels through `koinViewModel()` within Navigation 3 entries. Preserve saveable-state and ViewModel-store decorators so popping an entry clears its ViewModel. Keep Context in platform factories, not screen ViewModels. SDK must remain independent of app types.

Koin's compiler plugin is separate from Room's KSP processor. Do not add Koin KSP/annotations processing to solve compiler DSL errors. Check exact Kotlin/plugin compatibility before changing versions; compile the full app graph rather than disabling validation to hide a missing binding. Use [official compiler-plugin documentation](https://github.com/InsertKoinIO/koin-compiler-plugin) and the matching release notes for installed versions.

Run the affected Gradle tasks with `--no-daemon`. A successful compile does not prove that every lazy platform factory runs: when adding a binding, check its lifetime, qualifier and constructor path and test runtime resolution when the change warrants it.
