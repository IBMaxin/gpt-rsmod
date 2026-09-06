package org.rsmod.content.npcs.template

import jakarta.inject.Inject
import org.rsmod.api.config.refs.mesanims
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.content.npcs.template.configs.TemplateNpcs
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * NPC script template.
 *
 * Pattern:
 * - Register event handlers in startup() for each NPC interaction
 * - Use Dialogue for multi-step conversations
 * - Use ProtectedAccess for player-safe operations
 *
 * Common event handlers:
 * - onOpNpc1: Talk-to (first click)
 * - onOpNpc2: Second option (e.g., Trade)
 * - onOpNpc3: Third option (e.g., Attack, Pickpocket)
 * - onOpNpcU: Use item on NPC
 */
class TemplateNpcScript
@Inject
constructor(
    // Inject services as needed (Shops, etc.)
) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(TemplateNpcs.my_npc) { startDialogue(it.npc) }
        onOpNpc3(TemplateNpcs.my_npc) { /* openShop(it.npc) */ }
    }

    /**
     * Start a dialogue with the NPC.
     */
    private suspend fun ProtectedAccess.startDialogue(npc: Npc) {
        startDialogue(npc) { myNpcDialogue(npc) }
    }

    /**
     * NPC dialogue tree.
     *
     * Dialogue methods:
     * - chatPlayer(anim, text) - Player speaks
     * - chatNpc(anim, text) - NPC speaks
     * - chatPlayer(text) - Player speaks with default anim
     * - chatNpc(text) - NPC speaks with default anim
     * - multi(option1, option2, ...) - Show choice menu
     * - choice3(opt1, id1, opt2, id2, opt3, id3) - 3-choice menu
     * - mes(text) - System message
     * - nod() - NPC nods
     * - yes() - Player says yes
     * - no() - Player says no
     *
     * Animation IDs:
     * - mesanims.happy, mesanims.sad, mesanims.angry, mesanims.shocked
     * - mesanims.thinking, mesanims.confused, mesanims.worried
     */
    private suspend fun Dialogue.myNpcDialogue(npc: Npc) {
        chatNpc(happy, "Hello there, adventurer!")
        val choice =
            choice3(
                "What do you do here?",
                1,
                "Can you help me?",
                2,
                "Goodbye.",
                3,
            )
        when (choice) {
            1 -> aboutMe()
            2 -> helpPlayer()
            3 -> farewell()
        }
    }

    private suspend fun Dialogue.aboutMe() {
        chatPlayer(quiz, "What do you do here?")
        chatNpc(happy, "I'm just a humble NPC. I stand here all day.")
        chatNpc(happy, "Feel free to look around!")
    }

    private suspend fun Dialogue.helpPlayer() {
        chatPlayer(quiz, "Can you help me?")
        chatNpc(thinking, "Hmm, let me think about that...")
        chatNpc(happy, "I'm sure you'll figure it out!")
    }

    private suspend fun Dialogue.farewell() {
        chatPlayer(happy, "Goodbye!")
        chatNpc(happy, "Safe travels, adventurer!")
    }

    /**
     * Example: Opening a shop.
     * Uncomment and inject Shops in constructor.
     */
    // private fun Player.openShop(npc: Npc) {
    //     shops.open(this, npc, "My Shop", template_invs.my_shop)
    // }

    /**
     * Example: Shop dialogue.
     */
    // private suspend fun Dialogue.shopDialogue(npc: Npc) {
    //     chatPlayer(quiz, "What do you have for sale?")
    //     chatNpc(happy, "Take a look at my wares!")
    //     player.openShop(npc)
    // }
}
