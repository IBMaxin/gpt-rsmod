package org.rsmod.content.skills.template.configs

import org.rsmod.api.type.refs.enums.EnumReferences
import org.rsmod.api.type.refs.param.ParamReferences
import org.rsmod.game.type.enums.EnumType
import org.rsmod.game.type.obj.ObjType

/**
 * Custom param references for this skill.
 * Define params used to store success rates on loc types.
 */
object TemplateSkillParams : ParamReferences() {
    val success_rates = find<EnumType<ObjType, Int>>("template_skill_success_rates")
}
