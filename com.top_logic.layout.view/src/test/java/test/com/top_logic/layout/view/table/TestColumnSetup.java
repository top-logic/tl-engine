/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactValueListControl;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.react.controlprovider.ReactControlProvider;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.table.ColumnBinding;
import com.top_logic.layout.view.table.ColumnExport;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.layout.view.table.ColumnType;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.Group;
import com.top_logic.table.GroupKey;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.Row;
import com.top_logic.table.RowKind;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortSpec;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.DelegatingColumn;
import com.top_logic.table.impl.ListRowSource;

/**
 * Test for {@link ColumnSetup}: the column it builds is displayed in the width it configures, and
 * keeps the width of the column its {@link ColumnBinding} built when it configures none; it sorts
 * by the comparator it declares, in the order its binding derives when it declares none, and not
 * at all when it is not sortable.
 */
public class TestColumnSetup extends TestCase {

	/** The name of the column under test. */
	private static final String ATTRIBUTE = "name";

	/** The header label of the column under test. */
	private static final ResKey LABEL = ResKey.text("Name");

	/** The width a column has that configures none. */
	private static final int DEFAULT_WIDTH = 150;

	public void testConfiguredWidth() {
		Column<Object, ?> column = setup(220).buildColumn();

		assertEquals("The column is displayed in the configured width.", 220, column.defaultWidth());
		assertEquals("Everything but the width comes from the binding's column.", ATTRIBUTE, column.name());
		assertEquals(LABEL, column.label());
		assertTrue("The type-derived column is filterable.", column.filter().isPresent());
		assertTrue("The type-derived column is sortable.", column.sort().isPresent());
	}

	public void testWidthlessColumnKeepsTheBindingsWidth() {
		Column<Object, ?> column = setup(0).buildColumn();

		assertEquals("Without a configured width, the column keeps its own.", DEFAULT_WIDTH,
			column.defaultWidth());
		assertEquals(ATTRIBUTE, column.name());
	}

	/**
	 * A column over a computed value reads its cells through its own value function, so a row that
	 * is no model object shows what that function yields.
	 */
	public void testComputedValue() {
		Column<Object, ?> column = computed(null).buildColumn();

		assertEquals("The cell value comes from the column's own value function.",
			Integer.valueOf(3), column.value(Map.of(ATTRIBUTE, Integer.valueOf(3))));
	}

	/**
	 * A column declaring an aggregate shows it in the header row of a group, computed over the
	 * group's member rows and displayed as a cell of that column is.
	 */
	public void testAggregateOverAGroup() {
		Column<Object, ?> column = computed(rows -> Integer.valueOf(rows.size())).buildColumn();

		assertTrue("The column aggregates over a group.", column.aggregate().isPresent());
		CellContent aggregate = column.aggregate().get().over(group(Map.of(), Map.of(), Map.of()));
		assertEquals("The aggregate is displayed as a cell of the column is.", "3",
			((CellContent.Text) aggregate).text());

		assertTrue("A column declaring no aggregate leaves its group cell empty.",
			computed(null).buildColumn().aggregate().isEmpty());
	}

	/**
	 * Tests that a column whose declaration says {@code export="false"} is displayed but left out
	 * of exports, while every other column is exported.
	 */
	public void testExport() {
		ColumnSetup exported = exported(ColumnExport.DEFAULT);
		ColumnSetup suppressed = exported(new ColumnExport(false, null));

		assertTrue(exported.buildColumn().exportable());
		assertFalse(suppressed.buildColumn().exportable());
		assertTrue("A column kept out of exports is still offered for display.",
			suppressed.buildColumn().selectable());
	}

	private static ColumnSetup exported(ColumnExport export) {
		return new ColumnSetup(ATTRIBUTE, LABEL, ColumnType.UNRESOLVED,
			row -> ((Map<?, ?>) row).get(ATTRIBUTE), null, ColumnBinding.TYPE_DERIVED, 0, null, null, null, false,
			export);
	}

