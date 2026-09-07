# TL-Script Completion Context Variables Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let an `Expr` editor offer additional top-level `$`-variables that a surrounding context weaves into the script — first use: the OpenAPI operation parameters in `ServiceMethodBuilderByExpression`.

**Architecture:** A generic SPI in `model.search` (annotation `@ScriptContextVariables` + provider interface) lets an `Expr` property declare a provider that computes variable names from the edited configuration. `TLScriptCodeEditorControl` resolves them server-side (via the field's `ValueModel`) and hands them to `TLScriptAutoCompletionCommand`, which unions them (always in scope) into the completion candidates in `TLScriptCompletionService`. OpenAPI supplies a provider that reads the operation's declared parameters.

**Tech Stack:** Java 17, TopLogic TypedConfiguration (`ConfigPart`, `@Container`, `PropertyDescriptor.getAnnotation`), declarative forms (`EditorFactory.getValueModel`, `ValueModel`), JUnit 3 (`BasicTestCase` / `BasicTestSetup`).

## Global Constraints

- Member/static fields start with underscore `_`; locals and parameters do NOT.
- Java 17; ISO-8859-1 source encoding; build only through Maven, from project root with `-pl <module-dir>`; never `cd` into a module; never use `-am`.
- Tests skipped by default; enable with `-DskipTests=false`; `-Dtest` = fully-qualified class name; use `mvn -B`.
- SPDX/copyright header on every new Java file (copy from a sibling file in the same package; year 2026).
- Commit messages: `Ticket #29389: <description>` — no AI-attribution lines. **Show the diff and get the user's approval before every commit** (project rule).
- Git commit may fail with a gpg/sandbox error ("read-only file system" / "gpg-agent") — retry the commit with the sandbox disabled.
- JUnit3 `suite()` must wrap with `BasicTestSetup.createBasicTestSetup(new TestSuite(TheTest.class))`.
- Branch: `CWS/CWS_29389_tlscript_variable_completion` (this feature builds on the `$`-completion work already on it).

---

## File Structure

- **Create** `com.top_logic.model.search/.../ui/ScriptContextVariablesProvider.java` — provider interface.
- **Create** `com.top_logic.model.search/.../ui/ScriptContextVariables.java` — the `@ScriptContextVariables` annotation.
- **Create** `com.top_logic.model.search/.../ui/ScriptContextVariablesResolver.java` — resolves a `ValueModel` to the provider's variable names (guarded).
- **Modify** `com.top_logic.model.search/.../ui/TLScriptCompletionService.java` — union context variables into completion candidates.
- **Modify** `com.top_logic.model.search/.../ui/TLScriptAutoCompletionCommand.java` — pass the control's context variables to the service.
- **Modify** `com.top_logic.model.search/.../ui/TLScriptCodeEditorControl.java` — `getContextVariableNames()`.
- **Modify** `com.top_logic.service.openapi.server/.../impl/ServiceMethodBuilderByExpression.java` — `Config` becomes a `ConfigPart`, `getOperation()` annotated.
- **Create** `com.top_logic.service.openapi.server/.../impl/OperationParameterVariables.java` — the OpenAPI provider.
- **Tests:** `TestTLScriptAutoCompletionCommand.java` (extend), `TestScriptContextVariablesResolver.java` (new), `TestOperationParameterVariables.java` (new).

---

## Task 1: Union context variables into completion (model.search, core)

**Files:**
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/TLScriptCompletionService.java`
- Test: `com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/ui/TestTLScriptAutoCompletionCommand.java`

**Interfaces:**
- Consumes: existing `TLScriptVariableScope.inScopeVariables(String)`, existing static `startsWith(String, String, boolean)`.
- Produces:
  - `public static List<String> matchingVariables(String textToCursor, Collection<String> contextVariables, String prefix, boolean caseSensitive)` — union of text-scope variables and context variables, de-duplicated, prefix-filtered, each `$`-prefixed.
  - `public static List<CodeCompletion> computeCompletions(DisplayContext, String line, String prefix, String textToCursor, Collection<String> contextVariables, boolean caseSensitive)` — new overload.

- [ ] **Step 1: Write the failing test**

Add these methods to `TestTLScriptAutoCompletionCommand.java` (after `testMatchingVariablesCaseSensitive`):

```java
	public void testContextVariablesOfferedTopLevel() {
		java.util.Set<String> result = new HashSet<>(
			TLScriptCompletionService.matchingVariables("$", List.of("path", "id"), "$", false));
		assertEquals(new HashSet<>(List.of("$path", "$id")), result);
	}

	public void testContextVariablesUnionWithLambda() {
		java.util.Set<String> result = new HashSet<>(
			TLScriptCompletionService.matchingVariables("x -> $", List.of("path"), "$", false));
		assertEquals(new HashSet<>(List.of("$x", "$path")), result);
	}

	public void testContextVariablesDeduplicated() {
		java.util.Set<String> result = new HashSet<>(
			TLScriptCompletionService.matchingVariables("x -> $", List.of("x"), "$", false));
		assertEquals(new HashSet<>(List.of("$x")), result);
	}

	public void testContextVariablesFilteredByPrefix() {
		assertEquals(List.of("$path"),
			TLScriptCompletionService.matchingVariables("$pa", List.of("path", "id"), "$pa", false));
	}
```

Ensure `import java.util.List;` and `import java.util.HashSet;` exist (they do).

- [ ] **Step 2: Run the test to verify it fails**

```bash
cd /home/dbu/Development/workspaces/TL_trunk/git/tl-engine
mvn -B test -DskipTests=false -pl com.top_logic.model.search \
  -Dtest=test.com.top_logic.model.search.ui.TestTLScriptAutoCompletionCommand 2>&1 \
  | tee com.top_logic.model.search/target/test-ctx.log | grep -E "BUILD|ERROR|cannot find symbol"
```
Expected: compilation failure — the 4-arg `matchingVariables` overload does not exist.

- [ ] **Step 3: Implement the union in the service**

In `TLScriptCompletionService.java`:

(a) Add `import java.util.Collection;` and `import java.util.LinkedHashSet;` if missing (`java.util.Collection` and `java.util.LinkedHashSet` — add them to the import block).

(b) Replace the existing `matchingVariables(String, String, boolean)` method with a delegating pair that adds the context-variables variant:

```java
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
		return matchingVariables(textToCursor, Collections.emptyList(), prefix, caseSensitive);
	}

	/**
	 * Variant of {@link #matchingVariables(String, String, boolean)} that also offers the given
	 * context variables, which are always in scope (they wrap the whole script).
	 *
	 * @param contextVariables
	 *        Variable names (without <code>$</code>) provided by the surrounding context.
	 */
	public static List<String> matchingVariables(String textToCursor, Collection<String> contextVariables,
			String prefix, boolean caseSensitive) {
		String barePrefix = prefix.startsWith("$") ? prefix.substring(1) : prefix;

		LinkedHashSet<String> variables = new LinkedHashSet<>(TLScriptVariableScope.inScopeVariables(textToCursor));
		variables.addAll(contextVariables);

		List<String> result = new ArrayList<>();
		for (String variable : variables) {
			if (startsWith(variable, barePrefix, caseSensitive)) {
				result.add("$" + variable);
			}
		}
		return result;
	}
