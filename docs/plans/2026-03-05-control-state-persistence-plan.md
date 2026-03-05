# Control State Persistence Refactoring — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Remove `PersonalConfiguration` access from React controls and move persistence logic to the UIElement wiring layer, using direct callback parameters.

**Architecture:** Controls receive initial state + change callbacks via constructor. UIElement implementations pre-load state from `PersonalConfiguration` and pass lambdas that write back. `ViewContext` provides auto-derived personalization keys with optional override.

**Tech Stack:** Java 17, TopLogic TypedConfiguration, PersonalConfiguration API, JUnit 4

---

### Task 1: Add personalization key to ViewContext

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewContext.java`

**Step 1: Add `_personalizationPath` field and `childContext()` method**

Replace the entire `ViewContext.java` with:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import com.top_logic.layout.react.ViewDisplayContext;

/**
 * Hierarchical context for UIElement control creation.
 *
 * <p>
 * Provides session-scoped infrastructure needed to create and wire controls. Container elements may
 * create derived contexts that add scoped information for their children.
 * </p>
 */
public class ViewContext {

	private final ViewDisplayContext _displayContext;

	private final String _personalizationPath;

	/**
	 * Creates a root {@link ViewContext}.
	 *
	 * @param displayContext
	 *        The view display context providing ID allocation, SSE queue and other rendering
	 *        infrastructure.
	 */
	public ViewContext(ViewDisplayContext displayContext) {
		this(displayContext, "view");
	}

	private ViewContext(ViewDisplayContext displayContext, String personalizationPath) {
		_displayContext = displayContext;
		_personalizationPath = personalizationPath;
	}

	/**
	 * Creates a child context with an appended path segment.
	 *
	 * @param segment
	 *        The segment to append (e.g. "sidebar", "split-panel").
	 * @return A new context with the extended personalization path.
	 */
	public ViewContext childContext(String segment) {
		return new ViewContext(_displayContext, _personalizationPath + "." + segment);
	}

	/**
	 * The personalization key for the current position in the view tree.
	 *
	 * <p>
	 * Auto-derived from the tree path (e.g. "view.sidebar", "view.split-panel"). UIElements may
	 * override this with an explicit personalization key from configuration.
	 * </p>
	 */
	public String getPersonalizationKey() {
		return _personalizationPath;
	}

	/**
	 * The {@link ViewDisplayContext} for the current session.
	 */
	public ViewDisplayContext getDisplayContext() {
		return _displayContext;
	}
}
```

**Step 2: Commit**

```
Ticket #29108: Add personalization key infrastructure to ViewContext.
```

---

### Task 2: Add optional `personalization-key` to UIElement.Config

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/UIElement.java`

**Step 1: Add the property to the Config interface**

Replace the Config interface body (lines 30-32) — the comment inside — with:

```java
	interface Config extends PolymorphicConfiguration<UIElement> {

		/** Configuration name for {@link #getPersonalizationKey()}. */
		String PERSONALIZATION_KEY = "personalization-key";

		/**
		 * Optional override for the auto-derived personalization key.
		 *
		 * <p>
		 * If set, stateful elements use this key instead of the auto-derived path from
		 * {@link ViewContext#getPersonalizationKey()}.
		 * </p>
		 */
		@com.top_logic.basic.config.annotation.Name(PERSONALIZATION_KEY)
		String getPersonalizationKey();
	}
```

**Step 2: Commit**

```
Ticket #29108: Add optional personalization-key property to UIElement.Config.
```

---

### Task 3: Refactor ReactSidebarControl — remove PersonalConfiguration

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/sidebar/ReactSidebarControl.java`

This is the biggest single change. The control must become persistence-agnostic.

**Step 1: Change imports**

Remove:
```java
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.structure.PersonalizingExpandable;
```

Add:
```java
import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
```

**Step 2: Replace fields**

Remove `_personalizationKey` and `_defaultCollapsed` fields. Add callback fields:

```java
	private final Consumer<Boolean> _onCollapseChanged;

	private final BiConsumer<String, Boolean> _onGroupToggled;
```

**Step 3: Replace the main constructor**

New signature and body — the control no longer loads state itself. It receives pre-loaded values:

```java
	/**
	 * Creates a new {@link ReactSidebarControl}.
	 *
	 * @param items
	 *        The navigation item list.
	 * @param initialActiveItemId
	 *        The initially active navigation item ID, or {@code null} to default to the first
	 *        navigation item.
	 * @param initialCollapsed
	 *        The initial collapsed state (pre-loaded by the caller).
	 * @param initialGroupStates
	 *        Pre-loaded group expansion state overrides. Keys are group IDs, values are expanded
	 *        flags. May be {@code null} or empty for defaults.
	 * @param onCollapseChanged
	 *        Called when the user toggles the sidebar collapse. Receives the new collapsed state.
	 *        May be {@code null} if no persistence is desired.
	 * @param onGroupToggled
	 *        Called when the user toggles a group's expansion. Receives (groupId, expanded).
	 *        May be {@code null} if no persistence is desired.
	 * @param headerContent
	 *        Optional header slot control shown when expanded, or {@code null}.
	 * @param headerCollapsedContent
	 *        Optional header slot control shown when collapsed, or {@code null}.
	 * @param footerContent
	 *        Optional footer slot control shown when expanded, or {@code null}.
	 * @param footerCollapsedContent
	 *        Optional footer slot control shown when collapsed, or {@code null}.
	 */
	public ReactSidebarControl(List<SidebarItem> items, String initialActiveItemId,
			boolean initialCollapsed, Map<String, Boolean> initialGroupStates,
			Consumer<Boolean> onCollapseChanged, BiConsumer<String, Boolean> onGroupToggled,
			ReactControl headerContent, ReactControl headerCollapsedContent,
			ReactControl footerContent, ReactControl footerCollapsedContent) {
		super(null, REACT_MODULE, COMMANDS);
		_items = new ArrayList<>(items);
		_collapsed = initialCollapsed;
		_onCollapseChanged = onCollapseChanged;
		_onGroupToggled = onGroupToggled;
		_headerContent = headerContent;
		_headerCollapsedContent = headerCollapsedContent;
		_footerContent = footerContent;
		_footerCollapsedContent = footerCollapsedContent;

		// Determine initial active item.
		_activeItemId = initialActiveItemId != null ? initialActiveItemId : findFirstNavItemId(items);

		// Build serialized item list, merging pre-loaded group states.
		Map<String, Boolean> groupStates =
			initialGroupStates != null ? initialGroupStates : Collections.emptyMap();
		List<Map<String, Object>> itemList = new ArrayList<>();
		for (SidebarItem item : _items) {
			Map<String, Object> itemMap = item.toStateMap();
			if (item instanceof GroupItem) {
				Boolean persisted = groupStates.get(item.getId());
				if (persisted != null) {
					itemMap.put(GroupItem.EXPANDED, persisted);
				}
			}
			itemList.add(itemMap);
		}

		getReactState().put(ITEMS, itemList);
		getReactState().put(ACTIVE_ITEM_ID, _activeItemId);
		getReactState().put(COLLAPSED, Boolean.valueOf(_collapsed));
		if (_headerContent != null) {
			getReactState().put(HEADER_CONTENT, _headerContent);
		}
		if (_headerCollapsedContent != null) {
			getReactState().put(HEADER_COLLAPSED_CONTENT, _headerCollapsedContent);
		}
		if (_footerContent != null) {
			getReactState().put(FOOTER_CONTENT, _footerContent);
		}
		if (_footerCollapsedContent != null) {
			getReactState().put(FOOTER_COLLAPSED_CONTENT, _footerCollapsedContent);
		}
	}
```

**Step 4: Replace the convenience constructor**

```java
	/**
	 * Convenience constructor without collapsed-mode slot alternatives and no callbacks.
	 */
	public ReactSidebarControl(List<SidebarItem> items, String initialActiveItemId,
			boolean initialCollapsed, ReactControl headerContent, ReactControl footerContent) {
		this(items, initialActiveItemId, initialCollapsed, null, null, null,
			headerContent, null, footerContent, null);
	}
```

**Step 5: Fix `pushItemsUpdate()` — remove loadGroupStates call**

The `pushItemsUpdate()` method (used by `updateBadge()`) currently re-reads group states from PersonalConfiguration. Instead, track group states in a field. Add a field:

```java
	private final Map<String, Boolean> _groupStates;
```

Initialize it in the constructor:
```java
		_groupStates = new HashMap<>(groupStates);
```