	/**
	 * A column declaring a comparator sorts its values by it, a column declaring none keeps the
	 * order of the column its binding built.
	 */
	public void testComparatorDecidesTheValueOrder() {
		Column<Object, ?> declared = ordered(BY_LENGTH).buildColumn();
		assertTrue("The column is sortable.", declared.sort().isPresent());
		assertTrue("A shorter value comes first, although it is alphabetically later.",
			compare(declared, "zz", "aaa") < 0);

		Column<Object, ?> derived = ordered(null).buildColumn();
		assertTrue("Without a comparator, the values are ordered by their label.",
			compare(derived, "zz", "aaa") > 0);
	}

	/**
	 * A row source sorts by the declared comparator in both directions, with a row without a value
	 * last when sorting ascending.
	 */
	public void testRowSourceSortsByTheComparator() {
		ListRowSource<Object> source = source("zz", null, "a", "yyyy", "bbb");

		source.withOrder(SortSpec.ascending(ATTRIBUTE));
		assertEquals(Arrays.asList("a", "zz", "bbb", "yyyy", null), values(source));

		source.withOrder(new SortSpec(List.of(new SortColumn(ATTRIBUTE, false))));
		List<Object> descending = values(source);
		assertEquals("Descending reverses the declared order of the values.",
			List.of("yyyy", "bbb", "zz", "a"), nonNull(descending));
		assertEquals("Every row is shown.", 5, descending.size());
	}

	/**
	 * A table grouped by a column declaring a comparator orders its groups by it, with the group
	 * of the rows without a value last.
	 */
	public void testGroupsFollowTheComparator() {
		ListRowSource<Object> source = source("zz", null, "a", "yyyy", "zz", "bbb");

		source.withGrouping(new GroupSpec(List.of(ATTRIBUTE)));
		assertEquals(Arrays.asList("a", "zz", "bbb", "yyyy", null), groupValues(source));

		source.withOrder(new SortSpec(List.of(new SortColumn(ATTRIBUTE, false))));
		assertEquals("Sorting the grouping column descending reverses the group order, empty last.",
			Arrays.asList("yyyy", "bbb", "zz", "a", null), groupValues(source));
	}

	/**
	 * A column that is not sortable offers no sorting, even when it declares a comparator, and a
	 * table grouped by it keeps its groups in the order of the given rows - although the values
	 * would sort differently.
	 */
	public void testUnsortableColumnKeepsTheGroupsInRowOrder() {
		assertTrue("A column sorts by default.", ordered(null).buildColumn().sort().isPresent());

		Column<Object, ?> column = unsortable(null).buildColumn();
		assertTrue("The column offers no sorting.", column.sort().isEmpty());
		assertTrue("A comparator does not make the column sort.",
			unsortable(BY_LENGTH).buildColumn().sort().isEmpty());

		ListRowSource<Object> source = new ListRowSource<>(rows("b", "a", "b", "c"), List.of(column));
		source.withGrouping(new GroupSpec(List.of(ATTRIBUTE)));
		assertEquals(List.of("b", "a", "c"), groupValues(source));
		assertEquals("The rows of a group keep their order.", List.of("b", "b", "a", "c"), values(source));
	}

	/**
	 * Collections of values are ordered element by element by the order of their elements: the
	 * first differing element decides, a collection that is the start of a longer one comes first,
	 * an empty collection before every other one, and an empty element after every other one.
	 */
	public void testElementwiseOrder() {
		Comparator<Object> order = ColumnSetup.elementwise(BY_LENGTH);

		assertTrue("The first differing element decides, not the length of the collections.",
			order.compare(List.of("a", "zz", "x"), List.of("a", "yyy")) < 0);
		assertTrue("Of two collections differing only in length, the shorter comes first.",
			order.compare(List.of("a", "zz"), List.of("a", "zz", "y")) < 0);
		assertTrue(order.compare(List.of("a", "zz", "y"), List.of("a", "zz")) > 0);
		assertTrue("An empty collection comes first.", order.compare(List.of(), List.of("a")) < 0);
		assertEquals("Collections of equally ordered elements are equal.", 0,
			order.compare(List.of("a", "zz"), List.of("b", "yy")));
		assertTrue("An empty element comes after every value.",
			order.compare(Arrays.asList("a", null), Arrays.asList("a", "yyyy")) > 0);
		assertTrue("A single value is ordered as a collection holding only that value.",
			order.compare("zz", List.of("zz", "a")) < 0);
	}

