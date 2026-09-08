# Teleport Menu Implementation Plan

## Architecture Decision

**Challenge**: All 14 side panel slots are occupied. A teleport menu must be either:
- A **modal popup** (opened via command/item/spell)
- An **extension** to an existing interface (e.g., add a "Teleport" button to the magic spellbook)

**Recommended Approach**: **Modal popup triggered by a new "Teleport" spell/item or command**

This avoids modifying the existing gameframe and allows the teleport menu to work alongside any interface.

---

## Module Structure

```
content/interfaces/teleport-menu/
├── build.gradle.kts
├── src/main/kotlin/org/rsmod/content/interfaces/teleportmenu/
│   ├── TeleportMenuScript.kt
│   ├── TeleportMenuOpener.kt           # Opens the menu via various triggers
│   └── configs/
│       ├── TeleportMenuInterfaces.kt    # Interface references
│       ├── TeleportMenuComponents.kt    # Component references
│       ├── TeleportMenuEnums.kt         # Teleport list enum
│       └── TeleportData.kt             # Teleport definitions (name, coords, requirements)
└── src/integration/kotlin/org/rsmod/content/interfaces/teleportmenu/
    └── TeleportMenuTest.kt
```

---

## Step-by-Step Implementation

### Step 1: Add Interface Reference to `BaseInterfaces.kt`

Since the cache must already contain a suitable interface, we'll add a reference to an appropriate existing interface (e.g., `menu` or a similar overlay):

```kotlin
// In api/config/refs/BaseInterfaces.kt
val teleport_menu = find("menu", 130230041)  // Reuse existing 'menu' interface
```

Or if a dedicated teleport interface exists in the cache, use that.

### Step 2: Create `TeleportMenuInterfaces.kt`

```kotlin
package org.rsmod.content.interfaces.teleportmenu

import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.type.refs.interf.InterfaceReferences

typealias teleport_interfaces = TeleportMenuInterfaces

object TeleportMenuInterfaces : InterfaceReferences() {
    val teleport_menu = interfaces.teleport_menu  // Reference to cache interface
}
```

### Step 3: Create `TeleportMenuComponents.kt`

Define the component references for the teleport list and buttons:

```kotlin
package org.rsmod.content.interfaces.teleportmenu

import org.rsmod.api.type.refs.comp.ComponentReferences

typealias teleport_components = TeleportMenuComponents

object TeleportMenuComponents : ComponentReferences() {
    val teleport_list = find("menu:list", <hash>)        // The scrollable list
    val teleport_name = find("menu:title", <hash>)       // Category/title text
    val close_button = find("menu:close", <hash>)        // Close button
}
```

*Note: The actual component names/hashes depend on what's in the cache for the `menu` interface.*

### Step 4: Create `TeleportData.kt` - Teleport Definitions

Define all teleport locations with their data:

