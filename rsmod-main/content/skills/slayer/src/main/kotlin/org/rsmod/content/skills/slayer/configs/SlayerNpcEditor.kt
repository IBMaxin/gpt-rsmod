package org.rsmod.content.skills.slayer.configs

import org.rsmod.api.type.editors.npc.NpcEditor

object SlayerNpcEditor : NpcEditor() {
    init {
        edit(SlayerNpcRefs.cow) {
            param[SlayerParams.levelrequire] = 1
            param[SlayerParams.experience] = 80
        }
        edit(SlayerNpcRefs.goblin) {
            param[SlayerParams.levelrequire] = 1
            param[SlayerParams.experience] = 64
        }
    }
}
