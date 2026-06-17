/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.table;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.Row;
import com.top_logic.table.RowKind;
import com.top_logic.table.TableView;
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
				return CellContent.text("Σ " + sum);
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
		TableView<Sale> view = new DefaultTableView<>(columns(), grouped(), defaultState());
		Row<Sale> northHeader = view.rows(0, 1).get(0);

		// First column of the header shows the group label.
		CellContent label = view.cell(northHeader, "region");
		assertTrue(label instanceof CellContent.Labeled);
		assertEquals("North", ((CellContent.Labeled) label).text());

		// Amount column shows the aggregate (10 + 5 + 2 = 17).
		CellContent subtotal = view.cell(northHeader, "amount");
		assertTrue(subtotal instanceof CellContent.Text);
		assertEquals("Σ 17", ((CellContent.Text) subtotal).text());
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

	private com.top_logic.table.TableViewState defaultState() {
		com.top_logic.table.TableViewState state = new com.top_logic.table.TableViewState();
		state.setColumnOrder(List.of("region", "amount"));
		return state;
	}

}
