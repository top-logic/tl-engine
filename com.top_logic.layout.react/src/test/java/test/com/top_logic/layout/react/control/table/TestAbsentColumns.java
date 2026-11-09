/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnOption;
import com.top_logic.table.ColumnView;
import com.top_logic.table.FilterCodec;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.Row;
import com.top_logic.table.RowKind;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableId;
import com.top_logic.table.TableView;
import com.top_logic.table.TableViewListener;
import com.top_logic.table.TableViewState;
import com.top_logic.table.ViewStateStore;
import com.top_logic.table.filter.BooleanColumnFilter;
import com.top_logic.table.filter.BooleanFilterState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Tests the {@link TableView#setAbsentColumns(Set) absent columns} of a {@link DefaultTableView}:
 * columns that currently do not exist for the table, while the user's arrangement of them is kept.
 */
public class TestAbsentColumns extends TestCase {

	/** The name of the first column. */
	private static final String COLUMN_NAME = "name";

	/** The name of the second column, the one made absent. */
	private static final String COLUMN_AMOUNT = "amount";

	/** The name of the third column. */
	private static final String COLUMN_STATE = "state";

	/** The name of the fourth column, a filterable one. */
	private static final String COLUMN_OPEN = "open";

	/** The width every column starts with. */
	private static final int INITIAL_WIDTH = 120;

	/** A width the user gives a column. */
	private static final int USER_WIDTH = 200;

	/**
	 * A row business object.
	 *
	 * @param name
	 *        The row label.
	 * @param amount
	 *        A number displayed next to it.
	 * @param open
	 *        Whether the row is open.
	 */
	private record Item(String name, int amount, boolean open) {
		// Pure value type.
	}

	/** An in-memory {@link ViewStateStore} counting the saves. */
	private static final class CountingStore implements ViewStateStore {

		private final Map<TableId, TableViewState> _states = new HashMap<>();

		private int _saves;

		@Override
		public TableViewState load(TableId id, FilterCodec filters) {
			TableViewState state = _states.get(id);
			return state == null ? null : state.copy();
		}

		@Override
		public void save(TableId id, TableViewState state, FilterCodec filters) {
			_saves++;
			_states.put(id, state.copy());
		}

		/** The number of saves so far. */
		int saves() {
			return _saves;
		}

		/** The state last saved for the given table. */
		TableViewState saved(TableId id) {
			return _states.get(id);
		}

	}

	/** A listener counting the column changes. */
	private static final class ColumnChanges implements TableViewListener {

		private int _count;

		@Override
		public void columnsChanged() {
			_count++;
		}

		/** The number of column changes so far. */
		int count() {
			return _count;
		}

	}

	/**
	 * Tests that an absent column is neither displayed nor offered by the column selection, and is
	 * both again once it exists.
	 */
	public void testAbsentColumnIsNeitherDisplayedNorOffered() {
		DefaultTableView<Item> view = view();
		ColumnChanges changes = new ColumnChanges();
		view.addListener(changes);

		view.setAbsentColumns(Set.of(COLUMN_AMOUNT));

		assertEquals(Set.of(COLUMN_AMOUNT), view.absentColumns());
		assertEquals(List.of(COLUMN_NAME, COLUMN_STATE, COLUMN_OPEN), displayed(view));
		assertEquals(List.of(COLUMN_NAME, COLUMN_STATE, COLUMN_OPEN), offered(view));
		assertEquals(1, changes.count());

		view.setAbsentColumns(Set.of(COLUMN_AMOUNT));
		assertEquals("Setting the same columns again changes nothing.", 1, changes.count());

		view.setAbsentColumns(Set.of());

		assertEquals(List.of(COLUMN_NAME, COLUMN_AMOUNT, COLUMN_STATE, COLUMN_OPEN), displayed(view));
		assertEquals(List.of(COLUMN_NAME, COLUMN_AMOUNT, COLUMN_STATE, COLUMN_OPEN), offered(view));
		assertEquals(2, changes.count());
	}

	/**
	 * Tests that an absent column returns at its old place with its old width, and a hidden one
	 * stays hidden, after the user rearranged the existing columns in the meantime.
	 */
	public void testAbsentColumnReturnsWithItsArrangement() {
		DefaultTableView<Item> view = view();
		view.resizeColumn(COLUMN_AMOUNT, USER_WIDTH);
		view.setColumnVisible(COLUMN_OPEN, false);

		view.setAbsentColumns(Set.of(COLUMN_AMOUNT, COLUMN_OPEN));
		view.setColumnVisible(COLUMN_STATE, false);
		assertEquals(List.of(COLUMN_NAME), displayed(view));

		view.setAbsentColumns(Set.of());

		assertEquals(List.of(COLUMN_NAME, COLUMN_AMOUNT), displayed(view));
		assertEquals(USER_WIDTH, columnView(view, COLUMN_AMOUNT).width());
		assertEquals("The column the user hid is offered, but not displayed.",
			List.of(COLUMN_NAME, COLUMN_AMOUNT, COLUMN_STATE, COLUMN_OPEN), offered(view));
	}

	/**
	 * Tests that a column selection listing only the existing columns keeps the place of an absent
	 * column, and keeps an absent column hidden that the user hid before.
	 */
	public void testColumnSelectionKeepsAbsentColumns() {
		DefaultTableView<Item> view = view();
		view.setColumnVisible(COLUMN_OPEN, false);
		view.setAbsentColumns(Set.of(COLUMN_AMOUNT, COLUMN_OPEN));

		view.setColumnOrder(List.of(COLUMN_NAME));
		assertEquals(List.of(COLUMN_NAME), displayed(view));

		view.setColumnOrder(List.of(COLUMN_STATE, COLUMN_NAME));
		assertEquals(List.of(COLUMN_STATE, COLUMN_NAME), displayed(view));

		view.setAbsentColumns(Set.of());

		assertEquals(List.of(COLUMN_STATE, COLUMN_NAME, COLUMN_AMOUNT), displayed(view));
		assertTrue(view.state().getHiddenColumns().contains(COLUMN_OPEN));
	}

	/**
	 * Tests that a column moved among the displayed columns keeps an absent column in its place.
	 */
	public void testMoveKeepsAbsentColumnInPlace() {
		DefaultTableView<Item> view = view();
		view.setAbsentColumns(Set.of(COLUMN_AMOUNT));

		view.moveColumn(COLUMN_OPEN, 1);
		assertEquals(List.of(COLUMN_NAME, COLUMN_OPEN, COLUMN_STATE), displayed(view));

		view.setAbsentColumns(Set.of());
		assertEquals(List.of(COLUMN_NAME, COLUMN_AMOUNT, COLUMN_OPEN, COLUMN_STATE), displayed(view));
	}

	/**
	 * Tests that the frozen columns are counted among the displayed ones, and a frozen absent column
	 * is frozen again when it returns.
	 */
	public void testFrozenColumnsCountDisplayedColumns() {
		DefaultTableView<Item> view = view();
		view.setFrozenColumnCount(2);

		view.setAbsentColumns(Set.of(COLUMN_AMOUNT));
		assertEquals(1, view.frozenColumnCount());
		assertTrue(columnView(view, COLUMN_NAME).frozen());
		assertFalse(columnView(view, COLUMN_STATE).frozen());

		view.setFrozenColumnCount(2);
		assertEquals(2, view.frozenColumnCount());

		view.setAbsentColumns(Set.of());
		assertEquals(3, view.frozenColumnCount());
	}

	/**
	 * Tests that setting the absent columns persists nothing, and that the state persisted by a
	 * later user action keeps the arrangement of the absent column.
	 */
	public void testAbsentColumnsAreNotPersisted() {
		CountingStore store = new CountingStore();
		TableId id = new TableId("absent");
		DefaultTableView<Item> view = view(store, id);
		view.resizeColumn(COLUMN_AMOUNT, USER_WIDTH);
		int saves = store.saves();

		view.setAbsentColumns(Set.of(COLUMN_AMOUNT));
		view.setAbsentColumns(Set.of());
		view.setAbsentColumns(Set.of(COLUMN_AMOUNT));
		assertEquals(saves, store.saves());

		view.setColumnVisible(COLUMN_STATE, false);

		TableViewState saved = store.saved(id);
		assertEquals(List.of(COLUMN_NAME, COLUMN_AMOUNT, COLUMN_OPEN), saved.getColumnOrder());
		assertEquals(Integer.valueOf(USER_WIDTH), saved.getWidths().get(COLUMN_AMOUNT));

		DefaultTableView<Item> restored = view(store, id);
		assertEquals(List.of(COLUMN_NAME, COLUMN_AMOUNT, COLUMN_OPEN), displayed(restored));
	}

	/**
	 * Tests that a filter on an absent column is kept, but restricts the rows only while the column
	 * exists.
	 */
	public void testFilterOnAbsentColumnTakesNoEffect() {
		DefaultTableView<Item> view = view();
		view.filter(COLUMN_OPEN, new BooleanFilterState(true, false, false));
		assertEquals(1, view.rowCount());

		view.setAbsentColumns(Set.of(COLUMN_OPEN));
		assertEquals(2, view.rowCount());
		assertTrue(view.state().getFilters().containsKey(COLUMN_OPEN));

		view.setAbsentColumns(Set.of());
		assertEquals(1, view.rowCount());
	}

	/**
	 * Tests that a sort by an absent column is kept, but orders the rows only while the column
	 * exists.
	 */
	public void testSortByAbsentColumnTakesNoEffect() {
		DefaultTableView<Item> view = view();
		view.sort(new SortSpec(List.of(new SortColumn(COLUMN_AMOUNT, false))));
		assertEquals(List.of("b", "a"), rowNames(view));

		view.setAbsentColumns(Set.of(COLUMN_AMOUNT));
		assertEquals(List.of("a", "b"), rowNames(view));
		assertEquals(1, view.state().getSort().size());

		view.setAbsentColumns(Set.of());
		assertEquals(List.of("b", "a"), rowNames(view));
	}

	/**
	 * Tests that a grouping by an absent column is kept, but groups the rows only while the column
	 * exists.
	 */
	public void testGroupingByAbsentColumnTakesNoEffect() {
		DefaultTableView<Item> view = view();
		view.group(new GroupSpec(List.of(COLUMN_OPEN)));
		assertTrue(hasGroupHeader(view));

		view.setAbsentColumns(Set.of(COLUMN_OPEN));
		assertFalse(hasGroupHeader(view));
		assertEquals(List.of(COLUMN_OPEN), view.state().getGrouping().columns());

		view.setAbsentColumns(Set.of());
		assertTrue(hasGroupHeader(view));
	}

	/**
	 * Tests that restoring the defined arrangement does not make an absent column appear.
	 */
	public void testResetKeepsAbsentColumnsAbsent() {
		DefaultTableView<Item> view = view();
		view.setAbsentColumns(Set.of(COLUMN_AMOUNT));

		view.resetToDefaults();

		assertEquals(List.of(COLUMN_NAME, COLUMN_STATE, COLUMN_OPEN), displayed(view));
		assertEquals(List.of(COLUMN_NAME, COLUMN_STATE, COLUMN_OPEN), offered(view));
	}

	private static boolean hasGroupHeader(TableView<Item> view) {
		for (Row<Item> row : view.allRows()) {
			if (row.kind() == RowKind.GROUP_HEADER) {
				return true;
			}
		}
		return false;
	}

	/** The names of the data rows of the given table, in display order. */
	private static List<String> rowNames(TableView<Item> view) {
		List<String> result = new ArrayList<>();
		for (Row<Item> row : view.rows(0, view.rowCount())) {
			if (row.kind() == RowKind.DATA) {
				result.add(row.data().name());
			}
		}
		return result;
	}

	/** The names of the displayed columns of the given table, in display order. */
	private static List<String> displayed(TableView<Item> view) {
		List<String> result = new ArrayList<>();
		for (ColumnView columnView : view.columns()) {
			result.add(columnView.name());
		}
		return result;
	}

	/** The names of the columns the column selection of the given table offers. */
	private static List<String> offered(TableView<Item> view) {
		List<String> result = new ArrayList<>();
		for (ColumnOption option : view.columnOptions()) {
			result.add(option.name());
		}
		return result;
	}

	/** The displayed column of the given name. */
	private static ColumnView columnView(TableView<Item> view, String name) {
		for (ColumnView columnView : view.columns()) {
			if (columnView.name().equals(name)) {
				return columnView;
			}
		}
		throw new AssertionError("Column not displayed: " + name);
	}

	/** A table of the {@link #columns()} without persistence. */
	private static DefaultTableView<Item> view() {
		return view(null, null);
	}

	/** A table of the {@link #columns()} persisting through the given store. */
	private static DefaultTableView<Item> view(ViewStateStore store, TableId id) {
		List<Column<Item, ?>> columns = columns();
		ListRowSource<Item> rows = new ListRowSource<>(
			new ArrayList<>(List.of(new Item("a", 1, true), new Item("b", 2, false))), columns);
		return DefaultTableView.create(columns, rows, store, id);
	}

	/** The four columns of the table. */
	private static List<Column<Item, ?>> columns() {
		return List.of(
			DefaultColumn.<Item, String> builder(COLUMN_NAME, Item::name)
				.width(INITIAL_WIDTH)
				.build(),
			DefaultColumn.<Item, Integer> builder(COLUMN_AMOUNT, Item::amount)
				.width(INITIAL_WIDTH)
				.sort(() -> Comparator.naturalOrder())
				.build(),
			DefaultColumn.<Item, String> builder(COLUMN_STATE, Item::name)
				.width(INITIAL_WIDTH)
				.build(),
			DefaultColumn.<Item, Boolean> builder(COLUMN_OPEN, Item::open)
				.width(INITIAL_WIDTH)
				.filter(BooleanColumnFilter.INSTANCE)
				.build());
	}

	/** Suite requiring the resources the table builds its column headers from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestAbsentColumns.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
