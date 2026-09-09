package org.rsmod.content.skills.cooking.scripts

import org.rsmod.game.type.obj.ObjType

enum class CookingFood(
    val level: Int,
    val xp: Double,
    val burnLevel: Int,
    val burnRangeLevel: Int,
    val burnLumbridgeLevel: Int,
    val raw: ObjType?,
    val cooked: ObjType?,
    val burnt: ObjType?,
) {
    // TODO(cooking): Populate with food entries once cache .sym entries are available.
}
