package org.rsmod.content.skills.slayer.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.random.GameRandom
import org.rsmod.api.script.onOpNpc1
import org.rsmod.content.skills.slayer.configs.SlayerNpcRefs
import org.rsmod.content.skills.slayer.configs.SlayerVarps
import org.rsmod.game.type.npc.NpcType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class SlayerMaster @Inject constructor(private val random: GameRandom) : PluginScript() {
    private var ProtectedAccess.slayerTarget by intVarp(SlayerVarps.slayer_target)
    private var ProtectedAccess.slayerCount by intVarp(SlayerVarps.slayer_count)

    override fun ScriptContext.startup() {
        onOpNpc1(SlayerNpcRefs.master) { talkToTurael() }
    }

    private suspend fun ProtectedAccess.talkToTurael() {
        if (slayerCount > 0) {
            mes("You already have a slayer task. Kill $slayerCount more to complete it.")
            return
        }
        if (stat(stats.slayer) < MIN_SLAYER_LEVEL) {
            mes("You need a Slayer level of at least $MIN_SLAYER_LEVEL to get a task.")
            return
        }
        val level = stat(stats.slayer)
        val eligible = taskPool.filter { level >= it.levelReq }
        if (eligible.isEmpty()) {
            mes("You have no eligible slayer tasks for your level.")
            return
        }
        val task = random.pick(eligible)
        val count = random.of(task.countRange)
        slayerTarget = task.npcType.id
        slayerCount = count
        val pluralName = task.npcName + "s"
        mes("Your new task is to kill $count $pluralName.")
    }

    companion object {
        private const val MIN_SLAYER_LEVEL = 1

        private data class SlayerTaskEntry(
            val npcType: NpcType,
            val npcName: String,
            val countRange: IntRange,
            val levelReq: Int,
        )

        private val taskPool =
            listOf(
                SlayerTaskEntry(SlayerNpcRefs.cow, "cow", 5..10, 1),
                SlayerTaskEntry(SlayerNpcRefs.goblin, "goblin", 5..10, 1),
            )
    }
}
