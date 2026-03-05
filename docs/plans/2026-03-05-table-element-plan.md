# TableElement Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add a declarative `<table>` UIElement to the view system that wraps `ReactTableControl`, using ViewChannels for input data and TL-Script expressions for row derivation.

**Architecture:** `TableElement` is a `UIElement` with `@TagName("table")`. Its Config declares input channels (`List<ChannelRef>`), TL-Script expressions (`Expr`) for row computation and incremental update support, a `TableConfigurationProvider` for columns, and an optional selection output channel. Expression compilation happens once in the constructor; per-session wiring (channel resolution, table model creation, listener registration) happens in `createControl(ViewContext)`.

**Tech Stack:** Java 17, TypedConfiguration, QueryExecutor (TL-Script), ReactTableControl, ViewChannel

**Design doc:** `docs/plans/2026-03-05-table-element-design.md`

---

### Task 1: Add Maven Dependencies

**Files:**
- Modify: `com.top_logic.layout.view/pom.xml`

**Step 1: Add tl-core and model.search dependencies**

Add these dependencies to the `<dependencies>` section in `com.top_logic.layout.view/pom.xml`:

```xml
    <dependency>
      <groupId>com.top-logic</groupId>
      <artifactId>tl-core</artifactId>
    </dependency>

    <dependency>
      <groupId>com.top-logic</groupId>
      <artifactId>tl-model-search</artifactId>
    </dependency>
```

Insert after the existing `tl-layout-react` dependency (line 24) and before the test dependency.

**Step 2: Verify build compiles**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn compile -q`
Expected: BUILD SUCCESS

**Step 3: Commit**

```
Ticket #29108: Add tl-core and tl-model-search dependencies to tl-layout-view.
```

---

### Task 2: Add SelectionListener to ReactTableControl

`ReactTableControl` currently manages selection internally with no callback mechanism. We need a listener so `TableElement` can wire selection changes to a ViewChannel.

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/table/ReactTableControl.java`

**Step 1: Add SelectionListener interface and field**

Add inside `ReactTableControl`, near the top (after the existing member fields around line 142):

```java
/**
 * Listener notified when the set of selected rows changes.
 */
public interface SelectionListener {

    /**
     * Called after the selection has changed.
     *
     * @param selectedRows
     *        The current set of selected row objects (unmodifiable).
     */
    void selectionChanged(Set<Object> selectedRows);
}

private SelectionListener _selectionListener;
```

**Step 2: Add setter method**

Add a public setter (near the existing `setSelectionMode`, `setFrozenColumnCount` methods):

```java
/**
 * Sets the listener to be notified on selection changes.
 *
 * @param listener
 *        The listener, or {@code null} to remove.
 */
public void setSelectionListener(SelectionListener listener) {
    _selectionListener = listener;
}
```

**Step 3: Fire listener after selection changes**

In `SelectCommand.execute()`, after line 656 (`table.updateViewport(...)`), add:

```java
if (table._selectionListener != null) {
    table._selectionListener.selectionChanged(Collections.unmodifiableSet(table._selectedRows));
}
```

In `SelectAllCommand.execute()`, after line 694 (`table.updateViewport(...)`), add the same:

```java
if (table._selectionListener != null) {
    table._selectionListener.selectionChanged(Collections.unmodifiableSet(table._selectedRows));
}
```

Add `import java.util.Collections;` if not already present.

**Step 4: Add public getter for selected rows**

```java
/**
 * The current set of selected row objects (unmodifiable).
 */
public Set<Object> getSelectedRows() {
    return Collections.unmodifiableSet(_selectedRows);
}
```

**Step 5: Verify build compiles**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.react && mvn compile -q`
Expected: BUILD SUCCESS

**Step 6: Commit**

```
Ticket #29108: Add SelectionListener callback to ReactTableControl.
```

---

### Task 3: Write TableElement Config and Constructor (Test First)

**Files:**
- Create: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/element/TestTableElement.java`
- Create: `com.top_logic.layout.view/src/test/resources/test/com/top_logic/layout/view/element/test-table.view.xml`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/TableElement.java`

**Step 1: Write test XML**

Create `com.top_logic.layout.view/src/test/resources/test/com/top_logic/layout/view/element/test-table.view.xml`:

```xml
<?xml version="1.0" encoding="utf-8" ?>
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
  <channels>
    <channel name="testInput" />
    <channel name="selectedRow" />
  </channels>

  <table selection="selectedRow">
    <inputs>
      <input channel="testInput" />
    </inputs>
    <rows>input -> list($input)</rows>
  </table>
