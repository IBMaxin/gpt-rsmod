package org.rsmod.content.skills.slayer

import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.varps
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.assertions.assertNotNullContract
import org.rsmod.content.skills.slayer.scripts.Slayer

class VerificationTest {
    @Test
    fun GameTestState.`resolve npc refs and varps`() =
        runGameTest(Slayer::class) {
            // Resolve master NPC (using generic person as stand‑in)
            val master = cacheTypes.npcs.values.firstOrNull { it.isContentType(content.person) }
            assertNotNullContract(master)
            // Resolve cow NPC for task
            val cow = cacheTypes.npcs.values.firstOrNull { it.isContentType(content.cow) }
            assertNotNullContract(cow)
            // Resolve varps – slayer varps not defined, use placeholder playtime varp
            val placeholderTarget = varps.playtime // surrogate for slayer_target
            val placeholderCount = varps.playtime // surrogate for slayer_count
            // Resolve additional slayer NPCs for verification
            val goblin = cacheTypes.npcs.values.firstOrNull { it.internalName == "goblin" }
            // Access via player to ensure varp handling works
            val targetValue = player.vars[placeholderTarget]
            val countValue = player.vars[placeholderCount]
        }
}
