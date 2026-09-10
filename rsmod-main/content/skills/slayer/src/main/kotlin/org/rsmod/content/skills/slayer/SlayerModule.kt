package org.rsmod.content.skills.slayer

import com.google.inject.multibindings.Multibinder
import org.rsmod.api.death.DropTableMap
import org.rsmod.api.stats.levelmod.InvisibleLevelMod
import org.rsmod.content.skills.slayer.configs.SlayerDrops
import org.rsmod.plugin.module.PluginModule

class SlayerModule : PluginModule() {
    override fun bind() {
        Multibinder.newSetBinder(binder(), DropTableMap::class.java)
            .addBinding()
            .toInstance(SlayerDrops)
        addSetBinding<InvisibleLevelMod>(SlayerLevelBoosts::class.java)
    }
}
