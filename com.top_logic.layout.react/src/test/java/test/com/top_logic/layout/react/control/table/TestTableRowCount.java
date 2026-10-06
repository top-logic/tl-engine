/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.table.ExpandArguments;
import com.top_logic.layout.react.control.table.GroupArguments;
import com.top_logic.layout.react.control.table.SearchArguments;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.table.Column;
import com.top_logic.table.Selection;
import com.top_logic.table.SelectionMode;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableViewState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.util.Resources;

/**
 * Tests the row count a {@link TableViewControl} tells below its rows: how many rows the table has,
 * how many of them its filter lets pass, and how many are selected.
 */
public class TestTableRowCount extends TestCase {

	/** The name of the column the tests search in. */
	private static final String COLUMN_NAME = "name";

	/** The name of the column the tests group by. */
	private static final String COLUMN_STATUS = "status";

	/** State key of the text telling how many rows the table has. */
	private static final String ROW_COUNT = "rowCount";

	/** State key of the text telling how many rows are selected. */
	private static final String ROW_COUNT_SELECTED = "rowCountSelected";

	private static final Item ALPHA = new Item("alpha", "open");

	private static final Item BETA = new Item("beta", "closed");

	private static final Item GAMMA = new Item("gamma", "open");

	/**
	 * A row business object.
	 *
	 * @param name
	 *        The row label the tests search for.
	 * @param status
	 *        The value the tests group by.
	 */
	private record Item(String name, String status) {
		// Pure value type.
	}

	/** A control exposing what it pushed to the client. */
	private static final class TestTable extends TableViewControl<Item> {

		TestTable(ReactContext context, com.top_logic.table.TableView<Item> view) {
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
		_rows = new ListRowSource<>(new ArrayList<>(List.of(ALPHA, BETA, GAMMA)), columns());
		_table = table(SelectionMode.MULTI);
	}

	/**
	 * Tests that an unfiltered table tells how many rows it has, and nothing about a selection while
	 * none is made.
	 */
	public void testCountsAllRows() {
		assertEquals(text(I18NConstants.TABLE_ROW_COUNT__COUNT.fill(3)), _table.clientState(ROW_COUNT));
		assertEquals("", _table.clientState(ROW_COUNT_SELECTED));
	}

	/**
	 * Tests that a search makes the count tell how many of all rows it lets pass, and that clearing
	 * it brings the plain count back.
	 */
	public void testSearchCountsMatchingOfAll() {
		search("mm");
		assertEquals(text(I18NConstants.TABLE_ROW_COUNT_FILTERED__MATCHING_TOTAL.fill(1, 3)),
			_table.clientState(ROW_COUNT));

		search("");
		assertEquals(text(I18NConstants.TABLE_ROW_COUNT__COUNT.fill(3)), _table.clientState(ROW_COUNT));
	}

	/**
	 * Tests that a search matching every row still says the table is filtered: the user is told
	 * the rows pass the filter, not that there is none.
	 */
	public void testSearchMatchingAllStillCountsAsFiltered() {
		search("a");
		assertEquals(text(I18NConstants.TABLE_ROW_COUNT_FILTERED__MATCHING_TOTAL.fill(3, 3)),
			_table.clientState(ROW_COUNT));
	}

	/**
	 * Tests that a grouped table counts its rows, not the group headers, and goes on counting the
	 * rows of a collapsed group.
	 */
	public void testGroupedTableCountsDataRows() {
		_table.executeClientCommand(TableViewControl.CMD_GROUP, Map.of(GroupArguments.COLUMN, COLUMN_STATUS));
		assertEquals(text(I18NConstants.TABLE_ROW_COUNT__COUNT.fill(3)), _table.clientState(ROW_COUNT));

		_table.executeClientCommand(TableViewControl.CMD_EXPAND, Map.of(
			ExpandArguments.ROW_INDEX, Integer.valueOf(0),
			ExpandArguments.EXPANDED, Boolean.FALSE));
		assertEquals(text(I18NConstants.TABLE_ROW_COUNT__COUNT.fill(3)), _table.clientState(ROW_COUNT));

		search("mm");
		assertEquals("The filter applies to the grouped rows, the headers stay uncounted.",
			text(I18NConstants.TABLE_ROW_COUNT_FILTERED__MATCHING_TOTAL.fill(1, 3)),
			_table.clientState(ROW_COUNT));
	}

	/**
	 * Tests that a table selecting several rows tells how many are selected, and stops telling it
	 * once the selection is cleared.
	 */
	public void testCountsSelectedRows() {
		_table.selectRows(List.of(ALPHA, GAMMA));
		assertEquals(text(I18NConstants.TABLE_ROW_COUNT_SELECTED__COUNT.fill(2)),
			_table.clientState(ROW_COUNT_SELECTED));

		_table.selectRows(List.of());
		assertEquals("", _table.clientState(ROW_COUNT_SELECTED));
	}

