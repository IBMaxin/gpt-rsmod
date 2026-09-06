package org.rsmod.content.skills.thieving

import org.rsmod.api.stats.levelmod.InvisibleLevelMod
import org.rsmod.plugin.module.PluginModule

class ThievingModule : PluginModule() {
    override fun bind() {
        addSetBinding<InvisibleLevelMod>(ThievingLevelBoosts::class.java)
    }
}
