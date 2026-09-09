package org.rsmod.content.skills.cooking.scripts

import org.rsmod.content.skills.cooking.configs.CookingObjRefs
import org.rsmod.game.type.obj.ObjType

enum class CookingFood(
    val level: Int,
    val xp: Double,
    val burnLevel: Int,
    val burnRangeLevel: Int,
    val burnLumbridgeLevel: Int,
    val raw: ObjType,
    val cooked: ObjType,
    val burnt: ObjType,
) {
    SHRIMPS(
        1,
        30.0,
        34,
        31,
        31,
        CookingObjRefs.raw_shrimps,
        CookingObjRefs.shrimps,
        CookingObjRefs.burnt_shrimps,
    ),
    ANCHOVIES(
        1,
        30.0,
        34,
        31,
        31,
        CookingObjRefs.raw_anchovies,
        CookingObjRefs.cooked_anchovies,
        CookingObjRefs.burnt_anchovies,
    ),
    SARDINE(
        1,
        40.0,
        38,
        34,
        34,
        CookingObjRefs.raw_sardine,
        CookingObjRefs.cooked_sardine,
        CookingObjRefs.burnt_sardine,
    ),
    HERRING(
        5,
        50.0,
        41,
        38,
        38,
        CookingObjRefs.raw_herring,
        CookingObjRefs.cooked_herring,
        CookingObjRefs.burnt_herring,
    ),
    TROUT(
        15,
        70.0,
        49,
        45,
        45,
        CookingObjRefs.raw_trout,
        CookingObjRefs.cooked_trout,
        CookingObjRefs.burnt_trout,
    ),
    PIKE(
        20,
        80.0,
        54,
        50,
        49,
        CookingObjRefs.raw_pike,
        CookingObjRefs.cooked_pike,
        CookingObjRefs.burnt_pike,
    ),
    SALMON(
        25,
        90.0,
        58,
        55,
        55,
        CookingObjRefs.raw_salmon,
        CookingObjRefs.cooked_salmon,
        CookingObjRefs.burnt_salmon,
    ),
    TUNA(
        30,
        100.0,
        63,
        59,
        59,
        CookingObjRefs.raw_tuna,
        CookingObjRefs.cooked_tuna,
        CookingObjRefs.burnt_tuna,
    ),
    LOBSTER(
        40,
        120.0,
        74,
        70,
        70,
        CookingObjRefs.raw_lobster,
        CookingObjRefs.cooked_lobster,
        CookingObjRefs.burnt_lobster,
    ),
    SWORDFISH(
        45,
        140.0,
        86,
        80,
        76,
        CookingObjRefs.raw_swordfish,
        CookingObjRefs.cooked_swordfish,
        CookingObjRefs.burnt_swordfish,
    );

    companion object {
        private val byRawName = entries.associateBy { it.raw.internalName }

        fun fromRaw(raw: ObjType): CookingFood? = byRawName[raw.internalName]
    }
}
