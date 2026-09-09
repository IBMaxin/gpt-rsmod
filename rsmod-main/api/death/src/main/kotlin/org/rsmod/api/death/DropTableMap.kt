package org.rsmod.api.death

/**
 * Multibinding interface for registering drop tables per NPC type.
 *
 * Content modules implement this to define drops for their NPCs.
 * Example usage:
 * ```
 * object CowDrops : DropTableMap {
 *     override fun DropTableRepository.register() {
 *         register(npcTypes.cow, DropTable(
 *             entries = listOf(
 *                 Drop.Always(objs.bones, amount = 1),
 *                 Drop.Random(objs.coins, amount = 15, rate = 128),
 *             )
 *         ))
 *     }
 * }
 * ```
 */
public fun interface DropTableMap {
    public fun DropTableRepository.register()
}
