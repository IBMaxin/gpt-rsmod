# AGENTS.md - RS Mod (rsmod-main)

Agent instructions and technical reference for the RSMod codebase.

## Quick Start

- **Root**: `C:\Users\bob\Desktop\gpt-rsmod`
- **Main codebase**: `rsmod-main/` (Kotlin, Gradle, Java 21)
- **Companion tool**: `rsprox-master/`
- **Java**: `C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot`
- **Server port**: 43594, dev realm (no password, auto display names, 150x XP)

Run gradle from `rsmod-main/`:
```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
.\gradlew.bat <task>
```

## Requirements

- **Java 21** (Temurin Adoptium recommended)
- Gradle wrapper at `gradlew` (v8+)

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
| `./gradlew :content:skills:{name}:test` | Run unit tests for a specific skill |
| `./gradlew :content:skills:{name}:integration` | Run integration tests for a skill |
| `./gradlew :content:skills:{name}:compileKotlin` | Compile main sources only |

### CI Workflow Order

`konsistTest` and `docTest` run before `test`:
1. `./gradlew konsistTest --rerun-tasks`
2. `./gradlew docTest`
3. `./gradlew test`

### Custom Gradle Tasks

| Task | Description |
|---|---|
| `install` | Runs `GameServerInstallKt` |
| `cleanInstall` | Runs `GameServerCleanInstallKt` |
| `downloadCache` | Runs `GameServerCacheDownloaderKt` |
| `packCache` | Runs `GameServerCachePackerKt` |
| `generateRsa` | Runs `GameNetworkRsaGeneratorKt` |
| `setupLogbackNovice` / `setupLogbackAdvanced` | Copy logback configs |

### CI Notes

- Meta tests (konsistTest/docTest) combined in core-ci.yml
- Test summaries uploaded as GitHub artifacts
- Nightly CI runs `./gradlew integration`

## Project Structure

```
rsmod-main/
├── api/                    # API type system, testing, player, invtx, script
│   ├── invtx/              # Inventory transactions (invAdd, invDel, invSwap)
│   ├── player/             # Player, ProtectedAccess, stat, events/interact
│   ├── script/             # onOpHeldU, onOpNpc, onOpLoc, onProtectedEvent
│   ├── testing/            # GameTestScope, GameTestState, test infrastructure
│   └── config/refs/        # Shared refs: objs, stats, invs, components
├── content/
│   ├── skills/             # Skill implementations
│   ├── interfaces/         # Interface scripts (bank, settings, etc.)
│   └── areas/              # Area-specific content (cities, NPCs)
├── engine/
│   ├── events/             # EventBus (publish/subscribe)
│   ├── game/               # Core: Player, Npc, Inventory, ObjType, StatType
│   └── objtx/              # Transaction engine
└── .data/symbols/          # Cache symbol files (obj.sym, loc.sym, npc.sym)
```

## Content Development

### File Conventions

| File | Purpose |
|------|---------|
| `{Skill}ObjRefs.kt` | Per-module item refs via `find()` |
| `{Skill}Params.kt` | Server-only param definitions |
| `{Skill}Module.kt` | `PluginModule` + `InvisibleLevelMod` |
| `{Skill}LevelBoosts.kt` | Invisible level boosts (cape, diary) |
| `{Skill}Script.kt` / `{Skill}.kt` | `PluginScript` with `startup()` |
| `{Skill}ConfigTest.kt` | Verify all refs resolve against cache |
| `{Skill}Test.kt` / `{Skill}ScriptTest.kt` | Integration tests |

### Module Pattern

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
└── src/integration/kotlin/.../
    ├── configs/{Name}ConfigTest.kt
    └── scripts/{Name}Test.kt
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
| Editor merge test | `NpcPluginBuilder` + `NpcTypeBuilder.merge` | Replicate editor logic, merge with cache type, assert paramMap |
| Module | `PluginModule` + `InvisibleLevelMod` | Registers invisible level boosts |

### Phase 1 Skills

