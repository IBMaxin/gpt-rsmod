package org.rsmod.content.interfaces.teleportmenu

import jakarta.inject.Inject
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.protect.ProtectedAccessLauncher
import org.rsmod.api.player.stat.stat
import org.rsmod.api.script.onCommand
import org.rsmod.game.type.stat.StatTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class TeleportMenuScript
@Inject
constructor(
    private val protectedAccess: ProtectedAccessLauncher,
    private val statTypes: StatTypeList,
) : PluginScript() {

    override fun ScriptContext.startup() {
        onCommand("teleport") {
            desc = "Open teleport menu"
            cheat { protectedAccess.launch(player) { showCategoryMenu() } }
        }
    }

    internal suspend fun ProtectedAccess.showCategoryMenu() {
        val choice =
            choice4(
                TeleportCategory.Standard.displayName,
                TeleportCategory.Standard,
                TeleportCategory.Ancient.displayName,
                TeleportCategory.Ancient,
                TeleportCategory.Lunar.displayName,
                TeleportCategory.Lunar,
                TeleportCategory.Arceuus.displayName,
                TeleportCategory.Arceuus,
            )
        showTeleportList(choice, page = 0)
    }

    internal suspend fun ProtectedAccess.showTeleportList(category: TeleportCategory, page: Int) {
        val teleports = TeleportData.forCategory(category)
        val pageSize = 4
        val totalPages = (teleports.size + pageSize - 1) / pageSize
        val startIdx = page * pageSize
        val endIdx = minOf(startIdx + pageSize, teleports.size)
        val pageEntries = teleports.subList(startIdx, endIdx)
        val hasNextPage = page + 1 < totalPages
        val hasPrevPage = page > 0

        val choice =
            if (hasNextPage && hasPrevPage) {
                choice5(
                    pageEntries[0].name,
                    pageEntries[0],
                    pageEntries[1].name,
                    pageEntries[1],
                    pageEntries[2].name,
                    pageEntries[2],
                    pageEntries[3].name,
                    pageEntries[3],
                    "More...",
                    PAGE_DOWN,
                )
            } else if (hasNextPage) {
                choice5(
                    pageEntries[0].name,
                    pageEntries[0],
                    pageEntries[1].name,
                    pageEntries[1],
                    pageEntries[2].name,
                    pageEntries[2],
                    pageEntries[3].name,
                    pageEntries[3],
                    "More...",
                    PAGE_DOWN,
                )
            } else if (hasPrevPage) {
                choice5(
                    pageEntries[0].name,
                    pageEntries[0],
                    pageEntries[1].name,
                    pageEntries[1],
                    pageEntries[2].name,
                    pageEntries[2],
                    pageEntries[3].name,
                    pageEntries[3],
                    "Back...",
                    BACK,
                )
            } else {
                choice4(
                    pageEntries[0].name,
                    pageEntries[0],
                    pageEntries[1].name,
                    pageEntries[1],
                    pageEntries[2].name,
                    pageEntries[2],
                    pageEntries[3].name,
                    pageEntries[3],
                )
            }

        when (choice) {
            PAGE_DOWN -> showTeleportList(category, page + 1)
            BACK -> showCategoryMenu()
            is TeleportEntry -> performTeleport(choice)
        }
    }

    internal suspend fun ProtectedAccess.performTeleport(teleport: TeleportEntry) {
        val currentMagic = stat(statTypes[stats.magic])
        if (currentMagic < teleport.magicLevel) {
            mes("You need level ${teleport.magicLevel} Magic.")
            return
        }
        mes("Teleporting to ${teleport.name}...")
        telejump(teleport.coord)
    }

    private data object PAGE_DOWN

    private data object BACK
}
