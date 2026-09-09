package org.rsmod.content.skills.cooking

import org.rsmod.api.config.refs.stats
import org.rsmod.api.stats.levelmod.InvisibleLevelMod
import org.rsmod.game.entity.Player

class CookingLevelBoosts : InvisibleLevelMod(stats.cooking) {
    override fun Player.calculateBoost(): Int {
        // TODO: Cooking gauntlets (+0 effective levels, but reduce burn rate)
        // TODO: Lumbridge range bonus (requires Cook's Assistant quest)
        // TODO: Hosidius range bonus (requires diary)
        return 0
    }
}
