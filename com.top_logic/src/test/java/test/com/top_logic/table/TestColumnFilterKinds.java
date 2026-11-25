/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.table;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import junit.framework.TestCase;

import com.top_logic.basic.util.ResKey;
import com.top_logic.table.CellContent;
import com.top_logic.table.CellRenderer;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.ColumnFilterState;
import com.top_logic.table.FilterCodec;
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterSpec;
import com.top_logic.table.FilterState;
import com.top_logic.table.MatchCounts;
import com.top_logic.table.Option;
import com.top_logic.table.TableId;
import com.top_logic.table.TableView;
import com.top_logic.table.TableViewState;
import com.top_logic.table.ViewStateStore;
import com.top_logic.table.filter.ColumnFilterEditor;
import com.top_logic.table.filter.ComparableColumnFilter;
import com.top_logic.table.filter.ComparisonOperator;
import com.top_logic.table.filter.FilterEditors;
import com.top_logic.table.filter.OptionsColumnFilter;
import com.top_logic.table.filter.OptionsFilterState;
import com.top_logic.table.filter.RangeFilterState;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.filter.TextFilterState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.table.impl.TableViewStateCodec;

/**
 * Test for columns offering several {@link Column#filters() kinds of filter}: the active kind of
 * a {@link ColumnFilterState}, its inversion, its facet counts, its editor and its persistence.
 */
public class TestColumnFilterKinds extends TestCase {

	private static final String CITY = "city";

	private static final String AGE = "age";

	/** Kind of the {@link #initials() initials filter}. */
	private static final String KIND_INITIAL = "initial";

	private record Person(String name, String city, Integer age) {
		// Test fixture.
	}

	/** An in-memory {@link ViewStateStore} routing through the JSON codec, like the real store. */
	private static final class MapViewStateStore implements ViewStateStore {
		final Map<String, Map<String, Object>> _data = new HashMap<>();

		@Override
		public TableViewState load(TableId id, FilterCodec filters) {
			Map<String, Object> json = _data.get(id.value());
			if (json == null) {
				return null;
			}
			TableViewState state = new TableViewState();
			TableViewStateCodec.readInto(state, json, filters);
			return state;
		}

		@Override
		public void save(TableId id, TableViewState state, FilterCodec filters) {
			_data.put(id.value(), TableViewStateCodec.toJson(state, filters));
		}
	}

	private static List<Person> people() {
		return List.of(
			new Person("Anna", "Bern", Integer.valueOf(30)),
			new Person("Ben", "Hamburg", Integer.valueOf(40)),
			new Person("Carl", "Berlin", null),
			new Person("Dora", null, Integer.valueOf(30)));
	}

	private static OptionsColumnFilter<String> cityValues() {
		return new OptionsColumnFilter<>(List.of(
			new Option("Bern", ResKey.text("Bern")),
			new Option("Hamburg", ResKey.text("Hamburg")),
			new Option("Berlin", ResKey.text("Berlin"))));
	}

	/**
	 * A second options-like kind, bucketing a city by its initial, with a kind of its own.
	 */
	private static ColumnFilter<String> initials() {
		return new ColumnFilter<>() {
			@Override
			public String kind() {
				return KIND_INITIAL;
			}

			@Override
			public FilterInput input() {
				return new FilterInput.Options(List.of());
			}

			@Override
			public Predicate<String> predicate(FilterState state) {
				Set<Object> selected = ((OptionsFilterState) state).selected();
				return value -> value != null && selected.contains(value.substring(0, 1));
			}

			@Override
			public boolean countsMatches() {
				return true;
			}

			@Override
			public Collection<Object> facetKeys(String value) {
				return value == null ? List.of() : List.of(value.substring(0, 1));
			}
		};
	}

