# Script-Determined Access Parent Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the enum-based access parent setting (`access-parent` + `access-reference`) by one polymorphic `<access-parent>` element whose implementations are `container`, `target`, `self` (core) and `script` (TL-Script, `com.top_logic.model.search`).

**Architecture:** The configured side (`AccessParentDefinition`, polymorphic, tag-named, inside a wrapper item `AccessParentConfig`) is resolved once per `<class>` entry at service startup into a runtime `AccessParentFunction` (`ContainerRelation`, `TargetRelation`, `ScriptAccessParent`). `SecurityConfigurationService` works on `AccessParentFunction` everywhere it worked on the record `AccessParent`; the coverage analysis, editor and table follow.

**Tech Stack:** Java 17, TopLogic TypedConfiguration, TL-Script (`QueryExecutor`), JUnit 4 (`BasicTestCase`, `KBSetup`), Maven.

**Spec:** `docs/superpowers/specs/2026-10-05-script-access-parent-design.md`

## Global Constraints

- Commit messages: `Ticket #0: <description>` until a ticket number is assigned; end with `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`. Show the diff to the user and wait for approval before every commit (user rule).
- Java files are ISO-8859-1: no non-ASCII characters in `.java` files (use HTML entities in JavaDoc).
- Every new `.java` file: SPDX header (`SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>`, `SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0`) and a class JavaDoc comment.
- Member fields start with `_`. Reference symbols in JavaDoc with `{@link}`, never downgrade to `{@code}`.
- `@Mandatory` is `com.top_logic.basic.config.annotation.Mandatory`. `@ClassDefault` is required on a config interface extending `PolymorphicConfiguration<T>` only when the interface is not nested in its implementation class with a matching type parameter - follow the existing pattern (`PolymorphicConfiguration<Impl>` nested in `Impl`).
- Build from the root with `-pl`, never `-am`; `mvn -B ... 2>&1 | tee <module>/target/mvn-build.log`. Tests: `mvn -B test -DskipTests=false -pl <module> -Dtest=<FQN> -Dsurefire.failIfNoSpecifiedTests=false`.
- Maven needs the internal DNS (`dev.top-logic.com`): run Maven outside the sandbox. Eclipse "Build Automatically" must be off while Maven builds.
- Never edit `messages_en.properties` by hand; `messages_de.properties` new keys get German text by hand after the build seeded them.
- The old attribute form `access-parent="..."`/`access-reference="..."` is removed without compatibility layer.

## Review Focus

1. A `<class>` entry still using the old attribute form `access-parent="container"` - the startup must fail with a configuration error naming the attribute, not silently treat the type as `auto` (test in Task 2).
2. `<access-parent>` present but empty (no implementation inside) - must be a configuration error (`@Mandatory` on the definition), not "automatic" (test in Task 2).
3. A script whose collection result contains one `TLObject` plus `null` entries, e.g. `list($x, null)` - counts as several values, so denied and logged; not silently taking the object (test in Task 3).
4. A script throwing for one object must not break the access check of other objects in the same table/request (test in Task 3: second object still decided).
5. `getAccessibleTypes` for a script type with a user holding no role anywhere - the type counts as accessible (pre-filter), the per-object check still denies (test in Task 3).

---

## File Structure

`com.top_logic` (package `com.top_logic.model.security`):
- Create `AccessParentFunction.java` - runtime relation: `resolve`, `getParentTypes`, `getLabel`.
- Create `AccessParentDefinition.java` - configured relation: `resolve(InstantiationContext, TLClass)`.
- Create `AccessParentConfig.java` - wrapper item `<access-parent>` with the `@DefaultContainer` definition and a `@Container` back link.
- Create `ContainerAccessParent.java` (`<container reference="..."/>`) + `ContainerRelation.java` (record).
- Create `TargetAccessParent.java` (`<target reference="..."/>`) + `TargetRelation.java` (record).
- Create `SelfAccessParent.java` (`<self/>`).
- Create `AccessParentReferenceOptions.java` - option functions `Compositions` and `ToOneReferences` for the `reference` properties (replaces `AccessReferenceOptions`).
- Modify `SecurityConfigurationService.java` - `TLClassAccessRights.getAccessParent()` type, resolution, runtime use.
- Modify `ModelAccessRights.java`, `AccessParentStandsAlone.java`, `I18NConstants.java`.
- Delete `AccessParent.java`, `AccessParentKind.java`, `AccessReferenceOptions.java`, `AccessReferenceRequired.java`.

`com.top_logic.element`:
- Modify `boundsec/manager/coverage/TypeCoverage.java`, `SecurityCoverageAnalysis.java`, `SecurityDefinitionEditor.java`.
- Modify tests `TestSecurityCoverageAnalysis.java`, `TestSecurityDefinitionEditor.java`; create `TestAccessParentConfig.java`.
- Modify `src/test/webapp/WEB-INF/conf/element.test.config.xml`.

`com.top_logic.layout.view`:
- Modify `admin/SecurityCoverageTable.java`, `admin/I18NConstants.java`.

`com.top_logic.model.search`:
- Create `rules/ScriptAccessParent.java`, test `rules/TestScriptAccessParent.java`.
- Modify `deploy/local/webapp/WEB-INF/model/TestTLScriptSecurity.model.xml`, `kbase/TestTLScriptSecurityMeta.xml`, `conf/TestTLScriptSecurity-test.config.xml`.

`com.top_logic.demo.react`: modify `WEB-INF/conf/demoReactConf.config.xml`.
`docs/faq/access-configuration.md`: update.

---

### Task 1: Core API, fixed relations and all consumers

The core change breaks every consumer of `AccessParent`/`AccessParentKind` at compile time, so this task changes them together and ends with the existing tests green.

**Files:**
- Create: the seven core files listed above.
- Modify: `com.top_logic/src/main/java/com/top_logic/model/security/SecurityConfigurationService.java` (lines 205-284 `TLClassAccessRights`, 340-350 fields, 371-389 constructor, 414-441 inheritance, 478-558 `handleTLClass`/`resolveAccessParent`, 614-647 `getAccessParent`/`getAccessParentTypes`, 908-972 `compute`/`accessParentOf`/`roleHolder`, 1144-1165 `isAccessibleType`)
- Modify: `ModelAccessRights.java:80-94`, `AccessParentStandsAlone.java`, `I18NConstants.java` (security)
- Delete: `AccessParent.java`, `AccessParentKind.java`, `AccessReferenceOptions.java`, `AccessReferenceRequired.java`
- Modify: `com.top_logic.element/.../coverage/TypeCoverage.java:14,29,46`, `SecurityCoverageAnalysis.java:40,57,175`, `SecurityDefinitionEditor.java:26,440-488`
- Modify: `com.top_logic.layout.view/.../admin/SecurityCoverageTable.java:54,616-636,728-747`
- Modify: `com.top_logic.element/src/test/webapp/WEB-INF/conf/element.test.config.xml:147-182`
- Modify: `com.top_logic.demo.react/src/main/webapp/WEB-INF/conf/demoReactConf.config.xml:178-185`
- Test: `com.top_logic.element/src/test/java/test/com/top_logic/element/boundsec/manager/coverage/TestSecurityCoverageAnalysis.java:189-270`, `TestSecurityDefinitionEditor.java:255-295`, `TestAccessParentCheck.java` (unchanged, must stay green)

**Interfaces:**
- Produces (package `com.top_logic.model.security`):
  - `interface AccessParentFunction { TLObject resolve(TLObject object); Set<TLClass> getParentTypes(TLClass type); ResKey getLabel(); }`
  - `interface AccessParentDefinition { AccessParentFunction resolve(InstantiationContext context, TLClass type); }`
  - `interface AccessParentConfig extends ConfigurationItem { String DEFINITION = "definition"; String RIGHTS = "rights"; PolymorphicConfiguration<? extends AccessParentDefinition> getDefinition(); void setDefinition(PolymorphicConfiguration<? extends AccessParentDefinition>); static boolean delegates(AccessParentConfig config); }`
  - `record ContainerRelation(TLReference composition, boolean configured) implements AccessParentFunction` with `static ContainerRelation DEFAULT`, `static ContainerRelation ANY`
  - `record TargetRelation(TLReference reference) implements AccessParentFunction`
  - `ContainerAccessParent.Config` (`@TagName("container")`, `reference`), `TargetAccessParent.Config` (`@TagName("target")`, `reference`), `SelfAccessParent.Config` (`@TagName("self")`)
  - `ModelAccessRights.getAccessParent(TLClass)` returns `AccessParentFunction`
  - `TLClassAccessRights.getAccessParent()` returns `AccessParentConfig` (nullable), setter `setAccessParent(AccessParentConfig)`
  - `SecurityDefinitionEditor.setAccessParent(TLClass type, PolymorphicConfiguration<? extends AccessParentDefinition> definition)` (`null` = automatic)

