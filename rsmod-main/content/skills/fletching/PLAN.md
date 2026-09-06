# Fletching Skill Implementation Plan

## Overview

Use knife on logs → unstrung bows + arrow shafts. Use bowstring on unstrung bow → strung bow. Use feathers + arrow shafts → headless arrows. Use headless arrows + arrowtips → arrows.

**Mechanic**: Processing skill with multiple recipes
**XP Rate**: Dev realm 150x
**Scope**: Bows + arrows only (MVP)

---

## Module Structure

```
content/skills/fletching/
├── build.gradle.kts
├── PLAN.md                             # This file
├── src/main/kotlin/org/rsmod/content/skills/fletching/
│   ├── FletchingModule.kt              # PluginModule + InvisibleLevelMod binding
│   ├── FletchingLevelBoosts.kt         # InvisibleLevelMod (fletching cape)
│   ├── scripts/
│   │   ├── FletchingBow.kt            # Knife + log → unstrung bow
│   │   ├── FletchingBowString.kt      # Bowstring + unstrung → strung bow
│   │   ├── FletchingArrowShaft.kt     # Knife + log → arrow shafts
│   │   ├── FletchingHeadlessArrow.kt  # Shafts + feathers → headless arrows
│   │   └── FletchingArrow.kt          # Headless arrows + arrowtips → arrows
│   └── configs/
│       ├── FletchingObjRefs.kt         # find() for all fletching items
│       └── FletchingParams.kt          # Fletch-specific params
├── src/integration/kotlin/org/rsmod/content/skills/fletching/
│   ├── configs/
│   │   └── FletchingConfigTest.kt      # Verify item refs exist
│   └── scripts/
│       └── FletchingTest.kt            # Script integration tests
```

---

## Kotlin Codebase Patterns

### 1. Module Registration (`FletchingModule.kt`)

```kotlin
package org.rsmod.content.skills.fletching

import org.rsmod.api.stats.levelmod.InvisibleLevelMod
import org.rsmod.plugin.module.PluginModule

class FletchingModule : PluginModule() {
    override fun bind() {
        addSetBinding<InvisibleLevelMod>(FletchingLevelBoosts::class.java)
    }
}
```

### 2. Level Boosts (`FletchingLevelBoosts.kt`)

```kotlin
package org.rsmod.content.skills.fletching

import org.rsmod.api.config.refs.stats
import org.rsmod.api.stats.levelmod.InvisibleLevelMod
import org.rsmod.game.entity.Player

class FletchingLevelBoosts : InvisibleLevelMod(stats.fletching) {
    override fun Player.calculateBoost(): Int {
        // TODO: Fletching cape (level 99)
        return 0
    }
}
```

### 3. Item References (`FletchingObjRefs.kt`)

```kotlin
package org.rsmod.content.skills.fletching.configs

import org.rsmod.api.type.refs.obj.ObjReferences

internal object FletchingObjRefs : ObjReferences() {
    // Tools
    val knife = find("knife")

    // Logs (reference from BaseObjs)
    val logs = find("logs")
    val oak_logs = find("oak_logs")
    val willow_logs = find("willow_logs")
    val maple_logs = find("maple_logs")
    val yew_logs = find("yew_logs")
    val magic_logs = find("magic_logs")

    // Bow string
    val bow_string = find("bow_string")

    // Arrow components
    val arrow_shaft = find("arrow_shaft")
    val feather = find("feather")
    val headless_arrow = find("headless_arrow")

    // Arrowtips
    val bronze_arrowtips = find("bronze_arrowtips")
    val iron_arrowtips = find("iron_arrowtips")
    val steel_arrowtips = find("steel_arrowtips")
    val mithril_arrowtips = find("mithril_arrowtips")
    val adamant_arrowtips = find("adamant_arrowtips")
    val rune_arrowtips = find("rune_arrowtips")

    // Arrows (finished)
    val bronze_arrow = find("bronze_arrow")
    val iron_arrow = find("iron_arrow")
    val steel_arrow = find("steel_arrow")
    val mithril_arrow = find("mithril_arrow")
    val adamant_arrow = find("adamant_arrow")
    val rune_arrow = find("rune_arrow")

    // Unstrung bows
    val shortbow_u = find("shortbow_u")
    val longbow_u = find("longbow_u")
    val oak_shortbow_u = find("oak_shortbow_u")
    val oak_longbow_u = find("oak_longbow_u")
    val willow_shortbow_u = find("willow_shortbow_u")
    val willow_longbow_u = find("willow_longbow_u")
    val maple_shortbow_u = find("maple_shortbow_u")
    val maple_longbow_u = find("maple_longbow_u")
    val yew_shortbow_u = find("yew_shortbow_u")
    val yew_longbow_u = find("yew_longbow_u")
    val magic_shortbow_u = find("magic_shortbow_u")
    val magic_longbow_u = find("magic_longbow_u")

    // Strung bows (finished)
    val shortbow = find("shortbow")
    val longbow = find("longbow")
    val oak_shortbow = find("oak_shortbow")
    val oak_longbow = find("oak_longbow")
    val willow_shortbow = find("willow_shortbow")
    val willow_longbow = find("willow_longbow")
    val maple_shortbow = find("maple_shortbow")
    val maple_longbow = find("maple_longbow")
    val yew_shortbow = find("yew_shortbow")
    val yew_longbow = find("yew_longbow")
    val magic_shortbow = find("magic_shortbow")
    val magic_longbow = find("magic_longbow")
}
```

