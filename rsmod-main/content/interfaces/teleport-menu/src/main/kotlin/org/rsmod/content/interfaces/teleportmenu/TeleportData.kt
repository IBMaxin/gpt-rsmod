package org.rsmod.content.interfaces.teleportmenu

import org.rsmod.map.CoordGrid

data class TeleportEntry(val name: String, val coord: CoordGrid, val magicLevel: Int)

enum class TeleportCategory(val displayName: String) {
    STANDARD("Standard Spells"),
    ANCIENT("Ancient Spells"),
    LUNAR("Lunar Spells"),
    ARCEUUS("Arceuus Spells"),
}

object TeleportData {
    val standard: List<TeleportEntry> =
        listOf(
            TeleportEntry("Lumbridge", CoordGrid(0, 50, 50, 21, 18), 41),
            TeleportEntry("Varrock", CoordGrid(0, 50, 53, 13, 32), 35),
            TeleportEntry("Falador", CoordGrid(0, 46, 52, 21, 50), 47),
            TeleportEntry("Camelot", CoordGrid(0, 43, 54, 5, 22), 55),
            TeleportEntry("Ardougne", CoordGrid(0, 41, 51, 37, 38), 61),
            TeleportEntry("Watchtower", CoordGrid(0, 45, 73, 53, 40), 68),
            TeleportEntry("Trollheim", CoordGrid(0, 45, 57, 10, 31), 68),
            TeleportEntry("Ape Atoll", CoordGrid(0, 43, 43, 45, 46), 74),
        )

    val ancient: List<TeleportEntry> =
        listOf(
            TeleportEntry("Paddewwa", CoordGrid(0, 48, 154, 26, 26), 64),
            TeleportEntry("Senntisten", CoordGrid(0, 51, 52, 57, 8), 70),
            TeleportEntry("Kharyllyl", CoordGrid(0, 54, 54, 38, 17), 76),
            TeleportEntry("Lassar", CoordGrid(0, 46, 54, 60, 14), 82),
            TeleportEntry("Dareeyak", CoordGrid(0, 46, 57, 24, 48), 88),
            TeleportEntry("Carrallagar", CoordGrid(0, 49, 57, 22, 18), 94),
            TeleportEntry("Annakarl", CoordGrid(0, 51, 60, 24, 46), 100),
            TeleportEntry("Ghorrock", CoordGrid(0, 46, 60, 32, 32), 106),
        )

    val lunar: List<TeleportEntry> =
        listOf(
            TeleportEntry("Moonclan", CoordGrid(0, 33, 61, 2, 11), 66),
            TeleportEntry("ZMI Altar", CoordGrid(0, 38, 50, 36, 46), 69),
            TeleportEntry("Waterbirth Island", CoordGrid(0, 39, 58, 50, 44), 71),
            TeleportEntry("Barbarian Outpost", CoordGrid(0, 39, 55, 47, 49), 76),
            TeleportEntry("Port Khazard", CoordGrid(0, 41, 49, 12, 31), 80),
            TeleportEntry("Fishing Guild", CoordGrid(0, 40, 52, 51, 63), 89),
            TeleportEntry("Catherby", CoordGrid(0, 43, 53, 49, 57), 92),
            TeleportEntry("Lunar Ghorrock", CoordGrid(0, 46, 61, 30, 34), 96),
        )

    val arceuus: List<TeleportEntry> =
        listOf(
            TeleportEntry("Arceuus Library", CoordGrid(0, 25, 59, 33, 62), 10),
            TeleportEntry("Draynor Manor", CoordGrid(0, 48, 52, 36, 23), 16),
            TeleportEntry("Battlefront", CoordGrid(0, 21, 58, 4, 27), 19),
            TeleportEntry("Mind Altar", CoordGrid(0, 46, 54, 36, 53), 22),
            TeleportEntry("Salve Graveyard", CoordGrid(0, 53, 54, 40, 5), 30),
            TeleportEntry("Fenkenstrain's Castle", CoordGrid(0, 55, 55, 28, 9), 50),
            TeleportEntry("West Ardougne", CoordGrid(0, 39, 51, 4, 27), 68),
            TeleportEntry("Harmony Island", CoordGrid(0, 59, 44, 21, 51), 74),
            TeleportEntry("Cemetery", CoordGrid(0, 46, 58, 36, 51), 82),
            TeleportEntry("Barrows", CoordGrid(0, 55, 51, 45, 50), 90),
            TeleportEntry("Arceuus Ape Atoll", CoordGrid(0, 43, 142, 19, 13), 100),
        )

    fun forCategory(category: TeleportCategory): List<TeleportEntry> =
        when (category) {
            TeleportCategory.STANDARD -> standard
            TeleportCategory.ANCIENT -> ancient
            TeleportCategory.LUNAR -> lunar
            TeleportCategory.ARCEUUS -> arceuus
        }
}
