# RSMod OpenCode Cheat Sheet

For `gpt-rsmod` / `rsmod-main` on Windows. Keep this next to OpenCode.

**First job:** do not add a new skill yet. The shared `fletching_knife` blocker can fail integration tests.

---

## 1. Repo map

| Folder | What it is | Touch it? |
|---|---|---|
| `rsmod-main/content/` | Skills, areas, NPCs, interfaces | Yes, this is where plugins go |
| `rsmod-main/api/` | Shared APIs (`ProtectedAccess`, `find()`, tests) | Only if you must add a reusable API |
| `rsmod-main/engine/` | Game loop, map, pathfinding | No |
| `rsmod-main/server/` | Startup, port 43594 | Almost never |
| `rsmod-main/cache/` | Game cache | No |
| `rsprox-master/` | Proxy to the real client | Separate app, Java 11 |

- Server language: Kotlin
- Java for RSMod: 21
- Java for RSProx: 11
- Port: 43594
- Clone example: Thieving, not `_template`

---

## 2. Golden rules

1. Never use numeric IDs. Always `find("name")`.
2. Check names against `.sym` files. Wrong names crash boot.
3. Scripts extend `PluginScript` and implement `startup()`.
4. Modules extend `PluginModule` and implement `bind()`.
5. Keep 3 files separate: refs/editors, module (DI), scripts (logic).
6. New content modules depend on `plugin-commons` only when possible.
7. Content modules must not depend on other content modules.
8. Use `GameRandom`, not `java.util.Random`.
9. `delay(1)` waits 1 tick, not 2.
10. `mes("text")` pauses the script. Use `spam("text")` if you must not pause.
11. Write `PLAN.md`, then tests, then code.
12. Format with Spotless before you call it done.

---

## 3. New plugin folders

Put a skill here:

```text
rsmod-main/content/skills/<name>/
  build.gradle.kts
  PLAN.md
  src/main/kotlin/org/rsmod/content/skills/<name>/
    <Name>Module.kt
    <Name>LevelBoosts.kt          # skills only
    configs/
      <Name>ObjRefs.kt            # find("...")
      <Name>NpcRefs.kt
      <Name>LocRefs.kt
      <Name>ObjEditor.kt          # edit(type) { ... }
    scripts/
      <Name>Script.kt
  src/integration/kotlin/...
    configs/<Name>ConfigTest.kt
    scripts/<Name>Test.kt
```

Areas go under `content/areas/city/<name>/`.

Gradle finds any folder that has `build.gradle.kts`. You usually do not edit `settings.gradle.kts`.

Minimal `build.gradle.kts`:

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

---

## 4. The 3 class types

**Refs** (names only):

```kotlin
internal object PickpocketNpcRefs : NpcReferences() {
    val man = find("man")
}
```

**Editor** (change cache data):

```kotlin
internal object PickpocketNpcEditor : NpcEditor() {
    init {
        edit(npcs.man) { param[params.something] = 1 }
    }
}
```

**Script** (what happens in game):

```kotlin
class Pickpocket @Inject constructor(
    private val invisibleLvls: InvisibleLevels,
) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc3(PickpocketNpcRefs.man) { pickpocketMan() }
    }
}
```

**Module** (glue):

```kotlin
class ThievingModule : PluginModule() {
    override fun bind() {
        addSetBinding<InvisibleLevelMod>(ThievingLevelBoosts::class.java)
    }
}
```

Short names used everywhere:

| Short name | Means |
|---|---|
| `objs` | items |
| `npcs` | NPCs |
| `locs` | scenery / objects |
| `stats` | skills |
| `invs` | inventories (`inv`, `worn`, `bank`) |
| `seqs` | animations |
| `spotanims` | gfx |
| `synths` | sounds |
| `content` | content groups |
| `params` | extra type data |

---

## 5. Event hooks

Register these inside `startup()`.