| Skill | Status | Plan |
|-------|--------|------|
| Cooking | Planning | `content/skills/cooking/PLAN.md` |
| Firemaking | Planning | `content/skills/firemaking/PLAN.md` |
| Fishing | Planning | `content/skills/fishing/PLAN.md` |
| Fletching | ✅ Complete | `content/skills/fletching/` |
| Slayer | In Progress | `content/skills/slayer/` |

### TDD Workflow

1. **Red Phase** — Write `ConfigTest` + `ScriptTest` first
2. **Green Phase** — Implement to pass tests
3. **Refactor Phase** — Clean up code, run `./gradlew test`

### Reference Modules

- `content/skills/thieving/` — Pickpocketing pattern (NPC-based)
- `content/skills/woodcutting/` — Gathering pattern (LOC-based with timers)
- `content/skills/slayer/` — NPC editor merge pattern (SlayerConfigTest)

---

## Technical Reference

### ObjType System

- `ObjTypeList` — injected via Guice, provides `objTypes[hashedObjType]` → `UnpackedObjType`
- `HashedObjType` — lazy `internalId` computed from hash; used in `find()` calls
- `UnpackedObjType` — loaded from cache; `internalId` set directly
- Both share `ObjType.id` which returns `internalId`
- **NEVER assume item names** — always verify against `.data/symbols/obj.sym`
- `find("item_name")` returns `HashedObjType`

### Inventory Operations

```kotlin
invAdd(inv, type, count)              // Add items
invDel(inv, type, count)              // Delete items
invSwap(inv, fromSlot, intoSlot)      // Swap slots
invTransaction(inv) { ... }           // Raw transaction
```

### Script Registration

```kotlin
// Item-on-item
onOpHeldU(knifeRef, logsRef) { event ->
    // 'this' is ProtectedAccess, event.first/second are UnpackedObjType
}
// Loc interactions
onOpLoc1(treeType) { /* chop */ }
onOpLoc2(treeType) { /* ... */ }
// NPC interactions
onOpNpc3(npcType) { /* pickpocket */ }
```

### ProtectedAccess

- Wraps `Player` + `GameCoroutine` + `ProtectedAccessContext`
- Provides: `inv`, `worn`, `bank`, `stat()`, `statAdvance()`, `mes()`, `invDel()`, `invAdd()`
- `actionDelay = N` — delays next player action (non-blocking)
- `mes("text")` — **suspends** the coroutine until game cycle processes it
- `invDel`/`invAdd` — **synchronous**, execute immediately within coroutine

### Stats & XP

```kotlin
stat(stats.fletching)                         // Get level (with invisible boosts)
statAdvance(stats.fletching, xpAmount)        // Add XP (respects xpRate)
statRandom(stats.fletching, low, high, invisibleLvls)  // Success roll
```

### Events

```kotlin
// Publishing (used in tests)
eventBus.publish(protectedAccess, heldUEvent)
// Handler registration
eventBus.subscribeSuspend(HeldUEvents.Type::class.java, key, handler)
```

### How to Reference Types

```kotlin
objs.*        // Item types (BaseObjs.kt)
seqs.*        // Animation types (BaseSeqs.kt)
spotanims.*   // Graphic types (BaseSpotanims.kt)
synths.*      // Sound types (BaseSynths.kt)
stats.*       // Stat types (BaseStats.kt)
timers.*      // Timer types (BaseTimers.kt)
invs.*        // Inventory types (BaseInvs.kt)
components.*  // Interface components (BaseComponents.kt)
```

**NEVER invent IDs. Always use `find("name")` references.**

---

## Elvarg → RSMod API Mapping

**CRITICAL: Use ONLY the RSMod API names below. Elvarg names do not exist in RSMod.**

### Type References

| Elvarg (WRONG) | RSMod (CORRECT) | Example |
|----------------|-----------------|---------|
| `Item(id)` | `objs.*` | `objs.dragon_longsword` |
| `Animation(id)` | `seqs.*` | `seqs.human_unarmedpunch` |
| `Graphic(id)` | `spotanims.*` | `spotanims.smokepuff` |
| `Sound.ID` | `synths.*` | `synths.human_unarmedpunch` |
| `TimerKey.FOOD` | `timers.*` | `timers.stat_regen` |

### Entity Methods

