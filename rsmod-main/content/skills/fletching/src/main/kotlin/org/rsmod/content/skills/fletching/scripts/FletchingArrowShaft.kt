package org.rsmod.content.skills.fletching.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.refs.stats
import org.rsmod.api.invtx.invAdd
import org.rsmod.api.invtx.invDel
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.statAdvance
import org.rsmod.api.script.onOpHeldU
import org.rsmod.content.skills.fletching.configs.FletchingObjRefs
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class FletchingArrowShaft
@Inject
constructor(
    private val objTypes: ObjTypeList,
) : PluginScript() {
    override fun ScriptContext.startup() {
        /* Arrow shaft registrations are handled by FletchingBow to avoid
           duplicate HeldUEvents keys (knife+log combos). */
    }

    private suspend fun ProtectedAccess.makeShafts(log: ObjType) {
        val playerLevel = stat(stats.fletching)

        if (playerLevel < 1) {
            mes("You need a Fletching level of 1 to make arrow shafts.")
            return
        }

        if (inv.isFull()) {
            mes("Your inventory is too full to hold any more arrow shafts.")
            return
        }

        if (!inv.contains(log)) {
            mes("You need a ${log.internalName} to do this.")
            return
        }

        actionDelay = 3

        player.invDel(inv, log, 1)
        invAdd(inv, FletchingObjRefs.arrow_shaft, 15)
        statAdvance(stats.fletching, 5.0)

        mes("You carefully cut the ${log.internalName} into 15 arrow shafts.")
    }
}
