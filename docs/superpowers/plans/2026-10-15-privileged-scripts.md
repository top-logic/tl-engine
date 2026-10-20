# Privileged TL-Script Functions Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** A configured TL-Script function of the new type `privileged-script` is authorized as a whole by one command group check on a security object computed from its arguments, runs unsecured, and delivers a Discover-filtered result; `canCall(call)` reports whether such a call is allowed.

**Architecture:** `PrivilegedScript` extends `ConfiguredScript` with the mandatory properties `security-object` and `command-group` and the optional `on-denied`. `ConfiguredTLScriptFunctions` gets one entry point `call(...)` used by both call expressions (`QueryExecutorMethod`, `TracingScriptMethod`): for a secured call of a privileged script it checks, runs the unsecured executor and filters the result; every other call is unchanged. `CanCall` is a `GenericMethod` whose argument is a configured function call; it evaluates only the call's arguments and asks the service.

**Tech Stack:** Java 17, TopLogic TypedConfiguration, TL-Script (`com.top_logic.model.search`), model based security (`ModelAccessRights`, `CommandGroupRegistry`), JUnit 3/4 style `BasicTestCase` / `AbstractSearchExpressionTest`, Maven.

**Spec:** `com.top_logic/src/main/java/META-INF/tl-docs/access-privileged-scripts.md` (ticket #29858). Background: `com.top_logic/src/main/java/META-INF/tl-docs/access-discover-spec.md`.

## Global Constraints

- Ticket: #29858; commit messages `Ticket #29858: <description>.`; branch `CWS/CWS_29858_privileged_scripts`.
- **Every commit needs the user's explicit approval**: show `git status` / `git diff --stat` and a summary first, then wait. Run `git commit` as a standalone command (not chained with `&&`).
- Java files are ISO-8859-1: **ASCII only** in `.java` files (no umlauts, no typographic quotes); every new `.java` file gets the SPDX header and a class JavaDoc.
- Member variables start with `_`.
- Reference symbols in JavaDoc with `{@link}`, not `{@code}`.
- Build from the project root with `-pl`, never `cd` into a module, never `-am`, always `mvn -B ... 2>&1 | tee <module>/target/mvn-build.log`.
- New `I18NConstants` keys make `mvn install` call DeepL: run that build **unsandboxed**; never pass `-Dmaven.javadoc.skip=true`; check / fix the German entry in `messages_de.properties` after the build.
- Run a single test with its fully qualified class name: `mvn -B test -DskipTests=false -pl com.top_logic.model.search -Dtest=test.com.top_logic.model.search.configured.TestPrivilegedScript`.
- A `configured-script` keeps its exact current behaviour; no migration.
- XML views edited in the demo must be normalized (`mvn exec:java@normalize-layouts` covers layout dirs; keep the indentation style of the surrounding file for `.view.xml`).

## Review Focus

1. **Security object expression fails on a `null` argument** (an argument the user may not discover arrives as `null`): expected is a denied call, not an internal error. Test `testNullArgumentDenies` in Task 2.
2. **Security object is a collection** (e.g. `$p.get(members)`): expected is a denied call (no `TLObject`), never "allowed if one element allows". Test `testCollectionSecurityObjectDenies` in Task 2.
3. **Optional parameter with default value** omitted in the call: the security object and the implementation see the default. Test `testDefaultParameterReachesSecurityObject` in Task 2.
4. **`on-denied` calls `throw(...)`**: the message of the script reaches the caller unchanged. Test `testOnDeniedThrow` in Task 2.
5. **`canCall` with an argument that is no configured function call** (`canCall(true)`, `canCall(canExecute($p, 'Read'))`): a compile error, not `true`. Test `testCanCallRejectsNonCall` in Task 3.

---

## File Structure

| File | Responsibility |
|---|---|
| Create `com.top_logic.model.search/src/main/java/com/top_logic/model/search/configured/PrivilegedScript.java` | Config of the new type; resolves the command group; decides `isAllowed(args)`; produces the denied result. |
| Modify `.../configured/ConfiguredScript.java` | Expose the parameter wrapping of an expression (`withParameters(Expr)`) to the subclass. |
| Modify `.../configured/ConfiguredTLScriptFunctions.java` | `@ServiceDependencies(CommandGroupRegistry)`; `call(...)` and `isCallAllowed(...)`. |
| Modify `.../configured/QueryExecutorMethod.java`, `.../configured/TracingScriptMethod.java` | Delegate evaluation to `ConfiguredTLScriptFunctions.call(...)`. |
| Modify `.../configured/I18NConstants.java` | `ERROR_CALL_NOT_ALLOWED__NAME`, `ERROR_UNKNOWN_COMMAND_GROUP__GROUP_NAME`, `ERROR_CAN_CALL_NO_CALL__EXPR`. |
| Create `.../configured/CanCall.java` | The `canCall` TL-Script function. |
| Modify `com.top_logic.model.search/src/main/webapp/WEB-INF/conf/modelSearchConf.config.xml` | Register `canCall`. |
| Create `com.top_logic.model.search/src/main/webapp/doc/{en,de}/DeveloperGuide/TLScript/ModelOperations/canCall/{index.html,page.properties}` | Help pages. |
| Modify `com.top_logic.model.search/deploy/local/webapp/WEB-INF/conf/TestTLScriptSecurity-test.config.xml` | Test fixture: command group `Approve`, its grant, the test scripts. |
| Create `com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/configured/TestPrivilegedScriptConfig.java` | Configuration errors. |
| Create `com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/configured/TestPrivilegedScript.java` | Call semantics and `canCall`. |
| Modify `com.top_logic/src/main/java/META-INF/tl-docs/access-privileged-scripts.md`, `access-discover-spec.md` | Status, R12 note. |
| Modify `com.top_logic.demo.react/src/main/webapp/WEB-INF/conf/demoReactConf.config.xml`, `.../views/projects/milestone-list.view.xml` | Demo scenario. |

---

### Task 1: `PrivilegedScript` configuration and instantiation

**Files:**
- Create: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/configured/PrivilegedScript.java`
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/configured/ConfiguredScript.java:87-98` (`createBaseExpression`)
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/configured/ConfiguredTLScriptFunctions.java:53-56` (class annotations)
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/configured/I18NConstants.java`
- Test: `com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/configured/TestPrivilegedScriptConfig.java`

**Interfaces:**
- Produces: `PrivilegedScript.Config` with `getSecurityObject(): Expr` (`security-object`, mandatory), `getCommandGroup(): CommandGroupReference` (`command-group`, mandatory), `getOnDenied(): Expr` (`on-denied`, nullable); tag `privileged-script`.
- Produces: `ConfiguredScript#withParameters(Expr body): Expr` (protected) - wraps `body` into one `Define` per parameter, exactly as the implementation is wrapped today.
- Produces: `PrivilegedScript#getCommandGroup(): BoundCommandGroup` (resolved, never `null` after successful instantiation).

- [ ] **Step 1: Write the failing test**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.configured;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.model.search.configured.ConfiguredScript;
import com.top_logic.model.search.configured.PrivilegedScript;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Tests the configuration of a {@link PrivilegedScript} (ticket #29858).
 */
@SuppressWarnings("javadoc")
public class TestPrivilegedScriptConfig extends BasicTestCase {

	public void testReadsProperties() throws ConfigurationException {
		PrivilegedScript.Config config = (PrivilegedScript.Config) read(new BufferingProtocol(), """
			<privileged-script name="approve" security-object="$p" command-group="Write" on-denied="null">
				<parameters><parameter name="p"/></parameters>
				<implementation>$p</implementation>
			</privileged-script>
			""");
		assertEquals("Write", config.getCommandGroup().id());
		assertNotNull(config.getSecurityObject());
		assertNotNull(config.getOnDenied());
	}

	public void testSecurityObjectIsMandatory() {
		assertRejected("getSecurityObject()", """
			<privileged-script name="approve" command-group="Write">
				<implementation>null</implementation>
			</privileged-script>
			""");
	}

	public void testCommandGroupIsMandatory() {
		assertRejected("getCommandGroup()", """
			<privileged-script name="approve" security-object="null">
				<implementation>null</implementation>
			</privileged-script>
			""");
	}

	public void testUnknownCommandGroupIsRejected() throws ConfigurationException {
		Protocol log = new BufferingProtocol();
		PrivilegedScript.Config config = (PrivilegedScript.Config) read(log, """
			<privileged-script name="approve" security-object="null" command-group="NoSuchGroup">
				<implementation>null</implementation>
			</privileged-script>
			""");
		DefaultInstantiationContext context = new DefaultInstantiationContext(log);
		context.getInstance(config);
		try {
			context.checkErrors();
		} catch (ConfigurationException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("NoSuchGroup"));
			return;
		}
		fail("An unknown command group must be rejected.");
	}

	public void testResolvesCommandGroup() throws ConfigurationException {
		Protocol log = new BufferingProtocol();
		PrivilegedScript.Config config = (PrivilegedScript.Config) read(log, """
			<privileged-script name="approve" security-object="null" command-group="Write">
				<implementation>null</implementation>
			</privileged-script>
			""");
		PrivilegedScript script =
			(PrivilegedScript) new DefaultInstantiationContext(log).getInstance(config);
		log.checkErrors();
		assertSame(SimpleBoundCommandGroup.WRITE, script.getCommandGroup());
	}

	private void assertRejected(String property, String xml) {
		try {
			Protocol log = new BufferingProtocol();
			read(log, xml);
			log.checkErrors();
		} catch (ConfigurationException | RuntimeException ex) {
			assertTrue("The missing property is reported: " + ex.getMessage(),
				String.valueOf(ex.getMessage()).contains(property));
			return;
		}
		fail("Missing " + property + " must be rejected.");
	}

	private static Object read(Protocol log, String xml) throws ConfigurationException {
		Map<String, ConfigurationDescriptor> descriptors =
			Collections.singletonMap(PrivilegedScript.Config.TAG_NAME,
				TypedConfiguration.getConfigurationDescriptor(PrivilegedScript.Config.class));
		return new ConfigurationReader(new DefaultInstantiationContext(log), descriptors)
			.setSource(CharacterContents.newContent(xml))
			.read();
	}

	public static Test suite() {
		return ServiceTestSetup.createSetup(TestPrivilegedScriptConfig.class, CommandGroupRegistry.Module.INSTANCE);
	}
}
```

Note for the implementer: the reader resolves the root element `privileged-script` by its tag against `descriptors`. Remove imports the final code does not use (`ConfiguredScript`). If `ServiceTestSetup.createSetup(Class, BasicRuntimeModule...)` has a different signature, copy the suite setup of another `BasicTestCase` in `com.top_logic.model.search/src/test` that starts a single service module.

- [ ] **Step 2: Run the test to verify it fails**

Run: `mvn -B test -DskipTests=false -pl com.top_logic.model.search -Dtest=test.com.top_logic.model.search.configured.TestPrivilegedScriptConfig 2>&1 | tee com.top_logic.model.search/target/mvn-build.log`
Expected: compilation FAILS, `cannot find symbol: class PrivilegedScript`.

- [ ] **Step 3: Expose the parameter wrapping in `ConfiguredScript`**

Replace `createBaseExpression()` (lines 87-98) by:

```java
	private Expr createBaseExpression() {
		return withParameters(getConfig().getImplementation());
	}

	/**
	 * Wraps the given expression into a function of the configured
	 * {@link ScriptConfiguration#getParameters() parameters}, so that it is executed with the
	 * arguments of a call in the same way as the {@link ScriptConfiguration#getImplementation()
	 * implementation}.
	 */
	protected Expr withParameters(Expr body) {
		Expr expr = body;

		// Note: The last argument is passed to the innermost function. Therefore, the lambdas must
		// be constructed in reverse order.
		List<ScriptParameter> parameters = getConfig().getParameters();
		for (int n = parameters.size() - 1; n >= 0; n--) {
			expr = Define.create(parameters.get(n).getName(), expr);
		}
		return expr;
	}
```

- [ ] **Step 4: Add the I18N constant for an unknown command group**

In `configured/I18NConstants.java`, next to `ERROR_NO_SUCH_SCRIPT__NAME`:

```java
	/**
	 * @en The command group "{0}" of the privileged script "{1}" does not exist.
	 */
	public static ResKey2 ERROR_UNKNOWN_COMMAND_GROUP__GROUP_NAME;
```

(import `com.top_logic.basic.util.ResKey2` if not present.)

- [ ] **Step 5: Write `PrivilegedScript` (configuration and command group)**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.configured;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandGroupReference;

/**
 * {@link ConfiguredScript} that is authorized as a whole.
 *
 * <p>
 * A call from a secured evaluation checks the {@link Config#getCommandGroup() command group} once
 * on the {@link Config#getSecurityObject() security object} computed from the arguments, runs the
 * implementation without security, and filters the result for the Discover right. A call from an
 * unsecured evaluation is not checked. See the developer article
 * {@code access-privileged-scripts}.
 * </p>
 *
 * @see ConfiguredTLScriptFunctions#call(String, boolean, Object[], java.util.function.Function)
 */
public class PrivilegedScript extends ConfiguredScript {

	/**
	 * Configuration of a {@link PrivilegedScript}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends ConfiguredScript.Config {

		/** Tag name of a {@link PrivilegedScript} in the configured scripts. */
		String TAG_NAME = "privileged-script";

		/** Configuration name of {@link #getSecurityObject()}. */
		String SECURITY_OBJECT = "security-object";

		/** Configuration name of {@link #getCommandGroup()}. */
		String COMMAND_GROUP = "command-group";

		/** Configuration name of {@link #getOnDenied()}. */
		String ON_DENIED = "on-denied";

		@Override
		@ClassDefault(PrivilegedScript.class)
		Class<? extends ConfiguredScript> getImplementationClass();

		/**
		 * The object on which the {@link #getCommandGroup() command group} is checked.
		 *
		 * <p>
		 * The expression accesses the {@link #getParameters() parameters} as variables. It is
		 * evaluated without security; the arguments of a secured call contain only objects the user
		 * may discover. A result that is no single object denies the call.
		 * </p>
		 */
		@Mandatory
		@PropertyEditor(PlainEditor.class)
		@Constraint(value = CheckExprWithAdditionalParams.class, args = { @Ref(PARAMETERS) })
		@ScriptContextVariables(ScriptParameterVariables.class)
		@Name(SECURITY_OBJECT)
		Expr getSecurityObject();

		/**
		 * The operation the current user must hold on the {@link #getSecurityObject() security
		 * object} to call the function.
		 */
		@Mandatory
		@Name(COMMAND_GROUP)
		CommandGroupReference getCommandGroup();

		/**
		 * The result of a call the current user is not allowed to make.
		 *
		 * <p>
		 * The expression accesses the {@link #getParameters() parameters} as variables and is
		 * evaluated with the security of the user. It may deliver a replacement value (e.g.
		 * <code>null</code>) or fail with a message of its own (<code>throw(...)</code>). Without
		 * it, a denied call fails with a generic message.
		 * </p>
		 */
		@Nullable
		@PropertyEditor(PlainEditor.class)
		@Constraint(value = CheckExprWithAdditionalParams.class, args = { @Ref(PARAMETERS) })
		@ScriptContextVariables(ScriptParameterVariables.class)
		@Name(ON_DENIED)
		Expr getOnDenied();

	}

	private final BoundCommandGroup _commandGroup;

	/**
	 * Creates a {@link PrivilegedScript}.
	 */
	public PrivilegedScript(InstantiationContext context, Config config) {
		super(context, config);
		_commandGroup = config.getCommandGroup().resolve();
		if (_commandGroup == null) {
			context.error(I18NConstants.ERROR_UNKNOWN_COMMAND_GROUP__GROUP_NAME
				.fill(config.getCommandGroup().id(), config.getName()).toString());
		}
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	/**
	 * The resolved {@link Config#getCommandGroup() command group}.
	 */
	public BoundCommandGroup getCommandGroup() {
		return _commandGroup;
	}

}
```

Copy the imports of `PlainEditor`, `CheckExprWithAdditionalParams`, `ScriptContextVariables` exactly from `ScriptConfiguration.java` (same annotations as `getImplementation()`). If `context.error` has an overload taking a `ResKey`, use that instead of `.toString()`. If `getConfig()` in `AbstractConfiguredInstance` is final, remove the override and cast at the use sites.

- [ ] **Step 6: Make the service depend on the command group registry**

In `ConfiguredTLScriptFunctions.java` add above `@ServiceExtensionPoint(SearchBuilder.Module.class)`:

```java
@ServiceDependencies({
	CommandGroupRegistry.Module.class
})
```

(imports `com.top_logic.basic.module.services.ServiceDependencies` - copy the exact import from `SecurityConfigurationService.java` - and `com.top_logic.tool.boundsec.simple.CommandGroupRegistry`.)

- [ ] **Step 7: Run the test to verify it passes**

Run: `mvn -B install -pl com.top_logic.model.search 2>&1 | tee com.top_logic.model.search/target/mvn-build.log` (**unsandboxed**, new I18N key), then the test command of Step 2.
Expected: `Tests run: 5, Failures: 0, Errors: 0`. Check `messages_de.properties` for the new key and correct the DeepL German if needed.

- [ ] **Step 8: Commit (after the user approved the shown diff)**

```bash
git add com.top_logic.model.search/src/main/java/com/top_logic/model/search/configured/ com.top_logic.model.search/src/main/java/META-INF/messages_*.properties com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/configured/TestPrivilegedScriptConfig.java
git commit -m "Ticket #29858: Add the configuration of a privileged script."
```

---

### Task 2: Call semantics of a privileged script

**Files:**
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/configured/PrivilegedScript.java`
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/configured/ConfiguredTLScriptFunctions.java`
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/configured/QueryExecutorMethod.java:110-113` (`eval`)
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/configured/TracingScriptMethod.java:48-51` (`eval`)
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/configured/I18NConstants.java`
- Modify: `com.top_logic.model.search/deploy/local/webapp/WEB-INF/conf/TestTLScriptSecurity-test.config.xml`
- Test: `com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/configured/TestPrivilegedScript.java`

**Interfaces:**
- Consumes: `PrivilegedScript.Config`, `ConfiguredScript#withParameters(Expr)`, `PrivilegedScript#getCommandGroup()` (Task 1).
- Produces: `PrivilegedScript#isAllowed(Object[] arguments): boolean`, `PrivilegedScript#denied(Object[] arguments): Object`.
- Produces: `ConfiguredTLScriptFunctions#call(String scriptName, boolean usesSecurity, Object[] arguments, Function<Boolean, Object> execution): Object` - `execution.apply(secured)` runs the function body in the given security mode.
- Produces: `ConfiguredTLScriptFunctions#isCallAllowed(String scriptName, Object[] arguments): boolean`.

- [ ] **Step 1: Add the test fixture to the test configuration**

In `TestTLScriptSecurity-test.config.xml`, inside `<services>`:

```xml
		<config service-class="com.top_logic.tool.boundsec.simple.CommandGroupRegistry">
			<instance class="com.top_logic.tool.boundsec.simple.CommandGroupRegistry">
				<group name="Approve" type="write"/>
			</instance>
		</config>

		<config service-class="com.top_logic.model.search.configured.ConfiguredTLScriptFunctions">
			<instance class="com.top_logic.model.search.configured.ConfiguredTLScriptFunctions">
				<scripts>
					<!-- Writes the name of a project, which a ProjectReader may not write directly. -->
					<privileged-script name="testPrivilegedRename"
						command-group="Approve"
						security-object="$project"
					>
						<parameters>
							<parameter name="project"/>
							<parameter name="name"/>
						</parameters>
						<implementation>$project.set(`TestTLScriptSecurity:Project#name`, $name)</implementation>
					</privileged-script>
					<!-- Delivers all projects and their unfiltered number. -->
					<privileged-script name="testPrivilegedAll"
						command-group="Approve"
						security-object="$project"
					>
						<parameters>
							<parameter name="project"/>
						</parameters>
						<implementation>all(`TestTLScriptSecurity:Project`)</implementation>
					</privileged-script>
					<privileged-script name="testPrivilegedCount"
						command-group="Approve"
						security-object="$project"
					>
						<parameters>
							<parameter name="project"/>
						</parameters>
						<implementation>all(`TestTLScriptSecurity:Project`).size()</implementation>
					</privileged-script>
					<!-- Denies every secured call: the security object is always null. -->
					<privileged-script name="testPrivilegedNever"
						command-group="Approve"
						security-object="null"
					>
						<parameters>
							<parameter name="project"/>
						</parameters>
						<implementation>$project.get(`TestTLScriptSecurity:Project#name`)</implementation>
					</privileged-script>
					<!-- Calls a privileged script that denies every secured call. -->
					<privileged-script name="testPrivilegedOuter"
						command-group="Approve"
						security-object="$project"
					>
						<parameters>
							<parameter name="project"/>
						</parameters>
						<implementation>testPrivilegedNever($project)</implementation>
					</privileged-script>
					<privileged-script name="testPrivilegedOnDeniedValue"
						command-group="Approve"
						security-object="$project"
						on-denied="'denied:' + $project.get(`TestTLScriptSecurity:Project#name`)"
					>
						<parameters>
							<parameter name="project"/>
						</parameters>
						<implementation>'allowed'</implementation>
					</privileged-script>
					<privileged-script name="testPrivilegedOnDeniedThrow"
						command-group="Approve"
						security-object="$project"
						on-denied="throw('custom denial')"
					>
						<parameters>
							<parameter name="project"/>
						</parameters>
						<implementation>'allowed'</implementation>
					</privileged-script>
					<privileged-script name="testPrivilegedCollection"
						command-group="Approve"
						security-object="list($project, $project)"
					>
						<parameters>
							<parameter name="project"/>
						</parameters>
						<implementation>'allowed'</implementation>
					</privileged-script>
					<privileged-script name="testPrivilegedDefault"
						command-group="Approve"
						security-object="$target"
					>
						<parameters>
							<parameter name="project"/>
							<parameter name="target">
								<default-value>$project</default-value>
							</parameter>
						</parameters>
						<implementation>$target</implementation>
					</privileged-script>
					<configured-script name="testPlainScript">
						<parameters>
							<parameter name="project"/>
						</parameters>
						<implementation>$project.get(`TestTLScriptSecurity:Project#name`)</implementation>
					</configured-script>
				</scripts>
			</instance>
		</config>
```

and in the `TestTLScriptSecurity:Project` class of `<security-config>` add:

```xml
						<!-- The reader (a member of a project) may perform the privileged operations. -->
						<grant
							inherit="true"
							operation="Approve"
							roles="TestTLScriptSecurity.ProjectReader"
						/>
```

Check the exact syntax of `<default-value>` and of `<group .../>` against `ScriptParameter.java` and `CommandGroupRegistry.Config`; adapt the XML, not the semantics. `default-value` in `ScriptParameter`: the default is evaluated without access to other parameters - if `$project` is not visible there, use a parameter-free default and adapt `testDefaultParameterReachesSecurityObject` to assert on that default instead.

- [ ] **Step 2: Write the failing test**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.configured;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.person.TestPerson;
import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.service.db2.ObjectKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.configured.ConfiguredTLScriptFunctions;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.simple.TLSecurityDeviceManager;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests privileged TL-Script functions (ticket #29858) on the projects of the model
 * {@code TestTLScriptSecurity}.
 *
 * <p>
 * Fixture: the approver is a member of {@code p1} and therefore holds ProjectReader (read, and
 * Approve by the test configuration) on it, but no write right. The auditor reads {@code p1} as
 * ProjectAuditor, but holds no Approve. Nobody but root holds a role on {@code p2}.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestPrivilegedScript extends AbstractSearchExpressionTest {

	private KnowledgeBase _kb;

	private final Map<ObjectKey, KnowledgeItem> _toDelete = new HashMap<>();

	private final UpdateListener _creationListener = new UpdateListener() {
		@Override
		public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
			_toDelete.putAll(event.getCreatedObjects());
			_toDelete.keySet().removeAll(event.getDeletedObjectKeys());
		}
	};

	Person _approver;

	Person _auditor;

	TLObject _p1, _p2;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_kb = PersistencyLayer.getKnowledgeBase();
		_kb.addUpdateListener(_creationListener);
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_approver = TestPerson.createPerson("privilegedApprover");
			_auditor = TestPerson.createPerson("privilegedAuditor");

			TLObject member = newObject("Employee");
			member.tUpdateByName("account", _approver);

			_p1 = newObject("Project");
			_p1.tUpdateByName("name", "p1");
			_p1.tUpdateByName("members", Collections.singletonList(member));
			_p1.tUpdateByName("auditorAccount", _auditor);

			_p2 = newObject("Project");
			_p2.tUpdateByName("name", "p2");
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		becomeUser(PersonManager.getManager().getRoot());
		_kb.removeUpdateListener(_creationListener);
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			for (KnowledgeItem item : _toDelete.values()) {
				if (item.isAlive()) {
					_kb.delete(item);
				}
			}
			tx.commit();
		}
		_toDelete.clear();
		_kb = null;
		super.tearDown();
	}

	private TLObject newObject(String className) {
		TLClass type = (TLClass) TLModelUtil.findType("TestTLScriptSecurity:" + className);
		return DynamicModelService.getFactoryFor("TestTLScriptSecurity").createObject(type);
	}

	private Object inTransaction(String script, Object... args) throws Exception {
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			Object result = execute(search(script), args);
			tx.commit();
			return result;
		}
	}

	public void testDirectWriteIsDenied() throws Exception {
		becomeUser(_approver);
		try {
			inTransaction("p -> $p.set(`TestTLScriptSecurity:Project#name`, 'direct')", _p1);
			fail("The approver holds no write right on the project.");
		} catch (TopLogicException ex) {
			// expected
		}
	}

	public void testAllowedCallRunsUnsecured() throws Exception {
		becomeUser(_approver);
		inTransaction("p -> testPrivilegedRename($p, 'renamed')", _p1);
		assertEquals("renamed", _p1.tValueByName("name"));
	}

	public void testDeniedCallWithoutOnDeniedThrows() throws Exception {
		becomeUser(_auditor);
		try {
			inTransaction("p -> testPrivilegedRename($p, 'renamed')", _p1);
			fail("The auditor holds no Approve right.");
		} catch (TopLogicException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("testPrivilegedRename"));
		}
		assertEquals("p1", _p1.tValueByName("name"));
	}

	public void testOnDeniedValueIsEvaluatedSecured() throws Exception {
		becomeUser(_auditor);
		assertEquals("denied:p1", execute(search("p -> testPrivilegedOnDeniedValue($p)"), _p1));
		becomeUser(_approver);
		assertEquals("allowed", execute(search("p -> testPrivilegedOnDeniedValue($p)"), _p1));
	}

	public void testOnDeniedThrow() throws Exception {
		becomeUser(_auditor);
		try {
			execute(search("p -> testPrivilegedOnDeniedThrow($p)"), _p1);
			fail("on-denied throws.");
		} catch (RuntimeException ex) {
			assertTrue(String.valueOf(ex.getMessage()), String.valueOf(ex.getMessage()).contains("custom denial"));
		}
	}

	public void testNullSecurityObjectDenies() throws Exception {
		becomeUser(_approver);
		try {
			execute(search("p -> testPrivilegedNever($p)"), _p1);
			fail("A null security object denies the call.");
		} catch (TopLogicException ex) {
			// expected
		}
	}

	public void testNullArgumentDenies() throws Exception {
		// p2 is not discoverable for the approver: the argument arrives as null.
		becomeUser(_approver);
		try {
			execute(search("p -> testPrivilegedRename($p, 'x')"), _p2);
			fail("An undiscoverable argument denies the call.");
		} catch (TopLogicException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("testPrivilegedRename"));
		}
	}

	public void testCollectionSecurityObjectDenies() throws Exception {
		becomeUser(_approver);
		try {
			execute(search("p -> testPrivilegedCollection($p)"), _p1);
			fail("A collection is no security object.");
		} catch (TopLogicException ex) {
			// expected
		}
	}

	public void testDefaultParameterReachesSecurityObject() throws Exception {
		becomeUser(_approver);
		assertEquals(_p1, execute(search("p -> testPrivilegedDefault($p)"), _p1));
	}

	public void testResultIsFilteredForDiscover() throws Exception {
		becomeUser(_approver);
		Object projects = execute(search("p -> testPrivilegedAll($p)"), _p1);
		assertTrue(asSet(projects).contains(_p1));
		assertFalse("p2 is not discoverable for the approver.", asSet(projects).contains(_p2));
	}

	public void testComputedValueIsNotFiltered() throws Exception {
		becomeUser(PersonManager.getManager().getRoot());
		Object all = execute(search("all(`TestTLScriptSecurity:Project`).size()"));
		becomeUser(_approver);
		assertEquals(all, execute(search("p -> testPrivilegedCount($p)"), _p1));
	}

	public void testUnsecuredCallIsNotChecked() throws Exception {
		becomeUser(_approver);
		QueryExecutor executor = QueryExecutor.compile(search("p -> testPrivilegedNever($p)"));
		executor.disableSecurity();
		assertEquals("p1", executor.execute(_p1));
	}

	public void testNestedPrivilegedScriptIsNotCheckedAgain() throws Exception {
		becomeUser(_approver);
		assertEquals("p1", execute(search("p -> testPrivilegedOuter($p)"), _p1));
	}

	public void testPlainScriptKeepsSecurity() throws Exception {
		becomeUser(_auditor);
		assertEquals("p1", execute(search("p -> testPlainScript($p)"), _p1));
		becomeUser(_approver);
		assertEquals(null, execute(search("p -> testPlainScript($p)"), _p2));
	}

	public static Test suite() {
		return suite(TestPrivilegedScript.class,
			AccessManager.Module.INSTANCE,
			TLSecurityDeviceManager.Module.INSTANCE,
			PersonManager.Module.INSTANCE,
			InitialRolesManager.Module.INSTANCE,
			CommandGroupRegistry.Module.INSTANCE,
			SecurityConfigurationService.Module.INSTANCE,
			ConfiguredTLScriptFunctions.Module.INSTANCE);
	}

}
```

Copy the imports that do not resolve (e.g. `ObjectKey`, `DynamicModelService`, `TestPerson`) exactly from `test/com/top_logic/model/search/expr/TestDiscoverRight.java`, which uses the same helpers. Remove unused imports. If `ConfiguredTLScriptFunctions.Module` is not public, drop it from the list (it is started as a dependency of `SearchBuilder.Module`).

- [ ] **Step 3: Run the test to verify it fails**

Run: `mvn -B test -DskipTests=false -pl com.top_logic.model.search -Dtest=test.com.top_logic.model.search.configured.TestPrivilegedScript 2>&1 | tee com.top_logic.model.search/target/mvn-build.log`
Expected: `testAllowedCallRunsUnsecured` FAILS (a secured call of a privileged script is still checked step by step, the write is denied); `testDeniedCallWithoutOnDeniedThrows` FAILS (no message naming the function); `testDirectWriteIsDenied`, `testPlainScriptKeepsSecurity` PASS.

- [ ] **Step 4: Add the I18N constant for a denied call**

In `configured/I18NConstants.java`:

```java
	/**
	 * @en You are not allowed to call the function "{0}".
	 */
	public static ResKey1 ERROR_CALL_NOT_ALLOWED__NAME;
