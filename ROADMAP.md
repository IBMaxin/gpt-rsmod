# RSMod Development Roadmap

## Self-Contained Modular Architecture

**Every feature in this roadmap is implemented as a self-contained content module.** This means:

- **Zero edits to existing core files** — All new skills, quests, bosses, and areas live entirely within their own `content/` subdirectory
- **Independent development** — Each module can be built, tested, and shipped independently
- **No cross-module dependencies** — Modules communicate through the API layer, not by importing each other
- **Reusable patterns** — Skills follow the same structure (Module → Scripts → Configs → Tests)
- **Config-driven** — NPC params, item references, and content groups are defined per-module
- **TDD by default** — Every module includes integration tests in `src/integration/`

### Module Structure Template

```
content/{category}/{name}/
├── build.gradle.kts                    # Module build config
├── src/main/kotlin/.../
│   ├── {Name}Module.kt                 # Guice DI registration
│   ├── {Name}LevelBoosts.kt            # Invisible level modifiers (if needed)
│   ├── scripts/
│   │   └── {Feature}.kt                # Core gameplay script
│   └── configs/
│       ├── {Name}Refs.kt               # NpcReferences / ObjReferences
│       ├── {Name}Editor.kt             # NpcEditor / ObjEditor (param setup)
│       └── {Name}Params.kt             # Param aliases
└── src/integration/kotlin/.../
    ├── {Name}ConfigTest.kt             # Config validation tests
    └── {Feature}Test.kt                # Behavioral tests
```

### Existing Modules (Reference Patterns)

| Module | Location | Pattern |
|--------|----------|---------|
| Woodcutting | `content/skills/woodcutting/` | Gathering skill — tree chopping, axe detection, depletion, respawns |
| Thieving | `content/skills/thieving/` | Action skill — pickpocketing, success rates, stun, loot |
| Magic | `content/skills/magic/` | Combat skill — spell casting, projectiles, autocast |
| Lumbridge | `content/areas/city/lumbridge/` | Area — NPCs, shops, spawns, map data |

---

## Competitive Landscape (September 2026)

### Top OSRS Private Servers

| Server | Peak Players | Discord | Type | Key Features |
|--------|-------------|---------|------|-------------|
| Roat Pkz | 1,332 | 11,489 | 317 + OSRS | #1 PK, pre-geared builds, staking, gambling |
| Alora | 1,303 | 9,896 | OSRS-native | #1 economy, full raids, Ironman, Leagues, mobile |
| RXPS | 1,107 | 6,423 | 317 + OSRS | Economy + semi-custom, skilling, bossing |
| SpawnPK | 980 | 8,675 | Custom + OSRS | Spawn PvP, instant loadouts, Edgeville |
| RedemptionRSPS | 763 | 5,364 | OSRS | Custom bosses/raids, HD, 20-30x rates, mobile |
| Zenyte | 1,500 | — | OSRS | 10x XP, 3x drops |
| Simplicity | 890 | — | Semi-Custom | High XP, custom content |
| Zaros | 620 | — | Economy | 50x XP, trading-focused |
| BattleScape | 318 | 5,588 | OSRS | Authentic rates, Inferno, revenants, RuneLite |
| Reason PS | 365 | — | Economy + PvM | 1x XP, community-first, no pay-to-win |

### What Top Servers Have (That We Need)

| Category | Top Server Feature | RSMod Status |
|----------|-------------------|--------------|
| **Skills** | All 23 skills fully implemented | 3 of 23 (woodcutting, thieving, magic) |
| **Areas** | All OSRS cities + wilderness + dungeons | Lumbridge only |
| **Bosses** | All OSRS bosses + raids (COX, TOB, TOA) | None |
| **Quests** | 100+ OSRS-accurate quests | None |
| **Grand Exchange** | Full trading system with offers | Price data only |
| **Special Attacks** | 30+ weapons | 3 weapons |
| **Slayer** | Full system with masters, tasks, boss tasks | Params defined only |
| **Ironman** | Solo + Group ironman modes | Not implemented |
| **Minigames** | Barrows, Pest Control, BA, LMS, Castle Wars | None |
| **Diaries** | All achievement diaries with tiered rewards | None |
| **Clans** | Clan chat, wars, citadel | None |
| **Seasonal** | Leagues with relic system, events | None |
| **Mobile** | Native iOS/Android clients | RSProx (remote play) |

