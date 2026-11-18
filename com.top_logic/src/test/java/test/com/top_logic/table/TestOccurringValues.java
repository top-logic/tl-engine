/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import junit.framework.TestCase;

import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.ColumnFilterState;
import com.top_logic.table.FilterCodec;
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterSpec;
import com.top_logic.table.FilterState;
import com.top_logic.table.MatchCounts;
import com.top_logic.table.NamedFilter;
import com.top_logic.table.NamedFilterStore;
import com.top_logic.table.Option;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableId;
import com.top_logic.table.TableView;
import com.top_logic.table.TableViewState;
import com.top_logic.table.ViewStateStore;
import com.top_logic.table.filter.BoundCodec;
import com.top_logic.table.filter.ColumnFilterEditor;
import com.top_logic.table.filter.FilterEditor;
import com.top_logic.table.filter.FilterEditors;
import com.top_logic.table.filter.OptionsColumnFilter;
import com.top_logic.table.filter.OptionsFilterState;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.filter.TextFilterState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.table.impl.NamedFilterCodec;
import com.top_logic.table.impl.TableViewStateCodec;

/**
 * Test for an {@link OptionsColumnFilter} offering the values {@link OptionsColumnFilter#occurring
 * occurring} in the rows, and for reading a stored state without kind of a column offering several
 * kinds of filter.
 */
public class TestOccurringValues extends TestCase {

	private static final String CITY = "city";

	private static final String TAGS = "tags";

	private static final int MAX_VALUES = 3;

	private record Person(String name, String city, List<String> tags) {
		// Test fixture.
	}

	private static List<Person> people() {
		return List.of(
			new Person("Anna", "Bern", List.of("x", "y")),
			new Person("Ben", "Hamburg", List.of("y")),
			new Person("Carl", "Aachen", List.of()),
			new Person("Dora", "Bern", List.of("z")),
			new Person("Emil", null, List.of("x")));
	}

	private static final BoundCodec<String> TEXT = BoundCodec.text(Function.identity());

	/** The values a cell stands for: the elements of a collection, or the value itself. */
	private static Collection<String> elements(Object value) {
		if (value instanceof Collection<?> collection) {
			List<String> result = new ArrayList<>();
			for (Object element : collection) {
				result.add((String) element);
			}
			return result;
		}
		return value == null ? List.of() : List.of((String) value);
	}

	private static OptionsColumnFilter<Object> values(int max) {
		return OptionsColumnFilter.<Object, String> occurring(TestOccurringValues::elements, TEXT, max);
	}

	private static List<Column<Person, ?>> columns(int max) {
		Column<Person, Object> city = DefaultColumn.<Person, Object> builder(CITY, Person::city)
			.sort(() -> Comparator.comparing(Object::toString))
			.filters(List.of(values(max), new TextColumnFilter<>(value -> value == null ? null : value.toString())))
			.build();
		Column<Person, Object> tags = DefaultColumn.<Person, Object> builder(TAGS, Person::tags)
			.filter(values(max))
			.build();
		return List.of(city, tags);
	}

	private static TableView<Person> newView(int max) {
		List<Column<Person, ?>> columns = columns(max);
		return DefaultTableView.create(columns, new ListRowSource<>(people(), columns));
	}

	private static List<String> names(TableView<Person> view) {
		return view.rows(0, view.rowCount()).stream().map(r -> r.data().name()).toList();
	}

	/** The values occurring in the column are offered in the column's order, with their counts. */
	public void testValuesInColumnOrder() {
		ListRowSource<Person> source = new ListRowSource<>(people(), columns(MAX_VALUES));
		MatchCounts counts = source.matchCounts(CITY, ColumnFilter.KIND_VALUES);
		assertEquals(List.of("Aachen", "Bern", "Hamburg"), counts.values());
		assertEquals(2, counts.count("Bern"));
	}