```

- [ ] **Step 5: Implement the check and the denied result in `PrivilegedScript`**

Add to `PrivilegedScript`:

```java
	private volatile QueryExecutor _securityObject;

	private volatile QueryExecutor _onDenied;

	/**
	 * Whether the current user may call this function with the given arguments.
	 *
	 * <p>
	 * The {@link Config#getSecurityObject() security object} is computed without security. A
	 * result that is no single {@link TLObject} (e.g. <code>null</code> or a collection) denies
	 * the call.
	 * </p>
	 *
	 * @param arguments
	 *        The arguments of the call, in the order of the {@link Config#getParameters()
	 *        parameters}.
	 */
	public boolean isAllowed(Object[] arguments) {
		Object securityObject = securityObjectExecutor().executeIntermediate(arguments);
		if (!(securityObject instanceof TLObject object)) {
			return false;
		}
		return ModelAccessRights.getInstance().isAllowed(TLContext.currentUser(), object, _commandGroup);
	}

	/**
	 * The result of a call the current user is not {@link #isAllowed(Object[]) allowed} to make.
	 *
	 * @param arguments
	 *        The arguments of the call.
	 * @return The result of {@link Config#getOnDenied()}, evaluated with security.
	 * @throws TopLogicException
	 *         if no {@link Config#getOnDenied()} is configured.
	 */
	public Object denied(Object[] arguments) {
		Expr onDenied = getConfig().getOnDenied();
		if (onDenied == null) {
			throw new TopLogicException(I18NConstants.ERROR_CALL_NOT_ALLOWED__NAME.fill(getConfig().getName()));
		}
		QueryExecutor executor = _onDenied;
		if (executor == null) {
			executor = QueryExecutor.compile(withParameters(onDenied));
			_onDenied = executor;
		}
		return executor.executeIntermediate(arguments);
	}

	private QueryExecutor securityObjectExecutor() {
		QueryExecutor executor = _securityObject;
		if (executor == null) {
			executor = QueryExecutor.compile(withParameters(getConfig().getSecurityObject()));
			executor.disableSecurity();
			_securityObject = executor;
		}
		return executor;
	}
