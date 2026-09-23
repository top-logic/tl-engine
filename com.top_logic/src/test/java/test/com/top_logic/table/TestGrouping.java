/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.Row;
import com.top_logic.table.RowKind;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableView;
import com.top_logic.table.TableViewState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Test for single-level grouping in {@link ListRowSource} and the group-header / subtotal
 * rendering in {@link DefaultTableView}.
 */
public class TestGrouping extends TestCase {

	private record Sale(String region, int amount) {
		// Test fixture.
	}

	private List<Column<Sale, ?>> columns() {
		Column<Sale, String> region = DefaultColumn.<Sale, String> builder("region", Sale::region).build();
		Column<Sale, Integer> amount = DefaultColumn.<Sale, Integer> builder("amount", Sale::amount)
			.aggregate(group -> {
				int sum = group.members().stream().mapToInt(Sale::amount).sum();
				return CellContent.text("Sum: " + sum);
			})
			.build();
		return List.of(region, amount);
	}

	private List<Sale> sales() {
		return List.of(
			new Sale("North", 10),
			new Sale("South", 7),
			new Sale("North", 5),
			new Sale("South", 3),
			new Sale("North", 2));
	}

	private ListRowSource<Sale> grouped() {
		ListRowSource<Sale> source = new ListRowSource<>(sales(), columns());
		source.withGrouping(new GroupSpec(List.of("region")));
		return source;
	}

	public void testGroupHeadersAndMembers() {
		ListRowSource<Sale> source = grouped();
		// 2 group headers (North, South) + 5 data rows = 7 displayed rows.
		assertEquals(7, source.size());

		List<Row<Sale>> rows = source.window(0, source.size());
		assertEquals(RowKind.GROUP_HEADER, rows.get(0).kind());
		assertEquals("North", rows.get(0).group().key().values().get(0));
		assertEquals(3, rows.get(0).group().size());
		assertTrue(rows.get(0).expandable());
		assertTrue(rows.get(0).expanded());

		// First group's members follow at depth 1.
		assertEquals(RowKind.DATA, rows.get(1).kind());
		assertEquals(1, rows.get(1).depth());
		assertEquals("North", rows.get(1).data().region());
	}

	public void testSubtotalRenderedOnHeader() {
		// The view owns the grouping: the state's grouping is pushed to the source by the constructor.
		TableView<Sale> view =
			new DefaultTableView<>(columns(), new ListRowSource<>(sales(), columns()), groupedState());
		Row<Sale> northHeader = view.rows(0, 1).get(0);

		// First column of the header shows the group value, rendered by the grouped column's own
		// renderer - here the default text renderer.
		CellContent label = view.cell(northHeader, "region");
		assertTrue(label instanceof CellContent.Text);
		assertEquals("North", ((CellContent.Text) label).text());

		// Amount column shows the aggregate (10 + 5 + 2 = 17).
		CellContent subtotal = view.cell(northHeader, "amount");
		assertTrue(subtotal instanceof CellContent.Text);
		assertEquals("Sum: 17", ((CellContent.Text) subtotal).text());
	}

	public void testCollapseAndExpand() {
		ListRowSource<Sale> source = grouped();
		Object northKey = source.window(0, 1).get(0).key();

		source.setExpanded(northKey, false);
		// North collapsed: header(North) + header(South) + 2 South members = 4 rows.
		assertEquals(4, source.size());
		assertFalse(source.window(0, 1).get(0).expanded());

		source.setExpanded(northKey, true);
		assertEquals(7, source.size());
		assertTrue(source.window(0, 1).get(0).expanded());
	}

	/**
	 * When the table is sorted by another column than the grouping column, the groups follow
	 * the grouping column's comparator, while the rows inside each group follow the sort.
	 */
	public void testGroupsFollowGroupingColumnComparator() {
		ListRowSource<Sale> source = new ListRowSource<>(unorderedSales(), sortableColumns(true));
		source.withGrouping(new GroupSpec(List.of("region")));
		source.withOrder(SortSpec.ascending("amount"));

		// Sorted by amount, South's rows come first; the groups are nevertheless alphabetical.
		assertEquals(List.of("North", "South"), groupValues(source));
		assertEquals(List.of(5, 8, 9), memberAmounts(source, "North"));
		assertEquals(List.of(1, 2), memberAmounts(source, "South"));
	}

