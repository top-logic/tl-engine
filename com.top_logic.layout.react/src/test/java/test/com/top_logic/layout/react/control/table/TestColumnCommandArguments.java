/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.top_logic.layout.react.control.table.ColumnReorderArguments;
import com.top_logic.layout.react.control.table.ColumnResizeArguments;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnView;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableView;
import com.top_logic.table.TableViewState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests how the column commands of the {@link TableViewControl} treat the values the client sends
 * for {@link ColumnResizeArguments#getWidth()} and {@link ColumnReorderArguments#getTargetIndex()}:
 * a whole number is applied, anything else is rejected and leaves the table as it was.
 */
public class TestColumnCommandArguments extends TestCase {

	/** The name of the first column. */
	private static final String COLUMN_NAME = "name";

	/** The name of the second column. */
	private static final String COLUMN_AMOUNT = "amount";

	/** The name of the third column. */
	private static final String COLUMN_STATE = "state";

	/** The width every column starts with. */
	private static final int INITIAL_WIDTH = 120;

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

	/** A control over a table of three columns. */
	private static final class TestTable extends TableViewControl<Item> {

		TestTable(ReactContext context, TableView<Item> view) {
			super(context, view, false);
		}

	}

	/**
	 * Tests that a whole-number width resizes the column: the value the client is expected to send.
	 */
	public void testIntegerWidthResizesTheColumn() {
		TestTable table = control();

		HandlerResult result = resize(table, COLUMN_AMOUNT, Integer.valueOf(148));

		assertTrue("A whole number is the width the command takes.", result.isSuccess());
		assertEquals(148, width(table, COLUMN_AMOUNT));
	}

	/**
	 * Tests that a fractional width is rejected and changes nothing: the column keeps the width it
	 * had instead of ending up somewhere between the old and the requested one.
	 */
	public void testFractionalWidthIsRejected() {
		TestTable table = control();

		HandlerResult result = resize(table, COLUMN_AMOUNT, Double.valueOf(147.85));

		assertFalse("A fractional width is no column width.", result.isSuccess());
		assertEquals(INITIAL_WIDTH, width(table, COLUMN_AMOUNT));
	}

	/**
	 * Tests that a width sent as <code>null</code> is rejected and changes nothing.
	 */
	public void testNullWidthIsRejected() {
		TestTable table = control();

		Map<String, Object> arguments = new HashMap<>();
		arguments.put(ColumnResizeArguments.COLUMN, COLUMN_AMOUNT);
		arguments.put(ColumnResizeArguments.WIDTH, null);
		HandlerResult result = table.executeCommand(TableViewControl.CMD_COLUMN_RESIZE, arguments);

		assertFalse("A column has no width of nothing.", result.isSuccess());
		assertEquals(INITIAL_WIDTH, width(table, COLUMN_AMOUNT));
	}

	/**
	 * Tests that a resize without a width at all is rejected and changes nothing.
	 */
	public void testMissingWidthIsRejected() {
		TestTable table = control();

		HandlerResult result = table.executeCommand(TableViewControl.CMD_COLUMN_RESIZE,
			Map.of(ColumnResizeArguments.COLUMN, COLUMN_AMOUNT));

		assertFalse("The width is the point of the command.", result.isSuccess());
		assertEquals(INITIAL_WIDTH, width(table, COLUMN_AMOUNT));
	}

	/**
	 * Tests that a whole-number target index moves the column: the value the client is expected to
	 * send.
	 */
	public void testIntegerTargetIndexMovesTheColumn() {
		TestTable table = control();

		HandlerResult result = reorder(table, COLUMN_NAME, Integer.valueOf(2));

		assertTrue("A whole number is the position the command takes.", result.isSuccess());
		assertEquals(List.of(COLUMN_AMOUNT, COLUMN_STATE, COLUMN_NAME), displayedColumns(table));
	}

	/**
	 * Tests that a target index that is no number is rejected and leaves the column order as it
	 * was.
	 */
	public void testNonNumericTargetIndexIsRejected() {
		TestTable table = control();

		HandlerResult result = reorder(table, COLUMN_NAME, "x");

		assertFalse("A word is no column position.", result.isSuccess());
		assertEquals(initialOrder(), displayedColumns(table));
	}

	/**
	 * Tests that a move without a target index is rejected and leaves the column order as it was.
	 */
	public void testMissingTargetIndexIsRejected() {
		TestTable table = control();

		HandlerResult result = table.executeCommand(TableViewControl.CMD_COLUMN_REORDER,
			Map.of(ColumnReorderArguments.COLUMN, COLUMN_NAME));

		assertFalse("The target position is the point of the command.", result.isSuccess());
		assertEquals(initialOrder(), displayedColumns(table));
	}

	/** Sends the client's resize command with the given raw width value. */
	private static HandlerResult resize(TestTable table, String column, Object width) {
		return table.executeCommand(TableViewControl.CMD_COLUMN_RESIZE, Map.of(
			ColumnResizeArguments.COLUMN, column,
			ColumnResizeArguments.WIDTH, width));
	}

	/** Sends the client's reorder command with the given raw target index value. */
	private static HandlerResult reorder(TestTable table, String column, Object targetIndex) {
		return table.executeCommand(TableViewControl.CMD_COLUMN_REORDER, Map.of(
			ColumnReorderArguments.COLUMN, column,
			ColumnReorderArguments.TARGET_INDEX, targetIndex));
	}

	/** The width of the given column of the given table. */
	private static int width(TestTable table, String column) {
		for (ColumnView columnView : table.getView().columns()) {
			if (column.equals(columnView.name())) {
				return columnView.width();
			}
		}
		throw new AssertionError("No column '" + column + "'.");
	}

	/** The names of the displayed columns of the given table, in display order. */
	private static List<String> displayedColumns(TestTable table) {
		List<String> result = new ArrayList<>();
		for (ColumnView columnView : table.getView().columns()) {
			result.add(columnView.name());
		}
		return result;
	}

	/** The column order a freshly created {@link #control()} displays. */
	private static List<String> initialOrder() {
		return List.of(COLUMN_NAME, COLUMN_AMOUNT, COLUMN_STATE);
	}

	/** A control over a {@link #table()}. */
	private static TestTable control() {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
				new ReactWindowRegistry("test"));
		return new TestTable(context, table());
	}

	/** A table of three columns of equal width. */
	private static TableView<Item> table() {
		List<Column<Item, ?>> columns = columns();
		TableViewState state = DefaultTableView.initialState(columns, SortSpec.NONE, List.of());
		state.setColumnOrder(new ArrayList<>(initialOrder()));
		ListRowSource<Item> rows =
			new ListRowSource<>(new ArrayList<>(List.of(new Item("a", 1), new Item("b", 2))), columns);
		return new DefaultTableView<>(columns, rows, state);
	}

	/** Three plain columns. */
	private static List<Column<Item, ?>> columns() {
		return List.of(
			DefaultColumn.<Item, String> builder(COLUMN_NAME, Item::name)
				.width(INITIAL_WIDTH)
				.build(),
			DefaultColumn.<Item, Integer> builder(COLUMN_AMOUNT, Item::amount)
				.width(INITIAL_WIDTH)
				.build(),
			DefaultColumn.<Item, String> builder(COLUMN_STATE, Item::name)
				.width(INITIAL_WIDTH)
				.build());
	}

	/** Suite requiring the resources the table builds its column headers from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestColumnCommandArguments.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
