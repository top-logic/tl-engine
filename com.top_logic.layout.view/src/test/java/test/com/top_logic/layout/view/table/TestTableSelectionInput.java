/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.layout.view.model.TableSelectionBinding;
import com.top_logic.table.Column;
import com.top_logic.table.Selection;
import com.top_logic.table.SelectionMode;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableViewState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Tests that a table listing its own selection channel among its inputs keeps its rows and their
 * cell controls when the selection changes, while a change of another input that changes the rows
 * still refreshes them.
 *
 * <p>
 * The table is wired the way {@link com.top_logic.layout.view.element.TableElement} wires it: a
 * {@link RowSourceObserver} over all inputs, refreshing the table when the rows change, with the
 * selection channel - bound to the table by a {@link TableSelectionBinding} - declared as a
 * row-independent input, whose change only pushes the rows again where a row class reads it.
 * </p>
 */
public class TestTableSelectionInput extends TestCase {

	/** The name of the single column, showing the row object itself. */
	private static final String COLUMN_VALUE = "value";

	/** State key of the client's row list. */
	private static final String ROWS = "rows";

	/** Row property holding the cell controls, keyed by column name. */
	private static final String CELLS = "cells";

	/** Row property holding the CSS class of the row. */
	private static final String CSS_CLASS = "cssClass";

	/** The CSS class of the row of the selected object. */
	private static final String SELECTED_CLASS = "current";

	/** The objects of the table, before any filter. */
	private static final List<String> ELEMENTS = List.of("a", "ab", "b");

	/** A control recording the state keys it pushes, so a test can tell what a change sent. */
	private static final class TestTable extends TableViewControl<String> {

		/** The keys pushed since the last {@link #clearPushed()}, {@code null} while constructing. */
		private List<String> _pushed;

		TestTable(ReactContext context, com.top_logic.table.TableView<String> view) {
			super(context, view, false);
			_pushed = new ArrayList<>();
		}

		@Override
		protected void putState(String key, Object value) {
			if (_pushed != null) {
				_pushed.add(key);
			}
			super.putState(key, value);
		}

		Object clientState(String key) {
			return getState(key);
		}

		List<String> pushed() {
			return _pushed;
		}

		void clearPushed() {
			_pushed.clear();
		}
	}

	private ViewChannel _filter;

	private ViewChannel _selection;

	private TestTable _table;

