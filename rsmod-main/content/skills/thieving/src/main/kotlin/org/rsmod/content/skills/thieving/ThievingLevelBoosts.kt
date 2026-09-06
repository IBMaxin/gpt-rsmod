package org.rsmod.content.skills.thieving

import org.rsmod.api.config.refs.stats
import org.rsmod.api.stats.levelmod.InvisibleLevelMod
import org.rsmod.game.entity.Player

class ThievingLevelBoosts : InvisibleLevelMod(stats.thieving) {
    override fun Player.calculateBoost(): Int {
        // TODO: Ardougne diary bonus (+1 for medium, +2 for hard, +3 for elite)
        return 0
    }
}
