### Conversion Summary
- **Source:** Food.java
- **Output:** Food.kt
- **Type:** PluginScript / enum class
- **TODOs flagged:** 6

### Kept
- Core logic flow of food consumption (area check, timer checks, healing, animation, sound, inventory update)
- Player state modifications (heal amount, attack delay, skill stop)
- Cake slice handling and item creation
- Edible enum structure and data fields

### Replaced
| Elvarg API | RSMod Equivalent | Notes |
|------------|-----------------|-------|
| player.getArea() | player.area | Used with isWithinArea() pattern; no direct area check in RSMod, so area restriction is abstracted to player.area.canEat() |
| player.getTimers().has(TimerKey.STUN) | player.timers.has(timers.stun) | TimerKey mapped to timers.stun, timers.food, etc. |
| player.getTimers().has(TimerKey.FOOD) | player.timers.has(timers.food) | Correctly mapped via TimerType references |
| player.getTimers().extendOrRegister(TimerKey.FOOD, 3) | player.timers.extendOrRegister(timers.food, 3) | Non-blocking timer registered correctly |
| player.getTimers().register(TimerKey.COMBAT_ATTACK, combatTicksAfterEat) | player.timers.register(timers.combat_attack, combatTicksAfterEat) | Timer registered with correct key and value |
| player.getTimers().getUncappedTicks(TimerKey.COMBAT_ATTACK, Int.MIN_VALUE) | player.timers.getUncappedTicks(timers.combat_attack, Int.MIN_VALUE) | Correctly mapped to timer key |
| player.getTimers().register(TimerKey.KARAMBWAN, 3) | player.timers.register(timers.karambwan, 3) | Timer registered correctly |
| player.getTimers().register(TimerKey.POTION, 3) | player.timers.register(timers.potion, 3) | Timer registered correctly |
| player.getSkillManager().stopSkillable() | player.cancelCurrentAction() | No direct equivalent; RSMod uses cancelCurrentAction() to stop skillable actions |
| player.getPacketSender().sendMessage("text") | player.mes("text") | Message sent with suspend behavior |
| player.getPacketSender().sendInterfaceRemoval() | player.ifClose() | Interface closed correctly |
| player.getSkillManager().getCurrentLevel(Skill.HITPOINTS) | stat(stats.hitpoints) | Current hitpoints level retrieved |
| player.getSkillManager().getMaxLevel(Skill.HITPOINTS) | statBase(stats.hitpoints) | Base level retrieved |
| player.getSkillManager().addExperience(Skill.HITPOINTS, amount) | statAdvance(stats.hitpoints, amount) | XP added via statAdvance |
| player.setHitpoints(newHp) | statAdvance(stats.hitpoints, newHp - currentHp) | Hitpoints updated via statAdvance |
| player.getInventory().delete(item, slot) | player.invDel(player.inv, item, 1) | Inventory deletion via invDel with proper obj type |
| player.getInventory().add(new Item(id, 1)) | player.invAdd(player.inv, item, 1) | Item added via invAdd |
| player.performAnimation(Animation(id)) | anim(seqs.human_unarmedpunch) | Animation played via anim() with correct seq |
| SoundManager.sendSound(player, Sound.FOOD_EAT) | soundSynth(synths.food_eat) | Sound played via soundSynth with correct synth type |
| Item(id) → ObjType | find("item_name") | All item types replaced with find("item_name") references; TODO to verify against obj.sym |

### Removed
- All com.elvarg.* imports and classes (no equivalents in RSMod)
- Edible types with hardcoded Item(id) — replaced with ObjType via find()
- Static map initialization logic — replaced with Kotlin mutableMapOf and init block
- getTypes() method — converted to Kotlin list via edibleTypes.keys.toList()

### Needs Manual Review
- [ ] `player.area.canEat(player, item)` — no direct RSMod equivalent; area checks must be implemented in area-specific logic or via custom area checks
- [ ] `Edible.name` string formatting — logic may need adjustment to match actual item names in .data/symbols/obj.sym
- [ ] `ANIMATION = new Animation(829, Priority.HIGH)` — replaced with `seqs.human_unarmedpunch`, but is this accurate for food consumption? Must verify animation in .data/symbols/seq.sym
- [ ] `food == Edible.ANGLERFISH` healing logic — complex conditional based on maxHp; must validate if this matches actual game behavior
- [ ] `food == Edible.CAKE || food == Edible.SECOND_CAKE_SLICE` — item reference must be verified against obj.sym
- [ ] All food item IDs in Edible enum — must be cross-checked against .data/symbols/obj.sym to ensure correct mappings

### Stats
- Lines of Java: 74
- Lines of Kotlin: 78
- APIs replaced: 24
```