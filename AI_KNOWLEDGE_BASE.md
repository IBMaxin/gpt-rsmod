# RSMod AI Knowledge Base

Reference guide for AI agents and developers working on the RSMod codebase.

## Project Overview

- **Root**: `C:\Users\bob\Desktop\gpt-rsmod`
- **Main codebase**: `rsmod-main/` (Kotlin, Gradle, Java 21)
- **Companion tool**: `rsprox-master/`
- **Java**: `C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot`
- **Server port**: 43594, dev realm: no password, auto display names, 150x XP

## Build Commands

All run from `rsmod-main/` directory:
```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
& "C:\Users\bob\Desktop\gpt-rsmod\rsmod-main\gradlew.bat" -p "C:\Users\bob\Desktop\gpt-rsmod\rsmod-main" <task>
```

| Command | Description |
|---------|-------------|
| `:content:skills:{name}:integration` | Run integration tests for a skill |
| `:content:skills:{name}:compileKotlin` | Compile main sources only |
| `:content:skills:{name}:compileIntegrationKotlin` | Compile integration tests |
| `:content:skills:{name}:test` | Run unit tests |
| `:content:skills:fletching:build` | Full build + test |

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
│   ├── skills/             # Skill implementations (thieving, woodcutting, fletching)
│   ├── interfaces/         # Interface scripts (bank, settings, etc.)
│   └── areas/              # Area-specific content
├── engine/
│   ├── events/             # EventBus (publish/subscribe for keyed/suspend events)
│   ├── game/               # Core: Player, Npc, Inventory, ObjType, StatType
│   └── objtx/              # Transaction engine (TransactionInventory, queries)
└── .data/symbols/          # Cache symbol files (obj.sym, loc.sym, npc.sym)
```

## Key APIs

### ObjType System
- `ObjTypeList` — injected via Guice, provides `objTypes[hashedObjType]` → `UnpackedObjType`
- `HashedObjType` — lazy `internalId` computed from hash; used in `find()` calls
- `UnpackedObjType` — loaded from cache; `internalId` set directly
- Both share `ObjType.id` which returns `internalId`
- **NEVER assume item names** — always verify against `.data/symbols/obj.sym`
- `find("item_name")` returns `HashedObjType` (used for per-module refs)

### Inventory Operations
```kotlin
// Player extension functions (api/invtx/)
invAdd(inv, type, count)     // Add items
invDel(inv, type, count)     // Delete items
invSwap(inv, fromSlot, intoSlot)  // Swap slots
invTransaction(inv) { ... }  // Raw transaction

// ProtectedAccess member functions delegate to Player extensions
// Inside ProtectedAccess scope, member functions take priority over extensions
```

### Script Registration
```kotlin
// Item-on-item (onOpHeldU)
onOpHeldU(FletchingObjRefs.knife, FletchingObjRefs.logs) { event ->
    // 'this' is ProtectedAccess, event.first/second are UnpackedObjType
}

// Loc interactions
onOpLoc1(treeType) { /* chop */ }
onOpLoc2(treeType) { /* ... */ }

// NPC interactions
onOpNpc3(npcType) { /* pickpocket */ }

// All use EventBus.composeLongKey(first.id, second.id) for registration
```

### ProtectedAccess
- Wraps `Player` + `GameCoroutine` + `ProtectedAccessContext`
- Provides: `inv`, `worn`, `bank`, `stat()`, `statAdvance()`, `mes()`, `invDel()`, `invAdd()`
- `actionDelay = N` — delays next player action (non-blocking)
- `mes("text")` — **suspends** the coroutine until game cycle processes it
- `invDel`/`invAdd` — **synchronous**, execute immediately within the coroutine

### Stats & XP
```kotlin
stat(stats.fletching)                    // Get level (with invisible boosts)
statAdvance(stats.fletching, xpAmount)   // Add XP (respects xpRate, globalXpRate)
statRandom(stats.fletching, low, high, invisibleLvls)  // Success roll
```

### Events
```kotlin
// EventBus.publish for SuspendEvent (used in tests)
eventBus.publish(protectedAccess, heldUEvent)

// Handler registration
eventBus.subscribeSuspend(HeldUEvents.Type::class.java, key, handler)
```

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

    advance(ticks = 1)  // Required: processes game cycle + flushes capture clients
    assertContains(player.inv, expectedProduct)
    assertDoesNotContain(player.inv, consumedItem)
}
```

### Level-Check Pattern (no advance needed)
```kotlin
@Test
fun GameTestState.`requires level`() = runGameTest(Script::class) {
    player.stats[stats.fletching] = 0
    // ... setup ...
    player.withProtectedAccess {
        eventBus.publish(this, event)
    }
    // Do NOT call advance() — mes() is sent during withProtectedAccess
    assertMessageSent("You need a Fletching level of X to make Y.")
}
```

### NPC Interaction Pattern
```kotlin
@Test
fun GameTestState.`pickpocket test`() = runGameTest(Pickpocket::class) {
    player.stats[stats.thieving] = 1
    random.next = 0  // Force success roll
    player.opNpc3(npc)
    advance(ticks = 1)
    assertContains(player.inv, objs.coins)
}
```

### LOC Interaction Pattern
```kotlin
@Test
fun GameTestState.`chop tree`() = runGameTest(WoodcuttingAxe::class) {
    player.opLoc1(tree)
    advance(ticks = 2)  // Animation plays
    random.next = 0     // Force success
    advance(ticks = 1)
    assertContains(player.inv, logs)
}
```

## Critical Gotchas