Then `pushItemsUpdate()` becomes:
```java
	private void pushItemsUpdate() {
		List<Map<String, Object>> itemList = new ArrayList<>();
		for (SidebarItem item : _items) {
			Map<String, Object> itemMap = item.toStateMap();
			if (item instanceof GroupItem) {
				Boolean tracked = _groupStates.get(item.getId());
				if (tracked != null) {
					itemMap.put(GroupItem.EXPANDED, tracked);
				}
			}
			itemList.add(itemMap);
		}

		if (isSSEAttached()) {
			patchReactState(Map.of(ITEMS, itemList));
		} else {
			getReactState().put(ITEMS, itemList);
		}
	}
```

**Step 6: Fix ToggleCollapseCommand**

Replace the `execute` method body (lines 497-504):

```java
		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactSidebarControl sidebar = (ReactSidebarControl) control;
			sidebar._collapsed = !sidebar._collapsed;
			if (sidebar._onCollapseChanged != null) {
				sidebar._onCollapseChanged.accept(Boolean.valueOf(sidebar._collapsed));
			}
			sidebar.patchReactState(Map.of(COLLAPSED, Boolean.valueOf(sidebar._collapsed)));
			return HandlerResult.DEFAULT_RESULT;
		}
```

**Step 7: Fix ToggleGroupCommand**

Replace the `execute` method body (lines 525-531):

```java
		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactSidebarControl sidebar = (ReactSidebarControl) control;
			String itemId = (String) arguments.get(ITEM_ID_ARG);
			Object expandedObj = arguments.get(EXPANDED_ARG);
			boolean expanded = expandedObj instanceof Boolean && ((Boolean) expandedObj).booleanValue();
			sidebar._groupStates.put(itemId, Boolean.valueOf(expanded));
			if (sidebar._onGroupToggled != null) {
				sidebar._onGroupToggled.accept(itemId, Boolean.valueOf(expanded));
			}
			return HandlerResult.DEFAULT_RESULT;
		}
```

**Step 8: Delete `loadGroupStates()`, `saveGroupState()`, and `findGroup()` methods** (lines 366-422)

These are no longer needed — persistence moves to SidebarElement.

**Step 9: Build to verify compilation**

Run: `mvn compile -pl com.top_logic.layout.react -am`

This will fail because demo callers still use the old constructor. That's expected — we fix them in Task 5.

**Step 10: Commit**

```
Ticket #29108: Remove PersonalConfiguration from ReactSidebarControl, use callbacks.
```

---

### Task 4: Add callbacks to ReactSplitPanelControl

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/layout/ReactSplitPanelControl.java`

**Step 1: Add callback fields and imports**

Add imports:
```java
import java.util.LinkedHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
```

Add fields:
```java
	private final Consumer<Map<String, Float>> _onSizesChanged;

	private final BiConsumer<Integer, Boolean> _onChildCollapsed;
```

**Step 2: Add new constructor with callbacks**

```java
	/**
	 * Creates a new {@link ReactSplitPanelControl} with persistence callbacks.
	 *
	 * @param orientation
	 *        The layout orientation (horizontal or vertical).
	 * @param resizable
	 *        Whether draggable splitters are shown between children.
	 * @param onSizesChanged
	 *        Called after a drag-resize with a map of control ID to new pixel size. May be
	 *        {@code null}.
	 * @param onChildCollapsed
	 *        Called when a child collapses or expands, with (childIndex, collapsed). May be
	 *        {@code null}.
	 */
	public ReactSplitPanelControl(Orientation orientation, boolean resizable,
			Consumer<Map<String, Float>> onSizesChanged,
			BiConsumer<Integer, Boolean> onChildCollapsed) {
		super(null, REACT_MODULE, COMMANDS);
		_resizable = resizable;
		_onSizesChanged = onSizesChanged;
		_onChildCollapsed = onChildCollapsed;

		getReactState().put(ORIENTATION, orientation == Orientation.HORIZONTAL ? "horizontal" : "vertical");
		getReactState().put(RESIZABLE, Boolean.valueOf(resizable));
		getReactState().put(CHILDREN, new ArrayList<>());
	}
```

**Step 3: Modify existing 2-param constructor to delegate**

```java
	/**
	 * Creates a new {@link ReactSplitPanelControl} without persistence callbacks.
	 */
	public ReactSplitPanelControl(Orientation orientation, boolean resizable) {
		this(orientation, resizable, null, null);
	}
