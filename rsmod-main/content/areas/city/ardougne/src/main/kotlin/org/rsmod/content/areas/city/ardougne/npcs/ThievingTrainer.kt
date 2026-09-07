package org.rsmod.content.areas.city.ardougne.npcs

import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpNpc1
import org.rsmod.content.areas.city.ardougne.configs.ardougne_npcs
import org.rsmod.game.entity.Npc
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

private val ARDOUGNE_MARKET = CoordGrid(0, 51, 54, 11, 10)

class ThievingTrainer : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(ardougne_npcs.guard) { startDialogue(it.npc) }
    }

    private suspend fun ProtectedAccess.startDialogue(npc: Npc) =
        startDialogue(npc) {
            chatNpc(shifty, "Psst! Over here, friend.")
            mainMenu()
        }

    private suspend fun Dialogue.mainMenu() {
        val choice = choice3("Who are you?", 1, "Take me to the Thieving District.", 2, "Bye.", 3)
        when (choice) {
            1 -> whoAreYou()
            2 -> teleportToMarket()
            3 -> bye()
        }
    }

    private suspend fun Dialogue.whoAreYou() {
        chatPlayer(quiz, "Who are you?")
        chatNpc(
            shifty,
            "Between you and me, I know every pocket in Ardougne's market... " +
                "and most of them have been lighter after meeting me.",
        )
        mainMenu()
    }

    private suspend fun Dialogue.teleportToMarket() {
        chatPlayer(neutral, "Take me to the Thieving District.")
        chatNpc(happy, "Right this way. Mind the guards!")
        access.teleport(ARDOUGNE_MARKET)
    }

    private suspend fun Dialogue.bye() {
        chatPlayer(neutral, "Bye.")
        chatNpc(shifty, "Watch your pockets out there.")
    }
}
