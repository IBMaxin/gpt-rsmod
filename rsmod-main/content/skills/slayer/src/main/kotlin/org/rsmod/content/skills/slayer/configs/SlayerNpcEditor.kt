package org.rsmod.content.skills.slayer.configs

import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.game.type.npc.NpcType

object SlayerNpcEditor : NpcEditor() {
    init {
        // Only edit the cow NPC for the skeleton task
        edit(SlayerNpcRefs.cow) {
            param[SlayerParams.levelrequire] = 1
            param[SlayerParams.experience] = 80 // 8.0 XP (fine units ×10)
        }
    }
}
