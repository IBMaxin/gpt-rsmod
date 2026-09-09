package org.rsmod.api.death

import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.rsmod.game.type.npc.NpcType

/**
 * Registry for NPC drop tables. Content modules register their drop tables via [DropTableMap].
 */
@Singleton
public class DropTableRepository
@Inject
constructor() {
    private val tables = mutableMapOf<NpcType, DropTable>()

    /**
     * Registers a [dropTable] for the given [npcType].
     */
    public fun register(npcType: NpcType, dropTable: DropTable) {
        tables[npcType] = dropTable
    }

    /**
     * Returns the drop table for the given [npcType], or null if none is registered.
     */
    public fun getTable(npcType: NpcType): DropTable? = tables[npcType]
}