### 4. Params (`FletchingParams.kt`)

```kotlin
package org.rsmod.content.skills.fletching.configs

import org.rsmod.api.config.refs.params

object FletchingParams {
    val levelrequire = params.levelrequire
    val skill_xp = params.skill_xp
    val skill_productitem = params.skill_productitem
}
```

### 5. Bow Recipes Enum (`FletchingBowRecipe.kt`)

```kotlin
package org.rsmod.content.skills.fletching.scripts

import org.rsmod.api.type.refs.obj.ObjType

enum class FletchingBowRecipe(
    val level: Int,
    val log: ObjType,
    val unstrung: ObjType,
    val strung: ObjType,
    val unstrungXp: Double,
    val strungXp: Double,
) {
    SHORTBOW(5, FletchingObjRefs.logs, FletchingObjRefs.shortbow_u, FletchingObjRefs.shortbow, 5.0, 5.0),
    LONGBOW(10, FletchingObjRefs.logs, FletchingObjRefs.longbow_u, FletchingObjRefs.longbow, 10.0, 10.0),
    OAK_SHORTBOW(20, FletchingObjRefs.oak_logs, FletchingObjRefs.oak_shortbow_u, FletchingObjRefs.oak_shortbow, 16.5, 16.5),
    OAK_LONGBOW(25, FletchingObjRefs.oak_logs, FletchingObjRefs.oak_longbow_u, FletchingObjRefs.oak_longbow, 25.0, 25.0),
    WILLOW_SHORTBOW(35, FletchingObjRefs.willow_logs, FletchingObjRefs.willow_shortbow_u, FletchingObjRefs.willow_shortbow, 33.3, 33.2),
    WILLOW_LONGBOW(40, FletchingObjRefs.willow_logs, FletchingObjRefs.willow_longbow_u, FletchingObjRefs.willow_longbow, 41.5, 41.5),
    MAPLE_SHORTBOW(50, FletchingObjRefs.maple_logs, FletchingObjRefs.maple_shortbow_u, FletchingObjRefs.maple_shortbow, 50.0, 50.0),
    MAPLE_LONGBOW(55, FletchingObjRefs.maple_logs, FletchingObjRefs.maple_longbow_u, FletchingObjRefs.maple_longbow, 58.3, 58.2),
    YEW_SHORTBOW(65, FletchingObjRefs.yew_logs, FletchingObjRefs.yew_shortbow_u, FletchingObjRefs.yew_shortbow, 67.5, 67.5),
    YEW_LONGBOW(70, FletchingObjRefs.yew_logs, FletchingObjRefs.yew_longbow_u, FletchingObjRefs.yew_longbow, 75.0, 75.0),
    MAGIC_SHORTBOW(80, FletchingObjRefs.magic_logs, FletchingObjRefs.magic_shortbow_u, FletchingObjRefs.magic_shortbow, 83.3, 83.2),
    MAGIC_LONGBOW(85, FletchingObjRefs.magic_logs, FletchingObjRefs.magic_longbow_u, FletchingObjRefs.magic_longbow, 91.5, 91.5);

    companion object {
        fun fromLog(log: ObjType): List<FletchingBowRecipe> =
            entries.filter { it.log == log }
    }
}
```

