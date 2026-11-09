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
import com.top_logic.layout.react.control.table.ColumnMoveArguments;
import com.top_logic.layout.react.control.table.ReactColumnSelectControl;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnView;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableView;
import com.top_logic.table.TableViewState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Tests the {@code columnMove} command of the {@link TableViewControl}: a column dragged in the table header
 * changes its place and whether it is frozen in one step, the number of frozen columns following it across the
 * border.
 */
public class TestHeaderColumnMove extends TestCase {

	/** State key of the client's column list. */
	private static final String COLUMNS = "columns";

	/** State key of the number of frozen columns. */
	private static final String FROZEN_COLUMN_COUNT = "frozenColumnCount";

	/** Per-column state key of the column name. */
	private static final String NAME = "name";

	/** Per-column state key telling whether the user may move the column in the header. */
	private static final String REORDERABLE = "reorderable";

	/** The movable columns. */
	private static final List<String> FIVE = List.of("a", "b", "c", "d", "e");

	/** The column pinned to the end of the table. */
	private static final String ACTIONS = "actions";

	/** The leading column the user cannot arrange. */
	private static final String DETAIL = "detail";

	/**
	 * A row business object.
	 *
	 * @param name
	 *        The row label.
	 */
	private record Item(String name) {
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

	/** A movable column dropped behind the last frozen one with {@code locked} becomes frozen at that place. */
	public void testMovableBehindTheBlockIsFrozen() {
		TestTable table = control(FIVE, 2);

		move(table, "d", 2, true);

		assertEquals(List.of("a", "b", "d", "c", "e"), order(table));
		assertEquals(3, frozen(table));
	}

	/** A frozen column dropped at the border with {@code locked=false} is released and keeps its place. */
	public void testLastFrozenIsReleasedInPlace() {
		TestTable table = control(FIVE, 2);

		move(table, "b", 1, false);

		assertEquals(FIVE, order(table));
		assertEquals(1, frozen(table));
	}

	/** A movable column dragged far to the front, not frozen, stops behind the frozen block. */
	public void testMovableDraggedToTheFrontStopsBehindTheBlock() {
		TestTable table = control(FIVE, 2);

		move(table, "d", 0, false);

		assertEquals(List.of("a", "b", "d", "c", "e"), order(table));
		assertEquals(2, frozen(table));
	}

	/** A frozen column dragged far to the back, still frozen, stops at the end of the block. */
	public void testFrozenDraggedToTheBackStopsAtTheEndOfTheBlock() {
		TestTable table = control(FIVE, 2);

		move(table, "a", 9, true);

		assertEquals(List.of("b", "a", "c", "d", "e"), order(table));
		assertEquals(2, frozen(table));
	}

	/** A frozen column dragged to the back and released moves behind the block, which shrinks. */
	public void testFrozenReleasedFarBack() {
		TestTable table = control(FIVE, 2);

		move(table, "a", 3, false);

		assertEquals(List.of("b", "c", "d", "a", "e"), order(table));
		assertEquals(1, frozen(table));
	}

	/** Without frozen columns, the first column moved with {@code locked} makes a block of one. */
	public void testFirstColumnFreezesWithoutBlock() {
		TestTable table = control(FIVE, 0);

		move(table, "a", 0, true);

		assertEquals(FIVE, order(table));
		assertEquals(1, frozen(table));
	}

	/** A column pinned to the end is never passed: the target is clamped, and the count never reaches it. */
	public void testPinnedColumnTrailsAndIsNeverFrozen() {
		TestTable table = control(List.of("a", "b", "c", ACTIONS), 0, Spec.PINNED);

		move(table, "a", 99, false);
		assertEquals(List.of("b", "c", "a", ACTIONS), order(table));
		assertEquals(0, frozen(table));

		move(table, "b", 99, true);
		assertEquals("Frozen, the column ends the block at the latest.", List.of("b", "c", "a", ACTIONS), order(table));
		assertEquals(1, frozen(table));

		move(table, "a", 0, true);
		move(table, "c", 99, true);
		assertEquals(List.of("a", "b", "c", ACTIONS), order(table));
		assertEquals("The count stops in front of the pinned column.", 3, frozen(table));
	}

	/** Hiding, a pinned column and an unknown column change nothing. */
	public void testRejectedMovesChangeNothing() {
		TestTable table = control(List.of("a", "b", "c", ACTIONS), 1, Spec.PINNED);

		move(table, "c", 0, true, false);
		move(table, ACTIONS, 0, false);
		move(table, "unknown", 0, true);

		assertEquals(List.of("a", "b", "c", ACTIONS), order(table));
		assertEquals(1, frozen(table));
	}

