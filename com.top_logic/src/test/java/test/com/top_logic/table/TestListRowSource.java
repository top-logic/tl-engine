/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.table;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import junit.framework.TestCase;

import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterSpec;
import com.top_logic.table.FilterState;
import com.top_logic.table.GroupSpec;
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

	public void testFacetCountsUsePerOptionFacetKeys() {
		// A predicate-based facet filter that buckets a name by its first letter: the bucket key is
		// not the cell value itself, exercising the generalized facet counting (one value can fall
		// into a bucket shared by several values).
		Column<Person, String> bucketed = DefaultColumn.<Person, String> builder("name", Person::name)
			.filter(new ColumnFilter<>() {
				@Override
				public FilterInput input() {
					return new FilterInput.Options(List.of());
				}

				@Override
				public Predicate<String> predicate(FilterState state) {
					return value -> true;
				}

				@Override
				public boolean countsMatches() {
					return true;
				}

				@Override
				public Collection<Object> facetKeys(String value) {
					return value == null ? List.of() : List.of("initial:" + value.substring(0, 1).toLowerCase());
				}
			})
			.build();

		ListRowSource<Person> source = new ListRowSource<>(people(), List.of(bucketed));
		com.top_logic.table.MatchCounts counts = source.matchCounts("name");
		// people(): Charlie, alice, Bob, alma -> initials a=2 (alice, alma), b=1, c=1.
		assertEquals(2, counts.count("initial:a"));
		assertEquals(1, counts.count("initial:b"));
		assertEquals(1, counts.count("initial:c"));
		assertEquals(0, counts.count("initial:z"));
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

	/**
	 * Tests that {@link RowSource#containedKeys(Collection)} answers for the data, not for the rows
	 * the filter displays: a filtered element is contained, an object that is not an element is
	 * not.
	 */
	public void testContainedKeysIgnoresTheFilter() {
		List<Person> people = people();
		RowSource<Person> source = new ListRowSource<>(people, columns(), Person::name);
		source.withFilter(new FilterSpec(java.util.Map.of("name", new Contains("Bob"))));
		assertEquals(List.of("Bob"), names(source));

		assertEquals(java.util.Set.of("Charlie", "Bob"),
			source.containedKeys(List.of("Charlie", "Bob", "nobody")));
	}

	/**
	 * Tests that {@link RowSource#matchCount()} counts the elements the filter lets pass and
	 * {@link RowSource#dataCount()} all elements, and that both follow a change of the elements.
	 */
	public void testCountsFollowFilterAndElements() {
		ListRowSource<Person> source = new ListRowSource<>(people(), columns());
		assertEquals(4, source.matchCount());
		assertEquals(4, source.dataCount());

		source.withFilter(new FilterSpec(java.util.Map.of("name", new Contains("al"))));
		assertEquals("alice and alma contain 'al'.", 2, source.matchCount());
		assertEquals("The filter does not change the data.", 4, source.dataCount());

		source.setElements(List.of(new Person("alex", 50), new Person("Bob", 40)));
		assertEquals("The filter applies to the new elements.", 1, source.matchCount());
		assertEquals(2, source.dataCount());
	}

	/**
	 * Tests that a grouped source counts its elements, not the displayed lines: neither a group
	 * header nor collapsing a group changes the count.
	 */
	public void testCountsIgnoreGroupRows() {
		ListRowSource<Person> source = new ListRowSource<>(people(), columns());
		source.withFilter(new FilterSpec(java.util.Map.of("age", new AtLeast(30))));
		source.withGrouping(new GroupSpec(List.of("age")));
		assertEquals("Charlie (30) and Bob (40), each below a header of their own.", 4, source.size());
		assertEquals(2, source.matchCount());

		source.setExpanded(source.window(0, 1).get(0).key(), false);
		assertEquals("A collapsed group keeps its header only.", 3, source.size());
		assertEquals("A row in a collapsed group is still counted.", 2, source.matchCount());
		assertEquals(4, source.dataCount());
		assertEquals("A row in a collapsed group is still a matching row.",
			java.util.Set.of("Charlie", "Bob"), java.util.Set.copyOf(names(source.matchingKeys())));
	}

	/**
	 * Tests that the counts describe the same elements when the backing list - which the source
	 * does not copy - is changed without telling the source: both stay as they were computed, and
	 * {@link ListRowSource#setElements(List)} brings both up to date.
	 */
	public void testCountsAgreeOnAChangedBackingList() {
		List<Person> people = new java.util.ArrayList<>(people());
		ListRowSource<Person> source = new ListRowSource<>(people, columns());
		people.add(new Person("dave", 50));
		assertEquals(4, source.matchCount());
		assertEquals("Not counted before the source is told about it.", 4, source.dataCount());

		source.setElements(people);
		assertEquals(5, source.matchCount());
		assertEquals(5, source.dataCount());
	}

	/**
	 * Tests that the matching keys among given keys are those of rows the filter lets pass.
	 */
	public void testMatchingKeysAmongGivenKeys() {
		ListRowSource<Person> source = new ListRowSource<>(people(), columns(), Person::name);
		source.withFilter(new FilterSpec(java.util.Map.of("name", new Contains("al"))));
		assertEquals(java.util.Set.of("alma"), source.matchingKeys(List.of("Bob", "alma", "nobody")));

		source.withFilter(new FilterSpec(java.util.Map.of("name", new Contains("B"))));
		assertEquals("The answer follows a change of the filter.",
			java.util.Set.of("Bob"), source.matchingKeys(List.of("Bob", "alma", "nobody")));
		assertEquals(List.of("Bob"), source.matchingKeys());
	}

	/** The names of the people with the given keys. */
	private static List<String> names(List<Object> keys) {
		return keys.stream().map(key -> ((Person) key).name()).toList();
	}

}