```

(c) Add the context-aware `computeCompletions` overload and thread the parameter through `createCompletions` / `createVariableCompletions`. Replace the current context-less `computeCompletions(context, line, prefix, textToCursor, caseSensitive)`, `createCompletions(...)` and `createVariableCompletions(...)` with:

```java
	public static List<CodeCompletion> computeCompletions(DisplayContext context, String line,
			String prefix, String textToCursor, boolean caseSensitive) {
		return computeCompletions(context, line, prefix, textToCursor, Collections.emptyList(), caseSensitive);
	}

	/**
	 * Variant of
	 * {@link #computeCompletions(DisplayContext, String, String, String, boolean)} that also offers
	 * the given always-in-scope context variables for <code>$</code>-completion.
	 */
	public static List<CodeCompletion> computeCompletions(DisplayContext context, String line,
			String prefix, String textToCursor, Collection<String> contextVariables, boolean caseSensitive) {
		Optional<List<CodeCompletion>> completions =
			createCompletions(context, line, prefix, textToCursor, contextVariables, caseSensitive);

		completions.ifPresent(list -> orderCompletions(list));

		return completions.orElse(Collections.emptyList());
	}

	private static Optional<List<CodeCompletion>> createCompletions(DisplayContext context, String line,
			String prefix, String textToCursor, Collection<String> contextVariables, boolean caseSensitive) {
		if (inTLModelPartCompletionMode(line)) {
			return createTLModelPartCompletions(line, caseSensitive);
		} else if (inTextMode(line)) {
			return Optional.empty();
		} else if (inVariableCompletionMode(line)) {
			return createVariableCompletions(textToCursor, contextVariables, prefix, caseSensitive);
		} else {
			return createDefaultCompletion(context, line, prefix, caseSensitive);
		}
	}

	private static Optional<List<CodeCompletion>> createVariableCompletions(String textToCursor,
			Collection<String> contextVariables, String prefix, boolean caseSensitive) {
		if (textToCursor == null && contextVariables.isEmpty()) {
			return Optional.empty();
		}
		String safeText = textToCursor == null ? "" : textToCursor;
		List<CodeCompletion> completions = new ArrayList<>();
		for (String variable : matchingVariables(safeText, contextVariables, prefix, caseSensitive)) {
			CodeCompletion completion = new CodeCompletion();
			completion.setName(variable);
			completion.setValue(variable);
			completions.add(completion);
		}
		return Optional.of(completions);
	}
