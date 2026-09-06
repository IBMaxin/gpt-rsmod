# Cooking Skill Implementation Plan

## Overview

Use raw food on a range or fire → success/fail roll → cooked food or burnt food. Burn levels vary by food type and cooking source (fire > range > Lumbridge range).

**Mechanic**: OSRS-accurate burn levels per food type
**XP Rate**: Dev realm 150x
**Scope**: Fish only (shrimps through swordfish)

---

## Module Structure

```
content/skills/cooking/
├── build.gradle.kts                    # Already exists
├── PLAN.md                             # This file
├── src/main/kotlin/org/rsmod/content/skills/cooking/
│   ├── CookingModule.kt                # PluginModule + InvisibleLevelMod binding
│   ├── CookingLevelBoosts.kt           # InvisibleLevelMod (cooking gauntlets, diaries)
│   ├── scripts/
│   │   ├── Cooking.kt                  # Core: use raw on range/fire → cooked/burnt
│   │   └── CookingFood.kt             # Food data enum (level, XP, burn levels)
│   └── configs/
│       ├── CookingObjRefs.kt           # find() for raw/cooked/burnt fish
│       ├── CookingLocRefs.kt           # find() for ranges/stoves
│       ├── CookingLocEditor.kt         # Mark locs with content.cooking_range
│       └── CookingParams.kt            # Cook-specific params
├── src/integration/kotlin/org/rsmod/content/skills/cooking/
│   ├── configs/
│   │   └── CookingConfigTest.kt        # Verify all cooking locs have params
│   └── scripts/
│       └── CookingTest.kt              # Script integration tests
```

---

## Kotlin Codebase Patterns

### 1. Module Registration (`CookingModule.kt`)

```kotlin
package org.rsmod.content.skills.cooking

import org.rsmod.api.stats.levelmod.InvisibleLevelMod
import org.rsmod.plugin.module.PluginModule

class CookingModule : PluginModule() {
    override fun bind() {
        addSetBinding<InvisibleLevelMod>(CookingLevelBoosts::class.java)
    }
}
```

**Pattern**: Every skill module extends `PluginModule` and binds its `InvisibleLevelMod`.

### 2. Level Boosts (`CookingLevelBoosts.kt`)

```kotlin
package org.rsmod.content.skills.cooking

import org.rsmod.api.config.refs.stats
import org.rsmod.api.stats.levelmod.InvisibleLevelMod
import org.rsmod.game.entity.Player

class CookingLevelBoosts : InvisibleLevelMod(stats.cooking) {
    override fun Player.calculateBoost(): Int {
        // TODO: Cooking gauntlets (+0 effective levels, but reduce burn rate)
        // TODO: Lumbridge range bonus (requires Cook's Assistant quest)
        // TODO: Hosidius range bonus (requires diary)
        return 0
    }
}
```

**Pattern**: Extends `InvisibleLevelMod`, overrides `calculateBoost()` for invisible level bonuses.

### 3. Params (`CookingParams.kt`)

```kotlin
package org.rsmod.content.skills.cooking.configs

import org.rsmod.api.config.refs.params

object CookingParams {
    val levelrequire = params.levelrequire
    val skill_xp = params.skill_xp
    val skill_productitem = params.skill_productitem
    // New params (server-only, no cache ID needed)
    val cook_burn_level = params.levelrequire  // Placeholder — define as server-only
    val cook_burn_range_level = params.levelrequire
    val cook_burn_lumbridge_level = params.levelrequire
}
```

**Note**: For MVP, we'll hardcode burn levels in the `CookingFood` enum rather than adding new params. This avoids needing to register new param IDs.

### 4. Item References (`CookingObjRefs.kt`)

