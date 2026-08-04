/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.table;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import junit.framework.TestCase;

import com.top_logic.basic.util.ResKey;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.ColumnOption;
import com.top_logic.table.ColumnView;
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterState;
import com.top_logic.table.NegatedFilterState;
import com.top_logic.table.Row;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortDirection;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableId;
import com.top_logic.table.TableView;
import com.top_logic.table.TableViewListener;
import com.top_logic.table.TableViewState;
import com.top_logic.table.ViewStateStore;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.filter.TextFilterState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.table.impl.TableViewStateCodec;

/**
 * Test for {@link DefaultTableView}: column descriptors, cell rendering and the command
 * surface (sort, filter, reorder, visibility) over an in-memory {@link ListRowSource}.
 */
public class TestDefaultTableView extends TestCase {

	private record Person(String name, int age) {
		// Test fixture.
	}

	private record Contains(String needle) implements FilterState {
		@Override
		public boolean isEmpty() {
			return needle.isEmpty();
		}
	}

	/** An in-memory {@link ViewStateStore} routing through the JSON codec, like the real store. */
	private static final class MapViewStateStore implements ViewStateStore {
		private final java.util.Map<String, java.util.Map<String, Object>> _data = new java.util.HashMap<>();

		@Override
		public TableViewState load(TableId id, com.top_logic.table.FilterCodec filters) {
			java.util.Map<String, Object> json = _data.get(id.value());
			if (json == null) {
				return null;
			}
			TableViewState state = new TableViewState();
			TableViewStateCodec.readInto(state, json, filters);
			return state;
		}

		@Override
		public void save(TableId id, TableViewState state, com.top_logic.table.FilterCodec filters) {
			_data.put(id.value(), TableViewStateCodec.toJson(state, filters));
		}
	}

	private List<Column<Person, ?>> personColumns() {
		Column<Person, String> name = DefaultColumn.<Person, String> builder("name", Person::name)
			.sort(() -> Comparator.naturalOrder())
			.filter(new ColumnFilter<>() {
				@Override
				public FilterInput input() {
					return new FilterInput.Text();
				}

				@Override
				public Predicate<String> predicate(FilterState state) {
					return value -> value != null && value.contains(((Contains) state).needle());
				}
			})
			.width(200)
			.build();
		Column<Person, Integer> age = DefaultColumn.<Person, Integer> builder("age", Person::age)
			.sort(() -> Comparator.naturalOrder())
			.build();
		return List.of(name, age);
	}

	private List<Person> people() {
		return List.of(
			new Person("Charlie", 30),
			new Person("alice", 25),
			new Person("Bob", 40));
	}

	private TableView<Person> newView() {
		List<Column<Person, ?>> columns = personColumns();
		return DefaultTableView.create(columns, new ListRowSource<>(people(), columns));
	}

	private TableView<Person> newView(ViewStateStore store, TableId id) {
		List<Column<Person, ?>> columns = personColumns();
		return DefaultTableView.create(columns, new ListRowSource<>(people(), columns), store, id);
	}

	private List<String> names(TableView<Person> view) {
		return view.rows(0, view.rowCount()).stream().map(r -> r.data().name()).toList();
	}

	public void testColumnsDescriptor() {
		List<ColumnView> columns = newView().columns();
		assertEquals(2, columns.size());
		ColumnView name = columns.get(0);
		assertEquals("name", name.name());
		assertEquals(200, name.width());
		assertTrue(name.sortable());
		assertTrue(name.filterable());
		assertNull(name.sortDirection());
		assertEquals(0, name.sortPriority());
	}

	public void testCellRendering() {
		TableView<Person> view = newView();
		Row<Person> first = view.rows(0, 1).get(0);
		CellContent content = view.cell(first, "name");
		assertTrue(content instanceof CellContent.Text);
		assertEquals("Charlie", ((CellContent.Text) content).text());
	}

	public void testSortCommandUpdatesRowsAndHeader() {
		TableView<Person> view = newView();
		view.sort(SortSpec.ascending("name"));
		assertEquals(List.of("Bob", "Charlie", "alice"), names(view));

		ColumnView name = view.columns().get(0);
		assertEquals(SortDirection.ASC, name.sortDirection());
		assertEquals(1, name.sortPriority());
	}

	public void testFilterCommand() {
		TableView<Person> view = newView();
		view.filter("name", new Contains("li"));
		assertEquals(List.of("Charlie", "alice"), names(view));

		// Clearing with an empty state restores all rows.
		view.filter("name", new Contains(""));
		assertEquals(3, view.rowCount());
	}

