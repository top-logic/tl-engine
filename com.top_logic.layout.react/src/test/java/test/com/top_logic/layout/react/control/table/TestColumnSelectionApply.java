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
import com.top_logic.layout.react.control.table.ColumnLockArguments;
import com.top_logic.layout.react.control.table.ColumnMoveArguments;
import com.top_logic.layout.react.control.table.ColumnVisibleArguments;
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
 * Tests how the {@link TableViewControl} applies the column selection: it holds the selection as a child while the
 * view popover shows its page, and every gesture in it changes the table at once - the displayed columns in their
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

	/** The state key of the column selection the popover shows. */
	private static final String STATE_COLUMN_SELECTION = "columnSelection";

	/** The state keys telling how many of the offered columns are displayed. */
	private static final String STATE_COLUMNS_SHOWN = "columnsShown";

	private static final String STATE_COLUMNS_TOTAL = "columnsTotal";

	private static final String CMD_OPEN_COLUMNS = "openColumns";

	private static final String CMD_CLOSE_COLUMNS = "closeColumns";

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

	/** A control over a table of three columns, exposing what it pushed to the client. */
	private static final class TestTable extends TableViewControl<Item> {

		TestTable(ReactContext context, TableView<Item> view) {
			super(context, view, false);
		}

		Object clientState(String key) {
			return getState(key);
		}

		/** The column selection the table holds for the popover, {@code null} while the page is closed. */
		ReactColumnSelectControl selection() {
			return (ReactColumnSelectControl) clientState(STATE_COLUMN_SELECTION);
		}

		/** Opens the column selection as the client does, and returns it. */
		ReactColumnSelectControl open() {
			executeClientCommand(CMD_OPEN_COLUMNS, Map.of());
			return selection();
		}

		/** Opens {@link #applyColumns(List, List, String)} to the test, keeping the grouping. */
		void apply(List<String> columns, List<String> lockedColumns) {
			applyColumns(columns, lockedColumns, getGroupedColumn());
		}

	}

	/**
	 * Tests that opening the page puts the selection into the state, offering the columns as the table displays
	 * them, and that the table tells how many of them it displays.
	 */
	public void testOpenColumnsPutsTheSelectionIntoTheState() {
		TestTable table = control();
		assertNull(table.selection());
		assertEquals(Integer.valueOf(3), table.clientState(STATE_COLUMNS_SHOWN));
		assertEquals(Integer.valueOf(3), table.clientState(STATE_COLUMNS_TOTAL));

		ReactColumnSelectControl selection = table.open();

		assertNotNull(selection);
		assertEquals(initialOrder(), selection.order());
		assertEquals(initialOrder(), selection.visibleColumns());
		assertEquals(List.of(), selection.lockedColumns());
	}

	/** Tests that the entry of the page can tell n of m: hiding a column changes n, the total stays. */
	public void testTheTableTellsHowManyColumnsItDisplays() {
		TestTable table = control();
		ReactColumnSelectControl selection = table.open();

		visible(selection, COLUMN_AMOUNT, false);

		assertEquals(Integer.valueOf(2), table.clientState(STATE_COLUMNS_SHOWN));
		assertEquals(Integer.valueOf(3), table.clientState(STATE_COLUMNS_TOTAL));
	}

	/** Tests that a leading action column, which the selection does not offer, is none of the counted columns. */
	public void testTheActionColumnIsNotCounted() {
		TestTable table =
			control(withActions(columns()), List.of(COLUMN_ACTIONS, COLUMN_NAME, COLUMN_AMOUNT, COLUMN_STATE));

		assertEquals(Integer.valueOf(3), table.clientState(STATE_COLUMNS_SHOWN));
		assertEquals(Integer.valueOf(3), table.clientState(STATE_COLUMNS_TOTAL));
	}

	/** Tests that a table that does not offer the column selection opens none. */
	public void testNoSelectionWhereTheTableOffersNone() {
		TestTable table = control();
		table.setColumnSelect(false);

		table.executeClientCommand(CMD_OPEN_COLUMNS, Map.of());

		assertNull(table.selection());
	}

	/** Tests that the page opens with the columns the table freezes now locked, a leading action column not counted. */
	public void testFrozenColumnsOpenLocked() {
		TestTable table =
			control(withActions(columns()), List.of(COLUMN_ACTIONS, COLUMN_NAME, COLUMN_AMOUNT, COLUMN_STATE));
		table.getView().setFrozenColumnCount(2);

		ReactColumnSelectControl selection = table.open();

		assertEquals(List.of(COLUMN_NAME, COLUMN_AMOUNT, COLUMN_STATE), selection.order());
		assertEquals(List.of(COLUMN_NAME), selection.lockedColumns());
	}

	/** Tests that hiding a column in the selection hides it in the table at once, and showing it shows it again. */
	public void testHidingAndShowingAppliesAtOnce() {
		TestTable table = control();
		ReactColumnSelectControl selection = table.open();

		visible(selection, COLUMN_AMOUNT, false);
		assertEquals(List.of(COLUMN_NAME, COLUMN_STATE), displayedColumns(table));

		visible(selection, COLUMN_AMOUNT, true);
		assertEquals(initialOrder(), displayedColumns(table));
	}

	/** Tests that a drop in the selection reorders the columns of the table at once. */
	public void testReorderAppliesAtOnce() {
		TestTable table = control();
		ReactColumnSelectControl selection = table.open();

		reorder(selection, COLUMN_STATE, 0);

		assertEquals(List.of(COLUMN_STATE, COLUMN_NAME, COLUMN_AMOUNT), displayedColumns(table));
	}

	/**
	 * Tests that locking a column freezes it at once, as the frozen prefix of the new order, and that releasing it
	 * unfreezes it.
	 */
	public void testLockAndUnlockApplyAtOnce() {
		TestTable table = control();
		ReactColumnSelectControl selection = table.open();

		lock(selection, COLUMN_STATE, true);

		assertEquals(List.of(COLUMN_STATE, COLUMN_NAME, COLUMN_AMOUNT), displayedColumns(table));
		assertEquals(1, table.getView().frozenColumnCount());

		lock(selection, COLUMN_STATE, false);

		assertEquals(0, table.getView().frozenColumnCount());
	}

	/** Tests that a hidden column is shown and frozen by one command, its lock. */
	public void testShowAndFreezeInOneCommand() {
		TestTable table = control();
		ReactColumnSelectControl selection = table.open();
		visible(selection, COLUMN_AMOUNT, false);
		assertEquals(List.of(COLUMN_NAME, COLUMN_STATE), displayedColumns(table));

		lock(selection, COLUMN_AMOUNT, true);

		assertEquals(List.of(COLUMN_AMOUNT, COLUMN_NAME, COLUMN_STATE), displayedColumns(table));
		assertEquals(1, table.getView().frozenColumnCount());
	}

	/**
	 * Tests that hiding twice leaves the last displayed column: the table is never without a column, and the second
	 * command changes nothing.
	 */
	public void testTheLastColumnStaysInTheTable() {
		TestTable table = control();
		ReactColumnSelectControl selection = table.open();

		visible(selection, COLUMN_NAME, false);
		visible(selection, COLUMN_AMOUNT, false);
		visible(selection, COLUMN_STATE, false);

		assertEquals(List.of(COLUMN_STATE), displayedColumns(table));
	}

	/** Tests the round trip of the frozen columns: the page opens with them locked, and a change keeps them frozen. */
	public void testFrozenColumnsStayFrozenWhileAnotherChangeApplies() {
		TestTable table = control();
		table.getView().setFrozenColumnCount(2);
		ReactColumnSelectControl selection = table.open();
		assertEquals(List.of(COLUMN_NAME, COLUMN_AMOUNT), selection.lockedColumns());

		visible(selection, COLUMN_STATE, false);

		assertEquals(List.of(COLUMN_NAME, COLUMN_AMOUNT), displayedColumns(table));
		assertEquals(2, table.getView().frozenColumnCount());
	}

	/**
	 * Tests the round trip with a leading column the selection does not offer: it counts towards the frozen columns
	 * but is no entry of the page, so the page locks only the chosen column, and a change freezes both again.
	 */
	public void testFrozenColumnsRoundTripWithALeadingActionColumn() {
		TestTable table =
			control(withActions(columns()), List.of(COLUMN_ACTIONS, COLUMN_NAME, COLUMN_AMOUNT, COLUMN_STATE));
		table.getView().setFrozenColumnCount(2);
		ReactColumnSelectControl selection = table.open();

		visible(selection, COLUMN_STATE, false);

		assertEquals(List.of(COLUMN_ACTIONS, COLUMN_NAME, COLUMN_AMOUNT), displayedColumns(table));
		assertEquals(2, table.getView().frozenColumnCount());
	}

	/** Tests that the table opens the page with its grouping, and that the grouping chosen there applies at once. */
	public void testGroupingAppliesAtOnce() {
		TestTable table = control();
		table.setGroupedColumn(COLUMN_NAME);

		ReactColumnSelectControl selection = table.open();
		assertEquals(COLUMN_NAME, selection.groupedColumn());

		group(selection, COLUMN_AMOUNT);
		assertEquals(COLUMN_AMOUNT, table.getGroupedColumn());
	}

	/** Tests that removing the grouping in the selection shows the rows ungrouped at once. */
	public void testRemovingTheGroupingAppliesAtOnce() {
		TestTable table = control();
		table.setGroupedColumn(COLUMN_NAME);

		ReactColumnSelectControl selection = table.open();
		group(selection, "");

		assertNull(table.getGroupedColumn());
	}

	/** Tests that the rows can be grouped by a column the table does not display, which its header menu does not offer. */
	public void testGroupingByAHiddenColumn() {
		TestTable table = control();
		ReactColumnSelectControl selection = table.open();
		visible(selection, COLUMN_STATE, false);

		group(selection, COLUMN_STATE);

		assertEquals(COLUMN_STATE, table.getGroupedColumn());
		assertEquals(List.of(COLUMN_NAME, COLUMN_AMOUNT), displayedColumns(table));
	}

	/** Tests that closing the page releases the selection and takes it out of the state; the table stays as it is. */
	public void testCloseColumnsReleasesTheSelection() {
		TestTable table = control();
		ReactColumnSelectControl selection = table.open();
		visible(selection, COLUMN_AMOUNT, false);

		table.executeClientCommand(CMD_CLOSE_COLUMNS, Map.of());

		assertNull(table.selection());
		assertTrue("The released selection is disposed.", selection.isDisposed());
		assertEquals(List.of(COLUMN_NAME, COLUMN_STATE), displayedColumns(table));
	}

	/** Tests that closing a page that is not open is harmless. */
	public void testCloseWithoutSelectionIsHarmless() {
		TestTable table = control();

		table.executeClientCommand(CMD_CLOSE_COLUMNS, Map.of());

		assertNull(table.selection());
	}

	/** Tests that opening the page again replaces the selection and disposes the first one. */
	public void testSecondOpenReplacesAndDisposes() {
		TestTable table = control();
		ReactColumnSelectControl first = table.open();

		ReactColumnSelectControl second = table.open();

		assertNotSame(first, second);
		assertTrue(first.isDisposed());
		assertFalse(second.isDisposed());
	}

	/** Tests that a table leaving the display closes the page: displayed again, it shows no selection nobody opened. */
	public void testDetachClosesThePage() {
		TestTable table = control();
		table.attach();
		ReactColumnSelectControl selection = table.open();

		table.detach();

		assertNull(table.selection());
		assertTrue(selection.isDisposed());
	}

	/** Tests that disposing the table disposes its selection. */
	public void testDisposeReleasesTheSelection() {
		TestTable table = control();
		ReactColumnSelectControl selection = table.open();

		table.cleanupTree();

		assertTrue(selection.isDisposed());
	}

	/** Tests that the locked columns become the frozen prefix of the new column order. */
	public void testLockedColumnsBecomeTheFrozenPrefix() {
		TestTable table = control();

		table.apply(List.of(COLUMN_STATE, COLUMN_NAME, COLUMN_AMOUNT), List.of(COLUMN_STATE, COLUMN_NAME));

		assertEquals(List.of(COLUMN_STATE, COLUMN_NAME, COLUMN_AMOUNT), displayedColumns(table));
		assertEquals(2, table.getView().frozenColumnCount());
	}

	/** Tests that a selection without a locked column releases the columns the table froze before. */
	public void testNoLockedColumnFreezesNothing() {
		TestTable table = control();
		table.getView().setFrozenColumnCount(2);

		table.apply(List.of(COLUMN_NAME, COLUMN_AMOUNT), List.of());

		assertEquals(0, table.getView().frozenColumnCount());
	}

	private static void group(ReactColumnSelectControl selection, String column) {
		assertTrue(selection.executeClientCommand(ReactColumnSelectControl.CMD_GROUP,
			Map.of(GroupArguments.COLUMN, column)).isSuccess());
	}

	private static void visible(ReactColumnSelectControl selection, String column, boolean visible) {
		assertTrue(selection.executeClientCommand(ReactColumnSelectControl.CMD_COLUMN_VISIBLE,
			Map.of(ColumnVisibleArguments.COLUMN, column, ColumnVisibleArguments.VISIBLE, Boolean.valueOf(visible)))
			.isSuccess());
	}

	private static void lock(ReactColumnSelectControl selection, String column, boolean locked) {
		assertTrue(selection.executeClientCommand(ReactColumnSelectControl.CMD_COLUMN_LOCK,
			Map.of(ColumnLockArguments.COLUMN, column, ColumnLockArguments.LOCKED, Boolean.valueOf(locked)))
			.isSuccess());
	}

	private static void reorder(ReactColumnSelectControl selection, String column, int target) {
		assertTrue(selection.executeClientCommand(ReactColumnSelectControl.CMD_COLUMN_MOVE,
			Map.of(ColumnMoveArguments.COLUMN, column, ColumnMoveArguments.TARGET_INDEX, Integer.valueOf(target),
				ColumnMoveArguments.LOCKED, Boolean.FALSE, ColumnMoveArguments.VISIBLE, Boolean.TRUE))
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
