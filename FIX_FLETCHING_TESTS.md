# Task: Fix failing fletching integration tests

## Context
RSMod project at `C:\Users\bob\Desktop\gpt-rsmod\rsmod-main`. Java: `C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot`. Run tests with:
```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"; & "C:\Users\bob\Desktop\gpt-rsmod\rsmod-main\gradlew.bat" -p "C:\Users\bob\Desktop\gpt-rsmod\rsmod-main" :content:skills:fletching:integration
```

## CURRENT STATUS: BLOCKED BY GLOBAL TYPE VERIFIER ERROR

All integration tests (fletching, thieving, woodcutting, and ALL skills) fail with the same error:
```
java.lang.RuntimeException: The following references use names that are not defined in a .sym file (1 found)
	- Name: "fletching_knife"
	at org.rsmod.server.app.GameServer.verifyTypeResolver(GameServer.kt:229)
```

This is a **global blocker** - the `content.fletching_knife` reference in `BaseContent.kt:58` uses `find("fletching_knife")` which creates a `ContentGroupType`, but the type verifier validates it against the item `.sym` file (which only contains actual item names like `knife`, not `fletching_knife`). This causes ALL integration tests to fail at test initialization before any test code runs.

## Root Cause Analysis (Original - superseded by global blocker)

Previously identified a separate `invDel` failure in fletching tests. That issue is now **hidden** because the global type verifier error prevents ALL tests from initializing.

## Key Facts

1. `BaseContent.kt:58` defines `val fletching_knife = find("fletching_knife")`
2. `FletchingObjEditor.kt:13` uses `contentGroup = content.fletching_knife`
3. `FletchingConfigTest.kt:75` tests `assertTrue(knife!!.isContentType(content.fletching_knife))`
4. `ContentReferences.find()` creates a `ContentGroupType` but the verifier rejects it as undefined in the `.sym` file
5. The actual item is `knife` (id 946) per `.data/symbols/obj.sym`
6. ALL other skills' integration tests also fail with the same error
7. The `InvTransactionsTest` proves `invDel` works when called directly (via `runBasicGameTest`)

## Files
- **Failing test:** `content/skills/fletching/src/integration/kotlin/org/rsmod/content/skills/fletching/scripts/FletchingScriptTest.kt`
- **Script under test:** `content/skills/fletching/src/main/kotlin/org/rsmod/content/skills/fletching/scripts/FletchingBow.kt`
- **Script under test:** `content/skills/fletching/src/main/kotlin/org/rsmod/content/skills/fletching/scripts/FletchingBowString.kt`
- **Global blocker:** `api/config/src/main/kotlin/org/rsmod/api/config/refs/BaseContent.kt:58`
- **Editor using blocker:** `content/skills/fletching/src/main/kotlin/org/rsmod/content/skills/fletching/configs/FletchingObjEditor.kt:13`
- **Config test using blocker:** `content/skills/fletching/src/integration/kotlin/org/rsmod/content/skills/fletching/FletchingConfigTest.kt:75`
- **ProtectedAccess.invDel:** `api/player/src/main/kotlin/org/rsmod/api/player/protect/ProtectedAccess.kt:982`
- **Player.invDel:** `api/invtx/src/main/kotlin/org/rsmod/api/invtx/InvTransactionExtensions.kt:168`
- **invTransaction:** `api/invtx/src/main/kotlin/org/rsmod/api/invtx/InvTransactionExtensions.kt:522`
- **denyProtectedAccess:** `api/invtx/src/main/kotlin/org/rsmod/api/invtx/InvTransactionExtensions.kt:529`
- **Working invDel test:** `api/invtx/src/integration/kotlin/org/rsmod/api/invtx/InvTransactionsTest.kt`
- **EventBus.publish:** `engine/events/src/main/kotlin/org/rsmod/events/EventBus.kt:33`
- **GameTestScope.withProtectedAccess:** `api/testing/src/main/kotlin/org/rsmod/api/testing/scope/GameTestScope.kt:488`
- **GameTestScope.advance:** `api/testing/src/main/kotlin/org/rsmod/api/testing/scope/GameTestScope.kt:229`
- **Type verifier:** `api/type/type-verifier/src/main/kotlin/org/rsmod/api/type/verifier/TypeVerifier.kt`

## Constraints
- Do NOT edit existing main code files (only new content or fletching module files)
- The test pattern of `withProtectedAccess` + `eventBus.publish` is the correct way to test `onOpHeldU` scripts - there is no `ifButtonT` helper in `GameTestScope`

## What to figure out
1. **PRIORITY 1**: Fix `BaseContent.kt` - either remove `fletching_knife` or change it to use the actual item name `knife` or add it as a valid content group
2. Why does `invDel` silently fail while `invAdd` works in the same coroutine context?
3. Is there a difference in how `ProtectedAccess.invDel` vs `Player.invDel` handles the transaction?
4. Could `isAccessProtected` be `true` during the `invDel` call even though `protect=false`?
5. Is there an issue with the transaction system when called from within a `withProtectedAccess` coroutine that hasn't suspended yet?
6. Could the `ObjType.id` comparison fail between the `HashedObjType` stored in the inventory and the one used in `invDel`?

## Suggested approaches
- **PRIORITY 1**: Fix the `fletching_knife` content reference in `BaseContent.kt` to use a valid name
- Add debug logging/printing to see what `invDel` returns (TransactionResultList) and whether the transaction succeeds
- Check if `isAccessProtected` is `true` during `invDel` calls
- Try using `player.invDel(inv, type, count)` directly instead of `ProtectedAccess.invDel`
- Try calling `invDel` outside the event handler (directly in `withProtectedAccess` block) to isolate the issue
- Check if `ObjType.id` returns the expected value for `FletchingObjRefs.logs` and `FletchingObjRefs.unstrung_shortbow`