	public void testInvertedFilterAcceptsNonMatching() {
		TableView<Person> view = newView();
		// "Charlie" and "alice" contain "li"; inverting keeps exactly the rest.
		view.filter("name", new NegatedFilterState(new Contains("li")));
		assertEquals(List.of("Bob"), names(view));
	}

	public void testFilterPersistedAndRestored() {
		ViewStateStore store = new MapViewStateStore();
		TableId id = new TableId("t-filter");

		List<Column<Person, ?>> columns1 = textFilterColumns();
		TableView<Person> view1 =
			DefaultTableView.create(columns1, new ListRowSource<>(people(), columns1), store, id);
		view1.filter("name", TextFilterState.contains("li"));
		assertEquals(List.of("Charlie", "alice"), names(view1));

		// A fresh view over the same store restores the persisted filter (and re-applies it).
		List<Column<Person, ?>> columns2 = textFilterColumns();
		TableView<Person> view2 =
			DefaultTableView.create(columns2, new ListRowSource<>(people(), columns2), store, id);
		assertEquals(List.of("Charlie", "alice"), names(view2));
	}

	private List<Column<Person, ?>> textFilterColumns() {
		Column<Person, String> name = DefaultColumn.<Person, String> builder("name", Person::name)
			.filter(TextColumnFilter.forStrings())
			.width(200)
			.build();
		Column<Person, Integer> age = DefaultColumn.<Person, Integer> builder("age", Person::age).build();
		return List.of(name, age);
	}

	public void testMoveColumn() {
		TableView<Person> view = newView();
		view.moveColumn("age", 0);
		assertEquals(List.of("age", "name"),
			view.columns().stream().map(ColumnView::name).toList());
	}

	public void testColumnVisibility() {
		TableView<Person> view = newView();
		view.setColumnVisible("age", false);
		assertEquals(List.of("name"),
			view.columns().stream().map(ColumnView::name).toList());
	}

	public void testColumnOptionsOfferHiddenColumnsLast() {
		TableView<Person> view = newView();
		view.setColumnVisible("age", false);

		List<ColumnOption> options = view.columnOptions();
		assertEquals(List.of("name", "age"), options.stream().map(ColumnOption::name).toList());
		assertTrue(options.get(0).visible());
		assertFalse("A hidden column is still offered as an option.", options.get(1).visible());
	}

	public void testColumnOptionsFollowDisplayOrder() {
		TableView<Person> view = newView();
		view.moveColumn("age", 0);
		assertEquals(List.of("age", "name"), view.columnOptions().stream().map(ColumnOption::name).toList());
	}

	public void testSetColumnOrderAppliesSelectionAndOrder() {
		TableView<Person> view = newView();
		view.setColumnOrder(List.of("age"));
		assertEquals(List.of("age"), view.columns().stream().map(ColumnView::name).toList());

		// The dropped column comes back through the same command, in the requested position.
		view.setColumnOrder(List.of("name", "age"));
		assertEquals(List.of("name", "age"), view.columns().stream().map(ColumnView::name).toList());
	}

	public void testSetColumnOrderIgnoresUnknownAndRepeatedColumns() {
		TableView<Person> view = newView();
		view.setColumnOrder(List.of("age", "missing", "age", "name"));
		assertEquals(List.of("age", "name"), view.columns().stream().map(ColumnView::name).toList());
	}

	public void testSetColumnOrderShrinksFrozenPrefix() {
		TableView<Person> view = newView();
		view.setFrozenColumnCount(2);
		view.setColumnOrder(List.of("name"));
		assertEquals("The frozen prefix cannot reach beyond the remaining columns.", 1, view.frozenColumnCount());
	}

	public void testDefaultColumnOrderIsDeclarationOrder() {
		TableView<Person> view = newView();
		view.setColumnOrder(List.of("age"));
		assertEquals(List.of("name", "age"), view.defaultColumnOrder());
	}

	public void testColumnSelectionPersistedAndRestored() {
		ViewStateStore store = new MapViewStateStore();
		TableId id = new TableId("t-columns");

		TableView<Person> view1 = newView(store, id);
		view1.setColumnOrder(List.of("age"));

		TableView<Person> view2 = newView(store, id);
		// The hidden column is restored as an option, not as a displayed column.
		assertEquals(List.of("age"), view2.columns().stream().map(ColumnView::name).toList());
		assertEquals(List.of("age", "name"), view2.columnOptions().stream().map(ColumnOption::name).toList());
	}

