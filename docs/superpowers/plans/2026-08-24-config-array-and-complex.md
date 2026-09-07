# Array and Complex Properties in the Configuration Editor — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let the configuration editor display the two property kinds it can already handle with existing machinery — `COMPLEX` properties that carry a format, and `ARRAY` properties — instead of silently skipping them.

**Architecture:** `ConfigEditorControl.isSupportedKind` is the single gate that decides which properties appear in the form at all. Admitting `COMPLEX` with a value provider routes such a property down the existing plain-field path, where `ConfigControlService` already accepts it and gives it the format text field. Admitting `ARRAY` routes it to the existing `ConfigListEditorControl`, which needs to learn to read and write an array where it today assumes a `List`.

**Tech Stack:** Java 21, TopLogic TypedConfiguration, React control layer of `com.top_logic.layout.configedit`, JUnit 3-style tests (`junit.framework.TestCase` with a `suite()` method), Maven.

**Spec:** `docs/superpowers/specs/2026-08-12-config-form-element-design.md`, section 2. That section also covers `MAP` with inline-created entries, which is deliberately **not** in this plan — it needs a pending-entry mechanism of its own and gets a separate plan. This plan is the part that ships on existing machinery.

## Global Constraints

- **Module under change:** `com.top_logic.layout.configedit`. Do not touch `com.top_logic.layout.view.designer` or the view designer's behaviour.
- **Member variables** are prefixed with `_`. No `this.` prefix.
- **Every new `.java` file** needs the module's SPDX header plus a class-level JavaDoc comment:
  ```java
  /*
   * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
   *
   * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
   */
  ```
- **JavaDoc references members with `{@link}`**, not `{@code}`. `{@code}` only for literals, expressions, parameter names, and symbols genuinely off this module's classpath — anything in `com.top_logic.layout.view` is off it, because that module depends on this one and not the reverse.
- **A configured class's own class-level JavaDoc becomes in-app documentation** and must not reference the class's own methods; `TLDoclet` rejects that. Developer notes belong in `@implNote` or `@see`.
- **English `messages_en.properties` are generated** by the build and never edited by hand. German `messages_de.properties` are hand-maintained, and the build's automatic seeding does not work in this environment — write German by hand when a key is added.
- **Build command** (from the project root, never `cd` into a module; `target/` is owned by another user, hence `fakeroot`):
  ```bash
  HOME=/home/claude fakeroot mvn -B install -pl com.top_logic.layout.configedit \
    -Dmaven.repo.local=/home/claude/.m2/repository \
    -Dmaven.repo.local.tail=/home/dbu/.m2/repository \
    -Daether.chainedLocalRepository.ignoreTailAvailability=true -nsu
  ```
  It can fail at the install goal under the sandbox with "read-only file system"; retry that command outside the sandbox. Compiling and running tests works sandboxed.
- **Test command** — run the module's classes in one invocation and confirm from the `Running test…` lines that all six start:
  ```bash
  HOME=/home/claude fakeroot mvn -B test -DskipTests=false -pl com.top_logic.layout.configedit \
    -Dtest='test.com.top_logic.layout.configedit.TestConfig*' \
    -Dmaven.repo.local=/home/claude/.m2/repository \
    -Dmaven.repo.local.tail=/home/dbu/.m2/repository \
    -Daether.chainedLocalRepository.ignoreTailAvailability=true -nsu
  ```
- **Both javadoc checks** must pass before a commit, because they catch different things: `mvn install` runs `TLDoclet` over the main sources, and `mvn javadoc:test-javadoc` is the only thing that checks `{@link}`s in test comments — it runs in no build, so a broken reference there otherwise ships unnoticed. Ignore its known `element-list` read error and the `no @return`/`no comment` warnings on fixture getters.
- **Never pass `-Dmaven.javadoc.skip=true`.**
- **Commit messages:** `Ticket #29462: <description>.` No AI-attribution lines.

