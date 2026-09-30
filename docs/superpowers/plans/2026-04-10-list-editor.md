# ConfigListEditorControl Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** A new `ConfigListEditorControl` that provides full CRUD (Add, Remove, Move Up, Move Down) for LIST properties in the view designer's configuration form, with dynamic labels that update when the user edits the title property.

**Architecture:** `ConfigListEditorControl` extends `ReactFormLayoutControl`. It renders each list element as a collapsible `ReactFormGroupControl` with icon-only action buttons (Move Up, Move Down, Remove) in the header and a nested `ConfigEditorControl` for the element's properties. An Add button at the bottom creates new elements. The control holds a reference to the parent `ConfigurationItem` and the LIST `PropertyDescriptor`, mutates the live list directly, and rebuilds its children after each operation. Dynamic labels are driven by a `ConfigurationListener` on the title property.

**Tech Stack:** Java 17, `ReactFormGroupControl`, `ReactButtonControl`, `ReactFormLayoutControl`, `TypedConfiguration`, `ConfigurationListener`, `@TitleProperty` annotation.

**Spec:** `docs/superpowers/specs/2026-04-10-list-editor-design.md`

---

## File Structure

- **Create:** `com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigListEditorControl.java` — The list editor control. Manages the list of elements, provides add/remove/move operations, builds child controls, handles dynamic labels.
- **Modify:** `com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigEditorControl.java` — Replace the existing basic LIST handling with delegation to `ConfigListEditorControl`.

---

## Task 1: Create `ConfigListEditorControl` with rendering and dynamic labels

**Files:**
- Create: `com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigListEditorControl.java`

- [ ] **Step 1: Create the class with constructor and `rebuild()` method**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.form.values.edit.annotation.TitleProperty;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.layout.ReactFormGroupControl;
import com.top_logic.layout.react.control.layout.ReactFormLayoutControl;

/**
 * A {@link ReactControl} that renders a full editor for a LIST property of a
 * {@link ConfigurationItem}.
 *
 * <p>
 * Each list element is rendered as a collapsible {@link ReactFormGroupControl} with action buttons
 * (Move Up, Move Down, Remove) in the header and a nested {@link ConfigEditorControl} for the
 * element's properties. An Add button at the bottom creates new elements.
 * </p>
 *
 * <p>
 * Group labels are dynamic: a {@link ConfigurationListener} on the title property updates the
 * header when the user edits the identifying value.
 * </p>
 */
public class ConfigListEditorControl extends ReactFormLayoutControl {

	private final ReactContext _context;

	private final ConfigurationItem _parentConfig;

	private final PropertyDescriptor _property;

	/**
	 * Creates a {@link ConfigListEditorControl}.
	 *
	 * @param context
	 *        The React context.
	 * @param parentConfig
	 *        The parent configuration item owning the LIST property.
	 * @param property
	 *        The LIST property descriptor.
	 */
	public ConfigListEditorControl(ReactContext context, ConfigurationItem parentConfig,
			PropertyDescriptor property) {
		super(context);
		_context = context;
		_parentConfig = parentConfig;
		_property = property;

		rebuild();
	}

	/**
	 * Clears all children and rebuilds them from the current list state.
	 */
	@SuppressWarnings("unchecked")
	private void rebuild() {
		for (ReactControl child : getChildren()) {
			child.cleanupTree();
		}
		getChildren().clear();

		List<ConfigurationItem> items = (List<ConfigurationItem>) _parentConfig.value(_property);
		if (items != null) {
			for (int i = 0; i < items.size(); i++) {
				ConfigurationItem item = items.get(i);
				addChild(createElementGroup(item, i, items.size()));
			}
		}

		// Add button at the bottom.
		ReactButtonControl addButton = new ReactButtonControl(_context, addButtonLabel(), ctx -> {
			addElement();
			return com.top_logic.tool.boundsec.HandlerResult.DEFAULT_RESULT;
		});
		addChild(addButton);

		putState("children", getChildren());
	}

	private String addButtonLabel() {
		return "+ " + Labels.propertyLabel(_property, false);
	}

