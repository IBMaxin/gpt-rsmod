package org.rsmod.content.bosses.template

import org.rsmod.api.combat.PvNCombat
import org.rsmod.api.specials.SpecialAttackMap
import org.rsmod.content.bosses.template.scripts.TemplateBossSpecialAttacks
import org.rsmod.plugin.module.PluginModule

/**
 * Module for the boss encounter.
 *
 * Register boss-specific bindings here:
 * - Special attacks
 * - Custom combat handlers
 * - Drop tables (when implemented)
 */
class TemplateBossModule : PluginModule() {
    override fun bind() {
        addSetBinding<SpecialAttackMap>(TemplateBossSpecialAttacks::class.java)
        bindInstance<PvNCombat>()
    }
}
