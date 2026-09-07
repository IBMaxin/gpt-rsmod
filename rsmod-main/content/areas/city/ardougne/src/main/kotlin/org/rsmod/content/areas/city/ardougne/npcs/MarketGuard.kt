package org.rsmod.content.areas.city.ardougne.npcs

import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class MarketGuard : PluginScript() {
    override fun ScriptContext.startup() {
        // TODO: Register guard aggro on stall theft failure.
        // Requires line-of-sight detection wired into StallThieving.
        // Implement when the aggression system supports it.
    }
}
