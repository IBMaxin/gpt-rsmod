package org.rsmod.api.death

import org.rsmod.plugin.module.PluginModule

/**
 * Guice module that declares the drop table multibinding.
 * Content modules can add their drop tables via `addSetBinding<DropTableMap>`.
 */
public class DropTableModule : PluginModule() {
    override fun bind() {
        bindInstance<DropTableRepository>()
        newSetBinding<DropTableMap>()
    }
}
