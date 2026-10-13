/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableView;
import com.top_logic.table.TableViewState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Tests what a {@link TableViewControl} tells the client about the CSS classes marking its rows and
 * cells: the {@link TableView#rowCssClass(com.top_logic.table.Row) row class} and the
 * {@link TableView#cellCssClass(com.top_logic.table.Row, String) cell classes}.
 *
 * <p>
 * The control sends the classes that exist and none that does not, and the classes follow the row
 * objects when the table's data changes.
 * </p>
 */
public class TestTableRowClasses extends TestCase {

	/** The name of the column whose cells are marked by the row object's state. */
	private static final String COLUMN_STATE = "state";

	/** The name of the column whose cells are never marked. */
	private static final String COLUMN_NAME = "name";

	/** The class a row and the state cell of a failed row carry. */
	private static final String ERROR_CLASS = "tl-status-error";

	/** State key of the client's row list. */
	private static final String ROWS = "rows";

	/** Per-row state key of the row index. */
	private static final String ROW_INDEX = "index";

	/** Per-row state key of the row's CSS class. */
	private static final String ROW_CSS_CLASS = "cssClass";

	/** Per-row state key of the cell CSS classes, by column name. */
	private static final String ROW_CELL_CLASSES = "cellClasses";

	/**
	 * A row business object.
	 *
	 * @param name
	 *        The row label.
	 * @param failed
	 *        Whether the row is highlighted as failed.
	 */
	private record Item(String name, boolean failed) {
		// Pure value type.
	}

	/** A control exposing what it pushed to the client. */
	private static final class TestTable extends TableViewControl<Item> {

		TestTable(ReactContext context, TableView<Item> view) {
			super(context, view, false);
		}

		Object clientState(String key) {
			return getState(key);
		}
	}

	private ListRowSource<Item> _rows;

	private TestTable _table;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		List<Column<Item, ?>> columns = columns();
		TableViewState state = DefaultTableView.initialState(columns, SortSpec.NONE, List.of());
		_rows = new ListRowSource<>(
			new ArrayList<>(List.of(new Item("a", true), new Item("b", false))), columns);
		DefaultTableView<Item> view = new DefaultTableView<>(columns, _rows, state);
		view.setRowCssClass(item -> item.failed() ? ERROR_CLASS : null);
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test"));
		_table = new TestTable(context, view);
	}

	/**
	 * Tests that a row whose class function yields a class is sent with it, and a row without one
	 * is sent without the key.
	 */
	public void testRowClassIsSent() {
		assertEquals(ERROR_CLASS, rowState(0).get(ROW_CSS_CLASS));
		assertFalse("A row without a class sends none.", rowState(1).containsKey(ROW_CSS_CLASS));
	}

	/**
	 * Tests that the client is told the class of the cells that have one only, and that a row none
	 * of whose cells is marked is sent without the cell class map.
	 */
	public void testCellClassesAreSent() {
		assertEquals(Map.of(COLUMN_STATE, ERROR_CLASS), rowState(0).get(ROW_CELL_CLASSES));
		assertFalse("A row without marked cells sends no cell classes.",
			rowState(1).containsKey(ROW_CELL_CLASSES));
	}

	/**
	 * Tests that the classes follow the row objects when the data of the table changes.
	 */
	public void testClassesFollowData() {
		_rows.setElements(new ArrayList<>(List.of(new Item("a", false), new Item("b", true))));
		_table.refreshData();

		assertFalse("The row no longer failed.", rowState(0).containsKey(ROW_CSS_CLASS));
		assertFalse("The row's cells are no longer marked.", rowState(0).containsKey(ROW_CELL_CLASSES));
		assertEquals(ERROR_CLASS, rowState(1).get(ROW_CSS_CLASS));
		assertEquals(Map.of(COLUMN_STATE, ERROR_CLASS), rowState(1).get(ROW_CELL_CLASSES));
	}

	/** The state the table pushed to the client for the row with the given index. */
	private Map<String, Object> rowState(int index) {
		for (Map<String, Object> rowState : clientRows()) {
			if (Integer.valueOf(index).equals(rowState.get(ROW_INDEX))) {
				return rowState;
			}
		}
		throw new AssertionError("No state for row " + index + ".");
	}

	/** The rows the table pushed to the client. */
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> clientRows() {
		return (List<Map<String, Object>>) _table.clientState(ROWS);
	}

	/** A plain name column, and a state column marking the cell of a failed row. */
	private static List<Column<Item, ?>> columns() {
		return List.of(
			DefaultColumn.<Item, String> builder(COLUMN_NAME, Item::name)
				.renderer(name -> CellContent.text(name))
				.build(),
			DefaultColumn.<Item, Boolean> builder(COLUMN_STATE, Item::failed)
				.renderer(failed -> CellContent.text(failed.booleanValue() ? "failed" : "ok"))
				.css(item -> item.failed() ? ERROR_CLASS : null)
				.build());
	}

	/** Suite requiring the resources the table builds its column headers from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableRowClasses.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