	private TableView<Person> newViewOffering(String hiddenColumn, ViewStateStore store, TableId id) {
		List<Column<Person, ?>> columns = personColumns();
		return DefaultTableView.create(columns, new ListRowSource<>(people(), columns), store, id,
			SortSpec.NONE, List.of(hiddenColumn));
	}

	public void testColumnHiddenByDefaultIsOfferedNotDisplayed() {
		TableView<Person> view = newViewOffering("age", null, null);
		assertEquals(List.of("name"), view.columns().stream().map(ColumnView::name).toList());

		List<ColumnOption> options = view.columnOptions();
		assertEquals(List.of("name", "age"), options.stream().map(ColumnOption::name).toList());
		assertFalse(options.get(1).visible());
		assertEquals("Resetting must not show what the table only offers.", List.of("name"),
			view.defaultColumnOrder());
	}

	public void testColumnHiddenByDefaultStaysHiddenOnRestore() {
		ViewStateStore store = new MapViewStateStore();
		TableId id = new TableId("t-offered");

		// A personalization that predates the offered column: it knows neither of its display nor of
		// its exclusion.
		TableViewState persisted = new TableViewState();
		persisted.setColumnOrder(List.of("name"));
		store.save(id, persisted, com.top_logic.table.FilterCodec.NONE);

		TableView<Person> view = newViewOffering("age", store, id);
		assertEquals("An offered column must not appear as a newly added one.", List.of("name"),
			view.columns().stream().map(ColumnView::name).toList());
		assertEquals(List.of("name", "age"), view.columnOptions().stream().map(ColumnOption::name).toList());
	}

	public void testColumnHiddenByDefaultCanBeSelected() {
		ViewStateStore store = new MapViewStateStore();
		TableId id = new TableId("t-select-offered");

		TableView<Person> view1 = newViewOffering("age", store, id);
		view1.setColumnOrder(List.of("age", "name"));
		assertEquals(List.of("age", "name"), view1.columns().stream().map(ColumnView::name).toList());

		// The choice outlives the session, even though the column is hidden by default.
		TableView<Person> view2 = newViewOffering("age", store, id);
		assertEquals(List.of("age", "name"), view2.columns().stream().map(ColumnView::name).toList());
	}

	/** A table whose last column is an action column the user cannot decide about. */
	private TableView<Person> newViewWithActionColumn() {
		List<Column<Person, ?>> columns = new java.util.ArrayList<>(personColumns());
		columns.add(DefaultColumn.<Person, Person> builder("_delete", row -> row)
			.label(ResKey.text(""))
			.selectable(false)
			.build());
		return DefaultTableView.create(columns, new ListRowSource<>(people(), columns));
	}

	public void testActionColumnIsNoOption() {
		TableView<Person> view = newViewWithActionColumn();
		assertEquals(List.of("name", "age", "_delete"), view.columns().stream().map(ColumnView::name).toList());
		assertEquals("A column holding a button has no label to offer.", List.of("name", "age"),
			view.columnOptions().stream().map(ColumnOption::name).toList());
	}

	public void testActionColumnKeepsItsPlace() {
		TableView<Person> view = newViewWithActionColumn();
		// The column selection knows nothing about the action column, so it cannot list it.
		view.setColumnOrder(List.of("age", "name"));
		assertEquals(List.of("age", "name", "_delete"), view.columns().stream().map(ColumnView::name).toList());
		assertFalse("Hiding an action column would take away its action for good.",
			view.state().getHiddenColumns().contains("_delete"));

		// Also when the selection drops a column.
		view.setColumnOrder(List.of("name"));
		assertEquals(List.of("name", "_delete"), view.columns().stream().map(ColumnView::name).toList());
	}

	public void testTrailingActionColumnStaysLast() {
		List<Column<Person, ?>> columns = new java.util.ArrayList<>(personColumns());
		columns.add(DefaultColumn.<Person, Person> builder("extra", row -> row).selectable(true).build());
		columns.add(DefaultColumn.<Person, Person> builder("_delete", row -> row)
			.label(ResKey.text(""))
			.selectable(false)
			.build());
		TableView<Person> view = DefaultTableView.create(columns, new ListRowSource<>(people(), columns));
		view.setColumnOrder(List.of("name", "age"));
		assertEquals(List.of("name", "age", "_delete"), view.columns().stream().map(ColumnView::name).toList());

		// Switching a further column on must not push the action column to the left of it.
		view.setColumnOrder(List.of("name", "age", "extra"));
		assertEquals(List.of("name", "age", "extra", "_delete"),
			view.columns().stream().map(ColumnView::name).toList());
	}

