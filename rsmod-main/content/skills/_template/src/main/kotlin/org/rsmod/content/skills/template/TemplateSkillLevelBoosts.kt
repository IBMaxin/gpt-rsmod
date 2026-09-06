package org.rsmod.content.skills.template

import org.rsmod.api.config.refs.stats
import org.rsmod.api.stats.levelmod.InvisibleLevelMod
import org.rsmod.game.entity.Player

/**
 * Invisible level boosts for the skill.
 *
 * Override [calculateBoost] to return an integer representing
 * the invisible level boost for the player.
 */
class TemplateSkillLevelBoosts : InvisibleLevelMod(stats.woodcutting) {
    override fun Player.calculateBoost(): Int {
        // TODO: Implement area-specific boosts (e.g., skill guild +7)
        return 0
    }
}
