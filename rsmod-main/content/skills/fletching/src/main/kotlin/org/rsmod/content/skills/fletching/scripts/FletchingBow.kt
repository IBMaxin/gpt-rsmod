package org.rsmod.content.skills.fletching.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.objParam
import org.rsmod.api.config.objXpParam
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.stats
import org.rsmod.api.invtx.invAdd
import org.rsmod.api.invtx.invDel
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.statAdvance
import org.rsmod.api.script.onOpHeldU
import org.rsmod.content.skills.fletching.configs.FletchingObjRefs
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.UnpackedObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class FletchingBow @Inject constructor(private val objTypes: ObjTypeList) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpHeldU(FletchingObjRefs.knife, FletchingObjRefs.logs) { makeBow(FletchingObjRefs.logs) }
        onOpHeldU(FletchingObjRefs.knife, FletchingObjRefs.oak_logs) {
            makeBow(FletchingObjRefs.oak_logs)
        }
        onOpHeldU(FletchingObjRefs.knife, FletchingObjRefs.willow_logs) {
            makeBow(FletchingObjRefs.willow_logs)
        }
        onOpHeldU(FletchingObjRefs.knife, FletchingObjRefs.maple_logs) {
            makeBow(FletchingObjRefs.maple_logs)
        }
        onOpHeldU(FletchingObjRefs.knife, FletchingObjRefs.yew_logs) {
            makeBow(FletchingObjRefs.yew_logs)
        }
        onOpHeldU(FletchingObjRefs.knife, FletchingObjRefs.magic_logs) {
            makeBow(FletchingObjRefs.magic_logs)
        }
    }

    private suspend fun ProtectedAccess.makeBow(log: org.rsmod.game.type.obj.ObjType) {
        val logType = objTypes[log]
        val levelReq = logType.fletchLevelReq
        val xp = logType.fletchXp
        val product = logType.fletchProduct

        val playerLevel = stat(stats.fletching)
        if (playerLevel < levelReq) {
            mes("You need a Fletching level of $levelReq to make ${objTypes[product].name}.")
            return
        }

        if (inv.isFull()) {
            mes("Your inventory is too full to hold any more ${objTypes[product].lowercaseName}.")
            return
        }

        if (!inv.contains(log)) {
            mes("You need a ${logType.lowercaseName} to do this.")
            return
        }

        actionDelay = 3

        player.invDel(inv, log, 1)
        invAdd(inv, product, 1)
        statAdvance(stats.fletching, xp)

        mes("You carve the ${logType.lowercaseName} into ${objTypes[product].name}.")
    }

    companion object {
        val UnpackedObjType.fletchLevelReq: Int by objParam(params.levelrequire)
        val UnpackedObjType.fletchXp: Double by objXpParam(params.skill_xp)
        val UnpackedObjType.fletchProduct: org.rsmod.game.type.obj.ObjType by
            objParam(params.skill_productitem)
    }
}
