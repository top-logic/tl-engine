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
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResKey;
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
import com.top_logic.table.impl.ListRowSource;

/**
 * Test for {@link ColumnSetup}: the column it builds is displayed in the width it configures, and
 * keeps the width of the column its {@link ColumnBinding} built when it configures none; it sorts
 * by the comparator it declares, and in the order its binding derives when it declares none.
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
		List<Object> rows = new ArrayList<>();
		for (String value : values) {
			Map<String, Object> row = new HashMap<>();
			row.put(ATTRIBUTE, value);
			rows.add(row);
		}
		return new ListRowSource<>(rows, List.of(ordered(BY_LENGTH).buildColumn()));
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
	 * Test suite requiring the {@link ColumnProviderService}, which the type-derived
	 * {@link ColumnBinding} builds its columns through.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestColumnSetup.class, ColumnProviderService.Module.INSTANCE));
	}

}
