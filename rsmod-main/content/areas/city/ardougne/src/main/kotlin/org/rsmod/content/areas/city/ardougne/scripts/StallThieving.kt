package org.rsmod.content.areas.city.ardougne.scripts

import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.statAdvance
import org.rsmod.api.script.onOpLoc2
import org.rsmod.content.areas.city.ardougne.configs.ardougne_locs
import org.rsmod.content.areas.city.ardougne.configs.ardougne_objs
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class StallThieving : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc2(ardougne_locs.bakery_stall) { stealFrom(it.loc, StallTarget.Bakery) }
        onOpLoc2(ardougne_locs.silk_stall) { stealFrom(it.loc, StallTarget.Silk) }
    }

    private suspend fun ProtectedAccess.stealFrom(loc: BoundLocInfo, target: StallTarget) {
        if (stat(stats.thieving) < target.levelReq) {
            mes("You need a Thieving level of ${target.levelReq} to steal from this stall.")
            return
        }
        if (!inv.hasFreeSpace()) {
            mes("Your inventory is too full to steal anything.")
            return
        }
        actionDelay = 2
        // Stall thieving is always successful if level req met.
        // Guard-catch via line-of-sight is a future TODO (MarketGuard aggro system).
        invAdd(inv, target.loot)
        statAdvance(stats.thieving, target.xp)
        mes("You steal from the ${target.stallName}.")
    }
}

enum class StallTarget(
    val stallName: String,
    val levelReq: Int,
    val xp: Double,
    val loot: ObjType,
) {
    Bakery("bakery stall", 5, 16.0, ardougne_objs.cake),
    Silk("silk stall", 20, 24.0, ardougne_objs.silk),
}