- [ ] **Step 1: Adapt the coverage test to the new API (failing compile)**

In `TestSecurityCoverageAnalysis.java` replace `import com.top_logic.model.security.AccessParent;` by

```java
import com.top_logic.model.security.AccessParentFunction;
import com.top_logic.model.security.ContainerRelation;
import com.top_logic.model.security.TargetRelation;
```

and replace the bodies of the access parent tests (lines 189-270) by:

```java
	public void testCompositionPartDelegatesToContainerByDefault() {
		TypeCoverage coverage = coverage(SINGLE_CONTAINED);
		assertEquals(coverage.toString(), CoverageStatus.DELEGATED, coverage.status());
		assertTrue(coverage.toString(), coverage.findings().isEmpty());
		AccessParentFunction parent = coverage.accessParent();
		assertSame("A composition part without a role source has its container as access parent by default.",
			ContainerRelation.DEFAULT, parent);
		assertEquals(List.of(SINGLES_REFERENCE), qualifiedNames(coverage.containerReferences()));
	}

	public void testPartOfSeveralCompositionsDelegatesToContainer() {
		TypeCoverage coverage = coverage(DOUBLE_CONTAINED);
		assertEquals(coverage.toString(), CoverageStatus.DELEGATED, coverage.status());
		assertSame("Several compositions are no ambiguity: the container holding the object decides.",
			ContainerRelation.DEFAULT, coverage.accessParent());
		assertEquals(List.of(DOUBLES_A_REFERENCE, DOUBLES_B_REFERENCE),
			qualifiedNames(coverage.containerReferences()));
	}

	public void testChainOfCompositionParts() {
		assertEquals(CoverageStatus.DELEGATED, coverage(PART_OF_COVERED).status());
		assertEquals(CoverageStatus.DELEGATED, coverage(SUB_PART).status());
	}

	public void testConfiguredAccessParentBackwards() {
		TypeCoverage coverage = coverage(EXPLICIT_PART);
		ContainerRelation parent = (ContainerRelation) coverage.accessParent();
		assertTrue(parent.configured());
		assertEquals("A composition is navigated backwards to the container.", EXPLICIT_PARTS_REFERENCE,
			TLModelUtil.qualifiedName(parent.composition()));

		assertEquals("The role rule applying to the type is shadowed by the access parent: " + coverage,
			CoverageStatus.INCOMPLETE, coverage.status());
		CoverageFinding finding = singleFinding(coverage, FindingKind.SHADOWED_RULES);
		assertEquals(List.of(SHADOWED_RULE_ID), finding.getRuleIds());
	}

	public void testConfiguredAccessParentForwards() {
		TypeCoverage coverage = coverage(LINKED);
		assertEquals(coverage.toString(), CoverageStatus.DELEGATED, coverage.status());
		TargetRelation parent = (TargetRelation) coverage.accessParent();
		assertEquals("A to-one reference of the type is navigated forwards.", TARGET_REFERENCE,
			TLModelUtil.qualifiedName(parent.reference()));
		assertTrue("The type is held in no composition.", coverage.containerReferences().isEmpty());
	}

	public void testConfiguredAnyContainer() {
		TypeCoverage coverage = coverage(ANY_CONTAINER_PART);
		assertSame("Without a reference, the container holding the object decides; configured, not the default.",
			ContainerRelation.ANY, coverage.accessParent());

		CoverageFinding finding = singleFinding(coverage, FindingKind.SHADOWED_RULES);
		assertEquals(List.of(ANY_CONTAINER_RULE_ID), finding.getRuleIds());
	}

	public void testConfiguredContainerTellsDirectionOfRecursiveComposition() {
		ContainerRelation parent = (ContainerRelation) coverage(RECURSIVE).accessParent();
		assertEquals("A composition the type owns is navigated backwards when the container is configured.",
			DETAIL_REFERENCE, TLModelUtil.qualifiedName(parent.composition()));
	}

	public void testSelfSwitchesOffContainerDefault() {
		TypeCoverage coverage = coverage(SELF_DECIDING);
		assertNull("A composition part deciding for itself does not delegate to its container.",
			coverage.accessParent());
		assertEquals(Set.of(ROLE_READER), roleNames(coverage.readRoles()));
	}

	public void testSelfStopsInheritedAccessParent() {
		assertNotNull(coverage(LINKED).accessParent());
		assertNull("The specialization decides for itself instead of inheriting the access parent.",
			coverage(LINKED_SUB).accessParent());
	}
```

- [ ] **Step 2: Migrate the test configuration to the new form**

In `com.top_logic.element/src/test/webapp/WEB-INF/conf/element.test.config.xml` replace the seven entries (lines 147-182) by:

```xml
					<class name="TestSecurityCoverage:ExplicitPart">
						<access-parent>
							<container reference="TestSecurityCoverage:Covered#explicitParts"/>
						</access-parent>
					</class>
					<class name="TestSecurityCoverage:AnyContainerPart">
						<access-parent>
							<container/>
						</access-parent>
					</class>
					<class name="TestSecurityCoverage:Recursive">
						<access-parent>
							<container reference="TestSecurityCoverage:Recursive#detail"/>
						</access-parent>
					</class>
					<class name="TestSecurityCoverage:Linked">
						<access-parent>
							<target reference="TestSecurityCoverage:Linked#target"/>
						</access-parent>
					</class>
					<class name="TestSecurityCoverage:LinkedSub">
						<access-parent>
							<self/>
						</access-parent>
						<grant
							operation="Read"
							roles="TestSecurityCoverage.Reader"
						/>
					</class>
					<class name="TestSecurityCoverage:SelfDeciding">
						<access-parent>
							<self/>
						</access-parent>
						<grant
							operation="Read"
							roles="TestSecurityCoverage.Reader"
						/>
					</class>
					<class name="TestSecurityCoverage:Cyclic">
						<access-parent>
							<target reference="TestSecurityCoverage:Cyclic#next"/>
						</access-parent>
					</class>
```

In `com.top_logic.demo.react/src/main/webapp/WEB-INF/conf/demoReactConf.config.xml` replace lines 178-185 by:

```xml
					<class name="tl.demo.projectManagement:Milestone">
						<access-parent>
							<container reference="tl.demo.projectManagement:ProjectScope#milestones"/>
						</access-parent>
					</class>
					<class name="tl.demo.projectManagement:Note">
						<access-parent>
							<container reference="tl.demo.projectManagement:ProjectScope#notes"/>
						</access-parent>
					</class>
```

- [ ] **Step 3: Create the runtime and definition interfaces**

`com.top_logic/src/main/java/com/top_logic/model/security/AccessParentFunction.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;

/**
 * The relation leading from an object to its access parent: the object whose access definition
 * decides access to it.
 * <p>
 * An object with an access parent has no grants and no roles of its own. Whether a user may
 * perform an operation on it is whether the user may perform the corresponding operation on its
 * access parent, and that parent may delegate further.
 * </p>
 *
 * @see ModelAccessRights#getAccessParent(TLClass)
 * @see AccessParentDefinition
 */
public interface AccessParentFunction {

	/**
	 * The access parent of the given object.
	 *
	 * @param object
	 *        The object delegating its access decision.
	 * @return The object deciding, <code>null</code> when the relation leads nowhere. An object
	 *         without access parent is not accessible.
	 */
	TLObject resolve(TLObject object);

	/**
	 * The types an access parent of an object of the given type may have.
	 *
	 * @param type
	 *        The delegating type.
	 * @return The possible types of the access parent, <code>null</code> when they are not known
	 *         statically. A type whose parent types are unknown counts as accessible in a check
	 *         that depends on no concrete object.
	 */
	Set<TLClass> getParentTypes(TLClass type);

	/**
	 * Short description of the relation for the coverage display.
	 */
	ResKey getLabel();

}
```