## File Structure

| File | Responsibility |
|---|---|
| `com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigEditorControl.java` | **Modify.** The gate: which property kinds appear in the form, and which control renders each. |
| `…/configedit/ConfigListEditorControl.java` | **Modify.** Reads and writes the element collection; today it assumes a `List` and mutates it in place. |
| `com.top_logic.layout.configedit/src/test/java/test/com/top_logic/layout/configedit/TestConfigEditorControl.java` | **Modify.** Builds real controls over real configurations; the natural home for both tasks' tests. |

---

### Task 1: A `COMPLEX` property with a format appears as a field

`ConfigEditorControl.isSupportedKind` admits `PLAIN`, `REF`, `ITEM` and `LIST`, so a `COMPLEX` property is skipped and never rendered. That is why a `ResKey` property is invisible in the editor: a property whose type carries both a `@Format` and a `ConfigurationValueBinding` is classified `COMPLEX` by `PropertyDescriptorImpl.initKind`, because a value binding decides the kind before the value provider is even considered.

`ConfigControlService.checkSupportedKind` already accepts exactly the right subset — `COMPLEX` **with** a value provider — and rejects a binding-only one, because such a value cannot be turned into text. So this task only opens the gate; the service does the rest and gives such a property the format text field.

**Files:**
- Modify: `com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigEditorControl.java` (the `isSupportedKind` call site and the method, around lines 94 and 231)
- Test: `com.top_logic.layout.configedit/src/test/java/test/com/top_logic/layout/configedit/TestConfigEditorControl.java`

**Interfaces:**
- Consumes: `ConfigControlService.getInstance().createModel(ConfigurationItem, PropertyDescriptor)` and `.createControl(ReactContext, ConfigFieldModel)`, which throw `IllegalArgumentException` for a kind they cannot edit — accepted are `PLAIN`, `REF`, and `COMPLEX` with a value provider.
- Produces: `ConfigEditorControl.isSupportedKind(PropertyDescriptor)` — note the changed parameter: the decision now needs the property, not only its kind.

- [ ] **Step 1: Write the failing tests**

Add to `TestConfigEditorControl`'s test configuration interface a `ResKey` property and a binding-only `COMPLEX` property. Check first how that class declares its fixtures — it has a nested configuration interface and builds controls over a real `ConfigurationItem`; follow its shape rather than inventing a new one. If it already has a `ResKey` fixture, reuse it.

```java
	/**
	 * A {@link ResKey} property is {@link PropertyKind#COMPLEX} - its type carries a
	 * {@code @Format} and a {@code ConfigurationValueBinding}, and a binding decides the kind
	 * before the value provider is considered. It has a value provider, so it can be edited as
	 * text and must appear in the form.
	 */
	public void testComplexPropertyWithFormatIsDisplayed() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		ConfigEditorControl control = new ConfigEditorControl(context(), config);

		assertTrue("A ResKey property must be rendered, not skipped.",
			rendersProperty(control, TestConfig.LABEL));
	}

	/**
	 * A {@link PropertyKind#COMPLEX} property with only a value binding and no format has no way
	 * to become text, so it stays skipped - rendering it would hand the service a property it
	 * rejects.
	 */
	public void testComplexPropertyWithoutFormatIsSkipped() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		ConfigEditorControl control = new ConfigEditorControl(context(), config);

		assertFalse("A binding-only property has no text form and must stay skipped.",
			rendersProperty(control, TestConfig.BINDING_ONLY));
	}
```

`rendersProperty(control, propertyName)` does not exist yet. Write it as a private helper in the test class: walk the control's children and report whether one of them carries the label of that property. Look at how the class's existing tests inspect a built control — there is already an idiom for reaching into the rendered children; use it rather than adding a new accessor to production code. `TestConfigControlService` has a `BINDING_ONLY` fixture with a `NoFormatBinding`; copy that shape if `TestConfigEditorControl` has none.

