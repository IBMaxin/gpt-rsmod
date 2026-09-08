package org.rsmod.content.skills.slayer.configs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.assertions.assertNotNullContract
import org.rsmod.api.type.script.dsl.NpcPluginBuilder
import org.rsmod.game.type.npc.NpcTypeBuilder

class SlayerConfigTest {
    @Test
    fun GameTestState.`ensure slayer task npcs have slayer params`() = runBasicGameTest {
        val baseCow = cacheTypes.npcs.values.first { it.internalName == "cow" }

        val editorType =
            NpcPluginBuilder("cow")
                .apply {
                    param[SlayerParams.levelrequire] = 1
                    param[SlayerParams.experience] = 80
                }
                .build(id = -1)

        val merged = NpcTypeBuilder.merge(editorType, baseCow)

        val npcParams = merged.paramMap
        assertNotNullContract(npcParams)
        assertTrue(SlayerParams.levelrequire in npcParams)
        assertTrue(SlayerParams.experience in npcParams)
        assertEquals(1, npcParams[SlayerParams.levelrequire])
        assertEquals(80, npcParams[SlayerParams.experience])
    }
}
