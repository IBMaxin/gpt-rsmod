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
        val testCases =
            listOf(
                Triple("cow", 1, 80),
                Triple("goblin", 1, 64),
                Triple("chicken", 1, 10),
                Triple("rat", 1, 15),
            )
        for ((name, levelReq, xp) in testCases) {
            val baseType = cacheTypes.npcs.values.first { it.internalName == name }

            val editorType =
                NpcPluginBuilder(name)
                    .apply {
                        param[SlayerParams.levelrequire] = levelReq
                        param[SlayerParams.experience] = xp
                    }
                    .build(id = -1)

            val merged = NpcTypeBuilder.merge(editorType, baseType)

            val npcParams = merged.paramMap
            assertNotNullContract(npcParams)
            assertTrue(SlayerParams.levelrequire in npcParams)
            assertTrue(SlayerParams.experience in npcParams)
            assertEquals(levelReq, npcParams[SlayerParams.levelrequire])
            assertEquals(xp, npcParams[SlayerParams.experience])
        }
    }
}
