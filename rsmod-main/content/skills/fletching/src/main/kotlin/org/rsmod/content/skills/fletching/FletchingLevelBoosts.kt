package org.rsmod.content.skills.fletching

import org.rsmod.api.config.refs.stats
import org.rsmod.api.stats.levelmod.InvisibleLevelMod
import org.rsmod.game.entity.Player

class FletchingLevelBoosts : InvisibleLevelMod(stats.fletching) {
    override fun Player.calculateBoost(): Int {
        // TODO: Fletching cape (level 99) — no invisible boost, but guarantees success
        return 0
    }
}
