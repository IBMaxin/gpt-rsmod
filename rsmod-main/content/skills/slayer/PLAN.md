# Slayer Skill — Status & Remaining Work

## ✅ Done (committed in src/main and src/integration)

### Config files
- `configs/SlayerNpcRefs.kt` — Defines `master`, `cow`, `goblin` NPC refs via `find("name")`
- `configs/SlayerParams.kt` — Wraps `params.levelrequire` and `params.slayer_experience`
- `configs/SlayerVarps.kt` — Defines `slayer_target` (varp for task NPC id) and `slayer_count` (remaining kills)
- `configs/SlayerNpcEditor.kt` — `NpcEditor` setting `param[SlayerParams.levelrequire]=1` and `param[SlayerParams.experience]=80` on cow and goblin

### Scripts
- `scripts/SlayerMaster.kt` — `onOpNpc1(SlayerNpcRefs.master)` → assigns 5 cows via varps, rejects if active task
- `scripts/Slayer.kt` — `onNpcQueue(cow/goblin, queues.death)` → decrements `slayer_count`, resets varps on completion, calls `death.deathWithDrops(this)`

### Module
- `SlayerModule.kt` — Binds `SlayerDrops` (drop tables) and `SlayerLevelBoosts`
- `SlayerLevelBoosts.kt` — Stub returning 0
- `build.gradle.kts` — Dependencies: `api/plugin-commons`, `api/utils/utils-skills`, `api/death`

### New (uncommitted) files
- `configs/SlayerDrops.kt` — Drop tables for cow (bones, raw_beef, cow_hide, coins) and goblin (bones, coins)
- `PLAN.md` — This file

### Integration tests
- `configs/SlayerConfigTest.kt` — Verifies NPC editor param merge for cow (levelrequire=1, experience=80)
- `configs/SlayerDropTableTest.kt` — Verifies cow/goblin drop tables register and contain expected entries
- `SlayerMasterTest.kt` — Tests task assignment and active-task rejection
- `SlayerTest.kt` — Tests kill counting decrement, wrong NPC ignored, task completion resets varps
- `VerificationTest.kt` — Smoke test resolving content NPCs

## ❌ What's Left (hats to add)

### 1. Award slayer XP on kill
**File:** `scripts/Slayer.kt`
**Missing:** After decrementing `slayerCount`, no `statAdvance(stats.slayer, xp)` call.
**Fix:**
```kotlin
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.stat.statAdvance

// Inside onSlayerNpcDeath(), after decrementing count but before completion message:
val xp = npc.type.param(params.slayer_experience) ?: 0
hero.statAdvance(stats.slayer, xp)
```

### 2. Check slayer level before assigning task
**File:** `scripts/SlayerMaster.kt`
**Missing:** Master assigns task unconditionally regardless of player slayer level.
**Fix:**
```kotlin
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.stat.stat

// In talkToTurael() before setting varps:
val level = stat(stats.slayer)
if (level < 1) {
    mes("You need a Slayer level of at least 1 to get a task.")
    return
}
```

### 3. Random task pool instead of hardcoded cows
**File:** `scripts/SlayerMaster.kt`
**Missing:** Only ever assigns `SlayerNpcRefs.cow`. Need a task pool keyed by slayer level.
**Fix:**
```kotlin
// Define task pool:
private data class SlayerTask(val npcRef: NpcReferences, val count: IntRange, val levelReq: Int)

private val taskPool = listOf(
    SlayerTask(SlayerNpcRefs.cow, 5..10, 1),
    SlayerTask(SlayerNpcRefs.goblin, 5..10, 1),
    // add more as NPCs are defined...
)

// In talkToTurael(), pick a random task:
import org.rsmod.api.random.GameRandom
// ... inject GameRandom
val eligible = taskPool.filter { stat(stats.slayer) >= it.levelReq }
val task = eligible.random(random)
slayerTarget = task.npcRef.id
slayerCount = task.count.random(random)
mes("Your new task is to kill ${slayerCount} ${task.npcRef.name}.")
```

### 4. Register more NPCs with slayer params
**File:** `configs/SlayerNpcEditor.kt` and `configs/SlayerNpcRefs.kt`
**Missing:** Only cow and goblin have slayer data. Need a full table of slayer monsters.
**Fix:** Add entries to `SlayerNpcRefs` and `SlayerNpcEditor`:
```kotlin
// In SlayerNpcEditor:
edit(SlayerNpcRefs.goblin) { param[SlayerParams.levelrequire] = 1; param[SlayerParams.experience] = 64 }
edit(SlayerNpcRefs.cow) { param[SlayerParams.levelrequire] = 1; param[SlayerParams.experience] = 80 }
edit(SlayerNpcRefs.chicken) { param[SlayerParams.levelrequire] = 1; param[SlayerParams.experience] = 40 }
edit(SlayerNpcRefs.rat) { param[SlayerParams.levelrequire] = 1; param[SlayerParams.experience] = 10 }
edit(SlayerNpcRefs.spider) { param[SlayerParams.levelrequire] = 15; param[SlayerParams.experience] = 200 }
edit(SlayerNpcRefs.ghost) { param[SlayerParams.levelrequire] = 25; param[SlayerParams.experience] = 400 }
// ... etc
```

### 5. Register death queues for all slayer NPCs
**File:** `scripts/Slayer.kt`
**Missing:** Only cow and goblin have `onNpcQueue` registrations. Need to register all slayer NPCs.
**Fix:** Iterate all NPCs with `slayer_experience` param, or register explicitly in `startup()`.