---

## Development Phases

### Phase 1: Core Skills — Foundation (Weeks 1-4)

**Goal**: Give players 8-10 skills to train so the server feels like RuneScape.

**Every skill is a self-contained module under `content/skills/`. No edits to existing files.**

| Skill | Effort | Pattern | Key Features | Files |
|-------|--------|---------|--------------|-------|
| **Cooking** | 1-2 days | Processing | Burn levels, all fish types, cooking gauntlets, wines | 6-8 |
| **Firemaking** | 1-2 days | Processing | Log burning, bonfires, firemaking cape | 6-8 |
| **Fletching** | 2-3 days | Processing | Bow strings, bolts, arrows, darts, knives | 6-8 |
| **Fishing** | 2-3 days | Gathering | Fishing spots, net/harpoon/cage, fish types | 6-8 |
| **Mining** | 3-4 days | Gathering | Pickaxes, rock depletion, ore variants, Mining Guild | 8-10 |
| **Smithing** | 3-4 days | Processing | Bars, armour/weapon smithing, cannonballs | 8-10 |
| **Crafting** | 3-4 days | Processing | Leather, pottery, molten glass, jewellery | 8-10 |
| **Herblore** | 3-4 days | Processing | Clean herbs, unf potions, finished potions | 8-10 |

**Phase 1 estimated effort**: ~400-1200 lines per skill, 3-4 weeks total

**Skills follow the woodcutting/thieving pattern**:
- `{Skill}Module.kt` — Guice DI registration
- `{Skill}LevelBoosts.kt` — Invisible level modifiers (potions, diaries)
- `scripts/{Skill}.kt` — Core gameplay loop
- `configs/{Skill}Refs.kt` — Item/NPC references
- `configs/{Skill}Editor.kt` — Param setup on items/NPCs
- `{Skill}ConfigTest.kt` — Config validation
- `{Skill}Test.kt` — Behavioral TDD tests

---

### Phase 2: Economy & Progression (Weeks 5-6)

**Goal**: Enable trading and basic questing.

| Feature | Effort | Details | Self-Contained? |
|---------|--------|---------|-----------------|
| **Grand Exchange** | High | Buy/sell offers, price lookup, offer management, collection box | Yes — `content/interaces/grand-exchange/` |
| **Basic Quests (5-10)** | Medium | Cook's Assistant, Romeo & Juliet, Sheep Shearer, Rune Mystics, Restless Ghost, Imp Catcher, Ernest the Chicken, Goblin Diplomacy | Yes — each quest is `content/quests/{quest-name}/` |
| **More Special Attacks** | Low | AGS, D Claws, BGS, SGS, ZGS, Arclight, D Dagger, VLS | Yes — `content/other/special-attacks/` |
| **Trading** | Medium | Player-to-player trade interface | Yes — `content/interfaces/trading/` |

**Quest Module Structure**:
```
content/quests/cooks-assistant/
├── build.gradle.kts
├── src/main/kotlin/.../
│   ├── CooksAssistantModule.kt
│   ├── scripts/CooksAssistant.kt       # Quest script
│   └── configs/
│       ├── QuestNpcRefs.kt
│       └── QuestObjRefs.kt
└── src/integration/kotlin/.../
    └── CooksAssistantTest.kt
```

---

### Phase 3: Combat Content (Weeks 7-10)

**Goal**: Make combat meaningful with Slayer, potions, and more areas.

| Feature | Effort | Details | Self-Contained? |
|---------|--------|---------|-----------------|
| **Slayer** | High | Masters, task assignment, task weights, task扩展, boss tasks, slayer helmets, points system | Yes — `content/skills/slayer/` |
| **Herblore Potions** | Already Phase 1 | Attack, Strength, Defence, Range, Magic, Prayer, Super sets, Saradomin brews, Restore | Yes — `content/skills/herblore/` |
| **Varrock** | Medium | Shops, NPCs, GE, arena, chaos altar, varrock sewers | Yes — `content/areas/city/varrock/` |
| **Falador** | Medium | Shops, NPCs, Falador shield, party room, mining guild | Yes — `content/areas/city/falador/` |
| **Edgeville** | Low | Small town, wilderness access, bank | Yes — `content/areas/city/edgeville/` |
| **Ardougne** | Medium | East/West split, docks, chaos altar, knights | Yes — `content/areas/city/ardougne/` |
| **Prayer Altars** | Low | Altars for prayer restoration, chaos altar, wilderness altar | Yes — `content/generic/prayer-altars/` |