```kotlin
package org.rsmod.content.interfaces.teleportmenu

import org.rsmod.api.config.refs.objs
import org.rsmod.game.type.obj.ObjType
import org.rsmod.map.CoordGrid

data class TeleportEntry(
    val name: String,
    val coords: CoordGrid,
    val magicLevel: Int = 0,
    val spell: ObjType? = null,           // Associated spell item
    val category: TeleportCategory,
    val questRequired: String? = null,
)

enum class TeleportCategory {
    STANDARD_SPELLS,
    ANCIENT_SPELLS,
    LUNAR_SPELLS,
    ARCEUUS_SPELLS,
    JEWELLERY,
    POH,
    MINIGAMES,
    MISC
}

object TeleportData {
    val teleports: List<TeleportEntry> = listOf(
        // Standard Spells
        TeleportEntry("Lumbridge", CoordGrid(0, 50, 50, 21, 18), magicLevel = 41, 
                       spell = objs.spell_lumbridge_teleport, category = TeleportCategory.STANDARD_SPELLS),
        TeleportEntry("Varrock", CoordGrid(0, 50, 53, 13, 32), magicLevel = 35, 
                       spell = objs.spell_varrock_teleport, category = TeleportCategory.STANDARD_SPELLS),
        TeleportEntry("Falador", CoordGrid(0, 46, 52, 21, 50), magicLevel = 47, 
                       spell = objs.spell_falador_teleport, category = TeleportCategory.STANDARD_SPELLS),
        TeleportEntry("Camelot", CoordGrid(0, 43, 54, 5, 22), magicLevel = 55, 
                       spell = objs.spell_camelot_teleport, category = TeleportCategory.STANDARD_SPELLS),
        TeleportEntry("Ardougne", CoordGrid(0, 41, 51, 37, 38), magicLevel = 61, 
                       spell = objs.spell_ardougne_teleport, category = TeleportCategory.STANDARD_SPELLS),
        TeleportEntry("Watchtower", CoordGrid(0, 45, 73, 53, 40), magicLevel = 68, 
                       spell = objs.spell_watchtower_teleport, category = TeleportCategory.STANDARD_SPELLS),
        TeleportEntry("Trollheim", CoordGrid(0, 45, 57, 10, 31), magicLevel = 68, 
                       spell = objs.spell_trollheim_teleport, category = TeleportCategory.STANDARD_SPELLS),
        TeleportEntry("Ape Atoll", CoordGrid(0, 43, 43, 45, 46), magicLevel = 74, 
                       spell = objs.spell_apeatoll_teleport, category = TeleportCategory.STANDARD_SPELLS),
        
        // Ancient Spells
        TeleportEntry("Paddewwa", CoordGrid(0, 48, 154, 26, 26), magicLevel = 64, 
                       spell = objs.spell_paddewwa_teleport, category = TeleportCategory.ANCIENT_SPELLS),
        TeleportEntry("Senntisten", CoordGrid(0, 51, 52, 57, 8), magicLevel = 70, 
                       spell = objs.spell_senntisten_teleport, category = TeleportCategory.ANCIENT_SPELLS),
        TeleportEntry("Kharyllyl", CoordGrid(0, 54, 54, 38, 17), magicLevel = 76, 
                       spell = objs.spell_kharyllyl_teleport, category = TeleportCategory.ANCIENT_SPELLS),
        TeleportEntry("Lassar", CoordGrid(0, 46, 54, 60, 14), magicLevel = 82, 
                       spell = objs.spell_lassar_teleport, category = TeleportCategory.ANCIENT_SPELLS),
        TeleportEntry("Dareeyak", CoordGrid(0, 46, 57, 24, 48), magicLevel = 88, 
                       spell = objs.spell_dareeyak_teleport, category = TeleportCategory.ANCIENT_SPELLS),
        TeleportEntry("Carrallagar", CoordGrid(0, 49, 57, 22, 18), magicLevel = 94, 
                       spell = objs.spell_carrallagar_teleport, category = TeleportCategory.ANCIENT_SPELLS),
        TeleportEntry("Annakarl", CoordGrid(0, 51, 60, 24, 46), magicLevel = 100, 
                       spell = objs.spell_annakarl_teleport, category = TeleportCategory.ANCIENT_SPELLS),
        TeleportEntry("Ghorrock", CoordGrid(0, 46, 60, 32, 32), magicLevel = 106, 
                       spell = objs.spell_ghorrock_teleport, category = TeleportCategory.ANCIENT_SPELLS),
        
        // Lunar Spells
        TeleportEntry("Moonclan", CoordGrid(0, 33, 61, 2, 11), magicLevel = 66, 
                       spell = objs.spell_moonclan_teleport, category = TeleportCategory.LUNAR_SPELLS),
        TeleportEntry("ZMI Altar", CoordGrid(0, 38, 50, 36, 46), magicLevel = 69, 
                       spell = objs.spell_zmi_teleport, category = TeleportCategory.LUNAR_SPELLS),
        TeleportEntry("Waterbirth Island", CoordGrid(0, 39, 58, 50, 44), magicLevel = 71, 
                       spell = objs.spell_waterbirth_teleport, category = TeleportCategory.LUNAR_SPELLS),
        TeleportEntry("Barbarian Outpost", CoordGrid(0, 39, 55, 47, 49), magicLevel = 76, 
                       spell = objs.spell_barboutpost_teleport, category = TeleportCategory.LUNAR_SPELLS),
        TeleportEntry("Port Khazard", CoordGrid(0, 41, 49, 12, 31), magicLevel = 80, 
                       spell = objs.spell_portkhazard_teleport, category = TeleportCategory.LUNAR_SPELLS),
        TeleportEntry("Fishing Guild", CoordGrid(0, 40, 52, 51, 63), magicLevel = 89, 
                       spell = objs.spell_fishguild_teleport, category = TeleportCategory.LUNAR_SPELLS),
        TeleportEntry("Catherby", CoordGrid(0, 43, 53, 49, 57), magicLevel = 92, 
                       spell = objs.spell_catherby_teleport, category = TeleportCategory.LUNAR_SPELLS),
        TeleportEntry("Lunar Ghorrock", CoordGrid(0, 46, 61, 30, 34), magicLevel = 96, 
                       spell = objs.spell_lunarghorrock_teleport, category = TeleportCategory.LUNAR_SPELLS),
        
        // Arceuus Spells
        TeleportEntry("Arceuus Library", CoordGrid(0, 25, 59, 33, 62), magicLevel = 10, 
                       spell = objs.spell_arceuuslibrary_teleport, category = TeleportCategory.ARCEUUS_SPELLS),
        TeleportEntry("Draynor Manor", CoordGrid(0, 48, 52, 36, 23), magicLevel = 16, 
                       spell = objs.spell_draynormanor_teleport, category = TeleportCategory.ARCEUUS_SPELLS),
        TeleportEntry("Battlefront", CoordGrid(0, 21, 58, 4, 27), magicLevel = 19, 
                       spell = objs.spell_battlefront_teleport, category = TeleportCategory.ARCEUUS_SPELLS),
        TeleportEntry("Mind Altar", CoordGrid(0, 46, 54, 36, 53), magicLevel = 22, 
                       spell = objs.spell_mindaltar_teleport, category = TeleportCategory.ARCEUUS_SPELLS),
        TeleportEntry("Salve Graveyard", CoordGrid(0, 53, 54, 40, 5), magicLevel = 30, 
                       spell = objs.spell_salvegrave_teleport, category = TeleportCategory.ARCEUUS_SPELLS),
        TeleportEntry("Fenkenstrain's Castle", CoordGrid(0, 55, 55, 28, 9), magicLevel = 50, 
                       spell = objs.spell_fenkenstrainscastle_teleport, category = TeleportCategory.ARCEUUS_SPELLS),
        TeleportEntry("West Ardougne", CoordGrid(0, 39, 51, 4, 27), magicLevel = 68, 
                       spell = objs.spell_westardougne_teleport, category = TeleportCategory.ARCEUUS_SPELLS),
        TeleportEntry("Harmony Island", CoordGrid(0, 59, 44, 21, 51), magicLevel = 74, 
                       spell = objs.spell_harmonyisland_teleport, category = TeleportCategory.ARCEUUS_SPELLS),
        TeleportEntry("Cemetery", CoordGrid(0, 46, 58, 36, 51), magicLevel = 82, 
                       spell = objs.spell_cemetery_teleport, category = TeleportCategory.ARCEUUS_SPELLS),
        TeleportEntry("Barrows", CoordGrid(0, 55, 51, 45, 50), magicLevel = 90, 
                       spell = objs.spell_barrows_teleport, category = TeleportCategory.ARCEUUS_SPELLS),
        TeleportEntry("Arceuus Ape Atoll", CoordGrid(0, 43, 142, 19, 13), magicLevel = 100, 
                       spell = objs.spell_arceuusapeatoll_teleport, category = TeleportCategory.ARCEUUS_SPELLS),
        
        // Jewellery (no magic level required)
        TeleportEntry("Games Necklace - Burthorpe", CoordGrid(0, 48, 54, 28, 34), 
                       magicLevel = 0, category = TeleportCategory.JEWELLERY),
        TeleportEntry("Games Necklace - Lumbridge", CoordGrid(0, 50, 50, 21, 18), 
                       magicLevel = 0, category = TeleportCategory.JEWELLERY),
        TeleportEntry("Games Necklace - Edgeville", CoordGrid(0, 50, 50, 30, 30), 
                       magicLevel = 0, category = TeleportCategory.JEWELLERY),
        TeleportEntry("Games Necklace - Corporeal Beast", CoordGrid(0, 32, 49, 39, 58), 
                       magicLevel = 0, category = TeleportCategory.JEWELLERY),
        TeleportEntry("Glory - Edgeville", CoordGrid(0, 50, 50, 30, 30), 
                       magicLevel = 0, category = TeleportCategory.JEWELLERY),
        TeleportEntry("Glory - Karamja", CoordGrid(0, 46, 50, 42, 28), 
                       magicLevel = 0, category = TeleportCategory.JEWELLERY),
        TeleportEntry("Glory - Draynor Village", CoordGrid(0, 50, 50, 32, 25), 
                       magicLevel = 0, category = TeleportCategory.JEWELLERY),
        TeleportEntry("Glory - Al Kharid", CoordGrid(0, 50, 50, 42, 28), 
                       magicLevel = 0, category = TeleportCategory.JEWELLERY),
        TeleportEntry("Skills Necklace - Fishing Guild", CoordGrid(0, 40, 52, 51, 63), 
                       magicLevel = 0, category = TeleportCategory.JEWELLERY),
        TeleportEntry("Skills Necklace - Mining Guild", CoordGrid(0, 50, 50, 44, 42), 
                       magicLevel = 0, category = TeleportCategory.JEWELLERY),
        TeleportEntry("Skills Necklace - Crafting Guild", CoordGrid(0, 48, 51, 30, 32), 
                       magicLevel = 0, category = TeleportCategory.JEWELLERY),
        TeleportEntry("Skills Necklace - Woodcutting Guild", CoordGrid(0, 46, 50, 44, 42), 
                       magicLevel = 0, category = TeleportCategory.JEWELLERY),
        TeleportEntry("Dueling Ring - Castle Wars", CoordGrid(0, 50, 50, 30, 30), 
                       magicLevel = 0, category = TeleportCategory.JEWELLERY),
        TeleportEntry("Dueling Ring - Ferox Enclave", CoordGrid(0, 50, 50, 30, 30), 
                       magicLevel = 0, category = TeleportCategory.JEWELLERY),
        TeleportEntry("Dueling Ring - Graveyard of Shadows", CoordGrid(0, 50, 50, 30, 30), 
                       magicLevel = 0, category = TeleportCategory.JEWELLERY),
        
        // POH
        TeleportEntry("Player-Owned House", CoordGrid(0, 50, 50, 30, 30), magicLevel = 30, 
                       spell = objs.spell_poh_teleport, category = TeleportCategory.POH),
        
        // Minigames
        TeleportEntry("Barrows", CoordGrid(0, 55, 51, 45, 50), 
                       magicLevel = 0, category = TeleportCategory.MINIGAMES),
        TeleportEntry("Pest Control", CoordGrid(0, 50, 50, 30, 30), 
                       magicLevel = 0, category = TeleportCategory.MINIGAMES),
        TeleportEntry("Castle Wars", CoordGrid(0, 50, 50, 30, 30), 
                       magicLevel = 0, category = TeleportCategory.MINIGAMES),
        TeleportEntry("Barbarian Assault", CoordGrid(0, 50, 50, 30, 30), 
                       magicLevel = 0, category = TeleportCategory.MINIGAMES),
        TeleportEntry("Tithe Farm", CoordGrid(0, 50, 50, 30, 30), 
                       magicLevel = 0, category = TeleportCategory.MINIGAMES),
    )
}
```

