package org.rsmod.content.skills.thieving.configs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.params
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.assertions.assertNotNullContract
import org.rsmod.api.utils.skills.SkillingSuccessRate

class ThievingConfigTest {
    @Test
    fun GameTestState.`ensure all person npcs have thieving params`() = runBasicGameTest {
        val people = cacheTypes.npcs.values.filter { it.isContentType(content.person) }
        for (npc in people) {
            val npcParams = npc.paramMap
            assertNotNullContract(npcParams)
            assertTrue(params.levelrequire in npcParams)
            assertTrue(params.skill_xp in npcParams)
            assertEquals(1, npcParams[params.levelrequire])
            assertEquals(8, npcParams[params.skill_xp])
        }
    }

    @Test
    fun GameTestState.`verify pickpocket success rate at level 1`() = runBasicGameTest {
        val rate = SkillingSuccessRate.successRate(low = 180, high = 240, level = 1, maxLevel = 99)
        // (1 + 180) / 256 = 181/256 = 0.70703125
        assertEquals(181.0 / 256.0, rate, 0.0001)
    }

    @Test
    fun GameTestState.`verify pickpocket success rate at level 99`() = runBasicGameTest {
        val rate = SkillingSuccessRate.successRate(low = 180, high = 240, level = 99, maxLevel = 99)
        // (1 + 240) / 256 = 241/256 = 0.94140625
        assertEquals(241.0 / 256.0, rate, 0.0001)
    }

    @Test
    fun GameTestState.`verify pickpocket success rate at level 85`() = runBasicGameTest {
        val rate = SkillingSuccessRate.successRate(low = 180, high = 240, level = 85, maxLevel = 99)
        // ~90.6%
        assertTrue(rate > 0.90)
        assertTrue(rate < 0.92)
    }

    @Test
    fun GameTestState.`verify pickpocket success rate scales with level`() = runBasicGameTest {
        val rate1 = SkillingSuccessRate.successRate(low = 180, high = 240, level = 1, maxLevel = 99)
        val rate50 =
            SkillingSuccessRate.successRate(low = 180, high = 240, level = 50, maxLevel = 99)
        val rate99 =
            SkillingSuccessRate.successRate(low = 180, high = 240, level = 99, maxLevel = 99)
        assertTrue(rate1 < rate50)
        assertTrue(rate50 < rate99)
    }
}
