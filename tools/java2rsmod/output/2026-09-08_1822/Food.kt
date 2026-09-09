class Food {
    companion object {
        private val edibleTypes = mutableMapOf<Int, Edible>()

        init {
            for (edible in Edible.values()) {
                edibleTypes[edible.item.id] = edible
            }
        }

        fun consume(player: ProtectedAccess, item: Int, slot: Int): Boolean {
            val food = edibleTypes[item]
            if (food == null) return false

            // Check area restrictions
            if (player.area != null && !player.area.canEat(player, item)) {
                player.mes("You cannot eat here.")
                return true
            }

            // Check stun or food timer
            if (player.timers.has(timers.stun)) {
                player.mes("You're currently stunned!")
                return true
            }

            if (food == Edible.KARAMBWAN) {
                if (player.timers.has(timers.karambwan)) return true
            } else {
                if (player.timers.has(timers.food)) return true
            }

            player.timers.extendOrRegister(timers.food, 3)

            val combatTicks = player.timers.getUncappedTicks(timers.combat_attack, Int.MIN_VALUE)
            val addAttackDelay = when (food) {
                Edible.KARAMBWAN -> {
                    player.timers.register(timers.karambwan, 3)
                    player.timers.register(timers.potion, 3)
                    2
                }
                else -> 3
            }

            val combatTicksAfterEat = combatTicks + addAttackDelay
            if (combatTicksAfterEat > 0) {
                player.timers.register(timers.combat_attack, combatTicksAfterEat)
            }

            player.ifClose()

            // Stop current skill
            player.cancelCurrentAction()

            // Play sound
            soundSynth(synths.food_eat)

            // Play animation
            anim(seqs.human_unarmedpunch)

            // Delete food from inventory
            player.invDel(player.inv, food.item, 1)

            // Heal player
            val currentHp = stat(stats.hitpoints)
            val maxHp = statBase(stats.hitpoints)
            val healAmount = food.heal

            if (food == Edible.ANGLERFISH) {
                val c = when (maxHp) {
                    in 25..24 -> 2
                    in 25..49 -> 4
                    in 50..74 -> 6
                    in 75..92 -> 8
                    else -> 13
                }
                val heal = (maxHp / 10).toInt() + c
                healAmount = if (heal > 22) 22 else heal
                maxHp += healAmount
            }

            val effectiveHeal = if (currentHp + healAmount > maxHp) maxHp - currentHp else healAmount
            if (effectiveHeal < 0) return false

            statAdvance(stats.hitpoints, effectiveHeal)

            val actionText = if (food == Edible.BANDAGES) "use" else "eat"
            player.mes("You ${actionText} the ${food.name}.")

            // Handle cake slices
            if (food == Edible.CAKE || food == Edible.SECOND_CAKE_SLICE) {
                player.invAdd(player.inv, objs.food_slice, 1)
            }

            return true
        }
    }

    enum class Edible(
        val item: ObjType,
        val heal: Int
    ) {
        KEBAB(objs.kebab, 4),
        CHEESE(objs.cheese, 4),
        CAKE(objs.cake, 5),
        SECOND_CAKE_SLICE(objs.second_cake_slice, 5),
        THIRD_CAKE_SLICE(objs.third_cake_slice, 5),
        BANDAGES(objs.bandages, 12),
        JANGERBERRIES(objs.jangerberries, 2),
        WORM_CRUNCHIES(objs.worm_crunchies, 7),
        EDIBLE_SEAWEED(objs.edible_seaweed, 4),
        ANCHOVIES(objs.anchovies, 1),
        SHRIMPS(objs.shrimps, 3),
        SARDINE(objs.sardine, 4),
        COD(objs.cod, 7),
        TROUT(objs.trout, 7),
        PIKE(objs.pike, 8),
        SALMON(objs.salmon, 9),
        TUNA(objs.tuna, 10),
        LOBSTER(objs.lobster, 12),
        BASS(objs.bass, 13),
        SWORDFISH(objs.swordfish, 14),
        MEAT_PIZZA(objs.meat_pizza, 14),
        MONKFISH(objs.monkfish, 16),
        SHARK(objs.shark, 20),
        SEA_TURTLE(objs.sea_turtle, 21),
        DARK_CRAB(objs.dark_crab, 22),
        MANTA_RAY(objs.manta_ray, 22),
        KARAMBWAN(objs.karambwan, 18),
        ANGLERFISH(objs.anglerfish, 22),
        POTATO(objs.potato, 1),
        BAKED_POTATO(objs.baked_potato, 4),
        POTATO_WITH_BUTTER(objs.potato_with_butter, 14),
        CHILLI_POTATO(objs.chilli_potato, 14),
        EGG_POTATO(objs.egg_potato, 16),
        POTATO_WITH_CHEESE(objs.potato_with_cheese, 16),
        MUSHROOM_POTATO(objs.mushroom_potato, 20),
        TUNA_POTATO(objs.tuna_potato, 22),
        SPINACH_ROLL(objs.spinach_roll, 2),
        BANANA(objs.banana, 2),
        BANANA_(objs.banana_, 2),
        CABBAGE(objs.cabbage, 2),
        ORANGE(objs.orange, 2),
        PINEAPPLE_CHUNKS(objs.pineapple_chunks, 2),
        PINEAPPLE_RINGS(objs.pineapple_rings, 2),
        PEACH(objs.peach, 8),
        PURPLE_SWEETS(objs.purple_sweets, 3);

        private val item: ObjType
        private val heal: Int
        private val name: String

        init {
            this.item = find("item_name") // TODO: verify name against .data/symbols/obj.sym
            this.heal = heal
            this.name = name.lowercase().replace("__", "-").replace("_", " ")
        }

        fun getItem(): ObjType = item
        fun getHeal(): Int = heal
        fun getTypes(): List<Int> = edibleTypes.keys.toList()
    }
}