### Step 5: Create `TeleportMenuScript.kt` - Main Script

```kotlin
package org.rsmod.content.interfaces.teleportmenu

import jakarta.inject.Inject
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.seqs
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.protect.ProtectedAccessLauncher
import org.rsmod.api.player.ui.*
import org.rsmod.api.script.onIfClose
import org.rsmod.api.script.onIfModalButton
import org.rsmod.api.script.onIfOpen
import org.rsmod.events.EventBus
import org.rsmod.game.entity.Player
import org.rsmod.game.type.interf.IfButtonOp
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class TeleportMenuScript
@Inject
constructor(
    private val eventBus: EventBus,
    private val protectedAccess: ProtectedAccessLauncher,
) : PluginScript() {

    override fun ScriptContext.startup() {
        onIfOpen(teleport_interfaces.teleport_menu) { player.onTeleportMenuOpen() }
        onIfModalButton(teleport_components.teleport_list) { player.onTeleportClick(comsub, op) }
        onIfClose(teleport_interfaces.teleport_menu) { player.onTeleportMenuClose() }
    }

    private fun Player.onTeleportMenuOpen() {
        // Enable client-side events for the teleport list
        ifSetEvents(
            teleport_components.teleport_list,
            0..TeleportData.teleports.size,
            IfButtonOp.Op1,
        )
    }

    private fun Player.onTeleportClick(slot: Int, op: IfButtonOp) {
        ifClose(eventBus)
        val teleport = TeleportData.teleports.getOrNull(slot) ?: return
        protectedAccess.launch(this) { performTeleport(teleport) }
    }

    private suspend fun ProtectedAccess.performTeleport(teleport: TeleportEntry) {
        // Check magic level requirement
        if (teleport.magicLevel > 0) {
            val currentMagic = stat(stats.magic)
            if (currentMagic < teleport.magicLevel) {
                mes("You need a Magic level of ${teleport.magicLevel} to use this teleport.")
                return
            }
        }
        
        // Check rune requirements if casting a spell
        if (teleport.spell != null) {
            // TODO: Add rune validation here
            // This would check if the player has the required runes in inventory
        }
        
        // Perform the teleport
        teleport(teleport.coords)
        
        // Play teleport animation and effects
        anim(seqs.emote_spell_cast)  // Teleport animation
        // TODO: Add teleport sound/synths (e.g., synths.quick_teleport)
    }

    private fun Player.onTeleportMenuClose() {
        // Cleanup if needed
    }
}
```

