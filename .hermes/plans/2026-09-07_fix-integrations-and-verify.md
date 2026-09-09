# Fix Integration Failures + Verify Build, Tests, and Run

**Goal:** Remove the shared initialization blocker that prevents multiple integration suites from loading, then verify the build, relevant tests, and a runnable server path succeed end-to-end.

**Assumptions:**
- `BaseContent.kt:58` has an invalid item-style reference: `val fletching_knife = find("fletching_knife")`.
- The actual cache item name is `knife`, and the `.sym`/type resolver does not contain `fletching_knife`.
- This invalid reference causes `GameServer.verifyTypeResolver()` to fail before any integration test initializes.
- Fixing this single invalid reference may also surface remaining runtime test failures that need follow-up.
- Server run verification is limited to starting the server successfully; manual gameplay verification is out of scope unless you want a proxy or bot step later.

**Proposed approach:**
1. Inspect the current invalid reference and any usages.
2. Replace/remove the invalid reference in the canonical source file and matching module files.
3. Update related tests/docs to use a valid reference.
4. Re-run the failing integrations, then full integration task set.
5. Verify build/test success and a server run path starts cleanly.

**Step-by-step plan:**

### Task 1: Inspect invalid reference, usages, and item symbol names

**Objective:** Confirm whether the shared blocker is still `fletching_knife`, find every file using it, and confirm the valid item name.

**Read:**
- `rsmod-main/api/config/src/main/kotlin/org/rsmod/api/config/refs/BaseContent.kt`
- `rsmod-main/content/skills/fletching/src/main/kotlin/org/rsmod/content/skills/fletching/configs/FletchingObjEditor.kt`
- `rsmod-main/content/skills/fletching/src/integration/kotlin/org/rsmod/content/skills/fletching/FletchingConfigTest.kt`

**Commands:**
```bash
cd C:/Users/bob/Desktop/gpt-rsmod/rsmod-main
grep -Rni --include='*.kt' 'fletching_knife' rsmod-main
```

**Expected result:** Exact file list and whether a valid item name such as `knife` exists in cache symbols.

---

### Task 2: Remove or correct the invalid `fletching_knife` reference

**Objective:** Eliminate the verifier failure while preserving intent for fletching item content grouping.

**Preferred fix:** Remove `val fletching_knife = find("fletching_knife")` from `BaseContent.kt` because the cache does not define that content group name. If the fletching module still needs a named content group, create a module-local content group definition using a valid cache item name.

**Files to modify:**
- `rsmod-main/api/config/src/main/kotlin/org/rsmod/api/config/refs/BaseContent.kt`
- `rsmod-main/content/skills/fletching/src/main/kotlin/org/rsmod/content/skills/fletching/configs/FletchingObjEditor.kt`
- `rsmod-main/content/skills/fletching/src/integration/kotlin/org/rsmod/content/skills/fletching/FletchingConfigTest.kt`

**Verification:**
```bash
cd C:/Users/bob/Desktop/gpt-rsmod/rsmod-main
./gradlew.bat :content:skills:fletching:integration --console=plain
```

**Expected result:** The task should no longer fail at initialization. If it now runs test methods, note any new failure classes.

---

### Task 3: Re-run all previously failing integrations

**Objective:** Confirm whether the shared reference was the only blocker, and capture any remaining failures.

**Commands:**
```bash
cd C:/Users/bob/Desktop/gpt-rsmod/rsmod-main
./gradlew.bat :content:skills:thieving:integration :content:skills:woodcutting:integration :content:skills:fletching:integration :content:areas:city:ardougne:integration --console=plain
```

```bash
cd C:/Users/bob/Desktop/gpt-rsmod/rsmod-main
./gradlew.bat integration --console=plain
```

**Expected result:**
- Either all tasks complete, or
- A smaller set of failures with explicit test failures instead of `RuntimeException` initialization errors.

**Note:** If new failures appear, stop and report them before changing test behavior.

---

### Task 4: Verify full build and general test health

**Objective:** Confirm build success beyond integration tasks.

**Commands:**
```bash
cd C:/Users/bob/Desktop/gpt-rsmod/rsmod-main
./gradlew.bat build --console=plain
```

**Expected result:** Build completes without failure.

**Optional command:**
```bash
cd C:/Users/bob/Desktop/gpt-rsmod/rsmod-main
./gradlew.bat test --console=plain
```

**Expected result:** Unit tests pass or fail in a normal test-failure mode rather than initialization abort.

---

### Task 5: Verify server run path starts

**Objective:** Confirm the project can start the server entrypoint after dependency/tests changes.

**Command:**
```bash
cd C:/Users/bob/Desktop/gpt-rsmod/rsmod-main
./gradlew.bat :app:run --console=plain
```

**Expected result:** Server startup completes or fails with a clear runtime error. If it depends on cache/config paths, note the exact missing paths/files instead of retrying blindly.

---

### Task 6: Update docs to match fixed test state

**Objective:** Reflect actual post-fix test status instead of stale pass/fail claims.

**Files to update:**
- `PROGRESS.md`
- `CHANGELOG.md`
- `AI_KNOWLEDGE_BASE.md`
- `ROADMAP.md`

**Changes to make:**
- Replace all `initializationError` test status entries with actual counts once Task 3 results are known.
- Keep a short canonical note that `fletching_knife` was removed/corrected and why.

**Verification:**
```bash
cd C:/Users/bob/Desktop/gpt-rsmod
git diff --stat
```

**Expected result:** Only documentation and the intended code fix files change.

---

### Task 7: Final acceptance checks

**Objective:** Provide a single verifiable status summary.

**Run:**
```bash
cd C:/Users/bob/Desktop/gpt-rsmod/rsmod-main
./gradlew.bat integration --console=plain
```

```bash
cd C:/Users/bob/Desktop/gpt-rsmod/rsmod-main
./gradlew.bat build --console=plain
```

**Report format:**
- `integration`: pass/fail, failing tasks, failure class summary
- `build`: pass/fail
- `run`: started/stopped cleanly or exact startup failure

---

### Files likely to change

- `rsmod-main/api/config/src/main/kotlin/org/rsmod/api/config/refs/BaseContent.kt`
- `rsmod-main/content/skills/fletching/src/main/kotlin/org/rsmod/content/skills/fletching/configs/FletchingObjEditor.kt`
- `rsmod-main/content/skills/fletching/src/integration/kotlin/org/rsmod/content/skills/fletching/FletchingConfigTest.kt`
- `PROGRESS.md`
- `CHANGELOG.md`
- `AI_KNOWLEDGE_BASE.md`
- `ROADMAP.md`

---

### Risks and open questions

- If `fletching_knife` was intentionally used as a content group alias, removing it may require adding the alias to cache symbol data. If that path is required, confirm `.sym` editing is allowed in this workflow.
- After removing the shared blocker, hidden test failures in thieving/woodcutting/ardougne may appear. This plan intentionally does not change test expectations until those failures are visible.
- Server run verification may require config/cache files to be present; if `:app:run` fails on missing files, that becomes a separate setup blocker.
