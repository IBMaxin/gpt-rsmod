# Fishing Skill Implementation Plan

## Overview

Click fishing spot (NPC) with correct equipment → timer → catch fish. Different spot types offer different fish (net, bait, lure, cage, harpoon).

**Mechanic**: NPC-based gathering skill with equipment checks
**XP Rate**: Dev realm 150x
**Scope**: F2P fish only (shrimps through swordfish)
**Fishing Spots**: NPCs with multiple options

---

## Module Structure

```
content/skills/fishing/
├── build.gradle.kts
├── PLAN.md                             # This file
├── src/main/kotlin/org/rsmod/content/skills/fishing/
│   ├── FishingModule.kt                # PluginModule + InvisibleLevelMod binding
│   ├── FishingLevelBoosts.kt           # InvisibleLevelMod (diary bonuses)
│   ├── scripts/
│   │   ├── Fishing.kt                  # Core: click spot → timer → catch fish
│   │   ├── FishingSpot.kt             # Spot definitions (net/bait/lure/cage/harpoon)
│   │   └── FishingFish.kt             # Fish data enum
│   └── configs/
│       ├── FishingObjRefs.kt           # find() for raw fish, equipment
│       ├── FishingNpcRefs.kt           # find() for fishing spot NPCs
│       ├── FishingNpcEditor.kt         # Mark NPCs with content.fishing_spot
│       └── FishingParams.kt            # Fishing-specific params
├── src/integration/kotlin/org/rsmod/content/skills/fishing/
│   ├── configs/
│   │   └── FishingConfigTest.kt        # Verify all fishing spots have params
│   └── scripts/
│       └── FishingTest.kt              # Script integration tests
```

---

## Kotlin Codebase Patterns

### 1. Module Registration (`FishingModule.kt`)

```kotlin
package org.rsmod.content.skills.fishing

import org.rsmod.api.stats.levelmod.InvisibleLevelMod
import org.rsmod.plugin.module.PluginModule

class FishingModule : PluginModule() {
    override fun bind() {
        addSetBinding<InvisibleLevelMod>(FishingLevelBoosts::class.java)
    }
}
```

### 2. Level Boosts (`FishingLevelBoosts.kt`)

```kotlin
package org.rsmod.content.skills.fishing

import org.rsmod.api.config.refs.stats
import org.rsmod.api.stats.levelmod.InvisibleLevelMod
import org.rsmod.game.entity.Player

class FishingLevelBoosts : InvisibleLevelMod(stats.fishing) {
    override fun Player.calculateBoost(): Int {
        // TODO: Fishing cape (level 99)
        // TODO:_diary bonuses (Karamja, Kourend, etc.)
        // TODO: Rada's blessing (+4% catch rate)
        return 0
    }
}
```

### 3. Item References (`FishingObjRefs.kt`)

```kotlin
package org.rsmod.content.skills.fishing.configs

import org.rsmod.api.type.refs.obj.ObjReferences

internal object FishingObjRefs : ObjReferences() {
    // Equipment
    val small_fishing_net = find("small_fishing_net")
    val fishing_rod = find("fishing_rod")
    val fly_fishing_rod = find("fly_fishing_rod")
    val harpoon = find("harpoon")
    val lobster_pot = find("lobster_pot")
    val fishing_bait = find("fishing_bait")
    val feather = find("feather")

    // Raw fish
    val raw_shrimps = find("raw_shrimps")
    val raw_sardine = find("raw_sardine")
    val raw_herring = find("raw_herring")
    val raw_anchovies = find("raw_anchovies")
    val raw_trout = find("raw_trout")
    val raw_pike = find("raw_pike")
    val raw_salmon = find("raw_salmon")
    val raw_tuna = find("raw_tuna")
    val raw_lobster = find("raw_lobster")
    val raw_swordfish = find("raw_swordfish")
}
```

### 4. NPC References (`FishingNpcRefs.kt`)

