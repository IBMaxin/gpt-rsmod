package org.rsmod.api.death

import org.rsmod.game.type.obj.ObjType

/** Represents a single entry in an NPC's drop table. */
public sealed class Drop {
    /** Always drops this item (e.g., bones). */
    public data class Always(val objType: ObjType, val amount: Int = 1) : Drop()

    /** Drops with a random chance. [rate] is 1-in-[rate] (e.g., rate=128 means 1/128 chance). */
    public data class Random(val objType: ObjType, val amount: Int = 1, val rate: Int = 1) : Drop()

    /** Rare drop with a separate roll. Used for tertiary drops like elite clues. */
    public data class Tertiary(val objType: ObjType, val amount: Int = 1, val rate: Int = 1) :
        Drop()
}

/** A collection of [Drop] entries for an NPC type. */
public data class DropTable(val entries: List<Drop>)