`AccessParentDefinition.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLClass;

/**
 * The configured access parent of a type, see {@link AccessParentConfig}.
 * <p>
 * A definition is resolved once for the type it is configured for, when the access rights are
 * loaded, into the {@link AccessParentFunction} the access check follows.
 * </p>
 */
public interface AccessParentDefinition {

	/**
	 * Resolves this definition for the given type.
	 *
	 * @param context
	 *        The context to report an unusable setting to.
	 * @param type
	 *        The type the definition is configured for.
	 * @return The relation objects of the type delegate their access decision through,
	 *         <code>null</code> for a type deciding for itself ({@link SelfAccessParent}) and for an
	 *         unusable setting, which is reported to the context.
	 */
	AccessParentFunction resolve(InstantiationContext context, TLClass type);

}
```

`AccessParentConfig.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.security.SecurityConfigurationService.TLClassAccessRights;

/**
 * The access parent setting of a type: the element <code>&lt;access-parent&gt;</code> of a
 * {@link TLClassAccessRights} entry, holding exactly one {@link AccessParentDefinition}, e.g.
 * <code>&lt;container/&gt;</code>, <code>&lt;target reference="..."/&gt;</code>,
 * <code>&lt;self/&gt;</code>.
 */
public interface AccessParentConfig extends ConfigurationItem {

	/** Configuration name for {@link #getDefinition()}. */
	String DEFINITION = "definition";

	/** Configuration name for {@link #getRights()}. */
	String RIGHTS = "rights";

	/**
	 * How the access parent of an object of the type is determined.
	 */
	@Name(DEFINITION)
	@DefaultContainer
	@Mandatory
	PolymorphicConfiguration<? extends AccessParentDefinition> getDefinition();

	/**
	 * Setter for {@link #getDefinition()}.
	 */
	void setDefinition(PolymorphicConfiguration<? extends AccessParentDefinition> value);

	/**
	 * The entry this setting belongs to, giving the type the definition is configured for.
	 */
	@Name(RIGHTS)
	@Hidden
	@Container
	TLClassAccessRights getRights();

	/**
	 * Whether the given setting delegates the access decision: anything but
	 * {@link SelfAccessParent}.
	 *
	 * @param config
	 *        The setting, <code>null</code> for none.
	 */
	static boolean delegates(AccessParentConfig config) {
		return config != null && config.getDefinition() != null
			&& !(config.getDefinition() instanceof SelfAccessParent.Config);
	}

}
```

- [ ] **Step 4: Create the fixed relations**

`ContainerRelation.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link AccessParentFunction} leading to the container of an object.
 *
 * @param composition
 *        The composition the container must hold the object through, navigated backwards;
 *        <code>null</code> for whichever composition holds it.
 * @param configured
 *        Whether the relation is configured for the type, in contrast to the {@link #DEFAULT} a
 *        composition part gets.
 */
public record ContainerRelation(TLReference composition, boolean configured) implements AccessParentFunction {

	/** The container, whichever composition holds the object, as a composition part gets it by default. */
	public static final ContainerRelation DEFAULT = new ContainerRelation(null, false);

	/** The container, whichever composition holds the object, as configured for the type. */
	public static final ContainerRelation ANY = new ContainerRelation(null, true);

	@Override
	public TLObject resolve(TLObject object) {
		if (composition == null) {
			return object.tContainer();
		}
		TLReference via = object.tContainerReference();
		if (via == null || !via.getDefinition().equals(composition.getDefinition())) {
			return null;
		}
		return object.tContainer();
	}

	/**
	 * @return <code>null</code> without a {@link #composition()}: the possible containers are known
	 *         to the {@link SecurityConfigurationService}, which indexes the compositions.
	 */
	@Override
	public Set<TLClass> getParentTypes(TLClass type) {
		if (composition == null) {
			return null;
		}
		return composition.getOwner() instanceof TLClass owner ? Set.of(owner) : Set.of();
	}

	@Override
	public ResKey getLabel() {
		if (composition != null) {
			return I18NConstants.ACCESS_PARENT_CONTAINER_VIA__COMPOSITION.fill(TLModelUtil.qualifiedName(composition));
		}
		return configured ? I18NConstants.ACCESS_PARENT_CONTAINER : I18NConstants.ACCESS_PARENT_CONTAINER_DEFAULT;
	}

}
```

`TargetRelation.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link AccessParentFunction} leading to the object a to-one reference of the delegating object
 * points to.
 *
 * @param reference
 *        The reference navigated forwards. Its owner is the type of the delegating objects.
 */
public record TargetRelation(TLReference reference) implements AccessParentFunction {

	@Override
	public TLObject resolve(TLObject object) {
		Object value = object.tValue(reference);
		return value instanceof TLObject parent ? parent : null;
	}

	@Override
	public Set<TLClass> getParentTypes(TLClass type) {
		return reference.getType() instanceof TLClass target ? Set.of(target) : Set.of();
	}

	@Override
	public ResKey getLabel() {
		return ResKey.text(TLModelUtil.qualifiedName(reference));
	}

}
```

`ContainerAccessParent.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.Step;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLReference;
import com.top_logic.model.resources.TLPartInOwnerResourceProvider;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.security.SecurityConfigurationService.TLClassAccessRights;
import com.top_logic.util.model.ModelService;

/**
 * {@link AccessParentDefinition} delegating to the container holding an object, through whichever
 * composition or through the configured one.
 */
public class ContainerAccessParent extends AbstractConfiguredInstance<ContainerAccessParent.Config>
		implements AccessParentDefinition {

	/**
	 * Configuration of {@link ContainerAccessParent}.
	 */
	@TagName("container")
	@Label("Container")
	public interface Config extends PolymorphicConfiguration<ContainerAccessParent> {

		/** Configuration name for {@link #getReference()}. */
		String REFERENCE = "reference";

		/** Configuration name for {@link #getOwner()}. */
		String OWNER = "owner";

		/**
		 * The composition holding objects of the type, navigated backwards: the container is the
		 * access parent only when it holds the object through this composition. Without one,
		 * whichever composition holds the object leads to the access parent.
		 */
		@Name(REFERENCE)
		@Nullable
		@Options(fun = AccessParentReferenceOptions.Compositions.class, args = @Ref(steps = {
			@Step(OWNER),
			@Step(AccessParentConfig.RIGHTS),
			@Step(TLClassAccessRights.NAME_ATTRIBUTE) }), mapping = TLModelPartRef.PartMapping.class)
		@OptionLabels(TLPartInOwnerResourceProvider.class)
		TLModelPartRef getReference();

		/**
		 * Setter for {@link #getReference()}.
		 */
		void setReference(TLModelPartRef value);

		/**
		 * The setting this definition belongs to.
		 */
		@Name(OWNER)
		@Hidden
		@Container
		AccessParentConfig getOwner();
	}

	/**
	 * Creates a {@link ContainerAccessParent} from configuration.
	 */
	public ContainerAccessParent(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public AccessParentFunction resolve(InstantiationContext context, TLClass type) {
		TLModelPartRef ref = getConfig().getReference();
		if (ref == null) {
			return ContainerRelation.ANY;
		}
		String typeName = TLModelUtil.qualifiedName(type);
		TLModelPart part;
		try {
			part = ref.resolve(ModelService.getApplicationModel());
		} catch (RuntimeException ex) {
			context.error("The access reference " + ref + " of " + typeName + " does not exist.", ex);
			return null;
		}
		if (part instanceof TLReference reference && reference.isComposite()
			&& TLModelUtil.isCompatibleType(reference.getType(), type)) {
			return new ContainerRelation(reference, true);
		}
		context.error("The access reference " + ref + " of " + typeName
			+ " is not a composition holding objects of the type.");
		return null;
	}

}
```

