---
name: moviet-project-conventions
description: Apply Moviet project context and conventions when starting or changing its Android features, Compose screens, state flows, or package structure. Use for project work in Moviet rather than unrelated Android repositories.
---

# Moviet project work

Read the repository's `AGENTS.md`, `docs/PROJECT.md` and `docs/PROJECT_CONVENTIONS.md` before changing feature behavior. These files own scope, implementation status, package boundaries, and Notis-derived conventions. Do not assume the home assignment is already implemented.

Find the relevant feature and inspect its DI and UI root before editing. Follow `*ScreenRoot`/`*Screen`, modifier-first, resource/UiText and feature-package conventions. For initial data loading, use the state-lifetime section of the conventions: no startup work in ViewModel `init`, cold flows where possible, `onStart` with a deliberate refresh policy, `WhileSubscribed` and no duplicate collectors. Query changes cancel superseded work.

Use the installed official Navigation 3 or edge-to-edge skill when the task needs those workflows. Use the Room/Paging or Koin project skill for their concrete integration work. Consult `docs/DEPENDENCIES.md` when touching build configuration; preserve the newest stable requirement and validate compatibility.

Verify the affected behavior and run applicable build/lint checks with `--no-daemon`. Keep the current-status section of the project brief accurate. Respect the user's uncommitted review workflow.
