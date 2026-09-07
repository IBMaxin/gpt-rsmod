package org.rsmod.content.skills.fletching

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.params
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.assertions.assertNotNullContract
import org.rsmod.content.skills.fletching.configs.FletchingObjRefs

class FletchingConfigTest {
    @Test
    fun GameTestState.`verify all fletching item refs exist in cache`() = runBasicGameTest {
        val refs =
            listOf(
                FletchingObjRefs.knife,
                FletchingObjRefs.logs,
                FletchingObjRefs.oak_logs,
                FletchingObjRefs.willow_logs,
                FletchingObjRefs.maple_logs,
                FletchingObjRefs.yew_logs,
                FletchingObjRefs.magic_logs,
                FletchingObjRefs.bow_string,
                FletchingObjRefs.arrow_shaft,
                FletchingObjRefs.feather,
                FletchingObjRefs.headless_arrow,
                FletchingObjRefs.bronze_arrowheads,
                FletchingObjRefs.iron_arrowheads,
                FletchingObjRefs.steel_arrowheads,
                FletchingObjRefs.mithril_arrowheads,
                FletchingObjRefs.adamant_arrowheads,
                FletchingObjRefs.rune_arrowheads,
                FletchingObjRefs.bronze_arrow,
                FletchingObjRefs.iron_arrow,
                FletchingObjRefs.steel_arrow,
                FletchingObjRefs.mithril_arrow,
                FletchingObjRefs.adamant_arrow,
                FletchingObjRefs.rune_arrow,
                FletchingObjRefs.unstrung_shortbow,
                FletchingObjRefs.unstrung_longbow,
                FletchingObjRefs.unstrung_oak_shortbow,
                FletchingObjRefs.unstrung_oak_longbow,
                FletchingObjRefs.unstrung_willow_shortbow,
                FletchingObjRefs.unstrung_willow_longbow,
                FletchingObjRefs.unstrung_maple_shortbow,
                FletchingObjRefs.unstrung_maple_longbow,
                FletchingObjRefs.unstrung_yew_shortbow,
                FletchingObjRefs.unstrung_yew_longbow,
                FletchingObjRefs.unstrung_magic_shortbow,
                FletchingObjRefs.unstrung_magic_longbow,
                FletchingObjRefs.shortbow,
                FletchingObjRefs.longbow,
                FletchingObjRefs.oak_shortbow,
                FletchingObjRefs.oak_longbow,
                FletchingObjRefs.willow_shortbow,
                FletchingObjRefs.willow_longbow,
                FletchingObjRefs.maple_shortbow,
                FletchingObjRefs.maple_longbow,
                FletchingObjRefs.yew_shortbow,
                FletchingObjRefs.yew_longbow,
                FletchingObjRefs.magic_shortbow,
                FletchingObjRefs.magic_longbow,
            )
        for (ref in refs) {
            val type = cacheTypes.objs[ref.id]
            assertNotNull(type, "Obj ref '${ref.internalName}' (id=${ref.id}) not found in cache")
        }
        assertTrue(refs.isNotEmpty())
    }

    @Test
    fun GameTestState.`ensure knife has required params`() = runBasicGameTest {
        val knife = cacheTypes.objs[FletchingObjRefs.knife.id]
        assertNotNull(knife)
        assertTrue(knife!!.isContentType(content.fletching_knife))
    }

    @Test
    fun GameTestState.`ensure all log types have required params`() = runBasicGameTest {
        val logs =
            listOf(
                FletchingObjRefs.logs,
                FletchingObjRefs.oak_logs,
                FletchingObjRefs.willow_logs,
                FletchingObjRefs.maple_logs,
                FletchingObjRefs.yew_logs,
                FletchingObjRefs.magic_logs,
            )
        for (ref in logs) {
            val type = cacheTypes.objs[ref.id]
            assertNotNull(type, "Log ref '${ref.internalName}' not found in cache")
            val logParams = type!!.paramMap
            assertNotNullContract(logParams)
            assertTrue(params.levelrequire in logParams, "${ref.internalName} missing levelrequire")
            assertTrue(params.skill_xp in logParams, "${ref.internalName} missing skill_xp")
            assertTrue(
                params.skill_productitem in logParams,
                "${ref.internalName} missing skill_productitem",
            )
        }
    }

    @Test
    fun GameTestState.`ensure all arrowhead types have required params`() = runBasicGameTest {
        val arrowheads =
            listOf(
                FletchingObjRefs.bronze_arrowheads,
                FletchingObjRefs.iron_arrowheads,
                FletchingObjRefs.steel_arrowheads,
                FletchingObjRefs.mithril_arrowheads,
                FletchingObjRefs.adamant_arrowheads,
                FletchingObjRefs.rune_arrowheads,
            )
        for (ref in arrowheads) {
            val type = cacheTypes.objs[ref.id]
            assertNotNull(type, "Arrowhead ref '${ref.internalName}' not found in cache")
            val tipParams = type!!.paramMap
            assertNotNullContract(tipParams)
            assertTrue(params.levelrequire in tipParams, "${ref.internalName} missing levelrequire")
            assertTrue(params.skill_xp in tipParams, "${ref.internalName} missing skill_xp")
            assertTrue(
                params.skill_productitem in tipParams,
                "${ref.internalName} missing skill_productitem",
            )
        }
    }

    @Test
    fun GameTestState.`ensure all unstrung bow types have required params`() = runBasicGameTest {
        val unstrungBows =
            listOf(
                FletchingObjRefs.unstrung_shortbow,
                FletchingObjRefs.unstrung_longbow,
                FletchingObjRefs.unstrung_oak_shortbow,
                FletchingObjRefs.unstrung_oak_longbow,
                FletchingObjRefs.unstrung_willow_shortbow,
                FletchingObjRefs.unstrung_willow_longbow,
                FletchingObjRefs.unstrung_maple_shortbow,
                FletchingObjRefs.unstrung_maple_longbow,
                FletchingObjRefs.unstrung_yew_shortbow,
                FletchingObjRefs.unstrung_yew_longbow,
                FletchingObjRefs.unstrung_magic_shortbow,
                FletchingObjRefs.unstrung_magic_longbow,
            )
        for (ref in unstrungBows) {
            val type = cacheTypes.objs[ref.id]
            assertNotNull(type, "Unstrung bow ref '${ref.internalName}' not found in cache")
            val bowParams = type!!.paramMap
            assertNotNullContract(bowParams)
            assertTrue(params.levelrequire in bowParams, "${ref.internalName} missing levelrequire")
            assertTrue(params.skill_xp in bowParams, "${ref.internalName} missing skill_xp")
            assertTrue(
                params.skill_productitem in bowParams,
                "${ref.internalName} missing skill_productitem",
            )
        }
    }

    @Test
    fun GameTestState.`ensure feather has required params`() = runBasicGameTest {
        val feather = cacheTypes.objs[FletchingObjRefs.feather.id]
        assertNotNull(feather)
        val featherParams = feather!!.paramMap
        assertNotNullContract(featherParams)
        assertTrue(params.levelrequire in featherParams)
        assertTrue(params.skill_xp in featherParams)
    }

    @Test
    fun GameTestState.`ensure arrow shaft has required params`() = runBasicGameTest {
        val shaft = cacheTypes.objs[FletchingObjRefs.arrow_shaft.id]
        assertNotNull(shaft)
        val shaftParams = shaft!!.paramMap
        assertNotNullContract(shaftParams)
        assertTrue(params.levelrequire in shaftParams)
        assertTrue(params.skill_xp in shaftParams)
        assertTrue(params.skill_productitem in shaftParams)
    }
}
