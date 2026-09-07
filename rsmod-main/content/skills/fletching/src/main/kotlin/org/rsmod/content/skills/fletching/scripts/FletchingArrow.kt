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
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.UnpackedObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class FletchingArrow
@Inject
constructor(
    private val objTypes: ObjTypeList,
) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpHeldU(FletchingObjRefs.headless_arrow, FletchingObjRefs.bronze_arrowheads) { makeArrow(FletchingObjRefs.bronze_arrowheads) }
        onOpHeldU(FletchingObjRefs.headless_arrow, FletchingObjRefs.iron_arrowheads) { makeArrow(FletchingObjRefs.iron_arrowheads) }
        onOpHeldU(FletchingObjRefs.headless_arrow, FletchingObjRefs.steel_arrowheads) { makeArrow(FletchingObjRefs.steel_arrowheads) }
        onOpHeldU(FletchingObjRefs.headless_arrow, FletchingObjRefs.mithril_arrowheads) { makeArrow(FletchingObjRefs.mithril_arrowheads) }
        onOpHeldU(FletchingObjRefs.headless_arrow, FletchingObjRefs.adamant_arrowheads) { makeArrow(FletchingObjRefs.adamant_arrowheads) }
        onOpHeldU(FletchingObjRefs.headless_arrow, FletchingObjRefs.rune_arrowheads) { makeArrow(FletchingObjRefs.rune_arrowheads) }
    }

    private suspend fun ProtectedAccess.makeArrow(arrowheads: ObjType) {
        val tipType = objTypes[arrowheads]
        val levelReq = tipType.fletchLevelReq
        val xpPerArrow = tipType.fletchXp
        val product = tipType.fletchProduct

        val playerLevel = stat(stats.fletching)
        if (playerLevel < levelReq) {
            mes("You need a Fletching level of $levelReq to make ${objTypes[product].name}.")
            return
        }

        if (!inv.contains(FletchingObjRefs.headless_arrow)) {
            mes("You need some headless arrows to do this.")
            return
        }

        if (!inv.contains(arrowheads)) {
            mes("You need some ${tipType.lowercaseName} to do this.")
            return
        }

        val headlessType = objTypes[FletchingObjRefs.headless_arrow]
        val headless = inv.count(headlessType)
        val tips = inv.count(tipType)
        val batchSize = minOf(15, headless, tips)

        if (batchSize < 15) {
            mes("You need 15 headless arrows and 15 ${tipType.lowercaseName} to make arrows.")
            return
        }

        if (inv.isFull()) {
            mes("Your inventory is too full to hold any more ${objTypes[product].lowercaseName}.")
            return
        }

        actionDelay = 3

        invDel(inv, FletchingObjRefs.headless_arrow, 15)
        invDel(inv, arrowheads, 15)
        invAdd(inv, product, 15)
        statAdvance(stats.fletching, xpPerArrow * 15)

        mes("You attach ${tipType.lowercaseName} to the headless arrows, creating 15 ${objTypes[product].name}.")
    }

    companion object {
        val UnpackedObjType.fletchLevelReq: Int by objParam(params.levelrequire)
        val UnpackedObjType.fletchXp: Double by objXpParam(params.skill_xp)
        val UnpackedObjType.fletchProduct: ObjType by objParam(params.skill_productitem)
    }
}