```

Imports: `com.top_logic.model.TLObject`, `com.top_logic.model.search.expr.query.QueryExecutor`, `com.top_logic.model.security.ModelAccessRights`, `com.top_logic.util.TLContext`, `com.top_logic.util.error.TopLogicException`. (A race compiling an executor twice is harmless: both are equivalent, the last one wins. `disableSecurity()` is called before the executor is published.)

- [ ] **Step 6: Add the entry point to `ConfiguredTLScriptFunctions`**

```java
	/**
	 * Calls the configured function.
	 *
	 * <p>
	 * A secured call of a {@link PrivilegedScript} is checked once: if the current user is
	 * {@link PrivilegedScript#isAllowed(Object[]) allowed}, the function is executed without
	 * security and its result is filtered for the Discover right, since the calling script
	 * continues secured; otherwise the {@link PrivilegedScript#denied(Object[]) denied result} is
	 * returned. Every other call executes the function with the given security setting.
	 * </p>
	 *
	 * @param scriptName
	 *        The name of the function.
	 * @param usesSecurity
	 *        Whether the calling expression is evaluated with security.
	 * @param arguments
	 *        The arguments of the call.
	 * @param execution
	 *        Executes the function with the given security setting.
	 * @return The result of the call.
	 */
	public Object call(String scriptName, boolean usesSecurity, Object[] arguments,
			Function<Boolean, Object> execution) {
		if (usesSecurity && _builders.get(scriptName) instanceof PrivilegedScript privileged) {
			if (!privileged.isAllowed(arguments)) {
				return privileged.denied(arguments);
			}
			return SearchExpression.filterSecurity(execution.apply(Boolean.FALSE));
		}
		return execution.apply(Boolean.valueOf(usesSecurity));
	}

	/**
	 * Whether the current user may call the configured function with the given arguments.
	 *
	 * @return <code>true</code> for a function that is no {@link PrivilegedScript}.
	 * @throws TopLogicException
	 *         iff there is no function with the given name.
	 */
	public boolean isCallAllowed(String scriptName, Object[] arguments) {
		ConfiguredScript script = _builders.get(scriptName);
		if (script == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_SUCH_SCRIPT__NAME.fill(scriptName));
		}
		return !(script instanceof PrivilegedScript privileged) || privileged.isAllowed(arguments);
	}
