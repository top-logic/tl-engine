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
import com.top_logic.layout.react.control.ReactValueColor;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.model.annotate.ui.ValueColor;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableView;
import com.top_logic.table.TableViewState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Tests what a {@link TableViewControl} tells the client about the CSS classes and color roles
 * marking its rows and cells: the {@link TableView#rowCssClass(com.top_logic.table.Row) row class},
 * the {@link TableView#cellCssClass(com.top_logic.table.Row, String) cell classes}, the
 * {@link TableView#rowColor(com.top_logic.table.Row) row role} and the
 * {@link TableView#cellColor(com.top_logic.table.Row, String) cell roles}.
 *
 * <p>
 * The control sends the classes and roles that exist and none that does not, sends the roles
 * independently of the classes, and both follow the row objects when the table's data changes.
 * </p>
 */
public class TestTableRowClasses extends TestCase {

	/** The name of the column whose cells are marked by the row object's state. */
	private static final String COLUMN_STATE = "state";

	/** The name of the column whose cells are never marked. */
	private static final String COLUMN_NAME = "name";

	/** The class a row and the state cell of a failed row carry. */
	private static final String ERROR_CLASS = "my-highlight";

	/** State key of the client's row list. */
	private static final String ROWS = "rows";

	/** Per-row state key of the row index. */
	private static final String ROW_INDEX = "index";

	/** Per-row state key of the row's CSS class. */
	private static final String ROW_CSS_CLASS = "cssClass";

	/** Per-row state key of the cell CSS classes, by column name. */
	private static final String ROW_CELL_CLASSES = "cellClasses";

	/** Per-row state key of the row's color role. */
	private static final String ROW_ROLE = ReactValueColor.ROLE;

	/** Per-row state key of the cell color roles, by column name. */
	private static final String ROW_CELL_ROLES = "cellRoles";

	/**
	 * A row business object.
	 *
	 * @param name
	 *        The row label.
	 * @param failed
	 *        Whether the row is highlighted as failed: by a CSS class on the row and on its state
	 *        cell, and by the error role of its state cell.
	 * @param warned
	 *        Whether the row is tinted as warned: by the warning role of the row and the info role
	 *        of its name cell, without any CSS class.
	 */
	private record Item(String name, boolean failed, boolean warned) {
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
			new ArrayList<>(List.of(new Item("a", true, false), new Item("b", false, true),
				new Item("c", false, false))),
			columns);
		DefaultTableView<Item> view = new DefaultTableView<>(columns, _rows, state);
		view.setRowCssClass(item -> item.failed() ? ERROR_CLASS : null);
		view.setRowColor(item -> item.warned() ? ValueColor.WARNING : null);
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
		assertFalse("A row without a class sends none.", rowState(2).containsKey(ROW_CSS_CLASS));
	}

	/**
	 * Tests that a row whose color function yields a role is sent with its external name, and a row
	 * without one is sent without the key - independently of the row's class.
	 */
	public void testRowRoleIsSent() {
		assertEquals(ValueColor.WARNING.getExternalName(), rowState(1).get(ROW_ROLE));
		assertFalse("A row with a class but no role sends no role.", rowState(0).containsKey(ROW_ROLE));
		assertFalse("A row without a role sends none.", rowState(2).containsKey(ROW_ROLE));
	}

	/**
	 * Tests that the client is told the role of the cells that have one only, by column name, that
	 * the roles do not depend on the cell classes, and that a row none of whose cells has a role is
	 * sent without the cell role map.
	 */
	public void testCellRolesAreSent() {
		assertEquals(Map.of(COLUMN_STATE, ValueColor.ERROR.getExternalName()), rowState(0).get(ROW_CELL_ROLES));
		assertEquals(Map.of(COLUMN_NAME, ValueColor.INFO.getExternalName()), rowState(1).get(ROW_CELL_ROLES));
		assertFalse("Cells with a role but no class send no class.", rowState(1).containsKey(ROW_CELL_CLASSES));
		assertFalse("A row without cell roles sends none.", rowState(2).containsKey(ROW_CELL_ROLES));
	}

	/**
	 * Tests that the client is told the class of the cells that have one only, and that a row none
	 * of whose cells is marked is sent without the cell class map.
	 */
	public void testCellClassesAreSent() {
		assertEquals(Map.of(COLUMN_STATE, ERROR_CLASS), rowState(0).get(ROW_CELL_CLASSES));
		assertFalse("A row without marked cells sends no cell classes.",
			rowState(1).containsKey(ROW_CELL_CLASSES));
		assertFalse("A row without marked cells sends no cell classes.",
			rowState(2).containsKey(ROW_CELL_CLASSES));
	}

	/**
	 * Tests that the classes follow the row objects when the data of the table changes.
	 */
	public void testClassesFollowData() {
		_rows.setElements(new ArrayList<>(List.of(new Item("a", false, true), new Item("b", true, false),
			new Item("c", false, false))));
		_table.refreshData();

		assertFalse("The row no longer failed.", rowState(0).containsKey(ROW_CSS_CLASS));
		assertFalse("The row's cells are no longer marked.", rowState(0).containsKey(ROW_CELL_CLASSES));
		assertEquals(ERROR_CLASS, rowState(1).get(ROW_CSS_CLASS));
		assertEquals(Map.of(COLUMN_STATE, ERROR_CLASS), rowState(1).get(ROW_CELL_CLASSES));

		assertEquals(ValueColor.WARNING.getExternalName(), rowState(0).get(ROW_ROLE));
		assertEquals(Map.of(COLUMN_NAME, ValueColor.INFO.getExternalName()), rowState(0).get(ROW_CELL_ROLES));
		assertFalse("The row is no longer warned.", rowState(1).containsKey(ROW_ROLE));
		assertEquals(Map.of(COLUMN_STATE, ValueColor.ERROR.getExternalName()), rowState(1).get(ROW_CELL_ROLES));
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

	/**
	 * A name column tinting the cell of a warned row, and a state column marking the cell of a
	 * failed row with a class and a role.
	 */
	private static List<Column<Item, ?>> columns() {
		return List.of(
			DefaultColumn.<Item, String> builder(COLUMN_NAME, Item::name)
				.renderer(name -> CellContent.text(name))
				.color(item -> item.warned() ? ValueColor.INFO : null)
				.build(),
			DefaultColumn.<Item, Boolean> builder(COLUMN_STATE, Item::failed)
				.renderer(failed -> CellContent.text(failed.booleanValue() ? "failed" : "ok"))
				.css(item -> item.failed() ? ERROR_CLASS : null)
				.color(item -> item.failed() ? ValueColor.ERROR : null)
				.build());
	}

	/** Suite requiring the resources the table builds its column headers from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableRowClasses.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
