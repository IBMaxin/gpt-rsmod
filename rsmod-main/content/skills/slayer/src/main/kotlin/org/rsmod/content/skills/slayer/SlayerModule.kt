package org.rsmod.content.skills.slayer

import org.rsmod.api.stats.levelmod.InvisibleLevelMod
import org.rsmod.plugin.module.PluginModule

class SlayerModule : PluginModule() {
    override fun bind() {
        addSetBinding<InvisibleLevelMod>(SlayerLevelBoosts::class.java)
    }
}
