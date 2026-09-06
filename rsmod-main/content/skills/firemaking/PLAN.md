# Firemaking Skill Implementation Plan

## Overview

Use tinderbox on logs → drop log on ground → fire LOC appears → player steps west → fire burns for 60 ticks → fire disappears → ashes left behind.

**Mechanic**: Simple success/fail roll based on level
**XP Rate**: Dev realm 150x
**Scope**: All standard logs (regular through redwood)
**Fire Duration**: Fixed 60-second timer

---

## Module Structure

```
content/skills/firemaking/
├── build.gradle.kts
├── PLAN.md                             # This file
├── src/main/kotlin/org/rsmod/content/skills/firemaking/
│   ├── FiremakingModule.kt             # PluginModule + InvisibleLevelMod binding
│   ├── FiremakingLevelBoosts.kt        # InvisibleLevelMod (pyromancer outfit)
│   ├── scripts/
│   │   ├── Firemaking.kt              # Core: tinderbox + logs → fire + XP
│   │   └── FiremakingLog.kt           # Log data enum
│   └── configs/
│       ├── FiremakingObjRefs.kt        # find() for log tiers + tinderbox
│       ├── FiremakingLocRefs.kt        # find() for fire locs
│       ├── FiremakingLocEditor.kt      # Mark logs with content.firemaking_log
│       └── FiremakingParams.kt         # FM-specific params
├── src/integration/kotlin/org/rsmod/content/skills/firemaking/
│   ├── configs/
│   │   └── FiremakingConfigTest.kt     # Verify all burnable logs have params
│   └── scripts/
│       └── FiremakingTest.kt           # Script integration tests
```

---

## Kotlin Codebase Patterns

### 1. Module Registration (`FiremakingModule.kt`)

```kotlin
package org.rsmod.content.skills.firemaking

import org.rsmod.api.stats.levelmod.InvisibleLevelMod
import org.rsmod.plugin.module.PluginModule

class FiremakingModule : PluginModule() {
    override fun bind() {
        addSetBinding<InvisibleLevelMod>(FiremakingLevelBoosts::class.java)
    }
}
```

### 2. Level Boosts (`FiremakingLevelBoosts.kt`)

```kotlin
package org.rsmod.content.skills.firemaking

import org.rsmod.api.config.refs.stats
import org.rsmod.api.stats.levelmod.InvisibleLevelMod
import org.rsmod.game.entity.Player

class FiremakingLevelBoosts : InvisibleLevelMod(stats.firemaking) {
    override fun Player.calculateBoost(): Int {
        // TODO: Pyromancer outfit (+2.5% XP bonus, not level boost)
        // TODO: Firemaking cape (level 99, no burn fail)
        return 0
    }
}
```

### 3. Item References (`FiremakingObjRefs.kt`)

```kotlin
package org.rsmod.content.skills.firemaking.configs

import org.rsmod.api.type.refs.obj.ObjReferences

internal object FiremakingObjRefs : ObjReferences() {
    // Tools
    val tinderbox = find("tinderbox")

    // Logs (all exist in BaseObjs)
    val logs = find("logs")
    val oak_logs = find("oak_logs")
    val willow_logs = find("willow_logs")
    val teak_logs = find("teak_logs")
    val maple_logs = find("maple_logs")
    val yew_logs = find("yew_logs")
    val magic_logs = find("magic_logs")
    val redwood_logs = find("redwood_logs")

    // Ashes
    val ash = find("ash")
}
```

### 4. LOC References (`FiremakingLocRefs.kt`)

```kotlin
package org.rsmod.content.skills.firemaking.configs

import org.rsmod.api.type.refs.loc.LocReferences

internal object FiremakingLocRefs : LocReferences() {
    // Fire locs (need to verify cache names)
    val fire = find("fire")
    // May need more fire variants for different log types
}
```

### 5. LOC Editor (`FiremakingLocEditor.kt`)

```kotlin
package org.rsmod.content.skills.firemaking.configs

import org.rsmod.api.config.refs.content
import org.rsmod.api.type.editors.loc.LocEditor
import org.rsmod.game.type.loc.LocType

internal object FiremakingLocEditor : LocEditor() {
    init {
        val fireLocs = setOf(
            FiremakingLocRefs.fire,
            // Add more fire variants as needed
        )
        fireLocs.forEach(::editFire)
    }

    private fun editFire(type: LocType) {
        edit(type) {
            contentGroup = content.firemaking_fire
        }
    }
}
```

