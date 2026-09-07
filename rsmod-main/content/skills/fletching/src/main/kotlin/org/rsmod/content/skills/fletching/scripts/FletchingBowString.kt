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

class FletchingBowString @Inject constructor(private val objTypes: ObjTypeList) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpHeldU(FletchingObjRefs.bow_string, FletchingObjRefs.unstrung_shortbow) {
            stringBow(FletchingObjRefs.unstrung_shortbow)
        }
        onOpHeldU(FletchingObjRefs.bow_string, FletchingObjRefs.unstrung_longbow) {
            stringBow(FletchingObjRefs.unstrung_longbow)
        }
        onOpHeldU(FletchingObjRefs.bow_string, FletchingObjRefs.unstrung_oak_shortbow) {
            stringBow(FletchingObjRefs.unstrung_oak_shortbow)
        }
        onOpHeldU(FletchingObjRefs.bow_string, FletchingObjRefs.unstrung_oak_longbow) {
            stringBow(FletchingObjRefs.unstrung_oak_longbow)
        }
        onOpHeldU(FletchingObjRefs.bow_string, FletchingObjRefs.unstrung_willow_shortbow) {
            stringBow(FletchingObjRefs.unstrung_willow_shortbow)
        }
        onOpHeldU(FletchingObjRefs.bow_string, FletchingObjRefs.unstrung_willow_longbow) {
            stringBow(FletchingObjRefs.unstrung_willow_longbow)
        }
        onOpHeldU(FletchingObjRefs.bow_string, FletchingObjRefs.unstrung_maple_shortbow) {
            stringBow(FletchingObjRefs.unstrung_maple_shortbow)
        }
        onOpHeldU(FletchingObjRefs.bow_string, FletchingObjRefs.unstrung_maple_longbow) {
            stringBow(FletchingObjRefs.unstrung_maple_longbow)
        }
        onOpHeldU(FletchingObjRefs.bow_string, FletchingObjRefs.unstrung_yew_shortbow) {
            stringBow(FletchingObjRefs.unstrung_yew_shortbow)
        }
        onOpHeldU(FletchingObjRefs.bow_string, FletchingObjRefs.unstrung_yew_longbow) {
            stringBow(FletchingObjRefs.unstrung_yew_longbow)
        }
        onOpHeldU(FletchingObjRefs.bow_string, FletchingObjRefs.unstrung_magic_shortbow) {
            stringBow(FletchingObjRefs.unstrung_magic_shortbow)
        }
        onOpHeldU(FletchingObjRefs.bow_string, FletchingObjRefs.unstrung_magic_longbow) {
            stringBow(FletchingObjRefs.unstrung_magic_longbow)
        }
    }

    private suspend fun ProtectedAccess.stringBow(unstrung: org.rsmod.game.type.obj.ObjType) {
        val unstrungType = objTypes[unstrung]
        val levelReq = unstrungType.fletchLevelReq
        val xp = unstrungType.fletchXp
        val product = unstrungType.fletchProduct

        val playerLevel = stat(stats.fletching)
        if (playerLevel < levelReq) {
            mes("You need a Fletching level of $levelReq to string this bow.")
            return
        }

        if (inv.isFull()) {
            mes("Your inventory is too full to hold any more ${objTypes[product].lowercaseName}.")
            return
        }

        if (!inv.contains(unstrung)) {
            mes("You need an ${unstrungType.lowercaseName} to do this.")
            return
        }

        if (!inv.contains(FletchingObjRefs.bow_string)) {
            mes("You need a bow string to do this.")
            return
        }

        actionDelay = 3

        player.invDel(inv, unstrung, 1)
        player.invDel(inv, FletchingObjRefs.bow_string, 1)
        invAdd(inv, product, 1)
        statAdvance(stats.fletching, xp)

        mes("You string the ${unstrungType.lowercaseName} into ${objTypes[product].name}.")
    }

    companion object {
        val UnpackedObjType.fletchLevelReq: Int by objParam(params.levelrequire)
        val UnpackedObjType.fletchXp: Double by objXpParam(params.skill_xp)
        val UnpackedObjType.fletchProduct: org.rsmod.game.type.obj.ObjType by
            objParam(params.skill_productitem)
    }
}