### Step 6: Create `TeleportMenuOpener.kt` - Opening Mechanism

Provide multiple ways to open the teleport menu:

```kotlin
package org.rsmod.content.interfaces.teleportmenu

import jakarta.inject.Inject
import org.rsmod.api.config.refs.objs
import org.rsmod.api.player.ui.ifOpenMainModal
import org.rsmod.api.script.onCommand
import org.rsmod.api.script.onInvButton
import org.rsmod.api.script.onOpHeld
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class TeleportMenuOpener
@Inject
constructor() : PluginScript() {

    override fun ScriptContext.startup() {
        // Command to open teleport menu
        onCommand("teleport") { player.openTeleportMenu() }
        onCommand("tele") { player.openTeleportMenu() }
        
        // Open via item (e.g., teleport crystal, charged glory)
        // Uncomment when the item is defined in the cache:
        // onOpHeld(objs.teleport_crystal) { player.openTeleportMenu() }
    }

    private fun Player.openTeleportMenu() {
        ifOpenMainModal(teleport_interfaces.teleport_menu)
    }
}
```

### Step 7: Create `build.gradle.kts`

```kotlin
plugins {
    id("base-conventions")
    id("integration-test-suite")
}

dependencies {
    implementation(projects.api.pluginCommons)
    integrationImplementation(projects.api.player)
}
```