	private ReactFormGroupControl createElementGroup(ConfigurationItem item, int index, int listSize) {
		String label = resolveElementLabel(item, index);

		// Action buttons: Move Up, Move Down, Remove.
		ReactButtonControl moveUpButton = new ReactButtonControl(_context, "\u25B2", ctx -> {
			moveUp(index);
			return com.top_logic.tool.boundsec.HandlerResult.DEFAULT_RESULT;
		});
		moveUpButton.setIconOnly(true);
		moveUpButton.setDisabled(index == 0);

		ReactButtonControl moveDownButton = new ReactButtonControl(_context, "\u25BC", ctx -> {
			moveDown(index);
			return com.top_logic.tool.boundsec.HandlerResult.DEFAULT_RESULT;
		});
		moveDownButton.setIconOnly(true);
		moveDownButton.setDisabled(index == listSize - 1);

		ReactButtonControl removeButton = new ReactButtonControl(_context, "\u2715", ctx -> {
			removeElement(index);
			return com.top_logic.tool.boundsec.HandlerResult.DEFAULT_RESULT;
		});
		removeButton.setIconOnly(true);

		List<ReactControl> headerActions = List.of(moveUpButton, moveDownButton, removeButton);

		ConfigEditorControl nestedEditor = new ConfigEditorControl(_context, item);
		ReactFormGroupControl group = new ReactFormGroupControl(
			_context, label, true, true, "subtle", true,
			headerActions, List.of(nestedEditor));

		// Register dynamic label update on the title property.
		PropertyDescriptor titleProp = resolveTitleProperty(item);
		if (titleProp != null) {
			item.addConfigurationListener(titleProp, change -> {
				group.setHeader(resolveElementLabel(item, indexOf(item)));
			});
		}

		return group;
	}

	@SuppressWarnings("unchecked")
	private int indexOf(ConfigurationItem item) {
		List<ConfigurationItem> items = (List<ConfigurationItem>) _parentConfig.value(_property);
		return items != null ? items.indexOf(item) : -1;
	}

	// --- Operations ---

	/**
	 * Adds a new element with default values to the end of the list.
	 */
	@SuppressWarnings("unchecked")
	private void addElement() {
		List<ConfigurationItem> items = (List<ConfigurationItem>) _parentConfig.value(_property);
		Class<? extends ConfigurationItem> elementType = resolveNewElementType();
		ConfigurationItem newItem = TypedConfiguration.newConfigItem(elementType);
		items.add(newItem);
		rebuild();
	}

	/**
	 * Removes the element at the given index.
	 */
	@SuppressWarnings("unchecked")
	private void removeElement(int index) {
		List<ConfigurationItem> items = (List<ConfigurationItem>) _parentConfig.value(_property);
		if (items != null && index >= 0 && index < items.size()) {
			items.remove(index);
			rebuild();
		}
	}

	/**
	 * Moves the element at the given index one position up.
	 */
	@SuppressWarnings("unchecked")
	private void moveUp(int index) {
		List<ConfigurationItem> items = (List<ConfigurationItem>) _parentConfig.value(_property);
		if (items != null && index > 0 && index < items.size()) {
			ConfigurationItem item = items.remove(index);
			items.add(index - 1, item);
			rebuild();
		}
	}

	/**
	 * Moves the element at the given index one position down.
	 */
	@SuppressWarnings("unchecked")
	private void moveDown(int index) {
		List<ConfigurationItem> items = (List<ConfigurationItem>) _parentConfig.value(_property);
		if (items != null && index >= 0 && index < items.size() - 1) {
			ConfigurationItem item = items.remove(index);
			items.add(index + 1, item);
			rebuild();
		}
	}

	// --- Label resolution ---

	/**
	 * Resolves the display label for a list element.
	 *
	 * <p>
	 * Resolution order: {@link TitleProperty} annotation, key property, property named "name",
	 * property named "id", TagName + index, simple interface name + index.
	 * </p>
	 */
	private String resolveElementLabel(ConfigurationItem item, int index) {
		PropertyDescriptor titleProp = resolveTitleProperty(item);
		if (titleProp != null) {
			Object value = item.value(titleProp);
			if (value instanceof String s && !s.isEmpty()) {
				return s;
			}
		}

		// Fallback: TagName or simple interface name with index.
		Class<?> iface = item.descriptor().getConfigurationInterface();
		TagName tagName = iface.getAnnotation(TagName.class);
		String typeName;
		if (tagName != null) {
			typeName = tagName.value();
		} else {
			typeName = iface.getSimpleName();
			if (typeName.endsWith("Config")) {
				typeName = typeName.substring(0, typeName.length() - "Config".length());
			}
		}
		return typeName + " " + (index + 1);
	}

