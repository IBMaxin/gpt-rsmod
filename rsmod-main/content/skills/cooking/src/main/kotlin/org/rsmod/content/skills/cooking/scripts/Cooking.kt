package org.rsmod.content.skills.cooking.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.stats
import org.rsmod.api.invtx.invAdd
import org.rsmod.api.invtx.invDel
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.statAdvance
import org.rsmod.api.script.onOpLocU
import org.rsmod.content.skills.cooking.configs.CookingObjRefs
import org.rsmod.game.type.loc.UnpackedLocType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.UnpackedObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class Cooking @Inject constructor(private val objTypes: ObjTypeList) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLocU(content.cooking_range, CookingObjRefs.raw_shrimps) { cook(it.objType, it.type) }
        onOpLocU(content.cooking_range, CookingObjRefs.raw_anchovies) { cook(it.objType, it.type) }
        onOpLocU(content.cooking_range, CookingObjRefs.raw_sardine) { cook(it.objType, it.type) }
        onOpLocU(content.cooking_range, CookingObjRefs.raw_herring) { cook(it.objType, it.type) }
        onOpLocU(content.cooking_range, CookingObjRefs.raw_trout) { cook(it.objType, it.type) }
        onOpLocU(content.cooking_range, CookingObjRefs.raw_pike) { cook(it.objType, it.type) }
        onOpLocU(content.cooking_range, CookingObjRefs.raw_salmon) { cook(it.objType, it.type) }
        onOpLocU(content.cooking_range, CookingObjRefs.raw_tuna) { cook(it.objType, it.type) }
        onOpLocU(content.cooking_range, CookingObjRefs.raw_lobster) { cook(it.objType, it.type) }
        onOpLocU(content.cooking_range, CookingObjRefs.raw_swordfish) { cook(it.objType, it.type) }
    }

    private suspend fun ProtectedAccess.cook(raw: UnpackedObjType, loc: UnpackedLocType) {
        val food = CookingFood.fromRaw(raw) ?: return
        val playerLevel = stat(stats.cooking)

        if (playerLevel < food.level) {
            mes("You need a Cooking level of ${food.level} to cook ${raw.name.lowercase()}.")
            return
        }

        if (!inv.hasFreeSpace()) {
            mes("Your inventory is too full to hold any more ${objTypes[food.cooked].lowercaseName}.")
            return
        }

        actionDelay = 3

        val burnLevel =
            when {
                isLumbridgeRange(loc) -> food.burnLumbridgeLevel
                isRange(loc) -> food.burnRangeLevel
                else -> food.burnLevel
            }

        val burned =
            if (playerLevel >= burnLevel) {
                false
            } else {
                val burnChance = (burnLevel - playerLevel).toDouble() / (burnLevel - food.level + 1)
                val rand = random.randomDouble()
                rand < burnChance
            }

        invDel(inv, raw, 1)

        if (burned) {
            invAdd(inv, food.burnt, 1)
            mes("You accidentally burn the ${raw.name.lowercase()}.")
        } else {
            invAdd(inv, food.cooked, 1)
            statAdvance(stats.cooking, food.xp)
            mes(
                "You cook the ${raw.name.lowercase()}. You manage to make some ${objTypes[food.cooked].lowercaseName}."
            )
        }
    }

    private fun isRange(loc: UnpackedLocType): Boolean {
        return loc.isContentType(content.cooking_range)
    }

    private fun isLumbridgeRange(loc: UnpackedLocType): Boolean {
        return loc.name.contains("lumbridge") || loc.name.contains("cook-o-matic")
    }
}