	/**
	 * Tests that a table selecting a single row does not count its selection: the selected row is
	 * what the table highlights.
	 */
	public void testSingleSelectionIsNotCounted() {
		TestTable table = table(SelectionMode.SINGLE);
		table.selectRow(BETA);
		assertEquals("", table.clientState(ROW_COUNT_SELECTED));
	}

	/**
	 * Tests that the count follows rows added to and removed from the data.
	 */
	public void testCountFollowsChangedData() {
		_rows.setElements(new ArrayList<>(List.of(ALPHA, BETA, GAMMA, new Item("delta", "open"))));
		_table.refreshData();
		assertEquals(text(I18NConstants.TABLE_ROW_COUNT__COUNT.fill(4)), _table.clientState(ROW_COUNT));

		_rows.setElements(new ArrayList<>(List.of(BETA)));
		_table.refreshData();
		assertEquals(text(I18NConstants.TABLE_ROW_COUNT__COUNT.fill(1)), _table.clientState(ROW_COUNT));
	}

	/**
	 * Tests that a table switched to show no count tells nothing, whatever is selected.
	 */
	public void testNoCountWhenSwitchedOff() {
		_table.selectRows(List.of(ALPHA, GAMMA));
		_table.setRowCount(false);
		assertEquals("", _table.clientState(ROW_COUNT));
		assertEquals("", _table.clientState(ROW_COUNT_SELECTED));
	}

	/**
	 * Tests the plural forms of the count texts in English and German.
	 */
	public void testPluralForms() {
		Resources en = Resources.getInstance(Locale.ENGLISH);
		assertEquals("No rows", en.getString(I18NConstants.TABLE_ROW_COUNT__COUNT.fill(0)));
		assertEquals("1 row", en.getString(I18NConstants.TABLE_ROW_COUNT__COUNT.fill(1)));
		assertEquals("2 rows", en.getString(I18NConstants.TABLE_ROW_COUNT__COUNT.fill(2)));
		assertEquals("1,234 rows", en.getString(I18NConstants.TABLE_ROW_COUNT__COUNT.fill(1234)));
		assertEquals("0 of 1 row", en.getString(I18NConstants.TABLE_ROW_COUNT_FILTERED__MATCHING_TOTAL.fill(0, 1)));
		assertEquals("1 of 2 rows", en.getString(I18NConstants.TABLE_ROW_COUNT_FILTERED__MATCHING_TOTAL.fill(1, 2)));
		assertEquals("2 selected", en.getString(I18NConstants.TABLE_ROW_COUNT_SELECTED__COUNT.fill(2)));

		Resources de = Resources.getInstance(Locale.GERMAN);
		assertEquals("Keine Zeilen", de.getString(I18NConstants.TABLE_ROW_COUNT__COUNT.fill(0)));
		assertEquals("1 Zeile", de.getString(I18NConstants.TABLE_ROW_COUNT__COUNT.fill(1)));
		assertEquals("2 Zeilen", de.getString(I18NConstants.TABLE_ROW_COUNT__COUNT.fill(2)));
		assertEquals("1.234 Zeilen", de.getString(I18NConstants.TABLE_ROW_COUNT__COUNT.fill(1234)));
		assertEquals("0 von 1 Zeile", de.getString(I18NConstants.TABLE_ROW_COUNT_FILTERED__MATCHING_TOTAL.fill(0, 1)));
		assertEquals("1 von 2 Zeilen", de.getString(I18NConstants.TABLE_ROW_COUNT_FILTERED__MATCHING_TOTAL.fill(1, 2)));
		assertEquals("2 ausgewählt", de.getString(I18NConstants.TABLE_ROW_COUNT_SELECTED__COUNT.fill(2)));
	}

	private TestTable table(SelectionMode selectionMode) {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test"));
		TableViewState state = DefaultTableView.initialState(columns(), SortSpec.NONE, List.of());
		state.setSelection(Selection.none(selectionMode));
		return new TestTable(context, new DefaultTableView<>(columns(), _rows, state));
	}

	/** Sends the client's search command. */
	private void search(String term) {
		_table.executeClientCommand(TableViewControl.CMD_SEARCH, Map.of(SearchArguments.TERM, term));
	}

	/** The text the table pushes for the given message. */
	private static String text(ResKey key) {
		return Resources.getInstance().getString(key);
	}

	/** The two columns: the searched name, and the status the rows are grouped by. */
	private static List<Column<Item, ?>> columns() {
		return List.of(
			DefaultColumn.<Item, String> builder(COLUMN_NAME, Item::name).build(),
			DefaultColumn.<Item, String> builder(COLUMN_STATUS, Item::status).build());
	}

	/** Suite requiring the resources the table builds its texts from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableRowCount.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
