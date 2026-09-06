package org.rsmod.content.skills.template.configs

import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.seqs
import org.rsmod.api.type.editors.obj.ObjEditor

/**
 * ObjEditor that adds params to tool items (pickaxes, axes, etc.).
 *
 * Set the content group and animation params for each tool tier.
 */
internal object TemplateToolEditor : ObjEditor() {
    init {
        // TODO: Replace with actual tool items
        /*
        edit(objs.bronze_tool) {
            contentGroup = content.ore  // or define a new content group
            param[params.skill_anim] = seqs.human_mining_bronze_tool
        }
        edit(objs.iron_tool) {
            contentGroup = content.ore
            param[params.skill_anim] = seqs.human_mining_iron_tool
        }
        edit(objs.steel_tool) {
            contentGroup = content.ore
            param[params.skill_anim] = seqs.human_mining_steel_tool
        }
        */
    }
}
