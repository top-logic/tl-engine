# TL-Script Variable Completion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Typing `$` in the TL-Script editor opens a completion popup listing the lambda-bound variables whose body encloses the cursor.

**Architecture:** A new pure `TLScriptVariableScope` analyzer reuses the checked-in JavaCC lexer to compute in-scope variables from the text before the cursor. `TLScriptAutoCompletionCommand` gains a variable-completion mode that consumes the text-up-to-cursor (newly sent by the Ace completer) and returns `$name` completions. `TLScriptCodeEditorControl` adds a trigger-regex branch so `$` opens the popup.

**Tech Stack:** Java 17, JavaCC-generated lexer (`SearchExpressionParserTokenManager`), JUnit 3 test framework (`junit.framework.TestCase` / `BasicTestCase`), Ace editor (client JS), TopLogic control-command AJAX bridge.

## Global Constraints

- Member (instance/static) fields must start with an underscore `_`. Local variables and parameters must not.
- Java source level is 17; `List.of(...)` etc. are available.
- File encoding is ISO-8859-1; build only through Maven, never `javac`.
- Build from project root with `-pl <module-dir>`; never `cd` into a module. Never use `-am`.
- Commit message format: `Ticket #<number>: <description>` — no AI-attribution lines. **Confirm the correct ticket number with the user before the first commit** (the current branch `CWS/CWS_29221` is for an unrelated ticket).
- Copyright/license header on every new Java file (copy the SPDX header block from an existing file in the same package, e.g. `TLScriptConstants.java`).

---

## File Structure

- **Create** `com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/TLScriptVariableScope.java` — pure scope analyzer.
- **Create** `com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/ui/TestTLScriptVariableScope.java` — unit tests for the analyzer.
- **Create** `com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/ui/TestTLScriptAutoCompletionCommand.java` — unit tests for the command's pure variable-mode helpers.
- **Modify** `com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/TLScriptAutoCompletionCommand.java` — add variable-completion mode + read the new `textToCursor` argument.
- **Modify** `com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/TLScriptCodeEditorControl.java` — add the `$` trigger-regex branch.
- **Modify** `com.top_logic.model.search/src/main/webapp/script/ace/mode-tlscript.js` — send `textToCursor` in the completion request.

---

## Task 1: Scope analyzer `TLScriptVariableScope`

**Files:**
- Create: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/TLScriptVariableScope.java`
- Test: `com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/ui/TestTLScriptVariableScope.java`

**Interfaces:**
- Consumes: the checked-in lexer classes in `com.top_logic.model.search.expr.parser`: `SimpleCharStream(java.io.Reader)`, `SearchExpressionParserTokenManager(SimpleCharStream)`, `Token` (fields `kind`, `image`), `TokenMgrError`, and constants `SearchExpressionParserConstants.EOF` (0) and `SearchExpressionParserConstants.NAME` (16).
- Produces: `public static List<String> inScopeVariables(String textToCursor)` — variable names **without** the leading `$`, de-duplicated, ordering unspecified (caller sorts).

- [ ] **Step 1: Write the failing test**

Create `TestTLScriptVariableScope.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.model.search.ui.TLScriptVariableScope;

/**
 * Test for {@link TLScriptVariableScope}.
 */
public class TestTLScriptVariableScope extends BasicTestCase {

	private static Set<String> scope(String textToCursor) {
		return new HashSet<>(TLScriptVariableScope.inScopeVariables(textToCursor));
	}

	public void testTopLevelIsEmpty() {
		assertEquals(set(), scope("$"));
		assertEquals(set(), scope("count(all(`M:T`), x -> $x.foo())\n$"));
	}

	public void testSingleLambda() {
		assertEquals(set("x"), scope("x -> $"));
		assertEquals(set("x"), scope("list.filter(x -> $"));
	}

	public void testNestedLambdas() {
		// count(all(`M:T`), x -> $x.foo(y -> $<cursor>
		assertEquals(set("x", "y"),
			scope("count(all(`M:T`), x -> $x.foo(y -> $"));
	}

	public void testCommaEndsLambdaBody() {
		// Second argument is no longer inside the first lambda's body.
		assertEquals(set(), scope("foo(x -> $x, $"));
	}

	public void testChainedLambdasDoNotLeak() {
		// After .map(, the filter variable is out of scope.
		assertEquals(set("y"),
			scope("list.filter(x -> $x > 0).map(y -> $"));
	}

	public void testClosedLambdaOutOfScope() {
		assertEquals(set(), scope("list.filter(x -> $x > 0).size == $"));
	}

