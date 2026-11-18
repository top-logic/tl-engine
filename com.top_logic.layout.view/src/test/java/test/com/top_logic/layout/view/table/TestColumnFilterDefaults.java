/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.config.annotation.ConfigType;
import com.top_logic.element.meta.kbbased.storage.mappings.BooleanMapping;
import com.top_logic.element.meta.kbbased.storage.mappings.DirectMapping;
import com.top_logic.element.meta.kbbased.storage.mappings.IntMapping;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.ColumnType;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLType;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.ColumnFilterState;
import com.top_logic.table.MatchCounts;
import com.top_logic.table.TableView;
import com.top_logic.table.filter.BoundCodec;
import com.top_logic.table.filter.ComparisonOperator;
import com.top_logic.table.filter.FilterEditor;
import com.top_logic.table.filter.FilterEditors;
import com.top_logic.table.filter.FilterField;
import com.top_logic.table.filter.OptionsColumnFilter;
import com.top_logic.table.filter.OptionsFilterState;
import com.top_logic.table.filter.RangeFilterState;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.filter.TextFilterState;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Test for the kinds of filter the {@link ColumnProviderService} gives a column by the type of its
 * values, and for the list of the values occurring in it.
 */
public class TestColumnFilterDefaults extends TestCase {

	/** Name of the column under test, and the key of its value in a row map. */
	private static final String VALUE = "value";

	private TLModule _module;

	private TLClass _rowType;

	private TLEnumeration _level;

	private TLPrimitive _integer;

	private TLPrimitive _string;

	private TLPrimitive _date;

	private TLPrimitive _time;

	private TLPrimitive _dateTime;

