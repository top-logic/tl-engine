# Map Properties and Inline-Created Entries — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let the configuration editor create the entries of a keyed collection inline instead of refusing them, and show `MAP` properties at all.

**Architecture:** A keyed collection cannot hold an entry whose key is empty or already taken, which is why the classic form obtains the key in a dialog before inserting. Inline creation keeps that guarantee and moves the moment: the "+" produces a **pending entry** that lives in the control until its key is valid and unique, and only then enters the configuration. The same mechanism serves a keyed `LIST` and a `MAP`, because in both the key is a property of the entry itself (`PropertyDescriptor.getKeyProperty()` — "the property of the value descriptor that delivers the values to index this collection with"). `MAP` then differs from `LIST` in only two respects: the value is a `Map`, and it is unordered (`setKindMap` sets `setOrdered(false)`), so the reorder buttons do not apply.

**Tech Stack:** Java 21, TopLogic TypedConfiguration, React control layer of `com.top_logic.layout.configedit`, JUnit 3-style tests (`junit.framework.TestCase` with a `suite()` method), Maven.

**Spec:** `docs/superpowers/specs/2026-08-12-config-form-element-design.md`, section 2. Its `ARRAY` and `COMPLEX` parts are already implemented by `docs/superpowers/plans/2026-08-24-config-array-and-complex.md`; this plan is the rest.

## Global Constraints

- **Module under change:** `com.top_logic.layout.configedit`. Do not change the view designer's behaviour beyond what these tasks require.
- **Member variables** are prefixed with `_`. No `this.` prefix.
- **Every new `.java` file** needs the module's SPDX header plus a class-level JavaDoc comment:
  ```java
  /*
   * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
   *
   * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
   */
  ```
- **JavaDoc references members with `{@link}`**, not `{@code}`. `{@code}` only for literals, expressions, parameter names, and symbols genuinely off this module's classpath — anything in `com.top_logic.layout.view` is off it.
- **A configured class's own class-level JavaDoc becomes in-app documentation** and must not reference the class's own methods; `TLDoclet` rejects that. Developer notes belong in `@implNote` or `@see`.
- **English `messages_en.properties` are generated** by the build and never edited by hand. German `messages_de.properties` are hand-maintained, and the build's seeding does not work in this environment — write German by hand for every key added or removed.
- **Build command** (from the project root, never `cd` into a module):
  ```bash
  HOME=/home/claude fakeroot mvn -B install -pl com.top_logic.layout.configedit \
    -Dmaven.repo.local=/home/claude/.m2/repository \
    -Dmaven.repo.local.tail=/home/dbu/.m2/repository \
    -Daether.chainedLocalRepository.ignoreTailAvailability=true -nsu
  ```
  It can fail at the install goal under the sandbox with "read-only file system"; retry that command outside the sandbox.
- **Test command** — one invocation, and confirm from the `Running test…` lines that all six classes start:
  ```bash
  HOME=/home/claude fakeroot mvn -B test -DskipTests=false -pl com.top_logic.layout.configedit \
    -Dtest='test.com.top_logic.layout.configedit.TestConfig*' \
    -Dmaven.repo.local=/home/claude/.m2/repository \
    -Dmaven.repo.local.tail=/home/dbu/.m2/repository \
    -Daether.chainedLocalRepository.ignoreTailAvailability=true -nsu
  ```
- **Both javadoc checks** before every commit: `mvn install` runs `TLDoclet` over the main sources, and `mvn javadoc:test-javadoc` is the only thing that checks `{@link}`s in test comments — it runs in no build. Ignore its known `element-list` read error and the `no @return`/`no comment` warnings on fixture getters.
- **Commit messages:** `Ticket #29462: <description>.` No AI-attribution lines.

## File Structure