```

Note: `TLScriptVariableScope.inScopeVariables("")` returns an empty list, so passing `safeText` when `textToCursor` is null but context variables exist still yields the context variables.

- [ ] **Step 4: Run the test to verify it passes**

```bash
mvn -B test -DskipTests=false -pl com.top_logic.model.search \
  -Dtest=test.com.top_logic.model.search.ui.TestTLScriptAutoCompletionCommand 2>&1 \
  | tee com.top_logic.model.search/target/test-ctx.log | grep -E "BUILD|Tests run"
```
Expected: all tests pass, `BUILD SUCCESS`.

- [ ] **Step 5: Show diff and commit (after user approval)**

```bash
git add com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/TLScriptCompletionService.java \
        com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/ui/TestTLScriptAutoCompletionCommand.java
git commit -m "Ticket #29389: Offer context variables in TL-Script completion."
```

---

## Task 2: Generic SPI + control plumbing (model.search)

**Files:**
- Create: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/ScriptContextVariablesProvider.java`
- Create: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/ScriptContextVariables.java`
- Create: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/ScriptContextVariablesResolver.java`
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/TLScriptCodeEditorControl.java`
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/TLScriptAutoCompletionCommand.java`
- Test: `com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/ui/TestScriptContextVariablesResolver.java`

**Interfaces:**
- Consumes: `EditorFactory.getValueModel(FormMember)` → `ValueModel`; `ValueModel.getProperty()` → `PropertyDescriptor`; `PropertyDescriptor.getAnnotation(Class)`.
- Produces:
  - `interface ScriptContextVariablesProvider { List<String> getVariables(ValueModel valueModel); }`
  - `@ScriptContextVariables(Class<? extends ScriptContextVariablesProvider>)` (method target, runtime retention).
  - `ScriptContextVariablesResolver.resolve(ValueModel) : List<String>`.
  - `TLScriptCodeEditorControl.getContextVariableNames() : List<String>`.

- [ ] **Step 1: Write the failing test**

