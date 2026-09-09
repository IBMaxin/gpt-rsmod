package org.rsmod.content.skills.slayer.scripts

import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.script.onOpNpc1
import org.rsmod.content.skills.slayer.configs.SlayerNpcRefs
import org.rsmod.content.skills.slayer.configs.SlayerVarps
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class SlayerMaster : PluginScript() {
    private var ProtectedAccess.slayerTarget by intVarp(SlayerVarps.slayer_target)
    private var ProtectedAccess.slayerCount by intVarp(SlayerVarps.slayer_count)

    override fun ScriptContext.startup() {
        onOpNpc1(SlayerNpcRefs.master) { talkToTurael() }
    }

    private suspend fun ProtectedAccess.talkToTurael() {
        if (slayerCount > 0) {
            mes("You already have a slayer task. Kill ${slayerCount} more to complete it.")
            return
        }
        slayerTarget = SlayerNpcRefs.cow.id
        slayerCount = TASK_COUNT
        mes("Your new task is to kill ${TASK_COUNT} cows.")
    }

    companion object {
        private const val TASK_COUNT = 5
    }
}