	public void testLeadingActionColumnStaysFirst() {
		List<Column<Person, ?>> columns = new java.util.ArrayList<>();
		columns.add(DefaultColumn.<Person, Person> builder("_detail", row -> row)
			.label(ResKey.text(""))
			.selectable(false)
			.build());
		columns.addAll(personColumns());
		TableView<Person> view = DefaultTableView.create(columns, new ListRowSource<>(people(), columns));
		view.setColumnOrder(List.of("age", "name"));
		assertEquals(List.of("_detail", "age", "name"), view.columns().stream().map(ColumnView::name).toList());
	}

	public void testListenerNotifiedOnSortAndFilter() {
		TableView<Person> view = newView();
		int[] columnsChanged = {0};
		int[] rowsChanged = {0};
		view.addListener(new TableViewListener() {
			@Override
			public void columnsChanged() {
				columnsChanged[0]++;
			}

			@Override
			public void rowsChanged(int from, int to) {
				rowsChanged[0]++;
			}
		});

		view.sort(SortSpec.ascending("name"));
		view.filter("name", new Contains("a"));

		assertTrue("columnsChanged fired", columnsChanged[0] >= 2);
		assertTrue("rowsChanged fired via row source", rowsChanged[0] >= 2);
	}

	public void testPersistsAndRestoresPersonalization() {
		ViewStateStore store = new MapViewStateStore();
		TableId id = new TableId("t1");

		TableView<Person> view1 = newView(store, id);
		view1.sort(SortSpec.ascending("name"));
		view1.resizeColumn("name", 222);
		view1.moveColumn("age", 0);
		view1.setFrozenColumnCount(1);

		// A fresh view over the same store restores the personalization and re-applies the sort.
		TableView<Person> view2 = newView(store, id);
		assertEquals(List.of("age", "name"), view2.columns().stream().map(ColumnView::name).toList());
		assertEquals(222, view2.columns().stream().filter(c -> c.name().equals("name")).findFirst().get().width());
		assertEquals(1, view2.state().getFrozenCount());
		assertEquals(List.of(new SortColumn("name", true)), view2.state().getSort());
		assertEquals("restored sort re-applied to rows", List.of("Bob", "Charlie", "alice"), names(view2));
	}

	public void testRestoreDropsStaleAndAppendsNewColumns() {
		ViewStateStore store = new MapViewStateStore();
		TableId id = new TableId("t2");

		// Persist an order that references a removed column ("ghost") and omits "age".
		TableViewState stored = new TableViewState();
		stored.setColumnOrder(List.of("ghost", "name"));
		store.save(id, stored, com.top_logic.table.FilterCodec.NONE);

		TableView<Person> view = newView(store, id);
		// "ghost" dropped (no such column), "name" kept, "age" appended.
		assertEquals(List.of("name", "age"), view.columns().stream().map(ColumnView::name).toList());
	}

	public void testRestoreClampsFrozenCount() {
		ViewStateStore store = new MapViewStateStore();
		TableId id = new TableId("t3");

		TableViewState stored = new TableViewState();
		stored.setFrozenCount(9);
		store.save(id, stored, com.top_logic.table.FilterCodec.NONE);

		TableView<Person> view = newView(store, id);
		assertEquals("frozen count clamped to the available column count", 2, view.state().getFrozenCount());
	}

	public void testNoStoreLeavesDefaults() {
		// The non-persisting view keeps declaration order and zero frozen columns.
		TableView<Person> view = newView();
		assertEquals(List.of("name", "age"), view.columns().stream().map(ColumnView::name).toList());
		assertEquals(0, view.state().getFrozenCount());
	}

	public void testStateReflectsCommands() {
		TableView<Person> view = newView();
		view.sort(SortSpec.ascending("name"));
		view.setFrozenColumnCount(1);
		assertEquals(1, view.state().getSort().size());
		assertEquals("name", view.state().getSort().get(0).column());
		assertEquals(1, view.state().getFrozenCount());
		assertTrue(view.columns().get(0).frozen());
		assertFalse(view.columns().get(1).frozen());
	}

}