	public void testMultiLine() {
		assertEquals(set("element"),
			scope("all(`M:T`)\n  .filter(element ->\n    $"));
	}

	public void testOptionalParameter() {
		// tuple coordinate: name? -> body
		assertEquals(set("a"), scope("tuple(a? -> $"));
	}

	public void testUnbalancedBracketsDoNotThrow() {
		// Mid-edit: no exception, best-effort result.
		assertEquals(set("x"), scope("foo(bar(x -> $"));
	}

	public void testShadowingDeduplicates() {
		assertEquals(set("x"), scope("x -> foo(x -> $"));
	}

	private static Set<String> set(String... names) {
		return new HashSet<>(List.of(names));
	}

	public static Test suite() {
		return new TestSuite(TestTLScriptVariableScope.class);
	}
}
```

- [ ] **Step 2: Run the test to verify it fails to compile / fails**

Run:
```bash
mvn -B test -DskipTests=false -pl com.top_logic.model.search \
  -Dtest=test.com.top_logic.model.search.ui.TestTLScriptVariableScope 2>&1 \
  | tee com.top_logic.model.search/target/test-scope.log | grep -E "BUILD|ERROR|cannot find symbol"
```
Expected: compilation failure — `TLScriptVariableScope` does not exist yet.

- [ ] **Step 3: Write the analyzer**

Create `TLScriptVariableScope.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.model.search.expr.parser.SearchExpressionParserConstants;
import com.top_logic.model.search.expr.parser.SearchExpressionParserTokenManager;
import com.top_logic.model.search.expr.parser.SimpleCharStream;
import com.top_logic.model.search.expr.parser.Token;
import com.top_logic.model.search.expr.parser.TokenMgrError;

/**
 * Computes the TL-Script variables that are in scope at a given cursor position.
 *
 * <p>
 * The only construct binding a variable in TL-Script is a lambda
 * (<code>name -&gt; body</code>, referenced as <code>$name</code>). A variable is in scope exactly
 * while the cursor is inside the body of the lambda that bound it. This analyzer determines that set
 * from the source text up to the cursor by a single pass over the token stream produced by the
 * TL-Script lexer.
 * </p>
 */
public class TLScriptVariableScope {

	/**
	 * The variable names (without the leading <code>$</code>) that are in scope at the end of the
	 * given text.
	 *
	 * @param textToCursor
	 *        The script source from the beginning up to (and including) the cursor position. A
	 *        trailing, partially typed variable reference (e.g. <code>$fo</code>) is ignored.
	 * @return The in-scope variable names, de-duplicated. Never <code>null</code>.
	 */
	public static List<String> inScopeVariables(String textToCursor) {
		String cleaned = stripTrailingVariable(textToCursor);

		Deque<Set<String>> stack = new ArrayDeque<>();
		stack.push(new LinkedHashSet<>());

		SimpleCharStream stream = new SimpleCharStream(new StringReader(cleaned));
		SearchExpressionParserTokenManager tokens = new SearchExpressionParserTokenManager(stream);

		Token prev = null;
		Token prevPrev = null;
		try {
			for (Token token = tokens.getNextToken(); token.kind != SearchExpressionParserConstants.EOF; token =
				tokens.getNextToken()) {
				switch (token.image) {
					case "(":
					case "[":
					case "{":
						stack.push(new LinkedHashSet<>());
						break;
					case ")":
					case "]":
					case "}":
						if (stack.size() > 1) {
							stack.pop();
						}
						break;
					case ",":
						stack.peek().clear();
						break;
					case "->":
						String name = lambdaParameter(prev, prevPrev);
						if (name != null) {
							stack.peek().add(name);
						}
						break;
					default:
						break;
				}
				prevPrev = prev;
				prev = token;
			}
		} catch (TokenMgrError error) {
			// Incomplete or invalid input while editing: use the bindings collected so far.
		}

		Set<String> result = new LinkedHashSet<>();
		for (Set<String> frame : stack) {
			result.addAll(frame);
		}
		return new ArrayList<>(result);
	}

	private static String lambdaParameter(Token prev, Token prevPrev) {
		if (prev == null) {
			return null;
		}
		if (prev.kind == SearchExpressionParserConstants.NAME) {
			return prev.image;
		}
		// Optional-parameter form used in tuple coordinates: "name? -> body".
		if ("?".equals(prev.image) && prevPrev != null
			&& prevPrev.kind == SearchExpressionParserConstants.NAME) {
			return prevPrev.image;
		}
		return null;
	}