Create `TestScriptContextVariablesResolver.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.ui;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.form.values.edit.PropertyValue;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.search.ui.ScriptContextVariablesProvider;
import com.top_logic.model.search.ui.ScriptContextVariablesResolver;

/**
 * Test for {@link ScriptContextVariablesResolver}.
 */
public class TestScriptContextVariablesResolver extends BasicTestCase {

	/** Provider returning fixed names. */
	public static class FixedProvider implements ScriptContextVariablesProvider {
		@Override
		public List<String> getVariables(ValueModel valueModel) {
			return List.of("alpha", "beta");
		}
	}

	/** Config with an annotated property. */
	public interface Sample extends ConfigurationItem {
		String NAME = "value";

		@Name(NAME)
		@ScriptContextVariables(FixedProvider.class)
		String getValue();
	}

	/** Config with an un-annotated property. */
	public interface Plain extends ConfigurationItem {
		String NAME = "value";

		@Name(NAME)
		String getValue();
	}

	public void testResolvesAnnotatedProperty() {
		assertEquals(List.of("alpha", "beta"), ScriptContextVariablesResolver.resolve(valueModel(Sample.class)));
	}

	public void testEmptyWhenNoAnnotation() {
		assertEquals(List.of(), ScriptContextVariablesResolver.resolve(valueModel(Plain.class)));
	}

	public void testEmptyWhenNullValueModel() {
		assertEquals(List.of(), ScriptContextVariablesResolver.resolve(null));
	}

	private static ValueModel valueModel(Class<? extends ConfigurationItem> configType) {
		ConfigurationItem model = TypedConfiguration.newConfigItem(configType);
		PropertyDescriptor property = model.descriptor().getProperty("value");
		return new ValueModel() {
			@Override
			public ConfigurationItem getModel() {
				return model;
			}

			@Override
			public PropertyDescriptor getProperty() {
				return property;
			}

			@Override
			public Object getValue() {
				return model.value(property);
			}

			@Override
			public PropertyValue getPropertyValue() {
				return null;
			}
		};
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestScriptContextVariablesResolver.class));
	}
}
```

(If `ValueModel` has more abstract methods than the four overridden above, implement them minimally returning `null`/the field value — check `com.top_logic/.../edit/ValueModel.java` when the compile fails and add the missing overrides.)

- [ ] **Step 2: Run to verify it fails**

```bash
mvn -B test -DskipTests=false -pl com.top_logic.model.search \
  -Dtest=test.com.top_logic.model.search.ui.TestScriptContextVariablesResolver 2>&1 \
  | tee com.top_logic.model.search/target/test-resolver.log | grep -E "BUILD|ERROR|cannot find symbol"
```
Expected: compilation failure — the SPI types don't exist yet.

- [ ] **Step 3: Create the provider interface**

Create `ScriptContextVariablesProvider.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import java.util.List;

import com.top_logic.layout.form.values.edit.ValueModel;

/**
 * Provides the names of the variables that a surrounding context makes available at the top level of
 * a TL-Script expression, for use by <code>$</code>-completion.
 *
 * <p>
 * Implementations must have a public no-argument constructor.
 * </p>
 *
 * @see ScriptContextVariables
 */
public interface ScriptContextVariablesProvider {

	/**
	 * The context variable names (without the leading <code>$</code>) for the edited property.
	 *
	 * @param valueModel
	 *        Access to the edited configuration item and property.
	 * @return The variable names; never <code>null</code>.
	 */
	List<String> getVariables(ValueModel valueModel);

}
```

- [ ] **Step 4: Create the annotation**

Create `ScriptContextVariables.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates an {@link com.top_logic.model.search.expr.config.dom.Expr} configuration property with a
 * {@link ScriptContextVariablesProvider} whose variables are offered at the top level of
 * <code>$</code>-completion.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ScriptContextVariables {

	/**
	 * The provider computing the context variable names.
	 */
	Class<? extends ScriptContextVariablesProvider> value();

}
```

- [ ] **Step 5: Create the resolver**

Create `ScriptContextVariablesResolver.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.form.values.edit.ValueModel;

/**
 * Resolves the {@link ScriptContextVariables} annotation of an edited property to the context
 * variable names provided by its {@link ScriptContextVariablesProvider}.
 */
public class ScriptContextVariablesResolver {

	/**
	 * The context variable names for the property represented by the given {@link ValueModel}.
	 *
	 * @param valueModel
	 *        The value model of the edited property, or <code>null</code>.
	 * @return The variable names (without <code>$</code>); empty if there is no value model, no
	 *         annotation, or the provider fails.
	 */
	public static List<String> resolve(ValueModel valueModel) {
		if (valueModel == null) {
			return Collections.emptyList();
		}
		PropertyDescriptor property = valueModel.getProperty();
		if (property == null) {
			return Collections.emptyList();
		}
		ScriptContextVariables annotation = property.getAnnotation(ScriptContextVariables.class);
		if (annotation == null) {
			return Collections.emptyList();
		}
		try {
			ScriptContextVariablesProvider provider = annotation.value().getConstructor().newInstance();
			List<String> variables = provider.getVariables(valueModel);
			return variables == null ? Collections.emptyList() : variables;
		} catch (Exception ex) {
			Logger.error("Failed to compute TL-Script context variables for '" + property.getPropertyName() + "'.",
				ex, ScriptContextVariablesResolver.class);
			return Collections.emptyList();
		}
	}

}
```