| Player does this | Hook |
|---|---|
| Clicks scenery | `onOpLoc1(type)` … `onOpLoc5(type)` |
| Uses item on scenery | `onOpLocU(locType, itemType)` |
| Clicks NPC | `onOpNpc1(type)` … `onOpNpc5(type)` |
| Uses item on NPC | `onOpNpcU(npcType, itemType)` |
| Clicks inventory item | `onOpHeld1(type)` … `onOpHeld5(type)` |
| Uses item on item | `onOpHeldU(typeA, typeB)` |
| Clicks ground item | `onOpObj1(type)` |
| Logs in | `onPlayerLogin` |
| Opens interface | `onIfOpen(iface)` |
| Types `::cmd` | `onCommand("cmd")` |
| Server boots | `onGameStartup { }` |

Do not write raw packet handlers.

---

## 6. Common script calls

These run on `ProtectedAccess` (the player sandbox).

| Need | Call |
|---|---|
| Skill level | `stat(stats.thieving)` |
| Give XP | `statAdvance(stats.thieving, xp)` |
| Success roll | `statRandom(stats.thieving, LOW, HIGH, invisibleLvls)` |
| Add item | `invAdd(inv, objs.coins, 10)` |
| Remove item | `invDel(inv, objs.logs, 1)` |
| Chat line (pauses) | `mes("You fail.")` |
| Chat line (no pause) | `spam("...")` |
| Popup | `mesbox("...")` |
| Wait before next action | `actionDelay = 2` |
| Animation / sound | `anim(seqs.x)` / `soundSynth(synths.x)` |
| Timer | `timer(timers.x, cycles)` |
| Shop | `shops.open(player, npc, name, invRef)` |
| Dialogue | `startDialogue(npc) { chatNpc(...); choice2(...) }` |

Early return for “you can’t do that yet.” Do not throw for normal player mistakes.

---

## 7. Tests

Config test = “does this name exist in the cache?”
Script test = “does the player action work?”

Pattern:

```kotlin
@Test
fun GameTestState.`player pickpockets man`() = runGameTest(Pickpocket::class) {
    // set up player / npc
    player.opNpc3(npc)
    advance(2)
    assertContains(player.inv, objs.coins)
    assertMessageSent("You pick the man's pocket.")
}
```

Gotchas:

- Set `random.next = 0` to make rolls predictable.
- Assert chat **before** `advance()` on some level-check tests.
- `invDel` can silently fail in one known path. Prefer inventory before/after checks.
- Obj transaction tests may need `@Execution(ExecutionMode.SAME_THREAD)`.

---

## 8. Windows commands

From `rsmod-main` in PowerShell:

```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"

.\gradlew.bat spotlessApply
.\gradlew.bat konsistTest --rerun-tasks
.\gradlew.bat :content:skills:thieving:test
.\gradlew.bat install
.\gradlew.bat :content:skills:thieving:integration
```

Done means all of these pass for the module you touched:

1. `spotlessApply`
2. `konsistTest --rerun-tasks`
3. module `test`
4. module `integration` (needs `install` first)

---

## 9. Do not touch

- `engine/game`, `engine/events`, `engine/plugin`, `engine/objtx`
- `engine/coroutine`, `engine/map`, `engine/routefinder`
- `server/app`, `server/install`
- cache zips, `vanilla/`, `.data/`
- `_template/` folders
- `libs.versions.toml` unless bumping a dependency

---

## 10. Known landmines

| Issue | What to do |
|---|---|
| `find("fletching_knife")` in `BaseContent.kt` | Fix this first. Docs say it can break all integration tests |
| ROADMAP vs CHANGELOG disagree on Fletching | Trust the code + a real test run |
| `firemaking/` has no `build.gradle.kts` | Not a real module yet |
| RSProx is Java 11, RSMod is Java 21 | Two processes, two JDKs |

---

## 11. OpenCode one-liner

Tell the agent:

> Follow the RSMod cheat sheet. Read-only until I approve. Clone Thieving. No numeric IDs. No engine edits. Tests first. Fix `fletching_knife` before new content.