	/** The values stay offered while other filters narrow the rows; their counts follow. */
	public void testValuesIndependentOfOtherFilters() {
		ListRowSource<Person> source = new ListRowSource<>(people(), columns(MAX_VALUES));
		source.withFilter(new FilterSpec(Map.of(TAGS, new OptionsFilterState(Set.of("z")))));
		MatchCounts counts = source.matchCounts(CITY, ColumnFilter.KIND_VALUES);
		assertEquals(List.of("Aachen", "Bern", "Hamburg"), counts.values());
		assertEquals(1, counts.count("Bern"));
		assertEquals(0, counts.count("Hamburg"));
	}

	/** The list of values is offered up to the maximum number of distinct values. */
	public void testMaximum() {
		ListRowSource<Person> source = new ListRowSource<>(people(), columns(MAX_VALUES));
		OptionsColumnFilter<Object> atMaximum = values(3);
		assertTrue(atMaximum.offered(source.matchCounts(CITY, ColumnFilter.KIND_VALUES)));
		assertFalse(values(2).offered(source.matchCounts(CITY, ColumnFilter.KIND_VALUES)));
		assertFalse("Without counts, there are no values to offer.", atMaximum.offered(MatchCounts.NONE));
		assertTrue("Fixed options are always offered.",
			new OptionsColumnFilter<>(List.of(new Option("a", null))).offered(MatchCounts.NONE));
	}

	/** A cell holding several values matches if one of them is selected. */
	public void testMultipleValuesMatchOne() {
		TableView<Person> view = newView(MAX_VALUES);
		view.filter(TAGS, new OptionsFilterState(Set.of("y")));
		assertEquals(List.of("Anna", "Ben"), names(view));

		view.filter(TAGS, ColumnFilterState.invert(new OptionsFilterState(Set.of("x"))));
		assertEquals(List.of("Ben", "Carl", "Dora"), names(view));

		MatchCounts counts = new ListRowSource<>(people(), columns(MAX_VALUES)).matchCounts(TAGS);
		assertEquals(List.of("x", "y", "z"), counts.values());
		assertEquals(2, counts.count("x"));
	}

	/** The editor offers the occurring values, and keeps a selected value that no longer occurs. */
	public void testEditorOptions() {
		ListRowSource<Person> source = new ListRowSource<>(people(), columns(MAX_VALUES));
		MatchCounts counts = source.matchCounts(CITY, ColumnFilter.KIND_VALUES);
		OptionsColumnFilter<Object> filter = values(MAX_VALUES);
		assertTrue(((FilterInput.Options) filter.input()).occurring());

		List<Option> options = filter.options(counts, new OptionsFilterState(Set.of("Paris")));
		assertEquals(List.of("Aachen", "Bern", "Hamburg", "Paris"), options.stream().map(Option::value).toList());

		FilterEditor editor = FilterEditors.kindEditor(filter, null, counts);
		// One checkbox per value.
		assertEquals(3, editor.fields().size());
		editor.fields().get(1).model().setValue(Boolean.TRUE);
		assertEquals(new OptionsFilterState(Set.of("Bern")), editor.read());
	}

	/** An editor of a column not filtered starts with the first kind offered. */
	public void testEditorStartsWithOfferedKind() {
		List<Column<Person, ?>> columns = columns(2);
		ListRowSource<Person> source = new ListRowSource<>(people(), columns);
		ColumnFilterEditor editor =
			FilterEditors.create(columns.get(0).filters(), null, kind -> source.matchCounts(CITY, kind));
		assertFalse(editor.offered(ColumnFilter.KIND_VALUES));
		assertTrue(editor.offered(ColumnFilter.KIND_TEXT));
		assertEquals(ColumnFilter.KIND_TEXT, editor.activeKind());

		ColumnFilterEditor current = FilterEditors.create(columns.get(0).filters(),
			new OptionsFilterState(Set.of("Bern")), kind -> source.matchCounts(CITY, kind));
		assertEquals("A state of a kind not offered is still edited.", ColumnFilter.KIND_VALUES,
			current.activeKind());
	}

	/** A selection of occurring values is stored as the values themselves. */
	public void testPersistence() {
		OptionsColumnFilter<Object> filter = values(MAX_VALUES);
		OptionsFilterState state = new OptionsFilterState(Set.of("Bern", "Aachen"));
		assertEquals(state, filter.fromJson(filter.toJson(state)));
		assertNull("A text state is no selection.", filter.fromJson(Map.of(TextColumnFilter.PATTERN, "x")));
	}

