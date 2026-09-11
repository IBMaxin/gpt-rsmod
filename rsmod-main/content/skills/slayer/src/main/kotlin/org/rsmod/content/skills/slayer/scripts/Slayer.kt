package org.rsmod.content.skills.slayer.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.refs.queues
import org.rsmod.api.config.refs.stats
import org.rsmod.api.death.NpcDeath
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.stat.statAdvance
import org.rsmod.api.player.vars.VarPlayerIntMapSetter
import org.rsmod.api.script.onNpcQueue
import org.rsmod.content.skills.slayer.configs.SlayerNpcRefs
import org.rsmod.content.skills.slayer.configs.SlayerParams
import org.rsmod.content.skills.slayer.configs.SlayerVarps
import org.rsmod.game.entity.Player
import org.rsmod.game.entity.PlayerList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class Slayer @Inject constructor(private val death: NpcDeath, private val players: PlayerList) :
    PluginScript() {
    override fun ScriptContext.startup() {
        onNpcQueue(SlayerNpcRefs.cow, queues.death) { onSlayerNpcDeath() }
        onNpcQueue(SlayerNpcRefs.goblin, queues.death) { onSlayerNpcDeath() }
    }

    private suspend fun org.rsmod.api.npc.access.StandardNpcAccess.onSlayerNpcDeath() {
        val hero = findHero(players)
        if (hero != null) {
            val target = hero.slayerTarget
            val count = hero.slayerCount
            if (target == npc.type.id && count > 0) {
                val newCount = count - 1
                VarPlayerIntMapSetter.set(hero, SlayerVarps.slayer_count, newCount)
                val xp = npc.type.param(SlayerParams.experience).toDouble()
                hero.statAdvance(stats.slayer, xp)
                if (newCount <= 0) {
                    VarPlayerIntMapSetter.set(hero, SlayerVarps.slayer_target, 0)
                    hero.mes(
                        "You've completed your slayer task! Visit a slayer master for a new one."
                    )
                }
            }
        }
        death.deathWithDrops(this)
    }
}

private var Player.slayerTarget: Int
    get() = vars[SlayerVarps.slayer_target]
    set(value) {
        VarPlayerIntMapSetter.set(this, SlayerVarps.slayer_target, value)
    }

private var Player.slayerCount: Int
    get() = vars[SlayerVarps.slayer_count]
    set(value) {
        VarPlayerIntMapSetter.set(this, SlayerVarps.slayer_count, value)
    }