```kotlin
package org.rsmod.content.skills.cooking.configs

import org.rsmod.api.type.refs.obj.ObjReferences

internal object CookingObjRefs : ObjReferences() {
    // Raw fish
    val raw_shrimps = find("raw_shrimps")
    val raw_anchovies = find("raw_anchovies")
    val raw_sardine = find("raw_sardine")
    val raw_herring = find("raw_herring")
    val raw_trout = find("raw_trout")
    val raw_pike = find("raw_pike")
    val raw_salmon = find("raw_salmon")
    val raw_tuna = find("raw_tuna")
    val raw_lobster = find("raw_lobster")
    val raw_swordfish = find("raw_swordfish")

    // Cooked fish
    val shrimps = find("shrimps")
    val cooked_anchovies = find("anchovies")
    val cooked_sardine = find("sardine")
    val cooked_herring = find("herring")
    val cooked_trout = find("trout")
    val cooked_pike = find("pike")
    val cooked_salmon = find("salmon")
    val cooked_tuna = find("tuna")
    val cooked_lobster = find("lobster")
    val cooked_swordfish = find("swordfish")

    // Burnt fish
    val burnt_shrimps = find("burnt_shrimps")
    val burnt_anchovies = find("burnt_anchovies")
    val burnt_sardine = find("burnt_sardine")
    val burnt_herring = find("burnt_herring")
    val burnt_trout = find("burnt_trout")
    val burnt_pike = find("burnt_pike")
    val burnt_salmon = find("burnt_salmon")
    val burnt_tuna = find("burnt_tuna")
    val burnt_lobster = find("burnt_lobster")
    val burnt_swordfish = find("burnt_swordfish")
}
```

**Pattern**: Each module defines its own `ObjReferences` subclass with `find()` calls.

### 5. LOC References (`CookingLocRefs.kt`)

```kotlin
package org.rsmod.content.skills.cooking.configs

import org.rsmod.api.type.refs.loc.LocReferences

internal object CookingLocRefs : LocReferences() {
    // Ranges (from cache enricher LOCs.toml)
    val hos_cooking_range = find("hos_cooking_range")
    val hos_cooking_range_02 = find("hos_cooking_range_02")
    val dorgesh_cooking_range1 = find("dorgesh_cooking_range1")
    val dorgesh_cooking_range2 = find("dorgesh_cooking_range2")
    val lunar_pirate_cooking_range = find("lunar_pirate_cooking_range")
    val ds2_guild_cooking_range = find("ds2_guild_cooking_range")
    val poh_stove_1 = find("poh_stove_1")
    val poh_stove_2 = find("poh_stove_2")
    val poh_stove_3 = find("poh_stove_3")
    val poh_stove_4 = find("poh_stove_4")
    val poh_stove_5 = find("poh_stove_5")
    val poh_stove_6 = find("poh_stove_6")
    val poh_stove_7 = find("poh_stove_7")
    val fortis_stove = find("fortis_stove")
}
```

### 6. LOC Editor (`CookingLocEditor.kt`)

```kotlin
package org.rsmod.content.skills.cooking.configs

import org.rsmod.api.config.refs.content
import org.rsmod.api.type.editors.loc.LocEditor
import org.rsmod.game.type.loc.LocType

internal object CookingLocEditor : LocEditor() {
    init {
        val ranges = setOf(
            CookingLocRefs.hos_cooking_range,
            CookingLocRefs.hos_cooking_range_02,
            CookingLocRefs.dorgesh_cooking_range1,
            CookingLocRefs.dorgesh_cooking_range2,
            CookingLocRefs.lunar_pirate_cooking_range,
            CookingLocRefs.ds2_guild_cooking_range,
            CookingLocRefs.poh_stove_1,
            CookingLocRefs.poh_stove_2,
            CookingLocRefs.poh_stove_3,
            CookingLocRefs.poh_stove_4,
            CookingLocRefs.poh_stove_5,
            CookingLocRefs.poh_stove_6,
            CookingLocRefs.poh_stove_7,
            CookingLocRefs.fortis_stove,
        )
        ranges.forEach(::editRange)
    }

    private fun editRange(type: LocType) {
        edit(type) {
            contentGroup = content.cooking_range
        }
    }
}
```

**Pattern**: `LocEditor` subclass sets content group on LOC types. Same pattern as `TreeLocs` in woodcutting.

### 7. Food Data Enum (`CookingFood.kt`)