```kotlin
package org.rsmod.content.skills.fishing.configs

import org.rsmod.api.type.refs.npc.NpcReferences

internal object FishingNpcRefs : NpcReferences() {
    // Net/Bait fishing spots
    val fishing_spot_1527 = find("fishing_spot_1527")
    val fishing_spot_1530 = find("fishing_spot_1530")
    val fishing_spot_1518 = find("fishing_spot_1518")
    val fishing_spot_1521 = find("fishing_spot_1521")
    val fishing_spot_1523 = find("fishing_spot_1523")
    val fishing_spot_1524 = find("fishing_spot_1524")
    val fishing_spot_1525 = find("fishing_spot_1525")
    val fishing_spot_1528 = find("fishing_spot_1528")
    val fishing_spot_1544 = find("fishing_spot_1544")

    // Lure/Bait fishing spots
    val fishing_spot_1526 = find("fishing_spot_1526")
    val fishing_spot_1529 = find("fishing_spot_1529")
    val fishing_spot_1531 = find("fishing_spot_1531")
    val fishing_spot_1532 = find("fishing_spot_1532")

    // Cage/Harpoon fishing spots
    val fishing_spot_1515 = find("fishing_spot_1515")
    val fishing_spot_1516 = find("fishing_spot_1516")
    val fishing_spot_1519 = find("fishing_spot_1519")
    val fishing_spot_1520 = find("fishing_spot_1520")
    val fishing_spot_1522 = find("fishing_spot_1522")
    val fishing_spot_1533 = find("fishing_spot_1533")
    val fishing_spot_1534 = find("fishing_spot_1534")
    val fishing_spot_1536 = find("fishing_spot_1536")
    val fishing_spot_1537 = find("fishing_spot_1537")
}
```

### 5. NPC Editor (`FishingNpcEditor.kt`)

```kotlin
package org.rsmod.content.skills.fishing.configs

import org.rsmod.api.config.refs.content
import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.game.type.npc.NpcType

internal object FishingNpcEditor : NpcEditor() {
    init {
        // Net/Bait spots
        val netBaitSpots = setOf(
            FishingNpcRefs.fishing_spot_1527,
            FishingNpcRefs.fishing_spot_1530,
            FishingNpcRefs.fishing_spot_1518,
            FishingNpcRefs.fishing_spot_1521,
            FishingNpcRefs.fishing_spot_1523,
            FishingNpcRefs.fishing_spot_1524,
            FishingNpcRefs.fishing_spot_1525,
            FishingNpcRefs.fishing_spot_1528,
            FishingNpcRefs.fishing_spot_1544,
        )
        netBaitSpots.forEach { editSpot(it, FishingSpotType.NET_BAIT) }

        // Lure/Bait spots
        val lureBaitSpots = setOf(
            FishingNpcRefs.fishing_spot_1526,
            FishingNpcRefs.fishing_spot_1529,
            FishingNpcRefs.fishing_spot_1531,
            FishingNpcRefs.fishing_spot_1532,
        )
        lureBaitSpots.forEach { editSpot(it, FishingSpotType.LURE_BAIT) }

        // Cage/Harpoon spots
        val cageHarpoonSpots = setOf(
            FishingNpcRefs.fishing_spot_1515,
            FishingNpcRefs.fishing_spot_1516,
            FishingNpcRefs.fishing_spot_1519,
            FishingNpcRefs.fishing_spot_1520,
            FishingNpcRefs.fishing_spot_1522,
            FishingNpcRefs.fishing_spot_1533,
            FishingNpcRefs.fishing_spot_1534,
            FishingNpcRefs.fishing_spot_1536,
            FishingNpcRefs.fishing_spot_1537,
        )
        cageHarpoonSpots.forEach { editSpot(it, FishingSpotType.CAGE_HARPOON) }
    }

    private fun editSpot(type: NpcType, spotType: FishingSpotType) {
        edit(type) {
            contentGroup = content.fishing_spot
            param[FishingParams.fish_spot_type] = spotType.id
        }
    }
}
```