	/** Orders texts by their length, unlike the alphabetic order of their labels. */
	private static final Comparator<Object> BY_LENGTH =
		Comparator.comparingInt(value -> ((String) value).length());

	/** Compares the given values by the sort capability of the given column. */
	@SuppressWarnings("unchecked")
	private static int compare(Column<Object, ?> column, Object value1, Object value2) {
		return ((Comparator<Object>) column.sort().get().comparator()).compare(value1, value2);
	}

	/** A row source over rows holding the given values, in a column ordered {@link #BY_LENGTH}. */
	private static ListRowSource<Object> source(String... values) {
		return new ListRowSource<>(rows(values), List.of(ordered(BY_LENGTH).buildColumn()));
	}

	/** Rows holding the given values, in that order. */
	private static List<Object> rows(String... values) {
		List<Object> rows = new ArrayList<>();
		for (String value : values) {
			Map<String, Object> row = new HashMap<>();
			row.put(ATTRIBUTE, value);
			rows.add(row);
		}
		return rows;
	}

	/** The values of the data rows the given source shows, in display order. */
	private static List<Object> values(ListRowSource<Object> source) {
		List<Object> result = new ArrayList<>();
		for (Row<Object> row : source.window(0, source.size())) {
			if (row.kind() == RowKind.DATA) {
				result.add(((Map<?, ?>) row.data()).get(ATTRIBUTE));
			}
		}
		return result;
	}

	/** The value each group of the given source stands for, in display order. */
	private static List<Object> groupValues(ListRowSource<Object> source) {
		List<Object> result = new ArrayList<>();
		for (Row<Object> row : source.window(0, source.size())) {
			if (row.kind() == RowKind.GROUP_HEADER) {
				result.add(((Map<?, ?>) row.group().members().get(0)).get(ATTRIBUTE));
			}
		}
		return result;
	}

	/** The given values without the empty ones. */
	private static List<Object> nonNull(List<Object> values) {
		List<Object> result = new ArrayList<>(values);
		result.removeIf(value -> value == null);
		return result;
	}

	/**
	 * A descriptor over a value read from the row map, ordered by the given comparator.
	 *
	 * @param comparator
	 *        The order of the column's values, or {@code null} for the order its binding derives.
	 */
	private static ColumnSetup ordered(Comparator<Object> comparator) {
		return new ColumnSetup(ATTRIBUTE, LABEL, ColumnType.UNRESOLVED,
			row -> ((Map<?, ?>) row).get(ATTRIBUTE), null, ColumnBinding.TYPE_DERIVED, 0, null, null, comparator,
			false);
	}

	/**
	 * A descriptor over a value read from the row map, that is not sortable.
	 *
	 * @param comparator
	 *        The order of the column's values the descriptor states nevertheless, or {@code null}.
	 */
	private static ColumnSetup unsortable(Comparator<Object> comparator) {
		return new ColumnSetup(ATTRIBUTE, LABEL, ColumnType.UNRESOLVED,
			row -> ((Map<?, ?>) row).get(ATTRIBUTE), null, ColumnBinding.TYPE_DERIVED, 0, null, null, comparator,
			false, ColumnExport.DEFAULT, null, false);
	}

	/**
	 * A descriptor over a value read from the row map, aggregating with the given function.
	 *
	 * @param aggregate
	 *        What the column shows for a group, or {@code null} for a column that shows nothing
	 *        there.
	 */
	private static ColumnSetup computed(Function<List<Object>, Object> aggregate) {
		return new ColumnSetup(ATTRIBUTE, LABEL, ColumnType.UNRESOLVED,
			row -> ((Map<?, ?>) row).get(ATTRIBUTE), null, ColumnBinding.TYPE_DERIVED, 0, null, aggregate, null,
			false);
	}