### 1. `advance()` clears capture clients FIRST
```kotlin
fun advance(ticks: Int = 1) {
    repeat(ticks) {
        clearCaptureClients()  // WIPES messages sent during withProtectedAccess
        gameCycle.tick()
        flushCaptureClients()  // Only captures messages from this tick
    }
}
```
**Solution**: Assert messages BEFORE `advance()`, or don't call `advance()` for level-check tests.

### 2. `withProtectedAccess` doesn't await completion
`ProtectedAccessLauncher.launch()` calls `player.launch { }` which starts a coroutine but returns immediately. The coroutine runs synchronously if no suspension point, but if `mes()` is called, the coroutine suspends.

### 3. No `ifButtonT` test helper
`GameTestScope` has `ifButtonD` (drag) and `ifButton()` (3-button) but NO `ifButtonT` (use-on-use). To test `onOpHeldU` scripts, use `eventBus.publish(this, HeldUEvents.Type(...))` directly.

### 4. `invDel` transaction failure in event bus context
**KNOWN BUG**: `invDel` silently fails when called inside a script handler triggered by `eventBus.publish` within `withProtectedAccess`, while `invAdd` works. See `FIX_FLETCHING_TESTS.md` for details. The `InvTransactionsTest` proves `invDel` works when called directly via `Player.invDel` extension in `runBasicGameTest`.

### 5. `ObjTypeList.find()` returns `HashedObjType`
Per-module refs should use `find("name")` — don't modify `BaseObjs.kt`. The `internalId` is lazy-computed from hash.

### 6. `HashedObjType.equals()` compares `startHash` + `internalId`
`UnpackedObjType` is a data class — equality is structural. Both types share `ObjType.id` → `internalId`.

### 7. `inv.count()` requires `UnpackedObjType`
`Inventory.count(objType: UnpackedObjType)` needs the unpacked type. Inject `ObjTypeList` and resolve via `objTypes[hashedObjType]`.

### 8. `runGameTest` vs `runBasicGameTest`
- `runGameTest(Script::class)` — creates full test scope with script registration via Guice
- `runBasicGameTest` — simpler scope, uses `withPlayerInit` for direct inventory ops
- Inventory transaction tests should use `runBasicGameTest`

### 9. Global integration initialization failure
Multiple modules currently fail at test initialization with `RuntimeException` at `GameServer.kt:229` because `BaseContent.kt:58` references `content.fletching_knife`, which is not present in the item `.sym` file. This prevents initialization of affected integration suites until that reference is removed or corrected.

## Completed Skills

| Skill | Status | Tests | Notes |
|-------|--------|-------|-------|
| Thieving | Committed (`b888050`) | 0/2 failing at initialization | Pickpocket pattern (NPC-based) |
| Woodcutting | Committed | 0/2 failing at initialization | LOC gathering pattern with timers |
| Fletching | In progress | 0/2 failing at initialization | Item-on-item pattern; also see `invDel` note below |
| Cooking | Not started | — | Plan at `content/skills/cooking/PLAN.md` |
| Firemaking | Not started | — | Plan at `content/skills/firemaking/PLAN.md` |
| Fishing | Not started | — | Plan at `content/skills/fishing/PLAN.md` |

**NOTE**: Affected integration suites currently fail at initialization because `BaseContent.kt:58` references `content.fletching_knife`, which is not defined in the item `.sym` file. This blocks initialization for multiple modules until the invalid reference is removed or replaced.

## File Conventions

| File | Purpose | Example |
|------|---------|---------|
| `{Skill}ObjRefs.kt` | Per-module item refs via `find()` | `FletchingObjRefs.kt` |
| `{Skill}Params.kt` | Server-only param definitions | `FletchingParams.kt` |
| `{Skill}Module.kt` | PluginModule + InvisibleLevelMod | `FletchingModule.kt` |
| `{Skill}LevelBoosts.kt` | Invisible level boosts (cape) | `FletchingLevelBoosts.kt` |
| `{Skill}Script.kt` | PluginScript with startup() | `FletchingBow.kt` |
| `{Skill}ConfigTest.kt` | Verify all refs resolve against cache | `FletchingConfigTest.kt` |
| `{Skill}ScriptTest.kt` | Integration tests for scripts | `FletchingScriptTest.kt` |

## Module Pattern

```
content/skills/{name}/
├── build.gradle.kts          # base-conventions + integration-test-suite plugins
├── PLAN.md                   # Full design doc
├── src/main/kotlin/.../
│   ├── {Name}Module.kt       # PluginModule binding
│   ├── {Name}LevelBoosts.kt  # InvisibleLevelMod for cape boost
│   ├── {Name}ObjRefs.kt      # Item/NPC/LOC references
│   ├── {Name}Params.kt       # Param definitions
│   └── scripts/
│       ├── {Name}Script1.kt  # Script implementations
│       └── {Name}Script2.kt
└── src/integration/kotlin/.../
    ├── {Name}ConfigTest.kt   # Config validation
    └── scripts/{Name}ScriptTest.kt  # Script integration tests
```

## Useful Paths

- Cache symbols: `.data/symbols/obj.sym` (tab-separated `id\tname`)
- Git: repo initialized, initial commit `93263f5`, thieving `b888050`, framework `df42099`
- Planning docs: `DEVELOPMENT.md`, `CHANGELOG.md`, `ROADMAP.md`
- Skill templates: `_template/skills/`, `_template/bosses/`, `_template/npcs/`
- Existing skills (reference): `content/skills/thieving/`, `content/skills/woodcutting/`
