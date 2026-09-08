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

## Critical Gotchas

1. `advance()` clears capture clients FIRST — assert messages BEFORE advance() for level-check tests.
2. `withProtectedAccess` doesn't await completion — coroutine starts but returns immediately.
3. No `ifButtonT` test helper — use `eventBus.publish(this, HeldUEvents.Type(...))` directly.
4. `invDel` silently fails inside eventBus.publish within withProtectedAccess (known bug).
5. `ObjTypeList.find()` returns `HashedObjType` — per-module refs use find("name").
6. `inv.count()` requires `UnpackedObjType` — inject ObjTypeList and resolve first.

## File Conventions

| File | Purpose |
|------|---------|
| `{Skill}ObjRefs.kt` | Per-module item refs via find() |
| `{Skill}Params.kt` | Server-only param definitions |
| `{Skill}Module.kt` | PluginModule + InvisibleLevelMod |
| `{Skill}LevelBoosts.kt` | Invisible level boosts (cape) |
| `{Skill}Script.kt` | PluginScript with startup() |
| `{Skill}ConfigTest.kt` | Verify all refs resolve against cache |
| `{Skill}ScriptTest.kt` | Integration tests for scripts |

## Module Pattern

```
content/skills/{name}/
├── build.gradle.kts
├── PLAN.md
├── src/main/kotlin/.../
│   ├── {Name}Module.kt
│   ├── {Name}LevelBoosts.kt
│   ├── {Name}ObjRefs.kt
│   ├── {Name}Params.kt
│   └── scripts/
│       └── {Name}Script.kt
└── src/integration/kotlin/.../
    ├── {Name}ConfigTest.kt
    └── scripts/{Name}ScriptTest.kt
```