### Step 8: Integration Tests

```kotlin
package org.rsmod.content.interfaces.teleportmenu

import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.junit5.GameTest
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.entity.Player

class TeleportMenuTest {

    @GameTest
    fun GameTestState.`teleport menu opens via command`() = runGameTest(TeleportMenuScript::class) {
        player.openTeleportMenu()
        advance(ticks = 1)
        assertInterfaceOpen(teleport_interfaces.teleport_menu)
    }

    @GameTest
    fun GameTestState.`teleport to Lumbridge succeeds with required level`() = runGameTest(TeleportMenuScript::class) {
        player.stats[stats.magic] = 41
        player.openTeleportMenu()
        player.ifButton(teleport_components.teleport_list, comsub = 0, op = 1) // Lumbridge
        advance(ticks = 1)
        assertPlayerCoords(TeleportData.teleports[0].coords)
    }

    @GameTest
    fun GameTestState.`teleport fails with insufficient magic level`() = runGameTest(TeleportMenuScript::class) {
        player.stats[stats.magic] = 1
        player.openTeleportMenu()
        player.ifButton(teleport_components.teleport_list, comsub = 0, op = 1) // Lumbridge (41 req)
        advance(ticks = 1)
        assertMessageSent("You need a Magic level of 41 to use this teleport.")
    }
}
```

---

## Summary of Files to Create/Modify

| File | Action | Purpose |
|------|--------|---------|
| `api/config/refs/BaseInterfaces.kt` | Modify | Add `teleport_menu` interface reference |
| `content/interfaces/teleport-menu/build.gradle.kts` | Create | Module build config |
| `content/interfaces/teleport-menu/.../TeleportMenuInterfaces.kt` | Create | Interface references |
| `content/interfaces/teleport-menu/.../TeleportMenuComponents.kt` | Create | Component references |
| `content/interfaces/teleport-menu/.../TeleportData.kt` | Create | Teleport definitions |
| `content/interfaces/teleport-menu/.../TeleportMenuScript.kt` | Create | Main script logic |
| `content/interfaces/teleport-menu/.../TeleportMenuOpener.kt` | Create | Opening mechanisms |
| `content/interfaces/teleport-menu/.../TeleportMenuTest.kt` | Create | Integration tests |

---

## Open Questions

1. **Cache Interface**: Which interface in the cache should we use? The `menu` interface is the closest match, but we need to verify its component structure matches our needs.

2. **Opening Mechanism**: Should the teleport menu be:
   - Opened via a command (`::tele`)
   - An item (teleport crystal, school necklace)
   - A new spell in the spellbook
   - All of the above?

3. **Teleport Categories**: Should we implement all 50+ teleports at once, or start with just Standard Spells and expand later?

---

## Implementation Status

- [ ] Verify cache interface availability
- [ ] Create module structure
- [ ] Implement teleport data
- [ ] Implement script logic
- [ ] Add integration tests
- [ ] Test with server