```

**Step 4: Add callback invocation to `UpdateSizesCommand.execute()`**

After the existing size-update logic (after the `list.clear()` / `list.add()` loop, before the `return`), add:

```java
				if (splitPanel._onSizesChanged != null) {
					Map<String, Float> sizeMap = new LinkedHashMap<>();
					for (ChildEntry entry : splitPanel._children) {
						sizeMap.put(entry._control.getID(), Float.valueOf(entry._constraint._size));
					}
					splitPanel._onSizesChanged.accept(sizeMap);
				}
```

**Step 5: Add callback invocation to `childCollapsed()`**

After the cascading collapse logic (after the parent notification block), add:

```java
		if (_onChildCollapsed != null) {
			_onChildCollapsed.accept(Integer.valueOf(childIndex), Boolean.valueOf(collapsed));
		}
```

**Step 6: Build to verify compilation**

Run: `mvn compile -pl com.top_logic.layout.react -am`

**Step 7: Commit**

```
Ticket #29108: Add nullable persistence callbacks to ReactSplitPanelControl.
```

---

### Task 5: Update demo callers

**Files:**
- Modify: `com.top_logic.demo/src/main/java/com/top_logic/demo/react/DemoReactSidebarComponent.java:109-111`
- Modify: `com.top_logic.demo/src/main/java/com/top_logic/demo/react/DemoReactAppComponent.java:182-188`

**Step 1: Fix DemoReactSidebarComponent**

Add imports:
```java
import java.util.Collections;
import com.top_logic.layout.structure.PersonalizingExpandable;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
```

Replace lines 109-111 with:

```java
		boolean collapsed = PersonalizingExpandable.loadCollapsed("demo.sidebar.collapsed", false);
		Map<String, Boolean> groupStates = loadGroupStates("demo.sidebar");

		return new ReactSidebarControl(
			items, "dashboard",
			collapsed, groupStates,
			c -> PersonalizingExpandable.saveCollapsed("demo.sidebar.collapsed", c, false),
			(gid, exp) -> saveGroupState("demo.sidebar", gid, exp),
			headerSlot, headerCollapsedSlot, footerSlot, footerCollapsedSlot);
```

Add the helper methods (copied from the old ReactSidebarControl, adapted to static with key param):

```java
	@SuppressWarnings("unchecked")
	private static Map<String, Boolean> loadGroupStates(String key) {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return null;
		}
		Object value = pc.getJSONValue(key + ".groups");
		if (value instanceof Map) {
			Map<String, Object> raw = (Map<String, Object>) value;
			Map<String, Boolean> result = new java.util.HashMap<>();
			for (Map.Entry<String, Object> entry : raw.entrySet()) {
				if (entry.getValue() instanceof Boolean) {
					result.put(entry.getKey(), (Boolean) entry.getValue());
				}
			}
			return result;
		}
		return null;
	}

	private static void saveGroupState(String key, String groupId, boolean expanded) {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return;
		}
		Map<String, Boolean> states = loadGroupStates(key);
		if (states == null) {
			states = new java.util.HashMap<>();
		}
		states.put(groupId, Boolean.valueOf(expanded));
		if (states.isEmpty()) {
			pc.setJSONValue(key + ".groups", null);
		} else {
			pc.setJSONValue(key + ".groups", states);
		}
	}
```

**Step 2: Fix DemoReactAppComponent**

Add imports:
```java
import java.util.Collections;
import com.top_logic.layout.structure.PersonalizingExpandable;
```

Replace lines 182-188 with:

```java
		boolean collapsed = PersonalizingExpandable.loadCollapsed("demo.app.collapsed", false);

		return new ReactSidebarControl(
			items, PAGE_DASHBOARD,
			collapsed, null,
			c -> PersonalizingExpandable.saveCollapsed("demo.app.collapsed", c, false),
			null,
			null, null, null, null) {
			@Override
			public void selectItem(String itemId) {
				super.selectItem(itemId);
				onNavigate(itemId);
			}
		};
```

**Step 3: Build the demo module**

Run: `mvn compile -pl com.top_logic.demo -am`

Expected: BUILD SUCCESS

**Step 4: Commit**

```
Ticket #29108: Update demo callers to use callback-based ReactSidebarControl.
```

---

### Task 6: Wire SidebarElement persistence

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/SidebarElement.java`

**Step 1: Add imports**

```java
import java.util.HashMap;
import java.util.Map;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.structure.PersonalizingExpandable;
```

**Step 2: Modify `createControl()`**

