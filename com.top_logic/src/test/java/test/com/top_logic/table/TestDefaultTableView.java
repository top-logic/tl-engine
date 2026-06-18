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

import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.ColumnView;
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterState;
import com.top_logic.table.Row;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortDirection;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableId;
import com.top_logic.table.TableView;
import com.top_logic.table.TableViewListener;
import com.top_logic.table.TableViewState;
import com.top_logic.table.ViewStateStore;
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
		public TableViewState load(TableId id) {
			java.util.Map<String, Object> json = _data.get(id.value());
			if (json == null) {
				return null;
			}
			TableViewState state = new TableViewState();
			TableViewStateCodec.readInto(state, json);
			return state;
		}

		@Override
		public void save(TableId id, TableViewState state) {
			_data.put(id.value(), TableViewStateCodec.toJson(state));
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
		assertFalse(name.editable());
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
		store.save(id, stored);

		TableView<Person> view = newView(store, id);
		// "ghost" dropped (no such column), "name" kept, "age" appended.
		assertEquals(List.of("name", "age"), view.columns().stream().map(ColumnView::name).toList());
	}

	public void testRestoreClampsFrozenCount() {
		ViewStateStore store = new MapViewStateStore();
		TableId id = new TableId("t3");

		TableViewState stored = new TableViewState();
		stored.setFrozenCount(9);
		store.save(id, stored);

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
