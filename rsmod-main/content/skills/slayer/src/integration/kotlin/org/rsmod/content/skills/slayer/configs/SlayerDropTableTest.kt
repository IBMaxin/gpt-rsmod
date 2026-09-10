package org.rsmod.content.skills.slayer.configs

import com.google.inject.AbstractModule
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.death.Drop
import org.rsmod.api.death.DropTableModule
import org.rsmod.api.death.DropTableRepository
import org.rsmod.api.testing.GameTestState

class SlayerDropTableTest {
    class DropTableDeps @Inject constructor(val repo: DropTableRepository)

    @Test
    fun GameTestState.`cow has drop table with bones and coins`() =
        runInjectedGameTest(
            DropTableDeps::class,
            childModule = SlayerDropTestModule,
        ) { deps ->
            SlayerDrops.registerAll(deps.repo, npcTypes)
            val cowType = npcTypes[SlayerNpcRefs.cow]
            val table = deps.repo.getTable(cowType)
            assertNotNull(table)
            table!!

            val alwaysDrops = table.entries.filterIsInstance<Drop.Always>()
            assertTrue(alwaysDrops.any { it.objType.internalName == "bones" })

            val randomDrops = table.entries.filterIsInstance<Drop.Random>()
            assertTrue(randomDrops.any { it.objType.internalName == "coins" })
        }

    @Test
    fun GameTestState.`goblin has drop table with bones and coins`() =
        runInjectedGameTest(
            DropTableDeps::class,
            childModule = SlayerDropTestModule,
        ) { deps ->
            SlayerDrops.registerAll(deps.repo, npcTypes)
            val goblinType = npcTypes[SlayerNpcRefs.goblin]
            val table = deps.repo.getTable(goblinType)
            assertNotNull(table)
            table!!

            val alwaysDrops = table.entries.filterIsInstance<Drop.Always>()
            assertTrue(alwaysDrops.any { it.objType.internalName == "bones" })

            val randomDrops = table.entries.filterIsInstance<Drop.Random>()
            assertTrue(randomDrops.any { it.objType.internalName == "coins" })
        }

    @Test
    fun GameTestState.`cow drop table has 4 entries`() =
        runInjectedGameTest(
            DropTableDeps::class,
            childModule = SlayerDropTestModule,
        ) { deps ->
            SlayerDrops.registerAll(deps.repo, npcTypes)
            val cowType = npcTypes[SlayerNpcRefs.cow]
            val table = deps.repo.getTable(cowType)
            assertNotNull(table)
            assertEquals(4, table!!.entries.size)
        }

    private object SlayerDropTestModule : AbstractModule() {
        override fun configure() {
            install(DropTableModule())
        }
    }
}