	/**
	 * Sorting the grouping column descending reverses the order of the groups, while a
	 * secondary sort column still orders the rows inside each group.
	 */
	public void testDescendingGroupingColumnReversesGroupOrder() {
		ListRowSource<Sale> source = new ListRowSource<>(unorderedSales(), sortableColumns(true));
		source.withGrouping(new GroupSpec(List.of("region")));
		source.withOrder(new SortSpec(List.of(
			new SortColumn("region", false),
			new SortColumn("amount", true))));

		assertEquals(List.of("South", "North"), groupValues(source));
		assertEquals(List.of(1, 2), memberAmounts(source, "South"));
		assertEquals(List.of(5, 8, 9), memberAmounts(source, "North"));
	}

	/**
	 * The group of rows without a value in the grouping column comes last, in ascending as
	 * well as in descending group order.
	 */
	public void testNullGroupLast() {
		List<Sale> sales = new ArrayList<>(unorderedSales());
		sales.add(0, new Sale(null, 0));

		ListRowSource<Sale> source = new ListRowSource<>(sales, sortableColumns(true));
		source.withGrouping(new GroupSpec(List.of("region")));
		source.withOrder(SortSpec.ascending("amount"));
		assertEquals(Arrays.asList("North", "South", null), groupValues(source));

		source.withOrder(new SortSpec(List.of(new SortColumn("region", false))));
		assertEquals(Arrays.asList("South", "North", null), groupValues(source));
	}

	/**
	 * A grouping column without sort capability keeps the groups in the order their first rows
	 * appear in the sorted rows.
	 */
	public void testUnsortableGroupingColumnKeepsFirstAppearanceOrder() {
		ListRowSource<Sale> source = new ListRowSource<>(unorderedSales(), sortableColumns(false));
		source.withGrouping(new GroupSpec(List.of("region")));
		source.withOrder(SortSpec.ascending("amount"));

		// Sorted by amount, South's rows come first, and so does its group.
		assertEquals(List.of("South", "North"), groupValues(source));
		assertEquals(List.of(1, 2), memberAmounts(source, "South"));
		assertEquals(List.of(5, 8, 9), memberAmounts(source, "North"));
	}

	/**
	 * A region and a sortable amount column.
	 *
	 * @param sortableRegion
	 *        Whether the region column is sortable, too.
	 */
	private List<Column<Sale, ?>> sortableColumns(boolean sortableRegion) {
		DefaultColumn.Builder<Sale, String> regionBuilder = DefaultColumn.<Sale, String> builder("region", Sale::region);
		if (sortableRegion) {
			regionBuilder.sort(() -> Comparator.<String> naturalOrder());
		}
		Column<Sale, String> region = regionBuilder.build();
		Column<Sale, Integer> amount = DefaultColumn.<Sale, Integer> builder("amount", Sale::amount)
			.sort(() -> Comparator.<Integer> naturalOrder())
			.build();
		return List.of(region, amount);
	}

	/**
	 * Sales whose regions, ordered by amount, appear in reverse alphabetical order.
	 */
	private List<Sale> unorderedSales() {
		return List.of(
			new Sale("North", 9),
			new Sale("South", 2),
			new Sale("North", 5),
			new Sale("South", 1),
			new Sale("North", 8));
	}

	private static List<String> groupValues(ListRowSource<Sale> source) {
		List<String> result = new ArrayList<>();
		for (Row<Sale> row : source.window(0, source.size())) {
			if (row.kind() == RowKind.GROUP_HEADER) {
				result.add((String) row.group().key().values().get(0));
			}
		}
		return result;
	}

	private static List<Integer> memberAmounts(ListRowSource<Sale> source, String region) {
		List<Integer> result = new ArrayList<>();
		boolean inGroup = false;
		for (Row<Sale> row : source.window(0, source.size())) {
			if (row.kind() == RowKind.GROUP_HEADER) {
				inGroup = region.equals(row.group().key().values().get(0));
			} else if (inGroup) {
				result.add(row.data().amount());
			}
		}
		return result;
	}