Replace the current `createControl()` method (lines 219-226) with:

```java
	@Override
	public ViewControl createControl(ViewContext context) {
		String key = resolveKey(context, "sidebar");

		boolean collapsed = PersonalizingExpandable.loadCollapsed(key + ".collapsed", _collapsed);
		Map<String, Boolean> groupStates = loadGroupStates(key);

		List<SidebarItem> sidebarItems = new ArrayList<>();
		for (SidebarItemElement itemElement : _items) {
			sidebarItems.add(itemElement.createSidebarItem(context));
		}
		String activeItem = _activeItem != null && !_activeItem.isEmpty() ? _activeItem : null;
		return new ReactSidebarControl(
			sidebarItems, activeItem,
			collapsed, groupStates,
			c -> PersonalizingExpandable.saveCollapsed(key + ".collapsed", c, _collapsed),
			(gid, exp) -> saveGroupState(key, gid, exp),
			null, null, null, null);
	}
```

**Step 3: Add helper methods to SidebarElement**

```java
	private static String resolveKey(ViewContext context, String defaultSegment) {
		// For now, use the auto-derived path. The optional override from Config will be
		// wired when UIElement.Config gains the property accessor.
		return context.getPersonalizationKey() + "." + defaultSegment;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Boolean> loadGroupStates(String key) {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return null;
		}
		Object value = pc.getJSONValue(key + ".groups");
		if (value instanceof Map) {
			Map<String, Object> raw = (Map<String, Object>) value;
			Map<String, Boolean> result = new HashMap<>();
			for (Map.Entry<String, Object> entry : raw.entrySet()) {
				if (entry.getValue() instanceof Boolean) {
					result.put(entry.getKey(), (Boolean) entry.getValue());
				}
			}
			return result;
		}
		return null;
	}

	private static void saveGroupState(String key, String groupId, boolean expanded) {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return;
		}
		Map<String, Boolean> states = loadGroupStates(key);
		if (states == null) {
			states = new HashMap<>();
		}
		states.put(groupId, Boolean.valueOf(expanded));
		if (states.isEmpty()) {
			pc.setJSONValue(key + ".groups", null);
		} else {
			pc.setJSONValue(key + ".groups", states);
		}
	}
```

**Step 4: Build**

Run: `mvn compile -pl com.top_logic.layout.view -am`

Expected: BUILD SUCCESS

**Step 5: Commit**

```
Ticket #29108: Wire SidebarElement to persist collapse and group states via callbacks.
```

---

### Task 7: Wire SplitPanelElement persistence

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/SplitPanelElement.java`

**Step 1: Add imports**

```java
import java.util.HashMap;
import java.util.Map;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
```

**Step 2: Modify `createControl()`**

Replace the current `createControl()` method (lines 153-165) with:

```java
	@Override
	public ViewControl createControl(ViewContext context) {
		String key = resolveKey(context, "split-panel");

		Map<Integer, Float> persistedSizes = loadPaneSizes(key);
		Map<Integer, Boolean> persistedCollapse = loadCollapseStates(key);

		Orientation orientation = "vertical".equals(_orientation) ? Orientation.VERTICAL : Orientation.HORIZONTAL;
		ReactSplitPanelControl splitPanel = new ReactSplitPanelControl(orientation, _resizable,
			sizes -> savePaneSizes(key, sizes),
			(idx, collapsed) -> saveCollapseState(key, idx, collapsed));

		for (int i = 0; i < _panes.size(); i++) {
			PaneEntry pane = _panes.get(i);
			float size = persistedSizes.containsKey(Integer.valueOf(i))
				? persistedSizes.get(Integer.valueOf(i)).floatValue() : pane._size;
			DisplayUnit unit = persistedSizes.containsKey(Integer.valueOf(i))
				? DisplayUnit.PIXEL : ("%".equals(pane._unit) ? DisplayUnit.PERCENT : DisplayUnit.PIXEL);
			ChildConstraint constraint = new ChildConstraint(size, unit, pane._minSize, Scrolling.AUTO);
			ReactControl content = createContent(pane._children, context);
			splitPanel.addChild(content, constraint);
		}

		return splitPanel;
	}