### 6. Params (`FishingParams.kt`)

```kotlin
package org.rsmod.content.skills.fishing.configs

import org.rsmod.api.config.refs.params

object FishingParams {
    val levelrequire = params.levelrequire
    val skill_xp = params.skill_xp
    // New params (server-only)
    val fish_spot_type = params.levelrequire  // Placeholder — define as server-only
    val fish_method = params.levelrequire     // 0=net, 1=bait, 2=lure, 3=cage, 4=harpoon
}
```

### 7. Spot Type Enum (`FishingSpotType.kt`)

```kotlin
package org.rsmod.content.skills.fishing.configs

enum class FishingSpotType(val id: Int) {
    NET_BAIT(0),      // Net: shrimps/anchovies, Bait: sardine/herring/pike
    LURE_BAIT(1),     // Lure: trout/salmon, Bait: pike
    CAGE_HARPOON(2),  // Cage: lobster, Harpoon: tuna/swordfish
}
```

### 8. Fish Data Enum (`FishingFish.kt`)

```kotlin
package org.rsmod.content.skills.fishing.scripts

import org.rsmod.api.type.refs.obj.ObjType

enum class FishingFish(
    val level: Int,
    val xp: Double,
    val method: FishingMethod,
    val equipment: ObjType,
    val product: ObjType,
) {
    SHRIMPS(1, 10.0, FishingMethod.NET, FishingObjRefs.small_fishing_net, FishingObjRefs.raw_shrimps),
    SARDINE(5, 20.0, FishingMethod.BAIT, FishingObjRefs.fishing_rod, FishingObjRefs.raw_sardine),
    HERRING(10, 30.0, FishingMethod.BAIT, FishingObjRefs.fishing_rod, FishingObjRefs.raw_herring),
    ANCHOVIES(15, 40.0, FishingMethod.NET, FishingObjRefs.small_fishing_net, FishingObjRefs.raw_anchovies),
    TROUT(20, 50.0, FishingMethod.LURE, FishingObjRefs.fly_fishing_rod, FishingObjRefs.raw_trout),
    PIKE(25, 60.0, FishingMethod.BAIT, FishingObjRefs.fishing_rod, FishingObjRefs.raw_pike),
    SALMON(30, 70.0, FishingMethod.LURE, FishingObjRefs.fly_fishing_rod, FishingObjRefs.raw_salmon),
    TUNA(35, 80.0, FishingMethod.HARPOON, FishingObjRefs.harpoon, FishingObjRefs.raw_tuna),
    LOBSTER(40, 90.0, FishingMethod.CAGE, FishingObjRefs.lobster_pot, FishingObjRefs.raw_lobster),
    SWORDFISH(50, 100.0, FishingMethod.HARPOON, FishingObjRefs.harpoon, FishingObjRefs.raw_swordfish);

    companion object {
        fun forMethod(method: FishingMethod): List<FishingFish> =
            entries.filter { it.method == method }
    }
}

enum class FishingMethod {
    NET,
    BAIT,
    LURE,
    CAGE,
    HARPOON,
}
```

### 9. Spot Definitions (`FishingSpot.kt`)