</view>
```

**Step 2: Write the failing test**

Create `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/element/TestTableElement.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.element.TableElement;

/**
 * Tests parsing and instantiation of {@link TableElement}.
 */
public class TestTableElement extends TestCase {

	/**
	 * Tests that a view XML with a {@code <table>} element can be parsed into configuration.
	 */
	public void testParseTableConfig() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestTableElement.class, "test-table.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();

		context.checkErrors();
		assertNotNull("Config should be parsed", config);

		// The content should be a TableElement config.
		assertEquals("View should have one content element", 1, config.getContent().size());
		assertTrue("Content should be TableElement config",
			config.getContent().get(0) instanceof TableElement.Config);

		TableElement.Config tableConfig = (TableElement.Config) config.getContent().get(0);

		// Verify inputs.
		assertEquals("Should have one input", 1, tableConfig.getInputs().size());
		assertEquals("Input channel name", "testInput", tableConfig.getInputs().get(0).getChannelName());

		// Verify rows expression is present (non-null).
		assertNotNull("Rows expression should be set", tableConfig.getRows());

		// Verify selection channel.
		assertNotNull("Selection should be set", tableConfig.getSelection());
		assertEquals("Selection channel name", "selectedRow", tableConfig.getSelection().getChannelName());
	}

	/**
	 * Tests that the parsed configuration can be instantiated into a UIElement tree.
	 */
	public void testInstantiateTableElement() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestTableElement.class, "test-table.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();

		UIElement element = context.getInstance(config);
		context.checkErrors();
		assertNotNull("UIElement should be instantiated", element);
		assertTrue("Should be a ViewElement", element instanceof ViewElement);
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableElement.class, TypeIndex.Module.INSTANCE));
	}
}
```

**Step 3: Run test to verify it fails**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn test -DskipTests=false -Dtest=TestTableElement -q`
Expected: FAIL (TableElement class does not exist)

**Step 4: Write TableElement implementation**