### 6. Arrow Recipes Enum (`FletchingArrowRecipe.kt`)

```kotlin
package org.rsmod.content.skills.fletching.scripts

import org.rsmod.api.type.refs.obj.ObjType

enum class FletchingArrowRecipe(
    val level: Int,
    val arrowtips: ObjType,
    val arrow: ObjType,
    val xpPerArrow: Double,
    val batchSize: Int,
) {
    BRONZE(1, FletchingObjRefs.bronze_arrowtips, FletchingObjRefs.bronze_arrow, 1.3, 15),
    IRON(15, FletchingObjRefs.iron_arrowtips, FletchingObjRefs.iron_arrow, 2.5, 15),
    STEEL(30, FletchingObjRefs.steel_arrowtips, FletchingObjRefs.steel_arrow, 5.0, 15),
    MITHRIL(45, FletchingObjRefs.mithril_arrowtips, FletchingObjRefs.mithril_arrow, 7.5, 15),
    ADAMANT(60, FletchingObjRefs.adamant_arrowtips, FletchingObjRefs.adamant_arrow, 10.0, 15),
    RUNE(75, FletchingObjRefs.rune_arrowtips, FletchingObjRefs.rune_arrow, 12.5, 15);

    companion object {
        fun fromArrowtips(arrowtips: ObjType): FletchingArrowRecipe? =
            entries.find { it.arrowtips == arrowtips }
    }
}
```

### 7. Arrow Shaft Recipes Enum (`FletchingShaftRecipe.kt`)

```kotlin
package org.rsmod.content.skills.fletching.scripts

import org.rsmod.api.type.refs.obj.ObjType

enum class FletchingShaftRecipe(
    val level: Int,
    val log: ObjType,
    val shafts: Int,
    val xp: Double,
) {
    REGULAR(1, FletchingObjRefs.logs, 15, 5.0),
    OAK(15, FletchingObjRefs.oak_logs, 30, 10.0),
    WILLOW(30, FletchingObjRefs.willow_logs, 45, 15.0),
    MAPLE(45, FletchingObjRefs.maple_logs, 60, 20.0),
    YEW(60, FletchingObjRefs.yew_logs, 75, 25.0),
    MAGIC(75, FletchingObjRefs.magic_logs, 90, 30.0);

    companion object {
        fun fromLog(log: ObjType): FletchingShaftRecipe? =
            entries.find { it.log == log }
    }
}
```

### 8. Core Script — Bow Making (`FletchingBow.kt`)

```kotlin
package org.rsmod.content.skills.fletching.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.refs.stats
import org.rsmod.api.invtx.invAdd
import org.rsmod.api.invtx.invRemove
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.statAdvance
import org.rsmod.api.script.onUseObjU
import org.rsmod.game.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class FletchingBow @Inject constructor() : PluginScript() {
    override fun ScriptContext.startup() {
        // Knife on logs → unstrung bow
        onUseObjU(FletchingObjRefs.knife, FletchingObjRefs.logs) { makeBow(it.obj) }
        onUseObjU(FletchingObjRefs.knife, FletchingObjRefs.oak_logs) { makeBow(it.obj) }
        onUseObjU(FletchingObjRefs.knife, FletchingObjRefs.willow_logs) { makeBow(it.obj) }
        onUseObjU(FletchingObjRefs.knife, FletchingObjRefs.maple_logs) { makeBow(it.obj) }
        onUseObjU(FletchingObjRefs.knife, FletchingObjRefs.yew_logs) { makeBow(it.obj) }
        onUseObjU(FletchingObjRefs.knife, FletchingObjRefs.magic_logs) { makeBow(it.obj) }
    }

    private suspend fun ProtectedAccess.makeBow(log: ObjType) {
        val recipes = FletchingBowRecipe.fromLog(log)
        if (recipes.isEmpty()) return

        // Pick the first recipe (shortbow by default)
        // Real implementation would show menu to choose shortbow/longbow
        val recipe = recipes.first()
        val playerLevel = stat(stats.fletching)

        if (playerLevel < recipe.level) {
            mes("You need a Fletching level of ${recipe.level} to make ${recipe.unstrung.name.lowercase()}.")
            return
        }

        if (!inv.contains(log)) {
            mes("You need a ${log.name.lowercase()} to do this.")
            return
        }

        actionDelay = 3

        invRemove(inv, log, 1)
        invAdd(inv, recipe.unstrung, 1)
        statAdvance(stats.fletching, recipe.unstrungXp)

        mes("You carve the ${log.name.lowercase()} into ${recipe.unstrung.name.lowercase()}.")
    }
}
```