```kotlin
package org.rsmod.content.skills.fishing.scripts

data class FishingSpotDef(
    val type: FishingSpotType,
    val options: List<FishingOption>,
)

data class FishingOption(
    val name: String,
    val method: FishingMethod,
    val fish: List<FishingFish>,
)

object FishingSpots {
    val spots = mapOf(
        FishingSpotType.NET_BAIT to FishingSpotDef(
            type = FishingSpotType.NET_BAIT,
            options = listOf(
                FishingOption("Net", FishingMethod.NET, FishingFish.forMethod(FishingMethod.NET)),
                FishingOption("Bait", FishingMethod.BAIT, FishingFish.forMethod(FishingMethod.BAIT)),
            ),
        ),
        FishingSpotType.LURE_BAIT to FishingSpotDef(
            type = FishingSpotType.LURE_BAIT,
            options = listOf(
                FishingOption("Lure", FishingMethod.LURE, FishingFish.forMethod(FishingMethod.LURE)),
                FishingOption("Bait", FishingMethod.BAIT, FishingFish.forMethod(FishingMethod.BAIT)),
            ),
        ),
        FishingSpotType.CAGE_HARPOON to FishingSpotDef(
            type = FishingSpotType.CAGE_HARPOON,
            options = listOf(
                FishingOption("Cage", FishingMethod.CAGE, FishingFish.forMethod(FishingMethod.CAGE)),
                FishingOption("Harpoon", FishingMethod.HARPOON, FishingFish.forMethod(FishingMethod.HARPOON)),
            ),
        ),
    )
}
```

### 10. Core Script (`Fishing.kt`)

```kotlin
package org.rsmod.content.skills.fishing.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.stats
import org.rsmod.api.invtx.invAdd
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.statAdvance
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc2
import org.rsmod.game.entity.Npc
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class Fishing @Inject constructor() : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(content.fishing_spot) { showOptions(it.npc) }
        onOpNpc2(content.fishing_spot) { fish(it.npc) }
    }

    private suspend fun ProtectedAccess.showOptions(npc: Npc) {
        // Show fishing options menu
        // This is a simplified version — real implementation would show interface
        mes("What would you like to fish?")
    }

    private suspend fun ProtectedAccess.fish(npc: Npc) {
        val spotType = FishingSpotType.entries[
            npc.type.param(FishingParams.fish_spot_type)
        ]
        val spotDef = FishingSpots.spots[spotType] ?: return

        // Determine which method to use based on available equipment
        val option = determineOption(spotDef) ?: run {
            mes("You need the correct fishing equipment to fish here.")
            return
        }

        // Find the best fish the player can catch
        val fish = findBestFish(option.fish) ?: run {
            mes("Your Fishing level is too low to catch anything here.")
            return
        }

        // Check equipment
        if (!hasEquipment(fish.equipment)) {
            mes("You need a ${fish.equipment.name.lowercase()} to fish here.")
            return
        }

        // Check inventory space
        if (!inv.hasFreeSpace()) {
            mes("Your inventory is too full to hold any more ${fish.product.name.lowercase()}.")
            return
        }

        actionDelay = 4

        // Success check (simplified — always succeed for MVP)
        // Real implementation would use statRandom with catch rate formula
        invAdd(inv, fish.product, 1)
        statAdvance(stats.fishing, fish.xp)

        mes("You catch a ${fish.product.name.lowercase()}.")
    }

    private fun determineOption(spotDef: FishingSpotDef): FishingOption? {
        // Check which equipment player has and return matching option
        for (option in spotDef.options) {
            val fish = option.fish.firstOrNull() ?: continue
            if (hasEquipment(fish.equipment)) {
                return option
            }
        }
        return null
    }

    private fun findBestFish(fish: List<FishingFish>): FishingFish? {
        val playerLevel = stat(stats.fishing)
        return fish.filter { playerLevel >= it.level }.maxByOrNull { it.level }
    }

    private fun hasEquipment(equipment: ObjType): Boolean {
        return inv.contains(equipment)
    }
}
```

### 11. Config Test (`FishingConfigTest.kt`)

```kotlin
package org.rsmod.content.skills.fishing.configs

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.content
import org.rsmod.api.testing.GameTestState

class FishingConfigTest {
    @Test
    fun GameTestState.`ensure all fishing spots have content group`() = runBasicGameTest {
        val spots = cacheTypes.npcs.values.filter { it.isContentType(content.fishing_spot) }
        assertTrue(spots.isNotEmpty(), "No fishing spots found in cache")
        for (spot in spots) {
            assertTrue(spot.isContentType(content.fishing_spot))
        }
    }

    @Test
    fun GameTestState.`ensure all fishing spots have spot type param`() = runBasicGameTest {
        val spots = cacheTypes.npcs.values.filter { it.isContentType(content.fishing_spot) }
        for (spot in spots) {
            val params = spot.paramMap
            assertTrue(FishingParams.fish_spot_type in params)
        }
    }
}
```