### 6. Streak tracking and reward points
**Missing:** No varp for consecutive tasks completed. No streak milestone bonuses.
**Needed:**
```kotlin
// New varp: SlayerVarps.slayer_streak
// In SlayerMaster, on task completion:
streak += 1
// Check vs milestones for bonus points
```

### 7. Slayer level requirement param (distinct from combat level req)
**Note:** `SlayerParams.levelrequire` currently uses `params.levelrequire` (general). There is also `params.slayer_levelrequire` in the cache enricher. The NPC editor should set `params.slayer_levelrequire` instead so the cache enricher can use it for visible level req icons.
**Fix in `configs/SlayerParams.kt`:**
```kotlin
val levelrequire = params.slayer_levelrequire  // was params.levelrequire
```

### 8. Drop tables for all slayer NPCs
**File:** `configs/SlayerDrops.kt`
**Missing:** Only cow and goblin have drop tables. Need tables for every killable NPC.

### 9. Combat integration (aggressive NPC + on-death XP)
**File:** `scripts/Slayer.kt`
**Missing:** `onNpcQueue(queues.death)` only fires for explicitly registered NPCs. Need to either:
- Register every slayer NPC in `startup()` (explicit `onNpcQueue` for each)
- Or hook into a generic death listener that checks for `slayer_experience` param

### 10. Slayer items (helmet, ring, black mask)
**Missing:** No item definitions, no equipment bonuses, no crafting (black mask → slayer helmet).
**See:** `content/skills/slayer/PLAN.md` shop table for reference.

## File Locations Summary

| Purpose | Path |
|---------|------|
| Config refs | `content/skills/slayer/src/main/kotlin/org/rsmod/content/skills/slayer/configs/SlayerNpcRefs.kt` |
| Params | `content/skills/slayer/src/main/kotlin/org/rsmod/content/skills/slayer/configs/SlayerParams.kt` |
| Varps | `content/skills/slayer/src/main/kotlin/org/rsmod/content/skills/slayer/configs/SlayerVarps.kt` |
| NPC editor | `content/skills/slayer/src/main/kotlin/org/rsmod/content/skills/slayer/configs/SlayerNpcEditor.kt` |
| Drop tables | `content/skills/slayer/src/main/kotlin/org/rsmod/content/skills/slayer/configs/SlayerDrops.kt` |
| Main script | `content/skills/slayer/src/main/kotlin/org/rsmod/content/skills/slayer/scripts/Slayer.kt` |
| Master script | `content/skills/slayer/src/main/kotlin/org/rsmod/content/skills/slayer/scripts/SlayerMaster.kt` |
| Level boosts | `content/skills/slayer/src/main/kotlin/org/rsmod/content/skills/slayer/SlayerLevelBoosts.kt` |
| Module | `content/skills/slayer/src/main/kotlin/org/rsmod/content/skills/slayer/SlayerModule.kt` |
| Build deps | `content/skills/slayer/build.gradle.kts` |
| Config test | `content/skills/slayer/src/integration/kotlin/org/rsmod/content/skills/slayer/configs/SlayerConfigTest.kt` |
| Drop table test | `content/skills/slayer/src/integration/kotlin/org/rsmod/content/skills/slayer/configs/SlayerDropTableTest.kt` |
| Master test | `content/skills/slayer/src/integration/kotlin/org/rsmod/content/skills/slayer/SlayerMasterTest.kt` |
| Kill test | `content/skills/slayer/src/integration/kotlin/org/rsmod/content/skills/slayer/SlayerTest.kt` |
| Smoke test | `content/skills/slayer/src/integration/kotlin/org/rsmod/content/skills/slayer/VerificationTest.kt` |

## OSRS Reference Data

### Reward-points Mechanics
- Points start after **5 consecutive tasks** (no Turael reset).
- Points awarded **once a task finishes**; amount depends on the Slayer Master.
- **Milestone bonuses** at 10, 50, 100, 250, 1000 tasks give extra points.
- **Partner tasks:** percentage of master's points based on personal kill share (min 20%).
- **Cancel cost:** 30 pts.

### Slayer Masters
| Master | Combat lvl | Base pts | 10th | 50th | 100th | 250th | 1000th | Task size |
|--------|-----------|----------|------|------|-------|-------|--------|-----------|
| Turael/Aya | 0 | – | – | – | – | – | – | 5-10 |
| Mazchna | 20 | 6 | 30 | 90 | 150 | 210 | 300 | 30-40 |
| Vannaka | 40 | 8 | 40 | 120 | 200 | 280 | 400 | 35-45 |
| Chaeldar | 70 | 10 | 50 | 150 | 250 | 350 | 500 | 45-55 |
| Nieve/Steve | 85 | 12 | 60 | 180 | 300 | 420 | 600 | 50-65 |
| Duradel | 100 | 15 | 75 | 225 | 375 | 525 | 750 | 55-70 |
| Konar | 75 | 18 | 90 | 270 | 450 | 630 | 900 | 60-80 |
| Krystilia | any (Wildy) | 25 | 125 | 375 | 625 | 875 | 1250 | 75-100 |

### Shop Items (point costs)
- Slayer ring
- Slayer helmet (combined headgear)
- Unlock new monsters as tasks
- Block a task (permanent skip)
- Skip a task (30 pts)
- "Like a boss" ability (200 pts) — boss-type tasks
- Headgear upgrades (black mask, spiny helmet, etc.)

### Special bonuses
- Diary boosts: Western Provinces (Nieve → Duradel pts), Kourand & Kebos (Konar → Duradel pts)
- Boss Slayer unlock (200 pts): +5000 Slayer XP, custom kill-count (3-35)
- Mortimer: variable points (0-40 per task via points modifier)