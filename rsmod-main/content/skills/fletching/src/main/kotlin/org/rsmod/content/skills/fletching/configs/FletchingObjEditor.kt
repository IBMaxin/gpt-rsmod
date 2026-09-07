package org.rsmod.content.skills.fletching.configs

import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.params
import org.rsmod.api.type.editors.obj.ObjEditor
import org.rsmod.game.stat.PlayerStatMap
import org.rsmod.game.type.obj.ObjType

internal object FletchingObjEditor : ObjEditor() {
    init {
        edit(objs.knife) { contentGroup = content.fletching_knife }

        editLog(objs.logs, level = 1, xp = 5.0, FletchingObjRefs.unstrung_shortbow)
        editLog(objs.oak_logs, level = 20, xp = 16.5, FletchingObjRefs.unstrung_oak_shortbow)
        editLog(objs.willow_logs, level = 35, xp = 33.3, FletchingObjRefs.unstrung_willow_shortbow)
        editLog(objs.maple_logs, level = 41, xp = 50.0, FletchingObjRefs.unstrung_maple_shortbow)
        editLog(objs.yew_logs, level = 50, xp = 67.5, FletchingObjRefs.unstrung_yew_shortbow)
        editLog(objs.magic_logs, level = 80, xp = 83.3, FletchingObjRefs.unstrung_magic_shortbow)

        edit(FletchingObjRefs.arrow_shaft) {
            param[params.levelrequire] = 1
            param[params.skill_xp] = PlayerStatMap.toFineXP(5.0).toInt()
            param[params.skill_productitem] = FletchingObjRefs.arrow_shaft
        }

        edit(FletchingObjRefs.feather) {
            param[params.levelrequire] = 1
            param[params.skill_xp] = PlayerStatMap.toFineXP(1.0).toInt()
        }

        editArrow(
            FletchingObjRefs.bronze_arrowheads,
            level = 1,
            xp = 1.3,
            FletchingObjRefs.bronze_arrow,
        )
        editArrow(
            FletchingObjRefs.iron_arrowheads,
            level = 15,
            xp = 2.5,
            FletchingObjRefs.iron_arrow,
        )
        editArrow(
            FletchingObjRefs.steel_arrowheads,
            level = 30,
            xp = 5.0,
            FletchingObjRefs.steel_arrow,
        )
        editArrow(
            FletchingObjRefs.mithril_arrowheads,
            level = 45,
            xp = 7.5,
            FletchingObjRefs.mithril_arrow,
        )
        editArrow(
            FletchingObjRefs.adamant_arrowheads,
            level = 60,
            xp = 10.0,
            FletchingObjRefs.adamant_arrow,
        )
        editArrow(
            FletchingObjRefs.rune_arrowheads,
            level = 75,
            xp = 12.5,
            FletchingObjRefs.rune_arrow,
        )

        editBowString(
            FletchingObjRefs.unstrung_shortbow,
            level = 5,
            xp = 5.0,
            FletchingObjRefs.shortbow,
        )
        editBowString(
            FletchingObjRefs.unstrung_longbow,
            level = 10,
            xp = 10.0,
            FletchingObjRefs.longbow,
        )
        editBowString(
            FletchingObjRefs.unstrung_oak_shortbow,
            level = 20,
            xp = 16.5,
            FletchingObjRefs.oak_shortbow,
        )
        editBowString(
            FletchingObjRefs.unstrung_oak_longbow,
            level = 25,
            xp = 25.0,
            FletchingObjRefs.oak_longbow,
        )
        editBowString(
            FletchingObjRefs.unstrung_willow_shortbow,
            level = 35,
            xp = 33.2,
            FletchingObjRefs.willow_shortbow,
        )
        editBowString(
            FletchingObjRefs.unstrung_willow_longbow,
            level = 40,
            xp = 41.5,
            FletchingObjRefs.willow_longbow,
        )
        editBowString(
            FletchingObjRefs.unstrung_maple_shortbow,
            level = 50,
            xp = 50.0,
            FletchingObjRefs.maple_shortbow,
        )
        editBowString(
            FletchingObjRefs.unstrung_maple_longbow,
            level = 55,
            xp = 58.2,
            FletchingObjRefs.maple_longbow,
        )
        editBowString(
            FletchingObjRefs.unstrung_yew_shortbow,
            level = 65,
            xp = 67.5,
            FletchingObjRefs.yew_shortbow,
        )
        editBowString(
            FletchingObjRefs.unstrung_yew_longbow,
            level = 70,
            xp = 75.0,
            FletchingObjRefs.yew_longbow,
        )
        editBowString(
            FletchingObjRefs.unstrung_magic_shortbow,
            level = 80,
            xp = 83.2,
            FletchingObjRefs.magic_shortbow,
        )
        editBowString(
            FletchingObjRefs.unstrung_magic_longbow,
            level = 85,
            xp = 91.5,
            FletchingObjRefs.magic_longbow,
        )
    }

    private fun editLog(type: ObjType, level: Int, xp: Double, product: ObjType) {
        edit(type) {
            param[params.levelrequire] = level
            param[params.skill_xp] = PlayerStatMap.toFineXP(xp).toInt()
            param[params.skill_productitem] = product
        }
    }

    private fun editArrow(type: ObjType, level: Int, xp: Double, product: ObjType) {
        edit(type) {
            param[params.levelrequire] = level
            param[params.skill_xp] = PlayerStatMap.toFineXP(xp).toInt()
            param[params.skill_productitem] = product
        }
    }

    private fun editBowString(type: ObjType, level: Int, xp: Double, product: ObjType) {
        edit(type) {
            param[params.levelrequire] = level
            param[params.skill_xp] = PlayerStatMap.toFineXP(xp).toInt()
            param[params.skill_productitem] = product
        }
    }
}