	private static String stripTrailingVariable(String text) {
		return text.replaceFirst("\\$\\w*\\z", "");
	}

}
```

- [ ] **Step 4: Run the test to verify it passes**

Run:
```bash
mvn -B test -DskipTests=false -pl com.top_logic.model.search \
  -Dtest=test.com.top_logic.model.search.ui.TestTLScriptVariableScope 2>&1 \
  | tee com.top_logic.model.search/target/test-scope.log | grep -E "BUILD|Tests run"
```
Expected: `Tests run: 1, Failures: 0, Errors: 0` (one suite) and `BUILD SUCCESS`.

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/TLScriptVariableScope.java \
        com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/ui/TestTLScriptVariableScope.java
git commit -m "Ticket #<number>: Add TL-Script variable scope analyzer."
```

---

## Task 2: Variable-completion mode in `TLScriptAutoCompletionCommand`

**Files:**
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/TLScriptAutoCompletionCommand.java`
- Test: `com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/ui/TestTLScriptAutoCompletionCommand.java`

**Interfaces:**
- Consumes: `TLScriptVariableScope.inScopeVariables(String)` from Task 1.
- Produces:
  - `public static boolean inVariableCompletionMode(String line)` — true when the line-prefix ends in `$` + optional identifier characters.
  - `public static List<String> matchingVariables(String textToCursor, String prefix, boolean caseSensitive)` — in-scope variables filtered by the typed prefix, each returned **with** the leading `$` (e.g. `"$element"`).

- [ ] **Step 1: Write the failing test**

Create `TestTLScriptAutoCompletionCommand.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.model.search.ui.TLScriptAutoCompletionCommand;

/**
 * Test for the variable-completion helpers of {@link TLScriptAutoCompletionCommand}.
 */
public class TestTLScriptAutoCompletionCommand extends BasicTestCase {

	public void testInVariableCompletionMode() {
		assertTrue(TLScriptAutoCompletionCommand.inVariableCompletionMode("x -> $"));
		assertTrue(TLScriptAutoCompletionCommand.inVariableCompletionMode("x -> $fo"));
		assertFalse(TLScriptAutoCompletionCommand.inVariableCompletionMode("x -> $x."));
		assertFalse(TLScriptAutoCompletionCommand.inVariableCompletionMode("foo("));
		assertFalse(TLScriptAutoCompletionCommand.inVariableCompletionMode(""));
	}

	public void testMatchingVariablesReturnsDollarPrefixed() {
		Set<String> result =
			new HashSet<>(TLScriptAutoCompletionCommand.matchingVariables("x -> foo(y -> $", "$", false));
		assertEquals(new HashSet<>(List.of("$x", "$y")), result);
	}

	public void testMatchingVariablesFiltersByPrefix() {
		List<String> result =
			TLScriptAutoCompletionCommand.matchingVariables("element -> foo(other -> $el", "$el", false);
		assertEquals(List.of("$element"), result);
	}

	public void testMatchingVariablesCaseInsensitive() {
		List<String> result =
			TLScriptAutoCompletionCommand.matchingVariables("Element -> $e", "$e", false);
		assertEquals(List.of("$Element"), result);
	}

	public void testMatchingVariablesCaseSensitive() {
		List<String> result =
			TLScriptAutoCompletionCommand.matchingVariables("Element -> $e", "$e", true);
		assertEquals(List.of(), result);
	}