	private TLPrimitive _boolean;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		TLModelImpl model = new TLModelImpl();
		_module = TLModelUtil.addModule(model, "test.filterDefaults");
		_rowType = TLModelUtil.addClass(_module, "Row");
		_level = TLModelUtil.addEnumeration(_module, "Level");
		TLModelUtil.addClassifier(_level, "medium");
		TLModelUtil.addClassifier(_level, "high");
		TLModelUtil.addClassifier(_level, "low");
		_integer = datatype("Integer", Kind.INT, IntMapping.INSTANCE);
		_string = datatype("String", Kind.STRING, directMapping(String.class));
		_boolean = datatype("Boolean", Kind.BOOLEAN, BooleanMapping.INSTANCE);
		_date = dateType("Date", null);
		_time = dateType("Time", "TIME");
		_dateTime = dateType("DateTime", "DATE_TIME");
	}

	/** The kinds of filter per type of the column's values. */
	public void testKindsPerType() {
		assertKinds("Enumeration", List.of(ColumnFilter.KIND_VALUES), _level, false);
		assertKinds("Multi-valued enumeration", List.of(ColumnFilter.KIND_VALUES), _level, true);
		assertKinds("Reference", List.of(ColumnFilter.KIND_VALUES, ColumnFilter.KIND_TEXT), _rowType, false);
		assertKinds("Multi-valued reference", List.of(ColumnFilter.KIND_VALUES, ColumnFilter.KIND_TEXT), _rowType,
			true);
		assertKinds("Number", List.of(ColumnFilter.KIND_RANGE, ColumnFilter.KIND_VALUES), _integer, false);
		assertKinds("Date", List.of(ColumnFilter.KIND_RANGE, ColumnFilter.KIND_VALUES), _date, false);
		assertKinds("Time of day", List.of(ColumnFilter.KIND_RANGE, ColumnFilter.KIND_VALUES), _time, false);
		assertKinds("Text", List.of(ColumnFilter.KIND_TEXT, ColumnFilter.KIND_VALUES), _string, false);
		assertKinds("Multi-valued text", List.of(ColumnFilter.KIND_VALUES, ColumnFilter.KIND_TEXT), _string, true);
		assertKinds("Truth value", List.of(ColumnFilter.KIND_BOOLEAN), _boolean, false);
		assertKinds("Unresolved", List.of(ColumnFilter.KIND_VALUES, ColumnFilter.KIND_TEXT), null, false);
	}

	/** The classifiers of an enumeration are all offered, however many values occur. */
	public void testEnumerationOffersAllClassifiers() {
		Column<Object, ?> column = column(_level, false);
		OptionsColumnFilter<?> filter = (OptionsColumnFilter<?>) column.filter().get();
		assertFalse(filter.isOccurring());
		assertEquals(3, filter.options().size());
		assertTrue(filter.offered(MatchCounts.NONE));
	}

	/** A cell of several texts matches if one of them is selected; the texts are the values. */
	public void testMultipleTextsMatchOne() {
		Column<Object, ?> column = column(_string, true);
		TableView<Object> view = view(column, List.of("x", "y"), List.of("y"), List.of());
		view.filter(VALUE, new OptionsFilterState(Set.of("x")));
		assertEquals(1, view.rowCount());
		assertEquals(List.of("x", "y"), view.columnMatchCounts(VALUE, ColumnFilter.KIND_VALUES).values());

		view.filter(VALUE, new ColumnFilterState(ColumnFilter.KIND_TEXT, TextFilterState.contains("y"), false));
		assertEquals("The text kind matches the text of the cell.", 2, view.rowCount());
	}

	/** A column holding more distinct values than the maximum offers no list of values. */
	public void testMaximumNumberOfValues() {
		int max = ColumnProviderService.getInstance().getConfig().getMaxValueOptions();
		assertEquals("Shipped maximum.", 50, max);
		Column<Object, ?> column = column(_string, false);
		ColumnFilter<?> values = column.filter(ColumnFilter.KIND_VALUES).get();

		assertTrue(values.offered(source(column, texts(max)).matchCounts(VALUE, ColumnFilter.KIND_VALUES)));
		assertFalse(values.offered(source(column, texts(max + 1)).matchCounts(VALUE, ColumnFilter.KIND_VALUES)));
	}

	/** Points in time of the same day are one value; times of day are values of their own. */
	public void testDayBasis() {
		Date morning = date(2026, 3, 1, 8, 15);
		Date evening = date(2026, 3, 1, 19, 45);
		Date nextDay = date(2026, 3, 2, 8, 15);
		for (TLPrimitive type : List.of(_date, _dateTime)) {
			Column<Object, ?> column = column(type, false);
			ListRowSource<Object> source = source(column, evening, morning, nextDay);
			MatchCounts counts = source.matchCounts(VALUE, ColumnFilter.KIND_VALUES);
			assertEquals(type.getName(), List.of(date(2026, 3, 1, 0, 0), date(2026, 3, 2, 0, 0)), counts.values());
			assertEquals(2, counts.count(date(2026, 3, 1, 0, 0)));

			TableView<Object> view = DefaultTableView.create(List.of(column), source);
			view.filter(VALUE, new ColumnFilterState(ColumnFilter.KIND_VALUES,
				new OptionsFilterState(Set.of(date(2026, 3, 1, 0, 0))), false));
			assertEquals(2, view.rowCount());
		}

		Column<Object, ?> time = column(_time, false);
		MatchCounts counts =
			source(time, evening, morning, nextDay, morning).matchCounts(VALUE, ColumnFilter.KIND_VALUES);
		assertEquals("Times of day by their exact value.", List.of(morning, evening, nextDay), counts.values());
		assertEquals(2, counts.count(morning));
	}

	/** Texts are values by their exact, case-sensitive text. */
	public void testTextValuesExact() {
		Column<Object, ?> column = column(_string, false);
		MatchCounts counts = source(column, "b", "B", "a", "b", "").matchCounts(VALUE, ColumnFilter.KIND_VALUES);
		assertEquals(List.of("B", "a", "b"), counts.values());
		assertEquals(2, counts.count("b"));
	}

	/** The bounds of a range are edited as typed values, with the column's codec as text fallback. */
	@SuppressWarnings("unchecked")
	public void testTypedRangeBounds() {
		Column<Object, ?> column = column(_integer, false);
		ColumnFilter<?> range = column.filter(ColumnFilter.KIND_RANGE).get();
		FilterEditor editor =
			FilterEditors.kindEditor(range, RangeFilterState.of(ComparisonOperator.GE, Long.valueOf(7)), MatchCounts.NONE);
		FilterField bound = editor.fields().get(1);
		assertEquals(Long.valueOf(7), bound.model().getValue());
		assertEquals("7", ((BoundCodec<Object>) bound.valueCodec()).format(bound.model().getValue()));

		bound.model().setValue(Long.valueOf(12));
		assertEquals(Long.valueOf(12), ((RangeFilterState<?>) editor.read()).primary());

		TableView<Object> view = view(column, Long.valueOf(5), Long.valueOf(12), null);
		view.filter(VALUE, editor.read());
		assertEquals(1, view.rowCount());
	}

	/** Numbers are values of the column, written and read back from their stored form. */
	public void testNumberValuesPersisted() {
		ColumnFilter<?> values = column(_integer, false).filter(ColumnFilter.KIND_VALUES).get();
		OptionsFilterState state = new OptionsFilterState(Set.of(Long.valueOf(3), Long.valueOf(5)));
		assertEquals(state, values.fromJson(values.toJson(state)));
	}

	/** A column with a filter of its own keeps it as its only kind. */
	public void testCustomFilterKept() {
		TextColumnFilter<String> custom = TextColumnFilter.forStrings();
		Column<Object, ?> column = ColumnProviderService.getInstance().createColumn(VALUE, ResKey.text(VALUE),
			ColumnType.of(_integer, false), row -> ((Map<?, ?>) row).get(VALUE), custom);
		assertEquals(1, column.filters().size());
		assertEquals(ColumnFilter.KIND_TEXT, column.filter().get().kind());
	}

	private void assertKinds(String message, List<String> expected, TLType type, boolean multiple) {
		List<String> kinds = new ArrayList<>();
		for (ColumnFilter<?> filter : column(type, multiple).filters()) {
			kinds.add(filter.kind());
		}
		assertEquals(message, expected, kinds);
	}

	private static Column<Object, ?> column(TLType type, boolean multiple) {
		return ColumnProviderService.getInstance().createColumn(VALUE, ResKey.text(VALUE),
			type == null ? ColumnType.UNRESOLVED : ColumnType.of(type, multiple), row -> ((Map<?, ?>) row).get(VALUE));
	}

	@SuppressWarnings("unchecked")
	private static ListRowSource<Object> source(Column<Object, ?> column, Object... values) {
		List<Object> rows = new ArrayList<>();
		for (Object value : values) {
			rows.add(value == null ? Map.of() : Map.of(VALUE, value));
		}
		return new ListRowSource<>(rows, List.<Column<Object, ?>> of(column));
	}

	private static ListRowSource<Object> source(Column<Object, ?> column, List<String> values) {
		return source(column, values.toArray());
	}

	private static TableView<Object> view(Column<Object, ?> column, Object... values) {
		return DefaultTableView.create(List.<Column<Object, ?>> of(column), source(column, values));
	}

	private static List<String> texts(int count) {
		List<String> result = new ArrayList<>();
		for (int n = 0; n < count; n++) {
			result.add("text" + n);
		}
		return result;
	}

	private static Date date(int year, int month, int day, int hour, int minute) {
		return new GregorianCalendar(year, month - 1, day, hour, minute).getTime();
	}

	private TLPrimitive datatype(String name, Kind kind, StorageMapping<?> mapping) {
		return TLModelUtil.addDatatype(_module, _module, name, kind, mapping);
	}

	private TLPrimitive dateType(String name, String configType) throws ConfigurationException {
		TLPrimitive type = datatype(name, Kind.DATE, directMapping(Date.class));
		if (configType != null) {
			ConfigType annotation = TypedConfiguration.newConfigItem(ConfigType.class);
			annotation.setValue(configType);
			type.setAnnotation(annotation);
		}
		return type;
	}

	private static StorageMapping<?> directMapping(Class<?> applicationType) throws ConfigurationException {
		PolymorphicConfiguration<?> config =
			TypedConfiguration.createConfigItemForImplementationClass(DirectMapping.class);
		config.update(config.descriptor().getProperty(DirectMapping.Config.APPLICATION_TYPE), applicationType);
		return (StorageMapping<?>) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	/**
	 * Test suite requiring the {@link ColumnProviderService} building the columns under test and the
	 * {@link AttributeSettings} its display of a value consults.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestColumnFilterDefaults.class,
				ColumnProviderService.Module.INSTANCE,
				AttributeSettings.Module.INSTANCE));
	}

}
