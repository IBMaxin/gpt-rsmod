# Task: Fix 2 failing fletching integration tests

## Context
RSMod project at `C:\Users\bob\Desktop\gpt-rsmod\rsmod-main`. Java: `C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot`. Run tests with:
```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"; & "C:\Users\bob\Desktop\gpt-rsmod\rsmod-main\gradlew.bat" -p "C:\Users\bob\Desktop\gpt-rsmod\rsmod-main" :content:skills:fletching:integration
```

## Problem
9 integration tests, 7 pass, 2 fail. The 2 failing tests are the "makes item" success tests:
- `knife on logs makes unstrung bow` (FletchingScriptTest.kt:48)
- `bow string on unstrung bow makes strung bow` (FletchingScriptTest.kt:88)

## Root Cause Analysis
The tests use `eventBus.publish(this, event)` inside `player.withProtectedAccess { }` to trigger `onOpHeldU` script handlers. The event IS dispatched correctly (level-check tests prove this — messages are sent). Inventory `invAdd` works (new items appear). But `invDel` silently fails to remove source items.

**Key test output:**
- "knife on logs makes unstrung bow": inventory shows `[(0, knife 946), (1, logs 1511), (2, unstrung_shortbow 50)]` — bow created but logs NOT removed
- "bow string on unstrung bow makes strung bow": inventory shows `[(0, shortbow 841), (1, unstrung_shortbow 50)]` — strung bow created, bow_string removed BUT unstrung_shortbow NOT removed

**The script code (`FletchingBow.kt:49-50`):**
```kotlin
invDel(inv, log, 1)         // SILENTLY FAILS
invAdd(inv, recipe.unstrung, 1)  // WORKS
```

**The script code (`FletchingBowString.kt:57-59`):**
```kotlin
invDel(inv, unstrung, 1)    // SILENTLY FAILS
invDel(inv, FletchingObjRefs.bow_string, 1)  // WORKS
invAdd(inv, recipe.strung, 1)  // WORKS
```

## Key Facts
1. `ProtectedAccess` has its own `invDel` member function (line 982) that delegates to `player.invDel(inv, type, count, ...)`
2. `Player.invDel` calls `invTransaction` which checks `denyProtectedAccess(inv)` = `inv.type.protect && isAccessProtected`
3. Inventory type has `protect=false` in test output, so `denyProtectedAccess` should return `false`
4. `isAccessProtected` = `(isBusy || activeCoroutine?.isSuspended == true) && !pendingShutdown`
5. `withProtectedAccess` uses `player.launch { }` which starts a coroutine — if it suspends, `activeCoroutine` is set
6. `mes()` is called AFTER `invDel`/`invAdd` in the script, so suspension at `mes()` shouldn't block `invDel`
7. The `InvTransactionsTest` at `api/invtx/src/integration/kotlin/.../InvTransactionsTest.kt` proves `invDel` works in `runBasicGameTest` with `withPlayerInit` (uses `Player.invDel` extension directly)
8. The `InvTransactionsTest` uses `runBasicGameTest` (not `runGameTest`), and calls `invDel` directly (not through event bus)
9. `invAdd` and `invDel` are both `Player` extension functions AND `ProtectedAccess` member functions — inside `ProtectedAccess` scope, the member function is preferred
10. `ProtectedAccess.invDel` has no `placehold` parameter but `Player.invDel` does — named parameters handle this correctly

## Files
- **Failing test:** `content/skills/fletching/src/integration/kotlin/org/rsmod/content/skills/fletching/scripts/FletchingScriptTest.kt`
- **Script under test:** `content/skills/fletching/src/main/kotlin/org/rsmod/content/skills/fletching/scripts/FletchingBow.kt`
- **Script under test:** `content/skills/fletching/src/main/kotlin/org/rsmod/content/skills/fletching/scripts/FletchingBowString.kt`
- **ProtectedAccess.invDel:** `api/player/src/main/kotlin/org/rsmod/api/player/protect/ProtectedAccess.kt:982`
- **Player.invDel:** `api/invtx/src/main/kotlin/org/rsmod/api/invtx/InvTransactionExtensions.kt:168`
- **invTransaction:** `api/invtx/src/main/kotlin/org/rsmod/api/invtx/InvTransactionExtensions.kt:522`
- **denyProtectedAccess:** `api/invtx/src/main/kotlin/org/rsmod/api/invtx/InvTransactionExtensions.kt:529`
- **Working invDel test:** `api/invtx/src/integration/kotlin/org/rsmod/api/invtx/InvTransactionsTest.kt`
- **EventBus.publish:** `engine/events/src/main/kotlin/org/rsmod/events/EventBus.kt:33`
- **GameTestScope.withProtectedAccess:** `api/testing/src/main/kotlin/org/rsmod/api/testing/scope/GameTestScope.kt:488`
- **GameTestScope.advance:** `api/testing/src/main/kotlin/org/rsmod/api/testing/scope/GameTestScope.kt:229`

## Constraints
- Do NOT edit existing main code files (only new content or fletching module files)
- The test pattern of `withProtectedAccess` + `eventBus.publish` is the correct way to test `onOpHeldU` scripts — there is no `ifButtonT` helper in `GameTestScope`

## What to figure out
1. Why does `invDel` silently fail while `invAdd` works in the same coroutine context?
2. Is there a difference in how `ProtectedAccess.invDel` vs `Player.invDel` handles the transaction?
3. Could `isAccessProtected` be `true` during the `invDel` call even though `protect=false`?
4. Is there an issue with the transaction system when called from within a `withProtectedAccess` coroutine that hasn't suspended yet?
5. Could the `ObjType.id` comparison fail between the `HashedObjType` stored in the inventory and the one used in `invDel`?

## Suggested approaches
- Add debug logging/printing to see what `invDel` returns (TransactionResultList) and whether the transaction succeeds
- Check if `isAccessProtected` is `true` during `invDel` calls
- Try using `player.invDel(inv, type, count)` directly instead of `ProtectedAccess.invDel`
- Try calling `invDel` outside the event handler (directly in `withProtectedAccess` block) to isolate the issue
- Check if `ObjType.id` returns the expected value for `FletchingObjRefs.logs` and `FletchingObjRefs.unstrung_shortbow`
