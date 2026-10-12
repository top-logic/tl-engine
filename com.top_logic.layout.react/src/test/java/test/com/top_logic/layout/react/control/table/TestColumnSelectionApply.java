/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.List;

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
import com.top_logic.table.Column;
import com.top_logic.table.ColumnView;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableView;
import com.top_logic.table.TableViewState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Tests how the {@link TableViewControl} applies the result of the column selection: the displayed columns in their
 * order, and the locked ones as the frozen prefix.
 */
public class TestColumnSelectionApply extends TestCase {

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

		/** Opens the protected {@link #applyColumns(List, List)} to the test. */
		void apply(List<String> columns, List<String> lockedColumns) {
			applyColumns(columns, lockedColumns);
		}

	}

	/**
	 * Tests that the locked columns become the frozen prefix of the new column order.
	 */
	public void testLockedColumnsBecomeTheFrozenPrefix() {
		TestTable table = control();

		table.apply(List.of(COLUMN_STATE, COLUMN_NAME, COLUMN_AMOUNT), List.of(COLUMN_STATE, COLUMN_NAME));

		assertEquals(List.of(COLUMN_STATE, COLUMN_NAME, COLUMN_AMOUNT), displayedColumns(table));
		assertEquals(2, table.getView().frozenColumnCount());
	}

	/**
	 * Tests that a selection without a locked column releases the columns the table froze before.
	 */
	public void testNoLockedColumnFreezesNothing() {
		TestTable table = control();
		table.getView().setFrozenColumnCount(2);

		table.apply(List.of(COLUMN_NAME, COLUMN_AMOUNT), List.of());

		assertEquals(0, table.getView().frozenColumnCount());
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
			ServiceTestSetup.createSetup(TestColumnSelectionApply.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