	private RowSourceObserver<String> _observer;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_filter = new DefaultViewChannel("filter");
		_selection = new DefaultViewChannel("selection");
	}

	@Override
	protected void tearDown() throws Exception {
		if (_observer != null) {
			_observer.detach();
		}
		_observer = null;
		_table = null;
		_selection = null;
		_filter = null;
		super.tearDown();
	}

	/**
	 * Tests that a selection change pushes no rows and keeps the cell controls of a table without a
	 * row class.
	 */
	public void testSelectionChangeKeepsTheCells() {
		buildTable(false);
		List<Object> cellsBefore = cellControls();

		_selection.set("ab");

		assertEquals(Set.of("ab"), _table.getSelectedKeys());
		assertFalse("A selection change pushes no rows: " + _table.pushed(), _table.pushed().contains(ROWS));
		assertSameControls(cellsBefore, cellControls());
	}

	/**
	 * Tests that a selection change of a table whose row class reads the selection pushes the rows
	 * again with the new row class, but with the cell controls built before.
	 */
	public void testSelectionChangeRepaintsTheRowClass() {
		buildTable(true);
		List<Object> cellsBefore = cellControls();

		_selection.set("ab");

		assertTrue("The rows are pushed again for their class.", _table.pushed().contains(ROWS));
		assertSameControls(cellsBefore, cellControls());
		assertEquals(SELECTED_CLASS, row(1).get(CSS_CLASS));
		assertNull(row(0).get(CSS_CLASS));
	}

	/**
	 * Tests that a selection change rebuilds the cells of a table one of whose columns reads the
	 * selection channel: the selection is then an input like any other, and the cells follow it.
	 */
	public void testSelectionReadByACellRebuildsTheCells() {
		buildTable(false, true);
		List<Object> cellsBefore = cellControls();

		_selection.set("ab");

		assertEquals(Set.of("ab"), _table.getSelectedKeys());
		assertTrue("The rows are pushed with cells built anew.", _table.pushed().contains(ROWS));
		List<Object> cellsAfter = cellControls();
		assertEquals(cellsBefore.size(), cellsAfter.size());
		for (int n = 0; n < cellsBefore.size(); n++) {
			assertNotSame("The cell of row " + n + " is built again.", cellsBefore.get(n), cellsAfter.get(n));
		}
	}

	/**
	 * Tests that a change of another input that changes the rows refreshes the table.
	 */
	public void testOtherInputChangeRefreshesTheRows() {
		buildTable(false);
		List<Object> cellsBefore = cellControls();

		_filter.set("b");

		assertTrue("The changed rows are pushed.", _table.pushed().contains(ROWS));
		List<?> rows = (List<?>) _table.clientState(ROWS);
		assertEquals("The rows containing the filter term are displayed.", 2, rows.size());
		assertNotSame("The cells are built for the rows displayed now.", cellsBefore.get(1), cellControls().get(0));
	}

	/**
	 * Builds the table over {@link #ELEMENTS}, filtered by {@link #_filter}, with {@link #_selection}
	 * bound as its selection channel and listed among its inputs.
	 *
	 * @param rowClass
	 *        Whether the rows carry a CSS class marking the selected object.
	 */
	private void buildTable(boolean rowClass) {
		buildTable(rowClass, false);
	}

	/**
	 * Builds the table over {@link #ELEMENTS}, filtered by {@link #_filter}, with {@link #_selection}
	 * bound as its selection channel and listed among its inputs.
	 *
	 * @param rowClass
	 *        Whether the rows carry a CSS class marking the selected object.
	 * @param cellReadsSelection
	 *        Whether a column reads the selection channel, which makes it an input whose change
	 *        builds the cells again.
	 */
	private void buildTable(boolean rowClass, boolean cellReadsSelection) {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test"));
		List<Column<String, ?>> columns = columns();
		ListRowSource<String> source = new ListRowSource<>(new ArrayList<>(ELEMENTS), columns);
		TableViewState state = DefaultTableView.initialState(columns, SortSpec.NONE, List.of());
		state.setSelection(Selection.none(SelectionMode.SINGLE));
		DefaultTableView<String> view = new DefaultTableView<>(columns, source, state);
		Function<String, String> rowCssClass =
			rowClass ? row -> row.equals(_selection.get()) ? SELECTED_CLASS : null : null;
		if (rowCssClass != null) {
			view.setRowCssClass(rowCssClass);
		}
		_table = new TestTable(context, view);
		TableSelectionBinding binding = new TableSelectionBinding(_table, _selection);

		TestTable table = _table;
		_observer = new RowSourceObserver<>(source, args -> filtered(args[0]), Set.of(),
			List.of(_filter, _selection), () -> {
				table.refreshData();
				binding.rowsRefreshed();
			})
				.withRowIndependentInputs(cellReadsSelection ? List.of() : List.of(_selection))
				.onInputsChanged(cellReadsSelection ? List.of(_filter) : List.of(_filter, _selection), () -> {
					if (rowCssClass != null) {
						table.repaintRows();
					}
				});
		_observer.attach(null);
		_table.clearPushed();
	}

	/**
	 * The {@link #ELEMENTS} containing the given term, all of them for no term.
	 */
	private static List<String> filtered(Object term) {
		if (term == null) {
			return ELEMENTS;
		}
		return ELEMENTS.stream().filter(element -> element.contains(term.toString())).toList();
	}

	/**
	 * The cell control of each row the client holds, in row order.
	 */
	private List<Object> cellControls() {
		List<?> rows = (List<?>) _table.clientState(ROWS);
		List<Object> result = new ArrayList<>(rows.size());
		for (int n = 0; n < rows.size(); n++) {
			result.add(((Map<?, ?>) row(n).get(CELLS)).get(COLUMN_VALUE));
		}
		return result;
	}

	/**
	 * The state of the row with the given index the client holds.
	 */
	private Map<?, ?> row(int index) {
		return (Map<?, ?>) ((List<?>) _table.clientState(ROWS)).get(index);
	}

	/**
	 * Asserts that the given cell controls are the same instances, in the same order.
	 */
	private static void assertSameControls(List<Object> expected, List<Object> actual) {
		assertEquals(expected.size(), actual.size());
		for (int n = 0; n < expected.size(); n++) {
			assertNotNull(expected.get(n));
			assertSame("The cell control of row " + n + " is the one built before.", expected.get(n), actual.get(n));
		}
	}

	/** The single column, showing the row object itself. */
	private static List<Column<String, ?>> columns() {
		return List.<Column<String, ?>> of(DefaultColumn.<String, String> builder(COLUMN_VALUE, row -> row).build());
	}

	/** Suite requiring the resources the table builds its column headers from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableSelectionInput.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