	/** A group holding the given rows. */
	private static Group<Object> group(Object... rows) {
		List<Object> members = List.of(rows);
		return new Group<>() {
			@Override
			public GroupKey key() {
				return null;
			}

			@Override
			public int size() {
				return members.size();
			}

			@Override
			public List<Object> members() {
				return members;
			}
		};
	}

	/**
	 * A type-derived setup for the given width, over values nothing is known about - which yields
	 * the label column every column falls back to.
	 */
	private static ColumnSetup setup(int width) {
		return new ColumnSetup(ATTRIBUTE, LABEL, ColumnType.UNRESOLVED,
			row -> ColumnProviderService.attributeValue(row, ATTRIBUTE), null, ColumnBinding.TYPE_DERIVED, width);
	}

	/**
	 * A column with a tooltip puts it on its cells, computed from the row and the value, and on the
	 * header row of a group it forms, computed from the value of the group without a row.
	 */
	public void testTooltip() {
		Column<Object, ?> column = withTooltip().buildColumn();
		Map<String, Object> row = Map.of(ATTRIBUTE, "x");

		assertEquals("cell x", column.renderCell(row).tooltip());

		ListRowSource<Object> source = new ListRowSource<>(List.of(row), List.of(column));
		DefaultTableView<Object> view = DefaultTableView.create(List.of(column), source);
		view.group(new GroupSpec(List.of(ATTRIBUTE)));
		Row<Object> header = view.rows(0, 1).get(0);
		assertEquals(RowKind.GROUP_HEADER, header.kind());
		assertEquals("group x", view.cell(header, ATTRIBUTE).tooltip());
	}

	/** A column without a tooltip leaves its cells as they are. */
	public void testNoTooltip() {
		Column<Object, ?> column = computed(null).buildColumn();

		assertNull(column.renderCell(Map.of(ATTRIBUTE, "x")).tooltip());
	}

	private static ColumnSetup withTooltip() {
		return new ColumnSetup(ATTRIBUTE, LABEL, ColumnType.UNRESOLVED,
			row -> ((Map<?, ?>) row).get(ATTRIBUTE), null, ColumnBinding.TYPE_DERIVED, 0, null, null, null, false,
			ColumnExport.DEFAULT, (row, value) -> (row == null ? "group " : "cell ") + value);
	}

	/**
	 * A column with a CSS class puts the class computed from the row and the value on its data
	 * cells, and none where the computation yields nothing or an empty text.
	 */
	public void testCellClass() {
		Column<Object, ?> column = withCellClass(ColumnBinding.TYPE_DERIVED).buildColumn();

		assertEquals("cell-x", column.cssClass(Map.of(ATTRIBUTE, "x")));
		assertNull(column.cssClass(Map.of(ATTRIBUTE, NO_CLASS)));
		assertNull(column.cssClass(Map.of(ATTRIBUTE, EMPTY_CLASS)));
	}

	/**
	 * The CSS class a column computes is put on its cells in addition to the class the column its
	 * binding built puts there.
	 */
	public void testCellClassJoinsTheInnerClass() {
		ColumnBinding marking = setup -> markedInner(ColumnBinding.TYPE_DERIVED.createColumn(setup));
		Column<Object, ?> column = withCellClass(marking).buildColumn();

		assertEquals("inner cell-x", column.cssClass(Map.of(ATTRIBUTE, "x")));
		assertEquals("inner", column.cssClass(Map.of(ATTRIBUTE, NO_CLASS)));
	}

	/** The given column putting the class <code>inner</code> on every cell. */
	private static <V> Column<Object, V> markedInner(Column<Object, V> column) {
		return new DelegatingColumn<>(column) {
			@Override
			public String cssClass(Object row) {
				return "inner";
			}
		};
	}

	/** A cell the user may not read gets no CSS class computed from its value. */
	public void testNoCellClassForAnUnreadableCell() {
		Column<Object, ?> column = withCellClass(ColumnBinding.TYPE_DERIVED).buildColumn();

		assertNull(column.cssClass(Map.of(ATTRIBUTE, ColumnProviderService.UNREADABLE)));
	}