### 9. Core Script — Bow Stringing (`FletchingBowString.kt`)

```kotlin
package org.rsmod.content.skills.fletching.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.refs.stats
import org.rsmod.api.invtx.invAdd
import org.rsmod.api.invtx.invRemove
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.statAdvance
import org.rsmod.api.script.onUseObjU
import org.rsmod.game.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class FletchingBowString @Inject constructor() : PluginScript() {
    override fun ScriptContext.startup() {
        // Bow string on unstrung bow → strung bow
        onUseObjU(FletchingObjRefs.bow_string, FletchingObjRefs.shortbow_u) { stringBow(it.obj) }
        onUseObjU(FletchingObjRefs.bow_string, FletchingObjRefs.longbow_u) { stringBow(it.obj) }
        onUseObjU(FletchingObjRefs.bow_string, FletchingObjRefs.oak_shortbow_u) { stringBow(it.obj) }
        onUseObjU(FletchingObjRefs.bow_string, FletchingObjRefs.oak_longbow_u) { stringBow(it.obj) }
        onUseObjU(FletchingObjRefs.bow_string, FletchingObjRefs.willow_shortbow_u) { stringBow(it.obj) }
        onUseObjU(FletchingObjRefs.bow_string, FletchingObjRefs.willow_longbow_u) { stringBow(it.obj) }
        onUseObjU(FletchingObjRefs.bow_string, FletchingObjRefs.maple_shortbow_u) { stringBow(it.obj) }
        onUseObjU(FletchingObjRefs.bow_string, FletchingObjRefs.maple_longbow_u) { stringBow(it.obj) }
        onUseObjU(FletchingObjRefs.bow_string, FletchingObjRefs.yew_shortbow_u) { stringBow(it.obj) }
        onUseObjU(FletchingObjRefs.bow_string, FletchingObjRefs.yew_longbow_u) { stringBow(it.obj) }
        onUseObjU(FletchingObjRefs.bow_string, FletchingObjRefs.magic_shortbow_u) { stringBow(it.obj) }
        onUseObjU(FletchingObjRefs.bow_string, FletchingObjRefs.magic_longbow_u) { stringBow(it.obj) }
    }

    private suspend fun ProtectedAccess.stringBow(unstrung: ObjType) {
        val recipe = FletchingBowRecipe.entries.find { it.unstrung == unstrung } ?: return
        val playerLevel = stat(stats.fletching)

        if (playerLevel < recipe.level) {
            mes("You need a Fletching level of ${recipe.level} to string this bow.")
            return
        }

        if (!inv.contains(unstrung)) {
            mes("You need an ${unstrung.name.lowercase()} to do this.")
            return
        }

        if (!inv.contains(FletchingObjRefs.bow_string)) {
            mes("You need a bow string to do this.")
            return
        }

        actionDelay = 3

        invRemove(inv, unstrung, 1)
        invRemove(inv, FletchingObjRefs.bow_string, 1)
        invAdd(inv, recipe.strung, 1)
        statAdvance(stats.fletching, recipe.strungXp)

        mes("You string the ${unstrung.name.lowercase()} into ${recipe.strung.name.lowercase()}.")
    }
}
```

### 10. Core Script — Arrow Shafts (`FletchingArrowShaft.kt`)

