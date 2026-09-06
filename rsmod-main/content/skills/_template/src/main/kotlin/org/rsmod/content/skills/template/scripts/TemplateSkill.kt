package org.rsmod.content.skills.template.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.locParam
import org.rsmod.api.config.locXpParam
import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.synths
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.miningLvl
import org.rsmod.api.random.GameRandom
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLoc3
import org.rsmod.api.script.onOpLocU
import org.rsmod.api.stats.levelmod.InvisibleLevels
import org.rsmod.api.stats.xpmod.XpModifiers
import org.rsmod.content.skills.template.configs.TemplateSkillParams
import org.rsmod.game.MapClock
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.type.enums.EnumTypeList
import org.rsmod.game.type.enums.find
import org.rsmod.game.type.loc.LocTypeList
import org.rsmod.game.type.loc.UnpackedLocType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.UnpackedObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Main script for the skill.
 *
 * Pattern:
 * - onOpLoc1: First click on resource (start action)
 * - onOpLoc3: Continue action (re-trigger)
 * - onOpLocU: Use tool on resource
 *
 * Each handler validates level, checks for tool, rolls success,
 * gives XP, adds product to inventory, and transforms the resource.
 */
class TemplateSkill
@Inject
constructor(
    private val objTypes: ObjTypeList,
    private val locTypes: LocTypeList,
    private val enumTypes: EnumTypeList,
    private val locRepo: LocRepository,
    private val xpMods: XpModifiers,
    private val invisibleLvls: InvisibleLevels,
    private val mapClock: MapClock,
    private val random: GameRandom,
) : PluginScript() {
    override fun ScriptContext.startup() {
        // TODO: Replace content.ore with your content group
        // TODO: Replace content.pickaxe with your tool content group
        // onOpLoc1(content.ore) { attempt(it.loc, it.type) }
        // onOpLoc3(content.ore) { gather(it.loc, it.type) }
        // onOpLocU(content.ore, content.pickaxe) { gather(it.loc, it.type) }
    }

    private fun ProtectedAccess.attempt(loc: BoundLocInfo, type: UnpackedLocType) {
        // TODO: Implement level check
        if (player.miningLvl < type.levelReq) {
            mes("You need a level of ${type.levelReq} to do this.")
            return
        }

        // TODO: Implement inventory full check
        if (inv.isFull()) {
            mes("Your inventory is too full.")
            return
        }

        // TODO: Implement tool check and start animation
        // val tool = findTool(player, objTypes)
        // if (tool == null) {
        //     mes("You need a tool to do this.")
        //     return
        // }
        // anim(objTypes[tool].toolAnim)
        // spam("You swing your tool at the resource.")
        // gather(loc, type)
    }

    private fun ProtectedAccess.gather(loc: BoundLocInfo, type: UnpackedLocType) {
        // TODO: Implement full gathering logic
        // 1. Check tool exists
        // 2. Check level requirement
        // 3. Check inventory space
        // 4. Roll success using statRandom
        // 5. Give XP using statAdvance
        // 6. Add product to inventory
        // 7. Transform loc to depleted stage
        // 8. Schedule respawn
    }

    companion object {
        // TODO: Define extension properties for your loc/obj params
        val UnpackedLocType.levelReq: Int by locParam(params.levelrequire)
        val UnpackedLocType.product: ObjType by locParam(params.skill_productitem)
        val UnpackedLocType.xp: Double by locXpParam(params.skill_xp)
        val UnpackedLocType.depletedLoc: LocType by locParam(params.next_loc_stage)
        val UnpackedLocType.respawnTime: Int by locParam(params.respawn_time)

        // val UnpackedObjType.toolAnim: SeqType by objParam(params.skill_anim)

        /**
         * Look up success rates for a resource + tool combination.
         * Returns (low, high) bounds for statRandom.
         */
        fun successRates(
            resourceType: UnpackedLocType,
            tool: /* InvObj */ Any,
            enumTypes: EnumTypeList,
        ): Pair<Int, Int> {
            // TODO: Implement success rate lookup
            // val rates = enumTypes[resourceType.param(TemplateSkillParams.success_rates)].find(tool)
            // val low = rates shr 16
            // val high = rates and 0xFFFF
            // return low to high
            return 0 to 0
        }
    }
}
