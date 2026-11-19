/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProvider;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.ColumnType;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.ColumnFilterState;
import com.top_logic.table.FilterCodec;
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
 * Test for the list of values of a column over business objects: persisting a selection of
 * objects, reading the text states stored before the column offered the list, and a column of an
 * application's own {@link ColumnProvider}.
 */
public class TestObjectValueFilter extends AbstractModelAccessTest {

	/** Name of the column over projects. */
	private static final String PROJECTS = "projects";

	/** The cells are read by a user who may read every value. */
	@Override
	protected Person initialUser() {
		return _root;
	}

	private static Column<Object, ?> column() {
		return ColumnProviderService.getInstance().createColumn(PROJECTS, ResKey.text(PROJECTS),
			ColumnType.of(type(PROJECT), false), Function.identity());
	}

	private List<Object> rows() {
		return List.of(_project, _otherProject);
	}

	private static List<Object> data(TableView<Object> view) {
		return view.rows(0, view.rowCount()).stream().map(row -> row.data()).toList();
	}

	/**
	 * A column over objects offers the objects occurring in it, sorted as the column sorts them, and
	 * a text filter on their labels.
	 */
	public void testObjectValues() {
		Column<Object, ?> column = column();
		List<Column<Object, ?>> columns = List.of(column);
		ListRowSource<Object> source = new ListRowSource<>(List.of(_project, _otherProject, _project), columns);
		MatchCounts counts = source.matchCounts(PROJECTS, ColumnFilter.KIND_VALUES);
		assertEquals("Sorted by label.", List.of(_otherProject, _project), counts.values());
		assertEquals(2, counts.count(_project));
		assertTrue(column.filter(ColumnFilter.KIND_VALUES).get().offered(counts));

		TableView<Object> view = DefaultTableView.create(columns, source);
		view.filter(PROJECTS, new OptionsFilterState(Set.of(_otherProject)));
		assertEquals(List.of(_otherProject), data(view));

		view.filter(PROJECTS, new ColumnFilterState(ColumnFilter.KIND_TEXT, TextFilterState.contains("other"), true));
		assertEquals("The inverted text kind.", List.of(_project, _project), data(view));
	}

	/** A cell of several objects matches if one of them is selected. */
	public void testMultipleObjectsMatchOne() {
		Column<Object, ?> column = ColumnProviderService.getInstance().createColumn(PROJECTS, ResKey.text(PROJECTS),
			ColumnType.of(type(PROJECT), true), Function.identity());
		List<Column<Object, ?>> columns = List.of(column);
		List<Object> rows = List.of(List.of(_project, _otherProject), List.of(_project), List.of());
		TableView<Object> view = DefaultTableView.create(columns, new ListRowSource<>(rows, columns));
		view.filter(PROJECTS, new OptionsFilterState(Set.of(_otherProject)));
		assertEquals(1, view.rowCount());
		assertEquals("The cell without an object is offered as no value.",
			List.of(_otherProject, _project, Option.NO_VALUE),
			view.columnMatchCounts(PROJECTS, ColumnFilter.KIND_VALUES).values());

		view.filter(PROJECTS, new OptionsFilterState(Set.of(Option.NO_VALUE)));
		assertEquals(List.of(List.of()), data(view));
	}

	/**
	 * A cell of several classifiers matches if one of them is selected; all classifiers are offered.
	 */
	public void testMultipleClassifiersMatchOne() {
		TLModule module = TLModelUtil.addModule(new TLModelImpl(), "test.multipleClassifiers");
		TLEnumeration level = TLModelUtil.addEnumeration(module, "Level");
		TLClassifier low = TLModelUtil.addClassifier(level, "low");
		TLClassifier high = TLModelUtil.addClassifier(level, "high");
		TLClassifier medium = TLModelUtil.addClassifier(level, "medium");
		Column<Object, ?> column = ColumnProviderService.getInstance().createColumn(PROJECTS, ResKey.text(PROJECTS),
			ColumnType.of(level, true), Function.identity());
		assertEquals(List.of(ColumnFilter.KIND_VALUES), column.filters().stream().map(ColumnFilter::kind).toList());
		OptionsColumnFilter<?> values = (OptionsColumnFilter<?>) column.filter().get();
		assertFalse(values.isOccurring());
		assertEquals(3, values.options().size());

		List<Column<Object, ?>> columns = List.of(column);
		List<Object> rows = List.of(List.of(low, high), List.of(medium), List.of());
		TableView<Object> view = DefaultTableView.create(columns, new ListRowSource<>(rows, columns));
		view.filter(PROJECTS, new OptionsFilterState(Set.of(high)));
		assertEquals(1, view.rowCount());
	}

	/** A selection of objects is stored by their identities and read back. */
	public void testObjectsPersisted() {
		ColumnFilter<?> values = column().filter(ColumnFilter.KIND_VALUES).get();
		OptionsFilterState state = new OptionsFilterState(Set.of(_project, _otherProject));
		assertEquals(state, values.fromJson(values.toJson(state)));
	}