Create `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/TableElement.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.react.ViewControl;
import com.top_logic.layout.react.control.table.ReactCellControlProvider;
import com.top_logic.layout.react.control.table.ReactTableControl;
import com.top_logic.layout.react.control.table.ReactTextCellControl;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLModel;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.mig.html.ElementUpdate;
import com.top_logic.util.model.ModelService;

/**
 * Declarative {@link UIElement} that wraps a {@link ReactTableControl}.
 *
 * <p>
 * Replaces the legacy {@code ListModelBuilder} + {@code LayoutComponent} pattern. Input data is
 * provided via {@link ViewChannel}s, and rows are computed using TL-Script expressions.
 * </p>
 */
public class TableElement implements UIElement {

	/**
	 * Configuration for {@link TableElement}.
	 */
	@TagName("table")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(TableElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getInputs()}. */
		String INPUTS = "inputs";

		/** Configuration name for {@link #getRows()}. */
		String ROWS = "rows";

		/** Configuration name for {@link #getSupportsElement()}. */
		String SUPPORTS_ELEMENT = "supportsElement";

		/** Configuration name for {@link #getModelForElement()}. */
		String MODEL_FOR_ELEMENT = "modelForElement";

		/** Configuration name for {@link #getColumns()}. */
		String COLUMNS = "columns";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		/**
		 * References to {@link ViewChannel}s whose current values become positional arguments to
		 * the expression properties ({@link #getRows()}, {@link #getSupportsElement()},
		 * {@link #getModelForElement()}).
		 */
		@Name(INPUTS)
		@ListBinding(format = ChannelRefFormat.class, tag = "input", attribute = "channel")
		List<ChannelRef> getInputs();

		/**
		 * TL-Script function computing the row objects.
		 *
		 * <p>
		 * Takes the input channel values as positional arguments and returns a
		 * {@link Collection} of row objects.
		 * </p>
		 *
		 * <p>
		 * Example: {@code orga -> $orga.get(`tl.customers:Organization#customers`)}
		 * </p>
		 */
		@Name(ROWS)
		@Mandatory
		@NonNullable
		Expr getRows();

		/**
		 * Optional TL-Script function for incremental update decisions.
		 *
		 * <p>
		 * Takes the input channel values followed by a candidate object as last argument.
		 * Returns {@code true}/{@code "add"}, {@code false}/{@code "remove"},
		 * {@code "no_change"}, or {@code "unknown"}.
		 * </p>
		 */
		@Name(SUPPORTS_ELEMENT)
		Expr getSupportsElement();

		/**
		 * Optional TL-Script function for reverse model lookup.
		 *
		 * <p>
		 * Takes the input channel values followed by a candidate object as last argument.
		 * Returns a model object such that {@link #getRows()} would include the candidate.
		 * </p>
		 */
		@Name(MODEL_FOR_ELEMENT)
		Expr getModelForElement();

		/**
		 * Column configuration for the table.
		 *
		 * <p>
		 * Reuses the existing {@link TableConfigurationProvider} infrastructure for defining
		 * columns, accessors, comparators, and cell renderers.
		 * </p>
		 */
		@Name(COLUMNS)
		PolymorphicConfiguration<TableConfigurationProvider> getColumns();

		/**
		 * Optional reference to a {@link ViewChannel} to write the selected row object(s) to.
		 */
		@Name(SELECTION)
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();
	}

	private final List<ChannelRef> _inputRefs;

	private final QueryExecutor _rowsExecutor;

	private final QueryExecutor _supportsElementExecutor;

	private final QueryExecutor _modelForElementExecutor;

	private final TableConfigurationProvider _columnsProvider;

	private final ChannelRef _selectionRef;

	/**
	 * Creates a new {@link TableElement} from configuration.
	 */
	@CalledByReflection
	public TableElement(InstantiationContext context, Config config) {
		_inputRefs = config.getInputs();
		_selectionRef = config.getSelection();
		_columnsProvider = context.getInstance(config.getColumns());

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLModel model = ModelService.getApplicationModel();
		_rowsExecutor = QueryExecutor.compile(kb, model, config.getRows());
		_supportsElementExecutor = QueryExecutor.compileOptional(kb, model, config.getSupportsElement());
		_modelForElementExecutor = QueryExecutor.compileOptional(kb, model, config.getModelForElement());
	}

	@Override
	public ViewControl createControl(ViewContext context) {
		// 1. Resolve input channels.
		List<ViewChannel> inputChannels = new ArrayList<>(_inputRefs.size());
		for (ChannelRef ref : _inputRefs) {
			inputChannels.add(context.resolveChannel(ref));
		}

		// 2. Execute initial row query.
		Object[] channelValues = readChannelValues(inputChannels);
		Collection<?> rows = executeRowsQuery(channelValues);

		// 3. Build table configuration.
		TableConfiguration tableConfig;
		if (_columnsProvider != null) {
			tableConfig = TableConfigurationFactory.build(_columnsProvider);
		} else {
			tableConfig = TableConfigurationFactory.table();
		}

		// 4. Build column names from configuration.
		List<String> columnNames = new ArrayList<>(tableConfig.getDeclaredColumns().keySet());

		// 5. Create ObjectTableModel.
		ObjectTableModel tableModel =
			new ObjectTableModel(columnNames, tableConfig, new ArrayList<>(rows));

		// 6. Create cell provider.
		ReactCellControlProvider cellProvider = createCellProvider();

		// 7. Create ReactTableControl.
		ReactTableControl tableControl = new ReactTableControl(tableModel, cellProvider);

		// 8. Wire selection channel.
		if (_selectionRef != null) {
			ViewChannel selectionChannel = context.resolveChannel(_selectionRef);
			tableControl.setSelectionListener(selectedRows -> {
				if (selectedRows.size() == 1) {
					selectionChannel.set(selectedRows.iterator().next());
				} else if (selectedRows.isEmpty()) {
					selectionChannel.set(null);
				} else {
					selectionChannel.set(selectedRows);
				}
			});
		}

		// 9. Wire input channel listeners for re-query on change.
		ViewChannel.ChannelListener refreshListener = (sender, oldValue, newValue) -> {
			Object[] newValues = readChannelValues(inputChannels);
			Collection<?> newRows = executeRowsQuery(newValues);
			tableModel.setRowObjects(new ArrayList<>(newRows));
		};
		for (ViewChannel channel : inputChannels) {
			channel.addListener(refreshListener);
		}

		return tableControl;
	}

	private Object[] readChannelValues(List<ViewChannel> channels) {
		Object[] values = new Object[channels.size()];
		for (int i = 0; i < channels.size(); i++) {
			values[i] = channels.get(i).get();
		}
		return values;
	}

	private Collection<?> executeRowsQuery(Object[] channelValues) {
		Object result = _rowsExecutor.execute(channelValues);
		if (result instanceof Collection<?>) {
			return (Collection<?>) result;
		}
		if (result == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(result);
	}

	private ReactCellControlProvider createCellProvider() {
		return (rowObject, columnName, cellValue) -> {
			return new ReactTextCellControl(MetaLabelProvider.INSTANCE.getLabel(cellValue));
		};
	}
}
```

**Step 5: Run test to verify it passes**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn test -DskipTests=false -Dtest=TestTableElement -q`
Expected: PASS