	private static List<Column<Person, ?>> columns() {
		Column<Person, String> city = DefaultColumn.<Person, String> builder(CITY, Person::city)
			.filters(List.of(cityValues(), TextColumnFilter.forStrings(), initials()))
			.build();
		Column<Person, Integer> age = DefaultColumn.<Person, Integer> builder(AGE, Person::age)
			.sort(() -> Comparator.naturalOrder())
			.filters(List.of(ComparableColumnFilter.integers(), new OptionsColumnFilter<>(List.of(
				new Option(Integer.valueOf(30), ResKey.text("30")),
				new Option(Integer.valueOf(40), ResKey.text("40"))))))
			.build();
		return List.of(city, age);
	}

	private static TableView<Person> newView() {
		List<Column<Person, ?>> columns = columns();
		return DefaultTableView.create(columns, new ListRowSource<>(people(), columns));
	}

	private static TableView<Person> newView(ViewStateStore store, TableId id) {
		List<Column<Person, ?>> columns = columns();
		return DefaultTableView.create(columns, new ListRowSource<>(people(), columns), store, id);
	}

	private static List<String> names(TableView<Person> view) {
		return view.rows(0, view.rowCount()).stream().map(r -> r.data().name()).toList();
	}

	/** The column offers its kinds in the declared order, the first one being its {@link Column#filter()}. */
	public void testKinds() {
		Column<Person, ?> city = columns().get(0);
		assertEquals(List.of(ColumnFilter.KIND_VALUES, ColumnFilter.KIND_TEXT, KIND_INITIAL),
			city.filters().stream().map(ColumnFilter::kind).toList());
		assertSame(city.filters().get(0), city.filter().get());
		assertSame(city.filters().get(1), city.filter(ColumnFilter.KIND_TEXT).get());
		assertSame(city.filters().get(0), city.filter((String) null).get());
		assertTrue(city.filter("unknown").isEmpty());
	}

	/**
	 * A column implementing only {@link Column#filter()} offers that filter as its single kind,
	 * inversion included.
	 */
	public void testColumnOverridingOnlyFilter() {
		ColumnFilter<String> text = TextColumnFilter.forStrings();
		Column<Person, String> custom = new Column<>() {
			@Override
			public String name() {
				return CITY;
			}

			@Override
			public ResKey label() {
				return ResKey.text(CITY);
			}

			@Override
			public String value(Person row) {
				return row.city();
			}

			@Override
			public CellRenderer<String> renderer() {
				return value -> CellContent.text(String.valueOf(value));
			}

			@Override
			public Optional<ColumnFilter<String>> filter() {
				return Optional.of(text);
			}
		};
		assertEquals(List.of(text), custom.filters());
		assertSame(text, custom.filter(ColumnFilter.KIND_TEXT).get());

		List<Column<Person, ?>> columns = List.of(custom);
		TableView<Person> view = DefaultTableView.create(columns, new ListRowSource<>(people(), columns));
		assertTrue(view.columns().get(0).filterable());

		view.filter(CITY, TextFilterState.contains("ber"));
		assertEquals(List.of("Anna", "Carl"), names(view));

		view.filter(CITY, ColumnFilterState.invert(TextFilterState.contains("ber")));
		assertEquals(List.of("Ben", "Dora"), names(view));
	}

