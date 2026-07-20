# React TL-Script Editor Variable Completion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give the React/CodeMirror TL-Script editor `$`-variable completion (in-scope lambda params + local assignments), plus context variables declared on the `<tlscript-editor>` element.

**Architecture:** Server-side, `TLScriptEditorReactControl.complete` starts passing the client-supplied `textToCursor` and element-declared context variables into the existing `TLScriptCompletionService.computeCompletions(..., textToCursor, contextVariables, ...)`. Client-side, the CodeMirror completion source (`TLScriptEditor.tsx`) sends `textToCursor` and makes the replaced range `$`-aware so `$name` values insert cleanly.

**Tech Stack:** Java 17 (TopLogic React control + view element), TypeScript/CodeMirror 6 built with vite via `frontend-maven-plugin`, JUnit 3 (`BasicTestCase`/`BasicTestSetup`), TypedConfiguration.

## Global Constraints

- Member/static fields start with underscore `_`; locals and parameters do NOT.
- Java 17; UTF-8 source encoding (`project.build.sourceEncoding=UTF-8`); build only through Maven from project root with `-pl <module-dir>`; never `cd` into a module; never `-am`; use `mvn -B`; tests `-DskipTests=false -Dtest=<FQN>`.
- SPDX/copyright header on every new file (year 2026).
- Commit messages: `Ticket #29389: <description>` — no AI-attribution lines. **Show the diff and get the user's approval before every commit** (project rule).
- Git commit may fail with a gpg/sandbox error — retry the commit with the sandbox disabled.
- The React client TypeScript lives in `com.top_logic.model.search.react/react-src/`; it is built to `src/main/webapp/script/tl-script-editor.js` by vite during `mvn compile`. **Edit the `.tsx` source, never the generated `tl-script-editor.js`.** Do NOT run `vite`/`npx` directly — rebuild via `mvn -B compile -pl com.top_logic.model.search.react`.
- The shared `TLScriptCompletionService` must NOT gain per-client value variants; the React path reuses its existing overloads.
- Branch: `CWS/CWS_29389_tlscript_variable_completion`.

---

## File Structure

- **Modify** `com.top_logic.model.search.react/src/main/java/com/top_logic/model/search/react/TLScriptEditorReactControl.java` — hold declared context variables; `complete` handler passes `textToCursor` + context variables to the 6-arg completion overload.
- **Modify** `com.top_logic.model.search.react/src/main/java/com/top_logic/model/search/react/TLScriptEditorElement.java` — `context-variables` config option, passed to the control.
- **Modify** `com.top_logic.model.search.react/react-src/controls/TLScriptEditor.tsx` — send `textToCursor`; `$`-aware match range.
- **Test** `com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/ui/TestTLScriptAutoCompletionCommand.java` — pin the exact service overload the React handler uses (null context + textToCursor + contextVariables → `$name` values).

---

## Task 1: Server — pass textToCursor + declared context variables

**Files:**
- Modify: `com.top_logic.model.search.react/src/main/java/com/top_logic/model/search/react/TLScriptEditorReactControl.java`
- Modify: `com.top_logic.model.search.react/src/main/java/com/top_logic/model/search/react/TLScriptEditorElement.java`
- Test: `com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/ui/TestTLScriptAutoCompletionCommand.java`

**Interfaces:**
- Consumes: `TLScriptCompletionService.computeCompletions(DisplayContext, String line, String prefix, String textToCursor, java.util.Collection<String> contextVariables, boolean caseSensitive)` (exists on the branch).
- Produces: `TLScriptEditorReactControl(ReactContext, String value, boolean readOnly, List<String> contextVariables)`; `TLScriptEditorElement.Config.getContextVariables() : List<String>`.

- [ ] **Step 1: Write the failing test (service contract used by the React handler)**

Add to `TestTLScriptAutoCompletionCommand.java` (after `testContextVariablesFilteredByPrefix`). Ensure imports `java.util.stream.Collectors` and `com.top_logic.model.search.ui.CodeCompletion` exist (add if missing).

```java
	/**
	 * The overload the React editor's completion handler uses: a null {@link DisplayContext} is
	 * acceptable in variable mode, the text up to the cursor drives the in-scope variables, and the
	 * declared context variables are included — all returned with a leading <code>$</code>.
	 */
	public void testComputeCompletionsVariableModeNullContext() {
		List<CodeCompletion> completions = TLScriptCompletionService.computeCompletions(
			null, "x -> $", "$", "x -> $", List.of("ctx"), false);

		java.util.Set<String> names = new HashSet<>();
		for (CodeCompletion completion : completions) {
			names.add(completion.getName());
		}
		assertEquals(new HashSet<>(List.of("$x", "$ctx")), names);
	}
```