- [ ] **Step 6: Run the resolver test to verify it passes**

```bash
mvn -B test -DskipTests=false -pl com.top_logic.model.search \
  -Dtest=test.com.top_logic.model.search.ui.TestScriptContextVariablesResolver 2>&1 \
  | tee com.top_logic.model.search/target/test-resolver.log | grep -E "BUILD|Tests run"
```
Expected: all pass, `BUILD SUCCESS`.

- [ ] **Step 7: Wire the control**

In `TLScriptCodeEditorControl.java` add imports and a method. Add imports:
```java
import java.util.List;

import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.ValueModel;
```
Add this method (e.g. after the constructor):
```java
	/**
	 * The context variables (without the leading <code>$</code>) declared for the edited property via
	 * {@link ScriptContextVariables}, or an empty list if none.
	 */
	public List<String> getContextVariableNames() {
		ValueModel valueModel = EditorFactory.getValueModel(getFieldModel());
		return ScriptContextVariablesResolver.resolve(valueModel);
	}
```
(`getFieldModel()` is inherited from the form-field control base and returns this control's `FormField`.)

- [ ] **Step 8: Pass context variables from the command**

In `TLScriptAutoCompletionCommand.java`, change the `execute` completion call to pass the control's context variables. Replace:
```java
		List<CodeCompletion> completions =
			TLScriptCompletionService.computeCompletions(commandContext, line, prefix, textToCursor, _completeCaseSensitive);
```
with:
```java
		List<CodeCompletion> completions =
			TLScriptCompletionService.computeCompletions(commandContext, line, prefix, textToCursor,
				scriptCodeControl.getContextVariableNames(), _completeCaseSensitive);
```

- [ ] **Step 9: Rebuild the module and run the model.search ui tests**

```bash
mvn -B install -DskipTests=false -pl com.top_logic.model.search \
  -Dtest=test.com.top_logic.model.search.ui.TestTLScriptVariableScope,test.com.top_logic.model.search.ui.TestTLScriptAutoCompletionCommand,test.com.top_logic.model.search.ui.TestScriptContextVariablesResolver 2>&1 \
  | tee com.top_logic.model.search/target/mvn-task2.log | grep -E "BUILD|Tests run"
```
Expected: all pass, `BUILD SUCCESS`. (If the `install` fails only in the JavaDoc/message-generation phase due to unrelated upstream stale jars, run `.claude/scripts/rebuild-stale.sh` first, or verify with `mvn -B test-compile` + the test run above.)

- [ ] **Step 10: Show diff and commit (after user approval)**

```bash
git add com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/ScriptContextVariablesProvider.java \
        com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/ScriptContextVariables.java \
        com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/ScriptContextVariablesResolver.java \
        com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/TLScriptCodeEditorControl.java \
        com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/TLScriptAutoCompletionCommand.java \
        com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/ui/TestScriptContextVariablesResolver.java
git commit -m "Ticket #29389: Add SPI for TL-Script completion context variables."
```

---

## Task 3: OpenAPI provider (service.openapi.server)

**Files:**
- Modify: `com.top_logic.service.openapi.server/src/main/java/com/top_logic/service/openapi/server/impl/ServiceMethodBuilderByExpression.java`
- Create: `com.top_logic.service.openapi.server/src/main/java/com/top_logic/service/openapi/server/impl/OperationParameterVariables.java`
- Test: `com.top_logic.service.openapi.server/src/test/java/test/com/top_logic/service/openapi/server/impl/TestOperationParameterVariables.java`

**Interfaces:**
- Consumes: `ScriptContextVariablesProvider`, `@ScriptContextVariables`, `ValueModel`; `Operation.getParameters()`, `ConcreteRequestParameter.getScriptParameterNames()`.
- Produces: `OperationParameterVariables implements ScriptContextVariablesProvider`.

- [ ] **Step 1: Write the failing test**

Create `TestOperationParameterVariables.java`. It builds an `Operation` config with two parameters and a `ServiceMethodBuilderByExpression.Config` as its implementation, then asserts the provider returns the parameter names. (The provider reads the operation from the impl config's `@Container`.)

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.server.impl;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.service.openapi.server.conf.OperationByMethod;
import com.top_logic.service.openapi.server.impl.OperationParameterVariables;
import com.top_logic.service.openapi.server.impl.ServiceMethodBuilderByExpression;
import com.top_logic.service.openapi.server.parameter.PathParameter;

/**
 * Test for {@link OperationParameterVariables}.
 */
public class TestOperationParameterVariables extends BasicTestCase {

	public void testReturnsOperationParameterNames() {
		OperationByMethod operation = TypedConfiguration.newConfigItem(OperationByMethod.class);
		operation.getParameters().add(pathParam("id"));
		operation.getParameters().add(pathParam("name"));

		ServiceMethodBuilderByExpression.Config impl =
			TypedConfiguration.newConfigItem(ServiceMethodBuilderByExpression.Config.class);
		operation.setImplementation(impl);

		List<String> variables =
			new OperationParameterVariables().getVariablesFromModel(impl);

		assertTrue(variables.contains("id"));
		assertTrue(variables.contains("name"));
	}

	private static PathParameter.Config pathParam(String name) {
		PathParameter.Config config = TypedConfiguration.newConfigItem(PathParameter.Config.class);
		config.setName(name);
		return config;
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestOperationParameterVariables.class));
	}
}
```

Note: this test calls a package-visible helper `getVariablesFromModel(ConfigurationItem)` so the provider is testable without constructing a `ValueModel`. Confirm the exact parameter config type/`setName` and the `setImplementation` setter name when compiling — adjust to the actual API (`Operation.setImplementation(...)`, and the concrete path-parameter config class; if `PathParameter.Config` differs, use the correct `ConcreteRequestParameter` subtype whose `getScriptParameterNames()` returns its `getName()`).

- [ ] **Step 2: Run to verify it fails**

```bash
cd /home/dbu/Development/workspaces/TL_trunk/git/tl-engine
mvn -B test -DskipTests=false -pl com.top_logic.service.openapi.server \
  -Dtest=test.com.top_logic.service.openapi.server.impl.TestOperationParameterVariables 2>&1 \
  | tee com.top_logic.service.openapi.server/target/test-provider.log | grep -E "BUILD|ERROR|cannot find symbol"
