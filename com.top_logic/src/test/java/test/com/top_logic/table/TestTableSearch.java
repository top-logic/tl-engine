/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.FilterSpec;
import com.top_logic.table.FilterState;
import com.top_logic.table.MatchCounts;
import com.top_logic.table.Row;
import com.top_logic.table.RowSource;
import com.top_logic.table.SearchSpec;
import com.top_logic.table.TreeStructure;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.filter.TextFilterState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.table.impl.TreeRowSource;

/**
 * Test for the cross-column free-text {@link SearchSpec} of a {@link FilterSpec}: matching
 * the rendered cell text of the searched columns, combining with column filters, and its
 * effect on facet counts.
 */
public class TestTableSearch extends TestCase {

	private record Person(String name, String city, int age) {
		// Test fixture.
	}

	private final Column<Person, String> _name =
		DefaultColumn.<Person, String> builder("name", Person::name)
			.filter(TextColumnFilter.forStrings())
			.build();

	private final Column<Person, String> _city =
		DefaultColumn.<Person, String> builder("city", Person::city)
			.filter(TextColumnFilter.forStrings())
			.build();

	private final Column<Person, Integer> _age =
		DefaultColumn.<Person, Integer> builder("age", Person::age)
			.build();

	/** A column holding the city but rendering an empty cell, so it carries no searchable text. */
	private final Column<Person, String> _blank =
		DefaultColumn.<Person, String> builder("blank", Person::city)
			.renderer(value -> CellContent.empty())
			.build();

	private List<Column<Person, ?>> columns() {
		return List.of(_name, _city, _age, _blank);
	}

	private List<Person> people() {
		return List.of(
			new Person("Alice", "Berlin", 30),
			new Person("Bob", "Bern", 40),
			new Person("Carol", "Bergen", 25),
			new Person("dave", "Munich", 30));
	}

	private ListRowSource<Person> source() {
		return new ListRowSource<>(people(), columns());
	}

	private List<String> names(RowSource<Person> source) {
		List<Row<Person>> rows = source.window(0, source.size());
		return rows.stream().map(row -> row.data().name()).toList();
	}

	private List<String> search(SearchSpec search) {
		ListRowSource<Person> source = source();
		source.withFilter(new FilterSpec(Map.of(), search));
		return names(source);
	}

	public void testSearchNarrowsRows() {
		assertEquals(List.of("Alice"), search(SearchSpec.contains("Alice", "name")));
	}

	public void testSearchFindsSubstringCaseInsensitively() {
		assertEquals("Matches inside the text, ignoring case.",
			List.of("Carol"), search(SearchSpec.contains("ARO", "name")));
	}

	public void testSearchMatchesAnySearchedColumn() {
		// "be" occurs in the cities Berlin, Bern and Bergen, in no name.
		assertEquals(List.of("Alice", "Bob", "Carol"), search(SearchSpec.contains("be", "name", "city")));
	}

	public void testOnlyNamedColumnsAreSearched() {
		assertEquals("'Munich' occurs in the city column only, which is not searched.",
			List.of(), search(SearchSpec.contains("Munich", "name")));
		assertEquals(List.of("dave"), search(SearchSpec.contains("Munich", "city")));
	}

	public void testSearchAndColumnFilterIntersect() {
		ListRowSource<Person> source = source();
		// Column filter: name contains "a" -> Alice, Carol, dave. Search: "be" -> Alice, Bob, Carol.
		source.withFilter(new FilterSpec(
			Map.<String, FilterState> of("name", TextFilterState.contains("a")),
			SearchSpec.contains("be", "city")));
		assertEquals("Intersection of the column filter and the search, not their union.",
			List.of("Alice", "Carol"), names(source));
	}

	public void testEmptySearchMatchesEverything() {
		assertEquals(4, search(SearchSpec.NONE).size());
		assertEquals("An empty pattern is no search.", 4, search(SearchSpec.contains("", "name")).size());
		assertEquals("A search without a column is no search.",
			4, search(SearchSpec.contains("Alice", List.of())).size());
		assertEquals(4, search(new SearchSpec(null, List.of("name"))).size());
	}