| File | Responsibility |
|---|---|
| `com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigListEditorControl.java` | **Modify.** Renders the entries of a collection property and mutates it. Gains the entry's own key field, the pending entry, and `MAP` support. |
| `…/configedit/ConfigEditorControl.java` | **Modify.** The gate: admits `MAP` and routes it to the collection editor. |
| `…/configedit/I18NConstants.java` | **Modify.** One key added (duplicate key), one removed (the add-guard's message). |
| `com.top_logic.layout.configedit/src/main/java/META-INF/messages_de.properties` | **Modify.** German for the added key; the removed one goes. |
| `com.top_logic.layout.configedit/src/test/java/test/com/top_logic/layout/configedit/TestConfigEditorControl.java` | **Modify.** Builds real controls over real configurations; home for all three tasks' tests. |

---

### Task 1: The entry group owns its key field, and a committed key is immutable

Today an entry group renders a nested `ConfigEditorControl` over the whole entry, so the key property is one field among the others and is freely editable. Two things need it to be different: a pending entry's key must be reachable by the control that decides when to insert it (Task 2), and a committed entry's key must not be editable — changing it would silently re-index the collection, which is why the classic editor renders the key field immutable (`EditorFactory.initEditorGroup`, and again in `MapFormGroupBuilder.createKeyField`).

So the group renders the key field itself and hides that property from the nested editor. `ConfigEditorControl` already has a constructor taking the properties to hide.

This task changes visible behaviour on its own: the key of an existing entry becomes read-only.

**Files:**
- Modify: `…/configedit/ConfigListEditorControl.java` — `createElementGroup`, around lines 143–197
- Test: `…/test/…/configedit/TestConfigEditorControl.java`

**Interfaces:**
- Consumes: `ConfigControlService.getInstance().createModel(ConfigurationItem, PropertyDescriptor)` returning a `ConfigFieldModel`, and `.createControl(ReactContext, ConfigFieldModel)`; `ConfigEditorControl(ReactContext, ConfigurationItem, Set<PropertyDescriptor>)`; `AbstractFieldModel.setEditable(boolean)`; `ReactFormFieldChromeControl(ReactContext, String label, boolean required, boolean dirty, …, ReactControl input)` — copy the exact argument list from `ConfigEditorControl`'s own use of it rather than guessing.
- Produces: `ConfigListEditorControl.createKeyField(ConfigurationItem entry, boolean editable)` returning a `ReactControl`, used again by Task 2.

- [ ] **Step 1: Write the failing tests**

`TestConfigEditorControl` already has a keyed-list fixture and the helpers `findHeaderButton`, `findTypeFieldModel` and `elementGroups` from the previous plan. Follow their idiom; add a helper that reaches an entry group's key field model the same way `findTypeFieldModel` reaches the type selector's.

```java
	/**
	 * The key of an entry that is already in the collection cannot be edited: changing it would
	 * silently re-index the collection under the new key. The classic editor renders the key
	 * field immutable for the same reason.
	 */
	public void testKeyFieldOfExistingEntryIsReadOnly() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.ITEMS);
		Item entry = TypedConfiguration.newConfigItem(Item.class);
		entry.setName("first");
		((List<Item>) config.value(property)).add(entry);

		ConfigListEditorControl editor = new ConfigListEditorControl(context(), config, property);

		FieldModel keyModel = findKeyFieldModel(elementGroups(editor).get(0));
		assertNotNull("The entry group must render the key field itself.", keyModel);
		assertFalse("The key of a committed entry must not be editable.", keyModel.isEditable());
	}

	/**
	 * The key field is rendered once, by the group - not a second time by the nested editor over
	 * the same entry.
	 */
	public void testKeyPropertyIsNotRenderedTwice() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.ITEMS);
		Item entry = TypedConfiguration.newConfigItem(Item.class);
		entry.setName("first");
		((List<Item>) config.value(property)).add(entry);

		ConfigListEditorControl editor = new ConfigListEditorControl(context(), config, property);

		assertEquals("The key must appear exactly once in the entry group.", 1,
			countKeyFields(elementGroups(editor).get(0), property.getKeyProperty()));
	}
```

`findKeyFieldModel` and `countKeyFields` do not exist. Write them beside the existing helpers, using the same scripting projection the file already uses to reach nested controls. Do not add anything to production code to make them reachable; if you conclude there is no honest way, say so in your report rather than doing it silently.

- [ ] **Step 2: Run the tests to verify they fail**

The test command, narrowed with `-Dtest=test.com.top_logic.layout.configedit.TestConfigEditorControl`.

Expected: the first fails because no key field is rendered by the group; the second fails because the key appears once but from the nested editor, or twice once you start rendering it — read the failure and make sure you know which.

- [ ] **Step 3: Render the key field in the group**

In `createElementGroup`, before the nested editor is created, build the key field and hide the key property from the nested editor:

```java
		PropertyDescriptor keyProperty = _property.getKeyProperty();
		if (keyProperty != null) {
			bodyChildren.add(createKeyField(item, false));
		}
		if (!polymorphic || isTypeSelected(item)) {
			bodyChildren.add(new ConfigEditorControl(_context, item,
				keyProperty == null ? Collections.emptySet() : Collections.singleton(keyProperty)));
		}
```

and add the field factory:

```java
	/**
	 * The field for an entry's key property, rendered by this control rather than by the nested
	 * editor over the entry.
	 *
	 * <p>
	 * The key decides where the entry sits in the collection, so it is editable only while the
	 * entry is still pending - once it is in the collection, changing it would re-index the
	 * collection under a new key. The classic declarative form renders it immutable for the same
	 * reason.
	 * </p>
	 *
	 * @param entry
	 *        The entry whose key is edited.
	 * @param editable
	 *        Whether the key may still be changed, i.e. whether the entry is pending.
	 */
	private ReactControl createKeyField(ConfigurationItem entry, boolean editable) {
		PropertyDescriptor keyProperty = _property.getKeyProperty();
		ConfigFieldModel model = ConfigControlService.getInstance().createModel(entry, keyProperty);
		model.setEditable(editable);
		ReactControl input = ConfigControlService.getInstance().createControl(_context, model);
		return new ReactFormFieldChromeControl(_context, Labels.propertyLabel(keyProperty, false),
			model.isMandatory(), false, null, null, null, false, true, input);
	}
```

Check `ConfigEditorControl`'s own call of the chrome constructor and copy its argument list — the one above is written from that call and must match the real signature.

- [ ] **Step 4: Run the tests to verify they pass**

Same command, then the full module command. Every existing test must stay green; if one does not, work out why before changing it — a changed expectation here is the signal that the key is now rendered somewhere it was not.

- [ ] **Step 5: Both javadoc checks, then commit**

```bash
git add com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigListEditorControl.java \
        com.top_logic.layout.configedit/src/test/java/test/com/top_logic/layout/configedit/TestConfigEditorControl.java
git commit -m "Ticket #29462: Render an entry's key field in its group and fix it once the entry exists."
```

---

### Task 2: A pending entry replaces the refusal to add

`addElement` currently refuses to add a second entry to a keyed collection while an existing one has no key (`checkKeyAvailable`, added because TypedConfiguration rejects the duplicate empty key with a technical exception). The refusal is honest but the limitation stands: a user cannot start a second entry before naming the first.

The pending entry removes the limitation. The "+" creates the entry and keeps it in the control; its key field is editable; as soon as the key is non-empty and not already taken, the entry moves into the collection and its key field becomes immutable. An empty key leaves it pending silently — the user is still typing — and a taken key leaves it pending with an error on the key field.

**Files:**
- Modify: `…/configedit/ConfigListEditorControl.java` — the pending state, `rebuild`, `addElement`, and the removal of `checkKeyAvailable`/`isKeyUnset`
- Modify: `…/configedit/I18NConstants.java` — add the duplicate-key message, remove `ERROR_LIST_ELEMENT_KEY_MISSING__PROPERTY`
- Modify: `com.top_logic.layout.configedit/src/main/java/META-INF/messages_de.properties`
- Test: `…/test/…/configedit/TestConfigEditorControl.java`

**Interfaces:**
- Consumes: `createKeyField(ConfigurationItem, boolean)` from Task 1; `ConfigFieldModel` extends `AbstractFieldModel`, so `setError(ResKey)` is available on the model `createKeyField` builds.
- Produces: nothing outside the class.

- [ ] **Step 1: Write the failing tests**

```java
	/**
	 * Pressing "+" on a keyed collection creates an entry that is not yet in the collection: it
	 * has no key, and a keyed collection cannot hold it.
	 */
	public void testAddingToKeyedCollectionCreatesAPendingEntry() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.ITEMS);
		ConfigListEditorControl editor = new ConfigListEditorControl(context(), config, property);

		clickAddButton(editor);

		assertEquals("The pending entry must not be in the collection yet.", 0,
			((List<?>) config.value(property)).size());
		assertEquals("The pending entry must be rendered.", 1, elementGroups(editor).size());
	}

	/**
	 * Naming the pending entry moves it into the collection.
	 */
	public void testNamingThePendingEntryCommitsIt() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.ITEMS);
		ConfigListEditorControl editor = new ConfigListEditorControl(context(), config, property);
		clickAddButton(editor);

		findKeyFieldModel(elementGroups(editor).get(0)).setValue("first");

		List<?> items = (List<?>) config.value(property);
		assertEquals("The named entry must be in the collection.", 1, items.size());
		assertEquals("first", ((Item) items.get(0)).getName());
	}

	/**
	 * A key that is already taken leaves the entry pending and reports the clash at the field,
	 * rather than letting TypedConfiguration reject it with a technical exception.
	 */
	public void testDuplicateKeyLeavesTheEntryPendingWithAnError() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.ITEMS);
		Item existing = TypedConfiguration.newConfigItem(Item.class);
		existing.setName("first");
		((List<Item>) config.value(property)).add(existing);
		ConfigListEditorControl editor = new ConfigListEditorControl(context(), config, property);
		clickAddButton(editor);

		FieldModel keyModel = findKeyFieldModel(elementGroups(editor).get(1));
		keyModel.setValue("first");

		assertEquals("The clashing entry must stay out of the collection.", 1,
			((List<?>) config.value(property)).size());
		assertNotNull("The clash must be reported at the key field.", keyModel.getInputError());
	}

	/**
	 * A second entry can be started while the first is still unnamed - the limitation the old
	 * add-guard imposed is gone.
	 */
	public void testASecondEntryCanBeStartedBeforeTheFirstIsNamed() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.ITEMS);
		ConfigListEditorControl editor = new ConfigListEditorControl(context(), config, property);
		clickAddButton(editor);
		findKeyFieldModel(elementGroups(editor).get(0)).setValue("first");

		clickAddButton(editor);

		assertEquals("The named entry is in the collection.", 1, ((List<?>) config.value(property)).size());
		assertEquals("A new pending entry is rendered beside it.", 2, elementGroups(editor).size());
	}
```

- [ ] **Step 2: Run them to verify they fail**

Narrowed to `TestConfigEditorControl`. Expected: the first two fail because "+" inserts directly; the third fails with the guard's refusal or a `TopLogicException`; the fourth fails with the guard's refusal.

- [ ] **Step 3: Hold a pending entry**

Add the state and render it:

```java
	/**
	 * An entry created by the add button that is not yet in the edited collection.
	 *
	 * <p>
	 * A keyed collection is indexed by a property of its entries, so it cannot hold an entry
	 * whose key is empty or already taken. Such an entry therefore lives here until its key
	 * makes it acceptable - see {@link #commitPending()}.
	 * </p>
	 */
	private ConfigurationItem _pendingEntry;
```

In `rebuild`, after the loop over the committed entries and before the add button, render the pending entry's group if there is one, expanded, with its key field editable. Give `createElementGroup` a parameter for that, or add a sibling method — whichever keeps the method readable; say which you chose and why.

- [ ] **Step 4: Create pending instead of inserting**

```java
	private void addElement() {
		if (_property.getKeyProperty() != null) {
			if (_pendingEntry != null) {
				// One unfinished entry at a time: a second one could not be told apart from the
				// first, both having no key yet.
				return;
			}
			_pendingEntry = newEntry();
			listenForKey(_pendingEntry);
			rebuild(_pendingEntry);
			return;
		}

		List<ConfigurationItem> items = elements();
		ConfigurationItem newItem = newEntry();
		items.add(newItem);
		storeElements(items);
		rebuild(newItem);
	}
```

`newEntry()` is the item creation that `addElement` does today — extract it unchanged, including the `_choices` branch. `listenForKey` registers a `ConfigurationListener` on the pending entry's key property that calls `commitPending()`, and must be unregistered like the other listeners this class holds (see `_listeners` and `removeListeners`).

- [ ] **Step 5: Commit the pending entry when its key allows it**

```java
	/**
	 * Moves {@link #_pendingEntry} into the edited collection once its key is usable.
	 *
	 * <p>
	 * An empty key means the user has not finished typing, so the entry simply stays pending. A
	 * key that another entry already uses is reported at the key field: inserting would be
	 * rejected by TypedConfiguration with a message about the collection's index, which says
	 * nothing to whoever is editing the form.
	 * </p>
	 */
	private void commitPending() {
		PropertyDescriptor keyProperty = _property.getKeyProperty();
		Object key = _pendingEntry.value(keyProperty);
		if (key == null || key.toString().isEmpty()) {
			return;
		}
		if (hasEntryWithKey(key)) {
			keyFieldModel().setError(I18NConstants.ERROR_DUPLICATE_KEY__VALUE_PROPERTY
				.fill(key, Labels.propertyLabel(keyProperty, false)));
			return;
		}
		ConfigurationItem entry = _pendingEntry;
		_pendingEntry = null;
		List<ConfigurationItem> items = elements();
		items.add(entry);
		storeElements(items);
		rebuild(entry);
	}
```

`hasEntryWithKey` compares against the keys of the committed entries. `keyFieldModel()` must reach the model `createKeyField` built for the pending entry — hold it in a field when you create it rather than searching for it afterwards.

Declare the message in `I18NConstants`:

```java
	/**
	 * @en An entry with {0} "{1}" already exists.
	 */
	public static ResKey2 ERROR_DUPLICATE_KEY__VALUE_PROPERTY;
```

Write the German by hand. Delete `ERROR_LIST_ELEMENT_KEY_MISSING__PROPERTY` together with `checkKeyAvailable` and `isKeyUnset`, and delete its German line too — a key without an English counterpart is dead weight.

- [ ] **Step 6: Run the tests, then the full module**

All four new tests green, every existing test green. `TestConfigEditorControl` has a test for the old refusal; it must be deleted with the guard, not adjusted — say so in your report.

- [ ] **Step 7: Both javadoc checks, then commit**

```bash
git add com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigListEditorControl.java \
        com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/I18NConstants.java \
        com.top_logic.layout.configedit/src/main/java/META-INF/messages_de.properties \
        com.top_logic.layout.configedit/src/main/java/META-INF/messages_en.properties \
        com.top_logic.layout.configedit/src/test/java/test/com/top_logic/layout/configedit/TestConfigEditorControl.java
git commit -m "Ticket #29462: Create the entry of a keyed configuration collection inline."
```

---

### Task 3: `MAP` properties reach the editor

With the pending entry in place, a `MAP` property differs from a keyed `LIST` in two respects only: its value is a `Map` keyed by the entries' key values, and it is unordered — `setKindMap` calls `setOrdered(false)`, so `PropertyDescriptor.isOrdered()` tells the two apart and the reorder buttons do not apply.

**Files:**
- Modify: `…/configedit/ConfigEditorControl.java` — `isSupportedKind` and the branch that creates the collection editor
- Modify: `…/configedit/ConfigListEditorControl.java` — `elements()`, `storeElements(List)`, and the reorder buttons
- Test: `…/test/…/configedit/TestConfigEditorControl.java`

**Interfaces:**
- Consumes: everything from Tasks 1 and 2.
- Produces: nothing outside the module.

- [ ] **Step 1: Write the failing tests**

Add a `Map` fixture keyed by the entry's name — `@Key(Item.NAME) Map<String, Item> getIndex();` — beside the existing list fixture, and test:

```java
	/** A map property is rendered by the collection editor. */
	public void testMapPropertyIsRendered() { … assertTrue(rendersProperty(control, TestConfig.INDEX)); }

	/** Its entries are shown, one group each. */
	public void testMapEntriesAreRendered() { … assertEquals(2, elementGroups(editor).size()); }

	/** Naming a pending entry puts it into the map under its key. */
	public void testNamingThePendingEntryPutsItIntoTheMap() {
		…
		findKeyFieldModel(elementGroups(editor).get(0)).setValue("alpha");
		Map<?, ?> index = (Map<?, ?>) config.value(property);
		assertEquals(1, index.size());
		assertTrue("The map must be keyed by the entry's key value.", index.containsKey("alpha"));
	}

	/** An unordered collection offers no reorder buttons. */
	public void testMapEntriesHaveNoReorderButtons() {
		…
		assertNull("A map is unordered; moving an entry has no meaning.",
			findHeaderButton(elementGroups(editor).get(0), "▲"));
	}
```

Write the bodies in the file's established style; the elisions above are the fixture setup, which follows the existing tests.

- [ ] **Step 2: Run them to verify they fail**

Expected: the first two fail because the property is skipped by the gate.

- [ ] **Step 3: Admit `MAP` and route it**

Add `PropertyKind.MAP` to `isSupportedKind`, and include it in the branch that builds the collection editor alongside `LIST` and `ARRAY`.

- [ ] **Step 4: Read and write a map**

Extend `elements()` and `storeElements(List)`:

```java
		if (_property.kind() == PropertyKind.MAP) {
			Map<?, ?> map = (Map<?, ?>) _parentConfig.value(_property);
			return map == null ? new ArrayList<>() : new ArrayList<>((Collection<ConfigurationItem>) map.values());
		}
```

and, on the write side, rebuild the map from the entries' key values in iteration order, then `_parentConfig.update(_property, newMap)`. Use a `LinkedHashMap`, as `MapFormGroupBuilder` does, so the order the user sees is stable.

Determine by reading whether the configuration's own map is mutable in place, as the list is — if it is, say in your report which you chose and why. Whichever you choose, a duplicate key must still be impossible: Task 2's `commitPending` is what prevents it, so check that its `hasEntryWithKey` sees map entries too.

- [ ] **Step 5: Hide the reorder buttons for an unordered collection**

In `createElementGroup`, add the move buttons only when `_property.isOrdered()`. Say in the JavaDoc why, naming `setKindMap`'s `setOrdered(false)` as the source of the distinction.

- [ ] **Step 6: Run everything, both javadoc checks, then commit**

```bash
git commit -m "Ticket #29462: Edit a map configuration property with the collection editor."
```

---

### Task 4: Verify in the running application

**Files:** `com.top_logic.demo.react/src/main/java/com/top_logic/demo/react/view/DemoEditorConfig.java` — add a map property in the style of the existing ones.

- [ ] **Step 1: Add a map property to the demo configuration**

The demo already has a keyed `List<Item> items`; add a map keyed the same way so both can be tried side by side.

- [ ] **Step 2: Start the application and check**

Use the `tl-app` skill to start `com.top_logic.demo.react`. Report what you saw, not what you expected:

- pressing "+" on the keyed list produces an entry you can fill in, and it appears in the configuration once named
- a second entry can be started while the first is still unnamed — the behaviour this plan exists to restore
- giving an entry a name another entry already has shows the error at the key field, and the entry stays out
- the key of an entry that already exists is read-only
- the map property shows its entries, adding works the same way, and no reorder buttons appear
- applying and reloading keeps everything, and the three defects fixed earlier in this ticket (the `int` write-back, clearing a primitive property, the key collision) have not returned

- [ ] **Step 3: Commit the demo property**

```bash
git commit -m "Ticket #29462: Show a map property in the configuration editor demo."
```

If a defect turns up, describe it precisely enough that someone else can write the failing test, and fix it as its own commit with the test first.

---

## Self-Review

**Spec coverage** (section 2, remaining part):

| Spec requirement | Task |
|---|---|
| `MAP` — entries as groups | 3 |
| Entries created **inline**, via a pending entry rather than a dialog | 2 |
| The key becomes immutable once the entry exists, as in the classic editor | 1 |
| An empty or duplicate key leaves the entry pending, with a field error for the duplicate | 2 |
| The pending mechanism **replaces** `checkKeyAvailable` rather than sitting beside it | 2 |
| `ARRAY`, `COMPLEX` | done in the previous plan |

**Placeholder scan:** no `TBD`, no "handle edge cases". Task 3's Step 1 elides fixture setup that follows the file's existing tests, and Step 4 gives the read side as code and describes the write side in prose — both deliberate, because the surrounding idiom is established in the file and copying it is more reliable than my transcription of it. Two steps ask the implementer to choose and justify (where the pending group is rendered; whether the configuration's map is mutated in place) rather than prescribing, because both depend on facts best read from the code.

**Type consistency:** `createKeyField(ConfigurationItem, boolean)` is defined in Task 1 and used in Task 2. `_pendingEntry`, `newEntry()`, `listenForKey(ConfigurationItem)`, `commitPending()`, `hasEntryWithKey(Object)` and `keyFieldModel()` are defined and used within Task 2, and `hasEntryWithKey` is revisited in Task 3. `elements()` / `storeElements(List<ConfigurationItem>)` come from the previous plan and are extended in Task 3. `ERROR_DUPLICATE_KEY__VALUE_PROPERTY` is a `ResKey2` filled with the key value and the key property's label, matching its two `{0}`/`{1}` placeholders.

**One risk worth stating.** Task 1 changes behaviour on its own — the key of an existing entry becomes read-only — and it lands before Task 2 makes inline creation possible. Between those two commits the editor is stricter than it was: an entry created before this work whose key is empty can no longer be given one from the form. That window is a consequence of splitting the work at the reviewable seam, and it closes with Task 2. If the two are ever executed apart, they should still land together.
