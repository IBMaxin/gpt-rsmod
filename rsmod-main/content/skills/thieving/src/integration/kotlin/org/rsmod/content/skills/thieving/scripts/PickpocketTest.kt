package org.rsmod.content.skills.thieving.scripts

import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.stats
import org.rsmod.api.testing.GameTestState
import org.rsmod.map.CoordGrid

class PickpocketTest {
    @Test
    fun GameTestState.`validate level requirement`() =
        runGameTest(Pickpocket::class) {
            val npcType = npcTypes.values.first { it.isContentType(content.person) }
            val npc = spawnNpc(CoordGrid(0, 50, 50, 32, 32), npcType)
            player.teleport(CoordGrid(0, 50, 50, 32, 33))
            player.clearInv()

            player.stats[stats.thieving] = 0
            player.opNpc3(npc)
            advance(ticks = 1)
            assertMessageSent("You need a Thieving level of 1 to pickpocket this ${npcType.name}.")
        }

    @Test
    fun GameTestState.`successful pickpocket gives coins and xp`() =
        runGameTest(Pickpocket::class) {
            val npcType = npcTypes.values.first { it.isContentType(content.person) }
            val npc = spawnNpc(CoordGrid(0, 50, 50, 32, 32), npcType)
            player.teleport(CoordGrid(0, 50, 50, 32, 33))
            player.clearInv()

            player.stats[stats.thieving] = 1
            random.next = 0 // Guarantee success roll
            player.opNpc3(npc)
            advance(ticks = 1)
            assertMessageSent("You pick the ${npcType.name}'s pocket.")
            assertContains(player.inv, objs.coins)
            assertEquals(8, player.statMap.getXP(stats.thieving))
        }

    @Test
    fun GameTestState.`failed pickpocket stuns and damages`() =
        runGameTest(Pickpocket::class) {
            val npcType = npcTypes.values.first { it.isContentType(content.person) }
            val npc = spawnNpc(CoordGrid(0, 50, 50, 32, 32), npcType)
            player.teleport(CoordGrid(0, 50, 50, 32, 33))
            player.clearInv()

            player.stats[stats.thieving] = 1
            random.next = 71 // Guarantee failure roll (success rate at level 1 ~70.7%)
            player.opNpc3(npc)
            advance(ticks = 1)
            assertMessageSent("You fail to pick the ${npcType.name}'s pocket.")
            assertMessageSent("You have been stunned!")
            assertDoesNotContain(player.inv, objs.coins)
        }

    @Test
    fun GameTestState.`pickpocket with full inventory`() =
        runGameTest(Pickpocket::class) {
            val npcType = npcTypes.values.first { it.isContentType(content.person) }
            val npc = spawnNpc(CoordGrid(0, 50, 50, 32, 32), npcType)
            player.teleport(CoordGrid(0, 50, 50, 32, 33))
            player.fillInv()

            player.stats[stats.thieving] = 1
            player.opNpc3(npc)
            advance(ticks = 1)
            assertMessageSent("Your inventory is too full to hold any more coins.")
        }

    @Test
    fun GameTestState.`cannot pickpocket non-person npc`() =
        runGameTest(Pickpocket::class) {
            val npcType = npcTypes.values.first {
                !it.isContentType(content.person)
            }
            val npc = spawnNpc(CoordGrid(0, 50, 50, 32, 32), npcType)
            player.teleport(CoordGrid(0, 50, 50, 32, 33))

            player.stats[stats.thieving] = 1
            player.opNpc3(npc)
            advance(ticks = 1)
            // Non-person NPC sends default "Nothing interesting happens." message
            assertMessageSent("Nothing interesting happens.")
        }
}
