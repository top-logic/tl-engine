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

import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterSpec;
import com.top_logic.table.FilterState;
import com.top_logic.table.Row;
import com.top_logic.table.RowSource;
import com.top_logic.table.SortSpec;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.ListRowSource;

/**
 * Test for {@link ListRowSource}: in-memory sort and filter over typed columns.
 */
public class TestListRowSource extends TestCase {

	private record Person(String name, int age) {
		// Test fixture.
	}

	/** Filter state for a case-sensitive "contains" text filter. */
	private record Contains(String needle) implements FilterState {
		@Override
		public boolean isEmpty() {
			return needle.isEmpty();
		}
	}

	/** Filter state accepting ages greater than or equal to a bound. */
	private record AtLeast(int min) implements FilterState {
		@Override
		public boolean isEmpty() {
			return false;
		}
	}

	private final Column<Person, String> _name =
		DefaultColumn.<Person, String> builder("name", Person::name)
			.sort(() -> Comparator.naturalOrder())
			.filter(new ColumnFilter<>() {
				@Override
				public FilterInput input() {
					return new FilterInput.Text();
				}

				@Override
				public Predicate<String> predicate(FilterState state) {
					String needle = ((Contains) state).needle();
					return value -> value != null && value.contains(needle);
				}
			})
			.build();

	private final Column<Person, Integer> _age =
		DefaultColumn.<Person, Integer> builder("age", Person::age)
			.sort(() -> Comparator.naturalOrder())
			.filter(new ColumnFilter<>() {
				@Override
				public FilterInput input() {
					return new FilterInput.Range();
				}

				@Override
				public Predicate<Integer> predicate(FilterState state) {
					int min = ((AtLeast) state).min();
					return value -> value != null && value.intValue() >= min;
				}
			})
			.build();

	private List<Column<Person, ?>> columns() {
		return List.of(_name, _age);
	}

	private List<Person> people() {
		return List.of(
			new Person("Charlie", 30),
			new Person("alice", 25),
			new Person("Bob", 40),
			new Person("alma", 25));
	}

	private List<String> names(RowSource<Person> source) {
		List<Row<Person>> rows = source.window(0, source.size());
		return rows.stream().map(r -> r.data().name()).toList();
	}

	public void testUnsortedKeepsInsertionOrder() {
		RowSource<Person> source = new ListRowSource<>(people(), columns());
		assertEquals(4, source.size());
		assertEquals(List.of("Charlie", "alice", "Bob", "alma"), names(source));
	}

	public void testSortByNameAscending() {
		RowSource<Person> source = new ListRowSource<>(people(), columns());
		source.withOrder(SortSpec.ascending("name"));
		assertEquals(List.of("Bob", "Charlie", "alice", "alma"), names(source));
	}

	public void testSortByAgeDescendingThenName() {
		RowSource<Person> source = new ListRowSource<>(people(), columns());
		source.withOrder(new SortSpec(List.of(
			new com.top_logic.table.SortColumn("age", false),
			new com.top_logic.table.SortColumn("name", true))));
		// age desc: 40 (Bob), then 30 (Charlie), then 25 (alice, alma) by name asc.
		assertEquals(List.of("Bob", "Charlie", "alice", "alma"), names(source));
	}

	public void testFacetCountsReflectOtherFiltersButNotOwn() {
		ListRowSource<Person> source = new ListRowSource<>(people(), columns());
		// Two active filters: name contains "li" (Charlie, alice) and age >= 30 (Charlie, Bob).
		source.withFilter(new FilterSpec(java.util.Map.of("name", new Contains("li"), "age", new AtLeast(30))));
		// Displayed = intersection = Charlie only.
		assertEquals(List.of("Charlie"), names(source));

		// Facets on age exclude age's own filter but keep the name filter, so they count over
		// {Charlie(30), alice(25)} — age 25 still counted, ages outside the name match excluded.
		com.top_logic.table.MatchCounts ageFacets = source.matchCounts("age");
		assertTrue(ageFacets.isAvailable());
		assertEquals(1, ageFacets.count(Integer.valueOf(30)));
		assertEquals(1, ageFacets.count(Integer.valueOf(25)));
		assertEquals("Bob (age 40) is excluded by the name filter.", 0, ageFacets.count(Integer.valueOf(40)));
	}

	public void testFilterContains() {
		RowSource<Person> source = new ListRowSource<>(people(), columns());
		source.withFilter(new FilterSpec(java.util.Map.of("name", new Contains("al"))));
		assertEquals(List.of("alice", "alma"), names(source));
	}

	public void testFilterThenSort() {
		RowSource<Person> source = new ListRowSource<>(people(), columns());
		source.withFilter(new FilterSpec(java.util.Map.of("name", new Contains("a"))));
		source.withOrder(SortSpec.ascending("name"));
		// "Charlie", "alice", "alma" contain 'a'; "Bob" does not. Sorted: Charlie, alice, alma.
		assertEquals(List.of("Charlie", "alice", "alma"), names(source));
	}

	public void testEmptyFilterMatchesAll() {
		RowSource<Person> source = new ListRowSource<>(people(), columns());
		source.withFilter(new FilterSpec(java.util.Map.of("name", new Contains(""))));
		assertEquals(4, source.size());
	}

}
