package org.rsmod.content.skills.slayer.configs

import org.rsmod.api.type.refs.varp.VarpReferences

typealias slayer_varps = SlayerVarps

object SlayerVarps : VarpReferences() {
    val slayer_target = find("slayer_target")
    val slayer_count = find("slayer_count")
}
