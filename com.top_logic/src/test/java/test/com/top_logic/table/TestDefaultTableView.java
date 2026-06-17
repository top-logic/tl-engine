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
import com.top_logic.table.SortDirection;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableView;
import com.top_logic.table.TableViewListener;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

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

	private TableView<Person> newView() {
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

		List<Column<Person, ?>> columns = List.of(name, age);
		List<Person> people = List.of(
			new Person("Charlie", 30),
			new Person("alice", 25),
			new Person("Bob", 40));
		ListRowSource<Person> source = new ListRowSource<>(people, columns);
		return DefaultTableView.create(columns, source);
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