- [ ] **Step 2: Run the tests to verify they fail**

Use the module's test command from the Global Constraints, narrowed to this class with `-Dtest=test.com.top_logic.layout.configedit.TestConfigEditorControl`.

Expected: `testComplexPropertyWithFormatIsDisplayed` fails because the property is skipped. `testComplexPropertyWithoutFormatIsSkipped` passes already — it pins behaviour that must survive, which is the point of writing it now.

- [ ] **Step 3: Open the gate**

Change the signature so the decision can see the property, and admit the `COMPLEX` case the service accepts:

```java
	private static boolean isSupportedKind(PropertyDescriptor property) {
		PropertyKind kind = property.kind();
		return kind == PropertyKind.PLAIN || kind == PropertyKind.REF || kind == PropertyKind.ITEM
			|| kind == PropertyKind.LIST
			|| (kind == PropertyKind.COMPLEX && property.getValueProvider() != null);
	}
```

Update the call site accordingly. Extend the method's JavaDoc to say why a binding-only `COMPLEX` property stays out: it has no text form, and `ConfigControlService` would reject it — cite that class rather than restating its rule, so the two cannot drift apart silently.

- [ ] **Step 4: Run the tests to verify they pass**

Same command. Expected: both green, and the class's other tests unchanged.

- [ ] **Step 5: Run the whole module and both javadoc checks**

The full test command from the Global Constraints, then `mvn install` and `mvn javadoc:test-javadoc`.

- [ ] **Step 6: Commit**

```bash
git add com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigEditorControl.java \
        com.top_logic.layout.configedit/src/test/java/test/com/top_logic/layout/configedit/TestConfigEditorControl.java
git commit -m "Ticket #29462: Display a complex configuration property that has a format."
```

---

### Task 2: An `ARRAY` property is edited by the list editor

An `ARRAY` property is the same thing as a `LIST` property from the user's point of view — a sequence of elements with add, remove and reorder — and `ConfigListEditorControl` already provides all of it. The obstacle is the value: `_parentConfig.value(_property)` yields an array, while all four mutators cast to `List<ConfigurationItem>` and mutate it in place.

TypedConfiguration provides both conversions, and they are public: `PropertyDescriptorImpl.arrayAsList(Object)` and `PropertyDescriptorImpl.listAsArray(PropertyDescriptor, Collection<?>)`.

Note what is deliberately **not** changed: for a `LIST` property the mutators keep working on the configuration's own live list. Switching that to read-modify-write would move where TypedConfiguration validates a keyed list — today the collision surfaces at `items.add(...)`, which is what `checkKeyAvailable` guards in front of. Keeping the list path exactly as it is keeps that guard meaningful.

**Files:**
- Modify: `com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigEditorControl.java` (`isSupportedKind` and the `LIST` branch, around line 154)
- Modify: `…/configedit/ConfigListEditorControl.java` (the four mutators and `rebuild`, lines 116–384)
- Test: `com.top_logic.layout.configedit/src/test/java/test/com/top_logic/layout/configedit/TestConfigEditorControl.java`

**Interfaces:**
- Consumes: `ConfigEditorControl.isSupportedKind(PropertyDescriptor)` from Task 1.
- Produces: nothing new outside the module; `ConfigListEditorControl`'s constructor signature stays `(ReactContext, ConfigurationItem, PropertyDescriptor)`.

- [ ] **Step 1: Write the failing tests**

Add an array-typed property of configuration items to `TestConfigEditorControl`'s fixture — an `Item[]` alongside whatever list fixture the class already has, so the two can be compared. Then:

```java
	/**
	 * An array property is a sequence like a list and gets the same editor.
	 */
	public void testArrayPropertyIsRenderedByListEditor() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		ConfigEditorControl control = new ConfigEditorControl(context(), config);

		assertTrue("An array property must be rendered by the list editor.",
			rendersProperty(control, TestConfig.ITEM_ARRAY));
	}

	/**
	 * Adding to an array property stores a new array, not a list: the configuration rejects a
	 * value of the wrong shape, so this is what makes the editor usable at all.
	 */
	public void testAddingToArrayPropertyStoresAnArray() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.ITEM_ARRAY);
		ConfigListEditorControl editor = new ConfigListEditorControl(context(), config, property);

		clickAddButton(editor);

		Object value = config.value(property);
		assertNotNull("The array must have been stored.", value);
		assertTrue("An array property must hold an array, not a list.", value.getClass().isArray());
		assertEquals("One element must have been added.", 1, java.lang.reflect.Array.getLength(value));
	}
```

`clickAddButton(editor)` does not exist. The "+" is a `ReactButtonControl` created in `ConfigListEditorControl.rebuild`, and the test needs to invoke its command. Look at how `TestConfigEditorControl` already reaches controls and their commands; if there is no idiom for invoking a button, the honest alternative is to assert on what `rebuild` produced (a group per element plus the add button) and to drive the addition through the same public entry the button uses. Do not add a method to production code solely to make the test reachable without saying so in your report.

- [ ] **Step 2: Run the tests to verify they fail**

Narrowed to `TestConfigEditorControl`. Expected: the first fails because the property is skipped; the second fails with a `ClassCastException` from the mutator's cast, or earlier at the gate.

- [ ] **Step 3: Admit the kind and route it**

In `ConfigEditorControl`, add `PropertyKind.ARRAY` to `isSupportedKind`, and widen the branch that creates the list editor so it covers both kinds:

```java
			if (property.kind() == PropertyKind.LIST || property.kind() == PropertyKind.ARRAY) {
				ConfigListEditorControl listEditor =
					new ConfigListEditorControl(context, config, property);
				ReactFormGroupControl listGroup = new ReactFormGroupControl(
					context, null, true, false, "default", false,
					List.of(), List.of(listEditor));
				listGroup.setHeader(createGroupHeader(context, property));
				addChild(listGroup);
				continue;
			}
```

- [ ] **Step 4: Teach the list editor both shapes**

Give `ConfigListEditorControl` one place that reads the elements and one that writes them back, and route the four mutators and `rebuild` through them:

```java
	/**
	 * The elements currently held by the edited property.
	 *
	 * <p>
	 * For a {@link PropertyKind#LIST} property this is the configuration's own live list, so a
	 * mutation of it takes effect directly - which is what lets TypedConfiguration reject a
	 * duplicate key at the moment of the change. For a {@link PropertyKind#ARRAY} property it is a
	 * detached copy that only reaches the configuration through
	 * {@link #storeElements(List)}.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	private List<ConfigurationItem> elements() {
		Object value = _parentConfig.value(_property);
		if (_property.kind() == PropertyKind.ARRAY) {
			return value == null ? new ArrayList<>()
				: new ArrayList<>((List<ConfigurationItem>) PropertyDescriptorImpl.arrayAsList(value));
		}
		return (List<ConfigurationItem>) value;
	}

	/**
	 * Writes back the elements obtained from {@link #elements()}, for a property shape that cannot
	 * be mutated in place.
	 */
	private void storeElements(List<ConfigurationItem> elements) {
		if (_property.kind() == PropertyKind.ARRAY) {
			_parentConfig.update(_property, PropertyDescriptorImpl.listAsArray(_property, elements));
		}
		// A list property was mutated in place and needs no write-back.
	}
```

Then, in each of `addElement`, `removeElement`, `moveUp`, `moveDown`, replace the cast with `elements()` and add a `storeElements(items)` call after the mutation and before `rebuild(...)`. In `rebuild`, replace the cast with `elements()` as well; it only reads, so it needs no write-back.

Watch the null case: `rebuild` currently guards with `if (items != null)`, and `elements()` returns `null` for a list property that has no value. Keep that guard.