```kotlin
package org.rsmod.content.skills.fletching.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.refs.stats
import org.rsmod.api.invtx.invAdd
import org.rsmod.api.invtx.invRemove
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.statAdvance
import org.rsmod.api.script.onUseObjU
import org.rsmod.game.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class FletchingArrowShaft @Inject constructor() : PluginScript() {
    override fun ScriptContext.startup() {
        // Knife on logs → arrow shafts
        onUseObjU(FletchingObjRefs.knife, FletchingObjRefs.logs) { makeShafts(it.obj) }
        onUseObjU(FletchingObjRefs.knife, FletchingObjRefs.oak_logs) { makeShafts(it.obj) }
        onUseObjU(FletchingObjRefs.knife, FletchingObjRefs.willow_logs) { makeShafts(it.obj) }
        onUseObjU(FletchingObjRefs.knife, FletchingObjRefs.maple_logs) { makeShafts(it.obj) }
        onUseObjU(FletchingObjRefs.knife, FletchingObjRefs.yew_logs) { makeShafts(it.obj) }
        onUseObjU(FletchingObjRefs.knife, FletchingObjRefs.magic_logs) { makeShafts(it.obj) }
    }

    private suspend fun ProtectedAccess.makeShafts(log: ObjType) {
        // This conflicts with bow making — need to handle both
        // Real implementation would show menu: "What would you like to make?"
        // For MVP, we'll skip this and focus on bows
        // Arrow shafts will be handled separately
    }
}
```

### 11. Core Script — Headless Arrows (`FletchingHeadlessArrow.kt`)

```kotlin
package org.rsmod.content.skills.fletching.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.refs.stats
import org.rsmod.api.invtx.invAdd
import org.rsmod.api.invtx.invRemove
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.statAdvance
import org.rsmod.api.script.onUseObjU
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class FletchingHeadlessArrow @Inject constructor() : PluginScript() {
    override fun ScriptContext.startup() {
        // Feathers on arrow shafts → headless arrows
        onUseObjU(FletchingObjRefs.feather, FletchingObjRefs.arrow_shaft) { makeHeadless() }
    }

    private suspend fun ProtectedAccess.makeHeadless() {
        val playerLevel = stat(stats.fletching)

        if (playerLevel < 1) {
            mes("You need a Fletching level of 1 to make headless arrows.")
            return
        }

        if (!inv.contains(FletchingObjRefs.feather)) {
            mes("You need some feathers to do this.")
            return
        }

        if (!inv.contains(FletchingObjRefs.arrow_shaft)) {
            mes("You need some arrow shafts to do this.")
            return
        }

        // Check for 15 feathers and 15 shafts
        val feathers = inv.count(FletchingObjRefs.feather)
        val shafts = inv.count(FletchingObjRefs.arrow_shaft)
        val batchSize = minOf(15, feathers, shafts)

        if (batchSize < 15) {
            mes("You need 15 feathers and 15 arrow shafts to make headless arrows.")
            return
        }

        actionDelay = 3

        invRemove(inv, FletchingObjRefs.feather, 15)
        invRemove(inv, FletchingObjRefs.arrow_shaft, 15)
        invAdd(inv, FletchingObjRefs.headless_arrow, 15)
        statAdvance(stats.fletching, 15.0) // 1.0 XP per arrow * 15

        mes("You attach feathers to the arrow shafts, creating 15 headless arrows.")
    }
}
```

### 12. Core Script — Arrows (`FletchingArrow.kt`)

```kotlin
package org.rsmod.content.skills.fletching.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.refs.stats
import org.rsmod.api.invtx.invAdd
import org.rsmod.api.invtx.invRemove
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.statAdvance
import org.rsmod.api.script.onUseObjU
import org.rsmod.game.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class FletchingArrow @Inject constructor() : PluginScript() {
    override fun ScriptContext.startup() {
        // Headless arrows on arrowtips → arrows
        onUseObjU(FletchingObjRefs.headless_arrow, FletchingObjRefs.bronze_arrowtips) { makeArrow(it.obj) }
        onUseObjU(FletchingObjRefs.headless_arrow, FletchingObjRefs.iron_arrowtips) { makeArrow(it.obj) }
        onUseObjU(FletchingObjRefs.headless_arrow, FletchingObjRefs.steel_arrowtips) { makeArrow(it.obj) }
        onUseObjU(FletchingObjRefs.headless_arrow, FletchingObjRefs.mithril_arrowtips) { makeArrow(it.obj) }
        onUseObjU(FletchingObjRefs.headless_arrow, FletchingObjRefs.adamant_arrowtips) { makeArrow(it.obj) }
        onUseObjU(FletchingObjRefs.headless_arrow, FletchingObjRefs.rune_arrowtips) { makeArrow(it.obj) }
    }

    private suspend fun ProtectedAccess.makeArrow(arrowtips: ObjType) {
        val recipe = FletchingArrowRecipe.fromArrowtips(arrowtips) ?: return
        val playerLevel = stat(stats.fletching)

        if (playerLevel < recipe.level) {
            mes("You need a Fletching level of ${recipe.level} to make ${recipe.arrow.name.lowercase()}.")
            return
        }

        if (!inv.contains(FletchingObjRefs.headless_arrow)) {
            mes("You need some headless arrows to do this.")
            return
        }

        if (!inv.contains(arrowtips)) {
            mes("You need some ${arrowtips.name.lowercase()} to do this.")
            return
        }

        // Check for 15 headless arrows and 15 arrowtips
        val headless = inv.count(FletchingObjRefs.headless_arrow)
        val tips = inv.count(arrowtips)
        val batchSize = minOf(15, headless, tips)

        if (batchSize < 15) {
            mes("You need 15 headless arrows and 15 ${arrowtips.name.lowercase()} to make arrows.")
            return
        }

        actionDelay = 3

        invRemove(inv, FletchingObjRefs.headless_arrow, 15)
        invRemove(inv, arrowtips, 15)
        invAdd(inv, recipe.arrow, 15)
        statAdvance(stats.fletching, recipe.xpPerArrow * 15)

        mes("You attach ${arrowtips.name.lowercase()} to the headless arrows, creating 15 ${recipe.arrow.name.lowercase()}.")
    }
}
```