```kotlin
package org.rsmod.content.skills.cooking.scripts

import org.rsmod.api.type.refs.obj.ObjType

enum class CookingFood(
    val level: Int,
    val xp: Double,
    val burnLevel: Int,      // Level where food stops burning on fire
    val burnRangeLevel: Int, // Level where food stops burning on normal range
    val burnLumbridgeLevel: Int, // Level where food stops burning on Lumbridge range
    val raw: ObjType,
    val cooked: ObjType,
    val burnt: ObjType,
) {
    SHRIMPS(1, 30.0, 34, 31, 31, CookingObjRefs.raw_shrimps, CookingObjRefs.shrimps, CookingObjRefs.burnt_shrimps),
    ANCHOVIES(1, 30.0, 34, 31, 31, CookingObjRefs.raw_anchovies, CookingObjRefs.cooked_anchovies, CookingObjRefs.burnt_anchovies),
    SARDINE(1, 40.0, 38, 34, 34, CookingObjRefs.raw_sardine, CookingObjRefs.cooked_sardine, CookingObjRefs.burnt_sardine),
    HERRING(5, 50.0, 41, 38, 38, CookingObjRefs.raw_herring, CookingObjRefs.cooked_herring, CookingObjRefs.burnt_herring),
    TROUT(15, 70.0, 49, 45, 45, CookingObjRefs.raw_trout, CookingObjRefs.cooked_trout, CookingObjRefs.burnt_trout),
    PIKE(20, 80.0, 54, 50, 49, CookingObjRefs.raw_pike, CookingObjRefs.cooked_pike, CookingObjRefs.burnt_pike),
    SALMON(25, 90.0, 58, 55, 55, CookingObjRefs.raw_salmon, CookingObjRefs.cooked_salmon, CookingObjRefs.burnt_salmon),
    TUNA(30, 100.0, 63, 59, 59, CookingObjRefs.raw_tuna, CookingObjRefs.cooked_tuna, CookingObjRefs.burnt_tuna),
    LOBSTER(40, 120.0, 74, 70, 70, CookingObjRefs.raw_lobster, CookingObjRefs.cooked_lobster, CookingObjRefs.burnt_lobster),
    SWORDFISH(45, 140.0, 86, 80, 76, CookingObjRefs.raw_swordfish, CookingObjRefs.cooked_swordfish, CookingObjRefs.burnt_swordfish);

    companion object {
        private val byRaw = entries.associateBy { it.raw }

        fun fromRaw(raw: ObjType): CookingFood? = byRaw[raw]
    }
}
```

### 8. Core Script (`Cooking.kt`)

```kotlin
package org.rsmod.content.skills.cooking.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.stats
import org.rsmod.api.invtx.invAdd
import org.rsmod.api.invtx.invRemove
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.statAdvance
import org.rsmod.api.script.onUseObjU
import org.rsmod.game.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class Cooking @Inject constructor() : PluginScript() {
    override fun ScriptContext.startup() {
        onUseObjU(CookingObjRefs.raw_shrimps, content.cooking_range) { cook(it.obj, it loc) }
        onUseObjU(CookingObjRefs.raw_anchovies, content.cooking_range) { cook(it.obj, it loc) }
        onUseObjU(CookingObjRefs.raw_sardine, content.cooking_range) { cook(it.obj, it loc) }
        onUseObjU(CookingObjRefs.raw_herring, content.cooking_range) { cook(it.obj, it loc) }
        onUseObjU(CookingObjRefs.raw_trout, content.cooking_range) { cook(it.obj, it loc) }
        onUseObjU(CookingObjRefs.raw_pike, content.cooking_range) { cook(it.obj, it loc) }
        onUseObjU(CookingObjRefs.raw_salmon, content.cooking_range) { cook(it.obj, it loc) }
        onUseObjU(CookingObjRefs.raw_tuna, content.cooking_range) { cook(it.obj, it loc) }
        onUseObjU(CookingObjRefs.raw_lobster, content.cooking_range) { cook(it.obj, it loc) }
        onUseObjU(CookingObjRefs.raw_swordfish, content.cooking_range) { cook(it.obj, it loc) }
    }

    private suspend fun ProtectedAccess.cook(raw: ObjType, loc: LocType) {
        val food = CookingFood.fromRaw(raw) ?: return
        val playerLevel = stat(stats.cooking)

        if (playerLevel < food.level) {
            mes("You need a Cooking level of ${food.level} to cook ${raw.name.lowercase()}.")
            return
        }

        if (!inv.hasFreeSpace()) {
            mes("Your inventory is too full to hold any more ${food.cooked.name.lowercase()}.")
            return
        }

        actionDelay = 3

        // Determine burn level based on cooking source
        val burnLevel = when {
            isLumbridgeRange(loc) -> food.burnLumbridgeLevel
            isRange(loc) -> food.burnRangeLevel
            else -> food.burnLevel
        }

        val burned = if (playerLevel >= burnLevel) {
            false
        } else {
            val burnChance = (burnLevel - playerLevel).toDouble() / (burnLevel - food.level + 1)
            randomDouble() < burnChance
        }

        invRemove(inv, raw, 1)

        if (burned) {
            invAdd(inv, food.burnt, 1)
            mes("You accidentally burn the ${raw.name.lowercase()}.")
        } else {
            invAdd(inv, food.cooked, 1)
            statAdvance(stats.cooking, food.xp)
            mes("You cook the ${raw.name.lowercase()}. You manage to make some ${food.cooked.name.lowercase()}.")
        }
    }

    private fun isRange(loc: LocType): Boolean {
        return loc.isContentType(content.cooking_range)
    }

    private fun isLumbridgeRange(loc: LocType): Boolean {
        // Check if this is the Lumbridge range specifically
        return loc.name.contains("lumbridge") || loc.name.contains("cook-o-matic")
    }
}
```