	/** Two kinds of filter with the same identifier are rejected. */
	public void testDuplicateKindRejected() {
		try {
			DefaultColumn.<Person, String> builder(CITY, Person::city)
				.filters(List.of(TextColumnFilter.forStrings(), TextColumnFilter.forStrings()));
			fail("Duplicate kinds must be rejected.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
	}

	/** A plain state is one of the first kind. */
	public void testPlainStateIsFirstKind() {
		TableView<Person> view = newView();
		view.filter(CITY, new OptionsFilterState(Set.of("Bern")));
		assertEquals(List.of("Anna"), names(view));
	}

	/** A {@link ColumnFilterState} switches to another kind. */
	public void testSwitchKind() {
		TableView<Person> view = newView();
		view.filter(CITY, new ColumnFilterState(ColumnFilter.KIND_TEXT, TextFilterState.contains("ber"), false));
		assertEquals(List.of("Anna", "Carl"), names(view));

		view.filter(CITY, new ColumnFilterState(KIND_INITIAL, new OptionsFilterState(Set.of("H")), false));
		assertEquals(List.of("Ben"), names(view));
	}

	/** Every kind can be inverted, the empty cells then match. */
	public void testInvertEveryKind() {
		TableView<Person> view = newView();
		view.filter(CITY, ColumnFilterState.invert(new OptionsFilterState(Set.of("Bern"))));
		assertEquals(List.of("Ben", "Carl", "Dora"), names(view));

		view.filter(CITY, new ColumnFilterState(ColumnFilter.KIND_TEXT, TextFilterState.contains("ber"), true));
		assertEquals(List.of("Ben", "Dora"), names(view));

		view.filter(CITY, new ColumnFilterState(KIND_INITIAL, new OptionsFilterState(Set.of("B")), true));
		assertEquals(List.of("Ben", "Dora"), names(view));

		view.filter(CITY, null);
		view.filter(AGE, ColumnFilterState.invert(RangeFilterState.of(ComparisonOperator.GE, Integer.valueOf(35))));
		assertEquals(List.of("Anna", "Carl", "Dora"), names(view));

		view.filter(AGE, new ColumnFilterState(ColumnFilter.KIND_VALUES,
			new OptionsFilterState(Set.of(Integer.valueOf(30))), true));
		assertEquals(List.of("Ben", "Carl"), names(view));
	}

	/** The inverted "not equal" selects the cells "equal" selects, no empty cell (#7456). */
	public void testInvertedNotEqualIsEqual() {
		TableView<Person> view = newView();
		view.filter(AGE, RangeFilterState.of(ComparisonOperator.EQ, Integer.valueOf(30)));
		List<String> equal = names(view);
		assertEquals(List.of("Anna", "Dora"), equal);

		view.filter(AGE, ColumnFilterState.invert(RangeFilterState.of(ComparisonOperator.NE, Integer.valueOf(30))));
		assertEquals(equal, names(view));

		view.filter(AGE, RangeFilterState.of(ComparisonOperator.NE, Integer.valueOf(30)));
		assertEquals("An empty cell is not equal to any value.", List.of("Ben", "Carl"), names(view));
	}

	/** Inverting without a selection leaves the column unfiltered. */
	public void testInvertEmptyIsNoFilter() {
		assertTrue(ColumnFilterState.invert(new OptionsFilterState(Set.of())).isEmpty());
	}

	/** The facet counts are those of the active kind, or of the requested one. */
	public void testCountsOfActiveKind() {
		List<Column<Person, ?>> columns = columns();
		ListRowSource<Person> source = new ListRowSource<>(people(), columns);

		MatchCounts values = source.matchCounts(CITY);
		assertEquals("Unfiltered: counts of the first kind.", 1, values.count("Bern"));
		assertEquals(0, values.count("B"));

		source.withFilter(new FilterSpec(
			Map.of(CITY, new ColumnFilterState(KIND_INITIAL, new OptionsFilterState(Set.of("H")), false))));
		MatchCounts initials = source.matchCounts(CITY);
		assertEquals("Counts of the active kind.", 2, initials.count("B"));
		assertEquals(1, initials.count("H"));
		assertEquals(0, initials.count("Bern"));

		assertEquals("Counts of a requested kind.", 1, source.matchCounts(CITY, ColumnFilter.KIND_VALUES).count("Bern"));
		assertEquals(2, source.matchCounts(CITY, KIND_INITIAL).count("B"));
	}

	/** The canonical form of a column state. */
	public void testCanonicalForm() {
		List<ColumnFilter<String>> kinds = List.of(cityValues(), TextColumnFilter.forStrings());
		FilterState values = new OptionsFilterState(Set.of("Bern"));
		assertSame(values, ColumnFilterState.of(kinds, ColumnFilter.KIND_VALUES, values, false));
		assertSame(values, ColumnFilterState.of(kinds, null, values, false));
		assertEquals(ColumnFilterState.invert(values), ColumnFilterState.of(kinds, ColumnFilter.KIND_VALUES, values, true));

		FilterState text = TextFilterState.contains("x");
		assertEquals(new ColumnFilterState(ColumnFilter.KIND_TEXT, text, false),
			ColumnFilterState.of(kinds, ColumnFilter.KIND_TEXT, text, false));
	}

	/** The editor of a column switches between its kinds and reads the active one. */
	public void testEditorSwitchesKind() {
		Column<Person, ?> city = columns().get(0);
		ColumnFilterEditor editor =
			FilterEditors.create(city.filters(), new OptionsFilterState(Set.of("Bern")), kind -> MatchCounts.NONE);
		assertEquals(ColumnFilter.KIND_VALUES, editor.activeKind());
		assertEquals(new OptionsFilterState(Set.of("Bern")), editor.read());

		editor.setActiveKind(ColumnFilter.KIND_TEXT);
		assertTrue("A kind not yet edited starts empty.", editor.read().isEmpty());
		editor.fields().get(0).model().setValue("ber");
		// Pattern, search mode, case sensitive, invert.
		editor.fields().get(3).model().setValue(Boolean.TRUE);
		assertEquals(new ColumnFilterState(ColumnFilter.KIND_TEXT, TextFilterState.contains("ber"), true),
			editor.read());

		editor.setActiveKind(ColumnFilter.KIND_VALUES);
		assertEquals("Switching back restores the input of that kind.",
			ColumnFilterState.invert(new OptionsFilterState(Set.of("Bern"))), editor.read());
	}

	/** The editor is seeded with the kind and inversion of the current state. */
	public void testEditorSeededFromKindState() {
		Column<Person, ?> city = columns().get(0);
		ColumnFilterEditor editor = FilterEditors.create(city.filters(),
			new ColumnFilterState(ColumnFilter.KIND_TEXT, TextFilterState.contains("ham"), true), kind -> MatchCounts.NONE);
		assertEquals(ColumnFilter.KIND_TEXT, editor.activeKind());
		assertEquals("ham", editor.fields().get(0).model().getValue());
		assertEquals(Boolean.TRUE, editor.fields().get(3).model().getValue());
	}

	/** A state of another kind than the first is persisted with its kind and restored. */
	public void testKindPersisted() {
		MapViewStateStore store = new MapViewStateStore();
		TableId id = new TableId("t-kinds");
		ColumnFilterState state = new ColumnFilterState(ColumnFilter.KIND_TEXT, TextFilterState.contains("ber"), true);
		newView(store, id).filter(CITY, state);

		TableView<Person> restored = newView(store, id);
		assertEquals(state, restored.state().getFilters().get(CITY));
		assertEquals(List.of("Ben", "Dora"), names(restored));
	}

	/** A stored state without kind is a state of the first kind. */
	public void testStateWithoutKindRead() {
		MapViewStateStore store = new MapViewStateStore();
		TableId id = new TableId("t-plain");
		Map<String, Object> cityState = new LinkedHashMap<>();
		cityState.put("state", List.of(Integer.valueOf(1)));
		cityState.put("inverted", Boolean.TRUE);
		Map<String, Object> json = new LinkedHashMap<>();
		json.put("filters", Map.of(CITY, cityState));
		store._data.put(id.value(), json);

		TableView<Person> restored = newView(store, id);
		assertEquals(ColumnFilterState.invert(new OptionsFilterState(Set.of("Hamburg"))),
			restored.state().getFilters().get(CITY));
		assertEquals(List.of("Anna", "Carl", "Dora"), names(restored));
	}

	/** A stored state of a kind the column does not offer is dropped. */
	public void testUnknownKindDropped() {
		MapViewStateStore store = new MapViewStateStore();
		TableId id = new TableId("t-unknown");
		Map<String, Object> cityState = new LinkedHashMap<>();
		cityState.put("kind", "unknown");
		cityState.put("state", List.of(Integer.valueOf(1)));
		Map<String, Object> json = new LinkedHashMap<>();
		json.put("filters", Map.of(CITY, cityState));
		store._data.put(id.value(), json);

		TableView<Person> restored = newView(store, id);
		assertNull(restored.state().getFilters().get(CITY));
		assertEquals(4, restored.rowCount());
	}

}