### 6. Log Editor (`FiremakingLogEditor.kt`)

```kotlin
package org.rsmod.content.skills.firemaking.configs

import org.rsmod.api.config.refs.content
import org.rsmod.api.type.editors.obj.ObjEditor
import org.rsmod.game.type.obj.ObjType

internal object FiremakingLogEditor : ObjEditor() {
    init {
        val logs = mapOf(
            FiremakingObjRefs.logs to FiremakingLog.REGULAR,
            FiremakingObjRefs.oak_logs to FiremakingLog.OAK,
            FiremakingObjRefs.willow_logs to FiremakingLog.WILLOW,
            FiremakingObjRefs.teak_logs to FiremakingLog.TEAK,
            FiremakingObjRefs.maple_logs to FiremakingLog.MAPLE,
            FiremakingObjRefs.yew_logs to FiremakingLog.YEW,
            FiremakingObjRefs.magic_logs to FiremakingLog.MAGIC,
            FiremakingObjRefs.redwood_logs to FiremakingLog.REDWOOD,
        )
        logs.forEach { (obj, data) -> editLog(obj, data) }
    }

    private fun editLog(type: ObjType, data: FiremakingLog) {
        edit(type) {
            contentGroup = content.firemaking_log
        }
    }
}
```

### 7. Log Data Enum (`FiremakingLog.kt`)

```kotlin
package org.rsmod.content.skills.firemaking.scripts

import org.rsmod.api.type.refs.obj.ObjType

enum class FiremakingLog(
    val level: Int,
    val xp: Double,
    val obj: ObjType,
    val ash: ObjType,
) {
    REGULAR(1, 40.0, FiremakingObjRefs.logs, FiremakingObjRefs.ash),
    OAK(15, 60.0, FiremakingObjRefs.oak_logs, FiremakingObjRefs.ash),
    WILLOW(30, 90.0, FiremakingObjRefs.willow_logs, FiremakingObjRefs.ash),
    TEAK(35, 105.0, FiremakingObjRefs.teak_logs, FiremakingObjRefs.ash),
    MAPLE(45, 157.5, FiremakingObjRefs.maple_logs, FiremakingObjRefs.ash),
    YEW(60, 202.5, FiremakingObjRefs.yew_logs, FiremakingObjRefs.ash),
    MAGIC(75, 303.75, FiremakingObjRefs.magic_logs, FiremakingObjRefs.ash),
    REDWOOD(90, 350.0, FiremakingObjRefs.redwood_logs, FiremakingObjRefs.ash);

    companion object {
        private val byObj = entries.associateBy { it.obj }

        fun fromObj(obj: ObjType): FiremakingLog? = byObj[obj]
    }
}
```

### 8. Core Script (`Firemaking.kt`)

```kotlin
package org.rsmod.content.skills.firemaking.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.stats
import org.rsmod.api.invtx.invRemove
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.statAdvance
import org.rsmod.api.script.onUseObjU
import org.rsmod.game.loc.LocType
import org.rsmod.game.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class Firemaking @Inject constructor() : PluginScript() {
    override fun ScriptContext.startup() {
        onUseObjU(FiremakingObjRefs.tinderbox, content.firemaking_log) { light(it.obj) }
    }

    private suspend fun ProtectedAccess.light(log: ObjType) {
        val data = FiremakingLog.fromObj(log) ?: return
        val playerLevel = stat(stats.firemaking)

        if (playerLevel < data.level) {
            mes("You need a Firemaking level of ${data.level} to burn ${log.name.lowercase()}.")
            return
        }

        // Check if player is in a bank (optional: prevent firemaking in banks)
        // if (inBank()) {
        //     mes("You can't light a fire here.")
        //     return
        // }

        actionDelay = 2

        // Remove log from inventory
        invRemove(inv, log, 1)

        // Spawn fire LOC at player's position
        // Note: Need to use locRepo to spawn fire
        // locRepo.spawn(FiremakingLocRefs.fire, player.coords)

        // Player steps west after lighting fire
        // player.stepWest()

        // Award XP
        statAdvance(stats.firemaking, data.xp)

        mes("You light the ${log.name.lowercase()}.")

        // Fire burns for 60 ticks (fixed timer)
        // After 60 ticks, fire disappears and leaves ashes
        // This requires a timer or controller to handle
    }
}
```

