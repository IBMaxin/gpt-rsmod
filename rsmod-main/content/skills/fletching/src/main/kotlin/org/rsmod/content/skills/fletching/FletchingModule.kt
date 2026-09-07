package org.rsmod.content.skills.fletching

import org.rsmod.api.stats.levelmod.InvisibleLevelMod
import org.rsmod.plugin.module.PluginModule

class FletchingModule : PluginModule() {
    override fun bind() {
        addSetBinding<InvisibleLevelMod>(FletchingLevelBoosts::class.java)
    }
}