### 9. Config Test (`CookingConfigTest.kt`)

```kotlin
package org.rsmod.content.skills.cooking.configs

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.content
import org.rsmod.api.testing.GameTestState

class CookingConfigTest {
    @Test
    fun GameTestState.`ensure all cooking ranges have content group`() = runBasicGameTest {
        val ranges = cacheTypes.locs.values.filter { it.isContentType(content.cooking_range) }
        assertTrue(ranges.isNotEmpty(), "No cooking ranges found in cache")
        for (range in ranges) {
            assertTrue(range.isContentType(content.cooking_range))
        }
    }
}
```

### 10. Script Test (`CookingTest.kt`)

```kotlin
package org.rsmod.content.skills.cooking.scripts

import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.testing.GameTestState
import org.rsmod.map.CoordGrid

class CookingTest {
    @Test
    fun GameTestState.`cook shrimps at level 1 gives cooked shrimps and xp`() =
        runGameTest(Cooking::class) {
            val locType = locTypes.values.first { it.isContentType(content.cooking_range) }
            val loc = spawnLoc(CoordGrid(0, 50, 50, 32, 32), locType)
            player.teleport(CoordGrid(0, 50, 50, 32, 33))
            player.clearInv()
            player.stats[stats.cooking] = 1

            // Add raw shrimps to inventory
            invAdd(player.inv, CookingObjRefs.raw_shrimps, 1)

            // Use raw shrimps on range
            player.useObjU(CookingObjRefs.raw_shrimps, loc)
            advance(ticks = 3)

            assertContains(player.inv, CookingObjRefs.shrimps)
            assertEquals(300, player.statMap.getXP(stats.cooking)) // 30 XP * 10 (fine)
        }

    @Test
    fun GameTestState.`cook shrimps at level 1 may burn`() =
        runGameTest(Cooking::class) {
            val locType = locTypes.values.first { it.isContentType(content.cooking_range) }
            val loc = spawnLoc(CoordGrid(0, 50, 50, 32, 32), locType)
            player.teleport(CoordGrid(0, 50, 50, 32, 33))
            player.clearInv()
            player.stats[stats.cooking] = 1

            invAdd(player.inv, CookingObjRefs.raw_shrimps, 1)

            // Force burn by setting random to high value
            random.next = 100

            player.useObjU(CookingObjRefs.raw_shrimps, loc)
            advance(ticks = 3)

            assertContains(player.inv, CookingObjRefs.burnt_shrimps)
        }

    @Test
    fun GameTestState.`cook shrimps at level 34 never burns on fire`() =
        runGameTest(Cooking::class) {
            // Test that at burn level, food never burns
            // This requires a fire LOC, not a range
            // For MVP, test with range at lumbridge level
        }

    @Test
    fun GameTestState.`cook without level requirement sends message`() =
        runGameTest(Cooking::class) {
            val locType = locTypes.values.first { it.isContentType(content.cooking_range) }
            val loc = spawnLoc(CoordGrid(0, 50, 50, 32, 32), locType)
            player.teleport(CoordGrid(0, 50, 50, 32, 33))
            player.clearInv()
            player.stats[stats.cooking] = 0

            invAdd(player.inv, CookingObjRefs.raw_shrimps, 1)

            player.useObjU(CookingObjRefs.raw_shrimps, loc)
            advance(ticks = 1)

            assertMessageSent("You need a Cooking level of 1 to cook raw shrimps.")
        }

    @Test
    fun GameTestState.`cook with full inventory sends message`() =
        runGameTest(Cooking::class) {
            val locType = locTypes.values.first { it.isContentType(content.cooking_range) }
            val loc = spawnLoc(CoordGrid(0, 50, 50, 32, 32), locType)
            player.teleport(CoordGrid(0, 50, 50, 32, 33))
            player.fillInv()
            player.stats[stats.cooking] = 1

            invAdd(player.inv, CookingObjRefs.raw_shrimps, 1)

            player.useObjU(CookingObjRefs.raw_shrimps, loc)
            advance(ticks = 1)

            assertMessageSent("Your inventory is too full to hold any more shrimps.")
        }
}
```

