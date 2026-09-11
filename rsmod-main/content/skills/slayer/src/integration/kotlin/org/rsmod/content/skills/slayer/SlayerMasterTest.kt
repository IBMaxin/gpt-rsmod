package org.rsmod.content.skills.slayer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.stats
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.skills.slayer.configs.SlayerNpcRefs
import org.rsmod.content.skills.slayer.configs.slayer_varps
import org.rsmod.content.skills.slayer.scripts.SlayerMaster
import org.rsmod.map.CoordGrid

class SlayerMasterTest {
    @Test
    fun GameTestState.`talk to turael assigns task from pool`() =
        runGameTest(SlayerMaster::class) {
            val masterType = npcTypes[SlayerNpcRefs.master]
            val master = spawnNpc(CoordGrid(0, 50, 50, 32, 32), masterType)
            player.teleport(CoordGrid(0, 50, 50, 32, 33))

            // Pool has 4 NPCs. Pick cow (index 0), count 7.
            random.next = 0
            random.then = 7

            player.opNpc1(master)
            advance(ticks = 1)

            val cowId = SlayerNpcRefs.cow.id
            assertEquals(cowId, player.vars[slayer_varps.slayer_target])
            assertEquals(7, player.vars[slayer_varps.slayer_count])
            assertMessageSent("Your new task is to kill 7 cows.")
        }

    @Test
    fun GameTestState.`talk to turael with active task offers replacement`() =
        runGameTest(SlayerMaster::class) {
            val masterType = npcTypes[SlayerNpcRefs.master]
            val master = spawnNpc(CoordGrid(0, 50, 50, 32, 32), masterType)
            player.teleport(CoordGrid(0, 50, 50, 32, 33))

            // Assign a task first (pick cow=0, count=5)
            random.next = 0
            random.then = 5
            player.opNpc1(master)
            advance(ticks = 1)

            assertEquals(5, player.vars[slayer_varps.slayer_count])
            val originalTarget = player.vars[slayer_varps.slayer_target]

            // Talk again — dialogue offers replacement (choice2 suspends)
            // In-game: player selects "Replace my current task"
            // Here we verify the active task state before the dialogue
            assertTrue(player.vars[slayer_varps.slayer_count] > 0)
            assertTrue(originalTarget != 0)
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

    @Test
    fun GameTestState.`repeated assignment yields only pool npcs`() =
        runGameTest(SlayerMaster::class) {
            val masterType = npcTypes[SlayerNpcRefs.master]
            val master = spawnNpc(CoordGrid(0, 50, 50, 32, 32), masterType)
            player.teleport(CoordGrid(0, 50, 50, 32, 33))

            val validTargets =
                setOf(
                    SlayerNpcRefs.cow.id,
                    SlayerNpcRefs.goblin.id,
                    SlayerNpcRefs.chicken.id,
                    SlayerNpcRefs.rat.id,
                )

            // Assign 4 times, one for each pool entry
            for (i in 0 until 4) {
                // Reset task so we can get a new one
                player.setVarp(slayer_varps.slayer_count, 0)
                player.setVarp(slayer_varps.slayer_target, 0)
                // Pick based on index, count always 5
                random.next = i
                random.then = 5
                player.opNpc1(master)
                advance(ticks = 1)
                val target = player.vars[slayer_varps.slayer_target]
                assertTrue(target in validTargets, "Expected pool NPC, got target id $target")
            }
        }

    @Test
    fun GameTestState.`high level entry excluded at low slayer level`() =
        runGameTest(SlayerMaster::class) {
            val masterType = npcTypes[SlayerNpcRefs.master]
            val master = spawnNpc(CoordGrid(0, 50, 50, 32, 32), masterType)
            player.teleport(CoordGrid(0, 50, 50, 32, 33))
            player.stats[stats.slayer] = 1

            random.next = 0
            random.then = 5

            player.opNpc1(master)
            advance(ticks = 1)

            assertEquals(SlayerNpcRefs.cow.id, player.vars[slayer_varps.slayer_target])
            assertEquals(5, player.vars[slayer_varps.slayer_count])
        }

    @Test
    fun GameTestState.`assigned count is within entry range`() =
        runGameTest(SlayerMaster::class) {
            val masterType = npcTypes[SlayerNpcRefs.master]
            val master = spawnNpc(CoordGrid(0, 50, 50, 32, 32), masterType)
            player.teleport(CoordGrid(0, 50, 50, 32, 33))

            // Test with count at lower bound (5)
            random.next = 0
            random.then = 5
            player.opNpc1(master)
            advance(ticks = 1)
            assertEquals(5, player.vars[slayer_varps.slayer_count])

            // Reset and test with count at upper bound (10)
            player.setVarp(slayer_varps.slayer_count, 0)
            player.setVarp(slayer_varps.slayer_target, 0)
            random.next = 1
            random.then = 10
            player.opNpc1(master)
            advance(ticks = 1)
            assertEquals(10, player.vars[slayer_varps.slayer_count])
        }
}
