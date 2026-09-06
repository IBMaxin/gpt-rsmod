package org.rsmod.content.bosses.template.scripts

import org.rsmod.api.config.refs.objs
import org.rsmod.api.specials.SpecialAttack
import org.rsmod.api.specials.SpecialAttackMap
import org.rsmod.api.specials.SpecialAttackType
import org.rsmod.game.inv.InvObj

/**
 * Boss special attacks.
 *
 * Register special attacks via the SpecialAttackMap multibinding.
 * Each special attack is a function that takes the player and weapon,
 * and returns whether the special was activated.
 *
 * Special attacks are triggered when the player activates special attack
 * bar while fighting this boss (for weapon specials) or when the boss
 * uses a special attack (for NPC specials).
 */
object TemplateBossSpecialAttacks : SpecialAttackMap {
    override fun register(add: (SpecialAttackType, SpecialAttack) -> Unit) {
        // TODO: Register boss-specific special attacks
        // Example:
        // add(SpecialAttackType.Weapon) { player, weapon ->
        //     // Implement special attack logic
        //     // Return true if the special was activated
        //     true
        // }
    }
}
