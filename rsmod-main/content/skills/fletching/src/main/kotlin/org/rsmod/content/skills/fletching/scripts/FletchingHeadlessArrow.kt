package org.rsmod.content.skills.fletching.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.refs.stats
import org.rsmod.api.invtx.invAdd
import org.rsmod.api.invtx.invDel
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.statAdvance
import org.rsmod.api.script.onOpHeldU
import org.rsmod.content.skills.fletching.configs.FletchingObjRefs
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class FletchingHeadlessArrow @Inject constructor(private val objTypes: ObjTypeList) :
    PluginScript() {
    override fun ScriptContext.startup() {
        onOpHeldU(FletchingObjRefs.feather, FletchingObjRefs.arrow_shaft) { makeHeadless() }
    }

    private suspend fun ProtectedAccess.makeHeadless() {
        val playerLevel = stat(stats.fletching)

        if (playerLevel < 1) {
            mes("You need a Fletching level of 1 to make headless arrows.")
            return
        }

        if (inv.isFull()) {
            mes("Your inventory is too full to hold any more headless arrows.")
            return
        }

        if (!inv.contains(FletchingObjRefs.feather)) {
            mes("You need some feathers to do this.")
            return
        }

        if (!inv.contains(FletchingObjRefs.arrow_shaft)) {
            mes("You need some arrow shafts to do this.")
            return
        }

        val featherType = objTypes[FletchingObjRefs.feather]
        val shaftType = objTypes[FletchingObjRefs.arrow_shaft]
        val feathers = inv.count(featherType)
        val shafts = inv.count(shaftType)
        val batchSize = minOf(15, feathers, shafts)

        if (batchSize < 15) {
            mes("You need 15 feathers and 15 arrow shafts to make headless arrows.")
            return
        }

        actionDelay = 3

        invDel(inv, FletchingObjRefs.feather, 15)
        invDel(inv, FletchingObjRefs.arrow_shaft, 15)
        invAdd(inv, FletchingObjRefs.headless_arrow, 15)
        statAdvance(stats.fletching, 15.0)

        mes("You attach feathers to the arrow shafts, creating 15 headless arrows.")
    }
}