### 12. Script Test (`FishingTest.kt`)

```kotlin
package org.rsmod.content.skills.fishing.scripts

import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.stats
import org.rsmod.api.testing.GameTestState
import org.rsmod.map.CoordGrid

class FishingTest {
    @Test
    fun GameTestState.`click fishing spot at level 1 with net catches shrimps`() =
        runGameTest(Fishing::class) {
            val npcType = npcTypes.values.first { it.isContentType(content.fishing_spot) }
            val npc = spawnNpc(CoordGrid(0, 50, 50, 32, 32), npcType)
            player.teleport(CoordGrid(0, 50, 50, 32, 33))
            player.clearInv()
            player.stats[stats.fishing] = 1

            // Add small fishing net to inventory
            invAdd(player.inv, FishingObjRefs.small_fishing_net, 1)

            // Click fishing spot
            player.opNpc2(npc)
            advance(ticks = 4)

            // Verify raw shrimps were caught
            assertContains(player.inv, FishingObjRefs.raw_shrimps)

            // Verify XP was awarded (10 XP * 10 = 100 fine XP)
            assertEquals(100, player.statMap.getXP(stats.fishing))
        }

    @Test
    fun GameTestState.`click fishing spot at level 1 with rod catches sardine`() =
        runGameTest(Fishing::class) {
            val npcType = npcTypes.values.first { it.isContentType(content.fishing_spot) }
            val npc = spawnNpc(CoordGrid(0, 50, 50, 32, 32), npcType)
            player.teleport(CoordGrid(0, 50, 50, 32, 33))
            player.clearInv()
            player.stats[stats.fishing] = 1

            invAdd(player.inv, FishingObjRefs.fishing_rod, 1)
            invAdd(player.inv, FishingObjRefs.fishing_bait, 1)

            player.opNpc2(npc)
            advance(ticks = 4)

            assertContains(player.inv, FishingObjRefs.raw_sardine)
            assertEquals(200, player.statMap.getXP(stats.fishing))
        }

    @Test
    fun GameTestState.`click fishing spot without equipment sends message`() =
        runGameTest(Fishing::class) {
            val npcType = npcTypes.values.first { it.isContentType(content.fishing_spot) }
            val npc = spawnNpc(CoordGrid(0, 50, 50, 32, 32), npcType)
            player.teleport(CoordGrid(0, 50, 50, 32, 33))
            player.clearInv()
            player.stats[stats.fishing] = 1

            player.opNpc2(npc)
            advance(ticks = 1)

            assertMessageSent("You need the correct fishing equipment to fish here.")
        }

    @Test
    fun GameTestState.`click fishing spot with full inventory sends message`() =
        runGameTest(Fishing::class) {
            val npcType = npcTypes.values.first { it.isContentType(content.fishing_spot) }
            val npc = spawnNpc(CoordGrid(0, 50, 50, 32, 32), npcType)
            player.teleport(CoordGrid(0, 50, 50, 32, 33))
            player.fillInv()
            player.stats[stats.fishing] = 1

            invAdd(player.inv, FishingObjRefs.small_fishing_net, 1)

            player.opNpc2(npc)
            advance(ticks = 1)

            assertMessageSent("Your inventory is too full to hold any more raw shrimps.")
        }

    @Test
    fun GameTestState.`cannot click non-fishing-spot npc`() =
        runGameTest(Fishing::class) {
            val npcType = npcTypes.values.first {
                !it.isContentType(content.fishing_spot)
            }
            val npc = spawnNpc(CoordGrid(0, 50, 50, 32, 32), npcType)
            player.teleport(CoordGrid(0, 50, 50, 32, 33))

            player.opNpc1(npc)
            advance(ticks = 1)

            assertMessageSent("Nothing interesting happens.")
        }
}
```

