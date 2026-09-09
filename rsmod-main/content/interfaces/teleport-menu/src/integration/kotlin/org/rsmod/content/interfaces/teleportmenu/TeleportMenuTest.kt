package org.rsmod.content.interfaces.teleportmenu

import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.input.ResumePauseButtonInput
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.cheat.CheatCommandMap
import org.rsmod.map.CoordGrid

class TeleportMenuTest {

    private fun GameTestScope.selectChoice(comsub: Int) {
        player.resumeActiveCoroutine(ResumePauseButtonInput(components.chatmenu_pbutton, comsub))
    }

    @Test
    fun GameTestState.`teleport suspends at category menu`() =
        runInjectedGameTest(CheatCommandMap::class, scripts = arrayOf(TeleportMenuScript::class)) {
            commands ->
            val start = CoordGrid(0, 50, 50, 0, 0)
            player.placeAt(start)

            commands.execute(player, "teleport", emptyList())
            advance()

            assertNotNull(player.activeCoroutine)
            assertEquals(start, player.coords)
        }

    @Test
    fun GameTestState.`standard spells lumbridge teleports correctly`() =
        runInjectedGameTest(CheatCommandMap::class, scripts = arrayOf(TeleportMenuScript::class)) {
            commands ->
            player.placeAt(CoordGrid(0, 50, 50, 0, 0))
            player.stats[stats.magic] = 41

            commands.execute(player, "teleport", emptyList())
            advance()

            // Select "Standard Spells" (choice 1)
            selectChoice(1)
            advance()

            // Select "Lumbridge" (choice 1)
            selectChoice(1)
            advance()

            assertEquals(CoordGrid(0, 50, 50, 21, 18), player.coords)
        }

    @Test
    fun GameTestState.`ancient spells paddewwa teleports correctly`() =
        runInjectedGameTest(CheatCommandMap::class, scripts = arrayOf(TeleportMenuScript::class)) {
            commands ->
            player.placeAt(CoordGrid(0, 50, 50, 0, 0))
            player.stats[stats.magic] = 64

            commands.execute(player, "teleport", emptyList())
            advance()

            // Select "Ancient Spells" (choice 2)
            selectChoice(2)
            advance()

            // Select "Paddewwa" (choice 1)
            selectChoice(1)
            advance()

            assertEquals(CoordGrid(0, 48, 154, 26, 26), player.coords)
        }

    @Test
    fun GameTestState.`insufficient magic level blocks teleport`() =
        runInjectedGameTest(CheatCommandMap::class, scripts = arrayOf(TeleportMenuScript::class)) {
            commands ->
            player.placeAt(CoordGrid(0, 50, 50, 0, 0))
            player.stats[stats.magic] = 1

            commands.execute(player, "teleport", emptyList())
            advance()

            // Select "Standard Spells" (choice 1)
            selectChoice(1)
            advance()

            // Select "Lumbridge" (choice 1) - requires level 41
            selectChoice(1)
            advance()

            assertEquals(CoordGrid(0, 50, 50, 0, 0), player.coords)
        }

    @Test
    fun GameTestState.`arceuus spells library teleports correctly`() =
        runInjectedGameTest(CheatCommandMap::class, scripts = arrayOf(TeleportMenuScript::class)) {
            commands ->
            player.placeAt(CoordGrid(0, 50, 50, 0, 0))
            player.stats[stats.magic] = 10

            commands.execute(player, "teleport", emptyList())
            advance()

            // Select "Arceuus Spells" (choice 4)
            selectChoice(4)
            advance()

            // Select "Arceuus Library" (choice 1)
            selectChoice(1)
            advance()

            assertEquals(CoordGrid(0, 25, 59, 33, 62), player.coords)
        }
}
