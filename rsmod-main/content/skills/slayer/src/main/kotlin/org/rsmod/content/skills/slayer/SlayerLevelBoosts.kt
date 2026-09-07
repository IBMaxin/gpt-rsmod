package org.rsmod.content.skills.slayer

import org.rsmod.api.config.refs.stats
import org.rsmod.api.stats.levelmod.InvisibleLevelMod
import org.rsmod.game.entity.Player

class SlayerLevelBoosts : InvisibleLevelMod(stats.slayer) {
    override fun Player.calculateBoost(): Int {
        // No boost logic for the skeleton implementation
        return 0
    }
}