```
Expected: compilation failure — `OperationParameterVariables` does not exist.

- [ ] **Step 3: Make the impl Config a ConfigPart with container access**

In `ServiceMethodBuilderByExpression.java`, change the `Config` interface to also extend `ConfigPart` and add a `@Container` accessor to the owning `Operation`. Add imports:
```java
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.service.openapi.server.conf.Operation;
```
Change the interface declaration:
```java
	public interface Config extends PolymorphicConfiguration<ServiceMethodBuilderByExpression>, ConfigPart {
```
Add inside `Config` (e.g. after `TRANSACTION`):
```java
		/**
		 * The {@link Operation} this implementation belongs to.
		 */
		@Container
		Operation getOwner();
```
Annotate `getOperation()` (add above the existing `@Name(OPERATION)` line):
```java
		@ScriptContextVariables(OperationParameterVariables.class)
```

- [ ] **Step 4: Create the provider**

Create `OperationParameterVariables.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.impl;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.model.search.ui.ScriptContextVariablesProvider;
import com.top_logic.service.openapi.server.conf.Operation;
import com.top_logic.service.openapi.server.parameter.RequestParameter;

/**
 * {@link ScriptContextVariablesProvider} exposing the parameters of the enclosing
 * {@link Operation} as top-level variables for the operation script of
 * {@link ServiceMethodBuilderByExpression}.
 */
public class OperationParameterVariables implements ScriptContextVariablesProvider {

	@Override
	public List<String> getVariables(ValueModel valueModel) {
		return getVariablesFromModel(valueModel.getModel());
	}

	/**
	 * The parameter names of the {@link Operation} owning the given implementation config.
	 *
	 * @param model
	 *        The {@link ServiceMethodBuilderByExpression.Config} being edited.
	 */
	List<String> getVariablesFromModel(ConfigurationItem model) {
		List<String> result = new ArrayList<>();
		if (model instanceof ServiceMethodBuilderByExpression.Config) {
			Operation operation = ((ServiceMethodBuilderByExpression.Config) model).getOwner();
			if (operation != null) {
				for (RequestParameter.Config<?> parameter : operation.getParameters()) {
					result.addAll(parameter.getScriptParameterNames());
				}
			}
		}
		return result;
	}

}
```

Note: `RequestParameter.Config` is the configured-parameter type held by `Operation.getParameters()`. If `getScriptParameterNames()` is defined on the runtime `ConcreteRequestParameter` rather than on the config, instead read `parameter.getName()` from the config (which is what path/query parameters contribute). Verify against `Operation.getParameters()`'s element type and use the method that yields the script names at config level (`getName()` for concrete parameters); keep the guard so unknown parameter kinds contribute nothing.

- [ ] **Step 5: Run the provider test to verify it passes**

```bash
mvn -B test -DskipTests=false -pl com.top_logic.service.openapi.server \
  -Dtest=test.com.top_logic.service.openapi.server.impl.TestOperationParameterVariables 2>&1 \
  | tee com.top_logic.service.openapi.server/target/test-provider.log | grep -E "BUILD|Tests run"
```
Expected: pass, `BUILD SUCCESS`. (If `install` of the dependency `com.top_logic.model.search` is needed first for the new SPI types, run `mvn -B install -DskipTests=true -pl com.top_logic.model.search` — after Task 2 is committed — then retry.)

- [ ] **Step 6: Manual verification (Playwright)**

Reach the OpenAPI server configuration in `com.top_logic.demo` (or a test app with an OpenAPI server), open an operation whose implementation is "method-by-expression", declare a path/query parameter (e.g. `id`), and in the operation script editor type `$`. Confirm the parameter (`$id`) is offered at the top level, and that model-element (`` ` ``) and function (`.`) completion still work. Record the observation.

