@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.city.ardougne.configs

import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.api.type.refs.npc.NpcReferences

typealias ardougne_npcs = ArdougneNpcs

object ArdougneNpcs : NpcReferences() {
    val guard = find("ardougne_guard")
}

internal object ArdougneNpcEditor : NpcEditor() {
    init {
        edit(ardougne_npcs.guard) {
            op1 = "Talk-to"
            defaultMode = wander
            wanderRange = 3
        }
    }
}
