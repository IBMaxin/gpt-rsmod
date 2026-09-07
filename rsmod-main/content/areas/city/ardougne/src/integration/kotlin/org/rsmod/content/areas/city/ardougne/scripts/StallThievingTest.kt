package org.rsmod.content.areas.city.ardougne.scripts

import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.stats
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.areas.city.ardougne.configs.ardougne_locs
import org.rsmod.map.CoordGrid

class StallThievingTest {

    @Test
    fun GameTestState.`bakery stall requires level 5`() =
        runGameTest(StallThieving::class) {
            val loc = placeMapLoc(CoordGrid(0, 51, 54, 10, 10), ardougne_locs.bakery_stall)
            player.teleport(CoordGrid(0, 51, 54, 10, 11))
            player.clearInv()
            player.stats[stats.thieving] = 4
            player.opLoc2(loc)
            advance(ticks = 1)
            assertMessageSent("You need a Thieving level of 5 to steal from this stall.")
        }

    @Test
    fun GameTestState.`successful steal gives xp and loot`() =
        runGameTest(StallThieving::class) {
            val loc = placeMapLoc(CoordGrid(0, 51, 54, 10, 10), ardougne_locs.bakery_stall)
            player.teleport(CoordGrid(0, 51, 54, 10, 11))
            player.clearInv()
            player.stats[stats.thieving] = 5
            val xpBefore = player.statMap.getXP(stats.thieving)
            player.opLoc2(loc)
            advance(ticks = 1)
            assertMessageSent("You steal from the bakery stall.")
            assertTrue(player.inv.any { it != null }) { "Loot should be in inventory" }
            assertEquals(16, player.statMap.getXP(stats.thieving) - xpBefore)
        }

    @Test
    fun GameTestState.`full inventory blocks steal`() =
        runGameTest(StallThieving::class) {
            val loc = placeMapLoc(CoordGrid(0, 51, 54, 10, 10), ardougne_locs.bakery_stall)
            player.teleport(CoordGrid(0, 51, 54, 10, 11))
            player.fillInv()
            player.stats[stats.thieving] = 5
            player.opLoc2(loc)
            advance(ticks = 1)
            assertMessageSent("Your inventory is too full to steal anything.")
        }

    // TODO: Add guard-catch test once MarketGuard LOS aggro is implemented
}
