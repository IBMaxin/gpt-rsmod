package org.rsmod.content.skills.fletching.scripts

import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.events.interact.HeldUEvents
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.skills.fletching.configs.FletchingObjRefs
import org.rsmod.game.inv.InvObj

class FletchingScriptTest {
    @Test
    fun GameTestState.`knife on logs requires fletching level`() =
        runGameTest(FletchingBow::class) {
            player.clearInv()
            player.stats[stats.fletching] = 0

            player.inv[0] = InvObj(FletchingObjRefs.knife, 1)
            player.inv[1] = InvObj(FletchingObjRefs.logs, 1)

            player.withProtectedAccess {
                val knife = objTypes[FletchingObjRefs.knife]
                val logs = objTypes[FletchingObjRefs.logs]
                val event = HeldUEvents.Type(knife, 0, logs, 1)
                eventBus.publish(this, event)
            }

            assertMessageSent("You need a Fletching level of 1 to make Shortbow (u).")
        }

    @Test
    fun GameTestState.`knife on logs makes unstrung bow`() =
        runGameTest(FletchingBow::class) {
            player.clearInv()
            player.stats[stats.fletching] = 5

            player.inv[0] = InvObj(FletchingObjRefs.knife, 1)
            player.inv[1] = InvObj(FletchingObjRefs.logs, 1)

            player.withProtectedAccess {
                val knife = objTypes[FletchingObjRefs.knife]
                val logs = objTypes[FletchingObjRefs.logs]
                val event = HeldUEvents.Type(knife, 0, logs, 1)
                eventBus.publish(this, event)
            }

            advance(ticks = 1)
            assertContains(player.inv, FletchingObjRefs.unstrung_shortbow)
            assertDoesNotContain(player.inv, FletchingObjRefs.logs)
        }

    @Test
    fun GameTestState.`bow string on unstrung bow requires level`() =
        runGameTest(FletchingBowString::class) {
            player.clearInv()
            player.stats[stats.fletching] = 0

            player.inv[0] = InvObj(FletchingObjRefs.bow_string, 1)
            player.inv[1] = InvObj(FletchingObjRefs.unstrung_shortbow, 1)

            player.withProtectedAccess {
                val bowString = objTypes[FletchingObjRefs.bow_string]
                val unstrung = objTypes[FletchingObjRefs.unstrung_shortbow]
                val event = HeldUEvents.Type(bowString, 0, unstrung, 1)
                eventBus.publish(this, event)
            }

            assertMessageSent("You need a Fletching level of 5 to string this bow.")
        }

    @Test
    fun GameTestState.`bow string on unstrung bow makes strung bow`() =
        runGameTest(FletchingBowString::class) {
            player.clearInv()
            player.stats[stats.fletching] = 5

            player.inv[0] = InvObj(FletchingObjRefs.bow_string, 1)
            player.inv[1] = InvObj(FletchingObjRefs.unstrung_shortbow, 1)

            player.withProtectedAccess {
                val bowString = objTypes[FletchingObjRefs.bow_string]
                val unstrung = objTypes[FletchingObjRefs.unstrung_shortbow]
                val event = HeldUEvents.Type(bowString, 0, unstrung, 1)
                eventBus.publish(this, event)
            }

            advance(ticks = 1)
            assertContains(player.inv, FletchingObjRefs.shortbow)
            assertDoesNotContain(player.inv, FletchingObjRefs.unstrung_shortbow)
            assertDoesNotContain(player.inv, FletchingObjRefs.bow_string)
        }

    @Test
    fun GameTestState.`headless arrows require level`() =
        runGameTest(FletchingHeadlessArrow::class) {
            player.clearInv()
            player.stats[stats.fletching] = 0

            player.inv[0] = InvObj(FletchingObjRefs.feather, 15)
            player.inv[1] = InvObj(FletchingObjRefs.arrow_shaft, 15)

            player.withProtectedAccess {
                val feather = objTypes[FletchingObjRefs.feather]
                val shaft = objTypes[FletchingObjRefs.arrow_shaft]
                val event = HeldUEvents.Type(feather, 0, shaft, 1)
                eventBus.publish(this, event)
            }

            assertMessageSent("You need a Fletching level of 1 to make headless arrows.")
        }

    @Test
    fun GameTestState.`feather on arrow shafts makes headless arrows`() =
        runGameTest(FletchingHeadlessArrow::class) {
            player.clearInv()
            player.stats[stats.fletching] = 1

            player.inv[0] = InvObj(FletchingObjRefs.feather, 15)
            player.inv[1] = InvObj(FletchingObjRefs.arrow_shaft, 15)

            player.withProtectedAccess {
                val feather = objTypes[FletchingObjRefs.feather]
                val shaft = objTypes[FletchingObjRefs.arrow_shaft]
                val event = HeldUEvents.Type(feather, 0, shaft, 1)
                eventBus.publish(this, event)
            }

            advance(ticks = 1)
            assertContains(player.inv, FletchingObjRefs.headless_arrow)
            assertDoesNotContain(player.inv, FletchingObjRefs.feather)
            assertDoesNotContain(player.inv, FletchingObjRefs.arrow_shaft)
        }