**Slayer Module Structure**:
```
content/skills/slayer/
├── build.gradle.kts
├── src/main/kotlin/.../
│   ├── SlayerModule.kt
│   ├── SlayerLevelBoosts.kt
│   ├── scripts/
│   │   ├── Slayer.kt                   # Main slayer logic
│   │   ├── SlayerMasters.kt            # Master definitions
│   │   ├── SlayerTasks.kt              # Task definitions
│   │   └── SlayerRewards.kt            # Unlock shop
│   └── configs/
│       ├── SlayerNpcRefs.kt            # Monster references
│       └── SlayerParams.kt             # Task params
└── src/integration/kotlin/.../
    ├── SlayerConfigTest.kt
    └── SlayerTest.kt
```

---

### Phase 4: Endgame Content (Weeks 11-16)

**Goal**: Give maxed players something to work towards.

| Feature | Effort | Details | Self-Contained? |
|---------|--------|---------|-----------------|
| **Bosses (5-10)** | High | Dagannoth Kings, Bandos, Armadyl, Sara, Zammy, Zulrah, Vorkath | Yes — each boss is `content/bosses/{boss-name}/` |
| **Mini-bosses** | Medium | Cave horrors, nechryael, dust devils, black dragons | Yes — `content/npcs/mini-bosses/` |
| **Achievement Diaries** | High | Varrock, Lumbridge, Falador, Ardougne, Kandarin, Western Provinces | Yes — `content/diaries/{region}/` |
| **Agility Courses** | Medium | Gnome, Barbarian, Wilderness, Seers', Ardougne, Prifddinas | Yes — `content/skills/agility/` |
| **Runecrafting** | Medium | All altars, pouches, Ourania, GOTR-style minigame | Yes — `content/skills/runecrafting/` |
| **Farming** | High | Allotments, herbs, fruit trees, special patches, tithe farm | Yes — `content/skills/farming/` |
| **Woodcutting Guild** | Low | Requires 60+ woodcutting, special trees | Yes — `content/areas/skills/woodcutting-guild/` |

**Boss Module Structure**:
```
content/bosses/dagannoth-kings/
├── build.gradle.kts
├── src/main/kotlin/.../
│   ├── DagannothKingsModule.kt
│   ├── scripts/
│   │   ├── DagannothPrime.kt           # Magic boss
│   │   ├── DagannothRex.kt             # Melee boss
│   │   └── DagannothSupreme.kt         # Ranged boss
│   └── configs/
│       ├── BossNpcRefs.kt
│       ├── BossDrops.kt
│       └── BossSpawns.kt
└── src/integration/kotlin/.../
    ├── DagannothKingsConfigTest.kt
    └── DagannothKingsTest.kt
```

---

### Phase 5: Engagement & Retention (Weeks 17-20)

**Goal**: Keep players coming back.

| Feature | Effort | Details | Self-Contained? |
|---------|--------|---------|-----------------|
| **Minigames (3-5)** | High | Barrows, Pest Control, Castle Wars, Monkey Madness, Blast Furnace | Yes — each minigame is `content/minigames/{name}/` |
| **Ironman Mode** | Medium | Mode selection, GE restrictions, ironman hiscores, group ironman | Yes — `content/game-modes/ironman/` |
| **Clans** | Medium | Clan chat, clan wars, clan Citadel | Yes — `content/clans/` |
| **Diary Rewards** | Medium | Skillcape perks, diary armour, free teleports, skilling areas | Yes — `content/diaries/` |
| **Seasonal Events** | Low | Christmas, Easter, Halloween, Summer events | Yes — `content/events/{event}/` |

---

### Phase 6: Polish & Differentiation (Weeks 21+)

**Goal**: Stand out from other RSPS.

| Feature | Effort | Details | Self-Contained? |
|---------|--------|---------|-----------------|
| **Custom Content** | High | Unique bosses, custom raids, custom items | Yes — `content/custom/` |
| **Leagues Mode** | High | Relic system, task system, point shop | Yes — `content/game-modes/leagues/` |
| **Gambling** | Medium | Flower poker, blackjack, dice duels | Yes — `content/gambling/` |
| **Tutor System** | Low | Skill tutors in Lumbridge for new players | Yes — `content/npcs/tutors/` |
| **Achievement System** | Medium | Combat tasks, skilling tasks, completionist | Yes — `content/achievements/` |