### 13. Config Test (`FletchingConfigTest.kt`)

```kotlin
package org.rsmod.content.skills.fletching.configs

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.testing.GameTestState

class FletchingConfigTest {
    @Test
    fun GameTestState.`ensure all fletching items exist in cache`() = runBasicGameTest {
        // Verify knife exists
        assertNotNull(cacheTypes.objs[FletchingObjRefs.knife.id])

        // Verify logs exist
        assertNotNull(cacheTypes.objs[FletchingObjRefs.logs.id])
        assertNotNull(cacheTypes.objs[FletchingObjRefs.oak_logs.id])

        // Verify bow string exists
        assertNotNull(cacheTypes.objs[FletchingObjRefs.bow_string.id])

        // Verify arrow components exist
        assertNotNull(cacheTypes.objs[FletchingObjRefs.arrow_shaft.id])
        assertNotNull(cacheTypes.objs[FletchingObjRefs.feather.id])
        assertNotNull(cacheTypes.objs[FletchingObjRefs.headless_arrow.id])

        // Verify arrowtips exist
        assertNotNull(cacheTypes.objs[FletchingObjRefs.bronze_arrowtips.id])
        assertNotNull(cacheTypes.objs[FletchingObjRefs.rune_arrowtips.id])

        // Verify arrows exist
        assertNotNull(cacheTypes.objs[FletchingObjRefs.bronze_arrow.id])
        assertNotNull(cacheTypes.objs[FletchingObjRefs.rune_arrow.id])

        // Verify unstrung bows exist
        assertNotNull(cacheTypes.objs[FletchingObjRefs.shortbow_u.id])
        assertNotNull(cacheTypes.objs[FletchingObjRefs.magic_longbow_u.id])

        // Verify strung bows exist
        assertNotNull(cacheTypes.objs[FletchingObjRefs.shortbow.id])
        assertNotNull(cacheTypes.objs[FletchingObjRefs.magic_longbow.id])
    }
}
```

### 14. Script Test (`FletchingTest.kt`)