    @Test
    fun GameTestState.`headless arrows on arrowheads requires level`() =
        runGameTest(FletchingArrow::class) {
            player.clearInv()
            player.stats[stats.fletching] = 0

            player.inv[0] = InvObj(FletchingObjRefs.headless_arrow, 15)
            player.inv[1] = InvObj(FletchingObjRefs.bronze_arrowheads, 15)

            player.withProtectedAccess {
                val headless = objTypes[FletchingObjRefs.headless_arrow]
                val tips = objTypes[FletchingObjRefs.bronze_arrowheads]
                val event = HeldUEvents.Type(headless, 0, tips, 1)
                eventBus.publish(this, event)
            }

            assertMessageSent("You need a Fletching level of 1 to make Bronze arrow.")
        }

    @Test
    fun GameTestState.`headless arrows on arrowheads makes arrows`() =
        runGameTest(FletchingArrow::class) {
            player.clearInv()
            player.stats[stats.fletching] = 1

            player.inv[0] = InvObj(FletchingObjRefs.headless_arrow, 15)
            player.inv[1] = InvObj(FletchingObjRefs.bronze_arrowheads, 15)

            player.withProtectedAccess {
                val headless = objTypes[FletchingObjRefs.headless_arrow]
                val tips = objTypes[FletchingObjRefs.bronze_arrowheads]
                val event = HeldUEvents.Type(headless, 0, tips, 1)
                eventBus.publish(this, event)
            }

            advance(ticks = 1)
            assertContains(player.inv, FletchingObjRefs.bronze_arrow)
            assertDoesNotContain(player.inv, FletchingObjRefs.headless_arrow)
            assertDoesNotContain(player.inv, FletchingObjRefs.bronze_arrowheads)
        }

    @Test
    fun GameTestState.`inventory full prevents bow making`() =
        runGameTest(FletchingBow::class) {
            player.clearInv()
            player.stats[stats.fletching] = 100

            player.inv[0] = InvObj(FletchingObjRefs.knife, 1)
            player.inv[1] = InvObj(FletchingObjRefs.logs, 1)
            for (i in 2..27) {
                player.inv[i] = InvObj(FletchingObjRefs.arrow_shaft, 1)
            }

            player.withProtectedAccess {
                val knife = objTypes[FletchingObjRefs.knife]
                val logs = objTypes[FletchingObjRefs.logs]
                val event = HeldUEvents.Type(knife, 0, logs, 1)
                eventBus.publish(this, event)
            }

            assertMessageSent("Your inventory is too full to hold any more shortbow (u).")
            assertDoesNotContain(player.inv, FletchingObjRefs.unstrung_shortbow)
        }

    @Test
    fun GameTestState.`inventory full prevents bow stringing`() =
        runGameTest(FletchingBowString::class) {
            player.clearInv()
            player.stats[stats.fletching] = 100

            player.inv[0] = InvObj(FletchingObjRefs.bow_string, 1)
            player.inv[1] = InvObj(FletchingObjRefs.unstrung_shortbow, 1)
            for (i in 2..27) {
                player.inv[i] = InvObj(FletchingObjRefs.arrow_shaft, 1)
            }

            player.withProtectedAccess {
                val bowString = objTypes[FletchingObjRefs.bow_string]
                val unstrung = objTypes[FletchingObjRefs.unstrung_shortbow]
                val event = HeldUEvents.Type(bowString, 0, unstrung, 1)
                eventBus.publish(this, event)
            }

            assertMessageSent("Your inventory is too full to hold any more shortbow.")
            assertDoesNotContain(player.inv, FletchingObjRefs.shortbow)
        }

    @Test
    fun GameTestState.`wrong arrowheads count prevents arrow making`() =
        runGameTest(FletchingArrow::class) {
            player.clearInv()
            player.stats[stats.fletching] = 1

            player.inv[0] = InvObj(FletchingObjRefs.headless_arrow, 10)
            player.inv[1] = InvObj(FletchingObjRefs.bronze_arrowheads, 10)

            player.withProtectedAccess {
                val headless = objTypes[FletchingObjRefs.headless_arrow]
                val tips = objTypes[FletchingObjRefs.bronze_arrowheads]
                val event = HeldUEvents.Type(headless, 0, tips, 1)
                eventBus.publish(this, event)
            }

            assertMessageSent("You need 15 headless arrows and 15 bronze arrowtips to make arrows.")
            assertDoesNotContain(player.inv, FletchingObjRefs.bronze_arrow)
        }

    @Test
    fun GameTestState.`headless arrow with wrong feather count prevents making`() =
        runGameTest(FletchingHeadlessArrow::class) {
            player.clearInv()
            player.stats[stats.fletching] = 1

            player.inv[0] = InvObj(FletchingObjRefs.feather, 10)
            player.inv[1] = InvObj(FletchingObjRefs.arrow_shaft, 10)

            player.withProtectedAccess {
                val feather = objTypes[FletchingObjRefs.feather]
                val shaft = objTypes[FletchingObjRefs.arrow_shaft]
                val event = HeldUEvents.Type(feather, 0, shaft, 1)
                eventBus.publish(this, event)
            }

            assertMessageSent("You need 15 feathers and 15 arrow shafts to make headless arrows.")
            assertDoesNotContain(player.inv, FletchingObjRefs.headless_arrow)
        }
}