---

## TDD Approach

### Red Phase (Write Tests First)
1. Write `FishingConfigTest` — verify all fishing spots have `content.fishing_spot` group and params
2. Write `FishingTest` — verify fishing works at level 1, respects equipment, respects level reqs

### Green Phase (Implement to Pass)
1. Create `FishingModule.kt` — module registration
2. Create `FishingLevelBoosts.kt` — placeholder level boosts
3. Create `FishingObjRefs.kt` — item references
4. Create `FishingNpcRefs.kt` — NPC references
5. Create `FishingNpcEditor.kt` — mark NPCs with content group
6. Create `FishingParams.kt` — param aliases
7. Create `FishingSpotType.kt` — spot type enum
8. Create `FishingFish.kt` — fish data enum
9. Create `FishingSpot.kt` — spot definitions
10. Create `Fishing.kt` — core script

### Refactor Phase
- Verify all tests pass
- Run `./gradlew test` from project root
- Clean up any code smells

---

## Catch Rate Formula

OSRS fishing success rate (simplified):
```
success_chance = 0.25 + (level_diff * 0.005)
```

Where `level_diff = player_level - fish_level`

Example: Shrimps at level 1:
- level_diff = 1 - 1 = 0
- success_chance = 0.25 (25%)

Example: Shrimps at level 20:
- level_diff = 20 - 1 = 19
- success_chance = 0.25 + (19 * 0.005) = 0.345 (34.5%)

**MVP Decision**: Always succeed (simplified for testing). Real implementation would use `statRandom()` with catch rate formula.

---

## Test Summary

| Test | What it verifies |
|------|-----------------|
| `ensure all fishing spots have content group` | Config correctness |
| `ensure all fishing spots have spot type param` | Param correctness |
| `click fishing spot at level 1 with net catches shrimps` | Net fishing |
| `click fishing spot at level 1 with rod catches sardine` | Bait fishing |
| `click fishing spot without equipment sends message` | Equipment check |
| `click fishing spot with full inventory sends message` | Inventory check |
| `cannot click non-fishing-spot npc` | Content group check |

**Target**: 7+ tests, all passing

---

## Key Implementation Details

### NPC Interaction
- Fishing spots are NPCs with options (Net, Bait, Lure, Cage, Harpoon)
- `onOpNpc1` handles first click (show options)
- `onOpNpc2` handles second click (start fishing)
- Need to determine which option based on player's equipment

### Equipment Detection
- Check player's inventory for correct equipment
- Different fish require different equipment
- Equipment determines which fishing method is used

### Fishing Timer
- After clicking, start a timer (2-5 ticks)
- On timer complete, attempt to catch fish
- If successful, add fish to inventory and award XP
- If failed, show "You fail to catch anything" message

### Spot Movement
- OSRS fishing spots move every 250-530 ticks
- For MVP, spots are stationary
- Future: implement spot movement with timers

---

## Key File Locations

| File | Path |
|------|------|
| Module | `content/skills/fishing/src/main/kotlin/.../FishingModule.kt` |
| Script | `content/skills/fishing/src/main/kotlin/.../scripts/Fishing.kt` |
| Config | `content/skills/fishing/src/main/kotlin/.../configs/FishingNpcEditor.kt` |
| Tests | `content/skills/fishing/src/integration/kotlin/.../scripts/FishingTest.kt` |
| Build | `content/skills/fishing/build.gradle.kts` |

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
- OSRS Wiki Fishing: https://oldschool.runescape.wiki/w/Fishing
- BaseParams: `api/config/src/main/kotlin/.../BaseParams.kt`
- BaseContent: `api/config/src/main/kotlin/.../BaseContent.kt`
- BaseStats: `api/config/src/main/kotlin/.../BaseStats.kt`
- NPC Cache: `api/cache-enricher/src/main/resources/.../npc/npcs.toml`
