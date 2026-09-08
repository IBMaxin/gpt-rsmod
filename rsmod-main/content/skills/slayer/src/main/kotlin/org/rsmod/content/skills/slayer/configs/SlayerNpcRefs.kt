package org.rsmod.content.skills.slayer.configs

import org.rsmod.api.type.refs.npc.NpcReferences

object SlayerNpcRefs : NpcReferences() {
    // Master NPC – Turael (lowest-level slayer master)
    val master = find("slayer_master_1_tureal")
    // Task NPC – use the base cow NPC (exists in cache)
    val cow = find("cow")
}
