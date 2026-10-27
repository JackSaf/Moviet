# Project skills

Project-local skills live in `.agents/skills` and are available to agents working from this repository on subsequent turns. `AGENTS.md` supplies the initial context and routes to the brief/conventions even before a specialized skill is selected. Do not depend on a previous chat or on access to the Notis checkout.

## Official Android skills

Installed selectively from [android/skills](https://github.com/android/skills), pinned to commit `4ebabf2b0fd6c04b7150023c8b612f80592a6226` on 2026-10-26, using the skill-installer helper. These are upstream copies with their references retained. Their license is Apache 2.0; see `.agents/skills/LICENSE.android-skills.txt` and each installed skill's `LICENSE.txt`.

| Skill | Upstream path | When to use |
| --- | --- | --- |
| android-cli | devtools/android-cli | Android CLI, official documentation lookup, devices, SDK tools, Studio integration |
| navigation-3 | navigation/navigation-3 | Routes, back stacks, entry lifecycle, Navigation 3 integration |
| edge-to-edge | system/edge-to-edge | System bars, insets, keyboard, edge-to-edge Compose layouts |
| jetpack-compose-performance | jetpack-compose/jetpack-compose-performance | Investigating a demonstrated Compose rendering/performance issue |

The Navigation 3 upstream skill also includes alternative Hilt/migration recipes. This project uses Koin and starts with Navigation 3, so load the relevant Koin/basic/saveable recipes only. Installing a skill does not authorize adding every dependency mentioned in its references. The Compose performance skill is for targeted diagnosis; it is not an automatic repository-wide audit on ordinary UI tasks.

The upstream catalog has no dedicated Room, Paging, Koin or general Compose coding skill as of the installation date. Compose migration, experimental Styles, TV/Wear/XR, camera, billing, identity and other unrelated skills were not installed. Do not present local instructions as official Android skills.

## Moviet-authored skills

| Skill | When to use |
| --- | --- |
| moviet-project-conventions | Starting or changing Moviet features: context, package ownership, initial data loading, Compose/state conventions |
| moviet-room-paging | Local favorites persistence, Room schemas/migrations, query-driven Paging and list integration |
| moviet-koin | Koin startup/modules, constructor registration, Navigation-entry ViewModels, DI graph/build failures |

These skills link to shared project conventions instead of duplicating them. Read references only when relevant to the actual task. A skill should not expand a requested base setup into a full implementation or publication.

## Updating

Select upstream updates by explicit path and revision; inspect differences before replacing a local copy. Update the pinned revision and date here, preserve licenses, and validate skills using the available skill-creator validator. Do not run an install-all command.
