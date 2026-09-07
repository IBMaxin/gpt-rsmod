package org.rsmod.content.skills.thieving.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.walktriggers
import org.rsmod.api.invtx.invAdd
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.statAdvance
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.stats.levelmod.InvisibleLevels
import org.rsmod.content.skills.thieving.configs.ThievingParams
import org.rsmod.game.entity.Npc
import org.rsmod.game.hit.HitType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class Pickpocket @Inject constructor(private val invisibleLvls: InvisibleLevels) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc3(content.person) { pickpocket(it.npc) }
    }

    private suspend fun ProtectedAccess.pickpocket(npc: Npc) {
        val type = npc.type
        val levelReq = type.param(ThievingParams.levelrequire)
        val xp = type.param(ThievingParams.skill_xp)

        val playerLevel = stat(stats.thieving)
        if (playerLevel < levelReq) {
            mes("You need a Thieving level of $levelReq to pickpocket this ${type.name}.")
            return
        }

        if (!inv.hasFreeSpace()) {
            mes("Your inventory is too full to hold any more coins.")
            return
        }

        actionDelay = 2

        val success = statRandom(stats.thieving, LOW, HIGH, invisibleLvls)

        if (success) {
            val coins = random.of(MIN_COINS, MAX_COINS)
            invAdd(inv, objs.coins, coins)
            statAdvance(stats.thieving, xp.toDouble())
            mes("You pick the ${type.name}'s pocket.")
        } else {
            walkTrigger(walktriggers.stunned)
            queueHit(delay = 1, type = HitType.Typeless, damage = 1)
            mes("You fail to pick the ${type.name}'s pocket.")
            mes("You have been stunned!")
        }
    }

    companion object {
        /** OSRS pickpocket success rate numerators for men/woman (Mod Ash confirmed). */
        const val LOW = 180
        const val HIGH = 240

        /** OSRS loot range for men/woman pickpocket. */
        const val MIN_COINS = 3
        const val MAX_COINS = 9
    }
}
