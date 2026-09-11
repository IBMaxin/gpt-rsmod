package org.rsmod.content.skills.slayer

import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.stats
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.skills.slayer.configs.SlayerNpcRefs
import org.rsmod.content.skills.slayer.configs.slayer_varps
import org.rsmod.content.skills.slayer.scripts.SlayerMaster
import org.rsmod.map.CoordGrid

class SlayerMasterTest {
    @Test
    fun GameTestState.`talk to turael assigns cow task`() =
        runGameTest(SlayerMaster::class) {
            val masterType = npcTypes[SlayerNpcRefs.master]
            val master = spawnNpc(CoordGrid(0, 50, 50, 32, 32), masterType)
            player.teleport(CoordGrid(0, 50, 50, 32, 33))

            player.opNpc1(master)
            advance(ticks = 1)

            val cowId = SlayerNpcRefs.cow.id
            assertEquals(cowId, player.vars[slayer_varps.slayer_target])
            assertEquals(5, player.vars[slayer_varps.slayer_count])
            assertMessageSent("Your new task is to kill 5 cows.")
        }

    @Test
    fun GameTestState.`talk to turael with active task shows current task`() =
        runGameTest(SlayerMaster::class) {
            val masterType = npcTypes[SlayerNpcRefs.master]
            val master = spawnNpc(CoordGrid(0, 50, 50, 32, 32), masterType)
            player.teleport(CoordGrid(0, 50, 50, 32, 33))

            // Assign a task first
            player.opNpc1(master)
            advance(ticks = 1)

            // Talk again — should show existing task
            player.opNpc1(master)
            advance(ticks = 1)

            assertEquals(5, player.vars[slayer_varps.slayer_count])
            assertMessageSent("You already have a slayer task. Kill 5 more to complete it.")
        }

    @Test
    fun GameTestState.`talk to turael with level 0 gets rejected`() =
        runGameTest(SlayerMaster::class) {
            val masterType = npcTypes[SlayerNpcRefs.master]
            val master = spawnNpc(CoordGrid(0, 50, 50, 32, 32), masterType)
            player.teleport(CoordGrid(0, 50, 50, 32, 33))
            player.stats[stats.slayer] = 0

            player.opNpc1(master)
            advance(ticks = 1)

            assertMessageSent("You need a Slayer level of at least 1 to get a task.")
            assertEquals(0, player.vars[slayer_varps.slayer_target])
            assertEquals(0, player.vars[slayer_varps.slayer_count])
        }
}
