package org.rsmod.api.combat.formulas

import org.rsmod.game.entity.Player
import org.rsmod.game.type.npc.UnpackedNpcType
import org.rsmod.game.type.varp.HashedVarpType

/**
 * Hit chance formulas internally use decimals (e.g., `1%` = `0.01`, `100%` = `1.0`). To maintain
 * consistency with other combat formulas that use whole integers, we scale them using this
 * constant.
 */
internal const val HIT_CHANCE_SCALE: Int = 10_000

internal fun scale(base: Int, multiplier: Int, divisor: Int): Int = (base * multiplier) / divisor

private val slayerTargetVarp =
    HashedVarpType(startHash = null, internalName = "slayer_target", internalId = 395)

private val slayerCountVarp =
    HashedVarpType(startHash = null, internalName = "slayer_count", internalId = 394)

internal fun UnpackedNpcType.isSlayerTask(player: Player): Boolean {
    val target = player.vars[slayerTargetVarp]
    val count = player.vars[slayerCountVarp]
    return target == id && count > 0
}
