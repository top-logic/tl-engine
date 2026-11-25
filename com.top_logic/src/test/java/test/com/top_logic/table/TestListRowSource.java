/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.table;

import java.util.ArrayList;
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
import com.top_logic.table.GroupKey;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.Row;
import com.top_logic.table.RowKind;
import com.top_logic.table.RowSource;
import com.top_logic.table.SortColumn;
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

	/**
	 * Tests that a grouping by a column the source does not have is rejected, and that the source
	 * keeps its previous grouping.
	 */
	public void testGroupingByUnknownColumnIsRejected() {
		RowSource<Person> source = new ListRowSource<>(people(), columns());
		source.withGrouping(new GroupSpec(List.of("age")));
		int groupedSize = source.size();
		try {
			source.withGrouping(new GroupSpec(List.of("missing")));
			fail("A grouping by an unknown column must be rejected.");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("missing"));
		}
		assertEquals("The previous grouping stays in effect.", groupedSize, source.size());
	}

	/**
	 * Tests that an order by a column the source does not have is rejected, and that the source
	 * keeps its previous order.
	 */
	public void testOrderByUnknownColumnIsRejected() {
		RowSource<Person> source = new ListRowSource<>(people(), columns());
		source.withOrder(SortSpec.ascending("name"));
		try {
			source.withOrder(SortSpec.ascending("missing"));
			fail("An order by an unknown column must be rejected.");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("missing"));
		}
		source.withFilter(FilterSpec.NONE);
		assertEquals("The previous order stays in effect.", List.of("Bob", "Charlie", "alice", "alma"),
			names(source));
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

	/**
	 * Tests that {@link RowSource#matchCount(FilterSpec)} counts the elements the given filter lets
	 * pass, whatever the source is currently filtered, sorted and grouped by, and that counting
	 * leaves the displayed rows as they are.
	 */
	public void testMatchCountOfAFilterIgnoresTheCurrentState() {
		ListRowSource<Person> source = new ListRowSource<>(people(), columns());
		source.withFilter(new FilterSpec(java.util.Map.of("age", new AtLeast(30))));
		source.withOrder(SortSpec.ascending("name"));
		source.withGrouping(new GroupSpec(List.of("age")));
		List<Row<Person>> before = source.window(0, source.size());
		assertEquals("Charlie (30) and Bob (40), each below a header of their own.", 4, source.size());

		assertEquals("alice and alma contain 'al', although the current filter hides both.", 2,
			source.matchCount(new FilterSpec(java.util.Map.of("name", new Contains("al")))));
		assertEquals("Criteria of several columns combine: only alice and alma are 25, of whom "
			+ "only alma contains 'lm'.", 1,
			source.matchCount(new FilterSpec(java.util.Map.of("name", new Contains("lm"), "age", new AtLeast(25)))));
		assertEquals("The empty filter lets every element pass.", 4, source.matchCount(FilterSpec.NONE));

		assertEquals("Counting does not apply the filter.", 2, source.matchCount());
		assertEquals(4, source.size());
		assertEquals(before, source.window(0, source.size()));
	}

	/**
	 * Tests that a {@link RowSource} that does not count answers
	 * {@link RowSource#UNKNOWN_COUNT} for a given filter.
	 */
	public void testMatchCountOfAFilterDefaultsToUnknown() {
		ListRowSource<Person> counting = new ListRowSource<>(people(), columns());
		RowSource<Person> source = new RowSource<>() {
			@Override
			public int size() {
				return counting.size();
			}

			@Override
			public List<Row<Person>> window(int from, int to) {
				return counting.window(from, to);
			}

			@Override
			public java.util.Set<Object> containedKeys(Collection<?> keys) {
				return counting.containedKeys(keys);
			}

			@Override
			public RowSource<Person> withOrder(SortSpec sort) {
				return this;
			}

			@Override
			public RowSource<Person> withFilter(FilterSpec filter) {
				return this;
			}

			@Override
			public RowSource<Person> withGrouping(GroupSpec grouping) {
				return this;
			}

			@Override
			public void setExpanded(Object rowKey, boolean expanded) {
				// Nothing to expand.
			}

			@Override
			public void addListener(com.top_logic.table.RowSourceListener listener) {
				// Never changes.
			}

			@Override
			public void removeListener(com.top_logic.table.RowSourceListener listener) {
				// Never changes.
			}
		};
		assertEquals(RowSource.UNKNOWN_COUNT,
			source.matchCount(new FilterSpec(java.util.Map.of("name", new Contains("al")))));
	}

	/** The names of the people with the given keys. */
	private static List<String> names(List<Object> keys) {
		return keys.stream().map(key -> ((Person) key).name()).toList();
	}

	/** A sale of a product in a region, the fixture of the nested grouping tests. */
	private record Sale(String region, String product, int amount) {
		// Test fixture.
	}

	/**
	 * Region, product and amount columns, all sortable unless the product is requested without.
	 */
	private static List<Column<Sale, ?>> saleColumns(boolean sortableProduct) {
		DefaultColumn.Builder<Sale, String> product = DefaultColumn.<Sale, String> builder("product", Sale::product);
		if (sortableProduct) {
			product.sort(() -> Comparator.<String> naturalOrder());
		}
		return List.of(
			DefaultColumn.<Sale, String> builder("region", Sale::region)
				.sort(() -> Comparator.<String> naturalOrder())
				.build(),
			product.build(),
			DefaultColumn.<Sale, Integer> builder("amount", Sale::amount)
				.sort(() -> Comparator.<Integer> naturalOrder())
				.build());
	}

	private static List<Sale> sales() {
		return List.of(
			new Sale("North", "B", 1),
			new Sale("South", "A", 2),
			new Sale("North", "A", 3),
			new Sale("North", "B", 4),
			new Sale("South", "A", 5),
			new Sale("South", "C", 6));
	}

	/** A source over {@link #sales()} grouped by region and product. */
	private static ListRowSource<Sale> groupedSales() {
		ListRowSource<Sale> source = new ListRowSource<>(sales(), saleColumns(true));
		source.withGrouping(new GroupSpec(List.of("region", "product")));
		return source;
	}

	/**
	 * The given rows as text: a group header as its depth and the values of its key, a data row
	 * as its depth and amount.
	 */
	private static List<String> describe(List<Row<Sale>> rows) {
		List<String> result = new ArrayList<>();
		for (Row<Sale> row : rows) {
			if (row.kind() == RowKind.GROUP_HEADER) {
				result.add(row.depth() + ":" + String.join("/", row.group().key().values().stream()
					.map(String::valueOf).toList()) + "(" + row.group().size() + ")");
			} else {
				result.add(row.depth() + ":" + row.data().amount());
			}
		}
		return result;
	}

	private static List<String> describe(ListRowSource<Sale> source) {
		return describe(source.window(0, source.size()));
	}

	/**
	 * The key of the group with the given values.
	 */
	private static GroupKey groupKey(String... values) {
		return new GroupKey(List.of((Object[]) values));
	}

	/**
	 * Tests that grouping by two columns nests a level of groups per column: each header has the
	 * depth of its level, the path of values down to its level as key and the number of its rows,
	 * and the data rows follow the innermost headers.
	 */
	public void testTwoLevelGrouping() {
		ListRowSource<Sale> source = groupedSales();
		assertEquals(List.of(
			"0:North(3)", "1:North/A(1)", "2:3", "1:North/B(2)", "2:1", "2:4",
			"0:South(3)", "1:South/A(2)", "2:2", "2:5", "1:South/C(1)", "2:6"),
			describe(source));
		assertEquals("Group headers are no matches.", 6, source.matchCount());
	}

	/**
	 * Tests that collapsing an outer group hides the headers and rows of its nested groups, and
	 * that expanding it again shows the nested groups in the state they had.
	 */
	public void testCollapseOuterGroupKeepsStateOfInnerGroups() {
		ListRowSource<Sale> source = groupedSales();
		source.setExpanded(groupKey("North", "B"), false);
		assertEquals(List.of(
			"0:North(3)", "1:North/A(1)", "2:3", "1:North/B(2)",
			"0:South(3)", "1:South/A(2)", "2:2", "2:5", "1:South/C(1)", "2:6"),
			describe(source));

		source.setExpanded(groupKey("North"), false);
		assertEquals(List.of(
			"0:North(3)",
			"0:South(3)", "1:South/A(2)", "2:2", "2:5", "1:South/C(1)", "2:6"),
			describe(source));
		assertFalse(source.window(0, 1).get(0).expanded());
		assertEquals("Rows of a collapsed group are still matches.", 6, source.matchCount());
		assertEquals(6, source.matchingKeys().size());

		source.setExpanded(groupKey("North"), true);
		assertEquals(List.of(
			"0:North(3)", "1:North/A(1)", "2:3", "1:North/B(2)",
			"0:South(3)", "1:South/A(2)", "2:2", "2:5", "1:South/C(1)", "2:6"),
			describe(source));
		assertFalse("The inner group stays collapsed.", source.window(3, 4).get(0).expanded());
	}

	/**
	 * Tests that all rows of a nested grouping contain the content of collapsed groups at every
	 * level.
	 */
	public void testAllRowsOfNestedGroupingIncludeCollapsedGroups() {
		ListRowSource<Sale> source = groupedSales();
		source.setExpanded(groupKey("North", "B"), false);
		source.setExpanded(groupKey("South"), false);
		assertEquals(List.of(
			"0:North(3)", "1:North/A(1)", "2:3", "1:North/B(2)", "2:1", "2:4",
			"0:South(3)", "1:South/A(2)", "2:2", "2:5", "1:South/C(1)", "2:6"),
			describe(source.allRows()));
	}

	/**
	 * Tests that each level orders its groups by its own column, in the direction the sort gives
	 * that column, while the sort orders the rows within the innermost groups.
	 */
	public void testNestedGroupOrderFollowsEachLevelsColumn() {
		ListRowSource<Sale> source = groupedSales();
		source.withOrder(new SortSpec(List.of(
			new SortColumn("product", false),
			new SortColumn("amount", false))));
		assertEquals(List.of(
			"0:North(3)", "1:North/B(2)", "2:4", "2:1", "1:North/A(1)", "2:3",
			"0:South(3)", "1:South/C(1)", "2:6", "1:South/A(2)", "2:5", "2:2"),
			describe(source));
	}

	/**
	 * Tests that the groups of a nested grouping column without sort capability come in the order
	 * in which their first rows appear within the enclosing group in the backing list, whatever
	 * the rows are sorted by.
	 */
	public void testUnsortableInnerColumnKeepsFirstAppearanceWithinItsGroup() {
		List<Sale> sales = List.of(
			new Sale("North", "B", 1),
			new Sale("South", "A", 2),
			new Sale("North", "A", 9),
			new Sale("North", "B", 4),
			new Sale("South", "A", 5),
			new Sale("South", "B", 7));
		ListRowSource<Sale> source = new ListRowSource<>(sales, saleColumns(false));
		source.withGrouping(new GroupSpec(List.of("region", "product")));
		source.withOrder(new SortSpec(List.of(new SortColumn("amount", false))));
		assertEquals(List.of(
			"0:North(3)", "1:North/B(2)", "2:4", "2:1", "1:North/A(1)", "2:9",
			"0:South(3)", "1:South/A(2)", "2:5", "2:2", "1:South/B(1)", "2:7"),
			describe(source));
	}

}