- [ ] **Step 7: Show diff and commit (after user approval)**

```bash
git add com.top_logic.service.openapi.server/src/main/java/com/top_logic/service/openapi/server/impl/ServiceMethodBuilderByExpression.java \
        com.top_logic.service.openapi.server/src/main/java/com/top_logic/service/openapi/server/impl/OperationParameterVariables.java \
        com.top_logic.service.openapi.server/src/test/java/test/com/top_logic/service/openapi/server/impl/TestOperationParameterVariables.java
git commit -m "Ticket #29389: Expose OpenAPI operation parameters to TL-Script completion."
```

---

## Self-Review Notes

- **Spec coverage:** generic SPI (Task 2) ✓; annotation on `getOperation()` + OpenAPI provider (Task 3) ✓; always-in-scope union + prefix filter + dedup (Task 1) ✓; server-side only, no client change (command reads control) ✓; never-break degradation (resolver + provider guards, empty fallbacks) ✓; no validation change / `PlainEditor` untouched ✓; tests (Tasks 1–3) + Playwright ✓.
- **Open verification points flagged for the implementer (not placeholders — concrete fallbacks given):** exact `ValueModel` method set (Task 2 Step 1 note); the concrete parameter config type and the config-level method yielding script names (Task 3 Step 1 & Step 4 notes). Both have explicit "verify against actual API and adjust" instructions with the correct fallback.
- **Type consistency:** `matchingVariables(text, contextVariables, prefix, caseSensitive)` and `computeCompletions(..., contextVariables, ...)` used consistently; `getContextVariableNames()` → command → service; `ScriptContextVariablesProvider.getVariables(ValueModel)` and `@ScriptContextVariables(value)` consistent across tasks.
- **Known limitation (documented in spec):** global/path-item parameter references beyond the operation's own `getParameters()` are not resolved in v1; the provider returns the operation's declared parameters. Extendable later via the container chain (`Operation` → path item → server config).
