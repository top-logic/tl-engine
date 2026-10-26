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
import com.top_logic.layout.react.control.table.GroupArguments;
import com.top_logic.layout.react.control.table.ReactColumnSelectControl;
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
 * order, the locked ones as the frozen prefix, and the grouping.
 */
public class TestColumnSelectionApply extends TestCase {

	/** The name of the first column. */
	private static final String COLUMN_NAME = "name";

	/** The name of the second column. */
	private static final String COLUMN_AMOUNT = "amount";

	/** The name of the third column. */
	private static final String COLUMN_STATE = "state";

	/** The name of a leading column the user cannot choose, an action column. */
	private static final String COLUMN_ACTIONS = "actions";

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

		/** Opens the protected {@link #applyColumns(List, List, String)} to the test, keeping the grouping. */
		void apply(List<String> columns, List<String> lockedColumns) {
			applyColumns(columns, lockedColumns, getGroupedColumn());
		}

		/** Applies the result of the given column selection, as its OK button does. */
		void apply(ReactColumnSelectControl selection) {
			applyColumns(selection.visibleColumns(), selection.lockedColumns(), selection.groupedColumn());
		}

		/** The column selection as the {@link TableViewControl} creates it when it opens the dialog. */
		ReactColumnSelectControl openSelection() {
			return new ReactColumnSelectControl(getReactContext(), getView().columnOptions(), lockedColumnNames(),
				getGroupedColumn(), true);
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

	/**
	 * Tests the round trip of the frozen columns through the column selection: the dialog opens with the frozen columns
	 * locked, and applying it unchanged freezes the same columns again.
	 */
	public void testFrozenColumnsRoundTripThroughTheSelection() {
		TestTable table = control();
		table.getView().setFrozenColumnCount(2);

		ReactColumnSelectControl selection = table.openSelection();
		assertEquals(List.of(COLUMN_NAME, COLUMN_AMOUNT), selection.lockedColumns());

		table.apply(selection.visibleColumns(), selection.lockedColumns());

		assertEquals(List.of(COLUMN_NAME, COLUMN_AMOUNT, COLUMN_STATE), displayedColumns(table));
		assertEquals(2, table.getView().frozenColumnCount());
	}

	/**
	 * Tests the round trip with a leading column the selection does not offer: it counts towards the frozen columns
	 * but is no entry of the dialog, so the dialog locks only the chosen column, and applying it freezes both again.
	 */
	public void testFrozenColumnsRoundTripWithALeadingActionColumn() {
		TestTable table =
			control(withActions(columns()), List.of(COLUMN_ACTIONS, COLUMN_NAME, COLUMN_AMOUNT, COLUMN_STATE));
		table.getView().setFrozenColumnCount(2);

		ReactColumnSelectControl selection = table.openSelection();
		assertEquals(List.of(COLUMN_NAME, COLUMN_AMOUNT, COLUMN_STATE), selection.order());
		assertEquals(List.of(COLUMN_NAME), selection.lockedColumns());

		table.apply(selection.visibleColumns(), selection.lockedColumns());

		assertEquals(List.of(COLUMN_ACTIONS, COLUMN_NAME, COLUMN_AMOUNT, COLUMN_STATE), displayedColumns(table));
		assertEquals(2, table.getView().frozenColumnCount());
	}

	/**
	 * Tests that the grouping chosen in the selection is applied together with the columns, and that the table opens
	 * the selection with its grouping.
	 */
	public void testGroupingIsAppliedWithTheColumns() {
		TestTable table = control();
		table.setGroupedColumn(COLUMN_NAME);

		ReactColumnSelectControl selection = table.openSelection();
		assertEquals(COLUMN_NAME, selection.groupedColumn());

		group(selection, COLUMN_AMOUNT);
		assertEquals("The grouping waits for the dialog to be applied.", COLUMN_NAME, table.getGroupedColumn());

		table.apply(selection);
		assertEquals(COLUMN_AMOUNT, table.getGroupedColumn());
	}

	/**
	 * Tests that removing the grouping in the selection shows the rows ungrouped once applied.
	 */
	public void testRemovingTheGroupingIsApplied() {
		TestTable table = control();
		table.setGroupedColumn(COLUMN_NAME);

		ReactColumnSelectControl selection = table.openSelection();
		group(selection, "");
		table.apply(selection);

		assertNull(table.getGroupedColumn());
	}

	/**
	 * Tests that the rows can be grouped by a column the table does not display, which its header menu does not offer.
	 */
	public void testGroupingByAHiddenColumn() {
		TestTable table = control();
		table.apply(List.of(COLUMN_NAME, COLUMN_AMOUNT), List.of());

		ReactColumnSelectControl selection = table.openSelection();
		group(selection, COLUMN_STATE);
		table.apply(selection);

		assertEquals(COLUMN_STATE, table.getGroupedColumn());
		assertEquals(List.of(COLUMN_NAME, COLUMN_AMOUNT), displayedColumns(table));
	}

	private static void group(ReactColumnSelectControl selection, String column) {
		assertTrue(selection.executeCommand(ReactColumnSelectControl.CMD_GROUP, Map.of(GroupArguments.COLUMN, column))
			.isSuccess());
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

	/** A control over a table of the three {@link #columns()} in their {@link #initialOrder()}. */
	private static TestTable control() {
		return control(columns(), initialOrder());
	}

	/** A control over a table of the given columns in the given order. */
	private static TestTable control(List<Column<Item, ?>> columns, List<String> order) {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
				new ReactWindowRegistry("test"));
		return new TestTable(context, table(columns, order));
	}

	/** A table of the given columns in the given order. */
	private static TableView<Item> table(List<Column<Item, ?>> columns, List<String> order) {
		TableViewState state = DefaultTableView.initialState(columns, SortSpec.NONE, List.of());
		state.setColumnOrder(new ArrayList<>(order));
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

	/** The given columns behind a leading action column the user can neither choose nor freeze alone. */
	private static List<Column<Item, ?>> withActions(List<Column<Item, ?>> columns) {
		List<Column<Item, ?>> result = new ArrayList<>();
		result.add(DefaultColumn.<Item, String> builder(COLUMN_ACTIONS, Item::name)
			.width(INITIAL_WIDTH)
			.selectable(false)
			.frozenEligible(false)
			.build());
		result.addAll(columns);
		return result;
	}

	/** Suite requiring the resources the table builds its column headers from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestColumnSelectionApply.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
