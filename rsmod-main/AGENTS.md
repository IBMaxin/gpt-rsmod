# AGENTS.md - RS Mod (rsmod-main)

## Build & Test Commands

| Command | Description |
|---|---|
| `./gradlew build` | Full build (compile + test + meta tests) |
| `./gradlew test` | Run unit tests only |
| `./gradlew konsistTest` | Run konsist meta tests (architecture linting) |
| `./gradlew docTest` | Run documentation tests |
| `./gradlew install` | Run server installation process |
| `./gradlew run` | Run the RS Mod game server |
| `./gradlew integration` | Run integration tests (nightly CI) |

## Task Order (CI workflow)

`konsistTest` and `docTest` run before `test`. The CI runs:
1. `./gradlew konsistTest --rerun-tasks`
2. `./gradlew docTest`
3. `./gradlew test`

## Requirements

- **Java 21** (Temurin Adoptium recommended)
- Gradle wrapper at `gradlew` (v8+)

## Key Project Structure

- `api` - API type system (varps, vars, types, editors)
- `content` - Game content (items, npcs, interfaces, skills, travel)
- `engine` - Core game engine (game logic, mapping, modules, objtx)
- `server` - Server entry point (app, install, services, shared)

## Custom Gradle Tasks

- `install` - Runs `GameServerInstallKt` (installation process)
- `cleanInstall` - Runs `GameServerCleanInstallKt` (clean previous installs)
- `downloadCache` - Runs `GameServerCacheDownloaderKt`
- `packCache` - Runs `GameServerCachePackerKt`
- `generateRsa` - Runs `GameNetworkRsaGeneratorKt`
- `setupLogbackNovice` / `setupLogbackAdvanced` - Copy logback configs

## CI Notes

- Meta tests (konsistTest/docTest) are combined into one step in core-ci.yml
- Test summaries are uploaded as GitHub artifacts
- Nightly CI runs integration tests via `./gradlew integration