	public void testUnknownSearchColumnIsIgnored() {
		assertEquals("A column the table does not have is skipped, the rest still searched.",
			List.of("dave"), search(SearchSpec.contains("Munich", "stale", "city")));
		assertEquals("A search with no known column at all leaves the rows untouched.",
			4, search(SearchSpec.contains("Munich", "stale")).size());
	}

	public void testColumnWithoutCellTextNeverMatches() {
		assertEquals("The blank column renders no text, so its value is not searchable.",
			List.of(), search(SearchSpec.contains("Berlin", "blank")));
		assertEquals("The same value is found in a column that renders it.",
			List.of("Alice"), search(SearchSpec.contains("Berlin", "city")));
	}

	public void testSearchUsesTheTextFilterFlags() {
		assertEquals("Regular-expression pattern.",
			List.of("Alice"), search(new SearchSpec(new TextFilterState("^Ali.*e$", false, true, false),
				List.of("name"))));
		assertEquals("Whole-field matching: 'Ber' is no complete city.",
			List.of(), search(new SearchSpec(new TextFilterState("Ber", false, false, true), List.of("city"))));
		assertEquals("Whole-field matching: 'Bern' is.",
			List.of("Bob"), search(new SearchSpec(new TextFilterState("Bern", false, false, true), List.of("city"))));
		assertEquals("Case-sensitive matching.",
			List.of(), search(new SearchSpec(new TextFilterState("alice", true, false, false), List.of("name"))));
	}

	public void testFacetCountsReflectSearch() {
		ListRowSource<Person> source = source();
		// Searched rows: Alice (30), Bob (40), Carol (25); dave (Munich, 30) is dropped.
		source.withFilter(new FilterSpec(Map.of(), SearchSpec.contains("be", "city")));

		MatchCounts ageFacets = source.matchCounts("age");
		assertTrue(ageFacets.isAvailable());
		assertEquals("Only Alice is left with age 30, dave is searched away.",
			1, ageFacets.count(Integer.valueOf(30)));
		assertEquals(1, ageFacets.count(Integer.valueOf(40)));
		assertEquals(1, ageFacets.count(Integer.valueOf(25)));
	}

	public void testFacetCountsKeepSearchOfTheOwnColumn() {
		ListRowSource<Person> source = source();
		source.withFilter(new FilterSpec(
			Map.<String, FilterState> of("city", TextFilterState.contains("Berlin")),
			SearchSpec.contains("be", "city")));
		assertEquals(List.of("Alice"), names(source));

		// The city column's own filter is left out of its facets, but the search still applies.
		MatchCounts cityFacets = source.matchCounts("city");
		assertEquals(1, cityFacets.count("Berlin"));
		assertEquals(1, cityFacets.count("Bern"));
		assertEquals(1, cityFacets.count("Bergen"));
		assertEquals("Munich is excluded by the search, which facets keep.", 0, cityFacets.count("Munich"));
	}

	/** A tree node with a name and children. */
	private static final class Node {
		final String _name;

		final List<Node> _children = new ArrayList<>();

		Node(String name, Node... children) {
			_name = name;
			for (Node child : children) {
				_children.add(child);
			}
		}
	}

	public void testSearchInTree() {
		Node deep = new Node("needle");
		Node root = new Node("root", new Node("other"), new Node("branch", deep));
		Node second = new Node("second");
		TreeStructure<Node, Node> structure = new TreeStructure<>() {
			@Override
			public List<Node> roots() {
				return List.of(root, second);
			}

			@Override
			public List<Node> children(Node node) {
				return node._children;
			}

			@Override
			public boolean isLeaf(Node node) {
				return node._children.isEmpty();
			}

			@Override
			public Node businessObject(Node node) {
				return node;
			}
		};
		Column<Node, String> name = DefaultColumn.<Node, String> builder("name", node -> node._name).build();

		TreeRowSource<Node, Node> source = new TreeRowSource<>(structure, List.of(name));
		source.withFilter(new FilterSpec(Map.of(), SearchSpec.contains("NEEDLE", "name")));
		List<String> displayed =
			source.window(0, source.size()).stream().map(row -> row.data()._name).toList();
		assertEquals("The search reveals the deep match and keeps its ancestors.",
			List.of("root", "branch", "needle"), displayed);
	}

}