```

Imports: `java.util.function.Function`, `com.top_logic.model.search.expr.SearchExpression` (probably present).

- [ ] **Step 7: Route both call expressions through `call`**

`QueryExecutorMethod.eval` (keep the existing JavaDoc `@implNote`, append one sentence: "A secured call of a {@link PrivilegedScript} is checked and filtered by {@link ConfiguredTLScriptFunctions#call(String, boolean, Object[], Function)}."):

```java
	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		return ConfiguredTLScriptFunctions.Module.INSTANCE.getImplementationInstance().call(getName(),
			usesSecurity(), arguments,
			secured -> _executor.apply(getName(), secured).executeIntermediate(arguments));
	}
```

`TracingScriptMethod.eval`:

```java
	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		ConfiguredTLScriptFunctions<?> functions = ConfiguredTLScriptFunctions.Module.INSTANCE.getImplementationInstance();
		return functions.call(getName(), usesSecurity(), arguments,
			secured -> functions.getTracingExecutor(getName(), secured.booleanValue())
				.evalWith(definitions, Args.some(arguments)));
	}
```

and drop the now unused private `tracingSearch()`.

- [ ] **Step 8: Build and run the tests**

Run (**unsandboxed**, new I18N key): `mvn -B install -pl com.top_logic.model.search 2>&1 | tee com.top_logic.model.search/target/mvn-build.log`, then the test command of Step 3, then the regression tests:
`mvn -B test -DskipTests=false -pl com.top_logic.model.search -Dtest=test.com.top_logic.model.search.expr.TestTLScriptSecurity` and `-Dtest=test.com.top_logic.model.search.expr.TestDiscoverRight`.
Expected: all PASS. Fix the German text of `ERROR_CALL_NOT_ALLOWED__NAME` in `messages_de.properties` ("Sie duerfen die Funktion "{0}" nicht aufrufen." with a real umlaut - properties files are not Java sources) if DeepL produced something odd.

- [ ] **Step 9: Commit (after the user approved the shown diff)**

```bash
git add com.top_logic.model.search/src/main/java/com/top_logic/model/search/configured/ com.top_logic.model.search/src/main/java/META-INF/messages_*.properties com.top_logic.model.search/deploy/local/webapp/WEB-INF/conf/TestTLScriptSecurity-test.config.xml com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/configured/TestPrivilegedScript.java
git commit -m "Ticket #29858: Check a secured call of a privileged script once and run it unsecured."
```

---

### Task 3: `canCall`

**Files:**
- Create: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/configured/CanCall.java`
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/configured/I18NConstants.java`
- Modify: `com.top_logic.model.search/src/main/webapp/WEB-INF/conf/modelSearchConf.config.xml:442` (next to `canExecute`)
- Create: `com.top_logic.model.search/src/main/webapp/doc/en/DeveloperGuide/TLScript/ModelOperations/canCall/index.html`, `.../page.properties`
- Create: `com.top_logic.model.search/src/main/webapp/doc/de/DeveloperGuide/TLScript/ModelOperations/canCall/index.html`, `.../page.properties`
- Test: `com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/configured/TestPrivilegedScript.java`

**Interfaces:**
- Consumes: `ConfiguredTLScriptFunctions#isCallAllowed(String, Object[])` (Task 2); `QueryExecutorMethod` / `TracingScriptMethod` as `GenericMethod` with `getName()` and `getArguments()`.
- Produces: TL-Script function `canCall(call)` returning `Boolean`.

