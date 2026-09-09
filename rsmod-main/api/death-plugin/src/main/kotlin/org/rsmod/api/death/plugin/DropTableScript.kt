package org.rsmod.api.death.plugin

import jakarta.inject.Inject
import org.rsmod.api.death.DropTableMap
import org.rsmod.api.death.DropTableRepository
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Script that registers all drop tables from multibindings at startup.
 */
internal class DropTableScript
@Inject
constructor(
    private val repo: DropTableRepository,
    private val dropTables: Set<DropTableMap>,
) : PluginScript() {
    override fun ScriptContext.startup() {
        dropTables.registerAll()
    }

    private fun Iterable<DropTableMap>.registerAll() {
        for (dropTable in this) {
            dropTable.register()
        }
    }

    private fun DropTableMap.register() = repo.register()
}
