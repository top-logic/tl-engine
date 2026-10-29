# Discover Right Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the object-level right `Discover` of the model based security (ticket #29782) in the security core, TL-Script and the React view layer.

**Architecture:** `Discover` is a new built-in command group. `SecurityConfigurationService` derives the roles holding `Discover` on a type from every other grant (R2) and lets attribute grants replace the class grant (R3). TL-Script filters every object source and the script inputs for `Discover` through the existing `SearchExpression.filterSecurity` helper (whose criterion becomes `Discover`), checks new reference targets on writes, and evaluates label scripts unsecured. The React view layer filters navigation structures (already routed through `filterSecurity`) and masks undiscoverable elements in reference fields.

**Tech Stack:** Java 17, TopLogic typed configuration, TL-Script (`com.top_logic.model.search`), React view layer (`com.top_logic.layout.view`, `com.top_logic.layout.react`), JUnit 4 suites, Maven.

**Spec:** `com.top_logic/src/main/java/META-INF/tl-docs/access-discover-spec.md` (rules R1-R12). Read it before every task.

## Global Constraints

- Base is `master`; PR !1824 (#29453) stays untouched. It changes `All`, `DynamicAll`, `KBQuery`, `Copy`, `GenericDescendingVisitor`, `CreateTableAccess`, `FilterCompiler` in the same way as Task 5 (with `Read`); whichever merges second resolves the conflict.
- Branch `CWS/CWS_29782_discover_spec`. Commit messages `Ticket #29782: <description>.` - no `Co-Authored-By` trailer. Commits are GPG-signed: run `git commit -m "..."` as its own Bash call (never `cd … && git commit`, never `--no-gpg-sign`). Show the user the diff and wait for approval before every commit.
- Build from the project root with explicit `-pl` (no `-am`), batch mode and a log: `mvn -B install -pl <modules> 2>&1 | tee <module>/target/mvn-build.log`.
- Tests: `mvn -B test -DskipTests=false -pl <module> -Dtest=<fully.qualified.TestClass> 2>&1 | tee <module>/target/mvn-test.log`. `Class#method` does not work for suite tests.
- Every new `.java` file: SPDX header (copy from a neighbour, current year 2026) and a class JavaDoc comment. Member fields start with `_`.
- JavaDoc: reference symbols with `{@link}`, not `{@code}`.
- `messages_en.properties` are generated - never edit by hand; never pass `-Dmaven.javadoc.skip=true` when adding `I18NConstants`. `messages_de.properties` entries seeded by the build may be corrected by hand.
- `WEB-INF/conf/resources/Messages_*.properties` (command group labels) are hand-maintained and must stay normalized (see the `tl-model` skill, "Normalizing resource files", mandatory `-N`).
- Edited layout or view XML must be `XMLPrettyPrinter`-normalized.
- The `Discover` operation id is exactly `Discover`; the masked placeholder text is exactly `*****`.

## Review Focus

1. **Bypassing users** (technical admin, system context, `UncheckedAccessRights`) must still see every object and every unmasked label - pinned in Task 1 (`testAdminDiscoversEverything`) and Task 9 (`testAdminSeesUnmasked`).
2. **Objects created in the current transaction** (not yet committed) are not checked by `isAllowed` and must stay visible to the creating script - pinned in Task 4 (`testUncommittedObjectNotFiltered`).
3. **Nested script inputs** (a list or map containing undiscoverable objects passed as an argument) must be filtered recursively, not just top-level - pinned in Task 6 (`testNestedInputFiltered`).
4. **Ordered reference with masked elements in the middle**: removing or moving a visible element in a field must keep the masked ones in their place and write them back - pinned in Task 9 (`testMaskedElementsWrittenBack`, `testMaskedSortedLast`).
5. **Compiled (database) evaluation must equal interpreted evaluation** for `all(...)` with filters - pinned in Task 5 (`testCompiledFilterOnAll`), the `eval` helper of `AbstractSearchExpressionTest` asserts both.

---

## File Structure

| File | Responsibility |
|---|---|
| `com.top_logic/.../tool/boundsec/simple/SimpleBoundCommandGroup.java` | constant `DISCOVER` |
| `com.top_logic/.../tool/boundsec/simple/CommandGroupRegistry.java` | registers `DISCOVER` |
| `com.top_logic/src/main/webapp/WEB-INF/conf/resources/Messages_{en,de}.properties` | label/tooltip `commandGroup.Discover` |
| `com.top_logic/.../model/security/ModelAccessRights.java` | `isDiscoverAllowed(...)`, JavaDoc of R2/R3 |
| `com.top_logic/.../model/security/SecurityConfigurationService.java` | Discover roles (R2), attribute replacement (R3) |
| `com.top_logic.element/.../boundsec/manager/coverage/SecurityCoverageAnalysis.java` | a type is covered if any grant implies Discover |
| `com.top_logic.model.search/.../expr/SearchExpression.java` | `filterSecurity` criterion `Discover`, helper `discoverable(...)` |
| `com.top_logic.model.search/.../expr/{AccessLike,Referers,AssociationNavigation}.java`, `.../config/operations/{Container,ObjectFunctions,TLScriptMethod}.java` | filter what they deliver (R4) |
| `com.top_logic.model.search/.../expr/{All,DynamicAll,KBQuery,SearchExpressionFactory}.java`, `.../compile/transform/{CreateTableAccess,FilterCompiler}.java`, `.../config/operations/AllBuilder.java`, `.../visit/{Copy,GenericDescendingVisitor}.java`, `.../compile/eval/CompiledContext.java` | security flag + filter for enumeration (R4) |
| `com.top_logic.model.search/.../expr/query/QueryExecutor.java` | filter script inputs (R4) |
| `com.top_logic.model.search/.../expr/{Update,DynamicSet,Add,Remove}.java`, `.../expr/I18NConstants.java` | `Discover` on new reference targets (R5, R7), visible index for `add` (R10) |
| `com.top_logic.model.search/.../providers/{LabelByExpression,LabelProviderByExpression,TooltipProviderByExpression,TitleByExpression,ConstraintCheckByExpression}.java` | R12 |
| `com.top_logic.model.search/deploy/local/webapp/WEB-INF/{model/TestTLScriptSecurity.model.xml,kbase/TestTLScriptSecurityMeta.xml,conf/TestTLScriptSecurity-test.config.xml}` | path fixture Root -> A -> B -> C |
| `com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/expr/TestDiscoverRight.java` | tests of R1-R12 in TL-Script and the core |
| `com.top_logic.layout.view/.../security/ModelAccessPolicy.java`, `.../form/{AttributeRowSetBinding,TLObjectOverlay}.java`, `.../table/ColumnProviderService.java` | R3 in the view layer, R4 for composition rows, R7 on save, masked cell labels |
| `com.top_logic.layout.react/.../control/select/ReactDropdownSelectControl.java`, `.../I18NConstants.java` | masking in reference fields (R11) |
| `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/form/TestMaskedReferenceField.java` | tests of R11 |
| `com.top_logic/src/main/java/META-INF/tl-docs/access-configuration.md`, `access-discover-spec.md` | documentation |
| TL-Script help pages under `com.top_logic.model.search/src/main/webapp/doc/{en,de}/DeveloperGuide/TLScript/ModelOperations/{all,filterSecurity,get,referers}/index.html` | documentation |

---

### Task 1: `Discover` operation and Discover roles in the core (R1, R2)

**Files:**
- Modify: `com.top_logic/src/main/java/com/top_logic/tool/boundsec/simple/SimpleBoundCommandGroup.java` (name constants ~38-59, instances ~110-137)
- Modify: `com.top_logic/src/main/java/com/top_logic/tool/boundsec/simple/CommandGroupRegistry.java` (constructor, `addProgrammatic` calls)
- Modify: `com.top_logic/src/main/webapp/WEB-INF/conf/resources/Messages_en.properties`, `Messages_de.properties` (next to `commandGroup.Read`, ~2465)
- Modify: `com.top_logic/src/main/java/com/top_logic/model/security/ModelAccessRights.java` (next to `isReadAllowed`, ~249-293)
- Modify: `com.top_logic/src/main/java/com/top_logic/model/security/SecurityConfigurationService.java` (fields ~346-392, constructor ~402-429, `getRoles` ~852, `getAllowedRoles(TLClass, ..)` ~873)
- Modify: `com.top_logic.model.search/deploy/local/webapp/WEB-INF/model/TestTLScriptSecurity.model.xml`, `kbase/TestTLScriptSecurityMeta.xml`, `conf/TestTLScriptSecurity-test.config.xml`
- Create: `com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/expr/TestDiscoverRight.java`

**Interfaces:**
- Produces: `SimpleBoundCommandGroup.DISCOVER_NAME` (`"Discover"`), `SimpleBoundCommandGroup.DISCOVER` (`BoundCommandGroup`, `CommandGroupType.READ`); `ModelAccessRights#isDiscoverAllowed(TLObject)`, `ModelAccessRights#isDiscoverAllowed(Person, TLObject)`; `getAllowedRoles(TLClass, DISCOVER)` returns the derived Discover roles. Test fixture fields of `TestDiscoverRight` (`_root`, `_a1`, `_a2`, `_b1`, `_b2`, `_c1`, `_c2`, `_pathUser`, `_stranger`) used by all later TL-Script tasks.

- [ ] **Step 1: Add the fixture model**

In `TestTLScriptSecurity.model.xml`, inside `<types>` of module `TestTLScriptSecurity`, add:

```xml
				<class name="PathNode">
					<annotations>
						<table name="TLSecPathNode"/>
					</annotations>
					<attributes>
						<property name="name"
							type="tl.core:String"
						/>
					</attributes>
				</class>

				<class name="PathRoot">
					<generalizations>
						<generalization type="PathNode"/>
					</generalizations>
					<attributes>
						<reference name="as"
							multiple="true"
							ordered="true"
							type="PathA"
						/>
					</attributes>
				</class>

				<class name="PathA">
					<generalizations>
						<generalization type="PathNode"/>
					</generalizations>
					<attributes>
						<reference name="bs"
							multiple="true"
							ordered="true"
							type="PathB"
						/>
					</attributes>
				</class>

				<class name="PathB">
					<generalizations>
						<generalization type="PathNode"/>
					</generalizations>
					<attributes>
						<reference name="cs"
							multiple="true"
							ordered="true"
							type="PathC"
						/>
						<reference name="notes"
							composite="true"
							multiple="true"
							type="PathNote"
						/>
						<reference name="writers"
							multiple="true"
							type="Employee"
						/>
					</attributes>
				</class>

				<class name="PathC">
					<generalizations>
						<generalization type="PathNode"/>
					</generalizations>
					<attributes>
						<reference name="readers"
							multiple="true"
							type="Employee"
						/>
					</attributes>
				</class>

				<class name="PathNote">
					<generalizations>
						<generalization type="PathNode"/>
					</generalizations>
				</class>
```

In `kbase/TestTLScriptSecurityMeta.xml`, after `TLSecProjectTask`, add:

```xml
		<metaobject
			object_name="TLSecPathNode"
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

- [ ] **Step 2: Add the fixture security configuration**

In `conf/TestTLScriptSecurity-test.config.xml`: add the roles to `InitialRolesManager`:

```xml
					<role name="TestTLScriptSecurity.PathReader"/>
					<role name="TestTLScriptSecurity.PathViewer"/>
					<role name="TestTLScriptSecurity.NoteWriter"/>
```

Add to `<security-config>`:

```xml
					<class name="TestTLScriptSecurity:PathC">
						<!-- The reader of a C reads it completely. -->
						<grant
							operation="Read"
							roles="TestTLScriptSecurity.PathReader"
						/>
					</class>
					<class name="TestTLScriptSecurity:PathNote">
						<!-- A note writer may create notes in a B (Create implies Discover on the container B). -->
						<grant
							operation="Create"
							roles="TestTLScriptSecurity.NoteWriter"
						/>
					</class>
					<!-- The path role reads only the structure attributes; by R2 that implies Discover. -->
					<part name="TestTLScriptSecurity:PathRoot#as">
						<grant
							operation="Read"
							roles="TestTLScriptSecurity.PathViewer"
						/>
					</part>
					<part name="TestTLScriptSecurity:PathA#bs">
						<grant
							operation="Read"
							roles="TestTLScriptSecurity.PathViewer"
						/>
					</part>
					<part name="TestTLScriptSecurity:PathB#cs">
						<grant
							operation="Read"
							roles="TestTLScriptSecurity.PathViewer"
						/>
					</part>
```

Add to `<role-rules><rules>` of the `AccessManager`:

```xml
						<rule
							id="3c1f5e0a-9a77-4f0e-9a51-1b7e6a2d4c01"
							meta-element="TestTLScriptSecurity:PathC"
							role="TestTLScriptSecurity.PathReader"
						>
							<path>
								<step attribute="TestTLScriptSecurity:PathC#readers"/>
								<step attribute="TestTLScriptSecurity:Employee#account"/>
							</path>
						</rule>
						<rule
							id="3c1f5e0a-9a77-4f0e-9a51-1b7e6a2d4c02"
							meta-element="TestTLScriptSecurity:PathB"
							role="TestTLScriptSecurity.NoteWriter"
						>
							<path>
								<step attribute="TestTLScriptSecurity:PathB#writers"/>
								<step attribute="TestTLScriptSecurity:Employee#account"/>
							</path>
						</rule>
						<inheritance-rule
							id="3c1f5e0a-9a77-4f0e-9a51-1b7e6a2d4c03"
							meta-element="TestTLScriptSecurity:PathB"
							role="TestTLScriptSecurity.PathViewer"
							source-role="TestTLScriptSecurity.PathReader"
						>
							<path>
								<step attribute="TestTLScriptSecurity:PathB#cs"/>
							</path>
						</inheritance-rule>
						<inheritance-rule
							id="3c1f5e0a-9a77-4f0e-9a51-1b7e6a2d4c04"
							meta-element="TestTLScriptSecurity:PathA"
							role="TestTLScriptSecurity.PathViewer"
							source-role="TestTLScriptSecurity.PathReader"
						>
							<path>
								<step attribute="TestTLScriptSecurity:PathA#bs"/>
								<step attribute="TestTLScriptSecurity:PathB#cs"/>
							</path>
						</inheritance-rule>
						<inheritance-rule
							id="3c1f5e0a-9a77-4f0e-9a51-1b7e6a2d4c05"
							meta-element="TestTLScriptSecurity:PathRoot"
							role="TestTLScriptSecurity.PathViewer"
							source-role="TestTLScriptSecurity.PathReader"
						>
							<path>
								<step attribute="TestTLScriptSecurity:PathRoot#as"/>
								<step attribute="TestTLScriptSecurity:PathA#bs"/>
								<step attribute="TestTLScriptSecurity:PathB#cs"/>
							</path>
						</inheritance-rule>
```

If the configuration parser rejects `<inheritance-rule>` inside `<rules>`, look at `com.top_logic.demo.react/src/main/webapp/WEB-INF/conf/demoReactConf.config.xml:289-332` for the exact nesting and copy it.

- [ ] **Step 3: Write the fixture and the failing tests**

Create `TestDiscoverRight.java` (copy the SPDX header from `TestTLScriptSecurity.java`):

```java
package test.com.top_logic.model.search.expr;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;

import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.knowledge.wrap.person.TestPerson;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.simple.TLSecurityDeviceManager;
import com.top_logic.util.TLContext;

/**
 * Tests the {@link SimpleBoundCommandGroup#DISCOVER Discover} right (ticket #29782) in the security
 * core and in TL-Script, on the path structure {@code PathRoot -> PathA -> PathB -> PathC} of the
 * model {@code TestTLScriptSecurity}.
 *
 * <p>
 * Fixture: {@code root.as = [a1, a2]}, {@code a1.bs = [b1, b2]}, {@code b1.cs = [c1, c2]}. The
 * path user reads {@code c1} only and thereby holds the path role on {@code b1}, {@code a1} and
 * {@code root}.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestDiscoverRight extends AbstractSearchExpressionTest {

	private KnowledgeBase _kb;

	private final Map<Object, KnowledgeItem> _toDelete = new HashMap<>();

	private final UpdateListener _creationListener = (sender, event) -> {
		for (KnowledgeItem item : event.getCreatedObjects().values()) {
			_toDelete.put(item.tId(), item);
		}
	};

	Person _pathUser;

	Person _stranger;

	TLObject _root, _a1, _a2, _b1, _b2, _c1, _c2;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_kb = PersistencyLayer.getKnowledgeBase();
		_kb.addUpdateListener(_creationListener);
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_pathUser = TestPerson.createPerson("discoverPathUser");
			_stranger = TestPerson.createPerson("discoverStranger");

			TLObject reader = newObject("Employee");
			reader.tUpdateByName("account", _pathUser);

			_c1 = node("PathC", "c1");
			_c1.tUpdateByName("readers", Collections.singletonList(reader));
			_c2 = node("PathC", "c2");
			_b1 = node("PathB", "b1");
			_b1.tUpdateByName("cs", Arrays.asList(_c1, _c2));
			_b2 = node("PathB", "b2");
			_a1 = node("PathA", "a1");
			_a1.tUpdateByName("bs", Arrays.asList(_b1, _b2));
			_a2 = node("PathA", "a2");
			_root = node("PathRoot", "root");
			_root.tUpdateByName("as", Arrays.asList(_a1, _a2));
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		becomeUser(PersonManager.getManager().getRoot());
		_kb.removeUpdateListener(_creationListener);
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			KBUtils.deleteAllKI(_toDelete.values().iterator());
			tx.commit();
		}
		_toDelete.clear();
		_kb = null;
		super.tearDown();
	}

	TLObject newObject(String className) {
		TLClass type = (TLClass) TLModelUtil.findType("TestTLScriptSecurity:" + className);
		return DynamicModelService.getFactoryFor("TestTLScriptSecurity").createObject(type);
	}

	TLObject node(String className, String name) {
		TLObject result = newObject(className);
		result.tUpdateByName("name", name);
		return result;
	}

	static ModelAccessRights rights() {
		return ModelAccessRights.getInstance();
	}

	public void testDiscoverIsRegistered() {
		assertSame(SimpleBoundCommandGroup.DISCOVER, CommandGroupRegistry.resolve("Discover"));
		assertTrue(SimpleBoundCommandGroup.DISCOVER.isReadGroup());
	}

	public void testReadImpliesDiscover() {
		assertTrue(rights().isDiscoverAllowed(_pathUser, _c1));
		assertFalse(rights().isDiscoverAllowed(_pathUser, _c2));
	}

	public void testAttributeRightImpliesDiscover() {
		// The path role on b1, a1, root grants only attribute rights.
		assertTrue(rights().isDiscoverAllowed(_pathUser, _b1));
		assertTrue(rights().isDiscoverAllowed(_pathUser, _a1));
		assertTrue(rights().isDiscoverAllowed(_pathUser, _root));
		assertFalse(rights().isReadAllowed(_pathUser, _a1));
	}

	public void testSiblingsNotDiscoverable() {
		assertFalse(rights().isDiscoverAllowed(_pathUser, _a2));
		assertFalse(rights().isDiscoverAllowed(_pathUser, _b2));
		assertFalse(rights().isDiscoverAllowed(_stranger, _root));
	}

	public void testCreateImpliesDiscoverOnContainer() throws Exception {
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			TLObject writer = newObject("Employee");
			writer.tUpdateByName("account", _stranger);
			_b2.tUpdateByName("writers", Collections.singletonList(writer));
			tx.commit();
		}
		assertTrue(rights().isDiscoverAllowed(_stranger, _b2));
		assertFalse(rights().isReadAllowed(_stranger, _b2));
	}

	public void testAdminDiscoversEverything() {
		becomeUser(PersonManager.getManager().getRoot());
		assertTrue(rights().isDiscoverAllowed(_a2));
	}

	public static Test suite() {
		return suite(TestDiscoverRight.class,
			AccessManager.Module.INSTANCE,
			TLSecurityDeviceManager.Module.INSTANCE,
			PersonManager.Module.INSTANCE,
			InitialRolesManager.Module.INSTANCE,
			SecurityConfigurationService.Module.INSTANCE);
	}

}
```

Fix the imports of `TestPerson`, `TLSecurityDeviceManager`, `UpdateListener` by copying them from the import block of `TestTLScriptSecurity.java` (same fixture style); remove unused imports (`UpdateEvent`, `TLContext`) if the compiler warns.

- [ ] **Step 4: Run the tests to verify they fail**

Run:
`mvn -B test -DskipTests=false -pl com.top_logic.model.search -Dtest=test.com.top_logic.model.search.expr.TestDiscoverRight 2>&1 | tee com.top_logic.model.search/target/mvn-test.log`
Expected: compilation FAILS (`cannot find symbol DISCOVER`, `isDiscoverAllowed`).

- [ ] **Step 5: Add the command group**

`SimpleBoundCommandGroup.java`, next to the other name constants:

```java
	/** Name of {@link #DISCOVER}. */
	public static final String DISCOVER_NAME = "Discover";
```

next to the other instances:

```java
	/**
	 * Operation making an object visible: it appears in references, enumerations and option lists
	 * and shows its label, without making any of its attributes readable.
	 *
	 * <p>
	 * Every other right on an object or one of its attributes implies this operation, see
	 * {@link com.top_logic.model.security.ModelAccessRights#isDiscoverAllowed(Person, TLObject)}.
	 * </p>
	 */
	public static final BoundCommandGroup DISCOVER = new SimpleBoundCommandGroup(DISCOVER_NAME, CommandGroupType.READ);
```

(add the imports for `Person` and `TLObject` only if the JavaDoc link requires them; otherwise link the class only.)

`CommandGroupRegistry.java`, in the constructor next to `addProgrammatic(groups, SimpleBoundCommandGroup.READ)`:

```java
		addProgrammatic(groups, SimpleBoundCommandGroup.DISCOVER);
```

`Messages_en.properties` (sorted next to `commandGroup.Delete`):

```
commandGroup.Discover=Discover
commandGroup.Discover.tooltip=See the object in references, enumerations and option lists with its label, without reading its attributes.
```

`Messages_de.properties`:

```
commandGroup.Discover=Entdecken
commandGroup.Discover.tooltip=Das Objekt in Referenzen, Aufzählungen und Auswahllisten mit seiner Beschriftung sehen, ohne seine Attribute zu lesen.
```

Normalize both files as described in the `tl-model` skill ("Normalizing resource files", with `-N`).

- [ ] **Step 6: Add the API in `ModelAccessRights`**

Next to `isReadAllowed(Person, TLObject)`:

```java
	/**
	 * Checks whether the current person may discover the given instance.
	 *
	 * @implSpec Just calls {@link #isDiscoverAllowed(Person, TLObject)} with the current user.
	 */
	default boolean isDiscoverAllowed(TLObject instance) {
		return isDiscoverAllowed(TLContext.currentUser(), instance);
	}

	/**
	 * Checks whether the given person may discover the given instance.
	 *
	 * <p>
	 * A discoverable object appears in references, enumerations and option lists and shows its
	 * label; its attributes are readable only by their own read right. A person holds
	 * {@link SimpleBoundCommandGroup#DISCOVER} on an object if they hold a role on it that is granted
	 * {@link SimpleBoundCommandGroup#DISCOVER}, any other operation on the object's type, any right on
	 * one of its attributes, or the right to create objects in one of its compositions.
	 * </p>
	 *
	 * @implSpec Just calls {@link #isAllowed(Person, TLObject, BoundCommandGroup)} with
	 *           {@link SimpleBoundCommandGroup#DISCOVER}.
	 */
	default boolean isDiscoverAllowed(Person person, TLObject instance) {
		return isAllowed(person, instance, SimpleBoundCommandGroup.DISCOVER);
	}
```

Extend the class JavaDoc (the list of what a right protects, if present) with one item for Discover.

- [ ] **Step 7: Derive the Discover roles in `SecurityConfigurationService`**

Add the field next to `_expandedClassRights`:

```java
	/**
	 * The roles holding {@link SimpleBoundCommandGroup#DISCOVER} on the instances of a class: the
	 * union of all roles of its class rights, of the rights of its attributes, and of the
	 * {@link SimpleBoundCommandGroup#CREATE} rights of the types creatable in its compositions.
	 */
	private final Map<TLClass, Set<BoundedRole>> _discoverRoles = new HashMap<>();
```

At the end of the constructor, after `computeClassRights(...)` and before `inheritAccessParents(...)`, call `computeDiscoverRoles();` and add:

```java
	private void computeDiscoverRoles() {
		for (TLClass type : TLModelUtil.getAllGlobalClasses(_applicationModel)) {
			Set<BoundedRole> roles = new HashSet<>();
			_expandedClassRights.getOrDefault(type, Collections.emptyMap()).values().forEach(roles::addAll);
			for (TLStructuredTypePart part : type.getAllParts()) {
				_typePartRights.getOrDefault(part.getDefinition(), Collections.emptyMap()).values()
					.forEach(roles::addAll);
				if (part instanceof TLReference reference && reference.isComposite()
						&& reference.getType() instanceof TLClass target) {
					for (TLClass created : TLModelUtil.getConcreteSpecializations(target)) {
						roles.addAll(getAllowedRoles(created, SimpleBoundCommandGroup.CREATE));
					}
				}
			}
			if (!roles.isEmpty()) {
				_discoverRoles.put(type, roles);
			}
		}
	}
```

Change `getAllowedRoles(TLClass type, BoundCommandGroup commandGroup)` (~873):

```java
	@Override
	public Set<BoundedRole> getAllowedRoles(TLClass type, BoundCommandGroup commandGroup) {
		if (SimpleBoundCommandGroup.DISCOVER.equals(commandGroup)) {
			return _discoverRoles.getOrDefault(type, Collections.emptySet());
		}
		Map<BoundCommandGroup, Set<BoundedRole>> rights = _expandedClassRights.get(type);
		... unchanged ...
	}
```

In `getRoles(TLObject object, BoundCommandGroup group)` (~852), in the singleton branch, answer the union of all singleton rights when the group is `DISCOVER`:

```java
		Map<BoundCommandGroup, Set<BoundedRole>> singletonRights = _singletonRights.get(object);
		if (singletonRights != null) {
			if (SimpleBoundCommandGroup.DISCOVER.equals(group)) {
				Set<BoundedRole> all = new HashSet<>();
				singletonRights.values().forEach(all::addAll);
				return all;
			}
			... unchanged ...
		}
```

`operationOnParent` passes `DISCOVER` through unchanged - no change needed. `getConcreteSpecializations` is `TLModelUtil.java:1455`; check whether it includes the class itself and add `target` explicitly if not (`if (!target.isAbstract()) roles.addAll(getAllowedRoles(target, CREATE))`).

- [ ] **Step 8: Build and run the tests**

Run: `mvn -B install -pl com.top_logic 2>&1 | tee com.top_logic/target/mvn-build.log` then
`mvn -B test -DskipTests=false -pl com.top_logic.model.search -Dtest=test.com.top_logic.model.search.expr.TestDiscoverRight 2>&1 | tee com.top_logic.model.search/target/mvn-test.log`
Expected: all 6 tests PASS. Also run `-Dtest=test.com.top_logic.model.search.expr.TestTLScriptSecurity` - Expected: PASS (fixture additions do not change existing behaviour).

- [ ] **Step 9: Commit** (after showing the diff and getting approval)

```bash
git add com.top_logic/src/main/java/com/top_logic/tool/boundsec/simple/SimpleBoundCommandGroup.java com.top_logic/src/main/java/com/top_logic/tool/boundsec/simple/CommandGroupRegistry.java com.top_logic/src/main/webapp/WEB-INF/conf/resources/Messages_en.properties com.top_logic/src/main/webapp/WEB-INF/conf/resources/Messages_de.properties com.top_logic/src/main/java/com/top_logic/model/security/ModelAccessRights.java com.top_logic/src/main/java/com/top_logic/model/security/SecurityConfigurationService.java com.top_logic.model.search/deploy/local/webapp/WEB-INF com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/expr/TestDiscoverRight.java
git commit -m "Ticket #29782: Add the Discover operation, implied by every right on an object or its attributes."
```

---

### Task 2: Attribute grants replace the class grant (R3)

**Files:**
- Modify: `com.top_logic/src/main/java/com/top_logic/model/security/SecurityConfigurationService.java` (`isAllowed(Person, TLObject, TLStructuredTypePart, BoundCommandGroup)` ~1110-1153)
- Modify: `com.top_logic/src/main/java/com/top_logic/model/security/ModelAccessRights.java` (JavaDoc of `getAllowedRoles(TLStructuredTypePart, ..)` ~138-158)
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/expr/compile/eval/CompiledContext.java` (`readCheckBypassedByCompilation` ~63-73)
- Modify: `com.top_logic.model.search/src/test/java/test/com/top_logic/model/search/expr/TestTLScriptSecurity.java` (tests encoding the AND semantics, ~985-1010 and ~1108-1116)
- Modify: `com.top_logic/src/main/java/META-INF/tl-docs/access-configuration.md` (step 7, lines ~47-50)
- Test: `TestDiscoverRight.java`

**Interfaces:**
- Consumes: Task 1 fixture.
- Produces: `isAllowed(person, object, attribute, op)` = part roles when the part has a grant for `op`, else the class decision.

- [ ] **Step 1: Write the failing tests** (add to `TestDiscoverRight`)

```java
	public void testAttributeGrantWithoutClassRead() {
		// a1 is not readable as a whole, but its structure attribute is.
		assertFalse(rights().isReadAllowed(_pathUser, _a1));
		assertTrue(rights().isReadAllowed(_pathUser, _a1, part("PathA", "bs")));
		assertFalse(rights().isReadAllowed(_pathUser, _a1, part("PathNode", "name")));
	}

	public void testAttributeGrantOnlyForHolderOfRole() {
		assertFalse(rights().isReadAllowed(_pathUser, _a2, part("PathA", "bs")));
	}

	TLStructuredTypePart part(String className, String partName) {
		return ((TLClass) TLModelUtil.findType("TestTLScriptSecurity:" + className)).getPart(partName);
	}
```

(import `com.top_logic.model.TLStructuredTypePart`.)

- [ ] **Step 2: Run to verify they fail**

Run: `mvn -B test -DskipTests=false -pl com.top_logic.model.search -Dtest=test.com.top_logic.model.search.expr.TestDiscoverRight 2>&1 | tee com.top_logic.model.search/target/mvn-test.log`
Expected: `testAttributeGrantWithoutClassRead` FAILS (class decision denies first).

- [ ] **Step 3: Implement the replacement**

In `isAllowed(Person, TLObject instance, TLStructuredTypePart attribute, BoundCommandGroup commandGroup)` replace the block starting with `boolean allowedOnInstance = decide(...)` by:

```java
		Map<BoundCommandGroup, Set<BoundedRole>> partRights =
			_typePartRights.getOrDefault(attribute.getDefinition(), Collections.emptyMap());
		if (!partRights.containsKey(commandGroup)) {
			// No grant of the attribute itself: the class decision applies.
			return decide(decisionCache(), person, instance, commandGroup);
		}
		// The grants of the attribute replace the class grant for this attribute.
		Set<BoundedRole> requiredPartRoles = partRights.get(commandGroup);
		if (requiredPartRoles.isEmpty()) {
			return false;
		}
		TLObject roleHolder = roleHolder(instance);
		if (!(roleHolder instanceof BoundObject holder)) {
			return false;
		}
		return accessManager().hasRole(person, holder, requiredPartRoles);
```

Rewrite the JavaDoc of `ModelAccessRights#getAllowedRoles(TLStructuredTypePart, BoundCommandGroup)`: the roles granted the operation on the attribute; if non-empty (or present but empty), they **replace** the class grant for this attribute.

In `CompiledContext.readCheckBypassedByCompilation(TLStructuredTypePart part)` replace `!getAllowedRoles(part, READ).isEmpty()` by `ModelAccessRights.getInstance().hasGrant(part, SimpleBoundCommandGroup.READ)` (an empty "deny all" grant must also prevent compilation).

- [ ] **Step 4: Adapt the existing tests**

Run `TestTLScriptSecurity`. Tests asserting the AND semantics (e.g. `testAttributeDeniedForAllRoles`, and the test around line 1108 "Without an attribute entry, the class decision applies") must still pass: a `<part>` grant with no roles denies everyone; a part without grant falls back to the class. Any test that asserts "attribute grant for role X, but X lacks class Read -> denied" must be changed to expect "allowed" and its comment must state R3. Do not change other expectations.

- [ ] **Step 5: Update `access-configuration.md`**

Replace step 7 (lines ~47-50) by:

```markdown
7. For an attribute value: if the attribute has its own `<part>` grants for the operation, they
   **replace** the class grant for this attribute - the user must hold one of those roles, on the
   object or, for a delegating object, on the end of its chain of access parents; a grant without
   roles denies everyone. Without attribute-level grants the class-level decision stands.
```

Add a paragraph after the check list: "`Discover` - every right on an object or one of its attributes implies the operation `Discover`, see [the Discover specification](doc:access-discover-spec)."

- [ ] **Step 6: Build and run**

`mvn -B install -pl com.top_logic,com.top_logic.model.search 2>&1 | tee com.top_logic/target/mvn-build.log`, then run `TestDiscoverRight`, `TestTLScriptSecurity`, `TestSecurityFilterReport` (FQNs in package `test.com.top_logic.model.search.expr`). Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add -u com.top_logic com.top_logic.model.search
git commit -m "Ticket #29782: Let attribute grants replace the class grant for their attribute."
```

---

### Task 3: Coverage check counts every grant implying Discover

**Files:**
- Modify: `com.top_logic.element/src/main/java/com/top_logic/element/boundsec/manager/coverage/SecurityCoverageAnalysis.java` (`analyze(TLClass)` ~171-195)
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/admin/SecurityCoverageTable.java` (problem/solution texts for `NO_READ_GRANT` ~857, ~878 - only their I18N JavaDoc)
- Test: `com.top_logic.element/src/test/java/test/com/top_logic/element/boundsec/manager/coverage/TestSecurityCoverageAnalysis.java`

**Interfaces:**
- Consumes: `getAllowedRoles(TLClass, DISCOVER)` from Task 1.

- [ ] **Step 1: Write the failing test**

In `TestSecurityCoverageAnalysis` add a test using a type of `TestSecurityCoverage.model.xml` that has only a `<part>` Read grant (add such a type and grant to `com.top_logic.element/src/test/webapp/WEB-INF/model/TestSecurityCoverage.model.xml` and `.../conf/element.test.config.xml` next to the existing coverage types: class `PartOnly` with property `label`, `<part name="TestSecurityCoverage:PartOnly#label"><grant operation="Read" roles="<an existing test role of that file>"/></part>`, plus a role rule for it copied from a neighbour type), and assert:

```java
	public void testPartGrantCoversType() {
		TypeCoverage coverage = analysis().analyze(type("PartOnly"));
		assertFalse(coverage.findings().stream().anyMatch(f -> f.kind() == FindingKind.NO_READ_GRANT));
	}
```

Use the helper names that already exist in that test class for `analysis()`/`type(...)`; if they differ, adapt to them.

- [ ] **Step 2: Run to verify it fails**

`mvn -B test -DskipTests=false -pl com.top_logic.element -Dtest=test.com.top_logic.element.boundsec.manager.coverage.TestSecurityCoverageAnalysis 2>&1 | tee com.top_logic.element/target/mvn-test.log` - Expected: FAIL.

- [ ] **Step 3: Implement**

In `analyze(TLClass)` keep `readRoles` (shown in the table) but decide the finding on Discover:

```java
		Set<BoundedRole> readRoles = _accessRights.getAllowedRoles(type, SimpleBoundCommandGroup.READ);
		Set<BoundedRole> discoverRoles = _accessRights.getAllowedRoles(type, SimpleBoundCommandGroup.DISCOVER);
		...
		if (discoverRoles.isEmpty()) {
			findings.add(... NO_READ_GRANT ...);   // unchanged construction
		}
```

Update the I18N JavaDoc of the `NO_READ_GRANT` problem/solution texts in `SecurityCoverageTable` to say "no grant on the type or one of its attributes" (the English messages are generated; check `messages_de.properties` after the build).

- [ ] **Step 4: Build and run**

`mvn -B install -pl com.top_logic.element,com.top_logic.layout.view 2>&1 | tee com.top_logic.element/target/mvn-build.log`, run the test again. Expected: PASS. Also run `test.com.top_logic.element.boundsec.manager.coverage.TestSecurityCoverageCheck`: PASS.

- [ ] **Step 5: Commit**

```bash
git add -u com.top_logic.element com.top_logic.layout.view
git commit -m "Ticket #29782: Count every grant implying Discover in the security coverage check."
```

---

### Task 4: TL-Script object sources filter for Discover (R4)

**Files:**
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/expr/SearchExpression.java` (`securityFilter` ~258, `filterSecurityTLObject` ~367)
- Modify: `.../expr/AccessLike.java` (`lookupValue` ~79-101)
- Modify: `.../expr/Referers.java` (`lookupReferrers` ~78-89)
- Modify: `.../expr/AssociationNavigation.java` (`internalEval` ~104-128)
- Modify: `.../expr/config/operations/Container.java` (`eval` ~46-58)
- Modify: `.../expr/config/operations/ObjectFunctions.java` (~182, ~269)
- Modify: `.../expr/config/operations/TLScriptMethod.java` (`eval` ~130-152)
- Modify: `.../expr/FilterSecurity.java` (class JavaDoc ~20-45)
- Test: `TestDiscoverRight.java`

**Interfaces:**
- Produces: `public static Object SearchExpression.discoverable(Object value, boolean usesSecurity)` - returns `filterSecurity(value)` if `usesSecurity`, else `value`. `filterSecurity(...)` now keeps exactly the discoverable objects.

- [ ] **Step 1: Write the failing tests**

```java
	public void testNavigationDeliversOnlyDiscoverable() {
		becomeUser(_pathUser);
		assertEquals(list(_a1), eval("r -> $r.get(`TestTLScriptSecurity:PathRoot#as`)", _root));
		assertEquals(list(_b1), eval("a -> $a.get(`TestTLScriptSecurity:PathA#bs`)", _a1));
		assertEquals(list(_c1), eval("b -> $b.get(`TestTLScriptSecurity:PathB#cs`)", _b1));
	}

	public void testLabelOfDiscoverableVisibleAttributeNot() {
		becomeUser(_pathUser);
		assertEquals(list("a1"), eval("r -> $r.get(`TestTLScriptSecurity:PathRoot#as`).map(a -> toString($a))", _root));
		assertNull(eval("a -> $a.get(`TestTLScriptSecurity:PathNode#name`)", _a1));
	}

	public void testReferersFiltered() {
		becomeUser(_pathUser);
		assertEquals(list(_b1), eval("c -> $c.referers(`TestTLScriptSecurity:PathB#cs`)", _c1));
	}

	public void testUncommittedObjectNotFiltered() {
		becomeUser(_pathUser);
		Object result = eval("x -> new(`TestTLScriptSecurity:PathC`, transient: true)");
		assertNotNull(result);
	}

	public void testUnsecuredSeesEverything() {
		becomeUser(_pathUser);
		QueryExecutor executor = QueryExecutor.compile(search("r -> $r.get(`TestTLScriptSecurity:PathRoot#as`)"));
		executor.disableSecurity();
		assertEquals(list(_a1, _a2), executor.execute(_root));
	}
```

`list(...)` and `search(...)` exist in `AbstractSearchExpressionTest`/`BasicTestCase`; if `list` is missing use `Arrays.asList`. Check the `new` syntax for transient objects in `TestTLScriptSecurity` (search for `transient`) and copy it exactly.
- [ ] **Step 2: Run to verify they fail**

Run `TestDiscoverRight`. Expected: `testNavigationDeliversOnlyDiscoverable` FAILS (`a1.get(bs)` not readable before - after Task 2 it is readable but delivers `[b1, b2]`); `testReferersFiltered` FAILS.

- [ ] **Step 3: Switch the criterion and add the helper in `SearchExpression`**

In `filterSecurityTLObject` replace `isReadAllowed(user, value)` by `isDiscoverAllowed(user, value)`; same in `securityFilter()`. Update the JavaDoc of all `filterSecurity` overloads: "removes the business objects the user must not discover". Add:

```java
	/**
	 * Restricts a value delivered by an object source to the objects the current user may discover.
	 *
	 * @param value
	 *        The value delivered by the source.
	 * @param usesSecurity
	 *        Whether the delivering expression is evaluated with security, see
	 *        {@link WithSecurityCheck#usesSecurity()}.
	 * @return The value without undiscoverable objects (see {@link #filterSecurity(Object)}), or
	 *         the given value if security is not used.
	 */
	public static Object discoverable(Object value, boolean usesSecurity) {
		return usesSecurity ? filterSecurity(value) : value;
	}
```

- [ ] **Step 4: Filter at every source**

- `AccessLike.lookupValue(TLObject self, TLStructuredTypePart part, boolean withSecurity)`: the read check stays; the returned value becomes `SearchExpression.discoverable(value, withSecurity)`. Replace the comment at ~95-99 ("value returned unfiltered") with "References deliver only discoverable objects (R4 of the Discover specification)".
- `Referers.lookupReferrers(boolean usesSecurity, Object targetValue, TLReference reference)`: return `(Set<? extends TLObject>) SearchExpression.discoverable(self.tReferers(reference), usesSecurity)` - keep the existing read check on the target.
- `AssociationNavigation.internalEval`: wrap the result in `discoverable(result, usesSecurity())`.
- `Container.eval`: return `discoverable(object.tContainer(), usesSecurity())`.
- `ObjectFunctions.resolve` / `resolveKey`: replace `isReadAllowed(result)` by `isDiscoverAllowed(result)`.
- `TLScriptMethod.eval`: wrap the result of `_java.invoke(...)` in `SearchExpression.discoverable(result, usesSecurity())`. This covers every `TLScriptFunctions` subclass, including application functions.
- `FilterSecurity` class JavaDoc: replace the paragraph "Access operations in TL-Script return referenced objects unfiltered ..." by: "Every object source in a secured TL-Script delivers only discoverable objects (see the Discover specification); this function remains as a safety net for values assembled from unsecured sources."

- [ ] **Step 5: Build and run**

`mvn -B install -pl com.top_logic.model.search 2>&1 | tee com.top_logic.model.search/target/mvn-build.log`; run `TestDiscoverRight`, `TestTLScriptSecurity`, `TestSecurityFilterReport`, `test.com.top_logic.model.search.expr.config.operations.TestObjectFunctions`. Expected: new tests PASS. Existing tests that assert "a referenced unreadable object is delivered" must be changed to expect filtering, citing R4 in a comment; `TestSecurityFilterReport` tests that reach unreadable objects through a reference must reach them via an unsecured executor instead.

- [ ] **Step 6: Update the TL-Script help pages**

In `com.top_logic.model.search/src/main/webapp/doc/{en,de}/DeveloperGuide/TLScript/ModelOperations/get/index.html` and `.../referers/index.html` add one sentence: "In a secured evaluation only objects the current user may discover are delivered." (German: "Bei einer Auswertung mit Sicherheit werden nur Objekte geliefert, die der aktuelle Benutzer entdecken darf.") In `filterSecurity/index.html` replace the statement that references are unfiltered accordingly.

- [ ] **Step 7: Commit**

```bash
git add -u com.top_logic.model.search
git commit -m "Ticket #29782: Deliver only discoverable objects from references, referers, container and script functions."
```

---

### Task 5: `all()` and its database form filter for Discover (R4)

**Files:**
- Modify: `.../expr/All.java`, `.../expr/DynamicAll.java`, `.../expr/KBQuery.java`, `.../expr/SearchExpressionFactory.java` (`all` ~39, `query` ~108), `.../expr/config/operations/AllBuilder.java` (~39-41), `.../expr/compile/transform/CreateTableAccess.java` (~50-72), `.../expr/compile/transform/FilterCompiler.java` (~155), `.../expr/visit/Copy.java` (~107, ~130), `.../expr/visit/GenericDescendingVisitor.java` (`visitAll`, `visitKBQuery`)
- Modify: `.../doc/{en,de}/DeveloperGuide/TLScript/ModelOperations/all/index.html`
- Test: `TestDiscoverRight.java`

**Interfaces:**
- Produces: `All extends SearchExpressionWithSecurity`, `All(TLStructuredType, boolean usesSecurity)`; `public static List<? extends TLObject> All.all(SearchExpression self, TLStructuredType type, boolean usesSecurity)`; `DynamicAll extends GenericMethodWithSecurity`, `DynamicAll(String, SearchExpression[], boolean)`; `KBQuery extends SearchExpressionWithSecurity`, `KBQuery(TLClass, SetExpression, List<CompiledValue>, boolean)`; `SearchExpressionFactory.all(TLStructuredType)` (`@NoFactory`, delegates with `true`), `all(TLStructuredType, boolean)`, `query(TLClass, SetExpression, List<CompiledValue>, boolean)`.

- [ ] **Step 1: Write the failing tests**

```java
	public void testAllDeliversOnlyDiscoverable() {
		becomeUser(_pathUser);
		assertEquals(set(_a1), asSet(eval("all(`TestTLScriptSecurity:PathA`)")));
		assertEquals(Integer.valueOf(1), eval("all(`TestTLScriptSecurity:PathA`).size()"));
	}

	public void testDynamicAll() {
		becomeUser(_pathUser);
		assertEquals(set(_a1), asSet(eval("t -> all($t)", TLModelUtil.findType("TestTLScriptSecurity:PathA"))));
	}

	public void testCompiledFilterOnAll() {
		becomeUser(_pathUser);
		assertEquals(set(_b1), asSet(eval("all(`TestTLScriptSecurity:PathB`).filter(b -> $b.get(`TestTLScriptSecurity:PathB#cs`).size() > 0)")));
	}

	public void testUnsecuredAllSeesEverything() {
		becomeUser(_pathUser);
		QueryExecutor executor = QueryExecutor.compile(search("all(`TestTLScriptSecurity:PathA`)"));
		executor.disableSecurity();
		assertEquals(set(_a1, _a2), asSet(executor.execute()));
	}

	static java.util.Set<Object> asSet(Object value) {
		return new java.util.HashSet<>((java.util.Collection<?>) value);
	}
```

(`set(...)` comes from `BasicTestCase`; otherwise build a `HashSet`.)

- [ ] **Step 2: Run to verify they fail**

Run `TestDiscoverRight`. Expected: `testAllDeliversOnlyDiscoverable` FAILS (`[a1, a2]`).

- [ ] **Step 3: Implement the flag and the filter**

`All.java`:

```java
public class All extends SearchExpressionWithSecurity {

	private TLStructuredType _type;

	All(TLStructuredType type, boolean usesSecurity) {
		super(usesSecurity);
		_type = type;
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		return all(this, _type, usesSecurity());
	}

	/**
	 * Retrieves all instances of the given type.
	 *
	 * <p>
	 * The classifiers of a {@link TLEnumeration} are model elements and are delivered unfiltered.
	 * </p>
	 *
	 * @param usesSecurity
	 *        Whether to deliver only the instances the current user may discover.
	 */
	public static List<? extends TLObject> all(SearchExpression self, TLStructuredType type, boolean usesSecurity) {
		switch (type.getModelKind()) {
			case CLASS: {
				... unchanged loop filling result ...
				return usesSecurity ? filterDiscoverable(result) : result;
			}
			... ENUMERATION unchanged ...
		}
	}

	@SuppressWarnings("unchecked")
	static List<TLObject> filterDiscoverable(List<TLObject> instances) {
		return (List<TLObject>) filterSecurity(instances);
	}
```

Update the class JavaDoc: "When the expression uses security, only instances the current user may discover are delivered."

`DynamicAll`: extend `GenericMethodWithSecurity`, constructor `(String name, SearchExpression[] arguments, boolean usesSecurity)`, `copy` passes `usesSecurity()`, `evalDirect` calls `All.all(this, type, usesSecurity())`, its builder creates it with `true`.

`KBQuery`: extend `SearchExpressionWithSecurity`, constructor gains `boolean usesSecurity` (call `super(usesSecurity)`), at the end of `internalEval` return `usesSecurity() ? All.filterDiscoverable(result) : result;`, update the comment at ~89-91.

`SearchExpressionFactory`:

```java
	@NoFactory
	public static All all(TLStructuredType type) {
		return all(type, true);
	}

	public static All all(TLStructuredType type, boolean usesSecurity) {
		return new All(type, usesSecurity);
	}

	public static KBQuery query(TLClass classType, SetExpression query, List<CompiledValue> compiled,
			boolean usesSecurity) {
		return new KBQuery(classType, query, compiled, usesSecurity);
	}
```

(with JavaDoc `@param usesSecurity See {@link All#usesSecurity()}`.)

`AllBuilder` ~41: `new DynamicAll(getConfig().getName(), allArgs, true)`.
`CreateTableAccess` ~67: `query(classType, tableQuery, Collections.emptyList(), expr.usesSecurity())`.
`FilterCompiler` ~158: `SearchExpressionFactory.query(expr.getClassType(), newQuery, newDynamicFilters, expr.usesSecurity())`.
`Copy`: `composeAll` → `all(expr.getInstanceType(), expr.usesSecurity())`; `composeSource(KBQuery)` → `query(..., expr.usesSecurity())`.
`GenericDescendingVisitor`: `visitAll` → `compose(expr, arg, wrap(expr.getInstanceType()), wrap(expr.usesSecurity()))`; `visitKBQuery` adds `wrap(expr.usesSecurity())` as last argument.

`UpdateSecurityVisitor` switches the flag automatically because the classes now implement `WithSecurityCheck`.

Help page `all/index.html` (en/de): "In a secured evaluation only the instances the current user may discover are delivered; a count over `all(...)` counts these only." / "Bei einer Auswertung mit Sicherheit werden nur die Instanzen geliefert, die der aktuelle Benutzer entdecken darf; eine Zählung über `all(...)` zählt nur diese."

- [ ] **Step 4: Build and run**

`mvn -B install -pl com.top_logic.model.search 2>&1 | tee com.top_logic.model.search/target/mvn-build.log`; run `TestDiscoverRight`, `TestTLScriptSecurity`, `TestSecurityFilterReport`. Expected: PASS. Fix every compile error in other modules that call `All.all(self, type)` (search with `grep -rn "All.all(\|new DynamicAll(\|SearchExpressionFactory.query(" --include=*.java .`) by passing the caller's security flag (or `true`).

- [ ] **Step 5: Commit**

```bash
git add -u com.top_logic.model.search
git commit -m "Ticket #29782: Restrict all() and its database form to the discoverable instances in secured TL-Script."
```

---

### Task 6: Script inputs filter for Discover (R4)

**Files:**
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/expr/query/QueryExecutor.java` (`executeWith(EvalContext, Args)` ~405-411)
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/expr/query/Args.java` (helper)
- Test: `TestDiscoverRight.java`

**Interfaces:**
- Produces: `Args Args.map(java.util.function.UnaryOperator<Object> fun)` - a new `Args` with every value mapped.

- [ ] **Step 1: Write the failing tests**

```java
	public void testInputFiltered() {
		becomeUser(_pathUser);
		assertNull(eval("x -> $x", _a2));
	}

	public void testNestedInputFiltered() {
		becomeUser(_pathUser);
		assertEquals(list(_a1), eval("x -> $x", list(_a1, _a2)));
		assertEquals(Integer.valueOf(1), eval("x -> $x.size()", list(_a1, _a2)));
	}
```

- [ ] **Step 2: Run to verify they fail**

Expected: `testNestedInputFiltered` FAILS on the `size()` assertion (2).

- [ ] **Step 3: Implement**

In `Args` add (after `next()`):

```java
	/**
	 * Creates new {@link Args} with every value of these arguments mapped by the given function.
	 */
	public Args map(UnaryOperator<Object> fun) {
		if (isEmpty()) {
			return this;
		}
		return cons(fun.apply(value()), next().map(fun));
	}
```

(adapt to the actual names of `Args`: `isEmpty`, `cons`, `value`, `next` - read the class first.)

In `QueryExecutor.executeWith(EvalContext definitions, Args args)`:

```java
	public final Object executeWith(EvalContext definitions, Args args) {
		if (_securityEnabled) {
			// The inputs of a secured script are restricted to discoverable objects, like every
			// other object source.
			args = args.map(SearchExpression::filterSecurity);
		}
		Object result = internalExecuteWith(definitions, args);
		if (_securityEnabled) {
			return SearchExpression.filterSecurity(result, definitions.getSecurityReport());
		}
		return result;
	}
```

`executeIntermediate` stays unfiltered (its arguments come from an already secured caller).

- [ ] **Step 4: Build and run**

Run `TestDiscoverRight`, `TestTLScriptSecurity`, `TestSecurityFilterReport`, `test.com.top_logic.model.search.expr.config.operations.TestUsesSecurity`. Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add -u com.top_logic.model.search
git commit -m "Ticket #29782: Restrict the inputs of a secured script to discoverable objects."
```

---

### Task 7: Writes check new reference targets; `add` uses the visible index (R5, R7, R10)

**Files:**
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/expr/Update.java` (`internalEval` ~81-91, new static helper next to `checkWritePermission` ~110)
- Modify: `.../expr/DynamicSet.java` (`eval` ~51-62), `.../expr/Add.java` (`eval` ~47-108), `.../expr/I18NConstants.java`
- Modify: TL-Script help for `set`/`add` if a page exists under `.../doc/{en,de}/DeveloperGuide/TLScript/` (search `grep -rl '"set"\|>set<' com.top_logic.model.search/src/main/webapp/doc`); otherwise add the note to the `get` page.
- Test: `TestDiscoverRight.java`

**Interfaces:**
- Produces: `public static void Update.checkNewTargets(TLObject self, TLStructuredTypePart part, Object newValue)` - throws `TopLogicException(I18NConstants.ERROR_TARGET_NOT_DISCOVERABLE__OBJECT_ATTRIBUTE)` if a referenced object not contained in the stored value is not discoverable. Used by Task 9 (view layer save).

- [ ] **Step 1: Write the failing tests**

```java
	public void testSetToUndiscoverableTargetDenied() throws Exception {
		grantWrite(_pathUser, _b1);
		becomeUser(_pathUser);
		assertPermissionDenied(() -> eval("b -> c -> $b.set(`TestTLScriptSecurity:PathB#cs`, list($c))", _b1, _c2));
	}

	public void testSetReplacesIncludingHidden() throws Exception {
		grantWrite(_pathUser, _b1);
		becomeUser(_pathUser);
		eval("b -> $b.set(`TestTLScriptSecurity:PathB#cs`, list())", _b1);
		becomeUser(PersonManager.getManager().getRoot());
		assertEquals(list(), _b1.tValueByName("cs"));
	}

	public void testAddKeepsHidden() throws Exception {
		grantWrite(_pathUser, _b1);
		TLObject c3 = createReadableC("c3");
		becomeUser(_pathUser);
		eval("b -> c -> $b.add(`TestTLScriptSecurity:PathB#cs`, $c)", _b1, c3);
		becomeUser(PersonManager.getManager().getRoot());
		assertEquals(list(_c1, _c2, c3), _b1.tValueByName("cs"));
	}

	public void testAddAtVisibleIndex() throws Exception {
		grantWrite(_pathUser, _b1);
		TLObject c3 = createReadableC("c3");
		becomeUser(_pathUser);
		// The user sees [c1]; index 1 means "after c1" in the visible list.
		eval("b -> c -> $b.add(`TestTLScriptSecurity:PathB#cs`, 1, $c)", _b1, c3);
		becomeUser(PersonManager.getManager().getRoot());
		assertEquals(list(_c1, c3, _c2), _b1.tValueByName("cs"));
	}
```

Helpers in the test (Write on `PathB#cs` for the path user): add to the fixture config `<part name="TestTLScriptSecurity:PathB#cs"><grant operation="Write" roles="TestTLScriptSecurity.NoteWriter"/></part>` and implement

```java
	void grantWrite(Person person, TLObject b) throws Exception {
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			TLObject writer = newObject("Employee");
			writer.tUpdateByName("account", person);
			b.tUpdateByName("writers", Collections.singletonList(writer));
			tx.commit();
		}
	}

	TLObject createReadableC(String name) throws Exception {
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			TLObject reader = newObject("Employee");
			reader.tUpdateByName("account", _pathUser);
			TLObject c = node("PathC", name);
			c.tUpdateByName("readers", Collections.singletonList(reader));
			tx.commit();
			return c;
		}
	}
```

Copy `assertPermissionDenied`, `hasCause` and `ThrowingAction` from `TestTLScriptSecurity` (lines ~1306-1352). The `add` argument order with an index must match `Add`'s 4-argument form - read `Add.Builder` and adjust the scripts.

- [ ] **Step 2: Run to verify they fail**

Expected: `testSetToUndiscoverableTargetDenied` FAILS (no exception), `testAddAtVisibleIndex` FAILS (index refers to the stored list).

- [ ] **Step 3: Implement**

`I18NConstants` (model.search expr package), next to `WRITE_PERMISSION_DENIED__OBJECT_ATTRIBUTE`:

```java
	/**
	 * @en The value of "{1}" of "{0}" cannot be set to an object you may not discover.
	 */
	public static ResKey2 ERROR_TARGET_NOT_DISCOVERABLE__OBJECT_ATTRIBUTE;
```

`Update`:

```java
	/**
	 * Checks that the current user may discover every object the given new value adds to the
	 * reference.
	 *
	 * <p>
	 * Objects already contained in the stored value are not checked: they may stay even if the user
	 * cannot discover them.
	 * </p>
	 */
	public static void checkNewTargets(TLObject self, TLStructuredTypePart part, Object newValue) {
		if (!(part instanceof TLReference)) {
			return;
		}
		Collection<?> stored = CollectionUtil.asSet(self.tValue(part));
		ModelAccessRights rights = ModelAccessRights.getInstance();
		for (Object target : CollectionUtil.asList(newValue)) {
			if (target instanceof TLObject object && !stored.contains(object) && !rights.isDiscoverAllowed(object)) {
				throw new TopLogicException(
					I18NConstants.ERROR_TARGET_NOT_DISCOVERABLE__OBJECT_ATTRIBUTE.fill(self, part));
			}
		}
	}
```

(use the `asSet`/`asList` helpers of `CollectionUtil` or `SearchExpression` that `Add`/`Remove` already use; `asSet(null)` must yield an empty set.)

Call `checkNewTargets(self, part, value)` after `checkWritePermission(self, part)` in `Update.internalEval` and `DynamicSet.eval`, and in `Add.eval` for the added value, all only if `usesSecurity()`.

`Add.eval` with an index and `usesSecurity()`: translate the visible index into the stored index before inserting:

```java
	private static int storedIndex(List<?> stored, int visibleIndex) {
		ModelAccessRights rights = ModelAccessRights.getInstance();
		int visible = 0;
		for (int n = 0; n < stored.size(); n++) {
			Object element = stored.get(n);
			boolean discoverable = !(element instanceof TLObject object) || rights.isDiscoverAllowed(object);
			if (discoverable) {
				if (visible == visibleIndex) {
					return n;
				}
				visible++;
			}
		}
		return stored.size();
	}
```

Help text for `set`: "`set` replaces the whole value - also elements the user cannot discover are removed. Use `add`/`remove` to change single elements; `$o.set(ref, $o.get(ref).concat($x))` removes all undiscoverable elements." (German accordingly.)

- [ ] **Step 4: Build and run**

`mvn -B install -pl com.top_logic.model.search 2>&1 | tee com.top_logic.model.search/target/mvn-build.log` (without skipping javadoc - the I18N constant is generated); run `TestDiscoverRight`, `TestTLScriptSecurity`. Expected: PASS. Check the seeded German entry of the new key in `com.top_logic.model.search/src/main/java/META-INF/messages_de.properties` and correct it ("Der Wert von „{1}“ von „{0}“ kann nicht auf ein Objekt gesetzt werden, das Sie nicht entdecken dürfen.").

- [ ] **Step 5: Commit**

```bash
git add -u com.top_logic.model.search
git commit -m "Ticket #29782: Require Discover on new reference targets and interpret the index of add() in the visible list."
```

---

### Task 8: Label scripts unsecured, constraint messages masked (R12)

**Files:**
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/providers/LabelByExpression.java` (~94-95), `LabelProviderByExpression.java` (~55), `TooltipProviderByExpression.java` (~68), `TitleByExpression.java` (compile site), `ConstraintCheckByExpression.java` (`toResKey` ~123-135)
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/I18NConstants.java` (or the providers' `I18NConstants`) - constant for the masked object
- Test: `TestDiscoverRight.java`

**Interfaces:**
- Produces: `ResKey I18NConstants.MASKED_OBJECT` (`@en *****`) in `com.top_logic.model.search.providers` - not used elsewhere.

- [ ] **Step 1: Write the failing tests**

```java
	public void testLabelScriptUnsecured() throws Exception {
		LabelProviderByExpression provider = labelProvider("x -> $x.get(`TestTLScriptSecurity:PathNode#name`)");
		becomeUser(_pathUser);
		assertEquals("a1", provider.getLabel(_a1));
	}

	public void testConstraintMessageMasksUndiscoverable() {
		ResKey message = ConstraintCheckByExpression.toResKey(
			com.top_logic.basic.util.ResKey.message(TEST_KEY, _a2));
		becomeUser(_pathUser);
		ResKey masked = ConstraintCheckByExpression.maskUndiscoverable(message);
		assertFalse(java.util.Arrays.asList(masked.arguments()).contains(_a2));
	}
```

Build the provider with `TypedConfiguration.newConfigItem(LabelProviderByExpression.Config.class)`, set the label expression from the string via `QueryExecutor`-compatible `Expr` parsing (copy the pattern from an existing provider test: `grep -rn "LabelProviderByExpression\|LabelByExpression" com.top_logic.model.search/src/test`), and instantiate with `SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY`. `TEST_KEY` is `ResKey.text("{0}")` or any `ResKey1` from the test's own constants. Make `toResKey` and the new `maskUndiscoverable` package-visible static methods annotated `@FrameworkInternal` if needed for the test, or test through `check(...)` with a configured constraint.

- [ ] **Step 2: Run to verify they fail**

Expected: `testLabelScriptUnsecured` FAILS (empty label), the constraint test does not compile.

- [ ] **Step 3: Implement**

After each `QueryExecutor.compile(...)`/`compileOptional(...)` in the four label/tooltip/title providers call `disableSecurity()` on the executor (null-safe for `compileOptional`), with the comment `// Labels belong to Discover (R1/R12 of the Discover specification) and are computed unsecured.`

`ConstraintCheckByExpression`:

```java
	/**
	 * Replaces every argument of the given message that the current user may not discover by a
	 * masked placeholder.
	 */
	static ResKey maskUndiscoverable(ResKey message) {
		Object[] arguments = message.arguments();
		if (arguments == null || arguments.length == 0) {
			return message;
		}
		Object[] masked = new Object[arguments.length];
		boolean changed = false;
		for (int n = 0; n < arguments.length; n++) {
			Object argument = arguments[n];
			if (argument instanceof TLObject object && !ModelAccessRights.getInstance().isDiscoverAllowed(object)) {
				masked[n] = I18NConstants.MASKED_OBJECT;
				changed = true;
			} else if (argument instanceof ResKey nested) {
				masked[n] = maskUndiscoverable(nested);
				changed |= masked[n] != nested;
			} else {
				masked[n] = argument;
			}
		}
		return changed ? ResKey.message(message.plain(), masked) : message;
	}
```

In `check(TLObject, TLStructuredTypePart)` return `maskUndiscoverable(toResKey(...))`. Document in the class JavaDoc: "The check runs unsecured; objects in the message the user may not discover are masked. A message rendered to a string inside the script is not masked - pass objects as message arguments."

- [ ] **Step 4: Build and run** (`mvn -B install -pl com.top_logic.model.search ...`), run `TestDiscoverRight`. Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add -u com.top_logic.model.search
git commit -m "Ticket #29782: Compute label scripts unsecured and mask undiscoverable objects in constraint messages."
```

---

### Task 9: React view layer - masked reference fields, filtered rows, R3 and R7 on save (R4, R7, R11)

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/security/ModelAccessPolicy.java` (`onAttribute` ~152-158)
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/AttributeRowSetBinding.java` (`readRows` ~80-92)
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/TLObjectOverlay.java` (`checkApply` ~297)
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/table/ColumnProviderService.java` (`label(Object)` ~768)
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/select/ReactDropdownSelectControl.java` (`toOptionDescriptors` ~654, `describe` ~695, goto handler ~719, `getSelectionSorted` ~733, `handleSelectByKey` ~545)
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/I18NConstants.java` (or the select package's), constant `MASKED_VALUE`
- Create: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/form/TestMaskedReferenceField.java`
- Modify (fixture): `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/security/ModelAccessTest.config.xml` and `src/test/webapp/WEB-INF/model/TestViewModelAccess.model.xml` if a reference to an undiscoverable object is not yet available (the fixture has `_category` / `_hiddenCategory`; use them).

**Interfaces:**
- Consumes: `ModelAccessRights#isDiscoverAllowed` (Task 1), `Update.checkNewTargets` (Task 7), `filterSecurity` with Discover criterion (Task 4).
- Produces: `public static boolean ReactDropdownSelectControl.isMasked(Object option)`.

- [ ] **Step 1: Write the failing tests**

Create `TestMaskedReferenceField extends AbstractModelAccessTest` (read `AbstractModelAccessTest` and `TestAttributeFieldAccessRights` first and follow their fixture use; `suite()` like `TestAttributeFieldAccessRights.suite()` at ~396). Tests (adapt object/attribute names to the fixture - a multi reference of a readable object containing `_category` and `_hiddenCategory`, readable for `_responsible`):

```java
	public void testMaskedDescriptor() {
		becomeUser(_responsible);
		JsonValue descriptor = describe(control(field(_task, "categories")), _hiddenCategory);
		assertEquals("*****", descriptor.label());
		assertNull(descriptor.image());
		assertFalse(descriptor.link());
	}

	public void testMaskedSortedLast() {
		becomeUser(_responsible);
		List<?> sorted = control(field(_task, "categories")).getSelectionSorted();
		assertEquals(_hiddenCategory, sorted.get(sorted.size() - 1));
	}

	public void testMaskedElementsWrittenBack() throws Exception {
		becomeUser(_responsible);
		FormControl form = editForm(_task);
		select(form, "categories" /* remove the visible one */, List.of(_hiddenCategory));
		form.executeStoreState();
		assertEquals(List.of(_hiddenCategory), _task.tValueByName("categories"));
	}

	public void testAdminSeesUnmasked() {
		becomeUser(_root);
		assertEquals(label(_hiddenCategory), describe(control(field(_task, "categories")), _hiddenCategory).label());
	}

	public void testCompositionRowsFiltered() {
		becomeUser(_responsible);
		assertFalse(new AttributeRowSetBinding(part("Project", "tasks")).readRows(_project).contains(hiddenTask()));
	}
```

`field`, `control`, `describe`, `editForm`, `select` are helpers to write in the test from the existing patterns in `TestSelectControlProvider` and `TestFormSaveWriteRights` (they show how a `FormControl`, an `AttributeSelectFieldModel` and a `ReactDropdownSelectControl` are created in a test, and how the client state is read via `getState`). Make `describe` and `getSelectionSorted` package-visible or read the `FieldState.VALUE__PROP` state of the control instead, whichever the existing tests do. If the fixture lacks a composition with an undiscoverable part, drop `testCompositionRowsFiltered` and cover `AttributeRowSetBinding` with a direct call on `_project` using the fixture's existing composition.

- [ ] **Step 2: Run to verify they fail**

`mvn -B install -pl com.top_logic,com.top_logic.model.search,com.top_logic.layout.react 2>&1 | tee com.top_logic.layout.react/target/mvn-build.log` (if not yet installed), then
`mvn -B test -DskipTests=false -pl com.top_logic.layout.view -Dtest=test.com.top_logic.layout.view.form.TestMaskedReferenceField 2>&1 | tee com.top_logic.layout.view/target/mvn-test.log`
Expected: FAIL (label is the real label).

- [ ] **Step 3: Mask in `ReactDropdownSelectControl`**

I18N constant in the react module's `I18NConstants`:

```java
	/**
	 * @en *****
	 */
	public static ResKey MASKED_VALUE;
```

In `ReactDropdownSelectControl`:

```java
	/**
	 * Whether the given selected value must be shown masked: a business object the current user may
	 * not discover. Its identity (label, icon, colour, link) is not sent to the client.
	 */
	public static boolean isMasked(Object option) {
		return option instanceof TLObject object && !ModelAccessRights.getInstance().isDiscoverAllowed(object);
	}
```

At the start of `describe(String id, Object option, ResourceProvider resourceProvider)`:

```java
		if (isMasked(option)) {
			Map<String, Object> result = new HashMap<>();   // use the same descriptor type describe() builds today
			result.put(VALUE, id);
			result.put(LABEL, Resources.getInstance().getString(I18NConstants.MASKED_VALUE));
			return result;
		}
```

(match the actual descriptor type and property constants of `describe`, e.g. `DropdownSelectState.Option` builder; no image, no colour role.)

In `toOptionDescriptors` set `link` to `false` for masked values (skip `ObjectNavigator.canShow`). In the `goto` handler (~719) ignore a masked target. In `getSelectionSorted()` (~733), when not `customOrder`, sort the unmasked values by label and append the masked ones in their stored order. In `handleSelectByKey`/`recordCommand` (~545/573) resolve against the options **and** the current value so masked elements survive replay.

- [ ] **Step 4: Filtered rows, R3 and R7 on save, masked cell labels**

- `ModelAccessPolicy.onAttribute`: remove the shortcut `if (read && !rights.hasGrant(attribute, operation)) return EXECUTABLE;` - an object reached in the UI is only discoverable, so the class decision must be asked (R3). Keep the rest. Update the method JavaDoc.
- `AttributeRowSetBinding.readRows(TLObject)`: return `(List<?>) SearchExpression.filterSecurity(object.tValue(_part))` (composition rows are a table: filtered, R11 table rule). Adapt the list type to the method's signature.
- `TLObjectOverlay.checkApply()`: after `Update.checkWritePermission(_base, part)` call `Update.checkNewTargets(_base, part, value)` for the changed value.
- `ColumnProviderService.label(Object value)`: return the masked text for `ReactDropdownSelectControl.isMasked(value)` (sorting, filtering and export of a cell then never see the label).

- [ ] **Step 5: Build and run**

`mvn -B install -pl com.top_logic.layout.react,com.top_logic.layout.view 2>&1 | tee com.top_logic.layout.view/target/mvn-build.log`; run `TestMaskedReferenceField`, `test.com.top_logic.layout.view.form.TestAttributeFieldAccessRights`, `test.com.top_logic.layout.view.form.TestAttributeOptionsReadFilter`, `test.com.top_logic.layout.view.form.TestFormSaveWriteRights`, `test.com.top_logic.layout.view.form.TestTLObjectOverlayWriteRights`, `test.com.top_logic.layout.view.security.TestModelAccessRule`, `test.com.top_logic.layout.view.inspector.TestInspectedNode`. Expected: PASS; existing tests asserting "attribute without own grant is readable on an object read through a filter" are adapted to R3.

- [ ] **Step 6: Commit**

```bash
git add -u com.top_logic.layout.react com.top_logic.layout.view
git add com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/form/TestMaskedReferenceField.java
git commit -m "Ticket #29782: Mask undiscoverable elements in reference fields and filter composition rows."
```

---

### Task 10: Demo verification, documentation, spec status

**Files:**
- Modify: `com.top_logic.demo.react/src/main/webapp/WEB-INF/conf/demoReactConf.config.xml` only if needed for the scenario
- Modify: `com.top_logic/src/main/java/META-INF/tl-docs/access-discover-spec.md` (status line), `access-configuration.md`

- [ ] **Step 1: Verify in the running demo** (`com.top_logic.demo.react`, start via the `tl-app` skill; delegate the browser loop to a sub-agent as described in its "Verifying in the browser" section)

Scenario (as `root` / `root1234`, then as a test user):
1. As root, create an account `discover1` (administration view) and add its person as member of project P1 only (`ProjectScope#members`).
2. Log in as `discover1`. Tree demo (`views/demo/tree-demo.view.xml`, `all(ProjectScope)`): only P1 appears.
3. Projects cockpit (`views/projects/milestones.view.xml`), form of P1: field `members` / `admins` (persons, `tl.accounts:Person` readable only by `AccountReader`) shows `*****` for every person the user may not discover, no link, no icon; masked entries sort last.
4. Edit P1 (if the user holds Write) and remove one masked member, save; as root check that only that member was removed.

Record the results (screenshots or snapshot excerpts) in the PR description.

- [ ] **Step 2: Documentation**

- `access-discover-spec.md`: change the status line to "implemented by ticket #29782" and remove "draft"; keep the open question on the cost of upward role inheritance.
- `access-configuration.md`: in the table of operations (bullet list after the XML example) add "`Discover` - see the Discover specification; implied by every other right".

- [ ] **Step 3: Full module build and the regression suites**

`mvn -B install -pl com.top_logic,com.top_logic.element,com.top_logic.model.search,com.top_logic.layout.react,com.top_logic.layout.view,com.top_logic.demo.react 2>&1 | tee com.top_logic.demo.react/target/mvn-build.log`. Run: `TestDiscoverRight`, `TestTLScriptSecurity`, `TestSecurityFilterReport`, `TestObjectFunctions`, `TestSecurityCoverageAnalysis`, `TestMaskedReferenceField` (FQNs above). Expected: all PASS.

- [ ] **Step 4: Commit and hand-off**

```bash
git add -u
git commit -m "Ticket #29782: Document the Discover right as implemented."
```

Then ask the user whether to push and open the PR (title `Ticket #29782: Discover right for the model based security.`).

## Not covered by this plan

- **R6, caches of secured results:** no shared cache of secured script results is known in the engine; none is changed. Report any found during implementation.
- **Object sources outside TL-Script functions** (`MethodBuilder`-based expressions of other modules, `loadFromSession`, `changeLog`, `inRevision`): they are not filtered beyond the final result filter and the input filter; the spec's consequence "every source must use the common layer" applies to them as follow-up work.
- **Cost of the upward role inheritance:** open question of the spec, unchanged.
