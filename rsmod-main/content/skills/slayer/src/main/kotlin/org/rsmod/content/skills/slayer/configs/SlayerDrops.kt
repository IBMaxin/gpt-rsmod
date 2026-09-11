@file:Suppress("konsist.avoid usage of stdlib Random in functions")

package org.rsmod.content.skills.slayer.configs

import org.rsmod.api.config.refs.objs
import org.rsmod.api.death.Drop
import org.rsmod.api.death.DropTable
import org.rsmod.api.death.DropTableMap
import org.rsmod.api.death.DropTableRepository
import org.rsmod.game.type.npc.NpcTypeList

object SlayerDrops : DropTableMap {
    val cowTable =
        DropTable(
            entries =
                listOf(
                    Drop.Always(objs.bones, amount = 1),
                    Drop.Always(objs.raw_beef, amount = 1),
                    Drop.Always(objs.cow_hide, amount = 1),
                    Drop.Random(objs.coins, amount = 15, rate = 128),
                )
        )

    val goblinTable =
        DropTable(
            entries =
                listOf(
                    Drop.Always(objs.bones, amount = 1),
                    Drop.Random(objs.coins, amount = 5, rate = 128),
                )
        )

    override fun DropTableRepository.register() {
        register(SlayerNpcRefs.cow, cowTable)
        register(SlayerNpcRefs.goblin, goblinTable)
    }

    fun registerAll(repo: DropTableRepository, npcTypes: NpcTypeList) {
        repo.register(npcTypes[SlayerNpcRefs.cow], cowTable)
        repo.register(npcTypes[SlayerNpcRefs.goblin], goblinTable)
    }
}