---

## Quick Wins — Highest Impact, Lowest Effort

| # | Feature | Why | Effort | Impact |
|---|---------|-----|--------|--------|
| 1 | **Cooking** | Easiest skill, gives immediate progression | 1-2 days | High |
| 2 | **Firemaking** | Simple, gives firemaking cape, Wintertodt later | 1-2 days | Medium |
| 3 | **Fletching** | Simple processing skill, bow-making | 2-3 days | High |
| 4 | **Fishing** | Pairs with cooking, simple gathering | 2-3 days | High |
| 5 | **Mining** | Foundation for smithing, money maker | 3-4 days | High |
| 6 | **Smithing** | Core combat gear, cannonballs | 3-4 days | High |
| 7 | **Crafting** | Leather, dragonhide, jewellery | 3-4 days | High |
| 8 | **5 more special attacks** | PvP balance, takes days not weeks | 2-3 days | High |

---

## What Makes RSMod Different

| Advantage | How to Leverage |
|-----------|-----------------|
| **Modular content system** | Release skills faster than monolithic servers |
| **TDD approach** | Fewer bugs = better reputation |
| **Clean Kotlin code** | Attract other developers to help |
| **Config-driven** | Easy for players to customize rates |
| **RSProx** | Remote play without native client dev |
| **Self-contained modules** | Multiple developers can work simultaneously |

---

## MVP Milestone — "Playable Server"

**Minimum for players to stay more than 5 minutes**:

| Feature | Status | Priority |
|---------|--------|----------|
| 8+ skills to train | Pending | Critical |
| Grand Exchange | Pending | Critical |
| 3+ cities | Pending | Critical |
| 5+ quests | Pending | Critical |
| 10+ special attacks | Pending | Critical |
| Basic combat loop | Done | Done |
| Bank system | Done | Done |
| Shops | Done | Done |

**Estimated time to MVP**: 6-8 weeks for a solo developer

---

## Long-term Vision — "Competitive Server"

**To compete with Alora (1,303 peak)**:

| Feature | Our Gap | Effort |
|---------|---------|--------|
| All 23 skills | 18 missing | 3-4 months |
| All OSRS cities | 50+ missing | 2-3 months |
| 50+ bosses | All missing | 2-3 months |
| 100+ quests | All missing | 4-6 months |
| Full GE | Missing | 2-3 weeks |
| Slayer | Missing | 2-3 weeks |
| Ironman mode | Missing | 1-2 weeks |

**Estimated time to competitive**: 6-12 months for solo dev

---

## Current RSMod Inventory

### Implemented
- Combat system (full melee/ranged/magic formulas)
- Bank system (deposit/withdraw/tabs/settings)
- Prayer interface (tab, quick pray, drain, filter)
- Shops (buy/sell/restock)
- Equipment stats
- Death system
- Inventory system
- Routing/pathfinding
- 20 standard elemental spells
- Special attacks framework (3 weapons)
- Woodcutting (full)
- Thieving (pickpocketing men/women)
- Magic (spell attacks)
- Lumbridge (11 NPCs, shops, spawns)
- Canoe travel
- Admin commands
- Login system

### Not Implemented
- 18 skills (Attack, Defence, Strength, Hitpoints, Ranged, Fletching, Firemaking, Crafting, Smithing, Mining, Herblore, Agility, Slayer, Farming, Runecrafting, Hunter, Construction, Prayer content)
- Grand Exchange interface
- Quests (0)
- Bosses/Raids (0)
- Areas (only Lumbridge)
- Minigames (0)
- Achievement diaries (0)
- Ironman mode
- Clans
- Seasonal events

---

## References

- **OSRS Wiki**: https://oldschool.runescape.wiki
- **RSMod Source**: https://github.com/blurite/rsmod
- **RSPS Toplists**: https://nostalgic.gg, https://rsps.org, https://rulocus.com
- **Development Guide**: [DEVELOPMENT.md](DEVELOPMENT.md)
- **Setup Guide**: [SETUP.md](SETUP.md)
- **Changelog**: [CHANGELOG.md](CHANGELOG.md)

---

*Last updated: September 2026*