`TargetAccessParent.java` - same structure with these differences: `@TagName("target")`, `@Label("Reference target")`, the `reference` property is `@Mandatory` (instead of `@Nullable`) with options `AccessParentReferenceOptions.ToOneReferences.class` and JavaDoc "The to-one reference of the type whose target is the access parent, navigated forwards.", and:

```java
	@Override
	public AccessParentFunction resolve(InstantiationContext context, TLClass type) {
		TLModelPartRef ref = getConfig().getReference();
		String typeName = TLModelUtil.qualifiedName(type);
		TLModelPart part;
		try {
			part = ref.resolve(ModelService.getApplicationModel());
		} catch (RuntimeException ex) {
			context.error("The access reference " + ref + " of " + typeName + " does not exist.", ex);
			return null;
		}
		if (part instanceof TLReference reference && !reference.isMultiple()
			&& TLModelUtil.isCompatibleType(reference.getOwner(), type)) {
			return new TargetRelation(reference);
		}
		context.error("The access reference " + ref + " of " + typeName + " is not a to-one reference of the type.");
		return null;
	}
```

`SelfAccessParent.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLClass;

/**
 * {@link AccessParentDefinition} of a type deciding for itself through its grants and the roles
 * users hold on its objects. It switches off the default of a composition part and an access
 * parent inherited from a generalization.
 */
public class SelfAccessParent extends AbstractConfiguredInstance<SelfAccessParent.Config>
		implements AccessParentDefinition {

	/**
	 * Configuration of {@link SelfAccessParent}.
	 */
	@TagName("self")
	@Label("Own decision")
	public interface Config extends PolymorphicConfiguration<SelfAccessParent> {
		// No properties.
	}

	/**
	 * Creates a {@link SelfAccessParent} from configuration.
	 */
	public SelfAccessParent(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * @return Always <code>null</code>: the type does not delegate.
	 */
	@Override
	public AccessParentFunction resolve(InstantiationContext context, TLClass type) {
		return null;
	}

}
```

`AccessParentReferenceOptions.java`: move the two private helpers `compositions(TLClass)` and `toOneReferences(TLClass)` of `AccessReferenceOptions` here unchanged (as private static methods), plus:

```java
/**
 * Option functions for the reference of an {@link AccessParentDefinition}, offering the references
 * fitting the type of the enclosing {@link TLClassAccessRights} entry.
 */
public class AccessParentReferenceOptions {

	/**
	 * The compositions holding objects of the named type, see {@link ContainerAccessParent}.
	 */
	public static class Compositions extends Function1<Collection<TLReference>, String> {
		@Override
		public Collection<TLReference> apply(String typeName) {
			TLClass type = type(typeName);
			return type == null ? Collections.emptyList() : sorted(compositions(type));
		}
	}

	/**
	 * The to-one references of the named type, see {@link TargetAccessParent}.
	 */
	public static class ToOneReferences extends Function1<Collection<TLReference>, String> {
		@Override
		public Collection<TLReference> apply(String typeName) {
			TLClass type = type(typeName);
			return type == null ? Collections.emptyList() : sorted(toOneReferences(type));
		}
	}

	private static TLClass type(String typeName) {
		if (StringServices.isEmpty(typeName)) {
			return null;
		}
		try {
			return TLModelUtil.findType(ModelService.getApplicationModel(), typeName) instanceof TLClass clazz
				? clazz : null;
		} catch (RuntimeException ex) {
			// The name is not a type of the application: nothing to offer.
			return null;
		}
	}

	private static List<TLReference> sorted(List<TLReference> references) {
		references.sort(Comparator.comparing(TLModelUtil::qualifiedName));
		return references;
	}

	// compositions(TLClass) and toOneReferences(TLClass) moved from AccessReferenceOptions
}
```

- [ ] **Step 5: I18N constants of the core**

In `com.top_logic/src/main/java/com/top_logic/model/security/I18NConstants.java` remove `ACCESS_REFERENCE_REQUIRED` and add:

```java
	/**
	 * @en container (default)
	 */
	public static ResKey ACCESS_PARENT_CONTAINER_DEFAULT;

	/**
	 * @en container
	 */
	public static ResKey ACCESS_PARENT_CONTAINER;

	/**
	 * @en container via {0}
	 */
	public static ResKey1 ACCESS_PARENT_CONTAINER_VIA__COMPOSITION;
```

In `com.top_logic.layout.view/.../admin/I18NConstants.java` remove `COVERAGE_ACCESS_PARENT_DEFAULT` and `COVERAGE_ACCESS_PARENT_CONTAINER` (lines 88-96).

- [ ] **Step 6: Rewire `TLClassAccessRights` and the constraint**

In `SecurityConfigurationService.TLClassAccessRights` replace everything from `/** Configuration name for {@link #getAccessParent()}. */` through `void setAccessReference(TLModelPartRef value);` (lines 213-282, except `getName()`) by:

```java
		/** Configuration name for {@link #getAccessParent()}. */
		String ACCESS_PARENT = "access-parent";

		// getName() unchanged

		/**
		 * The access parent of the type: the object whose access definition decides access to an
		 * object of the type.
		 * <p>
		 * A type with a delegating access parent has no grants, no marks and no roles of its own:
		 * whether a user may read, write or export one of its objects is whether the user may do
		 * the same to the access parent, and creating or deleting one of its objects is writing
		 * the access parent. An object whose relation leads nowhere is not accessible.
		 * </p>
		 * <p>
		 * Without a setting the type inherits the setting of its generalizations; without one, a
		 * composition part without a role rule, without a role parent rule and without marks
		 * delegates to its container by default. The specializations of the type inherit the
		 * setting unless they have one of their own.
		 * </p>
		 */
		@Name(ACCESS_PARENT)
		@Constraint(value = AccessParentStandsAlone.class, args = { @Ref(GRANTS), @Ref(WITHOUT_SECURITY), @Ref(INTERNAL) })
		AccessParentConfig getAccessParent();

		/**
		 * Setter for {@link #getAccessParent()}.
		 */
		void setAccessParent(AccessParentConfig value);
```

Remove the now unused imports (`Nullable`, `OptionLabels`, `TLPartInOwnerResourceProvider`, `TLModelPartRef`, `DynamicMode` only if unused elsewhere in the file - `getGrants()` still uses `DynamicMode`).

In `AccessParentStandsAlone.check` replace the guard

```java
		if (!(self.getValue() instanceof AccessParentKind kind) || !kind.delegates()) {
			return;
		}
```

by

```java
		if (!(self.getValue() instanceof AccessParentConfig config) || !AccessParentConfig.delegates(config)) {
			return;
		}
```

- [ ] **Step 7: Rewire resolution and runtime use in `SecurityConfigurationService`**

Field (line 347): `private Map<TLClass, AccessParentFunction> _accessParents = new HashMap<>();` (JavaDoc: "A type deciding for itself ({@link SelfAccessParent}) is mapped to <code>null</code> ...").
Constructor (line 373) and `handleTLClass`/`inheritAccessParents`/`inheritAccessParent` signatures: `Map<TLClass, AccessParentFunction> explicitParents`.

Replace in `handleTLClass` (lines 490-498):

```java
		AccessParentConfig accessParent = config.getAccessParent();
		if (accessParent != null) {
			if (AccessParentConfig.delegates(accessParent)
				&& (!config.getGrants().isEmpty() || config.isWithoutSecurity() || config.isInternal())) {
				context.error("The type " + config.getName()
					+ " has an access parent and therefore must have neither grants nor marks of its own.");
			}
			resolveAccessParent(context, clazz, accessParent, explicitParents);
		}
```

Replace `resolveAccessParent` (lines 501-558) by:

```java
	/**
	 * Resolves the configured access parent of the given type and enters it into the given map; a
	 * type {@link SelfAccessParent deciding for itself} is entered as <code>null</code>, an unusable
	 * setting is reported and not entered.
	 */
	private void resolveAccessParent(InstantiationContext context, TLClass type, AccessParentConfig config,
			Map<TLClass, AccessParentFunction> explicitParents) {
		AccessParentDefinition definition = context.getInstance(config.getDefinition());
		if (definition == null) {
			return;
		}
		if (definition instanceof SelfAccessParent) {
			explicitParents.put(type, null);
			return;
		}
		AccessParentFunction function = definition.resolve(context, type);
		if (function != null) {
			explicitParents.put(type, function);
		}
	}
```