- [ ] **Step 1: Write the failing tests** (append to `TestPrivilegedScript`)

```java
	public void testCanCall() throws Exception {
		becomeUser(_approver);
		assertEquals(Boolean.TRUE, execute(search("p -> canCall(testPrivilegedRename($p, 'x'))"), _p1));
		assertEquals("canCall does not call the function.", "p1", _p1.tValueByName("name"));
		becomeUser(_auditor);
		assertEquals(Boolean.FALSE, execute(search("p -> canCall(testPrivilegedRename($p, 'x'))"), _p1));
	}

	public void testCanCallNullSecurityObject() throws Exception {
		becomeUser(_approver);
		assertEquals(Boolean.FALSE, execute(search("p -> canCall(testPrivilegedNever($p))"), _p1));
	}

	public void testCanCallPlainScript() throws Exception {
		becomeUser(_auditor);
		assertEquals(Boolean.TRUE, execute(search("p -> canCall(testPlainScript($p))"), _p1));
	}

	public void testCanCallChecksAlsoUnsecured() throws Exception {
		becomeUser(_auditor);
		QueryExecutor executor = QueryExecutor.compile(search("p -> canCall(testPrivilegedRename($p, 'x'))"));
		executor.disableSecurity();
		assertEquals(Boolean.FALSE, executor.execute(_p1));
	}

	public void testCanCallRejectsNonCall() {
		for (String script : new String[] { "canCall(true)", "p -> canCall(canExecute($p, 'Read'))" }) {
			try {
				QueryExecutor.compile(search(script));
				fail("canCall must reject: " + script);
			} catch (Exception ex) {
				// expected: compile error
			}
		}
	}
```

