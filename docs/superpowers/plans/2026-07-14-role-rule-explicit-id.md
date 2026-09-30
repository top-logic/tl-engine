# RoleRule explicit configured `id` — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the computed `RoleRule` id with an explicit, mandatory `id` on `RoleRuleConfig` that keys `RoleRulesConfig#rules` and is UUID-prefilled on UI creation.

**Architecture:** `RoleRuleConfig` gains a `@Mandatory` + `@ValueInitializer(UUIDInitializer.class)` `id`. `RoleRulesConfig#getRules()` becomes `@Key(RoleRuleConfig.ID)`. `RoleRulesImporter` composes each runtime `RoleRule` id from `configId` + role(s); `RoleRule.computeId`, `SingletonRule.computeId` and the whole `PathElement.appendId` chain are deleted. Every existing `<rule>`/`<inheritance-rule>`/`<singleton-rule>` in the repo gets an explicit id.

**Tech Stack:** Java (ISO-8859-1 sources), TopLogic TypedConfiguration, Maven, JUnit 4.

## Global Constraints

- Member fields are `_camelCase`; no `this.` for fields (see CLAUDE.md). (Existing rule classes predate this and use non-prefixed fields — match the surrounding file, do not reformat.)
- Build only via Maven from the project root with `-pl <module>` (never `cd`, never `-am`, never global `~/.m2`). Preserve build logs with `tee`.
- Never pass `-Dmaven.javadoc.skip=true` when touching `I18NConstants` or config properties whose labels are generated.
- Commit messages: `Ticket #29221: <description>` — no AI attribution. Work continues on the existing branch `CWS/CWS_29221` (this id change is part of the #29221 rule rework); do not create a new branch.
- `id` values must be unique within a single `RoleRulesConfig` file.

---

## id assignment table (used by Task 4 and Task 1)

Derivation: files with `resource-key` → last segment after `.`; files without → unqualified meta-element + `_` + primary-role-simple-name; singleton-rules (no meta-element) → role simple name; collisions get `_2`.

**Test fixtures (resource-key scheme):**

| File | rule (line) | id |
|---|---|---|
| `com.top_logic.model.search/src/test/webapp/WEB-INF/init/InitialTestMERoleRules.xml` | L7 | `assignedResponsible` |
| | L17 | `assignedMember` |
| | L28 | `inheritedResponsible` |
| | L43 | `subResponsible` |
| | L56 | `allUsers` |
| | L67 | `mainUsers` |
| | L79 | `observer` |
| | L91 | `projectObserver` |
| | L103 | `dependendResponsible` |
| | L114 | `dependendMember` |
| `.../roleRules/ValidRoleRules.xml` | L7 | `minimal` |
| | L17 | `simple` |
| | L27 | `forward` |
| | L38 | `backward` |
| `.../roleRules/InvalidAttributeRoleRules.xml` | L7 | `simple` |
| `.../roleRules/InvalidMetaElementRoleRules.xml` | L7 | `invalidMetaElement` |
| `.../roleRules/InvalidPathMetaElementRoleRules.xml` | L7 | `unknownPathMetaElement` |
| `.../roleRules/InvalidRoleRoleRules.xml` | L7 | `invalidRole` |

**Shipped / app configs (meta-element+role scheme):**

| File | rule (line) | id |
|---|---|---|
| `com.top_logic.element/src/main/webapp/WEB-INF/conf/mandatorStructure.config.xml` | L23 rule | `Mandator.all_navigation` |
| `com.top_logic.demo/.../autoconf/DemoSecurity.model.config.xml` | L48 singleton | `User` |
| | L55 singleton | `SecurityAdministrator` |
| `com.top_logic.demo/.../conf/InitialRoleRules.config.xml` | L9 rule | `WithOwner_OwnerRole` |
| | L19 rule | `DemoSecurity.A_Role1` |
| | L29 rule | `DemoSecurity.A_Role2` |
| | L39 inheritance | `DemoSecurity.A_SecurityAdministrator` |
| | L52 inheritance | `DemoSecurity.B_SecurityAdministratorB` |
| | L61 inheritance | `DemoSecurity.All_selection` |
| | L68 inheritance | `DemoSecurity.All_navigation` |
| | L75 inheritance | `DemoSecurityElementContainer_navigation` |
| | L85 inheritance | `DemoTypes.All_navigation` |
| | L92 inheritance | `DemoTypesContainer_navigation` |
| | L102 inheritance | `DemoTypes.All_Administrator` |
| | L115 inheritance | `DemoTypes.All_selection` |
| | L122 rule | `DemoTypes.A_Responsible` |
| | L131 rule | `DemoTypes.A_Responsible_2` |
| | L145 inheritance | `DemoTypes.B_BAdministrator` |
| | L153 rule | `DemoPlain.All_Administrator` |
| | L160 singleton | `Administrator` |
| | L167 singleton | `BAdministrator` |
| `com.top_logic.demo/.../conf/DemoConf.config.xml` | L170 singleton | `OwnerRole` |
| `com.top_logic.doc.app/.../conf/tl-doc-app.conf.config.xml` | L45 singleton | `Admin` |
| | L52 singleton | `DocumentationReader` |
| | L59 singleton | `DocumentationWriter` |
| `com.top_logic.contact/.../conf/orgStructureConf.config.xml` | L47 rule | `OrgUnit.all_navigation` |
| | L56 rule | `OrgUnit.all_navigation_2` |
| `com.top_logic.bpe.app/.../conf/tl-bpe-app.conf.config.xml` | L47 singleton | `user` |
| | L54 singleton | `superuser` |
| `com.top_logic.model.search/deploy/local/webapp/WEB-INF/conf/TestScriptPathElement-test.config.xml` | L25 rule | `Assignment_Writer` |
| | L33 rule | `Assignment_Reader` |
| | L41 inheritance | `Assembly_Reader` |

Add `id="<value>"` as the **first** attribute of each element. Keep the file's existing indentation/formatting.

---

## Task 1: Config — add mandatory `id`, key the rules, fix element-module tests

**Files:**
- Modify: `com.top_logic.element/src/main/java/com/top_logic/element/boundsec/manager/rule/config/RoleRuleConfig.java`
- Modify: `com.top_logic.element/src/main/java/com/top_logic/element/boundsec/manager/rule/config/RoleRulesConfig.java`
- Modify (add `id`): the 6 test fixtures in the table above under `com.top_logic.element/src/test/webapp/WEB-INF/xml/roleRules/*.xml`
- Modify: `com.top_logic.element/src/test/java/test/com/top_logic/element/boundsec/manager/rule/TestRoleRulesImporter.java`

**Interfaces:**
- Produces: `RoleRuleConfig.ID` (`String` constant `"id"`), `RoleRuleConfig.getId()` / `setId(String)`; `RoleRulesConfig#getRules()` keyed by `id`.

- [ ] **Step 1: Add the `id` property to `RoleRuleConfig`**

Add imports:
```java
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.ValueInitializer;
import com.top_logic.layout.form.values.edit.initializer.UUIDInitializer;
```
Add the constant next to the other `XML_ATTRIBUTE_*` constants:
```java
	/** Name of the value of {@link #getId()} in the configuration. */
	String ID = "id";
```
Add the property (place the getter first, before `getPathElements()`):
```java
	/**
	 * Stable, unique identifier of this rule within its {@link RoleRulesConfig}.
	 *
	 * <p>
	 * On creation in a form the value is prefilled with a generated id; in a
	 * configuration file it must be specified explicitly.
	 * </p>
	 */
	@Name(RoleRuleConfig.ID)
	@Mandatory
	@ValueInitializer(UUIDInitializer.class)
	String getId();

	/** @see #getId() */
	void setId(String id);
```
Add `RoleRuleConfig.ID` as the first entry of `@DisplayOrder`:
```java
@DisplayOrder({
	RoleRuleConfig.ID,
	RoleRuleConfig.XML_ATTRIBUTE_META_ELEMENT,
	RoleRuleConfig.XML_ATTRIBUTE_ROLE,
	RoleRuleConfig.XML_ATTRIBUTE_INHERIT,
	RoleRuleConfig.XML_ATTRIBUTE_TYPE,
	RoleRuleConfig.XML_ATTRIBUTE_SOURCE_META_ELEMENT,
	RoleRuleConfig.XML_ATTRIBUTE_SOURCE_ROLE,
	RoleRuleConfig.XML_ATTRIBUTE_RESOURCE_KEY,
	RoleRuleConfig.XML_TAG_PATH_ELEMENT,
})
```

- [ ] **Step 2: Key the rules list in `RoleRulesConfig`**

Add import `import com.top_logic.basic.config.annotation.Key;` and annotate:
```java
	@Name(RoleRulesConfig.XML_TAG_RULES)
	@EntryTag(RoleRulesConfig.XML_TAG_RULE)
	@Key(RoleRuleConfig.ID)
	@DefaultContainer
	List<RoleRuleConfig> getRules();
```

- [ ] **Step 3: Add `id` to the 6 element test fixtures**

Per the table, insert `id="..."` as the first attribute of the single `<rule>` in each `Invalid*RoleRules.xml`, and of each of the 4 `<rule>`s in `ValidRoleRules.xml`. Example for `ValidRoleRules.xml` L7:
```xml
		<rule
			id="minimal"
			inherit="true"
			meta-element="projElement:projElement.All"
			resource-key="roleRule.minimal"
			role="testRole"
		>
```

- [ ] **Step 4: Build the element module and run the importer test to discover shifted locations**

Run:
```bash
mvn -B install -pl com.top_logic.element -DskipTests=false \
  -Dtest=test.com.top_logic.element.boundsec.manager.rule.TestRoleRulesImporter \
  2>&1 | tee com.top_logic.element/target/mvn-t1.log
```
Expected: `testInvalidPathMetaElementRules` and `testInvalidAttributeRules` FAIL with a location mismatch (the id line shifted the reported line by +1). Read the actual expected `(line, col)` from the failure output.

- [ ] **Step 5: Update the two hard-coded locations in `TestRoleRulesImporter`**

In `testInvalidPathMetaElementRules` change `LocationImpl.location(..., 14, 44)` and in `testInvalidAttributeRules` change `LocationImpl.location(..., 17, 7)` to the exact `(line, col)` reported by Step 4 (adding the `id` line before the `<path>` shifts the line by +1 → expect `15, 44` and `18, 7`; use the values actually reported).

- [ ] **Step 6: Re-run the element importer test — all green**

Run the same command as Step 4. Expected: PASS (all `TestRoleRulesImporter` tests).

- [ ] **Step 7: Commit**
```bash
git add com.top_logic.element/src/main/java/com/top_logic/element/boundsec/manager/rule/config/RoleRuleConfig.java \
        com.top_logic.element/src/main/java/com/top_logic/element/boundsec/manager/rule/config/RoleRulesConfig.java \
        com.top_logic.element/src/test/webapp/WEB-INF/xml/roleRules/ \
        com.top_logic.element/src/test/java/test/com/top_logic/element/boundsec/manager/rule/TestRoleRulesImporter.java
git commit -m "Ticket #29221: Add mandatory, UUID-prefilled id to RoleRuleConfig and key the rules by it."
```

---

## Task 2: Runtime — compose the instance id from config id + role, drop `computeId`

**Files:**
- Modify: `com.top_logic.element/src/main/java/com/top_logic/element/boundsec/manager/RoleRulesImporter.java`
- Modify: `com.top_logic.element/src/main/java/com/top_logic/element/boundsec/manager/rule/DefaultRoleRule.java`
- Modify: `com.top_logic.element/src/main/java/com/top_logic/element/boundsec/manager/rule/SingletonRule.java`
- Modify: `com.top_logic.element/src/main/java/com/top_logic/element/boundsec/manager/rule/RoleRule.java`

**Interfaces:**
- Consumes: `RoleRuleConfig.getId()` (Task 1).
- Produces: `DefaultRoleRule(TLClass, TLClass, boolean, BoundRole, BoundRole, List<PathElement>, Type, ResKey, String id)`, `SingletonRule(TLObject, BoundRole, List<PathElement>, ResKey, String id)`; `RoleProvider.getId()` now returns `configId + "_" + role.getName() (+ "=" + sourceRole.getName())`.

- [ ] **Step 1: Add id parameter to `DefaultRoleRule`, drop `computeId` call**

Change the constructor signature and super call:
```java
	public DefaultRoleRule(TLClass aME, TLClass aSourceME, boolean isInherit, BoundRole aRole, BoundRole aSourceRole,
			List<PathElement> aPath, Type aType, ResKey aResourceKey, String id) {
		super(aRole, aPath, aResourceKey, id);
		this.metaElement = Objects.requireNonNull(aME);
		this.sourceMetaElement = aSourceME;
		this.inherit = isInherit;
		this.sourceRole = aSourceRole;
		this.type = aType;
	}
```

- [ ] **Step 2: Add id parameter to `SingletonRule`, drop its `computeId`**

Replace the constructor and delete the private `computeId(...)` (keep the private `getType(TLObject)` helper):
```java
	public SingletonRule(TLObject singleton, BoundRole aRole, List<PathElement> aPath, ResKey aResourceKey, String id) {
		super(aRole, aPath, aResourceKey, id);
		_singleton = singleton;
		_singletonType = getType(singleton);
	}
```
Remove the now-unused imports `java.io.IOError`, `java.io.IOException`, `java.util.Iterator`, `com.top_logic.basic.TLID`.

- [ ] **Step 3: Delete `RoleRule.computeId`**

Remove the entire static `computeId(...)` method (lines ~69-102). Remove the now-unused imports `java.io.IOError` and `java.io.IOException` (keep `java.util.Iterator` — still used by `getContent`/`getContentBackwards`).

- [ ] **Step 4: Compose the id in `RoleRulesImporter.createRules`**

Add a private helper and pass composed ids to the constructors:
```java
	private static String composeId(String configId, BoundRole role, BoundRole sourceRole) {
		StringBuilder result = new StringBuilder(configId);
		result.append('_').append(role.getName());
		if (sourceRole != null) {
			result.append('=').append(sourceRole.getName());
		}
		return result.toString();
	}
```
Rewrite `createRules(...)` bodies to use `roleRuleConfig.getId()`:
```java
	private void createRules(RoleRuleConfig roleRuleConfig, BoundedRole sourceRole, Collection<BoundedRole> roles,
			List<PathElement> path) {
		String configId = roleRuleConfig.getId();
		if (roleRuleConfig instanceof SingletonRuleConfig singletonRuleConf) {
			TLObject singleton;
			try {
				singleton = TLModelUtil.resolveQualifiedName(singletonRuleConf.getTarget());
			} catch (Exception ex) {
				addProblem(I18NConstants.INVALID_SINGLETON__NAME.fill(singletonRuleConf.getTarget()));
				return;
			}
			if (singleton == null) {
				addProblem(I18NConstants.INVALID_SINGLETON__NAME.fill(singletonRuleConf.getTarget()));
				return;
			}
			for (BoundedRole role : roles) {
				addRule(new SingletonRule(singleton, role, path, _resKey, composeId(configId, role, null)));
			}
		} else {
			for (BoundedRole role : roles) {
				if (Type.inheritance.equals(roleRuleConfig.getType())) {
					addRule(new DefaultRoleRule(_metaElement, _sourceMetaElement, _inherit, role, sourceRole, path,
						Type.inheritance, _resKey, composeId(configId, role, sourceRole)));
				} else {
					addRule(new DefaultRoleRule(_metaElement, null, _inherit, role, null, path, Type.reference,
						_resKey, composeId(configId, role, null)));
				}
			}
		}
	}
```

- [ ] **Step 5: Build the element module + run the rule tests**

Run:
```bash
mvn -B install -pl com.top_logic.element -DskipTests=false \
  -Dtest=test.com.top_logic.element.boundsec.manager.rule.TestRoleRulesImporter \
  2>&1 | tee com.top_logic.element/target/mvn-t2.log
```
Expected: BUILD SUCCESS, tests PASS. (No `computeId`/`appendId` compile references remain in element.)

- [ ] **Step 6: Commit**
```bash
git add com.top_logic.element/src/main/java/com/top_logic/element/boundsec/manager/RoleRulesImporter.java \
        com.top_logic.element/src/main/java/com/top_logic/element/boundsec/manager/rule/DefaultRoleRule.java \
        com.top_logic.element/src/main/java/com/top_logic/element/boundsec/manager/rule/SingletonRule.java \
        com.top_logic.element/src/main/java/com/top_logic/element/boundsec/manager/rule/RoleRule.java
git commit -m "Ticket #29221: Derive RoleRule instance id from the configured id instead of computing it."
```

---

## Task 3: Remove the dead `PathElement.appendId` chain

**Files:**
- Modify: `com.top_logic.element/.../rule/PathElement.java`
- Modify: `com.top_logic.element/.../rule/IdentityPathElement.java`
- Modify: `com.top_logic.element/.../rule/GroupWithName.java`
- Modify: `com.top_logic.element/.../rule/PathNavigation.java`
- Modify: `com.top_logic.model.search/.../rules/PathByExpression.java`

**Interfaces:**
- Consumes: nothing (the only callers, the two `computeId` methods, were removed in Task 2).
- Produces: `PathElement` interface without `appendId`.

- [ ] **Step 1: Remove `appendId` from the interface**

In `PathElement.java` delete the `appendId(Appendable)` method declaration and its Javadoc (lines ~86-99). Keep `appendForTooltip`. Keep `import java.io.IOException;` (still thrown by `appendForTooltip`).

- [ ] **Step 2: Remove the four `appendId` implementations**

Delete the `@Override public void appendId(Appendable out)` methods in `IdentityPathElement`, `GroupWithName`, `PathNavigation`, and `PathByExpression`. Keep each class's `appendForTooltip`. In `PathByExpression`, keep the private `searchAsString()` helper (used by `appendForTooltip`) and remove the imports that become unused: `java.security.MessageDigest`, `java.security.NoSuchAlgorithmException`, `java.nio.charset.StandardCharsets`, `java.util.Base64` (remove only those that the compiler reports as unused).

- [ ] **Step 3: Build both modules**

Run:
```bash
mvn -B install -pl com.top_logic.element,com.top_logic.model.search 2>&1 | tee com.top_logic.model.search/target/mvn-t3.log
```
Expected: BUILD SUCCESS (no unused-import or missing-method errors).

- [ ] **Step 4: Commit**
```bash
git add com.top_logic.element/src/main/java/com/top_logic/element/boundsec/manager/rule/PathElement.java \
        com.top_logic.element/src/main/java/com/top_logic/element/boundsec/manager/rule/IdentityPathElement.java \
        com.top_logic.element/src/main/java/com/top_logic/element/boundsec/manager/rule/GroupWithName.java \
        com.top_logic.element/src/main/java/com/top_logic/element/boundsec/manager/rule/PathNavigation.java \
        com.top_logic.model.search/src/main/java/com/top_logic/model/search/rules/PathByExpression.java
git commit -m "Ticket #29221: Remove the now-unused PathElement.appendId id-contribution chain."
```

---

## Task 4: Add `id` to all shipped / app role-rule configs

**Files (all "Modify"):**
- ~~`com.top_logic.element/src/main/webapp/WEB-INF/conf/mandatorStructure.config.xml`~~ — **already done in Task 1** (needed to keep element tests green); do not touch again.
- `com.top_logic.demo/src/main/webapp/WEB-INF/autoconf/DemoSecurity.model.config.xml`
- `com.top_logic.demo/src/main/webapp/WEB-INF/conf/InitialRoleRules.config.xml`
- `com.top_logic.demo/src/main/webapp/WEB-INF/conf/DemoConf.config.xml`
- `com.top_logic.doc.app/src/main/webapp/WEB-INF/conf/tl-doc-app.conf.config.xml`
- `com.top_logic.contact/src/main/webapp/WEB-INF/conf/orgStructureConf.config.xml`
- `com.top_logic.bpe.app/src/main/webapp/WEB-INF/conf/tl-bpe-app.conf.config.xml`
- `com.top_logic.model.search/deploy/local/webapp/WEB-INF/conf/TestScriptPathElement-test.config.xml`
- `com.top_logic.model.search/src/test/webapp/WEB-INF/init/InitialTestMERoleRules.xml` (test fixture — uses the resource-key ids from the first table)

**Interfaces:** none (data only).

- [ ] **Step 1: Insert `id` per the assignment tables**

For every `<rule>`, `<inheritance-rule>`, and `<singleton-rule>` in the shipped/app files, add `id="<value>"` as the first attribute, using the exact values from the "Shipped / app configs" table above. Preserve each file's indentation.

Also add the resource-key-derived ids to `InitialTestMERoleRules.xml` (10 rules, values in the "Test fixtures" table) — it is required by `TestElementAccessManager` (Task 6 Step 2). Example L67:
```xml
		<rule
			id="mainUsers"
			inherit="false"
			meta-element="projElement:Project"
			resource-key="roleRule.mainUsers"
			role="projElement.Hauptbenutzer"
		>
```

- [ ] **Step 2: Build the affected modules**

Run (dependency order):
```bash
mvn -B install -pl com.top_logic.element,com.top_logic.contact,com.top_logic.demo,com.top_logic.doc.app,com.top_logic.bpe.app,com.top_logic.model.search \
  2>&1 | tee com.top_logic.demo/target/mvn-t4.log
```
Expected: BUILD SUCCESS. (This does not fully parse the runtime configs; that happens in Task 6.)

- [ ] **Step 3: Commit**
```bash
git add com.top_logic.demo/src/main/webapp/WEB-INF/ \
        com.top_logic.doc.app/src/main/webapp/WEB-INF/conf/tl-doc-app.conf.config.xml \
        com.top_logic.contact/src/main/webapp/WEB-INF/conf/orgStructureConf.config.xml \
        com.top_logic.bpe.app/src/main/webapp/WEB-INF/conf/tl-bpe-app.conf.config.xml \
        com.top_logic.model.search/deploy/local/webapp/WEB-INF/conf/TestScriptPathElement-test.config.xml \
        com.top_logic.model.search/src/test/webapp/WEB-INF/init/InitialTestMERoleRules.xml
git commit -m "Ticket #29221: Add explicit id to all shipped and fixture role-rule configurations."
```

---

## Task 5: Negative tests — missing id and duplicate id are rejected

**Files:**
- Create: `com.top_logic.element/src/test/webapp/WEB-INF/xml/roleRules/MissingIdRoleRules.xml`
- Create: `com.top_logic.element/src/test/webapp/WEB-INF/xml/roleRules/DuplicateIdRoleRules.xml`
- Modify: `com.top_logic.element/src/test/java/test/com/top_logic/element/boundsec/manager/rule/TestRoleRulesImporter.java`

**Interfaces:**
- Consumes: `@Mandatory`/`@Key` on the config (Tasks 1).

- [ ] **Step 1: Write the two fixtures**

`MissingIdRoleRules.xml` (rule without `id`):
```xml
<?xml version="1.0" encoding="utf-8" ?>

<role-rules config:interface="com.top_logic.element.boundsec.manager.rule.config.RoleRulesConfig"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
>
	<rules>
		<rule
			inherit="true"
			meta-element="projElement:projElement.All"
			resource-key="roleRule.minimal"
			role="testRole"
		>
			<path>
				<step attribute="projElement:projElement.All#Verantwortlicher"/>
			</path>
		</rule>
	</rules>
</role-rules>
```
`DuplicateIdRoleRules.xml` (two rules with the same `id`):
```xml
<?xml version="1.0" encoding="utf-8" ?>

<role-rules config:interface="com.top_logic.element.boundsec.manager.rule.config.RoleRulesConfig"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
>
	<rules>
		<rule
			id="dup"
			inherit="true"
			meta-element="projElement:projElement.All"
			resource-key="roleRule.minimal"
			role="testRole"
		>
			<path>
				<step attribute="projElement:projElement.All#Verantwortlicher"/>
			</path>
		</rule>
		<rule
			id="dup"
			inherit="true"
			meta-element="projElement:projElement.All"
			resource-key="roleRule.simple"
			role="testRole"
		>
			<path>
				<step attribute="projElement:projElement.All#Mitarbeiter"/>
			</path>
		</rule>
	</rules>
</role-rules>
```

- [ ] **Step 2: Write the failing tests**

`getRoleRulesConfig` uses `AssertProtocol`, which throws on a reported error. Add to `TestRoleRulesImporter`:
```java
	private final static String ROLE_RULES_MISSING_ID = "/WEB-INF/xml/roleRules/MissingIdRoleRules.xml";

	private final static String ROLE_RULES_DUPLICATE_ID = "/WEB-INF/xml/roleRules/DuplicateIdRoleRules.xml";

	public void testMissingIdRejected() throws Exception {
		try {
			getRoleRulesConfig(ROLE_RULES_MISSING_ID);
			fail("A rule without a mandatory 'id' must be rejected.");
		} catch (Throwable expected) {
			// Expected: mandatory 'id' missing.
		}
	}

	public void testDuplicateIdRejected() throws Exception {
		try {
			getRoleRulesConfig(ROLE_RULES_DUPLICATE_ID);
			fail("Two rules with the same 'id' must be rejected by the @Key uniqueness.");
		} catch (Throwable expected) {
			// Expected: duplicate key 'dup'.
		}
	}
```

- [ ] **Step 3: Run to verify they pass**

Run:
```bash
mvn -B install -pl com.top_logic.element -DskipTests=false \
  -Dtest=test.com.top_logic.element.boundsec.manager.rule.TestRoleRulesImporter \
  2>&1 | tee com.top_logic.element/target/mvn-t5.log
```
Expected: PASS including the two new tests. (If either fixture is accepted without error, the `@Mandatory`/`@Key` from Task 1 is missing — go back and verify.)

- [ ] **Step 4: Commit**
```bash
git add com.top_logic.element/src/test/webapp/WEB-INF/xml/roleRules/MissingIdRoleRules.xml \
        com.top_logic.element/src/test/webapp/WEB-INF/xml/roleRules/DuplicateIdRoleRules.xml \
        com.top_logic.element/src/test/java/test/com/top_logic/element/boundsec/manager/rule/TestRoleRulesImporter.java
git commit -m "Ticket #29221: Add negative tests for missing and duplicate role-rule id."
```

---

## Task 6: Regenerate labels, run the search tests, manual verification

**Files:**
- Modify (generated): `com.top_logic.element/src/main/java/META-INF/messages_*.properties`

- [ ] **Step 1: Regenerate the message files for the new `id` label**

Run (must NOT skip javadoc/doclet):
```bash
mvn -B install -pl com.top_logic.element 2>&1 | tee com.top_logic.element/target/mvn-msg.log
git diff --stat com.top_logic.element/src/main/java/META-INF/messages_en.properties
```
Expected: a new key for the `id` property label appears in `messages_en.properties`. Review the German `messages_de.properties` seed and correct it if awkward (e.g. `ID`).

- [ ] **Step 2: Run the ElementAccessManager tests (search module)**

Run:
```bash
mvn -B install -pl com.top_logic.model.search -DskipTests=false \
  -Dtest=test.com.top_logic.model.search.rules.TestElementAccessManager \
  2>&1 | tee com.top_logic.model.search/target/mvn-eam.log
```
Expected: PASS (it loads `InitialTestMERoleRules.xml`, now with ids from Task 1's sibling table / Task 4). If it FAILS on a missing id, that fixture (`InitialTestMERoleRules.xml`) still lacks ids — add them per the table.

- [ ] **Step 3: Commit the regenerated messages**
```bash
git add com.top_logic.element/src/main/java/META-INF/messages_en.properties \
        com.top_logic.element/src/main/java/META-INF/messages_de.properties
git commit -m "Ticket #29221: Regenerate messages for the new RoleRuleConfig id label."
```

- [ ] **Step 4: Manual verification with the demo app (per CLAUDE.md)**

Start `com.top_logic.demo` (tl-app skill). Log in as `root` / `root1234`. Verify with Playwright:
  1. The application starts without a role-rule configuration error (proves Task 4 ids parse — `InitialRoleRules.config.xml`, `DemoSecurity.model.config.xml`, `DemoConf.config.xml` all load).
  2. Security still resolves (open an object whose access depends on a role rule; confirm the expected group/role assignment is unchanged from before the change).
  3. Where role rules are editable through the generic configuration form, adding a new rule shows the `id` field prefilled with a generated id.

Note: `InitialTestMERoleRules.xml` (file 9) is a **test** fixture — its ids are added as part of the search-module fixtures. If Task 1 only covered the element-module fixtures, ensure file 9 got its ids too (it is listed in the assignment table).

---

## Notes / risks (carry into execution)

- **Keyed-list merge semantics:** `@Key` changes `getRules()` from a positional append list to a keyed collection. If any application layers role-rule configs across multiple files/overlays, entries now merge/override by `id`. None of the 14 files observed rely on positional merge, but re-check if a downstream app defines rules in an overlay.
- **`InitialTestMERoleRules.xml` ownership:** it lives in `com.top_logic.model.search` but is listed with the test-fixture ids. Add its ids together with Task 1 (it is a standalone `<role-rules>` config), and ensure the search-module build sees them before Task 6 Step 2.
- **Line-shift assertions:** only `TestRoleRulesImporter` hard-codes `(line, col)`. After any re-indentation of the invalid fixtures, re-derive those constants.
- **`DemoSecurity.model.config.xml` lives under `autoconf/`:** confirm this file is hand-maintained and not regenerated by a build step. If it is generated, the generator (not the file) must emit the `id`; adjust the source of truth accordingly before editing by hand.
