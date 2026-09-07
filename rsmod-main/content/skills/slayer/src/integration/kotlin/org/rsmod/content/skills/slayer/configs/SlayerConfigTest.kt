package org.rsmod.content.skills.slayer.configs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.params
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.assertions.assertNotNullContract

class SlayerConfigTest {
    @Test
    fun GameTestState.`ensure all person npcs have slayer params`() = runBasicGameTest {
        val people = cacheTypes.npcs.values.filter { it.isContentType(content.person) }
        for (npc in people) {
            val npcParams = npc.paramMap
            assertNotNullContract(npcParams)
            assertTrue(params.slayer_levelrequire in npcParams)
            assertTrue(params.slayer_experience in npcParams)
            // Default values as set in the editor
            assertEquals(1, npcParams[params.slayer_levelrequire])
            assertEquals(8, npcParams[params.slayer_experience])
        }
    }
}
