package org.rsmod.content.skills.cooking.configs

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.content
import org.rsmod.api.testing.GameTestState

class CookingConfigTest {
    @Test
    fun GameTestState.`ensure all cooking ranges have content group`() = runBasicGameTest {
        val ranges = cacheTypes.locs.values.filter { it.isContentType(content.cooking_range) }
        assertTrue(ranges.isNotEmpty(), "No cooking ranges found in cache")
        for (range in ranges) {
            assertTrue(range.isContentType(content.cooking_range))
        }
    }
}
