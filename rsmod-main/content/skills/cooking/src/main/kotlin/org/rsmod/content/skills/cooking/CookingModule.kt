package org.rsmod.content.skills.cooking

import org.rsmod.api.stats.levelmod.InvisibleLevelMod
import org.rsmod.plugin.module.PluginModule

class CookingModule : PluginModule() {
    override fun bind() {
        addSetBinding<InvisibleLevelMod>(CookingLevelBoosts::class.java)
    }
}
