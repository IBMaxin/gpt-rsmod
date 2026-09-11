package org.rsmod.content.skills.slayer.map

import org.rsmod.api.type.builders.map.npc.MapNpcSpawnBuilder
import org.rsmod.content.skills.slayer.SlayerModule

object SlayerNpcSpawns : MapNpcSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<SlayerModule>("npcs.toml")
    }
}
