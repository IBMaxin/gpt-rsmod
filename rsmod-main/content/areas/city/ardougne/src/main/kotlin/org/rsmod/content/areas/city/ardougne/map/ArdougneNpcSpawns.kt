package org.rsmod.content.areas.city.ardougne.map

import org.rsmod.api.type.builders.map.npc.MapNpcSpawnBuilder
import org.rsmod.content.areas.city.ardougne.ArdougneModule

object ArdougneNpcSpawns : MapNpcSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<ArdougneModule>("npcs.toml")
    }
}