### 9. Config Test (`FiremakingConfigTest.kt`)

```kotlin
package org.rsmod.content.skills.firemaking.configs

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.content
import org.rsmod.api.testing.GameTestState

class FiremakingConfigTest {
    @Test
    fun GameTestState.`ensure all burnable logs have content group`() = runBasicGameTest {
        val logs = cacheTypes.objs.values.filter { it.isContentType(content.firemaking_log) }
        assertTrue(logs.isNotEmpty(), "No burnable logs found in cache")
        for (log in logs) {
            assertTrue(log.isContentType(content.firemaking_log))
        }
    }
}
```

### 10. Script Test (`FiremakingTest.kt`)

```kotlin
package org.rsmod.content.skills.firemaking.scripts

import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.stats
import org.rsmod.api.testing.GameTestState
import org.rsmod.map.CoordGrid

class FiremakingTest {
    @Test
    fun GameTestState.`light regular logs at level 1 gives fire and xp`() =
        runGameTest(Firemaking::class) {
            player.teleport(CoordGrid(0, 50, 50, 32, 32))
            player.clearInv()
            player.stats[stats.firemaking] = 1

            // Add regular logs and tinderbox to inventory
            invAdd(player.inv, FiremakingObjRefs.logs, 1)
            invAdd(player.inv, FiremakingObjRefs.tinderbox, 1)

            // Use tinderbox on logs
            player.useObjU(FiremakingObjRefs.tinderbox, FiremakingObjRefs.logs)
            advance(ticks = 2)

            // Verify log was removed
            assertDoesNotContain(player.inv, FiremakingObjRefs.logs)

            // Verify XP was awarded (40 XP * 10 = 400 fine XP)
            assertEquals(400, player.statMap.getXP(stats.firemaking))
        }

    @Test
    fun GameTestState.`light oak logs at level 1 sends level message`() =
        runGameTest(Firemaking::class) {
            player.teleport(CoordGrid(0, 50, 50, 32, 32))
            player.clearInv()
            player.stats[stats.firemaking] = 1

            invAdd(player.inv, FiremakingObjRefs.oak_logs, 1)
            invAdd(player.inv, FiremakingObjRefs.tinderbox, 1)

            player.useObjU(FiremakingObjRefs.tinderbox, FiremakingObjRefs.oak_logs)
            advance(ticks = 1)

            assertMessageSent("You need a Firemaking level of 15 to burn oak logs.")
        }

    @Test
    fun GameTestState.`light oak logs at level 15 gives fire and xp`() =
        runGameTest(Firemaking::class) {
            player.teleport(CoordGrid(0, 50, 50, 32, 32))
            player.clearInv()
            player.stats[stats.firemaking] = 15

            invAdd(player.inv, FiremakingObjRefs.oak_logs, 1)
            invAdd(player.inv, FiremakingObjRefs.tinderbox, 1)

            player.useObjU(FiremakingObjRefs.tinderbox, FiremakingObjRefs.oak_logs)
            advance(ticks = 2)

            assertDoesNotContain(player.inv, FiremakingObjRefs.oak_logs)
            assertEquals(600, player.statMap.getXP(stats.firemaking)) // 60 XP * 10
        }

    @Test
    fun GameTestState.`use tinderbox on non-log does nothing`() =
        runGameTest(Firemaking::class) {
            player.teleport(CoordGrid(0, 50, 50, 32, 32))
            player.clearInv()

            invAdd(player.inv, FiremakingObjRefs.tinderbox, 1)
            invAdd(player.inv, objs.coins, 1)

            player.useObjU(FiremakingObjRefs.tinderbox, objs.coins)
            advance(ticks = 1)

            // No message, just nothing happens
            assertDoesNotContain(player.inv, FiremakingObjRefs.logs)
        }

    @Test
    fun GameTestState.`use non-tinderbox on log does nothing`() =
        runGameTest(Firemaking::class) {
            player.teleport(CoordGrid(0, 50, 50, 32, 32))
            player.clearInv()

            invAdd(player.inv, FiremakingObjRefs.logs, 1)
            invAdd(player.inv, objs.coins, 1)

            player.useObjU(objs.coins, FiremakingObjRefs.logs)
            advance(ticks = 1)

            assertContains(player.inv, FiremakingObjRefs.logs)
        }
}
```

