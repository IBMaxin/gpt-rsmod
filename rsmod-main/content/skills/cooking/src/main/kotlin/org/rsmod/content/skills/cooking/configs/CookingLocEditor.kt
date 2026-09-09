package org.rsmod.content.skills.cooking.configs

import org.rsmod.api.config.refs.content
import org.rsmod.api.type.editors.loc.LocEditor
import org.rsmod.game.type.loc.LocType

internal object CookingLocEditor : LocEditor() {
    init {
        val ranges =
            setOf(
                CookingLocRefs.hos_cooking_range,
                CookingLocRefs.hos_cooking_range_02,
                CookingLocRefs.dorgesh_cooking_range1,
                CookingLocRefs.dorgesh_cooking_range2,
                CookingLocRefs.lunar_pirate_cooking_range,
                CookingLocRefs.ds2_guild_cooking_range,
                CookingLocRefs.poh_stove_1,
                CookingLocRefs.poh_stove_2,
                CookingLocRefs.poh_stove_3,
                CookingLocRefs.poh_stove_4,
                CookingLocRefs.poh_stove_5,
                CookingLocRefs.poh_stove_6,
                CookingLocRefs.poh_stove_7,
                CookingLocRefs.fortis_stove,
            )
        ranges.forEach(::editRange)
    }

    private fun editRange(type: LocType) {
        // TODO(cooking): Add content.cooking_range content group to BaseContent once defined in cache.
        // edit(type) { contentGroup = content.cooking_range }
    }
}