	/** Occurring values express no declared value. */
	public void testNoDeclaredValue() {
		assertNull(values(MAX_VALUES).stateFor("Bern"));
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

	/** An in-memory {@link NamedFilterStore} routing through the JSON codec, like the real store. */
	private static final class MapNamedFilterStore implements NamedFilterStore {
		final Map<String, Object> _data = new HashMap<>();

		@Override
		public List<NamedFilter> load(TableId id, FilterCodec codec) {
			return NamedFilterCodec.read(_data.get(id.value()), codec);
		}

		@Override
		public void save(TableId id, List<NamedFilter> filters, FilterCodec codec) {
			_data.put(id.value(), NamedFilterCodec.toJson(filters, codec));
		}
	}

	/**
	 * A filter state of the column stored as a state of its only filter: the inner state, without
	 * kind.
	 */
	private static Map<String, Object> storedWithoutKind(Object stateJson) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("state", stateJson);
		return result;
	}

	/**
	 * A text state stored without kind, from a time the column had only its text filter, is read as
	 * the text kind, although the values are the first kind of the column now.
	 */
	public void testStoredTextStateReadAsTextKind() {
		MapViewStateStore store = new MapViewStateStore();
		TableId id = new TableId("t-old-text");
		Map<String, Object> json = new LinkedHashMap<>();
		json.put("filters", Map.of(CITY, storedWithoutKind(TextColumnFilter.textToJson(TextFilterState.contains("e")))));
		store._data.put(id.value(), json);

		List<Column<Person, ?>> columns = columns(MAX_VALUES);
		TableView<Person> view = DefaultTableView.create(columns, new ListRowSource<>(people(), columns), store, id);
		assertEquals(new ColumnFilterState(ColumnFilter.KIND_TEXT, TextFilterState.contains("e"), false),
			view.state().getFilters().get(CITY));
		assertEquals(List.of("Anna", "Carl", "Dora"), names(view));
	}

	/** A named filter saved without kinds is read the same way. */
	public void testNamedFilterWithoutKindRead() {
		FilterCodec singleKind = new FilterCodec() {
			@Override
			public Object toJson(String column, FilterState state) {
				return storedWithoutKind(TextColumnFilter.textToJson((TextFilterState) state));
			}

			@Override
			public FilterState fromJson(String column, Object json) {
				throw new UnsupportedOperationException();
			}
		};
		MapNamedFilterStore filterStore = new MapNamedFilterStore();
		TableId id = new TableId("t-old-named");
		filterStore._data.put(id.value(), NamedFilterCodec.toJson(
			List.of(NamedFilter.saved("old", "Old", Map.of(CITY, TextFilterState.contains("e")), null)), singleKind));

		List<Column<Person, ?>> columns = columns(MAX_VALUES);
		TableView<Person> view = DefaultTableView.create(columns, new ListRowSource<>(people(), columns), null, id,
			SortSpec.NONE, List.of(), List.of(), filterStore);
		view.applyNamedFilter("old");
		assertEquals(List.of("Anna", "Carl", "Dora"), names(view));
	}

	/** A selection of values of a column offering several kinds is stored with its kind. */
	public void testValuesStatePersisted() {
		MapViewStateStore store = new MapViewStateStore();
		TableId id = new TableId("t-values");
		List<Column<Person, ?>> columns = columns(MAX_VALUES);
		DefaultTableView.create(columns, new ListRowSource<>(people(), columns), store, id)
			.filter(CITY, new OptionsFilterState(Set.of("Bern")));

		List<Column<Person, ?>> columns2 = columns(MAX_VALUES);
		TableView<Person> restored =
			DefaultTableView.create(columns2, new ListRowSource<>(people(), columns2), store, id);
		assertEquals(new OptionsFilterState(Set.of("Bern")), restored.state().getFilters().get(CITY));
		assertEquals(List.of("Anna", "Dora"), names(restored));
	}

}