- [ ] **Step 2: Run them to verify they fail**

Run: the test command of Task 2, Step 3.
Expected: the five new tests FAIL / ERROR with "unknown function canCall" (or similar parse/compile error); `testCanCallRejectsNonCall` may pass for the wrong reason - it must pass for the right reason after Step 5.

- [ ] **Step 3: Add the I18N constant**

```java
	/**
	 * @en The argument of canCall() must be a call of a configured TL-Script function: {0}
	 */
	public static ResKey1 ERROR_CAN_CALL_NO_CALL__EXPR;
```

- [ ] **Step 4: Write `CanCall`**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.configured;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.util.TypeSpec;

/**
 * {@link GenericMethod} reporting whether the current user may make a call of a configured
 * TL-Script function, without making it.
 *
 * <p>
 * The argument is the call itself, e.g. <code>canCall(approveMilestone($m))</code>. Only the
 * arguments of that call are evaluated; the result is
 * {@link ConfiguredTLScriptFunctions#isCallAllowed(String, Object[])}.
 * </p>
 *
 * @see PrivilegedScript
 */
public class CanCall extends GenericMethod {

	/**
	 * Creates a {@link CanCall}.
	 */
	CanCall(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new CanCall(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.BOOLEAN_TYPE);
	}

	/**
	 * Evaluates only the arguments of the wrapped call, not the call itself.
	 */
	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		GenericMethod call = (GenericMethod) getArguments()[0];
		SearchExpression[] callArguments = call.getArguments();
		Object[] values = new Object[callArguments.length];
		for (int n = 0; n < values.length; n++) {
			values[n] = callArguments[n] == null ? null : callArguments[n].eval(definitions);
		}
		return eval(new Object[] { call.getName(), values }, definitions);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		String scriptName = (String) arguments[0];
		Object[] callArguments = (Object[]) arguments[1];
		return Boolean.valueOf(ConfiguredTLScriptFunctions.Module.INSTANCE.getImplementationInstance()
			.isCallAllowed(scriptName, callArguments));
	}

	/**
	 * The check depends on the current user and can therefore not be evaluated at compile time.
	 */
	@Override
	public boolean canEvaluateAtCompileTime(Object[] arguments) {
		return false;
	}

	/**
	 * {@link com.top_logic.model.search.expr.config.operations.MethodBuilder} creating the
	 * {@code canCall} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<CanCall> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("call")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

		@Override
		public CanCall build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			if (!(args[0] instanceof QueryExecutorMethod)) {
				throw new ConfigurationException(I18NConstants.ERROR_CAN_CALL_NO_CALL__EXPR.fill(expr),
					getName(), expr.location());
			}
			return new CanCall(getName(), args);
		}

		@Override
		public Object getId() {
			return "canCall";
		}

	}

}
```

Copy the imports of `AbstractSimpleMethodBuilder`, `ArgumentDescriptor`, `Args`, `TypeSpec` exactly from `CanExecute.java`. Copy the way a builder reports a compile error (the exact `ConfigurationException` constructor) from another builder in `com.top_logic.model.search.expr.config.operations` that rejects an argument (search for `throw new ConfigurationException(` there) - do not invent a constructor. A `TracingScriptMethod` (after `TracingAccessRewriter`) is also a `GenericMethod` with the script name and arguments, so `internalEval` handles both.

- [ ] **Step 5: Register the function**

In `modelSearchConf.config.xml`, after the `canExecute` line:

```xml
					<method name="canCall" class="com.top_logic.model.search.configured.CanCall$Builder"/>
```

- [ ] **Step 6: Write the help pages**

`doc/en/.../ModelOperations/canCall/page.properties`:

```
position=16571
source=com.top_logic.model.search
title=Check whether a function may be called
uuid=<generate with: python3 -c "import uuid; print(uuid.uuid4())">
```

`doc/de/.../canCall/page.properties`: same `position`, `source` and `uuid` (the same uuid as en, check this against the `canExecute` pair), `title=Prüfen, ob eine Funktion aufgerufen werden darf` (follow the encoding used in `doc/de/.../canExecute/page.properties`).

`index.html` (en; de analogous in German) - copy the HTML skeleton of `canExecute/index.html` and fill the sections:
- Syntax: `canCall($call)`
- Description: reports whether the current user may make the given call of a configured function without making it. For a privileged script, the security object is computed and the command group is checked on it; a plain configured function may always be called. Typical use: the executability of a command calling a privileged function.
- Parameters: `call` - a call of a configured TL-Script function; anything else is a compile error.
- Return value: Boolean.
- Example: `canCall(approveMilestone($milestone))`.

- [ ] **Step 7: Build and run the tests**

Run (**unsandboxed**): `mvn -B install -pl com.top_logic.model.search 2>&1 | tee com.top_logic.model.search/target/mvn-build.log`, then the test command of Task 2, Step 3.
Expected: all tests of `TestPrivilegedScript` PASS, `testCanCallRejectsNonCall` fails before Step 4 for the missing function, after it for the explicit check.

- [ ] **Step 8: Commit (after the user approved the shown diff)**

```bash
git add com.top_logic.model.search/src/main/java/com/top_logic/model/search/configured/ com.top_logic.model.search/src/main/java/META-INF/messages_*.properties com.top_logic.model.search/src/main/webapp/WEB-INF/conf/modelSearchConf.config.xml com.top_logic.model.search/src/main/webapp/doc/ com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/configured/TestPrivilegedScript.java
git commit -m "Ticket #29858: Add canCall() reporting whether a configured function may be called."
```

---

### Task 4: Demo scenario in `tl-demo-react` and manual verification

**Files:**
- Modify: `com.top_logic.demo.react/src/main/webapp/WEB-INF/conf/demoReactConf.config.xml`
- Modify: `com.top_logic.demo.react/src/main/webapp/WEB-INF/views/projects/milestone-list.view.xml`

**Interfaces:**
- Consumes: `privileged-script`, `canCall` (Tasks 1-3).

- [ ] **Step 1: Configure the command group, the grant and the script**

In `demoReactConf.config.xml`, inside `<services>`:

```xml
		<config service-class="com.top_logic.tool.boundsec.simple.CommandGroupRegistry">
			<instance class="com.top_logic.tool.boundsec.simple.CommandGroupRegistry">
				<group name="Approve" type="write"/>
			</instance>
		</config>

		<config service-class="com.top_logic.model.search.configured.ConfiguredTLScriptFunctions">
			<instance class="com.top_logic.model.search.configured.ConfiguredTLScriptFunctions">
				<scripts>
					<!--
						A member of a project approves a milestone of it, although only an
						administrator may write Milestone#approved (see the security-config).
					-->
					<privileged-script name="approveMilestone"
						command-group="Approve"
						security-object="$milestone.container()"
					>
						<parameters>
							<parameter name="milestone"/>
						</parameters>
						<implementation>{
							$milestone.set(`tl.demo.projectManagement:Milestone#approved`, true);
							$milestone;
						}</implementation>
					</privileged-script>
				</scripts>
			</instance>
		</config>
```

and in the `<class name="tl.demo.projectManagement:ProjectScope">` of the `<security-config>`:

```xml
						<grant
							operation="Approve"
							roles="demo.react.ProjectMember"
						/>
```

Extend the comment block above the `<module name="tl.demo.projectManagement">` grants with one sentence: "A member approves a milestone of P1 with the "Approve" command of the milestone list (the privileged script approveMilestone), a ticket watcher cannot."

- [ ] **Step 2: Add the command to the milestone list**

In `milestone-list.view.xml`, after the delete command (line 72), and one sentence in the header comment ("The toolbar also approves the selected milestone through the privileged script approveMilestone, enabled only for a user allowed to call it."):

```xml
			<generic-command
				image="css:bi bi-patch-check"
				input="milestone"
				placement="TOOLBAR"
			>
				<label>
					<en>Approve</en>
					<de>Freigeben</de>
				</label>
				<executability>
					<null-input-disabled/>
					<disabled-if expr="m -> if(canCall(approveMilestone($m)), null, #('You may not approve this milestone.'@en, 'Sie d&#252;rfen diesen Meilenstein nicht freigeben.'@de))"/>
				</executability>
				<with-transaction>
					<execute-script function="m -> approveMilestone($m)"/>
				</with-transaction>
			</generic-command>
```

(Write the umlaut the way the file already does - it is UTF-8, so a plain `ü` is fine; check line 70 for the existing style.)

- [ ] **Step 3: Build**

Run: `mvn -B install -pl com.top_logic.model.search,com.top_logic.demo.react 2>&1 | tee com.top_logic.demo.react/target/mvn-build.log`
Expected: BUILD SUCCESS.

- [ ] **Step 4: Verify manually in the browser**

Start `tl-demo-react` with the `tl-app` skill and delegate the browser loop to a sub-agent (see the `tl-app` skill, "Verifying in the browser"). Scenario:
1. As `root` / `root1234`: create an account A (administration), make A's person a member (not admin) of a project P; create an account W and add W's person as watcher of a ticket of P (see the ticket-watcher scenario in `access-discover-spec.md`).
2. As A: open the milestones of P, select an unapproved milestone. "Approve" is enabled; clicking it sets "Approved". Editing the milestone form shows "Approved" still not editable.
3. As W: open the same milestone list (via the tree demo / project). "Approve" is disabled with the tooltip "You may not approve this milestone."
Expected: as described; no server error in the log.

- [ ] **Step 5: Commit (after the user approved the shown diff)**

```bash
git add com.top_logic.demo.react/src/main/webapp/WEB-INF/conf/demoReactConf.config.xml com.top_logic.demo.react/src/main/webapp/WEB-INF/views/projects/milestone-list.view.xml
git commit -m "Ticket #29858: Approve a milestone through a privileged script in the React demo."
```

---

### Task 5: Documentation

**Files:**
- Modify: `com.top_logic/src/main/java/META-INF/tl-docs/access-privileged-scripts.md`
- Modify: `com.top_logic/src/main/java/META-INF/tl-docs/access-discover-spec.md` (R12)
- Modify: `com.top_logic/src/main/java/META-INF/tl-docs/access-configuration.md`

- [ ] **Step 1: Mark the spec as implemented**

In `access-privileged-scripts.md` replace the status line by:

```markdown
**Status:** implemented by ticket #29858. It extends the model based security
```

and change the description front matter to start with "Read before configuring or changing privileged TL-Script functions - ...". Adapt the "Example" section if the demo (Task 4) deviates from it (role, attribute names).

- [ ] **Step 2: Add the exception to R12 of the Discover specification**

In `access-discover-spec.md`, R12, after "A called function - built-in or configured (`ConfiguredTLScriptFunctions`) - has no setting of its own: it takes over the setting of the calling script.", add:

```markdown
Exception: a privileged function (`privileged-script`) called from a secured script checks its
command group once and runs unsecured, see [privileged TL-Script functions](doc:access-privileged-scripts).
```

- [ ] **Step 3: Point to it from the access configuration article**

In `access-configuration.md`, at the end of the section describing grants of custom operations / command groups (search for `operation=`; if there is no such section, at the end of the article), add:

```markdown
A custom operation can also authorize a configured TL-Script function as a whole, see
[privileged TL-Script functions](doc:access-privileged-scripts).
```

- [ ] **Step 4: Check the articles**

Run: `mvn -B install -pl com.top_logic 2>&1 | tee com.top_logic/target/mvn-build.log` and `mvn -B test -DskipTests=false -pl com.top_logic -Dtest=test.com.top_logic.TestAll 2>&1 | tee -a com.top_logic/target/mvn-build.log | grep -E "TestDocumentation|Tests run|FAIL"` (TestDocumentation runs within the module's TestAll; if that suite is too long, find the class that runs `TestDocumentation` for `com.top_logic` with `grep -rn "TestDocumentation" com.top_logic/src/test` and run it by its FQN).
Expected: no failure of `TestDocumentation` (descriptions present, `doc:` links resolve).

- [ ] **Step 5: Commit (after the user approved the shown diff)**

```bash
git add com.top_logic/src/main/java/META-INF/tl-docs/
git commit -m "Ticket #29858: Document privileged TL-Script functions as implemented."
```

---

## Before pushing

- All new `.java` files: SPDX header, class JavaDoc, ASCII only.
- `mvn -B test -DskipTests=false -pl com.top_logic.model.search -Dtest=test.com.top_logic.model.search.configured.TestPrivilegedScriptConfig`, `...TestPrivilegedScript`, `...expr.TestTLScriptSecurity`, `...expr.TestDiscoverRight` green.
- Build conformance: see `com.top_logic.basic/src/main/java/META-INF/tl-docs/engine/build-conformance.md`.