	/**
	 * Sorting the first displayed column of a table grouped by a hidden column orders the groups
	 * in that direction, too, and the members within each group; the state keeps only the user's
	 * sort.
	 */
	public void testFirstColumnSortDecidesGroupDirection() {
		TableView<Sale> view = hiddenGroupingView(true);
		view.sort(new SortSpec(List.of(new SortColumn("amount", false))));

		assertEquals(List.of("South", "North"), groupValues(view));
		assertEquals(List.of(2, 1), memberAmounts(view, "South"));
		assertEquals(List.of(9, 8, 5), memberAmounts(view, "North"));
		assertEquals(List.of(new SortColumn("amount", false)), view.state().getSort());
	}

	/**
	 * Sorting a column other than the first displayed one leaves the groups in ascending order,
	 * while the members within each group follow the sort.
	 */
	public void testOtherColumnSortKeepsGroupsAscending() {
		TableView<Sale> view = hiddenGroupingView(true);
		view.sort(new SortSpec(List.of(new SortColumn("label", false))));

		assertEquals(List.of("North", "South"), groupValues(view));
		assertEquals(List.of(9, 8, 5), memberAmounts(view, "North"));
		assertEquals(List.of(2, 1), memberAmounts(view, "South"));
	}

	/**
	 * Moving another column to the front takes the direction of the group order from the sorted
	 * column that is no longer first; moving it back to the front restores that direction.
	 */
	public void testColumnOrderChangeUpdatesGroupDirection() {
		TableView<Sale> view = hiddenGroupingView(true);
		view.sort(new SortSpec(List.of(new SortColumn("amount", false))));
		assertEquals(List.of("South", "North"), groupValues(view));

		view.moveColumn("label", 0);
		assertEquals(List.of("North", "South"), groupValues(view));
		assertEquals(List.of(9, 8, 5), memberAmounts(view, "North"));

		view.moveColumn("amount", 0);
		assertEquals(List.of("South", "North"), groupValues(view));
	}

	/**
	 * Grouping a table whose first displayed column is already sorted descending orders the
	 * groups descending.
	 */
	public void testGroupingAfterSortTakesFirstColumnDirection() {
		TableView<Sale> view = hiddenGroupingView(false);
		view.sort(new SortSpec(List.of(new SortColumn("amount", false))));
		view.group(new GroupSpec(List.of("region")));

		assertEquals(List.of("South", "North"), groupValues(view));
		assertEquals(List.of(9, 8, 5), memberAmounts(view, "North"));
		assertEquals(List.of(new SortColumn("amount", false)), view.state().getSort());
	}

	/**
	 * A {@link DefaultTableView} over {@link #unorderedSales()} displaying the amount and a label
	 * column, with the sortable region column hidden.
	 *
	 * @param grouped
	 *        Whether the table starts grouped by the region column.
	 */
	private TableView<Sale> hiddenGroupingView(boolean grouped) {
		List<Column<Sale, ?>> columns = new ArrayList<>(sortableColumns(true));
		columns.add(DefaultColumn.<Sale, String> builder("label", sale -> sale.region() + " " + sale.amount())
			.sort(() -> Comparator.<String> naturalOrder())
			.build());
		TableViewState state = new TableViewState();
		state.setColumnOrder(new ArrayList<>(List.of("amount", "label")));
		Set<String> hidden = new LinkedHashSet<>();
		hidden.add("region");
		state.setHiddenColumns(hidden);
		if (grouped) {
			state.setGrouping(new GroupSpec(List.of("region")));
		}
		return new DefaultTableView<>(columns, new ListRowSource<>(unorderedSales(), columns), state);
	}

	private static List<String> groupValues(TableView<Sale> view) {
		List<String> result = new ArrayList<>();
		for (Row<Sale> row : view.rows(0, view.rowCount())) {
			if (row.kind() == RowKind.GROUP_HEADER) {
				result.add((String) row.group().key().values().get(0));
			}
		}
		return result;
	}

	private static List<Integer> memberAmounts(TableView<Sale> view, String region) {
		List<Integer> result = new ArrayList<>();
		boolean inGroup = false;
		for (Row<Sale> row : view.rows(0, view.rowCount())) {
			if (row.kind() == RowKind.GROUP_HEADER) {
				inGroup = region.equals(row.group().key().values().get(0));
			} else if (inGroup) {
				result.add(row.data().amount());
			}
		}
		return result;
	}

	private TableViewState groupedState() {
		TableViewState state = new TableViewState();
		state.setColumnOrder(List.of("region", "amount"));
		state.setGrouping(new GroupSpec(List.of("region")));
		return state;
	}

}
