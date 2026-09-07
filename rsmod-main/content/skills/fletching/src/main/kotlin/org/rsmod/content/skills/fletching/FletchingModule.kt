package org.rsmod.content.skills.fletching

import org.rsmod.api.stats.levelmod.InvisibleLevelMod
import org.rsmod.content.skills.fletching.scripts.FletchingArrow
import org.rsmod.content.skills.fletching.scripts.FletchingArrowShaft
import org.rsmod.content.skills.fletching.scripts.FletchingBow
import org.rsmod.content.skills.fletching.scripts.FletchingBowString
import org.rsmod.content.skills.fletching.scripts.FletchingHeadlessArrow
import org.rsmod.plugin.module.PluginModule
import org.rsmod.plugin.scripts.PluginScript

class FletchingModule : PluginModule() {
    override fun bind() {
        addSetBinding<InvisibleLevelMod>(FletchingLevelBoosts::class.java)
        addSetBinding<PluginScript>(FletchingBow::class.java)
        addSetBinding<PluginScript>(FletchingBowString::class.java)
        addSetBinding<PluginScript>(FletchingArrow::class.java)
        addSetBinding<PluginScript>(FletchingArrowShaft::class.java)
        addSetBinding<PluginScript>(FletchingHeadlessArrow::class.java)
    }
}