```kotlin
package org.rsmod.content.skills.fletching.scripts

import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.stats
import org.rsmod.api.testing.GameTestState
import org.rsmod.map.CoordGrid

class FletchingTest {
    @Test
    fun GameTestState.`knife on logs at level 5 makes shortbow u`() =
        runGameTest(FletchingBow::class) {
            player.teleport(CoordGrid(0, 50, 50, 32, 32))
            player.clearInv()
            player.stats[stats.fletching] = 5

            invAdd(player.inv, FletchingObjRefs.knife, 1)
            invAdd(player.inv, FletchingObjRefs.logs, 1)

            player.useObjU(FletchingObjRefs.knife, FletchingObjRefs.logs)
            advance(ticks = 3)

            assertContains(player.inv, FletchingObjRefs.shortbow_u)
            assertEquals(50, player.statMap.getXP(stats.fletching)) // 5.0 XP * 10
        }

    @Test
    fun GameTestState.`knife on logs at level 1 sends level message`() =
        runGameTest(FletchingBow::class) {
            player.teleport(CoordGrid(0, 50, 50, 32, 32))
            player.clearInv()
            player.stats[stats.fletching] = 1

            invAdd(player.inv, FletchingObjRefs.knife, 1)
            invAdd(player.inv, FletchingObjRefs.logs, 1)

            player.useObjU(FletchingObjRefs.knife, FletchingObjRefs.logs)
            advance(ticks = 1)

            assertMessageSent("You need a Fletching level of 5 to make shortbow (u).")
        }

    @Test
    fun GameTestState.`bow string on shortbow u makes shortbow`() =
        runGameTest(FletchingBowString::class) {
            player.teleport(CoordGrid(0, 50, 50, 32, 32))
            player.clearInv()
            player.stats[stats.fletching] = 5

            invAdd(player.inv, FletchingObjRefs.shortbow_u, 1)
            invAdd(player.inv, FletchingObjRefs.bow_string, 1)

            player.useObjU(FletchingObjRefs.bow_string, FletchingObjRefs.shortbow_u)
            advance(ticks = 3)

            assertContains(player.inv, FletchingObjRefs.shortbow)
            assertEquals(50, player.statMap.getXP(stats.fletching))
        }

    @Test
    fun GameTestState.`headless arrows on bronze tips makes bronze arrows`() =
        runGameTest(FletchingArrow::class) {
            player.teleport(CoordGrid(0, 50, 50, 32, 32))
            player.clearInv()
            player.stats[stats.fletching] = 1

            invAdd(player.inv, FletchingObjRefs.headless_arrow, 15)
            invAdd(player.inv, FletchingObjRefs.bronze_arrowtips, 15)

            player.useObjU(FletchingObjRefs.headless_arrow, FletchingObjRefs.bronze_arrowtips)
            advance(ticks = 3)

            assertContains(player.inv, FletchingObjRefs.bronze_arrow)
            assertEquals(195, player.statMap.getXP(stats.fletching)) // 1.3 * 15 * 10
        }

    @Test
    fun GameTestState.`headless arrows on rune tips at level 75 makes rune arrows`() =
        runGameTest(FletchingArrow::class) {
            player.teleport(CoordGrid(0, 50, 50, 32, 32))
            player.clearInv()
            player.stats[stats.fletching] = 75

            invAdd(player.inv, FletchingObjRefs.headless_arrow, 15)
            invAdd(player.inv, FletchingObjRefs.rune_arrowtips, 15)

            player.useObjU(FletchingObjRefs.headless_arrow, FletchingObjRefs.rune_arrowtips)
            advance(ticks = 3)

            assertContains(player.inv, FletchingObjRefs.rune_arrow)
            assertEquals(1875, player.statMap.getXP(stats.fletching)) // 12.5 * 15 * 10
        }

    @Test
    fun GameTestState.`headless arrows on rune tips at level 74 sends level message`() =
        runGameTest(FletchingArrow::class) {
            player.teleport(CoordGrid(0, 50, 50, 32, 32))
            player.clearInv()
            player.stats[stats.fletching] = 74

            invAdd(player.inv, FletchingObjRefs.headless_arrow, 15)
            invAdd(player.inv, FletchingObjRefs.rune_arrowtips, 15)

            player.useObjU(FletchingObjRefs.headless_arrow, FletchingObjRefs.rune_arrowtips)
            advance(ticks = 1)

            assertMessageSent("You need a Fletching level of 75 to make rune arrows.")
        }

    @Test
    fun GameTestState.`knife on non-log does nothing`() =
        runGameTest(FletchingBow::class) {
            player.teleport(CoordGrid(0, 50, 50, 32, 32))
            player.clearInv()

            invAdd(player.inv, FletchingObjRefs.knife, 1)
            invAdd(player.inv, objs.coins, 1)

            player.useObjU(FletchingObjRefs.knife, objs.coins)
            advance(ticks = 1)

            assertDoesNotContain(player.inv, FletchingObjRefs.shortbow_u)
        }

    @Test
    fun GameTestState.`bow string on non-unstrung bow does nothing`() =
        runGameTest(FletchingBowString::class) {
            player.teleport(CoordGrid(0, 50, 50, 32, 32))
            player.clearInv()

            invAdd(player.inv, FletchingObjRefs.bow_string, 1)
            invAdd(player.inv, objs.coins, 1)

            player.useObjU(FletchingObjRefs.bow_string, objs.coins)
            advance(ticks = 1)

            assertDoesNotContain(player.inv, FletchingObjRefs.shortbow)
        }

    @Test
    fun GameTestState.`insufficient feathers sends message`() =
        runGameTest(FletchingHeadlessArrow::class) {
            player.teleport(CoordGrid(0, 50, 50, 32, 32))
            player.clearInv()
            player.stats[stats.fletching] = 1

            invAdd(player.inv, FletchingObjRefs.feather, 10) // Only 10, need 15
            invAdd(player.inv, FletchingObjRefs.arrow_shaft, 15)

            player.useObjU(FletchingObjRefs.feather, FletchingObjRefs.arrow_shaft)
            advance(ticks = 1)

            assertMessageSent("You need 15 feathers and 15 arrow shafts to make headless arrows.")
        }
}
```

