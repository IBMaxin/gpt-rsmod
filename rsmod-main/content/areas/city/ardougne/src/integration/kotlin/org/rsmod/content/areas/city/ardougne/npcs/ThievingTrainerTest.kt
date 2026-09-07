package org.rsmod.content.areas.city.ardougne.npcs

import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.areas.city.ardougne.configs.ardougne_npcs
import org.rsmod.map.CoordGrid

class ThievingTrainerTest {

    @Test
    fun GameTestState.`talking to trainer opens dialogue`() =
        runGameTest(ThievingTrainer::class) {
            val guardType = npcTypes[ardougne_npcs.guard]
            guardType.op[0] = "Talk-to"
            val npc = spawnNpc(CoordGrid(0, 50, 50, 30, 30), guardType)
            player.teleport(CoordGrid(0, 50, 50, 30, 31))
            player.opNpc1(npc)
            advance(ticks = 1)
            assertModalOpen(interfaces.chat_left)
        }
}