	/** A stored object that no longer exists is dropped from the selection. */
	public void testDeletedObjectDropped() {
		TLObject temporary;
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			temporary = DynamicModelService.getFactoryFor(MODULE).createObject(type(PROJECT));
			temporary.tUpdateByName(NAME, "temporary");
			tx.commit();
		}
		ColumnFilter<?> values = column().filter(ColumnFilter.KIND_VALUES).get();
		Object json = values.toJson(new OptionsFilterState(Set.of(_project, temporary)));

		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			temporary.tDelete();
			tx.commit();
		}
		assertEquals(new OptionsFilterState(Set.of(_project)), values.fromJson(json));
	}

	/**
	 * A text state stored for a column over objects before the column offered a list of values -
	 * without kind - is read as the text kind and filters as before.
	 */
	public void testStoredTextStateReadAsText() {
		MapViewStateStore store = new MapViewStateStore();
		TableId id = new TableId("t-projects");
		Map<String, Object> json = new LinkedHashMap<>();
		json.put("filters", Map.of(PROJECTS, storedWithoutKind(TextFilterState.contains("other"))));
		store._data.put(id.value(), json);

		List<Column<Object, ?>> columns = List.of(column());
		TableView<Object> view = DefaultTableView.create(columns, new ListRowSource<>(rows(), columns), store, id);
		assertEquals(new ColumnFilterState(ColumnFilter.KIND_TEXT, TextFilterState.contains("other"), false),
			view.state().getFilters().get(PROJECTS));
		assertEquals(List.of(_otherProject), data(view));
	}

	/** A named filter saved without kinds for a column over objects is read the same way. */
	public void testNamedFilterWithoutKindRead() {
		FilterCodec singleKind = new FilterCodec() {
			@Override
			public Object toJson(String column, FilterState state) {
				return storedWithoutKind((TextFilterState) state);
			}

			@Override
			public FilterState fromJson(String column, Object json) {
				throw new UnsupportedOperationException();
			}
		};
		MapNamedFilterStore filterStore = new MapNamedFilterStore();
		TableId id = new TableId("t-projects-named");
		filterStore._data.put(id.value(), NamedFilterCodec.toJson(
			List.of(NamedFilter.saved("old", "Old", Map.of(PROJECTS, TextFilterState.contains("other")), null)),
			singleKind));

		List<Column<Object, ?>> columns = List.of(column());
		TableView<Object> view = DefaultTableView.create(columns, new ListRowSource<>(rows(), columns), null, id,
			SortSpec.NONE, List.of(), List.of(), filterStore);
		view.applyNamedFilter("old");
		assertEquals(List.of(_otherProject), data(view));
	}

	/** A column of an application's provider offering a single filter keeps it, unchanged. */
	public void testProviderWithSingleFilter() throws ConfigurationException {
		Column<Object, ?> column = providerService().createColumn(NAME, ResKey.text(NAME),
			ColumnType.of(TLModelPartRef.ref("tl.core:String").resolveType(), false),
			row -> ((TLObject) row).tValueByName(NAME));
		assertEquals(1, column.filters().size());
		assertTrue(column.filter().get() instanceof TextColumnFilter);

		List<Column<Object, ?>> columns = List.of(column);
		TableView<Object> view = DefaultTableView.create(columns, new ListRowSource<>(rows(), columns));
		view.filter(NAME, ColumnFilterState.invert(TextFilterState.contains("other")));
		assertEquals(List.of(_project), data(view));
	}

	/** The state of a column with a single text filter, as it is stored: without kind. */
	private static Map<String, Object> storedWithoutKind(TextFilterState state) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("state", TextColumnFilter.textToJson(state));
		return result;
	}

	/**
	 * A {@link ColumnProviderService} building the columns of strings by {@link TextProvider}.
	 */
	private static ColumnProviderService providerService() throws ConfigurationException {
		ColumnProviderService.Config config = TypedConfiguration.newConfigItem(ColumnProviderService.Config.class);
		config.setImplementationClass(ColumnProviderService.class);
		ColumnProviderService.ProviderMapping mapping =
			TypedConfiguration.newConfigItem(ColumnProviderService.ProviderMapping.class);
		TLModelPartRef stringType = TLModelPartRef.ref("tl.core:String");
		mapping.update(mapping.descriptor().getProperty(ColumnProviderService.ProviderMapping.TYPE), stringType);
		mapping.update(mapping.descriptor().getProperty("impl"),
			TypedConfiguration.createConfigItemForImplementationClass(TextProvider.class));
		config.getProviders().put(stringType, mapping);
		return new ColumnProviderService(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config) {
			{
				startUp();
			}
		};
	}

	/**
	 * A {@link ColumnProvider} of an application building a column with a single text filter.
	 */
	public static class TextProvider implements ColumnProvider {

		/**
		 * Creates a {@link TextProvider}.
		 */
		@CalledByReflection
		public TextProvider(InstantiationContext context, PolymorphicConfiguration<?> config) {
			// No configuration.
		}

		@Override
		public Column<Object, ?> createColumn(String name, ResKey label, ColumnType type,
				Function<Object, Object> value) {
			return DefaultColumn.<Object, Object> builder(name, value)
				.label(label)
				.sort(() -> Comparator.comparing(String::valueOf))
				.filter(new TextColumnFilter<>(String::valueOf))
				.build();
		}
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
	 * The test suite, additionally starting the services building the columns.
	 */
	public static Test suite() {
		return suiteWith(TestObjectValueFilter.class, TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE,
			FieldControlService.Module.INSTANCE, ColumnProviderService.Module.INSTANCE);
	}

}
