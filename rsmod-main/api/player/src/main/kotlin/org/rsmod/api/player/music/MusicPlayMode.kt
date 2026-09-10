package org.rsmod.api.player.music

import org.rsmod.api.utils.vars.VarEnumDelegate

@Suppress("konsist.avoid usage of stdlib Random in properties")
public enum class MusicPlayMode(override val varValue: Int) : VarEnumDelegate {
    Manual(0),
    Area(1),
    Random(2),
}
