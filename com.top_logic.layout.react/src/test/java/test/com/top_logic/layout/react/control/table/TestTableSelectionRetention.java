/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.table.ExpandArguments;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.table.Column;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Tests that the selection of a {@link TableViewControl} follows the table's data, not what the
 * table displays of it.
 *
 * <p>
 * A filter and a grouping only change what is displayed: a selected row the filter hides, or one
 * inside a collapsed group, stays selected as long as its object is part of the data. Only an
 * object that is gone from the data is dropped from the selection.
 * </p>
 */
public class TestTableSelectionRetention extends TestCase {

	/** The name of the column showing the row object itself. */
	private static final String COLUMN_VALUE = "value";

	/** The name of the column the tests group by: the first letter of the row. */
	private static final String COLUMN_GROUP = "group";

	/** State key of the keyboard cursor row index. */
	private static final String CURSOR_INDEX = "cursorIndex";

	/** State key of the client's row list. */
	private static final String ROWS = "rows";

	/** Per-row state key of whether the row is selected. */
	private static final String SELECTED = "selected";

	/** The row the tests select, in group {@code a}. */
	private static final String A1 = "a1";

	/** A second row in group {@code a}. */
	private static final String A2 = "a2";

	/** The single row of group {@code b}. */
	private static final String B1 = "b1";

	/** A control exposing what it pushed to the client, so a test can read the row state. */
	private static final class TestTable extends TableViewControl<String> {

		TestTable(ReactContext context, com.top_logic.table.TableView<String> view) {
			super(context, view, false);
		}

		Object clientState(String key) {
			return getState(key);
		}
	}

	private ListRowSource<String> _rows;

	private DefaultTableView<String> _view;

	private TestTable _table;

	/** The selections the listener was told about, in notification order. */
	private List<Set<Object>> _notified;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test"));
		_rows = new ListRowSource<>(new ArrayList<>(List.of(A1, A2, B1)), columns());
		_view = DefaultTableView.create(columns(), _rows);
		_table = new TestTable(context, _view);
		_notified = new ArrayList<>();
		_table.addSelectionListener(selectedKeys -> _notified.add(new LinkedHashSet<>(selectedKeys)));
	}

	/**
	 * Tests that a selected row hidden by a filter is still selected after a refresh of the data
	 * that keeps its object, and is displayed as selected once the filter is removed.
	 */
	public void testFilteredSelectionSurvivesRefresh() {
		_table.selectRow(A1);
		search(B1);
		assertEquals("The precondition: the filter hides the selected row.", 1, clientRows().size());

		_rows.setElements(new ArrayList<>(List.of(A1, A2, B1)));
		_table.refreshData();

		assertEquals("The object is still part of the data.", Set.of(A1), _table.getSelectedKeys());
		assertEquals(Set.<Object> of(A1), _view.state().getSelection().keys());
		assertEquals("The listeners were never told about a deselection.", List.of(Set.of(A1)), _notified);

		search("");

		assertEquals(Boolean.TRUE, clientRows().get(0).get(SELECTED));
	}

	/**
	 * Tests that a selected row hidden by a filter is dropped from the selection once its object is
	 * gone from the data.
	 */
	public void testFilteredSelectionIsDroppedWhenRemovedFromTheData() {
		_table.selectRow(A1);
		search(B1);

		_rows.setElements(new ArrayList<>(List.of(A2, B1)));
		_table.refreshData();

		assertEquals(Set.of(), _table.getSelectedKeys());
	}

	/**
	 * Tests that a displayed selected row is dropped from the selection once its object is gone
	 * from the data.
	 */
	public void testDisplayedSelectionIsDroppedWhenRemovedFromTheData() {
		_table.selectRow(A1);

		_rows.setElements(new ArrayList<>(List.of(A2, B1)));
		_table.refreshData();

		assertEquals(Set.of(), _table.getSelectedKeys());
	}

	/**
	 * Tests that a selected row inside a collapsed group is still selected after a refresh of the
	 * data that keeps its object.
	 */
	public void testSelectionInCollapsedGroupSurvivesRefresh() {
		_table.setGroupedColumn(COLUMN_GROUP);
		_notified.clear();
		_table.selectRow(A1);
		collapse(0);
		assertEquals("The precondition: the selected row's group is collapsed.", 3, clientRows().size());

		_table.refreshData();

		assertEquals(Set.of(A1), _table.getSelectedKeys());
		assertEquals(List.of(Set.of(A1)), _notified);
	}

	/**
	 * Tests that grouping the rows keeps a selected row the filter hides: the grouping rearranges
	 * the displayed rows, it does not change the data.
	 */
	public void testGroupingKeepsFilteredSelection() {
		_table.selectRow(A1);
		search(B1);

		_table.setGroupedColumn(COLUMN_GROUP);

		assertEquals(Set.of(A1), _table.getSelectedKeys());
		assertFalse("The listeners were never told about a deselection.", _notified.contains(Set.of()));

		_table.setGroupedColumn(null);

		assertEquals(Set.of(A1), _table.getSelectedKeys());
	}

	/**
	 * Tests that a row of the data the filter hides can be selected programmatically: it becomes
	 * the selection without a cursor, and shows as selected once the filter is removed.
	 */
	public void testSelectingAFilteredRow() {
		search(B1);

		_table.selectRows(List.of(A1));

		assertEquals(Set.of(A1), _table.getSelectedKeys());
		assertEquals(List.of(Set.of(A1)), _notified);
		assertEquals("No displayed row carries the cursor.", Integer.valueOf(-1), _table.clientState(CURSOR_INDEX));

		search("");

		assertEquals(Boolean.TRUE, clientRows().get(0).get(SELECTED));
	}

	/**
	 * Tests that a key the table's data has no row for is not selected.
	 */
	public void testSelectingAForeignKeySelectsNothing() {
		_table.selectRows(List.of("elsewhere"));

		assertEquals(Set.of(), _table.getSelectedKeys());
	}

	/** Searches the rows for the given term, an empty one clearing the search. */
	private void search(String term) {
		_table.search(term);
	}

	/** Sends the client's command collapsing the row at the given index. */
	private void collapse(int rowIndex) {
		_table.executeClientCommand(TableViewControl.CMD_EXPAND, Map.of(
			ExpandArguments.ROW_INDEX, Integer.valueOf(rowIndex),
			ExpandArguments.EXPANDED, Boolean.FALSE));
	}

	/** The rows as pushed to the client. */
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> clientRows() {
		return (List<Map<String, Object>>) _table.clientState(ROWS);
	}

	/** The row object itself, and its first letter to group by. */
	private static List<Column<String, ?>> columns() {
		return List.of(
			DefaultColumn.<String, String> builder(COLUMN_VALUE, row -> row).build(),
			DefaultColumn.<String, String> builder(COLUMN_GROUP, row -> row.substring(0, 1)).build());
	}

	/** Suite requiring the resources the table builds its column headers from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableSelectionRetention.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