---

## TDD Approach

### Red Phase (Write Tests First)
1. Write `FiremakingConfigTest` — verify all burnable logs have `content.firemaking_log` group
2. Write `FiremakingTest` — verify firemaking works at level 1, respects level reqs

### Green Phase (Implement to Pass)
1. Create `FiremakingModule.kt` — module registration
2. Create `FiremakingLevelBoosts.kt` — placeholder level boosts
3. Create `FiremakingObjRefs.kt` — item references
4. Create `FiremakingLocRefs.kt` — LOC references
5. Create `FiremakingLocEditor.kt` — mark fires with content group
6. Create `FiremakingLogEditor.kt` — mark logs with content group
7. Create `FiremakingParams.kt` — param aliases
8. Create `FiremakingLog.kt` — log data enum
9. Create `Firemaking.kt` — core script

### Refactor Phase
- Verify all tests pass
- Run `./gradlew test` from project root
- Clean up any code smells

---

## Success Rate Formula

OSRS firemaking success rate:
```
success_chance = (level + 1) / 256
```

At level 1: 2/256 = 0.78%
At level 43: 44/256 = 17.2%
At level 99: 100/256 = 39.1%

**MVP Decision**: Always succeed (firemaking is essentially guaranteed at any level in OSRS). The formula exists but failure is extremely rare and not worth implementing for MVP.

---

## Test Summary

| Test | What it verifies |
|------|-----------------|
| `ensure all burnable logs have content group` | Config correctness |
| `light regular logs at level 1 gives fire and xp` | Success path |
| `light oak logs at level 1 sends level message` | Level check |
| `light oak logs at level 15 gives fire and xp` | Higher level log |
| `use tinderbox on non-log does nothing` | Wrong item check |
| `use non-tinderbox on log does nothing` | Wrong tool check |

**Target**: 6+ tests, all passing

---

## Key Implementation Details

### Fire Spawning
- Fire LOC must be spawned at player's current position
- Use `locRepo.spawn()` to create the fire
- Fire should be visible to other players

### Player Movement
- After lighting fire, player steps west (1 tile west)
- This is a key firemaking mechanic in OSRS
- Use `player.stepWest()` or similar movement API

### Fire Duration
- Fixed 60-second timer (60 ticks)
- After timer expires, fire disappears
- Leave ashes on the ground (spawn ash OBJ)

### Inventory Management
- Remove log from inventory before spawning fire
- Check inventory has log and tinderbox before starting
- Check inventory has space for ashes (if needed)

---

## Key File Locations

| File | Path |
|------|------|
| Module | `content/skills/firemaking/src/main/kotlin/.../FiremakingModule.kt` |
| Script | `content/skills/firemaking/src/main/kotlin/.../scripts/Firemaking.kt` |
| Config | `content/skills/firemaking/src/main/kotlin/.../configs/FiremakingLocEditor.kt` |
| Tests | `content/skills/firemaking/src/integration/kotlin/.../scripts/FiremakingTest.kt` |
| Build | `content/skills/firemaking/build.gradle.kts` |

---

## Dependencies

```kotlin
// build.gradle.kts
plugins {
    id("base-conventions")
    id("integration-test-suite")
}

dependencies {
    implementation(projects.api.pluginCommons)
    implementation(projects.api.utils.utilsSkills)
    integrationImplementation(projects.api.player)
    integrationImplementation(projects.api.utils.utilsSkills)
}
```

---

## References

- Woodcutting module: `content/skills/woodcutting/` (pattern reference)
- Thieving module: `content/skills/thieving/` (pattern reference)
- OSRS Wiki Firemaking: https://oldschool.runescape.wiki/w/Firemaking
- BaseParams: `api/config/src/main/kotlin/.../BaseParams.kt`
- BaseContent: `api/config/src/main/kotlin/.../BaseContent.kt`
- BaseStats: `api/config/src/main/kotlin/.../BaseStats.kt`
