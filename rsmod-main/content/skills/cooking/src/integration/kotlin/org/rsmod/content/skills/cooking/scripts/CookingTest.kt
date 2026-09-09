package org.rsmod.content.skills.cooking.scripts

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.content
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.skills.cooking.configs.CookingObjRefs

class CookingTest {
    @Test
    fun GameTestState.`cooking food enum maps raw shrimps correctly`() = runBasicGameTest {
        val food = CookingFood.fromRaw(CookingObjRefs.raw_shrimps)
        assertNotNull(food)
        val f = food!!
        assertEquals(1, f.level)
        assertEquals(30.0, f.xp)
        assertEquals(34, f.burnLevel)
        assertEquals(31, f.burnRangeLevel)
        assertEquals(31, f.burnLumbridgeLevel)
    }

    @Test
    fun GameTestState.`cooking food enum maps all raw fish`() = runBasicGameTest {
        val raws =
            listOf(
                CookingObjRefs.raw_shrimps,
                CookingObjRefs.raw_anchovies,
                CookingObjRefs.raw_sardine,
                CookingObjRefs.raw_herring,
                CookingObjRefs.raw_trout,
                CookingObjRefs.raw_pike,
                CookingObjRefs.raw_salmon,
                CookingObjRefs.raw_tuna,
                CookingObjRefs.raw_lobster,
                CookingObjRefs.raw_swordfish,
            )
        for (raw in raws) {
            assertNotNull(CookingFood.fromRaw(raw), "Missing food entry for $raw")
        }
    }

    @Test
    fun GameTestState.`cooking food enum returns null for non-food item`() = runBasicGameTest {
        val food = CookingFood.fromRaw(CookingObjRefs.shrimps)
        assertNull(food)
    }

    @Test
    fun GameTestState.`ensure cooking ranges exist in cache`() = runBasicGameTest {
        val ranges = cacheTypes.locs.values.filter { it.isContentType(content.cooking_range) }
        assertTrue(ranges.isNotEmpty(), "No cooking ranges found in cache")
    }

    @Test
    fun GameTestState.`cooking range has correct content group`() = runBasicGameTest {
        val ranges = cacheTypes.locs.values.filter { it.isContentType(content.cooking_range) }
        for (range in ranges) {
            assertTrue(range.isContentType(content.cooking_range))
        }
    }
}