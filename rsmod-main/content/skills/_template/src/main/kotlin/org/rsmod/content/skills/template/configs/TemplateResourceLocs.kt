package org.rsmod.content.skills.template.configs

import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.params
import org.rsmod.api.type.editors.loc.LocEditor
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.content.skills.template.configs.TemplateSkillParams.success_rates
import org.rsmod.game.stat.PlayerStatMap
import org.rsmod.game.type.loc.LocType
import org.rsmod.game.type.obj.ObjType

private typealias skill_enums = TemplateSkillEnums

/**
 * LocEditor that configures resource loc types (rocks, trees, etc.).
 *
 * Each resource gets: content group, level requirement, XP, product item,
 * depleted stage, respawn time, and success rate enum.
 */
internal object TemplateResourceLocs : LocEditor() {
    init {
        // TODO: Replace with actual resource locs
        /*
        edit(resourceLocs.copper_rock) {
            contentGroup = content.ore
            param[params.levelrequire] = 1
            param[params.skill_xp] = PlayerStatMap.toFineXP(17.5).toInt()
            param[params.skill_productitem] = objs.copper_ore
            param[params.next_loc_stage] = resourceLocs.copper_rock_depleted
            param[params.respawn_time] = 4
            param[success_rates] = skill_enums.copper_rates
        }
        */
    }
}

/**
 * LocReferences for resource locs (rocks, trees, etc.).
 */
internal object ResourceLocs : LocReferences() {
    // TODO: Replace with actual loc references
    // val copper_rock = find("copper_rocks")
    // val copper_rock_depleted = find("copper_rocks_depleted")
    // val iron_rock = find("iron_rocks")
    // val iron_rock_depleted = find("iron_rocks_depleted")
}