	/** A column without a CSS class leaves its cells without one. */
	public void testNoCellClass() {
		Column<Object, ?> column = withTooltip().buildColumn();

		assertNull(column.cssClass(Map.of(ATTRIBUTE, "x")));
	}

	/** The value for which {@link #withCellClass(ColumnBinding)} computes no class. */
	private static final String NO_CLASS = "none";

	/** The value for which {@link #withCellClass(ColumnBinding)} computes an empty class. */
	private static final String EMPTY_CLASS = "empty";

	/**
	 * A column of the given binding marking a cell with the class <code>cell-</code> and its value,
	 * except for the values {@link #NO_CLASS} and {@link #EMPTY_CLASS}. The computation rejects
	 * the {@link ColumnProviderService#UNREADABLE} marker, which it must never be passed.
	 */
	private static ColumnSetup withCellClass(ColumnBinding binding) {
		return new ColumnSetup(ATTRIBUTE, LABEL, ColumnType.UNRESOLVED,
			row -> ((Map<?, ?>) row).get(ATTRIBUTE), null, binding, 0, null, null, null, false,
			ColumnExport.DEFAULT, null, (row, value) -> {
				assertNotSame(ColumnProviderService.UNREADABLE, value);
				assertNotNull(row);
				if (NO_CLASS.equals(value)) {
					return null;
				}
				if (EMPTY_CLASS.equals(value)) {
					return "";
				}
				return "cell-" + value;
			});
	}

	/**
	 * A column with a display shows the value of a data cell through it, and keeps the display of
	 * its type for an empty cell and a cell the user may not read.
	 */
	public void testDisplay() {
		RecordingDisplay display = new RecordingDisplay();
		Column<Object, ?> column = withDisplay(display, null, null).buildColumn();
		Column<Object, ?> plain = withDisplay(null, null, null).buildColumn();

		assertEquals("x", shown(display, column.renderCell(Map.of(ATTRIBUTE, "x"))));

		Map<String, Object> empty = Map.of();
		assertEquals("An empty cell keeps the display of its type.", plain.renderCell(empty),
			column.renderCell(empty));
		Map<String, Object> unreadable = Map.of(ATTRIBUTE, ColumnProviderService.UNREADABLE);
		assertEquals("A cell the user may not read keeps the display of its type.",
			plain.renderCell(unreadable), column.renderCell(unreadable));
		Map<String, Object> noValues = Map.of(ATTRIBUTE, List.of());
		assertEquals("A cell without values keeps the display of its type.", plain.renderCell(noValues),
			column.renderCell(noValues));
		assertEquals("Only the displayed value reached the display.", List.of("x"), display._shown);
	}

	/**
	 * The header row of a group and the aggregate of a group show their value through the display
	 * of the column.
	 */
	public void testDisplayOfGroupValues() {
		RecordingDisplay display = new RecordingDisplay();
		Column<Object, ?> column = withDisplay(display, rows -> "sum " + rows.size(), null).buildColumn();
		Column<Object, ?> plain = withDisplay(null, null, null).buildColumn();

		assertEquals("g", shown(display, render(column, "g")));
		assertEquals("A group without a value keeps the display of its type.", render(plain, null),
			render(column, null));
		assertEquals("sum 2", shown(display, column.aggregate().get().over(group("a", "b"))));
	}

	/** Renders the given group value through the renderer of the given column. */
	@SuppressWarnings("unchecked")
	private static CellContent render(Column<Object, ?> column, Object groupValue) {
		return ((Column<Object, Object>) column).renderer().render(groupValue);
	}

	/** The tooltip of a column wraps what its display shows. */
	public void testDisplayKeepsTheTooltip() {
		RecordingDisplay display = new RecordingDisplay();
		Column<Object, ?> column =
			withDisplay(display, null, (row, value) -> (row == null ? "group " : "cell ") + value).buildColumn();

		CellContent cell = column.renderCell(Map.of(ATTRIBUTE, "x"));
		assertEquals("cell x", cell.tooltip());
		assertEquals("x", shown(display, cell));

		CellContent groupCell = render(column, "g");
		assertEquals("group g", groupCell.tooltip());
		assertEquals("g", shown(display, groupCell));
	}