	public static Test suite() {
		return new TestSuite(TestTLScriptAutoCompletionCommand.class);
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run:
```bash
mvn -B test -DskipTests=false -pl com.top_logic.model.search \
  -Dtest=test.com.top_logic.model.search.ui.TestTLScriptAutoCompletionCommand 2>&1 \
  | tee com.top_logic.model.search/target/test-cmd.log | grep -E "BUILD|ERROR|cannot find symbol"
```
Expected: compilation failure — `inVariableCompletionMode` / `matchingVariables` do not exist.

- [ ] **Step 3: Add the pure helper methods**

In `TLScriptAutoCompletionCommand.java`, add imports if missing (`java.util.ArrayList`, `java.util.List` already present) and add these methods (e.g. just after `createDefaultFunctions`):

```java
	/**
	 * Whether the given line-prefix ends with a (possibly empty) variable reference, i.e. a
	 * <code>$</code> optionally followed by identifier characters.
	 */
	public static boolean inVariableCompletionMode(String line) {
		return VARIABLE_PREFIX_PATTERN.matcher(line).find();
	}

	/**
	 * The in-scope variables (each with the leading <code>$</code>) matching the typed prefix.
	 *
	 * @param textToCursor
	 *        The script source up to the cursor.
	 * @param prefix
	 *        The completion prefix as computed by the editor; includes the leading <code>$</code>.
	 * @param caseSensitive
	 *        Whether prefix matching is case sensitive.
	 */
	public static List<String> matchingVariables(String textToCursor, String prefix, boolean caseSensitive) {
		String barePrefix = prefix.startsWith("$") ? prefix.substring(1) : prefix;

		List<String> result = new ArrayList<>();
		for (String variable : TLScriptVariableScope.inScopeVariables(textToCursor)) {
			boolean matches = caseSensitive
				? variable.startsWith(barePrefix)
				: variable.toUpperCase().startsWith(barePrefix.toUpperCase());
			if (matches) {
				result.add("$" + variable);
			}
		}
		return result;
	}
```

Add the pattern constant near the top of the class (after the `COMMAND_ID` field):

```java
	private static final Pattern VARIABLE_PREFIX_PATTERN = Pattern.compile("\\$\\w*$");
```

(`java.util.regex.Pattern` is already imported.)

- [ ] **Step 4: Run the test to verify it passes**

Run:
```bash
mvn -B test -DskipTests=false -pl com.top_logic.model.search \
  -Dtest=test.com.top_logic.model.search.ui.TestTLScriptAutoCompletionCommand 2>&1 \
  | tee com.top_logic.model.search/target/test-cmd.log | grep -E "BUILD|Tests run"
```
Expected: `Tests run: 1, Failures: 0, Errors: 0` and `BUILD SUCCESS`.

- [ ] **Step 5: Wire the mode into request handling**

Still in `TLScriptAutoCompletionCommand.java`:

(a) In `execute(...)`, read the new argument and thread it through. Replace the existing body up to the `createCompletions` call:

```java
		TLScriptCodeEditorControl scriptCodeControl = (TLScriptCodeEditorControl) control;

		String prefix = (String) arguments.get("prefix");
		String line = (String) arguments.get("line");
		String textToCursor = (String) arguments.get("textToCursor");

		Optional<List<CodeCompletion>> completions = createCompletions(commandContext, line, prefix, textToCursor);
```

(b) Change `createCompletions` to accept `textToCursor` and dispatch to the variable mode. Replace the whole `createCompletions` method:

```java
	private Optional<List<CodeCompletion>> createCompletions(DisplayContext context, String line, String prefix,
			String textToCursor) {
		if (inTLModelPartCompletionMode(line)) {
			return createTLModelPartCompletions(line);
		} else if (inTextMode(line)) {
			return Optional.empty();
		} else if (inVariableCompletionMode(line)) {
			return createVariableCompletions(textToCursor, prefix);
		} else {
			return createDefaultCompletion(context, line, prefix);
		}
	}

	private Optional<List<CodeCompletion>> createVariableCompletions(String textToCursor, String prefix) {
		if (textToCursor == null) {
			return Optional.empty();
		}
		List<CodeCompletion> completions = new ArrayList<>();
		for (String variable : matchingVariables(textToCursor, prefix, _completeCaseSensitive)) {
			CodeCompletion completion = new CodeCompletion();
			completion.setName(variable);
			completion.setValue(variable);
			completions.add(completion);
		}
		return Optional.of(completions);
	}
```

Note: no `snippet` is set, so the client falls back to the `value` (`$name`) when inserting — a `$`-bearing snippet would be misinterpreted as a snippet tab-stop.

- [ ] **Step 6: Rebuild the module to verify compilation and re-run both test suites**

Run:
```bash
mvn -B install -DskipTests=false -pl com.top_logic.model.search \
  -Dtest=test.com.top_logic.model.search.ui.TestTLScriptVariableScope,test.com.top_logic.model.search.ui.TestTLScriptAutoCompletionCommand 2>&1 \
  | tee com.top_logic.model.search/target/mvn-build.log | grep -E "BUILD|Tests run"
```
Expected: both suites pass and `BUILD SUCCESS`.

- [ ] **Step 7: Commit**

```bash
git add com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/TLScriptAutoCompletionCommand.java \
        com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/ui/TestTLScriptAutoCompletionCommand.java
git commit -m "Ticket #<number>: Add variable-completion mode to TL-Script autocompletion command."
```

---

## Task 3: Trigger regex + client wiring + manual verification

**Files:**
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/TLScriptCodeEditorControl.java`
- Modify: `com.top_logic.model.search/src/main/webapp/script/ace/mode-tlscript.js`

**Interfaces:**
- Consumes: the server-side `tlScriptAutoCompletion` command now reads a `textToCursor` argument (Task 2).
- Produces: end-user behavior — typing `$` opens the popup with in-scope variables.

- [ ] **Step 1: Add the `$` trigger-regex branch (server-generated client regex)**

In `TLScriptCodeEditorControl.java`, add a fourth alternative to `createTLScriptAutoCompletionRegex()` and a helper. Replace the method and add the helper:

```java
	private String createTLScriptAutoCompletionRegex() {
		return new TLRegexBuilder().add(createTLModelConstantExpression())
			.or().add(createTLScriptFunctionExpression())
			.or().add(createVariableExpression())
			.or().add(createDefaultExpression()).toJavascriptString();
	}

	private String createVariableExpression() {
		return new TLRegexBuilder().then("$").emptyWord().endOfLine().toString();
	}
```

(`createVariableExpression` produces `(?:\$)(?:\w*)$` — a `$` followed by optional word characters at end of line.)

- [ ] **Step 2: Send `textToCursor` from the Ace completer**

In `mode-tlscript.js`, inside `tlScriptCompleter.getCompletions`, compute the text up to the cursor and add it to the AJAX call. Replace the body of `getCompletions` from the `var line` line through the `services.ajax.execute(...)` call:

```javascript
				var line = session.getLine(pos.row);
				var prefixLine = line.substring(0, pos.column);

				var fullText = session.getValue();
				var cursorOffset = session.doc.positionToIndex(pos);
				var textToCursor = fullText.substring(0, cursorOffset);

				// Delegates the computation of suggestions to the server.
				services.ajax.execute('dispatchControlCommand', {controlCommand: 'tlScriptAutoCompletion', controlID: controlID, prefix: prefix, line: prefixLine, textToCursor: textToCursor });
```

- [ ] **Step 3: Rebuild the module (packages the changed JS into the webapp)**

Run:
```bash
mvn -B install -pl com.top_logic.model.search 2>&1 \
  | tee com.top_logic.model.search/target/mvn-build.log | grep -E "BUILD"
```
Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Manually verify in the demo app with Playwright**

1. Start the demo app (use the `tl-app` skill). Log in as `root` / `root1234`.
2. Navigate to a TL-Script editor. Reliable location: the expert search (an `ExpertSearchExpressionEditor` / `TLScriptCodeEditorControl` usage). If no `$`-scope is readily reachable, type a scripted expression such as:
   ```
   all(`tl.demo.types:A`).filter(x -> $)
   ```
   Confirm the popup appears after typing `$` and lists `$x`.
3. Type a nested case:
   ```
   all(`tl.demo.types:A`).filter(x -> $x.foo(y -> $))
   ```
   Confirm both `$x` and `$y` are offered at the inner `$`.
4. At the very start of an empty editor, type `$` and confirm no variables are offered.
5. Confirm that inside a string literal (`"...$"`) typing `$` offers nothing.
6. Confirm selecting a suggestion inserts `$name` correctly (replacing the typed `$`).

Record the observed behavior (screenshots) before declaring done.

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/TLScriptCodeEditorControl.java \
        com.top_logic.model.search/src/main/webapp/script/ace/mode-tlscript.js
git commit -m "Ticket #<number>: Trigger TL-Script variable completion on '$'."
```

---

## Self-Review Notes

- **Spec coverage:** trigger on `$` (Task 3) ✓; accurate enclosing-lambda scope (Task 1) ✓; full text + cursor sent to server, realized as `textToCursor` (Task 3 client + Task 2 server) ✓; no suggestions in strings/model constants (Task 2 mode dispatch order) ✓; nothing at top level (Task 1 `testTopLevelIsEmpty`) ✓; ordering/filtering consistent (Task 2 reuses case-sensitivity flag; command already sorts via `CompletionByNameComparator`) ✓; JUnit tests (Tasks 1–2) + Playwright (Task 3) ✓.
- **Known limitation (documented in spec "Out of scope" spirit):** inside embedded HTML template regions (`{{{ ... }}}`) the lexer switches states and bracket tracking is approximate; the try/catch prevents any failure. Acceptable for this iteration.
- **Type consistency:** `inScopeVariables` returns names without `$`; `matchingVariables` adds the `$`; `createVariableCompletions` sets both `name` and `value` to the `$`-prefixed string; no `snippet`.
```
