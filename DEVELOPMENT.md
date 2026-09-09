# RSMod Development Guide

A comprehensive guide for adding new content (skills, bosses, NPCs, areas) to the RSMod server.

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Module System](#2-module-system)
3. [Creating a New Module](#3-creating-a-new-module)
4. [Plugin System](#4-plugin-system)
5. [Type System](#5-type-system)
6. [Type Editors](#6-type-editors)
7. [TOML Configs](#7-toml-configs)
8. [Content Patterns — Skills](#8-content-patterns--skills)
9. [Content Patterns — Bosses](#9-content-patterns--bosses)
10. [Content Patterns — NPCs](#10-content-patterns--npcs)
11. [Testing](#11-testing)

---

## 1. Architecture Overview

RSMod is a Kotlin Gradle multi-module project (127+ modules) organized into four layers:

```
engine/          Core game loop, DI, plugin system, map, routing (12 modules)
  |
api/             Game APIs: combat, types, cache, stats, utilities (83 modules)
  |
content/         Game content: skills, interfaces, areas, travel (27 modules)
  |
server/          Application entry point, services, logging (5 modules)
```

**Key principles:**
- **Dependency Injection:** All services are injected via Guice (`@Inject`).
- **Type Safety:** Cache types (NPCs, items, locs) are referenced via generated/statically-typed reference objects (`objs.*`, `params.*`, `stats.*`, `content.*`).
- **Event-Driven:** Scripts register handlers on the `EventBus` via helper functions (`onOpLoc1`, `onApNpc2`, etc.).
- **Data-Driven Config:** Game stats, rates, and properties are stored as params on cache types, set via TOML configs and type editors.
- **Separation of Concerns:** Config editing (editors), DI binding (modules), and runtime logic (scripts) are always in separate files/classes.

---

## 2. Module System

### Auto-Discovery

Modules are auto-discovered by `settings.gradle.kts`. Any directory containing a `build.gradle.kts` is automatically included as a Gradle sub-project. This means:

- To create a new module, just create a directory with a `build.gradle.kts`
- No need to edit `settings.gradle.kts`
- Directories starting with `_` are excluded from compilation by convention

### Convention Plugins

All content modules apply `base-conventions` which provides:
- Kotlin JVM configuration
- Java 21 toolchain
- Shared dependency versions
- Type-safe project accessors (`projects.api.pluginCommons`)

For modules with tests, also apply `integration-test-suite` which adds the `integration` source set.

---

## 3. Creating a New Module

### Directory Structure

Every content module follows this structure:

```
content/<category>/<name>/
  build.gradle.kts
  src/main/kotlin/org/rsmod/content/<category>/<name>/
    <Name>Module.kt              -- PluginModule (DI bindings)
    <Name>Script.kt              -- PluginScript (event handlers)
    configs/                      -- Type editors, references, builders
    scripts/                      -- Additional scripts (optional)
  src/main/resources/org/rsmod/content/<category>/<name>/
    npcs.toml                     -- NPC spawn data (optional)
    objs.toml                     -- Object spawn data (optional)
```

### Minimal build.gradle.kts

```kotlin
plugins {
    id("base-conventions")
    id("integration-test-suite")  // optional, for tests
}

dependencies {
    implementation(projects.api.pluginCommons)
    // Add specific dependencies as needed:
    // implementation(projects.api.combat.combatCommons)
    // implementation(projects.api.combat.combatManager)
    // implementation(projects.api.scriptAdvanced)
    // integrationImplementation(projects.api.player)  // for integration tests
}
```

**The `pluginCommons` module is the umbrella dependency** that re-exports nearly the entire API surface. Most content modules only need this one dependency.

---

## 4. Plugin System

### PluginScript

The base class for all game logic scripts:

```kotlin
package org.rsmod.plugin.scripts

public abstract class PluginScript {
    public abstract fun ScriptContext.startup()
}
```

### PluginModule

The base class for dependency injection modules:

```kotlin
class MyModule : PluginModule() {
    override fun bind() {
        // Register bindings here
        addSetBinding<InvisibleLevelMod>(MyLevelBoosts::class.java)
        bindSingleton<MyService>()
    }
}
```

### Event Handlers

All event handlers are extension functions on `ScriptContext`. They are imported from `org.rsmod.api.script.*`.

#### Location Events
```kotlin
onOpLoc1(content.tree) { /* player clicks loc option 1 */ }
onOpLoc2(content.tree) { /* player clicks loc option 2 */ }
onOpLoc3(content.tree) { /* player clicks loc option 3 */ }
onOpLocU(content.tree, objs.axe) { /* player uses item on loc */ }
onApLoc1(content.tree) { /* auto-proximity loc option 1 */ }
```

#### NPC Events
```kotlin
onOpNpc1(npcs.goblin) { /* player clicks NPC option 1 */ }
onOpNpc2(npcs.goblin) { /* player clicks NPC option 2 */ }
onOpNpcU(npcs.goblin, objs.food) { /* player uses item on NPC */ }
onNpcTimer(npcs.boss) { /* NPC timer tick */ }
onNpcQueue(queues.death) { /* NPC death queue */ }
```

#### Object (Item) Events
```kotlin
onOpObj1(objs.bones) { /* player clicks ground item option 1 */ }
onOpObj2(objs.bones) { /* player clicks ground item option 2 */ }
```

#### Held (Inventory) Events
```kotlin
onOpHeld1(objs.food) { /* player clicks inventory item option 1 */ }
onOpHeldU(objs.hammer, objs.anvil) { /* player uses item on item */ }
onEquipObj(objs.sword) { /* player equips item */ }
onUnequipObj(objs.sword) { /* player unequips item */ }
```

#### Player Events
```kotlin
onPlayerLogin { /* player logs in */ }
onPlayerLogout { /* player logs out */ }
onPlayerTimer { /* player timer tick */ }
onPlayerQueue { /* player queue event */ }
```

#### AI / Controller Events
```kotlin
onAiTimer { /* NPC AI timer */ }
onAiConTimer(controllers.my_timer) { /* controller timer */ }
onAiOpPlayer2 { /* NPC auto-attacks player */ }
```

#### Custom Events
```kotlin
onEvent<MyCustomEvent> { /* handle custom event */ }
onCommand("mycommand") { /* cheat command handler */ }
```

### ProtectedAccess

All player interactions go through `ProtectedAccess`, a sandboxed API that ensures safe state mutations. Common operations:

```kotlin
private suspend fun ProtectedAccess.doSomething() {
    mes("This sends a game message.")           // Chat message
    spam("This sends a spam message.")           // Spam message
    anim(827)                                     // Play animation
    soundSynth(451)                               // Play sound
    invAdd(inv, objs.item)                        // Add item to inventory
    invTotal(inv, objs.item)                      // Count items in inventory
    statAdvance(stats.mining, xp)                 // Advance skill XP
    player.miningLvl                              // Get visible skill level
    isFull()                                      // Check if inventory is full
    actionDelay < mapClock                        // Check action delay
    actionDelay = mapClock + 3                    // Set action delay
    clearPendingAction()                          // Cancel current action
    opLoc1(loc)                                   // Re-trigger loc option 1
}
```

---

## 5. Type System

### References

Type references provide type-safe access to cache types. They use `find("name")` or `find("name", hash)` to look up types by internal name.

#### LocReferences
```kotlin
internal object MyLocs : LocReferences() {
    val my_tree = find("tree")                          // by name
    val my_loc = find("my_custom_loc", 12345L)          // by name + hash
}
```

#### NpcReferences
```kotlin
internal object MyNpcs : NpcReferences() {
    val goblin = find("goblin")
    val boss = find("my_boss", 67890L)
}
```

#### ObjReferences
```kotlin
internal object MyObjs : ObjReferences() {
    val sword = find("dragon_sword")
    val ore = find("iron_ore", 11111L)
}
```

#### ParamReferences
```kotlin
internal object MyParams : ParamReferences() {
    val my_custom_param: ParamInt = find("my_custom_param")
    val my_level_req: ParamInt = find("my_level_req")
}
```

#### StatReferences
```kotlin
// Stats are already defined in api/config/refs/BaseStats.kt
// Access via: stats.woodcutting, stats.mining, stats.fishing, etc.
```

#### ContentReferences
```kotlin
// Content groups are already defined in api/config/refs/BaseContent.kt
// Access via: content.tree, content.ore, content.food, etc.
```

#### EnumReferences
```kotlin
internal object MyEnums : EnumReferences() {
    val my_rates = find<ObjType, Int>("my_success_rates")
}
```

---

## 6. Type Editors

Type editors modify existing cache types at load time, adding params and content groups.

### LocEditor

Modifies location (object) types:

```kotlin
internal object MyTreeLocs : LocEditor() {
    init {
        edit(trees.my_tree) {
            contentGroup = content.tree
            param[params.levelrequire] = 15
            param[params.skill_xp] = PlayerStatMap.toFineXP(50.0).toInt()
            param[params.skill_productitem] = objs.logs
            param[params.next_loc_stage] = stumps.my_stump
            param[params.respawn_time] = 60
        }
    }
}
```

### NpcEditor

Modifies NPC types:

```kotlin
internal object MyNpcEditor : NpcEditor() {
    init {
        edit(npcs.my_npc) {
            param[params.attack_anim] = seqs.my_attack_anim
            param[params.defend_anim] = seqs.my_defend_anim
            param[params.death_anim] = seqs.my_death_anim
            param[params.attack_melee] = 50
            param[params.defence_stab] = 30
            param[params.melee_strength] = 40
        }
    }
}
```

### ObjEditor

Modifies item types:

```kotlin
internal object MyToolEditor : ObjEditor() {
    init {
        edit(objs.my_pickaxe) {
            contentGroup = content.ore  // tag as mining tool
            param[params.skill_anim] = seqs.my_mining_anim
            param[params.levelrequire] = 30
        }
    }
}
```

### EnumBuilder

Defines enum data tables (e.g., success rates per tool):

```kotlin
internal object MyEnumBuilder : EnumBuilder() {
    init {
        build<ObjType, Int>("my_pickaxe_rates") {
            this[objs.bronze_pickaxe] = rate(64, 200)
            this[objs.iron_pickaxe] = rate(96, 300)
            this[objs.steel_pickaxe] = rate(128, 400)
        }
    }

    private fun rate(low: Int, high: Int): Int = (low shl 16) or high
}
```

### ParamBuilder

Defines custom param types:

```kotlin
internal object MyParamBuilder : ParamBuilder() {
    init {
        build<Int>("my_custom_param")
        build<Int>("my_level_req")
    }
}
```

---

## 7. TOML Configs

### NPC Spawn Data

Create `npcs.toml` in `src/main/resources/org/rsmod/content/<category>/<name>/`:

```toml
[[spawn]]
npc = 'goblin'
coords = '0_49_49_32_29'

[[spawn]]
npc = 'my_boss'
coords = '0_49_49_50_50'
```

**Coordinate format:** `{level}_{chunkX}_{chunkY}_{localX}_{localY}`

Load in a builder:
```kotlin
internal object MyNpcSpawns : MapNpcSpawnBuilder() {
    init {
        resourceFile<MyScript>(path = "npcs.toml")
    }
}
```

### NPC Stat Enrichment

NPC stats (combat bonuses, animations, examine text) are defined in `api/cache-enricher/src/main/resources/org/rsmod/api/cache/enricher/npc/npcs.toml`:

```toml
[[config]]
npc = 'my_boss'
examine = 'A fearsome creature.'
attack_type = 'Slash'
attack_anim = 422
attack_sound = 2566
defend_anim = 424
defend_sound = 513
death_anim = 836
death_sound = 512
attack_melee = 50
defence_stab = 30
defence_slash = 25
defence_crush = 20
melee_strength = 40
```

### Object Spawn Data

Create `objs.toml` in `src/main/resources/org/rsmod/content/<category>/<name>/`:

```toml
[[spawn]]
obj = 'coins'
coords = '0_49_49_32_29'
count = 100
```

---

## 8. Content Patterns — Skills

The canonical example is **woodcutting** (`content/skills/woodcutting/`).

### File Structure

```
content/skills/<skill>/
  build.gradle.kts
  src/main/kotlin/org/rsmod/content/skills/<skill>/
    <Skill>Module.kt              -- DI bindings
    <Skill>LevelBoosts.kt         -- Invisible level boosts
    configs/
      <Skill>Params.kt            -- Custom param references
      <Skill>Enums.kt             -- Success rate enums
      ToolEditor.kt               -- ObjEditor for tools
      ResourceLocs.kt             -- LocEditor for resources
    scripts/
      <Skill>.kt                  -- Main plugin script
```

### Step-by-Step Pattern

**1. Module (DI bindings):**
```kotlin
class MiningModule : PluginModule() {
    override fun bind() {
        addSetBinding<InvisibleLevelMod>(MiningLevelBoosts::class.java)
    }
}
```

**2. Level boosts:**
```kotlin
class MiningLevelBoosts : InvisibleLevelMod(stats.mining) {
    override fun Player.calculateBoost(): Int {
        // Check if player is in mining guild for +7 boost
        return 0
    }
}
```

**3. Custom params + enums:**
```kotlin
object MiningParams : ParamReferences() {
    val success_rates = find<EnumType<ObjType, Int>>("mining_pickaxe_rates")
}

object MiningEnums : EnumReferences() {
    val copper_rates = find<ObjType, Int>("copper_ore_rates")
    val iron_rates = find<ObjType, Int>("iron_ore_rates")
}

object MiningEnumBuilder : EnumBuilder() {
    init {
        build<ObjType, Int>("copper_ore_rates") {
            this[objs.brace_pickaxe] = rate(64, 200)
            this[objs.iron_pickaxe] = rate(96, 300)
            // ...
        }
    }
    private fun rate(low: Int, high: Int): Int = (low shl 16) or high
}
```

**4. Tool editor:**
```kotlin
internal object PickaxeEditor : ObjEditor() {
    init {
        edit(objs.bronze_pickaxe) {
            contentGroup = content.ore  // use existing or define new
            param[params.skill_anim] = seqs.human_mining_bronze_pickaxe
        }
        // ... more pickaxes
    }
}
```

**5. Resource loc editor:**
```kotlin
internal object OreLocs : LocEditor() {
    init {
        edit(rockLocs.copper_rocks) {
            contentGroup = content.ore
            param[params.levelrequire] = 1
            param[params.skill_xp] = PlayerStatMap.toFineXP(17.5).toInt()
            param[params.skill_productitem] = objs.copper_ore
            param[params.next_loc_stage] = rockLocs.copper_rocks_depleted
            param[params.respawn_time] = 4
        }
    }
}

internal object RockLocs : LocReferences() {
    val copper_rocks = find("copper_rocks")
    val copper_rocks_depleted = find("copper_rocks_depleted")
}
```

**6. Main script:**
```kotlin
class Mining
@Inject
constructor(
    private val objTypes: ObjTypeList,
    private val locTypes: LocTypeList,
    private val enumTypes: EnumTypeList,
    private val locRepo: LocRepository,
    private val xpMods: XpModifiers,
    private val invisibleLvls: InvisibleLevels,
    private val mapClock: MapClock,
) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(content.ore) { attempt(it.loc, it.type) }
        onOpLoc3(content.ore) { mine(it.loc, it.type) }
        onOpLocU(content.ore, content.pickaxe) { mine(it.loc, it.type) }
    }

    private fun ProtectedAccess.attempt(loc: BoundLocInfo, type: UnpackedLocType) {
        if (player.miningLvl < type.levelReq) {
            mes("You need a Mining level of ${type.levelReq} to mine this rock.")
            return
        }
        // Check for pickaxe, start animation, etc.
    }

    private fun ProtectedAccess.mine(loc: BoundLocInfo, type: UnpackedLocType) {
        // Roll success, give XP, add ore, change loc to depleted
        val (low, high) = successRates(type, pickaxe, enumTypes)
        if (statRandom(stats.mining, low, high, invisibleLvls)) {
            val xp = type.xp * xpMods.get(player, stats.mining)
            statAdvance(stats.mining, xp)
            invAdd(inv, type.product)
            locRepo.change(loc, type.depletedLoc, type.respawnTime)
        }
    }

    companion object {
        val UnpackedLocType.levelReq: Int by locParam(params.levelrequire)
        val UnpackedLocType.product: ObjType by locParam(params.skill_productitem)
        val UnpackedLocType.xp: Double by locXpParam(params.skill_xp)
        val UnpackedLocType.depletedLoc: LocType by locParam(params.next_loc_stage)
        val UnpackedLocType.respawnTime: Int by locParam(params.respawn_time)
    }
}
```

---

## 9. Content Patterns — Bosses

Bosses combine combat, custom AI, and drop tables. No boss content exists yet in the codebase, so this is a new pattern.

### Key Components

**1. Combat setup:** NPCs are configured via TOML enrichment with combat stats, attack types, and animations.

**2. Custom AI:** Boss scripts use `onNpcTimer`, `onNpcQueue`, and `onEvent<NpcStateEvents.*>` for phase-based behavior.

**3. Phases:** Track NPC phase via varnpc or a controller. Change attack patterns at HP thresholds.

**4. Special attacks:** Implement `SpecialAttackMap` and register via `addSetBinding` in the module.

**5. Drops:** The current system only drops bones. A full drop table system needs to be built (see `api/death/NpcDeath.kt` — `TODO: Drop tables`).

### Example Boss Structure

```kotlin
class BossModule : PluginModule() {
    override fun bind() {
        addSetBinding<SpecialAttackMap>(BossSpecialAttacks::class.java)
    }
}

class BossScript
@Inject
constructor(
    private val npcRepo: NpcRepository,
    private val mapClock: MapClock,
) : PluginScript() {
    override fun ScriptContext.startup() {
        onNpcTimer(npcs.my_boss) { bossTick() }
        onEvent<NpcStateEvents.Create> { initializeBoss() }
    }

    private fun Npc.bossTick() {
        when {
            hitpoints < type.hitpoints / 4 -> phase3()
            hitpoints < type.hitpoints / 2 -> phase2()
            else -> phase1()
        }
    }

    private fun Npc.phase1() { /* standard attacks */ }
    private fun Npc.phase2() { /* new mechanics */ }
    private fun Npc.phase3() { /* enrage mode */ }
}
```

### Combat NPC Params (in npcs.toml)

```toml
[[config]]
npc = 'my_boss'
examine = 'A powerful boss.'
attack_type = 'Slash'
attack_anim = 422
attack_sound = 2566
defend_anim = 424
death_anim = 836
attack_melee = 80
defence_stab = 60
defence_slash = 50
defence_crush = 40
melee_strength = 70
poison_immunity = true
venom_immunity = 'Immune'
```

### NPC Creature Types (Params)

Set these params on the NPC to trigger combat bonuses:

| Param | Effect |
|-------|--------|
| `params.undead` | Salve amulet bonus (120% attack/damage) |
| `params.demon` | Arclight bonus (170% attack/damage) |
| `params.draconic` | Dragon hunter lance bonus (120% attack/damage) |
| `params.kalphite` | Keris bonus (133% damage, 3x proc) |
| `params.revenant` | Amulet of avarice bonus (135% damage) |
| `params.leafy` | Leaf-bladed weapon bonus (117.5% damage) |

---

## 10. Content Patterns — NPCs

### NPC Spawning

Define spawns in TOML (see [TOML Configs](#7-toml-configs)).

### NPC Dialogue

Create a script for each NPC:

```kotlin
class MyNpcScript : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(npcs.my_npc) { talkToNpc() }
    }

    private suspend fun ProtectedAccess.talkToNpc() {
        mes("Hello, adventurer!")
        val choice = multi("What do you need?", "Combat training", "Quests", "Nothing")
        when (choice) {
            0 -> mes("I can teach you how to fight.")
            1 -> mes("Come back when you're ready for a quest.")
            2 -> mes("Goodbye!")
        }
    }
}
```

### NPC Behavior Modes

NPCs have behavioral modes defined in `NpcMode.kt`:

| Mode | Description |
|------|-------------|
| `None` | Idle, no behavior |
| `Wander` | Walks around spawn area |
| `Patrol` | Follows predefined path |
| `PlayerFollow` | Follows nearby player |
| `PlayerFace` | Faces nearby player |
| `ApPlayer1-8` | Auto-attacks player (combat) |

Set in TOML:
```toml
[[spawn]]
npc = 'guard'
coords = '0_49_49_32_29'
wander_range = 4
```

---

## 11. Testing

### Integration Tests

Use the `integration-test-suite` convention plugin and `GameTestState`:

```kotlin
class MyScriptTest {
    @Test
    fun GameTestState.`validate level requirement`() =
        runGameTest(MyScript::class) {
            val type = findLocType(content.my_resource) { it.levelReq == 10 }
            val loc = placeMapLoc(CoordGrid(0, 50, 50, 32, 32), type)
            player.teleport(loc.coords.translateX(-1))
            player.stats[stats.mining] = 5
            player.opLoc1(loc)
            advance(ticks = 2)
            assertMessageSent("You need a Mining level of 10 to mine this rock.")
        }
}
```

### Config Validation Tests

```kotlin
class MyConfigTest {
    @Test
    fun GameTestState.`validate all ore rocks have required params`() =
        runGameTest(MyScript::class) {
            val locs = listOf(rockLocs.copper, rockLocs.tin, rockLocs.iron)
            for (loc in locs) {
                val type = findLocType(loc)
                assertHasParam(type, params.levelrequire)
                assertHasParam(type, params.skill_xp)
                assertHasParam(type, params.skill_productitem)
            }
        }
}
```

### Running Tests

See **Build & Test Commands** in [AGENTS.md](rsmod-main/AGENTS.md) for the full command reference and [docs/archive/ardougne-thieving-progress.md](rsmod-main/docs/archive/ardougne-thieving-progress.md) for current test status.

---

## Quick Reference: Common Imports

```kotlin
// Script base
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

// Event handlers
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLoc3
import org.rsmod.api.script.onOpLocU
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc2
import org.rsmod.api.script.onOpHeld1
import org.rsmod.api.script.onOpHeldU
import org.rsmod.api.script.onPlayerLogin
import org.rsmod.api.script.onEvent
import org.rsmod.api.script.onNpcTimer
import org.rsmod.api.script.onNpcQueue
import org.rsmod.api.script.onAiConTimer
import org.rsmod.api.script.onCommand

// Config refs
import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.seqs
import org.rsmod.api.config.refs.synths
import org.rsmod.api.config.refs.controllers
import org.rsmod.api.config.refs.queues
import org.rsmod.api.config.refs.varcons
import org.rsmod.api.config.locParam
import org.rsmod.api.config.locXpParam
import org.rsmod.api.config.objParam

// Type system
import org.rsmod.api.type.editors.loc.LocEditor
import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.api.type.editors.obj.ObjEditor
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.enums.EnumReferences
import org.rsmod.api.type.refs.param.ParamReferences
import org.rsmod.api.type.builders.enums.EnumBuilder
import org.rsmod.api.type.builders.param.ParamBuilder

// Player access
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.miningLvl
import org.rsmod.api.player.stat.woodcuttingLvl
import org.rsmod.api.player.righthand

// Stats / XP
import org.rsmod.api.stats.levelmod.InvisibleLevelMod
import org.rsmod.api.stats.levelmod.InvisibleLevels
import org.rsmod.api.stats.xpmod.XpModifiers
import org.rsmod.game.stat.PlayerStatMap

// Repos
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.repo.npc.NpcRepository
import org.rsmod.api.repo.controller.ControllerRepository

// Game types
import org.rsmod.game.entity.Player
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Controller
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.inv.InvObj
import org.rsmod.game.MapClock
import org.rsmod.game.type.loc.UnpackedLocType
import org.rsmod.game.type.obj.UnpackedObjType
import org.rsmod.game.type.npc.UnpackedNpcType
import org.rsmod.game.type.enums.EnumTypeList
import org.rsmod.game.type.loc.LocTypeList
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.loc.LocType
import org.rsmod.game.type.obj.ObjType

// Combat
import org.rsmod.api.combat.PvNCombat
import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.combat.commons.styles.AttackStyle
import org.rsmod.api.combat.commons.types.MeleeAttackType
import org.rsmod.api.combat.manager.PlayerAttackManager
import org.rsmod.api.combat.weapon.styles.AttackStyles
import org.rsmod.api.combat.weapon.types.AttackTypes

// Map
import org.rsmod.map.CoordGrid
import org.rsmod.map.zone.ZoneKey

## See Also

- [AGENTS.md](rsmod-main/AGENTS.md) — AI agent reference (build commands, API reference, gotchas)
- [ROADMAP.md](ROADMAP.md) — Project roadmap and phases
- [SETUP.md](SETUP.md) — Detailed setup guide
- [docs/design/teleport-menu.md](rsmod-main/docs/design/teleport-menu.md) — Teleport menu design doc
- [docs/quirks.md](rsmod-main/docs/quirks.md) — Technical quirks and design decisions
```
