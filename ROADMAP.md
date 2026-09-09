# RSMod Development Roadmap

> **Last updated:** September 2026  
> **Developer:** Solo · Java 21 · Kotlin · Gradle · TDD-first · Modular content system  
> **Server:** Port 43594 · Dev realm · 150x XP · No password

---

## Table of Contents

1. [Architecture Principles](#1-architecture-principles)
2. [Module Structure Templates](#2-module-structure-templates)
3. [Key API Quick Reference](#3-key-api-quick-reference)
4. [Current Inventory](#4-current-inventory)
5. [Known Blockers](#5-known-blockers)
6. [Competitive Landscape](#6-competitive-landscape)
7. [What Players Want in 2026](#7-what-players-want-in-2026)
8. [MVP Milestone](#8-mvp-milestone)
9. [Phase 1 — Core Skills](#9-phase-1--core-skills-weeks-14)
10. [Phase 2 — Economy & Quests](#10-phase-2--economy--quests-weeks-56)
11. [Phase 3 — Combat & Cities](#11-phase-3--combat--cities-weeks-710)
12. [Phase 4 — Endgame Content](#12-phase-4--endgame-content-weeks-1116)
13. [Phase 5 — Differentiation Layer](#13-phase-5--differentiation-layer-weeks-1720)
14. [Phase 6 — Engagement & Retention](#14-phase-6--engagement--retention-weeks-2124)
15. [Phase 7 — Polish & Custom Content](#15-phase-7--polish--custom-content-weeks-25)
16. [Quick Wins Table](#16-quick-wins-table)
17. [Long-term Vision](#17-long-term-vision)
18. [References](#18-references)

---

## 1. Architecture Principles

Every feature in this roadmap is a **self-contained content module**. No exceptions.

- **Zero edits to existing core files** — skills, quests, bosses, and areas live entirely inside their own `content/` subdirectory
- **Independent development** — each module can be built, tested, and shipped on its own
- **No cross-module dependencies** — modules communicate through the shared API layer only
- **Reusable patterns** — every skill follows Module → Scripts → Configs → Tests
- **Config-driven** — NPC params, item references, and content groups are defined per-module
- **TDD by default** — every module ships with integration tests in `src/integration/`
- **PLAN.md first** — write the design doc before writing any code

---

## 2. Module Structure Templates

Template directory structures for each module type are documented in [DEVELOPMENT.md](DEVELOPMENT.md#3-creating-a-new-module) with detailed setup guides. Quick reference patterns are also in [AGENTS.md](rsmod-main/AGENTS.md#module-pattern).

**Key rule:** Every feature is a self-contained `content/<category>/<name>/` module with its own `build.gradle.kts`, `PLAN.md`, and `src/` tree. No exceptions.

---

## 3. Key API Quick Reference

### Item / Inventory
```kotlin
val raw_shrimps = find("raw_shrimps")       // ObjRef via sym name
invAdd(inv, objs.cooked_shrimps, 1)         // Add item to inventory
invDel(inv, objs.raw_shrimps, 1)            // Remove item from inventory
invTotal(inv, objs.coins)                   // Count items in inventory
```

### Stats / XP
```kotlin
stat(stats.cooking)                         // Current level (with boosts)
statBase(stats.cooking)                     // Base level (no boosts)
statAdvance(stats.cooking, 30.0)            // Add XP (fine units ×10; 30.0 = 3.0 XP)
statRandom(stats.cooking, low, high, invisibleLvls) // OSRS success roll
```

### Player Actions
```kotlin
anim(seqs.human_cooking)                    // Play animation
spotanim(spotanims.fire_hit, height = 96)   // Play graphic
soundSynth(synths.cooking_complete)         // Play sound
mes("You successfully cook the shrimps.")   // Send chat message (suspends coroutine)
actionDelay = mapClock + 3                  // Delay next player action (non-blocking)
```

### Script Registration
```kotlin
onOpLoc2(cooking_locs.fire) { event -> }   // Use loc (e.g. cook on fire)
onOpHeldU(knife_ref, logs_ref) { event -> } // Item-on-item (e.g. fletch)
onOpNpc3(npc_ref) { event -> }              // NPC interaction (e.g. pickpocket)
```

### Integration Test Pattern
```kotlin
@Test
fun GameTestState.`cook shrimps at level 1`() = runGameTest(CookingScript::class) {
    player.stats[stats.cooking] = 1
    player.inv[0] = InvObj(cooking_obj_refs.raw_shrimps, 1)
    random.next = 0  // force success

    player.withProtectedAccess {
        val raw = objTypes[cooking_obj_refs.raw_shrimps]
        eventBus.publish(this, OpLoc2Events.Type(fire_loc, raw, 0))
    }

    advance(ticks = 1)
    assertContains(player.inv, cooking_obj_refs.cooked_shrimps)
    assertDoesNotContain(player.inv, cooking_obj_refs.raw_shrimps)
}

@Test
fun GameTestState.`requires level 15 for trout`() = runGameTest(CookingScript::class) {
    player.stats[stats.cooking] = 1
    player.inv[0] = InvObj(cooking_obj_refs.raw_trout, 1)

    player.withProtectedAccess {
        val raw = objTypes[cooking_obj_refs.raw_trout]
        eventBus.publish(this, OpLoc2Events.Type(fire_loc, raw, 0))
    }
    // NOTE: assert messages BEFORE advance() — advance() clears capture clients
    assertMessageSent("You need a Cooking level of 15 to cook this.")
}
```

---

## 4. Current Inventory

### ✅ Implemented & Working
| Feature | Notes |
|---------|-------|
| Combat system | Full melee / ranged / magic formulas |
| Bank system | Deposit, withdraw, tabs, settings |
| Prayer interface | Tab, quick pray, drain, filter |
| Shops | Buy, sell, restock |
| Equipment stats | All slot bonuses |
| Death system | Item drop, grave timer |
| Inventory system | Full inv / worn / bank |
| Routing / pathfinding | Smart path avoidance |
| 20 elemental spells | Standard magic spellbook |
| Special attacks | Framework + 3 weapons |
| Woodcutting | Full — code works, tests BLOCKED (see §5) |
| Thieving | Pickpocketing men/women in Lumbridge — code works, tests BLOCKED |
| Magic | Spell attacks |
| Lumbridge | 11 NPCs, shops, spawns |
| Ardougne thieving area | Source written — tests BLOCKED |
| Canoe travel | Canoe system |
| Admin commands | Dev tools |
| Login system | Account auth |
| Slayer (skeleton) | Cow NPC editor, config test, death script |

### ❌ Not Yet Implemented
- 17+ skills: Cooking, Firemaking, Fishing, Mining, Smithing, Crafting, Herblore,
  Agility, Farming, Runecrafting, Hunter, Construction, Prayer content,
  Attack, Defence, Strength, Hitpoints standalone content, Fletching (in progress)
- Grand Exchange interface
- Quests (0 of 100+)
- Bosses / Raids (0)
- Cities beyond Lumbridge
- Minigames (0)
- Achievement diaries (0)
- Collection log
- Ironman / Group Ironman modes
- Prestige system
- Clans
- Seasonal events

---

## 5. Known Blockers

> ⚠️ Fix these **before** starting any new feature work. Both are global.

### 🔴 BLOCKER 1 — `fletching_knife` TypeVerifier crash (affects ALL integration tests)

**File:** `api/config/refs/BaseContent.kt:58`  
**Cause:** `ContentReferences.find("fletching_knife")` creates a `ContentGroupType`, but
`TypeVerifier` validates it against the item `.sym` file — that name does not exist there.  
**Symptom:** Every integration test suite throws `initializationError` at startup.  
**Fix:** Remove or replace the invalid reference.

```kotlin
// BaseContent.kt:58 — REMOVE or rename this:
val fletching_knife = find("fletching_knife")   // ← "fletching_knife" not in obj.sym
```

**Steps to fix:**
1. Open `rsmod-main/api/config/refs/BaseContent.kt`
2. Find line 58 — the `fletching_knife` content group reference
3. Either remove it entirely, or replace with a valid sym name from `.data/symbols/obj.sym`
4. Run `./gradlew :content:skills:fletching:integration` to confirm tests initialize

---

### 🟡 BLOCKER 2 — Fletching `invDel` silent failure

**Affects:** Fletching integration tests.  
**Cause:** `invDel` silently fails inside the `withProtectedAccess` + `eventBus.publish` path.  
**Workaround:** Assert inventory delta counts rather than exact presence.  
**Fix:** Needs investigation into coroutine timing.

---

### 🟡 BUG — Ardougne Guard missing `op[0] = "Talk-to"` in editor

**File:** `content/areas/city/ardougne/configs/ArdougneNpcs.kt`  
**Cause:** Cache NPC has null `op[0]`; `OpNpcHandler.hasOp()` silently blocks the interaction.  
**Fix:**
```kotlin
edit(ardougne_npcs.guard) {
    op[0] = "Talk-to"
    defaultMode = wander
    wanderRange = 3
}
```

---

## 6. Competitive Landscape

### Top OSRS Private Servers — September 2026

| Server | Peak Players | Type | Core Identity |
|--------|-------------|------|---------------|
| **Ferox** | 1,500+ | OSRS-style | All 3 raids, weekly updates, item upgrades, Android |
| **Roat PKZ** | 1,332 | 317 + OSRS | #1 PvP/gambling, daily GP tournaments, clan cups |
| **Alora** | 1,303 | OSRS-native | #1 economy, 3 raids, Ironman + GIM, Leagues, iOS + Android |
| **RXPS** | 1,107 | 317 + OSRS | Economy + semi-custom, skilling + bossing |
| **SpawnPK** | 980 | Custom + OSRS | Instant PvP loadouts, clan wars, Bounty Hunter |
| **Echo** | 600–800 | OSRS + custom | 3 League formats, 570+ combat achievements, GIM |
| **GrinderScape** | 200–350 | OSRS | 117 HD graphics, 54 quests, Grand Exchange |
| **BattleScape** | 150–250 | OSRS authentic | 20+ years stable, RuneLite + mobile + webapp |

### What We're Missing vs. Top Servers

| Category | Top Server Feature | Our Status |
|----------|-------------------|------------|
| Skills | All 23 implemented | 3 of 23 |
| Raids | CoX, ToB, ToA all present | 0 |
| Cities | 20+ OSRS cities + wilderness | Lumbridge only |
| Bosses | 50+ bosses | 0 |
| Quests | 54–100+ quests | 0 |
| Grand Exchange | Real-time offer system | Price data only |
| Collection Log | Per-boss / per-content tracking | Not started |
| Special Attacks | 30+ weapons | 3 weapons |
| Ironman + GIM | Multiple Ironman modes | Not started |
| Mobile client | iOS + Android | RSProx (remote play only) |
| 117 HD graphics | RuneLite 117 HD plugin | Not evaluated |
| Leagues / Relics | 2–3 seasonal formats | Not started |
| Prestige system | Skill reset for talent points | Not planned yet |
| Daily tournaments | OSRS GP prizes daily | Not planned yet |
| Collection log | Per-boss kill log | Not started |
| Boss highscores | Kill count leaderboards | Not started |

---

## 7. What Players Want in 2026

Based on RSPS toplists, Reddit surveys, and active server research (September 2026).

### 🔥 Retention-critical — losing these causes immediate player churn

| Feature | Why It Matters |
|---------|---------------|
| **Grand Exchange with real volume** | Items sell in minutes, not hours; players quit if market is dead |
| **All 3 raids (CoX, ToB, ToA)** | Biggest population driver on every top server; Ferox + Alora lead because of this |
| **Ironman + Group Ironman** | Large dedicated playerbase; Alora built their brand identity on Ironman |
| **Collection log** | Players grind for log completion, not just drops; massive session-length driver |
| **Boss highscores / leaderboards** | Competitive hook; gives players a reason to log in every day |
| **Mobile client** | Android minimum; iOS separates top-5 from everyone else |

### ⭐ Differentiators — what new servers use to stand out

| Feature | Example Server | Notes |
|---------|---------------|-------|
| **Leagues / Relic system** | Echo (3 formats) | Server identity; dedicated seasonal playerbase |
| **Prestige system** | August RSPS | Reset skill → permanent talent point; meaningful endgame loop |
| **Perk / Talent trees** | August RSPS | PvM tree, Skilling tree, Utility tree; build customization |
| **World boss arena** | August RSPS | Server-wide community boss spawn; everyone gets loot; easy to implement |
| **Daily GP tournaments** | Impact, Roat PKZ | OSRS GP daily prizes; strongest daily login hook in RSPS |
| **Item upgrade paths** | Ferox | Economy sink; players craft + boss to upgrade gear tiers |
| **Party system** | Various | Proximity XP boost near party members; adds social glue |
| **PvM pets that fight with you** | New servers | Unique vs. vanilla OSRS; collectible + functional |

---

## 8. MVP Milestone — "Playable Server"

> Minimum for players to stay more than 5 minutes.

| Feature | Status | Priority |
|---------|--------|----------|
| Fix `fletching_knife` TypeVerifier blocker | ❌ Open | 🔴 Do first |
| Fix Ardougne Guard `op[0]` editor bug | ❌ Open | 🔴 Do first |
| 8+ skills to train | ❌ Pending | 🔴 Critical |
| Grand Exchange (basic offers) | ❌ Pending | 🔴 Critical |
| 3+ cities with shops + NPCs | ❌ Pending | 🔴 Critical |
| 5+ starter quests | ❌ Pending | 🔴 Critical |
| 10+ special attacks | ❌ Pending | 🔴 Critical |
| Collection log (basic shell) | ❌ Pending | 🟡 High |
| Boss highscores (kill count) | ❌ Pending | 🟡 High |
| Basic combat loop | ✅ Done | — |
| Bank system | ✅ Done | — |
| Shops | ✅ Done | — |

**Estimated time to MVP:** 6–8 weeks solo

---

## 9. Phase 1 — Core Skills (Weeks 1–4)

> **Goal:** Give players 8–10 skills so the server feels like RuneScape.  
> **Pattern:** Follow `woodcutting` (LOC-based gathering) and `thieving` (NPC-based action).  
> **Rule:** Zero edits to existing files. Every skill is a fully self-contained module.

| Skill | Status | Effort | Pattern | Key Features |
|-------|--------|--------|---------|--------------|
| **Cooking** | 📋 Planning | 1–2 days | Processing (LOC) | Burn levels, all fish/meat, cooking gauntlets, wine |
| **Firemaking** | 📋 Planning | 1–2 days | Processing (item-on-loc) | Log types, bonfires, firemaking cape |
| **Fletching** | 🚧 In Progress | 2–3 days | Processing (item-on-item) | Bows, bowstrings, arrows, bolts, darts, knives |
| **Fishing** | 📋 Planning | 2–3 days | Gathering (LOC) | Spots, net/harpoon/cage/rod, fish types |
| **Mining** | ❌ Not started | 3–4 days | Gathering (LOC) | Pickaxes, rock depletion, ore variants, Mining Guild |
| **Smithing** | ❌ Not started | 3–4 days | Processing (LOC) | Bars, armour, cannonballs, anvil + furnace |
| **Crafting** | ❌ Not started | 3–4 days | Processing (item-on-item / LOC) | Leather, pottery, glass, jewellery, dragonhide |
| **Herblore** | ❌ Not started | 3–4 days | Processing (item-on-item) | Clean herbs, unfinished potions, finished potions |

**Phase 1 estimated effort:** 3–4 weeks total (~400–1,200 lines per skill)

### Boilerplate: CookingModule.kt
```kotlin
class CookingModule : PluginModule() {
    override fun bind() {
        bindScripts(CookingScript::class)
        bindInvisibleLevelMod(CookingLevelBoosts::class)
    }
}
```

### Boilerplate: CookingScript.kt (item-on-loc / LOC pattern)
```kotlin
class CookingScript : PluginScript() {
    override fun startup() {
        onOpLoc2(cooking_locs.fire) { event ->
            val raw = objTypes[event.obj]
            val params = CookingParams.resolve(raw) ?: return@onOpLoc2
            if (stat(stats.cooking) < params.levelRequire) {
                mes("You need a Cooking level of ${params.levelRequire} to cook this.")
                return@onOpLoc2
            }
            anim(seqs.human_cooking)
            val success = statRandom(stats.cooking, params.burnLow, params.burnHigh, invisibleLvls)
            if (success) {
                invDel(inv, raw.id, 1)
                invAdd(inv, params.cookedObj, 1)
                statAdvance(stats.cooking, params.xp)
                mes("You cook the ${raw.name}.")
            } else {
                invDel(inv, raw.id, 1)
                invAdd(inv, objs.burnt_food, 1)
                mes("You accidentally burn the ${raw.name}.")
            }
        }
    }
}
```

---

## 10. Phase 2 — Economy & Quests (Weeks 5–6)

> **Goal:** Enable trading and give players goals to work toward.

| Feature | Effort | Location | Notes |
|---------|--------|----------|-------|
| **Grand Exchange** | High | `content/interfaces/grand-exchange/` | Buy/sell offers, price lookup, collection box |
| **Player Trading** | Medium | `content/interfaces/trading/` | Trade screen, offer/accept flow |
| **Collection Log** | Medium | `content/interfaces/collection-log/` | Per-boss / per-content log — core retention hook |
| **Boss Highscores** | Low | `content/interfaces/hiscores/` | Kill count leaderboard; competitive daily login driver |
| **Basic Quests (5)** | Medium | `content/quests/{name}/` | Cook's Assistant, Sheep Shearer, Imp Catcher, Goblin Diplomacy, Restless Ghost |
| **Special Attacks +7** | Low | `content/other/special-attacks/` | AGS, D Claws, BGS, SGS, ZGS, Arclight, VLS |

### Boilerplate: Quest Module (Cook's Assistant)
```kotlin
// CooksAssistantModule.kt
class CooksAssistantModule : PluginModule() {
    override fun bind() {
        bindScripts(CooksAssistantScript::class)
    }
}

// CooksAssistantScript.kt — stage-gated quest dialogue
class CooksAssistantScript : PluginScript() {
    override fun startup() {
        onOpNpc1(quest_npcs.lumbridge_cook) { _ ->
            when (player.questStage(quests.cooks_assistant)) {
                0 -> startQuest()    // give task
                1 -> checkItems()    // accept items, give reward XP
            }
        }
    }
}
```

### Quest Module File Structure
```
content/quests/cooks-assistant/
├── build.gradle.kts
├── PLAN.md
├── src/main/kotlin/.../cooks_assistant/
│   ├── CooksAssistantModule.kt
│   ├── scripts/CooksAssistant.kt
│   └── configs/
│       ├── CooksAssistantNpcRefs.kt
│       └── CooksAssistantObjRefs.kt
└── src/integration/kotlin/.../
    └── CooksAssistantTest.kt
```

---

## 11. Phase 3 — Combat & Cities (Weeks 7–10)

> **Goal:** Make combat meaningful. Give players places to go beyond Lumbridge.

| Feature | Effort | Location | Notes |
|---------|--------|----------|-------|
| **Slayer (full)** | High | `content/skills/slayer/` | Masters, weights, boss tasks, Slayer helm, points shop |
| **Ironman mode** | Medium | `content/game-modes/ironman/` | Mode select at creation, GE lockout, Ironman hiscores |
| **Group Ironman** | Medium | `content/game-modes/group-ironman/` | Shared bank, group rules, GIM hiscores |
| **Varrock** | Medium | `content/areas/city/varrock/` | GE building, shops, arena, sewers |
| **Falador** | Medium | `content/areas/city/falador/` | Mining guild, party room, Falador shield |
| **Edgeville** | Low | `content/areas/city/edgeville/` | Bank, furnace, wilderness access |
| **Ardougne** | Medium | `content/areas/city/ardougne/` | East/West split, knights, stalls (partial — unblock first) |
| **Prayer Altars** | Low | `content/generic/prayer-altars/` | Restoration altars, chaos altar, wilderness altar |

> **Note on Ironman:** Alora's most popular game mode. Launch Ironman at the same time as
> the GE — Ironman players are the most engaged and most likely to recruit others.

---

## 12. Phase 4 — Endgame Content (Weeks 11–16)

> **Goal:** Give maxed or near-maxed players meaningful goals.

| Feature | Effort | Location | Notes |
|---------|--------|----------|-------|
| **Bosses — Wave 1 (5–7)** | High | `content/bosses/{name}/` | Zulrah, Vorkath, Dagannoth Kings, GWD 4 (Bandos, Arma, Sara, Zammy) |
| **Chambers of Xeric (CoX)** | Very High | `content/raids/chambers-of-xeric/` | #1 population driver on every top server; procedural rooms + Olm |
| **Achievement Diaries** | High | `content/diaries/{region}/` | Lumbridge, Varrock, Falador, Ardougne |
| **Agility** | Medium | `content/skills/agility/` | Gnome, Barbarian, Wilderness, Seers', Ardougne courses |
| **Runecrafting** | Medium | `content/skills/runecrafting/` | All altars, pouches, GOTR-style minigame |
| **Farming** | High | `content/skills/farming/` | Allotments, herbs, fruit trees, special patches |
| **Mini-bosses** | Medium | `content/npcs/mini-bosses/` | Cave horrors, nechryael, dust devils, black dragons |

### Boss Module: Dagannoth Kings
```
content/bosses/dagannoth-kings/
├── build.gradle.kts
├── PLAN.md
├── src/main/kotlin/.../dagannoth_kings/
│   ├── DagannothKingsModule.kt
│   ├── scripts/
│   │   ├── DagannothPrime.kt          ← magic attack rotation
│   │   ├── DagannothRex.kt            ← melee attack rotation
│   │   └── DagannothSupreme.kt        ← ranged attack rotation
│   └── configs/
│       ├── BossNpcRefs.kt
│       ├── BossDrops.kt               ← berserker ring, archer ring, seers ring, warrior ring
│       └── BossSpawns.kt
└── src/integration/kotlin/.../
    ├── DagannothKingsConfigTest.kt
    └── DagannothKingsTest.kt
```

---

## 13. Phase 5 — Differentiation Layer (Weeks 17–20)

> **Goal:** Features that make your server stand out from vanilla OSRS-clone servers.
> These are the features players tell their friends about.

| Feature | Effort | Location | Why It Matters |
|---------|--------|----------|----------------|
| **Prestige System** | Medium | `content/game-modes/prestige/` | Reset skill → permanent talent point; major endgame loop |
| **Talent / Perk Trees** | High | `content/game-modes/talents/` | PvM, Skilling, Utility branches; gives players a build identity |
| **World Boss Arena** | Medium | `content/bosses/world-boss-arena/` | Server-wide spawn; everyone gets loot; community event each hour |
| **Daily Tournaments** | Medium | `content/events/daily-tournaments/` | OSRS GP daily prizes; strongest daily login hook in RSPS (Roat/Impact model) |
| **Party System** | Low | `content/generic/party/` | Proximity XP boost; social glue between players |
| **PvM Pets** | Medium | `content/npcs/pvm-pets/` | Pets that fight alongside you; unique vs. vanilla OSRS |
| **Item Upgrade Paths** | Medium | `content/other/item-upgrades/` | Economy sink; Ferox model — upgrade tier weapons/armour with boss mats |

### Boilerplate: Prestige System Design
```
content/game-modes/prestige/
├── PLAN.md                              ← write this first!
├── src/main/kotlin/.../prestige/
│   ├── PrestigeModule.kt
│   ├── scripts/
│   │   └── Prestige.kt                ← NPC dialogue → confirm → reset skill → award point
│   └── configs/
│       ├── PrestigeNpcRefs.kt          ← prestige master NPC ref
│       └── PrestigeParams.kt           ← prestige_count, talent_points
└── src/integration/kotlin/.../prestige/
    └── PrestigeTest.kt
```

---

## 14. Phase 6 — Engagement & Retention (Weeks 21–24)

> **Goal:** Keep players logging in every day and recommending the server to friends.

| Feature | Effort | Location | Notes |
|---------|--------|----------|-------|
| **Leagues / Relics** | High | `content/game-modes/leagues/` | 2–3 formats; relic system, task log, point shop |
| **Theatre of Blood (ToB)** | Very High | `content/raids/theatre-of-blood/` | Wave 2 of raids after CoX proves the framework |
| **Tombs of Amascut (ToA)** | Very High | `content/raids/tombs-of-amascut/` | Wave 3 raids |
| **Minigames (3+)** | High | `content/minigames/{name}/` | Barrows, Pest Control, Blast Furnace |
| **Seasonal Events** | Low | `content/events/{event}/` | Christmas, Easter, Halloween |
| **Clans** | Medium | `content/clans/` | Clan chat, clan wars, citadel |
| **Diary Rewards** | Medium | `content/diaries/` | Skillcape perks, free teleports, skilling bonuses |

---

## 15. Phase 7 — Polish & Custom Content (Weeks 25+)

> **Goal:** Completionist mode. Lock in your server's long-term identity.

| Feature | Effort | Location | Notes |
|---------|--------|----------|-------|
| **Custom Bosses** | High | `content/custom/bosses/` | Unique lore, drop tables, mechanics |
| **Custom Raids** | Very High | `content/custom/raids/` | Original multi-room encounter — the server's signature content |
| **Achievement System** | Medium | `content/achievements/` | Combat tasks, skilling tasks, completionist cape |
| **Tutor System** | Low | `content/npcs/tutors/` | Skill tutors in Lumbridge for new players |
| **Gambling** | Medium | `content/gambling/` | Flower poker, blackjack, dice duels |
| **117 HD Evaluation** | Research | N/A | Assess RuneLite 117 HD plugin compatibility vs. RSProx constraints |

---

## 16. Quick Wins Table

> Highest impact, lowest effort. Do these first when you need momentum.

| # | Feature | Reason | Effort | Impact |
|---|---------|--------|--------|--------|
| 1 | **Fix `fletching_knife` blocker** | Unblocks ALL integration tests server-wide | ~30 min | 🔴 Critical |
| 2 | **Fix Ardougne Guard `op[0]`** | Unblocks Ardougne gameplay in-game | ~5 min | 🔴 Critical |
| 3 | **Cooking** | Easiest skill; pairs with Fishing for instant progression loop | 1–2 days | 🟢 High |
| 4 | **Firemaking** | Simple log-burning; unlocks Wintertodt later | 1–2 days | 🟡 Medium |
| 5 | **Fishing** | Pairs with Cooking; LOC-based, clean pattern | 2–3 days | 🟢 High |
| 6 | **Fletching** | Already in progress — finish it | 2–3 days | 🟢 High |
| 7 | **Collection Log (basic shell)** | Instant retention boost; low UI cost | 1–2 days | 🟢 High |
| 8 | **Boss Highscores (kill count)** | Competitive hook; zero gameplay code needed | 1 day | 🟢 High |
| 9 | **7 more special attacks** | PvP depth; days not weeks | 2–3 days | 🟢 High |
| 10 | **Mining + Smithing** | Core economy loop; enables cannonballs | ~1 week | 🟢 High |

---

## 17. Long-term Vision

### To compete with Ferox / Alora (1,000–1,500 peak players):

| Feature | Our Gap | Realistic Effort |
|---------|---------|-----------------|
| All 23 skills | 17 missing | 3–4 months |
| All OSRS cities | 20+ missing | 2–3 months |
| 3 raids (CoX, ToB, ToA) | All missing | 3–4 months |
| 50+ bosses | All missing | 2–3 months |
| 50+ quests | All missing | 3–5 months |
| Full Grand Exchange | Missing | 2–3 weeks |
| Full Slayer system | Skeleton only | 2–3 weeks |
| Ironman + GIM | Missing | 1–2 weeks |
| Collection log | Missing | 1–2 weeks |
| Leagues / Prestige | Missing | 4–6 weeks |

**Estimated time to MVP (playable):** 6–8 weeks  
**Estimated time to competitive:** 9–12 months solo

### What Makes RSMod Different

| Advantage | How to Use It |
|-----------|--------------|
| Modular content system | Ship skills faster than monolithic servers |
| TDD approach | Fewer bugs = better player reputation |
| Clean Kotlin code | Easier to attract and onboard contributors |
| Config-driven | XP rates, drop rates tunable without code changes |
| RSProx | Remote play without native client development |
| Solo-optimised phases | Quick-win batches keep momentum and motivation high |

---

## 18. References

- **OSRS Wiki:** https://oldschool.runescape.wiki
- **RSMod Source:** https://github.com/blurite/rsmod
- **RSPS Toplists:** https://nostalgic.gg · https://rsps.org · https://rspsinsider.com · https://runelist.io
- **RSPS Reddit:** https://reddit.com/r/RSPS
- **Development Guide:** [DEVELOPMENT.md](DEVELOPMENT.md)
- **Setup Guide:** [SETUP.md](SETUP.md)
- **AI Agent Reference:** [AGENTS.md](rsmod-main/AGENTS.md)
- **Teleport Menu Design:** [docs/design/teleport-menu.md](rsmod-main/docs/design/teleport-menu.md)
- **Progress Archive:** [docs/archive/ardougne-thieving-progress.md](rsmod-main/docs/archive/ardougne-thieving-progress.md)
- **Changelog:** [CHANGELOG.md](CHANGELOG.md)
