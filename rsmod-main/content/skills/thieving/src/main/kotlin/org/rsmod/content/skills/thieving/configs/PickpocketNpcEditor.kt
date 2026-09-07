package org.rsmod.content.skills.thieving.configs

import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.game.type.npc.NpcType

object PickpocketNpcEditor : NpcEditor() {
    init {
        val people =
            setOf(
                PickpocketNpcRefs.man,
                PickpocketNpcRefs.man2,
                PickpocketNpcRefs.man3,
                PickpocketNpcRefs.man_indoor,
                PickpocketNpcRefs.woman,
                PickpocketNpcRefs.woman2,
                PickpocketNpcRefs.woman3,
            )
        people.forEach(::editPickpocketNpc)
    }

    private fun editPickpocketNpc(type: NpcType) {
        edit(type) {
            param[ThievingParams.levelrequire] = 1
            param[ThievingParams.skill_xp] = 8
        }
    }
}