`getAccessParent(TLClass)` (line 621): return type `AccessParentFunction`; `return AccessParent.container();` becomes `return ContainerRelation.DEFAULT;`.

`getAccessParentTypes(TLClass)` (lines 638-647):

```java
	/**
	 * The types the objects of the given type may delegate their access decision to.
	 *
	 * @return Empty when the type has no access parent, <code>null</code> when the possible types are
	 *         not known statically.
	 * @see #getAccessParent(TLClass)
	 */
	public Set<TLClass> getAccessParentTypes(TLClass type) {
		AccessParentFunction parent = getAccessParent(type);
		if (parent == null) {
			return Collections.emptySet();
		}
		if (parent instanceof ContainerRelation container && container.composition() == null) {
			return _containerTypes.getOrDefault(type, Collections.emptySet());
		}
		return parent.getParentTypes(type);
	}
```

In `compute`, `accessParentOf`, `roleHolder` replace the type `AccessParent` by `AccessParentFunction` (no other change).

In `isAccessibleType` (lines 1156-1159):

```java
		if (getAccessParent(type) != null) {
			BoundCommandGroup parentOperation = operationOnParent(commandGroup);
			Set<TLClass> parentTypes = getAccessParentTypes(type);
			// Unknown parent types (a computed access parent): the type-level check is a pre-filter
			// only, the check of each object follows the relation.
			result = parentTypes == null || parentTypes.stream()
				.anyMatch(parentType -> isAccessibleType(parentType, person, parentOperation, securityRoot, memo));
		} else {
```

`ModelAccessRights.getAccessParent(TLClass)` (line 92): return type `AccessParentFunction`, JavaDoc unchanged except `@return The relation to the access parent, ...`.

Delete `AccessParent.java`, `AccessParentKind.java`, `AccessReferenceOptions.java`, `AccessReferenceRequired.java`. Fix the remaining `{@link AccessParentKind ...}` references in JavaDoc of the security package (`grep -rn AccessParentKind com.top_logic/src/main/java` must be empty).

- [ ] **Step 8: Adapt the element consumers**

`TypeCoverage.java`: import `com.top_logic.model.security.AccessParentFunction` instead of `AccessParent`; record component `AccessParentFunction accessParent`; JavaDoc `@param accessParent` unchanged in meaning.

`SecurityCoverageAnalysis.java:40,57,175`: same type replacement.

`SecurityDefinitionEditor.java`: replace `setAccessParent` and `dropDelegation` (lines 445-488) by:

```java
	/**
	 * Sets the access parent of the given type, or drops that setting.
	 * <p>
	 * A type with a delegating access parent has no grants and no marks of its own, so setting one
	 * drops the grants and marks the stored configuration holds for the type.
	 * </p>
	 *
	 * @param type
	 *        The type whose access parent is set.
	 * @param definition
	 *        The definition to store, e.g. a {@link ContainerAccessParent.Config}; <code>null</code>
	 *        to drop the setting.
	 * @throws IOException
	 *         When the file cannot be written.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 */
	public void setAccessParent(TLClass type, PolymorphicConfiguration<? extends AccessParentDefinition> definition)
			throws IOException, ConfigurationException {
		TLClassAccessRights entry = editableAccessRights(type);
		if (definition == null) {
			entry.setAccessParent(null);
		} else {
			AccessParentConfig setting = TypedConfiguration.newConfigItem(AccessParentConfig.class);
			setting.setDefinition(definition);
			entry.setAccessParent(setting);
			if (AccessParentConfig.delegates(setting)) {
				entry.getGrants().clear();
				entry.setInternal(false);
				entry.setWithoutSecurity(false);
			}
		}
		putAccessRights(entry);
	}

	/**
	 * Drops a delegating access parent of the given entry, which contradicts a definition of its own.
	 */
	private static void dropDelegation(TLClassAccessRights entry) {
		if (AccessParentConfig.delegates(entry.getAccessParent())) {
			entry.setAccessParent(null);
		}
	}
```

Imports: drop `AccessParentKind` and `TLModelPartRef` if unused; add `AccessParentConfig`, `AccessParentDefinition`, `ContainerAccessParent`, `PolymorphicConfiguration`, `TypedConfiguration`.

`TestSecurityDefinitionEditor.java` (lines 255-295): replace the `AccessParentKind` calls:

```java
	private static PolymorphicConfiguration<? extends AccessParentDefinition> container(String composition) {
		ContainerAccessParent.Config config = TypedConfiguration.newConfigItem(ContainerAccessParent.Config.class);
		config.setReference(TLModelPartRef.ref(composition));
		return config;
	}

	private static PolymorphicConfiguration<? extends AccessParentDefinition> self() {
		return TypedConfiguration.newConfigItem(SelfAccessParent.Config.class);
	}
```

and in the tests:
- `_editor.setAccessParent(contained, AccessParentKind.CONTAINER, TLModelPartRef.ref(CONTAINER_REFERENCE))` becomes `_editor.setAccessParent(contained, container(CONTAINER_REFERENCE))`;
- `assertEquals(AccessParentKind.CONTAINER, stored.getAccessParent()); assertEquals(CONTAINER_REFERENCE, stored.getAccessReference().qualifiedName());` becomes `assertEquals(CONTAINER_REFERENCE, ((ContainerAccessParent.Config) stored.getAccessParent().getDefinition()).getReference().qualifiedName());`;
- "Marking the type internal drops its access parent": `assertNull("Marking the type internal drops its access parent.", stored.getAccessParent());` (drop the `getAccessReference()` assertion);
- `setAccessParent(contained, AccessParentKind.AUTO, null)` becomes `setAccessParent(contained, null)` with `assertNull(_editor.editableAccessRights(contained).getAccessParent());`;
- in `testSelfKeepsOwnDefinition`: `_editor.setAccessParent(contained, self());` and `assertTrue(stored.getAccessParent().getDefinition() instanceof SelfAccessParent.Config);` (drop the access reference assertion, keep the grants assertion).

- [ ] **Step 9: Adapt the coverage table**

In `SecurityCoverageTable.java` replace the import of `AccessParent` by `AccessParentFunction` and `ContainerRelation`, and replace `accessParent(Object)` (lines 624-636) by:

```java
	private static String accessParent(Object row) {
		AccessParentFunction parent = coverage(row).accessParent();
		if (parent == null) {
			return "";
		}
		return Resources.getInstance().getString(parent.getLabel());
	}
```

and in `delegation(TypeCoverage)` (lines 733-747):

```java
		AccessParentFunction parent = coverage.accessParent();
		if (parent == null) {
			return null;
		}
		if (parent instanceof ContainerRelation container && container.composition() == null) {
			String containers = coverage.containerReferences().stream()
				.map(TLModelUtil::qualifiedName)
				.collect(Collectors.joining(VALUE_SEPARATOR));
			return container.configured()
				? I18NConstants.COVERAGE_DELEGATED_CONTAINER__CONTAINERS.fill(containers)
				: I18NConstants.COVERAGE_DELEGATED_DEFAULT__CONTAINERS.fill(containers);
		}
		return I18NConstants.COVERAGE_DELEGATED__PARENT.fill(accessParent(coverage));
```

Remove `INVERSE_MARKER` if it becomes unused (it is still used for role parent paths - keep it then; its JavaDoc mention of the access parent column must go).

- [ ] **Step 10: Build and run the affected tests**

Run (outside the sandbox):

```bash
mvn -B install -pl com.top_logic,com.top_logic.element,com.top_logic.layout.view 2>&1 | tee com.top_logic.element/target/mvn-build.log | grep -E "^\[ERROR\]|BUILD (SUCCESS|FAILURE)"
mvn -B test -DskipTests=false -pl com.top_logic.element -Dsurefire.failIfNoSpecifiedTests=false \
  -Dtest=test.com.top_logic.element.boundsec.manager.coverage.TestAccessParentCheck,test.com.top_logic.element.boundsec.manager.coverage.TestSecurityCoverageAnalysis,test.com.top_logic.element.boundsec.manager.coverage.TestSecurityDefinitionEditor,test.com.top_logic.element.boundsec.manager.coverage.TestSecurityCoverageCheck \
  2>&1 | tee com.top_logic.element/target/mvn-test.log | grep -E "Tests run:|FAIL|BUILD"
```

