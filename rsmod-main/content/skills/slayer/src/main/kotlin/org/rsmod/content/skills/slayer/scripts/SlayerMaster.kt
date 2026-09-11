package org.rsmod.content.skills.slayer.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.output.mes
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
        if (stat(stats.slayer) < MIN_SLAYER_LEVEL) {
            mes("You need a Slayer level of at least $MIN_SLAYER_LEVEL to get a task.")
            return
        }
        if (slayerCount > 0) {
            val choice = choice2("Replace my current task", 1, "Keep my current task", 2)
            if (choice == 1) {
                assignNewTask()
            } else {
                val targetName = resolveNpcName(slayerTarget)
                mes("You still need to kill $slayerCount $targetName to complete your task.")
            }
            return
        }
        assignNewTask()
    }

    internal fun assignRandomTask(): Pair<NpcType, Int> {
        val level = MIN_SLAYER_LEVEL
        val eligible = taskPool.filter { level >= it.levelReq }
        check(eligible.isNotEmpty()) { "No eligible slayer tasks for level $level" }
        val task = random.pick(eligible)
        val count = random.of(task.countRange)
        return task.npcType to count
    }

    private suspend fun ProtectedAccess.assignNewTask() {
        val (npcType, count) = assignRandomTask()
        slayerTarget = npcType.id
        slayerCount = count
        val pluralName = resolveNpcName(npcType.id) + "s"
        mes("Your new task is to kill $count $pluralName.")
    }

    private fun resolveNpcName(npcId: Int): String {
        val entry = taskPool.firstOrNull { it.npcType.id == npcId }
        return entry?.npcName ?: "monster"
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
                SlayerTaskEntry(SlayerNpcRefs.chicken, "chicken", 5..10, 1),
                SlayerTaskEntry(SlayerNpcRefs.rat, "rat", 5..10, 1),
            )
    }
}