```

**Step 3: Add persistence helpers**

```java
	private static String resolveKey(ViewContext context, String defaultSegment) {
		return context.getPersonalizationKey() + "." + defaultSegment;
	}

	@SuppressWarnings("unchecked")
	private static Map<Integer, Float> loadPaneSizes(String key) {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return Map.of();
		}
		Object value = pc.getJSONValue(key + ".sizes");
		if (value instanceof Map) {
			Map<String, Object> raw = (Map<String, Object>) value;
			Map<Integer, Float> result = new HashMap<>();
			for (Map.Entry<String, Object> entry : raw.entrySet()) {
				try {
					int index = Integer.parseInt(entry.getKey());
					if (entry.getValue() instanceof Number) {
						result.put(Integer.valueOf(index), Float.valueOf(((Number) entry.getValue()).floatValue()));
					}
				} catch (NumberFormatException e) {
					// Skip corrupt entries.
				}
			}
			return result;
		}
		return Map.of();
	}

	private static void savePaneSizes(String key, Map<String, Float> controlIdToSize) {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return;
		}
		// The callback receives controlId->size, but we store by pane index for stability.
		// For now, store the raw map. The control IDs are session-scoped, so index-based
		// storage would be better — but that requires the split panel to report indices.
		// We store by index order (iteration order of the map is insertion order).
		Map<String, Object> indexed = new HashMap<>();
		int i = 0;
		for (Map.Entry<String, Float> entry : controlIdToSize.entrySet()) {
			indexed.put(String.valueOf(i), entry.getValue());
			i++;
		}
		pc.setJSONValue(key + ".sizes", indexed);
	}

	@SuppressWarnings("unchecked")
	private static Map<Integer, Boolean> loadCollapseStates(String key) {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return Map.of();
		}
		Object value = pc.getJSONValue(key + ".collapse");
		if (value instanceof Map) {
			Map<String, Object> raw = (Map<String, Object>) value;
			Map<Integer, Boolean> result = new HashMap<>();
			for (Map.Entry<String, Object> entry : raw.entrySet()) {
				try {
					int index = Integer.parseInt(entry.getKey());
					if (entry.getValue() instanceof Boolean) {
						result.put(Integer.valueOf(index), (Boolean) entry.getValue());
					}
				} catch (NumberFormatException e) {
					// Skip corrupt entries.
				}
			}
			return result;
		}
		return Map.of();
	}

	private static void saveCollapseState(String key, int childIndex, boolean collapsed) {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return;
		}
		Map<Integer, Boolean> states = loadCollapseStates(key);
		Map<String, Object> store = new HashMap<>();
		for (Map.Entry<Integer, Boolean> entry : states.entrySet()) {
			store.put(entry.getKey().toString(), entry.getValue());
		}
		if (collapsed) {
			store.put(String.valueOf(childIndex), Boolean.TRUE);
		} else {
			store.remove(String.valueOf(childIndex));
		}
		if (store.isEmpty()) {
			pc.setJSONValue(key + ".collapse", null);
		} else {
			pc.setJSONValue(key + ".collapse", store);
		}
	}
```

**Step 4: Build**

Run: `mvn compile -pl com.top_logic.layout.view -am`

Expected: BUILD SUCCESS

**Step 5: Commit**

```
Ticket #29108: Wire SplitPanelElement to persist pane sizes and collapse states.
```

---

### Task 8: Full build and existing test verification

**Files:** None (verification only)

**Step 1: Build the full project**

Run: `mvn compile -pl com.top_logic.layout.view,com.top_logic.layout.react,com.top_logic.demo -am`

Expected: BUILD SUCCESS

**Step 2: Run existing tests**

Run: `cd com.top_logic.layout.view && mvn test -DskipTests=false`

Expected: All tests pass (TestViewElement still parses XML correctly — it doesn't test personalization).

**Step 3: Commit (if any fixes needed)**

---

### Task 9: Regenerate message properties

**Files:**
- `com.top_logic.layout.view/src/main/java/META-INF/messages_en.properties`
- `com.top_logic.layout.view/src/main/java/META-INF/messages_de.properties`

**Step 1: Build the view module to regenerate**

Run: `cd com.top_logic.layout.view && mvn install`

This regenerates message properties from JavaDoc/Label annotations. The new `personalization-key` property in UIElement.Config will get auto-generated labels.

**Step 2: Check generated messages**

Run: `git diff com.top_logic.layout.view/src/main/java/META-INF/messages_*.properties`

Verify the new property appears with a sensible label.

**Step 3: Commit the regenerated files**

```
Ticket #29108: Regenerate messages after adding personalization-key property.
```
