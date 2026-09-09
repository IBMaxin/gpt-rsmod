package org.rsmod.content.skills.cooking.scripts

import jakarta.inject.Inject
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class Cooking @Inject constructor(private val objTypes: ObjTypeList) : PluginScript() {
    override fun ScriptContext.startup() {
        // TODO(cooking): Register item-on-loc handlers once cache .sym entries are available.
    }
}