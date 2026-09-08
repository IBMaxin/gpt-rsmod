package org.rsmod.content.skills.slayer.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.refs.queues
import org.rsmod.api.death.NpcDeath
import org.rsmod.api.script.onNpcQueue
import org.rsmod.content.skills.slayer.configs.SlayerNpcRefs
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class Slayer @Inject constructor(private val death: NpcDeath) : PluginScript() {
    override fun ScriptContext.startup() {
        // Register death handling for the cow NPC only (type‑specific)
        onNpcQueue(SlayerNpcRefs.cow, queues.death) { death.deathWithDrops(this) }
        // No additional logic for this verification skeleton
    }
}