(Requires imports: `com.top_logic.model.search.ui.CodeCompletion`, `com.top_logic.layout.DisplayContext` is only referenced via the call's `null` literal — no import needed. `HashSet`/`List` already imported.)

- [ ] **Step 2: Run the test to verify it fails**

```bash
cd /home/dbu/Development/workspaces/TL_trunk/git/tl-engine
mvn -B test -DskipTests=false -pl com.top_logic.model.search \
  -Dtest=test.com.top_logic.model.search.ui.TestTLScriptAutoCompletionCommand 2>&1 \
  | tee com.top_logic.model.search/target/test-react-contract.log | grep -E "BUILD|Tests run|cannot find symbol"
```
Expected: compilation failure if `CodeCompletion` import is missing, otherwise the new test may already pass (it exercises existing service behavior). If it passes immediately, that is acceptable — it is a regression guard for the overload the React handler depends on; proceed. (If it FAILS, fix the imports/assertion, not the service.)

- [ ] **Step 3: Add the context-variables option to the element config**

In `TLScriptEditorElement.java`, add imports:
```java
import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
```
Add the config constant + getter inside `Config` (after `READ_ONLY`):
```java
		/** Configuration name for {@link #getContextVariables()}. */
		String CONTEXT_VARIABLES = "context-variables";
```
```java
		/**
		 * Names of variables that the surrounding infrastructure binds around the edited script and
		 * that are therefore offered at the top level of <code>$</code>-completion.
		 */
		@Name(CONTEXT_VARIABLES)
		@Format(CommaSeparatedStrings.class)
		List<String> getContextVariables();
```
(`@Format` and `@Name` are already imported in this file.)

- [ ] **Step 4: Thread the context variables into the control**

Still in `TLScriptEditorElement.java`:

Add a field alongside the others:
```java
	private final List<String> _contextVariables;
```
Set it in the constructor (after `_readOnly = config.getReadOnly();`):
```java
		_contextVariables = config.getContextVariables();
```
In `createControl`, change the control construction to pass the names:
```java
		TLScriptEditorReactControl control =
			new TLScriptEditorReactControl(context, initial, _readOnly, _contextVariables);
```

- [ ] **Step 5: Extend the control to hold and use the context variables**

In `TLScriptEditorReactControl.java`:

Add imports:
```java
import java.util.Collections;
```
(`java.util.List` is already imported.)

Add a field (after `_valueCallback`):
```java
	private final List<String> _contextVariables;
```

Replace the constructor with one that accepts the context variables (keep the Javadoc, extend it):
```java
	/**
	 * Creates a new {@link TLScriptEditorReactControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param value
	 *        The initial TL-Script text, may be {@code null}.
	 * @param readOnly
	 *        Whether the editor is read-only.
	 * @param contextVariables
	 *        Names of variables always in scope for completion (may be {@code null}).
	 */
	public TLScriptEditorReactControl(ReactContext context, String value, boolean readOnly,
			List<String> contextVariables) {
		super(context, null, "TLScriptEditor");
		putState(VALUE, value != null ? value : "");
		putState(READ_ONLY, Boolean.valueOf(readOnly));
		_contextVariables = contextVariables == null ? Collections.emptyList() : contextVariables;
	}
```

In `handleComplete`, read `textToCursor` and use the context-aware overload. Replace:
```java
		String line = (String) arguments.get("line");
		String prefix = (String) arguments.get("prefix");
		String requestId = (String) arguments.get("requestId");

		// Pass null for DisplayContext — documentation will be omitted for now.
		List<CodeCompletion> completions =
			TLScriptCompletionService.computeCompletions(null, line, prefix, false);
```
with:
```java
		String line = (String) arguments.get("line");
		String prefix = (String) arguments.get("prefix");
		String textToCursor = (String) arguments.get("textToCursor");
		String requestId = (String) arguments.get("requestId");

		// Pass null for DisplayContext — documentation will be omitted for now.
		List<CodeCompletion> completions =
			TLScriptCompletionService.computeCompletions(null, line, prefix, textToCursor, _contextVariables, false);
```

- [ ] **Step 6: Build the two modified Java modules and run the contract test**

```bash
mvn -B install -DskipTests=true -pl com.top_logic.model.search.react 2>&1 \
  | tee com.top_logic.model.search.react/target/mvn-task1.log | grep -E "BUILD|ERROR|cannot find symbol"
mvn -B test -DskipTests=false -pl com.top_logic.model.search \
  -Dtest=test.com.top_logic.model.search.ui.TestTLScriptAutoCompletionCommand 2>&1 \
  | grep -E "BUILD|Tests run"
```
Expected: both `BUILD SUCCESS`; the command test suite passes (now including `testComputeCompletionsVariableModeNullContext`). Note: the react module `install` also runs the vite build of the (still-unchanged) client — if it fails only in an unrelated network/JavaDoc phase, fall back to `mvn -B compile -pl com.top_logic.model.search.react` and the targeted test run above.

- [ ] **Step 7: Show diff and commit (after user approval)**

```bash
git add com.top_logic.model.search.react/src/main/java/com/top_logic/model/search/react/TLScriptEditorReactControl.java \
        com.top_logic.model.search.react/src/main/java/com/top_logic/model/search/react/TLScriptEditorElement.java \
        com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/ui/TestTLScriptAutoCompletionCommand.java
git commit -m "Ticket #29389: Pass text-to-cursor and context variables to React TL-Script completion."
```

---

## Task 2: Client — send text-to-cursor and make the range `$`-aware

**Files:**
- Modify: `com.top_logic.model.search.react/react-src/controls/TLScriptEditor.tsx`
- (Rebuild regenerates `com.top_logic.model.search.react/src/main/webapp/script/tl-script-editor.js`.)

**Interfaces:**
- Consumes: the server `complete` handler now reads a `textToCursor` argument (Task 1).

- [ ] **Step 1: Update the CodeMirror completion source**

In `com.top_logic.model.search.react/react-src/controls/TLScriptEditor.tsx`, in `completionSource`, replace this block:
```typescript
      // Narrow match for the word being typed — determines replacement range.
      const wordMatch = context.matchBefore(/[\w]+/);
      const prefix = wordMatch?.text ?? '';
      const from = wordMatch?.from ?? pos;

      // Send text from line start to cursor (not full line) —
      // the server uses backtick parity to detect model-part mode.
      const lineUpToCursor = line.text.substring(0, pos - line.from);

      const requestId = String(Date.now()) + Math.random();
      sendCommand('complete', { line: lineUpToCursor, prefix, requestId });
```
with:
```typescript
      // Narrow match for the word being typed — determines replacement range.
      // Include an optional leading '$' so a variable completion value ("$name")
      // returned by the server replaces the typed "$fo" cleanly (no doubled '$').
      const wordMatch = context.matchBefore(/\$?[\w]*/);
      const prefix = wordMatch?.text ?? '';
      const from = wordMatch?.from ?? pos;

      // Send text from line start to cursor (not full line) —
      // the server uses backtick parity to detect model-part mode.
      const lineUpToCursor = line.text.substring(0, pos - line.from);

      // Full text up to the cursor — the server determines the in-scope
      // variables (which may span earlier lines) for '$'-completion.
      const textToCursor = context.state.sliceDoc(0, pos);

      const requestId = String(Date.now()) + Math.random();
      sendCommand('complete', { line: lineUpToCursor, prefix, textToCursor, requestId });
```

- [ ] **Step 2: Rebuild the React client (vite via Maven) and verify the bundle regenerated**

```bash
cd /home/dbu/Development/workspaces/TL_trunk/git/tl-engine
mvn -B compile -pl com.top_logic.model.search.react 2>&1 \
  | tee com.top_logic.model.search.react/target/mvn-task2.log | grep -E "BUILD|ERROR"
git status --short com.top_logic.model.search.react/src/main/webapp/script/tl-script-editor.js
```
Expected: `BUILD SUCCESS`; `tl-script-editor.js` shows as modified (the bundle now contains `textToCursor` and `\$?[\w]*`). Confirm with:
```bash
grep -c "textToCursor" com.top_logic.model.search.react/src/main/webapp/script/tl-script-editor.js
```
Expected: at least `1`.

- [ ] **Step 3: Manual verification (Playwright) in the React demo**

1. Ensure a `<tlscript-editor>` is reachable in `com.top_logic.demo.react` (add one to a `.view.xml` if needed, e.g. `<tlscript-editor value="x -> $x" context-variables="topic, key, message" />`).
2. Start the React demo app; log in `root` / `root1234`.
3. In the editor type `all(...).filter(x -> $` → confirm `$x` is offered; select it → inserts `$x` (no `$$`).
4. Type `$` at top level with `context-variables="topic, key, message"` declared → confirm `$topic`, `$key`, `$message` are offered.
5. Confirm `` ` `` (model elements) and `.` (functions) completion still work.
Record observations (screenshots).

- [ ] **Step 4: Show diff and commit (after user approval)**

```bash
git add com.top_logic.model.search.react/react-src/controls/TLScriptEditor.tsx \
        com.top_logic.model.search.react/src/main/webapp/script/tl-script-editor.js
git commit -m "Ticket #29389: Offer variable completion in the React TL-Script editor."
```

---

## Self-Review Notes

- **Spec coverage:** client sends `textToCursor` + `$`-aware range (Task 2) ✓; server passes `textToCursor` + context variables to the 6-arg overload (Task 1) ✓; `context-variables` declared on the element (Task 1, Steps 3–4) ✓; no `$$` doubling (Task 2 range + manual step 3) ✓; server reuse, no per-client variant (Task 1 uses existing overload) ✓; tests — server contract test (Task 1) + manual Playwright (Task 2) ✓.
- **Placeholder scan:** none — all steps carry exact code/paths/commands.
- **Type consistency:** `TLScriptEditorReactControl(ReactContext, String, boolean, List<String>)` used consistently by `TLScriptEditorElement.createControl`; `getContextVariables() : List<String>` produced in Task 1 and consumed in the same task; `computeCompletions(..., textToCursor, contextVariables, false)` matches the branch's existing 6-arg overload.
- **Known limitation (from spec):** annotation-based context-variable derivation is not available in the React editor; context variables are declared explicitly on the element. Documented.
- **Build note:** the generated `tl-script-editor.js` IS git-tracked, so Task 2 commits both the `.tsx` source and the regenerated bundle (consistent with how the file is currently maintained).