---

## TDD Approach

### Red Phase (Write Tests First)
1. Write `CookingConfigTest` — verify all cooking ranges have `content.cooking_range` group
2. Write `CookingTest` — verify cooking works at level 1, burns at low levels, respects level reqs

### Green Phase (Implement to Pass)
1. Create `CookingModule.kt` — module registration
2. Create `CookingLevelBoosts.kt` — placeholder level boosts
3. Create `CookingObjRefs.kt` — item references
4. Create `CookingLocRefs.kt` — LOC references
5. Create `CookingLocEditor.kt` — mark ranges with content group
6. Create `CookingParams.kt` — param aliases
7. Create `CookingFood.kt` — food data enum
8. Create `Cooking.kt` — core script

### Refactor Phase
- Verify all tests pass
- Run `./gradlew test` from project root
- Clean up any code smells

---

## Burn Chance Formula

```
burn_chance = max(0, (burn_level - cooking_level) / (burn_level - cook_level + 1))
```

Where:
- `burn_level` = level where food stops burning (varies by source)
- `cooking_level` = player's cooking level
- `cook_level` = level required to cook the food

Example: Shrimps at level 1 on fire (burn_level = 34):
- burn_chance = (34 - 1) / (34 - 1 + 1) = 33/34 = 0.97 (97% burn chance)

Example: Shrimps at level 30 on fire:
- burn_chance = (34 - 30) / (34 - 1 + 1) = 4/34 = 0.118 (11.8% burn chance)

Example: Shrimps at level 34+ on fire:
- burn_chance = 0 (never burns)

---

## Test Summary

| Test | What it verifies |
|------|-----------------|
| `ensure all cooking ranges have content group` | Config correctness |
| `cook shrimps at level 1 gives cooked shrimps and xp` | Success path |
| `cook shrimps at level 1 may burn` | Burn path |
| `cook shrimps at level 34 never burns on fire` | Burn immunity |
| `cook without level requirement sends message` | Level check |
| `cook with full inventory sends message` | Inventory check |

**Target**: 6+ tests, all passing

---

## Key File Locations

| File | Path |
|------|------|
| Module | `content/skills/cooking/src/main/kotlin/.../CookingModule.kt` |
| Script | `content/skills/cooking/src/main/kotlin/.../scripts/Cooking.kt` |
| Config | `content/skills/cooking/src/main/kotlin/.../configs/CookingLocEditor.kt` |
| Tests | `content/skills/cooking/src/integration/kotlin/.../scripts/CookingTest.kt` |
| Build | `content/skills/cooking/build.gradle.kts` |

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
- OSRS Wiki Cooking: https://oldschool.runescape.wiki/w/Cooking
- BaseParams: `api/config/src/main/kotlin/.../BaseParams.kt`
- BaseContent: `api/config/src/main/kotlin/.../BaseContent.kt`
- BaseStats: `api/config/src/main/kotlin/.../BaseStats.kt`