	/**
	 * Resolves the title property for a list element.
	 *
	 * <p>
	 * Checks {@link TitleProperty} on the LIST property, then on the element type, then the key
	 * property, then common names ("name", "id").
	 * </p>
	 */
	private PropertyDescriptor resolveTitleProperty(ConfigurationItem item) {
		// 1. @TitleProperty on the LIST property itself.
		TitleProperty titleOnList = _property.getAnnotation(TitleProperty.class);
		if (titleOnList != null && !titleOnList.name().isEmpty()) {
			PropertyDescriptor prop = item.descriptor().getProperty(titleOnList.name());
			if (prop != null) {
				return prop;
			}
		}

		// 2. @TitleProperty on the element type.
		TitleProperty titleOnType = item.descriptor().getConfigurationInterface().getAnnotation(TitleProperty.class);
		if (titleOnType != null && !titleOnType.name().isEmpty()) {
			PropertyDescriptor prop = item.descriptor().getProperty(titleOnType.name());
			if (prop != null) {
				return prop;
			}
		}

		// 3. Key property.
		PropertyDescriptor keyProp = _property.getKeyProperty();
		if (keyProp != null) {
			return keyProp;
		}

		// 4. Common names.
		for (String name : new String[] { "name", "id" }) {
			PropertyDescriptor prop = item.descriptor().getProperty(name);
			if (prop != null && prop.kind() == PropertyKind.PLAIN) {
				return prop;
			}
		}

		return null;
	}

	// --- Element type resolution ---

	/**
	 * Resolves the configuration interface to instantiate for new list elements.
	 */
	@SuppressWarnings("unchecked")
	private Class<? extends ConfigurationItem> resolveNewElementType() {
		return (Class<? extends ConfigurationItem>) _property.getDefaultDescriptor().getConfigurationInterface();
	}
}
```

- [ ] **Step 2: Build the module**

Run: `mvn -B compile -pl com.top_logic.layout.configedit 2>&1 | tail -5`
Expected: BUILD SUCCESS (or fix compilation errors)

- [ ] **Step 3: Commit**

```
Ticket #29108: Add ConfigListEditorControl with full CRUD and dynamic labels.
```

---

## Task 2: Integrate `ConfigListEditorControl` into `ConfigEditorControl`

**Files:**
- Modify: `com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigEditorControl.java`

- [ ] **Step 1: Replace the existing LIST handling block**

In the constructor, replace the entire `if (property.kind() == PropertyKind.LIST) { ... }` block with:

```java
if (property.kind() == PropertyKind.LIST) {
    String label = resolveLabel(property);
    ConfigListEditorControl listEditor = new ConfigListEditorControl(context, config, property);
    ReactFormGroupControl group = new ReactFormGroupControl(
        context, label, true, false, "default", false,
        List.of(), List.of(listEditor));
    addChild(group);
    continue;
}
```

- [ ] **Step 2: Remove the `resolveListItemLabel` method**

The `resolveListItemLabel` static method is no longer needed — label resolution is now in `ConfigListEditorControl.resolveElementLabel()`. Delete it entirely.

- [ ] **Step 3: Build the module**

Run: `mvn -B install -pl com.top_logic.layout.configedit 2>&1 | tail -5`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```
Ticket #29108: Integrate ConfigListEditorControl into ConfigEditorControl.
```

---

## Task 3: Build, deploy, and verify

**Files:** none (verification only)

- [ ] **Step 1: Build the demo app**

Run: `mvn -B install -pl com.top_logic.demo -Dmaven.javadoc.skip=true 2>&1 | tail -5`
Expected: BUILD SUCCESS

- [ ] **Step 2: Restart the demo app**

Run: `.claude/scripts/tl-app.sh restart com.top_logic.demo`

- [ ] **Step 3: Open the designer and verify**

Using Playwright:
1. Login, open designer
2. Select the `view` node — verify the form shows a **Kanäle** (channels) group
3. Expand the channels group — each channel should be a collapsible sub-group with its `name` as label
4. Verify action buttons (Move Up, Move Down, Remove) are visible in each element header
5. Verify the Add button at the bottom of the list
6. Test Add: click Add, verify a new empty channel appears
7. Test Remove: click Remove on a channel, verify it disappears
8. Test Move: click Move Down on first channel, verify order changes
9. Edit a channel's name field, verify the group label updates dynamically
10. Select a `panel` node — verify the form shows **Befehle** (commands) group (even if empty, with Add button)

- [ ] **Step 4: Commit any fixes found during verification**
