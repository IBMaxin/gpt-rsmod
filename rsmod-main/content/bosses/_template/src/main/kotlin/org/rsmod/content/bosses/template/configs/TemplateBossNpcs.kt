package org.rsmod.content.bosses.template.configs

import org.rsmod.api.config.refs.params
import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.api.type.refs.npc.NpcReferences

/**
 * NpcReferences for this boss.
 */
internal object TemplateBossNpcs : NpcReferences() {
    // TODO: Replace with your boss NPC reference
    // val my_boss = find("my_boss")
}

/**
 * NpcEditor that configures the boss NPC with combat stats.
 *
 * Set attack/defence levels, combat bonuses, animations, immunities, and creature type params.
 */
internal object TemplateBossEditor : NpcEditor() {
    init {
        // TODO: Configure your boss NPC
        /*
        edit(TemplateBossNpcs.my_boss) {
            param[params.attack_anim] = seqs.my_boss_attack
            param[params.defend_anim] = seqs.my_boss_defend
            param[params.death_anim] = seqs.my_boss_death
            param[params.attack_sound] = synths.my_boss_attack_sound
            param[params.defend_sound] = synths.my_boss_defend_sound
            param[params.death_sound] = synths.my_boss_death_sound

            // Combat stats (these are also on the NPC type from cache)
            param[params.attack_melee] = 80
            param[params.defence_stab] = 60
            param[params.defence_slash] = 50
            param[params.defence_crush] = 40
            param[params.defence_magic] = 30
            param[params.defence_ranged] = 50
            param[params.melee_strength] = 70

            // Creature type (triggers weapon bonuses against this NPC)
            param[params.undead] = 1          // salve amulet bonus
            // param[params.draconic] = 1      // dragon hunter weapon bonus
            // param[params.demon] = 1         // arclight bonus
            // param[params.kalphite] = 1      // keris bonus

            // Immunities
            param[params.poison_immunity] = true
            param[params.venom_immunity] = "Immune"
        }
        */
    }
}