	/** A column the column selection does not offer is not moved, and a movable one dragged before it lands behind. */
	public void testUnselectableColumnStaysInFront() {
		TestTable table = control(List.of(DETAIL, "a", "b", "c"), 0, Spec.UNSELECTABLE);

		move(table, DETAIL, 2, false);
		assertEquals(List.of(DETAIL, "a", "b", "c"), order(table));
		assertEquals(0, frozen(table));

		move(table, "c", 0, false);
		assertEquals(List.of(DETAIL, "c", "a", "b"), order(table));
		assertEquals(0, frozen(table));

		move(table, "a", 0, true);
		assertEquals("The detail column does not become the frozen one.", List.of(DETAIL, "a", "c", "b"), order(table));
		assertEquals("The count includes the detail column in front.", 2, frozen(table));
	}

	/** Every column tells the client whether the header may move it. */
	public void testClientIsToldWhichColumnsMove() {
		TestTable table = control(List.of(DETAIL, "a", ACTIONS), 0, Spec.UNSELECTABLE, Spec.PINNED);

		assertEquals(Boolean.FALSE, columnState(table, DETAIL).get(REORDERABLE));
		assertEquals(Boolean.TRUE, columnState(table, "a").get(REORDERABLE));
		assertEquals(Boolean.FALSE, columnState(table, ACTIONS).get(REORDERABLE));
	}

	/** The client state follows the move: order and frozen count are pushed. */
	public void testClientStateFollowsTheMove() {
		TestTable table = control(FIVE, 2);

		move(table, "d", 2, true);

		List<String> pushed = new ArrayList<>();
		for (Map<String, Object> column : clientColumns(table)) {
			pushed.add((String) column.get(NAME));
		}
		assertEquals(List.of("a", "b", "d", "c", "e"), pushed);
		assertEquals(Integer.valueOf(3), table.clientState(FROZEN_COLUMN_COUNT));
	}

	/** A move leaves the grouping as it is. */
	public void testGroupedTableStaysGrouped() {
		TestTable table = control(FIVE, 2, Spec.GROUPED);
		GroupSpec grouping = table.getView().state().getGrouping();
		assertFalse(grouping.columns().isEmpty());

		move(table, "d", 2, true);

		assertEquals(grouping, table.getView().state().getGrouping());
		assertEquals(List.of("a", "b", "d", "c", "e"), order(table));
	}

	private enum Spec {
		PINNED, UNSELECTABLE, GROUPED
	}

	private static void move(TestTable table, String column, int target, boolean locked) {
		move(table, column, target, locked, true);
	}

	private static void move(TestTable table, String column, int target, boolean locked, boolean visible) {
		table.executeClientCommand(ReactColumnSelectControl.CMD_COLUMN_MOVE, Map.of(
			ColumnMoveArguments.COLUMN, column,
			ColumnMoveArguments.TARGET_INDEX, Integer.valueOf(target),
			ColumnMoveArguments.LOCKED, Boolean.valueOf(locked),
			ColumnMoveArguments.VISIBLE, Boolean.valueOf(visible)));
	}

	private static List<String> order(TestTable table) {
		List<String> result = new ArrayList<>();
		for (ColumnView column : table.getView().columns()) {
			result.add(column.name());
		}
		return result;
	}

	private static int frozen(TestTable table) {
		return table.getView().frozenColumnCount();
	}

	private static Map<String, Object> columnState(TestTable table, String column) {
		for (Map<String, Object> columnState : clientColumns(table)) {
			if (column.equals(columnState.get(NAME))) {
				return columnState;
			}
		}
		throw new AssertionError("No state for column '" + column + "'.");
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> clientColumns(TestTable table) {
		return (List<Map<String, Object>>) table.clientState(COLUMNS);
	}

	/**
	 * A control over a table displaying the given columns in the given order with the given number of frozen
	 * ones. The column named {@link #ACTIONS} is pinned to the end with {@link Spec#PINNED}, the one named
	 * {@link #DETAIL} is not selectable with {@link Spec#UNSELECTABLE}.
	 */
	private static TestTable control(List<String> names, int frozen, Spec... specs) {
		List<Spec> options = List.of(specs);
		List<Column<Item, ?>> columns = new ArrayList<>();
		for (String name : names) {
			DefaultColumn.Builder<Item, String> builder = DefaultColumn.<Item, String> builder(name, Item::name);
			if (options.contains(Spec.PINNED) && name.equals(ACTIONS)) {
				builder.pinnedEnd(true);
			}
			if (options.contains(Spec.UNSELECTABLE) && name.equals(DETAIL)) {
				builder.selectable(false).frozenEligible(false);
			}
			columns.add(builder.build());
		}
		TableViewState state = DefaultTableView.initialState(columns, SortSpec.NONE, List.of());
		state.setFrozenCount(frozen);
		if (options.contains(Spec.GROUPED)) {
			state.setGrouping(new GroupSpec(List.of("a")));
		}
		ListRowSource<Item> rows =
			new ListRowSource<>(new ArrayList<>(List.of(new Item("a"), new Item("b"))), columns);
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
				new ReactWindowRegistry("test"));
		return new TestTable(context, new DefaultTableView<>(columns, rows, state));
	}

	/** Suite requiring the resources the table builds its column headers from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestHeaderColumnMove.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
