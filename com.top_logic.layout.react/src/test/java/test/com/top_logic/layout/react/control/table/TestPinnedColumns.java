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
import com.top_logic.layout.react.control.table.ColumnResizeArguments;
import com.top_logic.layout.react.control.table.SetFrozenColumnCountArguments;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnOption;
import com.top_logic.table.ColumnView;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableView;
import com.top_logic.table.TableViewState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Tests a column {@link Column#pinnedEnd() pinned} to the end of a table: where the table model
 * puts it, that it is none of the user's business, and what the {@link TableViewControl} tells the
 * client about it.
 */
public class TestPinnedColumns extends TestCase {

	/** The name of the first column. */
	private static final String COLUMN_NAME = "name";

	/** The name of the second column. */
	private static final String COLUMN_AMOUNT = "amount";

	/** The name of the column pinned to the end of the table. */
	private static final String COLUMN_ACTIONS = "actions";

	/** The width the pinned column is defined with. */
	private static final int ACTIONS_WIDTH = 80;

	/** State key of the client's column list. */
	private static final String COLUMNS = "columns";

	/** State key of the number of frozen columns. */
	private static final String FROZEN_COLUMN_COUNT = "frozenColumnCount";

	/** Per-column state key of the column name. */
	private static final String NAME = "name";

	/** Per-column state key of the display width. */
	private static final String WIDTH = "width";

	/** Per-column state key telling that the column keeps its place at the end of the table. */
	private static final String PINNED_END = "pinnedEnd";

	/**
	 * A row business object.
	 *
	 * @param name
	 *        The row label.
	 * @param amount
	 *        A number displayed next to it.
	 */
	private record Item(String name, int amount) {
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

	/**
	 * Tests that the pinned column is displayed behind all others, even when the column order puts
	 * it first - a personalization made before the column was pinned, for instance.
	 */
	public void testPinnedColumnIsDisplayedLast() {
		TableView<Item> view = table(List.of(COLUMN_ACTIONS, COLUMN_NAME, COLUMN_AMOUNT));

		assertEquals(List.of(COLUMN_NAME, COLUMN_AMOUNT, COLUMN_ACTIONS), displayedColumns(view));
		assertTrue("The trailing column is the pinned one.", last(view.columns()).pinnedEnd());
		assertFalse("The others are not.", view.columns().get(0).pinnedEnd());
	}

	/**
	 * Tests that the column selection does not offer the pinned column: the user decides neither
	 * about its place nor about whether it is displayed at all.
	 */
	public void testPinnedColumnIsNotOffered() {
		TableView<Item> view = table();

		List<String> offered = new ArrayList<>();
		for (ColumnOption option : view.columnOptions()) {
			offered.add(option.name());
		}
		assertEquals(List.of(COLUMN_NAME, COLUMN_AMOUNT), offered);
	}

	/**
	 * Tests that a column selection leaving the pinned column out keeps it: it returns behind the
	 * chosen columns, in front of nothing.
	 */
	public void testColumnSelectionKeepsThePinnedColumn() {
		TableView<Item> view = table();

		view.setColumnOrder(List.of(COLUMN_AMOUNT));

		assertEquals(List.of(COLUMN_AMOUNT, COLUMN_ACTIONS), displayedColumns(view));
	}

	/**
	 * Tests that the frozen prefix counts the columns the user arranges only: freezing everything
	 * leaves the pinned column unfrozen, where it is fixed to the other edge.
	 */
	public void testFreezingNeverReachesThePinnedColumn() {
		TableView<Item> view = table();

		view.setFrozenColumnCount(3);

		assertEquals("The two columns in front of the pinned one.", 2, view.frozenColumnCount());
		assertFalse("The pinned column is not frozen.", last(view.columns()).frozen());
	}

	/**
	 * Tests that the pinned column keeps its width: a resize of it changes nothing, while the other
	 * columns are resized as usual.
	 */
	public void testPinnedColumnIsNotResized() {
		TestTable table = control();

		resize(table, COLUMN_AMOUNT, 300);
		assertEquals(Integer.valueOf(300), columnState(table, COLUMN_AMOUNT).get(WIDTH));

		resize(table, COLUMN_ACTIONS, 300);

		assertEquals("The pinned column keeps the width it is defined with.",
			Integer.valueOf(ACTIONS_WIDTH), columnState(table, COLUMN_ACTIONS).get(WIDTH));
	}

	/**
	 * Tests that the client is told which column is pinned, and that it is told so for that column
	 * only.
	 */
	public void testClientIsToldAboutThePinnedColumn() {
		TestTable table = control();

		assertEquals(Boolean.TRUE, columnState(table, COLUMN_ACTIONS).get(PINNED_END));
		assertEquals(Boolean.FALSE, columnState(table, COLUMN_NAME).get(PINNED_END));
		assertEquals(Boolean.FALSE, columnState(table, COLUMN_AMOUNT).get(PINNED_END));
	}

	/**
	 * Tests that the command freezing the columns up to the pinned one freezes the ones in front of
	 * it instead of it.
	 */
	public void testFreezeCommandStopsAtThePinnedColumn() {
		TestTable table = control();

		table.executeClientCommand(TableViewControl.CMD_SET_FROZEN_COLUMN_COUNT,
			Map.of(SetFrozenColumnCountArguments.COUNT, Integer.valueOf(3)));

		assertEquals(Integer.valueOf(2), table.clientState(FROZEN_COLUMN_COUNT));
	}

	/** Sends the client's resize command for a column. */
	private static void resize(TestTable table, String column, int width) {
		table.executeClientCommand(TableViewControl.CMD_COLUMN_RESIZE, Map.of(
			ColumnResizeArguments.COLUMN, column,
			ColumnResizeArguments.WIDTH, Integer.valueOf(width)));
	}

	/** The column state the given table pushed to the client for the given column. */
	private static Map<String, Object> columnState(TestTable table, String column) {
		for (Map<String, Object> columnState : clientColumns(table)) {
			if (column.equals(columnState.get(NAME))) {
				return columnState;
			}
		}
		throw new AssertionError("No state for column '" + column + "'.");
	}

	/** The columns the given table pushed to the client. */
	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> clientColumns(TestTable table) {
		return (List<Map<String, Object>>) table.clientState(COLUMNS);
	}

	/** The names of the displayed columns, in display order. */
	private static List<String> displayedColumns(TableView<Item> view) {
		List<String> result = new ArrayList<>();
		for (ColumnView column : view.columns()) {
			result.add(column.name());
		}
		return result;
	}

	/** The last of the given columns. */
	private static ColumnView last(List<ColumnView> columns) {
		return columns.get(columns.size() - 1);
	}

	/** A control over a {@link #table()}. */
	private static TestTable control() {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue());
		return new TestTable(context, table());
	}

	/** A table displaying its columns in declaration order. */
	private static TableView<Item> table() {
		return table(List.of(COLUMN_NAME, COLUMN_AMOUNT, COLUMN_ACTIONS));
	}

	/** A table whose state displays its columns in the given order. */
	private static TableView<Item> table(List<String> order) {
		List<Column<Item, ?>> columns = columns();
		TableViewState state = DefaultTableView.initialState(columns, SortSpec.NONE, List.of());
		state.setColumnOrder(new ArrayList<>(order));
		ListRowSource<Item> rows =
			new ListRowSource<>(new ArrayList<>(List.of(new Item("a", 1), new Item("b", 2))), columns);
		return new DefaultTableView<>(columns, rows, state);
	}

	/** Two ordinary columns and one pinned to the end of the table. */
	private static List<Column<Item, ?>> columns() {
		return List.of(
			DefaultColumn.<Item, String> builder(COLUMN_NAME, Item::name)
				.build(),
			DefaultColumn.<Item, Integer> builder(COLUMN_AMOUNT, Item::amount)
				.build(),
			DefaultColumn.<Item, Item> builder(COLUMN_ACTIONS, item -> item)
				.width(ACTIONS_WIDTH)
				.pinnedEnd(true)
				.build());
	}

	/** Suite requiring the resources the table builds its column headers from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestPinnedColumns.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