- [ ] **Step 5: Run the tests to verify they pass**

Narrowed to `TestConfigEditorControl`, then the full module command. Expected: green, including every existing list test — those pin that the list path did not change.

- [ ] **Step 6: Both javadoc checks**

`mvn install` and `mvn javadoc:test-javadoc`, as in the Global Constraints.

- [ ] **Step 7: Commit**

```bash
git add com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigEditorControl.java \
        com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigListEditorControl.java \
        com.top_logic.layout.configedit/src/test/java/test/com/top_logic/layout/configedit/TestConfigEditorControl.java
git commit -m "Ticket #29462: Edit an array configuration property with the list editor."
```

---

### Task 3: Verify both kinds in the running application

**Files:** none — verification only.

- [ ] **Step 1: Give the demo a property of each kind**

`com.top_logic.demo.react/src/main/java/com/top_logic/demo/react/view/DemoEditorConfig.java` is the configuration behind the configuration-editor demo, the one the human uses to try this out. It has no array property and, unless it gained one, no `ResKey` property either. Add one of each, in the style of the properties already there, so both kinds can actually be seen. Keep them plausible for a demo rather than named after their type.

- [ ] **Step 2: Start the application and look**

Use the `tl-app` skill to start `com.top_logic.demo.react` — read that skill first for the start command and the browser guidance. The start goal needs the module directory as its working directory, otherwise the database is missing. Login is `root` / `root1234`; a freshly created database prints an initial password in the log instead.

Check, and report what you saw rather than what you expected:
- the `ResKey` property appears and shows its value in the format's text form
- the array property appears with the list editor's group, and its "+", remove and reorder buttons work
- adding, changing and removing an array element survives an apply and a reload
- neither property produces a technical error banner

- [ ] **Step 3: Commit the demo properties**

```bash
git add com.top_logic.demo.react/src/main/java/com/top_logic/demo/react/view/DemoEditorConfig.java
git commit -m "Ticket #29462: Show an array and a formatted complex property in the editor demo."
```

If Step 2 found a defect, describe it precisely enough that someone else can write the failing test, and fix it as its own commit with the test first.

---

## Self-Review

**Spec coverage** (section 2 of the design document):

| Spec requirement | Task |
|---|---|
| `ARRAY` — the same list editor as `LIST`, reading and writing both value shapes | 2 |
| `COMPLEX` — the format field, gated on having a value provider | 1 |
| `MAP` — entries created inline through a pending entry | **deliberately deferred to its own plan**, see below |

`MAP` is the whole reason section 2 needed a design discussion, and it is the one part that cannot be built from existing machinery: an entry has no place to live between "the user pressed +" and "the key is valid and unique", so the editor needs a pending entry it holds itself. That mechanism also replaces `ConfigListEditorControl.checkKeyAvailable`, the guard that currently only refuses the second nameless entry of a keyed list. Both belong in one plan, written once the array and complex work has landed — the list editor is the file all three touch, and sequencing them avoids three plans editing it at once.

**Placeholder scan:** no `TBD`, no "handle edge cases", no "similar to Task N". Two steps deliberately tell the implementer to find an existing idiom rather than prescribing code — the test helpers `rendersProperty` and `clickAddButton` — because `TestConfigEditorControl`'s way of reaching into a built control is established in that file and inventing a second way would be worse than reusing it. Both steps say what the helper must achieve and forbid the tempting shortcut of widening production code to suit the test.

**Type consistency:** `isSupportedKind(PropertyDescriptor)` is defined in Task 1 and used in Task 2. `elements()` / `storeElements(List<ConfigurationItem>)` are defined and used only within Task 2. `PropertyDescriptorImpl.arrayAsList(Object)` and `listAsArray(PropertyDescriptor, Collection<?>)` are existing public statics, verified in `com.top_logic.basic/src/main/java/com/top_logic/basic/config/PropertyDescriptorImpl.java` at lines 2461 and 2468.