| Elvarg (WRONG) | RSMod (CORRECT) |
|----------------|-----------------|
| `player.performAnimation(anim)` | `anim(seqs.name)` |
| `player.performGraphic(gfx)` | `spotanim(spotanims.name, height = 96)` |
| `player.sendMessage("text")` | `mes("text")` (suspends) |
| `player.sendString(id, text)` | `ifSetText(component, text)` |
| `player.sendInterfaceRemoval()` | `ifClose()` |
| `SoundManager.sendSound(player, sound)` | `soundSynth(synths.name)` |

### Inventory

| Elvarg (WRONG) | RSMod (CORRECT) |
|----------------|-----------------|
| `player.getInventory().add(item, slot)` | `invAdd(inv, objs.name, count)` |
| `player.getInventory().delete(item, slot)` | `invDel(inv, objs.name, count)` |
| `player.getInventory().contains(id)` | `invTotal(inv, objs.name) > 0` |

### Stats & Skills

| Elvarg (WRONG) | RSMod (CORRECT) |
|----------------|-----------------|
| `getCurrentLevel(Skill.X)` | `stat(stats.x)` |
| `getMaxLevel(Skill.X)` | `statBase(stats.x)` |
| `addExperience(Skill.X, xp)` | `statAdvance(stats.x, xp)` |

### Combat

| Elvarg (WRONG) | RSMod (CORRECT) |
|----------------|-----------------|
| `extends MeleeCombatMethod` | `SpecialAttackMap` + `MeleeSpecialAttack` |
| `PendingHit` | `Hit` / `queueHit()` |
| `CombatSpecial.drain(char, amount)` | Handled by SpecialAttackManager |

### Timers

| Elvarg (WRONG) | RSMod (CORRECT) |
|----------------|-----------------|
| `register(key, ticks)` | `timer(timers.name, cycles)` |
| `extendOrRegister(key, ticks)` | `softTimer(timers.name, cycles)` |
| Attack delay | `actionDelay = mapClock + cycles` |

---

## Integration Test Patterns

### Basic Pattern (item-on-item)

```kotlin
@Test
fun GameTestState.`test name`() = runGameTest(ScriptUnderTest::class) {
    player.clearInv()
    player.stats[stats.skill] = requiredLevel
    player.inv[0] = InvObj(item1Ref, 1)
    player.inv[1] = InvObj(item2Ref, 1)

    player.withProtectedAccess {
        val type1 = objTypes[item1Ref]
        val type2 = objTypes[item2Ref]
        val event = HeldUEvents.Type(type1, 0, type2, 1)
        eventBus.publish(this, event)
    }

    advance(ticks = 1)
    assertContains(player.inv, expectedProduct)
    assertDoesNotContain(player.inv, consumedItem)
}
```

### Level-Check Pattern

```kotlin
@Test
fun GameTestState.`requires level`() = runGameTest(Script::class) {
    player.stats[stats.fletching] = 0
    player.withProtectedAccess {
        eventBus.publish(this, event)
    }
    assertMessageSent("You need a Fletching level of X to make Y.")
}
```

### NPC Interaction Pattern

```kotlin
@Test
fun GameTestState.`pickpocket test`() = runGameTest(Pickpocket::class) {
    player.stats[stats.thieving] = 1
    random.next = 0
    player.opNpc3(npc)
    advance(ticks = 1)
    assertContains(player.inv, objs.coins)
}
```

---

## Critical Gotchas

1. `advance()` clears capture clients FIRST — assert messages **before** `advance()` for level-check tests.
2. `withProtectedAccess` doesn't await completion — coroutine starts but returns immediately.
3. No `ifButtonT` test helper — use `eventBus.publish(this, HeldUEvents.Type(...))` directly.
4. `invDel` silently fails inside `eventBus.publish` within `withProtectedAccess` (known bug).
5. `ObjTypeList.find()` returns `HashedObjType` — per-module refs use `find("name")`.
6. `inv.count()` requires `UnpackedObjType` — inject `ObjTypeList` and resolve first.
7. All type references use `find("name")` — never use numeric IDs.

## See Also

- [DEVELOPMENT.md](../DEVELOPMENT.md) — Detailed human developer guide
- [ROADMAP.md](../ROADMAP.md) — Project roadmap and phases