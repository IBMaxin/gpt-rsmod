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
- Nightly CI runs integration tests via `./gradlew integration`

## Content Development

### Module Pattern

Each skill follows this structure:
```
content/skills/{name}/
├── build.gradle.kts
├── PLAN.md
├── src/main/kotlin/.../
│   ├── {Name}Module.kt
│   ├── {Name}LevelBoosts.kt
│   ├── scripts/{Name}.kt
│   └── configs/
│       ├── {Name}ObjRefs.kt
│       └── {Name}Params.kt
├── src/integration/kotlin/.../
│   ├── configs/{Name}ConfigTest.kt
│   └── scripts/{Name}Test.kt
```

### Key Patterns

| Pattern | How | Example |
|---------|-----|---------|
| Params | Server-only in module's Params.kt | `val levelrequire = params.levelrequire` |
| LOC editors | `LocEditor` subclass | `edit(type) { contentGroup = content.tree }` |
| NPC editors | `NpcEditor` subclass | `edit(type) { param[params.levelrequire] = 1 }` |
| LOC param access | `locParam()` extension | `val treeLevelReq: Int by locParam(params.levelrequire)` |
| NPC param access | Direct `type.param()` | `val levelReq = type.param(ThievingParams.levelrequire)` |
| Item refs | Per-module `find()` | `find("raw_shrimps")` |
| Success rolls | `statRandom(stats.skill, low, high, invisibleLvls)` | OSRS formula: `(level + low) / 256` |
| XP | `statAdvance(stats.skill, xp)` | XP in fine units (×10) |
| Module | `PluginModule` + `InvisibleLevelMod` | Registers invisible level boosts |

### Phase 1 Skills (In Progress)

| Skill | Status | Plan |
|-------|--------|------|
| Cooking | Planning | `content/skills/cooking/PLAN.md` |
| Firemaking | Planning | `content/skills/firemaking/PLAN.md` |
| Fishing | Planning | `content/skills/fishing/PLAN.md` |
| Fletching | Planning | `content/skills/fletching/PLAN.md` |

### TDD Workflow

1. **Red Phase** — Write `ConfigTest` + `ScriptTest` first
2. **Green Phase** — Implement to pass tests
3. **Refactor Phase** — Clean up code, run `./gradlew test`

### Test Commands

```bash
# Run all tests for a skill
./gradlew :content:skills:{name}:test

# Run integration tests
./gradlew :content:skills:{name}:integration

# Run all skill tests
./gradlew :content:skills:cooking:test :content:skills:firemaking:test :content:skills:fishing:test :content:skills:fletching:test
```

### Reference Modules

- `content/skills/thieving/` — Pickpocketing pattern (NPC-based)
- `content/skills/woodcutting/` — Gathering pattern (LOC-based with timers)