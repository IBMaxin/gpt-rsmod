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

## Elvarg → RSMod API Mapping

**CRITICAL: Use ONLY the real RSMod API names listed below. The Elvarg names on the left DO NOT EXIST in RSMod.**

### Type References (use these, not Elvarg's Item/Animation/Graphic)

| Elvarg (WRONG) | RSMod (CORRECT) | Example |
|----------------|-----------------|---------|
| `Item(id)` / `Item(id, amt)` | `objs.*` (ObjType) | `objs.dragon_longsword` |
| `Animation(id, Priority.HIGH)` | `seqs.*` (SeqType) | `seqs.human_unarmedpunch` |
| `Graphic(id, height, Priority.HIGH)` | `spotanims.*` (SpotanimType) | `spotanims.smokepuff` |
| `Sound.ID` / `SoundManager` | `synths.*` (SynthType) | `synths.human_unarmedpunch` |
| `PlayerRights.DEVELOPER` | Check player rights directly | See existing scripts |
| `Boundary(x1, x2, y1, y2)` | `isWithinArea(CoordGrid(...), CoordGrid(...))` | Position-based checks |
| `TimerKey.FOOD` | `timers.*` (TimerType) | `timers.stat_regen` |

### Entity Methods (use these, not Elvarg's methods)

| Elvarg (WRONG) | RSMod (CORRECT) | Notes |
|----------------|-----------------|-------|
| `player.performAnimation(anim)` | `anim(seqs.name)` | On ProtectedAccess |
| `player.performGraphic(gfx)` | `spotanim(spotanims.name, height = 96)` | On ProtectedAccess |
| `player.isPlayer()` / `getAsPlayer()` | `is Player` / `as Player` | Kotlin native type check |
| `npc.isNpc()` / `getAsNpc()` | `is Npc` / `as Npc` | Kotlin native type check |
| `player.sendMessage("text")` | `mes("text")` | Suspends coroutine |
| `player.getPacketSender().sendString(id, text)` | `ifSetText(component, text)` | Interface text |
| `player.getPacketSender().sendInterfaceRemoval()` | `ifClose()` | Close interface |
| `player.getPacketSender().sendWalkableInterface(id)` | `ifSetWalkable(component)` | Walkable interface |
| `SoundManager.sendSound(player, sound)` | `soundSynth(synths.name)` | On ProtectedAccess |
| `player.getSkillManager().stopSkillable()` | Cancel current action | Context-dependent |
| `player.getArea()` | Area check via `isWithinArea()` | Position-based |

### Inventory Methods

| Elvarg (WRONG) | RSMod (CORRECT) | Example |
|----------------|-----------------|---------|
| `player.getInventory().add(item, slot)` | `invAdd(inv, objs.name, count)` | `invAdd(inv, objs.bones, 1)` |
| `player.getInventory().delete(item, slot)` | `invDel(inv, objs.name, count)` | `invDel(inv, objs.bones, 1)` |
| `player.getInventory().contains(id)` | `invTotal(inv, objs.name) > 0` | `invTotal(inv, objs.coins) > 0` |
| `player.getInventory().getCount(id)` | `invTotal(inv, objs.name)` | Returns count |

### Stats & Skills

| Elvarg (WRONG) | RSMod (CORRECT) | Example |
|----------------|-----------------|---------|
| `player.getSkillManager().getCurrentLevel(Skill.X)` | `stat(stats.x)` | `stat(stats.attack)` |
| `player.getSkillManager().getMaxLevel(Skill.X)` | `statBase(stats.x)` | Base level |
| `player.getSkillManager().addExperience(Skill.X, xp)` | `statAdvance(stats.x, xp)` | XP in fine units (x10) |
| `CombatFactory.combatLevelDifference(a, b)` | Manual calculation | Check existing combat code |

### Combat System

| Elvarg (WRONG) | RSMod (CORRECT) | Notes |
|----------------|-----------------|-------|
| `extends MeleeCombatMethod` | `SpecialAttackMap` + `MeleeSpecialAttack` | Different class hierarchy |
| `PendingHit` | `Hit` / `queueHit()` | `queueHit(source, delay, HitType.Melee, damage)` |
| `CombatSpecial.drain(char, amount)` | Handled by SpecialAttackManager | Automatic on special activation |
| `CombatSpecial.DRAGON_DAGGER` | `objs.dragon_longsword` + `special_seqs.*` | Type-based references |

### Timers & Delays

| Elvarg (WRONG) | RSMod (CORRECT) | Example |
|----------------|-----------------|---------|
| `player.getTimers().has(TimerKey.X)` | Check timer state | Context-dependent |
| `player.getTimers().register(key, ticks)` | `timer(timers.name, cycles)` | `timer(timers.prayer_drain, 1)` |
| `player.getTimers().extendOrRegister(key, ticks)` | `softTimer(timers.name, cycles)` | Non-blocking timer |
| Attack delay | `actionDelay = mapClock + cycles` | On ProtectedAccess |

### Sound System

| Elvarg (WRONG) | RSMod (CORRECT) | Example |
|----------------|-----------------|---------|
| `Sound.FOOD_EAT` | `synths.*` | `synths.human_unarmedpunch` |
| `SoundManager.sendSound(player, sound)` | `soundSynth(synths.name)` | On ProtectedAccess |
| `Sound.DRAGON_DAGGER_SPECIAL` | `synths.*` | Look up in BaseSynths |

### Special Attacks (full pattern)

```kotlin
// Elvarg pattern (WRONG):
class DragonDaggerCombatMethod : MeleeCombatMethod() {
    override fun start(character: Mobile, target: Mobile) {
        CombatSpecial.drain(character, CombatSpecial.DRAGON_DAGGER.getDrainAmount())
        character.performAnimation(ANIMATION)
        character.performGraphic(GRAPHIC)
        SoundManager.sendSound(character.getAsPlayer(), Sound.DRAGON_DAGGER_SPECIAL)
    }
}

// RSMod pattern (CORRECT):
class DragonDaggerSpecialAttack : SpecialAttackMap {
    override fun SpecialAttackRepository.register(manager: SpecialAttackManager) {
        registerMelee(objs.dragon_dagger, DragonDagger(manager))
    }
    private class DragonDagger(private val manager: SpecialAttackManager) : MeleeSpecialAttack {
        override suspend fun ProtectedAccess.attack(target: PathingEntity, attack: CombatAttack.Melee) {
            anim(special_seqs.dragon_dagger)
            spotanim(spot = special_spots.dragon_dagger, slot = constants.spotanim_slot_combat, height = 96)
            val damage = manager.rollMeleeDamage(source = this, target = target, attack = attack,
                accuracyMultiplier = 1.25, maxHitMultiplier = 1.25, blockType = MeleeAttackType.Slash)
            manager.giveCombatXp(this, target, attack, damage)
            manager.queueMeleeHit(this, target, damage)
            manager.continueCombat(this, target)
        }
    }
}
```

### How to Reference Types

All type references use `find("name")` and are defined in `api/config/refs/`:
- `objs.*` — Item types (BaseObjs.kt)
- `seqs.*` — Animation types (BaseSeqs.kt)
- `spotanims.*` — Graphic types (BaseSpotanims.kt)
- `synths.*` — Sound types (BaseSynths.kt)
- `stats.*` — Stat types (BaseStats.kt)
- `timers.*` — Timer types (BaseTimers.kt)
- `invs.*` — Inventory types (BaseInvs.kt)
- `components.*` — Interface components (BaseComponents.kt)

**NEVER invent IDs. Always use find("name") references.**

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
