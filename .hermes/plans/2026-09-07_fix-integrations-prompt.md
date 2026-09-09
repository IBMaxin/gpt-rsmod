Fix the integration test initialization blocker end-to-end with no assumptions.

## First: inspect the actual current state
Do not edit anything yet. From `C:/Users/bob/Desktop/gpt-rsmod/rsmod-main`:

1. Read the actual current contents of:
   - `api/config/src/main/kotlin/org/rsmod/api/config/refs/BaseContent.kt`
   - `content/skills/fletching/src/main/kotlin/org/rsmod/content/skills/fletching/configs/FletchingObjEditor.kt`
   - `content/skills/fletching/src/integration/kotlin/org/rsmod/content/skills/fletching/FletchingConfigTest.kt`

2. Search the entire `rsmod-main` tree for the exact string `fletching_knife` in all `*.kt` files. Note every file and line.

3. Re-run and capture exact failure text for:
   - `./gradlew.bat :content:skills:fletching:integration --console=plain`
   - `./gradlew.bat :content:skills:thieving:integration :content:skills:woodcutting:integration :content:areas:city:ardougne:integration --console=plain`

4. Search for the actual cache item name that should be used instead of `fletching_knife`:
   - Look in repo paths matching `*.sym` or symbol/config lookup code for `knife`.
   - Confirm whether a replacement name actually exists before using it.

## Determine the minimal correct fix
Based only on the inspection above:
- If `fletching_knife` exists in `BaseContent.kt`, remove only that reference.
- Update only files that actually use `content.fletching_knife` or the exact invalid reference.
- Do not replace it with another name unless you have verified that name exists in cache symbols. If unsure, remove the reference and delete or adjust dependent assertions rather than guessing a replacement.

## Apply only the minimal required changes
Make the smallest possible edits:
- Only edit files that actually contain the invalid reference.
- Do not refactor unrelated code.
- Do not add new content group definitions unless strictly required and verified.

## Verify everything after the fix
Run from `C:/Users/bob/Desktop/gpt-rsmod/rsmod-main` and capture exact output:

1. `./gradlew.bat :content:skills:fletching:integration --console=plain`
2. `./gradlew.bat :content:skills:thieving:integration :content:skills:woodcutting:integration :content:skills:fletching:integration :content:areas:city:ardougne:integration --console=plain`
3. `./gradlew.bat integration --console=plain`
4. `./gradlew.bat build --console=plain`

## Report format
After running, report:
- Which files you changed and the exact diffs
- Exact pass/fail result for each command
- If any task still fails, the full first failure text only
- If build passes but tests fail, the exact test failure text only
- Do not claim success unless the command outputs show success

## Constraints
- Do not edit cache/symbol files directly
- Do not change test expectations unless a test is actually failing after the initialization blocker is removed
- Do not make assumptions about valid item names; verify them from the repo
- Keep changes minimal and surgical