Expected: `BUILD SUCCESS`, all four classes `Failures: 0, Errors: 0`. Check the build log for TLDoclet warnings about the new files and fix them.

- [ ] **Step 11: German labels**

The build seeded `messages_de.properties` of `com.top_logic` with the new keys (DeepL). Set: `ACCESS_PARENT_CONTAINER_DEFAULT = Container (Standard)`, `ACCESS_PARENT_CONTAINER = Container`, `ACCESS_PARENT_CONTAINER_VIA__COMPOSITION = Container über {0}`, and the labels of the config properties (`ContainerAccessParent.Config` = "Container", `TargetAccessParent.Config` = "Referenzziel", `SelfAccessParent.Config` = "Eigene Entscheidung", `AccessParentConfig.definition` = "Bestimmung", `TLClassAccessRights.access-parent` = "Zugriffselternobjekt"). Remove the German entries of the deleted keys (`ACCESS_REFERENCE_REQUIRED`, `COVERAGE_ACCESS_PARENT_*`, `AccessParentKind.*`, `access-reference`) if the build did not.

- [ ] **Step 12: Commit (after showing the diff to the user)**

```bash
git add -A com.top_logic/src/main/java/com/top_logic/model/security com.top_logic/src/main/java/META-INF \
  com.top_logic.element/src com.top_logic.layout.view/src/main/java com.top_logic.demo.react/src/main/webapp/WEB-INF/conf/demoReactConf.config.xml
git commit -m "Ticket #0: Configure the access parent of a type as one polymorphic <access-parent> element (container, target, self) instead of the kind and reference attributes.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>"
```

---

### Task 2: Configuration validation tests

**Files:**
- Create: `com.top_logic.element/src/test/java/test/com/top_logic/element/boundsec/manager/coverage/TestAccessParentConfig.java`

**Interfaces:**
- Consumes: `AccessParentConfig`, `ContainerAccessParent`, `TargetAccessParent`, `SelfAccessParent`, `AccessParentDefinition.resolve(InstantiationContext, TLClass)` from Task 1; the model module `TestSecurityCoverage` of the element test application.

- [ ] **Step 1: Write the tests**

The tests parse a single `<class>` entry with `TypedConfiguration` and resolve its definition against the test model, collecting errors in a `BufferingProtocol`-backed `DefaultInstantiationContext`. Use the setup of `TestAccessParentCheck` (`suite()` copied from there, same modules) so that the model service is running.

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager.coverage;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.knowledge.service.PersonManager;
import com.top_logic.knowledge.wrap.person.TLSecurityDeviceManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.security.AccessParentDefinition;
import com.top_logic.model.security.AccessParentFunction;
import com.top_logic.model.security.ContainerRelation;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.security.TargetRelation;
import com.top_logic.model.util.TLModelUtil;
// Verify the packages of ElementWebTestSetup, PersonManager and TLSecurityDeviceManager against the
// imports of TestAccessParentCheck and copy them from there.

/**
 * Test for reading and resolving the <code>&lt;access-parent&gt;</code> setting of a
 * {@link SecurityConfigurationService.TLClassAccessRights} entry.
 */
@SuppressWarnings("javadoc")
public class TestAccessParentConfig extends BasicTestCase {

	private static final String MODULE = "TestSecurityCoverage";

	public void testContainerWithReference() throws Exception {
		AccessParentFunction parent = resolveOk("ExplicitPart",
			"<access-parent><container reference='TestSecurityCoverage:Covered#explicitParts'/></access-parent>");
		assertEquals("TestSecurityCoverage:Covered#explicitParts",
			TLModelUtil.qualifiedName(((ContainerRelation) parent).composition()));
	}

	public void testContainerWithoutReference() throws Exception {
		assertSame(ContainerRelation.ANY, resolveOk("AnyContainerPart", "<access-parent><container/></access-parent>"));
	}

	public void testTarget() throws Exception {
		AccessParentFunction parent = resolveOk("Linked",
			"<access-parent><target reference='TestSecurityCoverage:Linked#target'/></access-parent>");
		assertEquals("TestSecurityCoverage:Linked#target",
			TLModelUtil.qualifiedName(((TargetRelation) parent).reference()));
	}

	public void testSelf() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		assertNull(resolve(log, "SelfDeciding", "<access-parent><self/></access-parent>"));
		assertFalse(log.toString(), log.hasErrors());
	}

	public void testCompositionNotHoldingTheType() throws Exception {
		assertResolveError("Linked",
			"<access-parent><container reference='TestSecurityCoverage:Covered#explicitParts'/></access-parent>",
			"is not a composition holding objects of the type");
	}

	public void testTargetMultiple() throws Exception {
		// Covered#explicitParts is multiple.
		assertResolveError("Covered",
			"<access-parent><target reference='TestSecurityCoverage:Covered#explicitParts'/></access-parent>",
			"is not a to-one reference of the type");
	}

	public void testTargetWithoutReference() {
		assertParseError("<class name='TestSecurityCoverage:Linked'><access-parent><target/></access-parent></class>");
	}

	public void testEmptyAccessParent() {
		assertParseError("<class name='TestSecurityCoverage:Linked'><access-parent/></class>");
	}

	public void testOldAttributeForm() {
		assertParseError("<class name='TestSecurityCoverage:Linked' access-parent='container'/>");
	}

	public void testDelegatingParentWithGrantsIsRejected() throws Exception {
		SecurityConfigurationService.TLClassAccessRights entry = read("<class name='TestSecurityCoverage:Linked'>"
			+ "<access-parent><target reference='TestSecurityCoverage:Linked#target'/></access-parent>"
			+ "<grant operation='Read' roles='TestSecurityCoverage.Reader'/></class>");
		ConstraintChecker checker = new ConstraintChecker();
		try {
			checker.check(entry);
			fail("AccessParentStandsAlone must reject an access parent together with grants.");
		} catch (ConfigurationException ex) {
			// expected
		}
	}

	private AccessParentFunction resolveOk(String typeName, String accessParentXml) throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		AccessParentFunction result = resolve(log, typeName, accessParentXml);
		assertFalse(log.toString(), log.hasErrors());
		assertNotNull(result);
		return result;
	}

	private void assertResolveError(String typeName, String accessParentXml, String expectedMessagePart)
			throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		assertNull(resolve(log, typeName, accessParentXml));
		assertTrue(log.toString(), log.hasErrors());
		assertTrue(log.toString(), log.toString().contains(expectedMessagePart));
	}

	private AccessParentFunction resolve(BufferingProtocol log, String typeName, String accessParentXml)
			throws Exception {
		SecurityConfigurationService.TLClassAccessRights entry = read(
			"<class name='" + MODULE + ":" + typeName + "'>" + accessParentXml + "</class>");
		DefaultInstantiationContext context = new DefaultInstantiationContext(log);
		AccessParentDefinition definition = context.getInstance(entry.getAccessParent().getDefinition());
		return definition.resolve(context, (TLClass) TLModelUtil.findType(MODULE + ":" + typeName));
	}

	private static void assertParseError(String xml) {
		try {
			read(xml);
			fail("Expected a configuration error for: " + xml);
		} catch (ConfigurationException | ConfigurationError ex) {
			// expected
		}
	}

	private static SecurityConfigurationService.TLClassAccessRights read(String xml) throws ConfigurationException {
		return TypedConfiguration.parse("class", SecurityConfigurationService.TLClassAccessRights.class,
			CharacterContents.newContent(xml));
	}

	public static Test suite() {
		Test suite = ServiceTestSetup.createSetup(new TestSuite(TestAccessParentConfig.class),
			TLSecurityDeviceManager.Module.INSTANCE, PersonManager.Module.INSTANCE);
		return ElementWebTestSetup.createElementWebTestSetup(suite);
	}

}
```

`TypedConfiguration.parse(String rootTag, Class<T>, Content)` reads one `<class>` entry; `ConstraintChecker.check(ConfigurationItem)` throws a `ConfigurationException` when a constraint (here `AccessParentStandsAlone`) fails. If `BufferingProtocol` has no `hasErrors()`, use the error accessor it offers (`grep -n "public" com.top_logic.basic/src/main/java/com/top_logic/basic/BufferingProtocol.java`).

- [ ] **Step 2: Run the tests**

```bash
mvn -B test -DskipTests=false -pl com.top_logic.element -Dsurefire.failIfNoSpecifiedTests=false \
  -Dtest=test.com.top_logic.element.boundsec.manager.coverage.TestAccessParentConfig 2>&1 | tee com.top_logic.element/target/mvn-test.log | grep -E "Tests run:|FAIL|BUILD"
