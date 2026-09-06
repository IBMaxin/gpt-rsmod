package org.rsmod.content.skills.template

import org.rsmod.api.stats.levelmod.InvisibleLevelMod
import org.rsmod.plugin.module.PluginModule

/**
 * Module for the [TemplateSkill] skill.
 *
 * Register any multibindings here (level boosts, etc.).
 */
class TemplateSkillModule : PluginModule() {
    override fun bind() {
        addSetBinding<InvisibleLevelMod>(TemplateSkillLevelBoosts::class.java)
    }
}
