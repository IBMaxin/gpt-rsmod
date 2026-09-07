package org.rsmod.content.skills.slayer.configs

import org.rsmod.api.type.refs.npc.NpcReferences

object SlayerNpcRefs : NpcReferences() {
    // Master NPC – using generic person as stand‑in (turael not present in cache)
    val master = find("person")
    // Task NPC – use the base cow NPC (exists in cache)
    val cow = find("cow")
}