---

## TDD Approach

### Red Phase (Write Tests First)
1. Write `FletchingConfigTest` — verify all fletching items exist in cache
2. Write `FletchingTest` — verify bow making, stringing, arrow making work

### Green Phase (Implement to Pass)
1. Create `FletchingModule.kt` — module registration
2. Create `FletchingLevelBoosts.kt` — placeholder level boosts
3. Create `FletchingObjRefs.kt` — item references
4. Create `FletchingParams.kt` — param aliases
5. Create `FletchingBowRecipe.kt` — bow recipe enum
6. Create `FletchingArrowRecipe.kt` — arrow recipe enum
7. Create `FletchingShaftRecipe.kt` — shaft recipe enum
8. Create `FletchingBow.kt` — bow making script
9. Create `FletchingBowString.kt` — bow stringing script
10. Create `FletchingArrowShaft.kt` — arrow shaft script (MVP: skip menu, focus on bows)
11. Create `FletchingHeadlessArrow.kt` — headless arrow script
12. Create `FletchingArrow.kt` — arrow making script

### Refactor Phase
- Verify all tests pass
- Run `./gradlew test` from project root
- Clean up any code smells

---

## Test Summary

| Test | What it verifies |
|------|-----------------|
| `ensure all fletching items exist in cache` | Config correctness |
| `knife on logs at level 5 makes shortbow u` | Bow making |
| `knife on logs at level 1 sends level message` | Level check |
| `bow string on shortbow u makes shortbow` | Bow stringing |
| `headless arrows on bronze tips makes bronze arrows` | Arrow making |
| `headless arrows on rune tips at level 75 makes rune arrows` | High-level arrows |
| `headless arrows on rune tips at level 74 sends level message` | Level check |
| `knife on non-log does nothing` | Wrong item check |
| `bow string on non-unstrung bow does nothing` | Wrong item check |
| `insufficient feathers sends message` | Quantity check |

**Target**: 10+ tests, all passing

---

## Key Implementation Details

### Recipe Selection
- Knife on logs shows menu: "What would you like to make?" → Shortbow / Longbow / Arrow Shafts
- For MVP, default to shortbow (first recipe)
- Future: implement menu selection

### Batch Processing
- Arrow making produces 15 arrows at a time
- Requires 15 headless arrows + 15 arrowtips
- If player has fewer than 15, use what they have (or require minimum)

### Item Conflicts
- Knife on logs can make bows OR arrow shafts
- Need to handle both cases with menu or priority
- For MVP, focus on bows only

### Inventory Management
- Remove input items before adding output
- Check inventory has space for output
- Check player has required items

---

## Key File Locations

| File | Path |
|------|------|
| Module | `content/skills/fletching/src/main/kotlin/.../FletchingModule.kt` |
| Scripts | `content/skills/fletching/src/main/kotlin/.../scripts/*.kt` |
| Config | `content/skills/fletching/src/main/kotlin/.../configs/FletchingObjRefs.kt` |
| Tests | `content/skills/fletching/src/integration/kotlin/.../scripts/FletchingTest.kt` |
| Build | `content/skills/fletching/build.gradle.kts` |

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
- OSRS Wiki Fletching: https://oldschool.runescape.wiki/w/Fletching
- BaseParams: `api/config/src/main/kotlin/.../BaseParams.kt`
- BaseContent: `api/config/src/main/kotlin/.../BaseContent.kt`
- BaseStats: `api/config/src/main/kotlin/.../BaseStats.kt`