```

Expected: 10 tests, 0 failures. A failing `testOldAttributeForm` or `testEmptyAccessParent` means the old form or an empty element is silently accepted: fix the configuration (e.g. `@Mandatory` on `AccessParentConfig.getDefinition()` must be honoured; an unknown attribute on `<class>` must be an error), not the test.

- [ ] **Step 3: Commit (after showing the diff to the user)**

```bash
git add com.top_logic.element/src/test/java/test/com/top_logic/element/boundsec/manager/coverage/TestAccessParentConfig.java
git commit -m "Ticket #0: Test reading and resolving the <access-parent> setting, including the rejected old attribute form.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>"
```

---

### Task 3: Script implementation

**Files:**
- Create: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/rules/ScriptAccessParent.java`
- Create: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/rules/I18NConstants.java` only if the package has none (check first: `ls com.top_logic.model.search/src/main/java/com/top_logic/model/search/rules/`); the class needs no I18N key otherwise.
- Modify: `com.top_logic.model.search/deploy/local/webapp/WEB-INF/model/TestTLScriptSecurity.model.xml`, `.../kbase/TestTLScriptSecurityMeta.xml`, `.../conf/TestTLScriptSecurity-test.config.xml`
- Test: `com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/rules/TestScriptAccessParent.java`

**Interfaces:**
- Consumes: `AccessParentDefinition`, `AccessParentFunction` (Task 1).
- Produces: `ScriptAccessParent.Config` (`@TagName("script")`, property `expr`, `Expr getExpr()` / `setExpr(Expr)`), `ScriptAccessParent implements AccessParentDefinition, AccessParentFunction`.

- [ ] **Step 1: Extend the test model**

In `TestTLScriptSecurity.model.xml` add before `</types>`:

```xml
				<class name="ProjectTask">
					<annotations>
						<table name="TLSecProjectTask"/>
					</annotations>
					<attributes>
						<property name="name"
							type="tl.core:String"
						/>
						<!-- The employee the task is assigned to; the access parent is the project
							this employee is responsible for (backwards over a non-composition
							reference, through an object the user may not read). -->
						<reference name="assignee"
							type="Employee"
						/>
						<!-- Free projects for the result handling tests. -->
						<reference name="candidates"
							multiple="true"
							type="Project"
						/>
					</attributes>
				</class>
```

In `TestTLScriptSecurityMeta.xml` add:

```xml
		<metaobject
			object_name="TLSecProjectTask"
			super_class="TLObject"
		>
			<attributes>
				<mo_attribute
					att_name="name"
					att_type="String"
					mandatory="false"
				/>
			</attributes>
		</metaobject>
```

In `TestTLScriptSecurity-test.config.xml` add inside `<security-config>`:

```xml
					<class name="TestTLScriptSecurity:ProjectTask">
						<access-parent>
							<script expr="t -> $t.get(`TestTLScriptSecurity:ProjectTask#assignee`).referers(`TestTLScriptSecurity:Project#responsible`)"/>
						</access-parent>
					</class>
```

- [ ] **Step 2: Write the failing test**

`TestScriptAccessParent` extends the setup of `TestTLScriptSecurity` - read its `setUp`/`tearDown`/helpers (`createPerson`, project and employee creation, the role rule giving `ProjectResponsible` to `Project#responsibleAccount`, `allowed(...)` style checks via `ModelAccessRights.getInstance().isAllowed(person, object, group)`) and copy the parts needed; `suite()` identical to `TestTLScriptSecurity.suite()`.

Fixture: person `responsible` (holds `ProjectResponsible` on `_project` through `Project#responsibleAccount`), person `other` (no role); employee `_employee` (account `responsible`), `_project` with `responsible = _employee`, `responsibleAccount = responsible`; second project `_project2`; task `_task` with `assignee = _employee`; task `_freeTask` without assignee.

Tests (each a method):

```java
	public void testScriptDelegatesToProject() {
		assertTrue("The task follows the project its assignee is responsible for.",
			allowed(_responsible, _task, SimpleBoundCommandGroup.READ));
		assertFalse(allowed(_other, _task, SimpleBoundCommandGroup.READ));
	}

	public void testScriptNavigatesThroughUnreadableObject() {
		assertFalse("Precondition: the employee itself is not readable.",
			allowed(_responsible, _employee, SimpleBoundCommandGroup.READ));
		assertTrue("The script runs without security, so it reaches the project through the employee.",
			allowed(_responsible, _task, SimpleBoundCommandGroup.READ));
	}

	public void testEmptyResultDenies() {
		assertFalse(allowed(_responsible, _freeTask, SimpleBoundCommandGroup.READ));
	}

	public void testSingleObject() {
		assertSame(_project, function("t -> " + projectRef(_project)).resolve(_task));
	}

	public void testCollectionOfOne() {
		assertSame(_project, function("t -> list(" + projectRef(_project) + ")").resolve(_task));
	}

	public void testEmptyCollection() {
		assertNull(function("t -> list()").resolve(_task));
		// no error logged: checked with the log listener, see below
	}

	public void testSeveralObjectsDenyAndLog() {
		assertNull(expectError(() -> function("t -> list(" + projectRef(_project) + ", " + projectRef(_project2) + ")").resolve(_task)));
	}

	public void testObjectWithNullDeniesAndLogs() {
		assertNull(expectError(() -> function("t -> list(" + projectRef(_project) + ", null)").resolve(_task)));
	}

	public void testNonObjectDeniesAndLogs() {
		assertNull(expectError(() -> function("t -> 42").resolve(_task)));
	}

	public void testExceptionDeniesAndLogs() {
		ScriptAccessParent failing = function("t -> throw('broken')");
		assertNull(expectError(() -> failing.resolve(_task)));
		assertSame("Another object is still decided after a failure.", _project,
			function("t -> " + projectRef(_project)).resolve(_freeTask));
	}

	public void testTypeLevelCheckCountsScriptTypeAsAccessible() {
		TLClass taskType = (TLClass) TLModelUtil.findType("TestTLScriptSecurity:ProjectTask");
		assertTrue(ModelAccessRights.getInstance().getAccessibleTypes(_other, SimpleBoundCommandGroup.READ)
			.contains(taskType));
		assertFalse("The object check still denies.", allowed(_other, _task, SimpleBoundCommandGroup.READ));
	}
```

Helpers:
- `ScriptAccessParent function(String expr)`: `ScriptAccessParent.Config c = TypedConfiguration.newConfigItem(ScriptAccessParent.Config.class); c.setExpr(ExprFormat.INSTANCE.getValue("expr", expr)); return (ScriptAccessParent) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(c);`
- `String projectRef(TLObject project)`: a TL-Script expression yielding exactly that project, e.g. `all(\`TestTLScriptSecurity:Project\`).filter(p -> $p.get(\`TestTLScriptSecurity:Project#name\`) == '<name>').singleElement()` - give both projects unique names in the fixture.
- `Object expectError(Supplier<?> action)`: runs the action while capturing `Logger` output, asserts at least one ERROR entry from `ScriptAccessParent`, returns the action's result. Use the logger capture mechanism the module's tests already use (`git grep -n "LoggerListener\|TestingLogger\|LogCapture\|Logger.addListener" com.top_logic.basic/src/test com.top_logic.model.search/src/test | head`); if there is none, use `test.com.top_logic.basic.AssertProtocol`-style `Logger` replacement found by the same grep.