	/**
	 * The search finds a cell by the label a display computes, and by the text of the type of the
	 * values where the display computes none.
	 */
	public void testSearchTextFollowsALabelingDisplay() {
		Column<Object, ?> labeled = withDisplay(new LabelingDisplay(), null, null).buildColumn();
		Column<Object, ?> unlabeled = withDisplay(new RecordingDisplay(), null, null).buildColumn();

		assertEquals("label x", labeled.searchText(Map.of(ATTRIBUTE, "x")));
		assertEquals("x", unlabeled.searchText(Map.of(ATTRIBUTE, "x")));
		Map<String, Object> empty = Map.of();
		assertEquals("An empty cell is searched by the text of its type.",
			withDisplay(null, null, null).buildColumn().searchText(empty), labeled.searchText(empty));
	}

	/**
	 * A cell holding several values shows each of them through the display, in a read-only list of
	 * values, and is searched by their labels joined as the values of any multi-valued cell are.
	 */
	public void testDisplayOfSeveralValues() {
		LabelingDisplay display = new LabelingDisplay();
		Column<Object, ?> column = withDisplay(display, null, null).buildColumn();
		Map<String, Object> row = Map.of(ATTRIBUTE, List.of("a", "b"));

		CellContent cell = column.renderCell(row);
		assertTrue("Not shown through a control: " + cell, cell instanceof CellContent.Raw);
		ReactContext context =
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		ReactControl control = ((CellControlFactory) ((CellContent.Raw) cell).payload()).create(context);
		assertTrue("Not a list of values: " + control, control instanceof ReactValueListControl);
		assertEquals("Each value is shown through the display.", List.of("a", "b"), display._shown);

		assertEquals("label a, label b", column.searchText(row));
	}

	/**
	 * The value the given display was given by the control factory of the given cell content.
	 */
	private static Object shown(RecordingDisplay display, CellContent content) {
		assertTrue("Not shown through the display: " + content, content instanceof CellContent.Raw);
		Object payload = ((CellContent.Raw) content).payload();
		assertTrue("Not a control factory: " + payload, payload instanceof CellControlFactory);
		int before = display._shown.size();
		((CellControlFactory) payload).create(null);
		assertEquals("The display created one control.", before + 1, display._shown.size());
		return display._shown.get(before);
	}

	/**
	 * A column reading the value under {@link #ATTRIBUTE}, shown through the given display.
	 *
	 * @param display
	 *        The display, {@code null} for the type-derived one.
	 * @param aggregate
	 *        The aggregate of a group, {@code null} for none.
	 * @param tooltip
	 *        The tooltip of a cell, {@code null} for none.
	 */
	private static ColumnSetup withDisplay(ReactControlProvider display, Function<List<Object>, Object> aggregate,
			BiFunction<Object, Object, String> tooltip) {
		return new ColumnSetup(ATTRIBUTE, LABEL, ColumnType.UNRESOLVED,
			row -> ((Map<?, ?>) row).get(ATTRIBUTE), null, ColumnBinding.TYPE_DERIVED, 0, null, aggregate, null,
			false, ColumnExport.DEFAULT, tooltip, null, display);
	}

	/**
	 * {@link ReactControlProvider} recording the values it is given, creating no control.
	 */
	private static class RecordingDisplay implements ReactControlProvider {

		final List<Object> _shown = new ArrayList<>();

		@Override
		public ReactControl createControl(ReactContext context, Object model) {
			_shown.add(model);
			return null;
		}
	}

	/**
	 * {@link RecordingDisplay} computing a label of its own for a value.
	 */
	private static class LabelingDisplay extends RecordingDisplay implements LabelProvider {

		@Override
		public String getLabel(Object object) {
			return "label " + object;
		}
	}

	/**
	 * Test suite requiring the {@link ColumnProviderService}, which the type-derived
	 * {@link ColumnBinding} builds its columns through.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestColumnSetup.class, ColumnProviderService.Module.INSTANCE));
	}

}
