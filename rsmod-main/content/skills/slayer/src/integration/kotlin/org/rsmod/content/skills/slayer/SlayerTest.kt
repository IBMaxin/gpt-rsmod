package org.rsmod.content.skills.slayer

import org.junit.jupiter.api.Test
import org.rsmod.api.npc.queueDeath
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.skills.slayer.configs.SlayerNpcRefs
import org.rsmod.content.skills.slayer.configs.slayer_varps
import org.rsmod.content.skills.slayer.scripts.Slayer
import org.rsmod.content.skills.slayer.scripts.SlayerMaster
import org.rsmod.map.CoordGrid

class SlayerTest {
    @Test
    fun GameTestState.`killing task npc decrements slayer count`() =
        runGameTest(SlayerMaster::class, Slayer::class) {
            val masterType = npcTypes[SlayerNpcRefs.master]
            val master = spawnNpc(CoordGrid(0, 50, 50, 32, 32), masterType)
            player.teleport(CoordGrid(0, 50, 50, 32, 33))

            // Assign cow task
            player.opNpc1(master)
            advance(ticks = 1)
            assertEquals(5, player.vars[slayer_varps.slayer_count])

            // Spawn and kill a cow
            val cowType = npcTypes[SlayerNpcRefs.cow]
            val cow = spawnNpc(CoordGrid(0, 50, 50, 34, 32), cowType)
            cow.hitpoints = 0
            cow.heroPoints(player, 1)
            cow.queueDeath()
            advance(ticks = 1)

            assertEquals(4, player.vars[slayer_varps.slayer_count])
        }

    @Test
    fun GameTestState.`killing wrong npc does not affect slayer count`() =
        runGameTest(SlayerMaster::class, Slayer::class) {
            val masterType = npcTypes[SlayerNpcRefs.master]
            val master = spawnNpc(CoordGrid(0, 50, 50, 32, 32), masterType)
            player.teleport(CoordGrid(0, 50, 50, 32, 33))

            // Assign cow task
            player.opNpc1(master)
            advance(ticks = 1)
            assertEquals(5, player.vars[slayer_varps.slayer_count])

            // Spawn and kill a goblin (not the task NPC)
            val goblinType = npcTypes[SlayerNpcRefs.goblin]
            val goblin = spawnNpc(CoordGrid(0, 50, 50, 34, 32), goblinType)
            goblin.hitpoints = 0
            goblin.heroPoints(player, 1)
            goblin.queueDeath()
            advance(ticks = 1)

            // Count should be unchanged
            assertEquals(5, player.vars[slayer_varps.slayer_count])
        }

    @Test
    fun GameTestState.`completing task resets varps and sends message`() =
        runGameTest(SlayerMaster::class, Slayer::class) {
            val masterType = npcTypes[SlayerNpcRefs.master]
            val master = spawnNpc(CoordGrid(0, 50, 50, 32, 32), masterType)
            player.teleport(CoordGrid(0, 50, 50, 32, 33))

            // Assign cow task
            player.opNpc1(master)
            advance(ticks = 1)

            // Set count to 1 so next kill completes the task
            player.setVarp(slayer_varps.slayer_count, 1)

            // Spawn and kill the cow
            val cowType = npcTypes[SlayerNpcRefs.cow]
            val cow = spawnNpc(CoordGrid(0, 50, 50, 34, 32), cowType)
            cow.hitpoints = 0
            cow.heroPoints(player, 1)
            cow.queueDeath()
            advance(ticks = 1)

            // Task should be complete: both varps reset
            assertEquals(0, player.vars[slayer_varps.slayer_target])
            assertEquals(0, player.vars[slayer_varps.slayer_count])
            assertMessageSent("You've completed your slayer task! Visit a slayer master for a new one.")
        }
}