Run: `mvn -B test -DskipTests=false -pl com.top_logic.model.search -Dsurefire.failIfNoSpecifiedTests=false -Dtest=test.com.top_logic.model.search.rules.TestScriptAccessParent`
Expected: compile error, `ScriptAccessParent` does not exist.

- [ ] **Step 3: Implement `ScriptAccessParent`**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.rules;

import java.util.Collection;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.security.AccessParentDefinition;
import com.top_logic.model.security.AccessParentFunction;

/**
 * {@link AccessParentDefinition} computing the access parent of an object by a TL-Script function.
 *
 * <p>
 * The function receives the object and yields its access parent. A result of exactly one object, or
 * a collection holding exactly one object, is the access parent. <code>null</code> or an empty
 * collection means that the object has no access parent and is therefore not accessible. Any other
 * result - several objects, a value that is no object - and a failure of the function deny access,
 * too, and are logged as an error: the author of the function is responsible for it yielding at most
 * one object.
 * </p>
 *
 * <p>
 * The function is evaluated without access checks, so that it can navigate through objects the
 * current user may not read and does not recurse into the access check it is part of. Since access
 * decisions are not kept beyond an interaction and a revision, the function may use any navigation,
 * derived attributes included.
 * </p>
 */
public class ScriptAccessParent extends AbstractConfiguredInstance<ScriptAccessParent.Config>
		implements AccessParentDefinition, AccessParentFunction {

	/**
	 * Configuration of {@link ScriptAccessParent}.
	 */
	@TagName("script")
	@Label("Script")
	public interface Config extends PolymorphicConfiguration<ScriptAccessParent> {

		/** Configuration name for {@link #getExpr()}. */
		String EXPR = "expr";

		/**
		 * Function computing the access parent of the object it receives.
		 */
		@Name(EXPR)
		@Mandatory
		Expr getExpr();

		/**
		 * Setter for {@link #getExpr()}.
		 */
		void setExpr(Expr value);
	}

	private final QueryExecutor _function;

	private final String _source;

	/**
	 * Creates a {@link ScriptAccessParent} from configuration.
	 */
	public ScriptAccessParent(InstantiationContext context, Config config) {
		super(context, config);
		_source = ExprFormat.INSTANCE.getSpecification(config.getExpr());
		QueryExecutor function;
		try {
			function = QueryExecutor.compile(config.getExpr());
			function.disableSecurity();
		} catch (RuntimeException ex) {
			context.error("Invalid access parent script: " + _source, ex);
			function = null;
		}
		_function = function;
	}

	/**
	 * @return This instance, the function being independent of the type it is configured for.
	 */
	@Override
	public AccessParentFunction resolve(InstantiationContext context, TLClass type) {
		return _function == null ? null : this;
	}

	@Override
	public TLObject resolve(TLObject object) {
		Object result;
		try {
			result = _function.execute(object);
		} catch (RuntimeException ex) {
			Logger.error("Access parent script failed for " + object + " of type " + object.tType() + ": " + _source,
				ex, ScriptAccessParent.class);
			return null;
		}
		if (result == null) {
			return null;
		}
		if (result instanceof TLObject parent) {
			return parent;
		}
		if (result instanceof Collection<?> collection) {
			if (collection.isEmpty()) {
				return null;
			}
			if (collection.size() == 1 && collection.iterator().next() instanceof TLObject parent) {
				return parent;
			}
		}
		Logger.error("Access parent script yields no single object for " + object + " of type " + object.tType()
			+ ", access is denied: " + result + " (" + _source + ")", ScriptAccessParent.class);
		return null;
	}

	/**
	 * @return <code>null</code>: the types of the computed access parents are not known statically.
	 */
	@Override
	public Set<TLClass> getParentTypes(TLClass type) {
		return null;
	}

	@Override
	public ResKey getLabel() {
		return ResKey.text(_source);
	}

}
```

If `ExprFormat` has no public `getSpecification(Expr)`, use the public method of `AbstractConfigurationValueProvider` that formats a value (`git grep -n "public .* getSpecification" com.top_logic.basic/src/main/java/com/top_logic/basic/config/AbstractConfigurationValueProvider.java`).

- [ ] **Step 4: Run the tests**

```bash
mvn -B install -pl com.top_logic.model.search 2>&1 | tee com.top_logic.model.search/target/mvn-build.log | grep -E "^\[ERROR\]|BUILD"
mvn -B test -DskipTests=false -pl com.top_logic.model.search -Dsurefire.failIfNoSpecifiedTests=false \
  -Dtest=test.com.top_logic.model.search.rules.TestScriptAccessParent,test.com.top_logic.model.search.expr.TestTLScriptSecurity \
  2>&1 | tee com.top_logic.model.search/target/mvn-test.log | grep -E "Tests run:|FAIL|BUILD"
```

Expected: both classes green (`TestTLScriptSecurity` checks that the model extension broke nothing).

- [ ] **Step 5: Commit (after showing the diff to the user)**

```bash
git add com.top_logic.model.search/src com.top_logic.model.search/deploy/local/webapp/WEB-INF
git commit -m "Ticket #0: Let a TL-Script function determine the access parent of a type (<access-parent><script expr=\"...\"/></access-parent>), evaluated without security; several or non-object results and failures deny access and are logged.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>"
```

---

### Task 4: Documentation, dialog check, migration text

**Files:**
- Modify: `docs/faq/access-configuration.md`

- [ ] **Step 1: Update the FAQ**

Replace every `access-parent="..."`/`access-reference="..."` example by the element form (table from spec section 4) and add a section "Computed access parent (TL-Script)":

```markdown
### Computed access parent (TL-Script)

Where none of the fixed relations fits - several steps, a backward navigation over a reference that
is no composition, a multi-valued or derived attribute - a TL-Script function computes the access
parent:

```xml
<class name="demo.tickets:Task">
	<access-parent>
		<script expr="t -> $t.get(`demo.tickets:Task#milestone`).container()"/>
	</access-parent>
</class>
```

- The function receives the object and yields its access parent: one object, or a collection
  holding exactly one object.
- `null` or an empty collection: no access parent, so nobody but the technical admin may access the
  object. Not logged.
- Several objects, a value that is no object, or a failure of the function: access is denied and an
  error is logged. The author of the function must make sure it yields at most one object, also
  when it navigates a multi-valued reference.
- The function is evaluated without access checks: it can navigate through objects the user may
  not read.
- A check that depends on no concrete object (the commands of a type) counts a type with a computed
  access parent as accessible; the check of each object follows the function.
- The coverage view shows such a type as delegated, the script text in the access parent column.
```

- [ ] **Step 2: Manual check in the React demo**

Rebuild and restart: `mvn -B install -pl com.top_logic,com.top_logic.element,com.top_logic.model.search,com.top_logic.layout.view,com.top_logic.demo.react`, then restart `com.top_logic.demo.react` with the `tl-app` skill. Ask the user to check (no browser access in this environment):
1. Administration > Zugriffskontrolle > Abdeckung: `Milestone`/`Note` show "Container über tl.demo.projectManagement:ProjectScope#..." and status "Delegiert".
2. Select a type, "Zugriffsrechte...": the group "Zugriffselternobjekt" offers Container, Referenzziel, Eigene Entscheidung, Script; the reference choice of Container/Referenzziel lists only fitting references of the selected type (proves the `@Container` steps of the options).
3. Set a script for a demo type, save, "Konfiguration anwenden": the type shows "Delegiert" and the script text; a non-admin user sees exactly the objects whose computed parent he may read.

- [ ] **Step 3: Prepare the migration text for the ticket**

Write the `== Migration ==` section (Trac wiki syntax) with the table of spec section 4 and the hint at `WEB-INF/autoconf/com.top_logic.model.security.SecurityConfigurationService.config.xml`, and hand it to the user; it goes into the ticket description with keyword `RequiresMigration` once a ticket exists.

- [ ] **Step 4: Commit (after showing the diff to the user)**

```bash
git add docs/faq/access-configuration.md
git commit -m "Ticket #0: Document the <access-parent> element and the computed (TL-Script) access parent.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>"
```
