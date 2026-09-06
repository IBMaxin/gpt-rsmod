package org.rsmod.content.skills.template.configs

import org.rsmod.api.config.refs.objs
import org.rsmod.api.type.builders.enums.EnumBuilder
import org.rsmod.api.type.builders.param.ParamBuilder
import org.rsmod.api.type.refs.enums.EnumReferences
import org.rsmod.game.type.enums.EnumType
import org.rsmod.game.type.obj.ObjType

/**
 * Enum references for per-resource success rate tables.
 */
internal object TemplateSkillEnums : EnumReferences() {
    val copper_rates = find<ObjType, Int>("copper_rates")
    val iron_rates = find<ObjType, Int>("iron_rates")
}

/**
 * Param builder for custom params.
 */
internal object TemplateSkillParamBuilder : ParamBuilder() {
    init {
        build<EnumType<ObjType, Int>>("template_skill_success_rates")
    }
}

/**
 * Enum builder defining success rates per tool for each resource tier.
 *
 * Rate format: (low shl 16) or high
 * Where low/high are the bounds for the statRandom roll.
 */
internal object TemplateSkillEnumBuilder : EnumBuilder() {
    init {
        build<ObjType, Int>("copper_rates") {
            // TODO: Replace with actual tool items and rates
            // this[objs.bronze_tool] = rate(64, 200)
            // this[objs.iron_tool] = rate(96, 300)
            // this[objs.steel_tool] = rate(128, 400)
        }

        build<ObjType, Int>("iron_rates") {
            // this[objs.bronze_tool] = rate(32, 100)
            // this[objs.iron_tool] = rate(48, 150)
            // this[objs.steel_tool] = rate(64, 200)
        }
    }

    private fun rate(low: Int, high: Int): Int = (low shl 16) or high
}