**Note:** The test only validates config parsing and UIElement instantiation (Phase 1 lifecycle). It does NOT call `createControl()` because that requires a running `PersistencyLayer` / `ModelService` for `QueryExecutor.compile()`. The constructor's expression compilation will be exercised in Task 5 with a more comprehensive test setup.

**Step 6: Commit**

```
Ticket #29108: TableElement with config parsing, expression compilation, and channel wiring.
```

---

### Task 4: Verify ObjectTableModel.setRowObjects Exists

The `createControl` wiring uses `tableModel.setRowObjects(...)` to refresh rows when input channels change. Verify this method exists on `ObjectTableModel`.

**Step 1: Check for setRowObjects**

Search for `setRowObjects` in `ObjectTableModel.java`. If it does not exist, check `EditableRowTableModel` or parent classes for a method that replaces the row list.

If no such method exists, use `tableModel.removeAll()` followed by `tableModel.addAllRowObjects(newRows)` instead.

**Step 2: Update TableElement if needed**

If `setRowObjects` doesn't exist, update the refresh listener in `TableElement.createControl()`:

```java
ViewChannel.ChannelListener refreshListener = (sender, oldValue, newValue) -> {
    Object[] newValues = readChannelValues(inputChannels);
    Collection<?> newRows = executeRowsQuery(newValues);
    List<?> currentRows = new ArrayList<>(tableModel.getAllRows());
    for (Object row : currentRows) {
        tableModel.removeRowObject(row);
    }
    tableModel.addAllRowObjects(new ArrayList<>(newRows));
};
```

**Step 3: Compile and commit if changes were needed**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn compile -q`

```
Ticket #29108: Fix row refresh to use available ObjectTableModel API.
```

---

### Task 5: Build and Verify Module Compiles End-to-End

**Step 1: Build the full module**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn install -q`
Expected: BUILD SUCCESS (includes test execution)

**Step 2: Fix any compilation or test issues**

Address any issues that arise. Common problems:
- Missing imports
- `QueryExecutor.compile()` requiring services not started in test → wrap in try/catch in test or add service setup
- `@ListBinding` format issues

**Step 3: Commit if fixes were needed**

```
Ticket #29108: Fix build issues in TableElement.
```

---

### Task 6: Regenerate Messages and Commit Generated Files

The `messages_en.properties` and `messages_de.properties` files are generated during `mvn install` from JavaDoc and `@Label` annotations on Config interfaces.

**Step 1: Verify generated messages**

After the `mvn install` from Task 5, check if new message entries were generated:

Run: `git diff com.top_logic.layout.view/src/main/java/META-INF/messages_en.properties`

New entries should appear for the `TableElement.Config` properties (inputs, rows, supportsElement, modelForElement, columns, selection).

**Step 2: Stage and commit generated files**

```bash
git add com.top_logic.layout.view/src/main/java/META-INF/messages_en.properties
git add com.top_logic.layout.view/src/main/java/META-INF/messages_de.properties
```

```
Ticket #29108: Regenerate messages for TableElement config properties.
```
