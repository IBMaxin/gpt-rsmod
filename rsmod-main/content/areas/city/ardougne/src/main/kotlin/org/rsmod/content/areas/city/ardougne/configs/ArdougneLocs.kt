package org.rsmod.content.areas.city.ardougne.configs

import org.rsmod.api.type.refs.loc.LocReferences

typealias ardougne_locs = ArdougneLocs

object ArdougneLocs : LocReferences() {
    val bakery_stall = find("cakethiefstall")
    val silk_stall = find("silkthiefstall")
}
