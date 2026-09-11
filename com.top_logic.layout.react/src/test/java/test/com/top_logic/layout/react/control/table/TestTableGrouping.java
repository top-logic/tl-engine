/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.table.ExpandArguments;
import com.top_logic.layout.react.control.table.GroupArguments;
import com.top_logic.layout.react.control.table.SearchArguments;
import com.top_logic.layout.react.control.table.SelectRowArguments;
import com.top_logic.layout.react.control.table.SortArguments;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableViewState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Tests the grouping a {@link TableViewControl} offers over the table model: what the client is
 * told about a grouped table, and what the gestures on a group header do.
 */
public class TestTableGrouping extends TestCase {

	/** The name of the first column, which carries the group label. */
	private static final String COLUMN_NAME = "name";

	/** The name of the column the tests group by. */
	private static final String COLUMN_STATUS = "status";

	/** The name of the column aggregating its values over a group. */
	private static final String COLUMN_AMOUNT = "amount";

	/** State key of the client's row list. */
	private static final String ROWS = "rows";

	/** Per-row state key of the nesting depth. */
	private static final String TREE_DEPTH = "treeDepth";

	/** Per-row state key of whether the row can be expanded. */
	private static final String EXPANDABLE = "expandable";

	/** Per-row state key of whether an expandable row is expanded. */
	private static final String EXPANDED = "expanded";

	/** Per-row state key of whether the row is selected. */
	private static final String SELECTED = "selected";

	/** Per-row state key of a group's size. */
	private static final String GROUP_COUNT = "groupCount";

	/** State key telling the client to render the group affordances. */
	private static final String TREE_MODE = "treeMode";

	/** State key of the grouped column. */
	private static final String GROUPING = "grouping";

	private static final String OPEN = "open";

	private static final String CLOSED = "closed";

	/** What the status column displays for {@link #OPEN} - not the value itself. */
	private static final String OPEN_LABEL = "Offen";

	/** What the status column displays for {@link #CLOSED} - not the value itself. */
	private static final String CLOSED_LABEL = "Geschlossen";

	/** State key of the keyboard cursor row index. */
	private static final String CURSOR_INDEX = "cursorIndex";

	/** A row that is {@link #OPEN}. */
	private static final Item A = new Item("a", OPEN, 1);

	/** A row that is {@link #CLOSED}. */
	private static final Item B = new Item("b", CLOSED, 2);

	/** A row that is {@link #OPEN}. */
	private static final Item C = new Item("c", OPEN, 4);

	/**
	 * A row business object: its label, the value the tests group by, and a number to aggregate.
	 *
	 * @param name
	 *        The row label, shown in the first column.
	 * @param status
	 *        The value the rows are grouped by.
	 * @param amount
	 *        The number the amount column sums over a group.
	 */
	private record Item(String name, String status, int amount) {
		// Pure value type.
	}

	/** A control exposing what it pushed to the client, so a test can read the row state. */
	private static final class TestTable extends TableViewControl<Item> {

		TestTable(ReactContext context, com.top_logic.table.TableView<Item> view) {
			super(context, view, false);
		}

		Object clientState(String key) {
			return getState(key);
		}
	}

	private TestTable _table;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue());
		ListRowSource<Item> rows = new ListRowSource<>(new ArrayList<>(List.of(A, B, C)), columns());
		_table = new TestTable(context, DefaultTableView.create(columns(), rows));
		// Order the rows, so what a group holds is decided but the group order still follows the
		// rows: a grouping is applied to the sorted rows, not instead of the sort.
		sort(COLUMN_NAME, "asc");
	}

	/**
	 * Tests that grouping turns the rows into collapsible headers with their members nested below,
	 * each header carrying its size, and that the client is told to render the affordances.
	 */
	public void testGroupingProducesGroupRows() {
		group(COLUMN_STATUS);

		assertEquals(COLUMN_STATUS, _table.getGroupedColumn());
		assertEquals("A grouped table renders the group affordances of a tree.",
			Boolean.TRUE, _table.clientState(TREE_MODE));
		assertEquals(COLUMN_STATUS, _table.clientState(GROUPING));

		List<Map<String, Object>> rows = clientRows();
		assertEquals("Two group headers and the three rows they hold.", 5, rows.size());

		Map<String, Object> openGroup = rows.get(0);
		assertEquals(Integer.valueOf(0), openGroup.get(TREE_DEPTH));
		assertEquals(Boolean.TRUE, openGroup.get(EXPANDABLE));
		assertEquals(Boolean.TRUE, openGroup.get(EXPANDED));
		assertEquals("Two rows are open.", Integer.valueOf(2), openGroup.get(GROUP_COUNT));

		assertEquals("A member of the group is nested below it.",
			Integer.valueOf(1), rows.get(1).get(TREE_DEPTH));
		assertNull("A data row is no group.", rows.get(1).get(GROUP_COUNT));

		Map<String, Object> closedGroup = rows.get(3);
		assertEquals(Integer.valueOf(0), closedGroup.get(TREE_DEPTH));
		assertEquals("One row is closed.", Integer.valueOf(1), closedGroup.get(GROUP_COUNT));
	}

	/**
	 * Tests that a group header shows the group's value in the first column and, in the others,
	 * what the column aggregates over the group's rows.
	 *
	 * <p>
	 * The value is shown the way the grouped column shows it in its cells - by its label, its
	 * format - not as the raw value it groups by.
	 * </p>
	 */
	public void testGroupHeaderShowsValueAndAggregate() {
		group(COLUMN_STATUS);

		List<Map<String, Object>> rows = agentRows();
		assertEquals("The header shows what a member's cell of the grouped column shows.",
			cell(rows.get(1), COLUMN_STATUS), cell(rows.get(0), COLUMN_NAME));
		assertEquals(OPEN_LABEL, cell(rows.get(0), COLUMN_NAME));
		assertEquals("1 + 4 over the two open rows.", "5", cell(rows.get(0), COLUMN_AMOUNT));
		assertEquals(CLOSED_LABEL, cell(rows.get(3), COLUMN_NAME));
		assertEquals("2", cell(rows.get(3), COLUMN_AMOUNT));
		assertEquals("A data row shows its own value.", "1", cell(rows.get(1), COLUMN_AMOUNT));
	}

	/**
	 * Tests that the sort order decides the order within a group: reversing it reverses the members
	 * of each group.
	 */
	public void testSortAppliesWithinGroups() {
		group(COLUMN_STATUS);
		assertEquals(List.of("a", "c"), memberNames(1, 2));

		sort(COLUMN_NAME, "desc");

		assertEquals("The members follow the reversed order.", List.of("c", "a"), memberNames(1, 2));
	}

	/**
	 * Tests that filtering happens before grouping: a group holds only the rows that pass, and a
	 * group whose rows all fail is gone.
	 */
	public void testFilterAppliesBeforeGrouping() {
		group(COLUMN_STATUS);

		_table.executeClientCommand(TableViewControl.CMD_SEARCH, Map.of(SearchArguments.TERM, "a"));

		List<Map<String, Object>> rows = clientRows();
		assertEquals("One group with its single remaining row.", 2, rows.size());
		assertEquals(Integer.valueOf(1), rows.get(0).get(GROUP_COUNT));
		assertEquals(OPEN_LABEL, cell(agentRows().get(0), COLUMN_NAME));
	}

	/**
	 * Tests that collapsing a group hides its members and expanding it brings them back.
	 */
	public void testExpandTogglesAGroup() {
		group(COLUMN_STATUS);

		expand(0, false);

		List<Map<String, Object>> collapsed = clientRows();
		assertEquals("The two rows of the collapsed group are gone, the other group is untouched.",
			3, collapsed.size());
		assertEquals(Boolean.FALSE, collapsed.get(0).get(EXPANDED));
		assertEquals("The collapsed group still knows its size.",
			Integer.valueOf(2), collapsed.get(0).get(GROUP_COUNT));
		assertEquals("The other group follows, still expanded.",
			Integer.valueOf(1), collapsed.get(1).get(GROUP_COUNT));
		assertEquals(Boolean.TRUE, collapsed.get(1).get(EXPANDED));

		expand(0, true);

		assertEquals(5, clientRows().size());
	}

	/**
	 * Tests that the gesture selecting a row collapses a group header instead: a group stands for
	 * no object, so it never becomes the selection.
	 */
	public void testSelectOnAGroupCollapsesItAndSelectsNothing() {
		group(COLUMN_STATUS);
		select(1);
		assertEquals("The precondition: a data row is selectable.", Set.of(A), _table.getSelectedKeys());

		select(0);

		assertEquals("The row selected before is still the selection.", Set.of(A), _table.getSelectedKeys());
		assertEquals("The group collapsed instead.", 3, clientRows().size());

		select(0);

		assertEquals(Set.of(A), _table.getSelectedKeys());
		assertEquals("And expanded again.", 5, clientRows().size());
	}

	/**
	 * Tests that grouping and ungrouping keep the selection: a grouping rearranges the rows, it
	 * does not replace them, so a selected row stays selected - at its new position, which the
	 * keyboard cursor follows.
	 */
	public void testSelectionSurvivesGroupingAndUngrouping() {
		select(1);
		assertEquals(Set.of(B), _table.getSelectedKeys());

		group(COLUMN_STATUS);

		assertEquals("The selected row is the same object under the same key.",
			Set.of(B), _table.getSelectedKeys());
		List<Map<String, Object>> grouped = clientRows();
		assertEquals("It is the last row now: the second group's single member.",
			Boolean.TRUE, grouped.get(4).get(SELECTED));
		assertEquals("The cursor found it again.", Integer.valueOf(4), _table.clientState(CURSOR_INDEX));

		group("");

		assertEquals(Set.of(B), _table.getSelectedKeys());
		List<Map<String, Object>> flat = clientRows();
		assertEquals(Boolean.TRUE, flat.get(1).get(SELECTED));
		assertEquals(Integer.valueOf(1), _table.clientState(CURSOR_INDEX));
	}

	/**
	 * Tests that removing the grouping shows the rows flat again, without group affordances.
	 */
	public void testRemovingTheGroupingRestoresFlatRows() {
		group(COLUMN_STATUS);
		assertEquals(5, clientRows().size());

		group("");

		assertNull(_table.getGroupedColumn());
		assertEquals("", _table.clientState(GROUPING));
		assertEquals(Boolean.FALSE, _table.clientState(TREE_MODE));
		List<Map<String, Object>> rows = clientRows();
		assertEquals(3, rows.size());
		assertNull("A flat row is no group.", rows.get(0).get(GROUP_COUNT));
		assertNull("And carries no nesting.", rows.get(0).get(TREE_DEPTH));
	}

	/**
	 * Tests that a table whose initial state carries a grouping - the grouping a view configures -
	 * shows its group rows from the start, without anyone having to group it.
	 */
	public void testInitialGroupingShowsGroupRows() {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue());
		ListRowSource<Item> rows = new ListRowSource<>(new ArrayList<>(List.of(A, B, C)), columns());
		TableViewState initialState =
			DefaultTableView.initialState(columns(), SortSpec.NONE, List.of());
		initialState.setGrouping(new GroupSpec(List.of(COLUMN_STATUS)));
		TestTable table = new TestTable(context,
			new DefaultTableView<>(columns(), rows, initialState, null, null));

		assertEquals(COLUMN_STATUS, table.getGroupedColumn());
		assertEquals(Boolean.TRUE, table.clientState(TREE_MODE));
		assertEquals("Two group headers and the three rows they hold.", 5, clientRows(table).size());
	}

	/** Sends the client's group command for the given column, empty for no grouping. */
	private void group(String column) {
		_table.executeClientCommand(TableViewControl.CMD_GROUP, Map.of(GroupArguments.COLUMN, column));
	}

	/** Sends the client's sort command. */
	private void sort(String column, String direction) {
		_table.executeClientCommand(TableViewControl.CMD_SORT,
			Map.of(SortArguments.COLUMN, column, SortArguments.DIRECTION, direction));
	}

	/** Sends the client's expand command for the row at the given index. */
	private void expand(int rowIndex, boolean expanded) {
		_table.executeClientCommand(TableViewControl.CMD_EXPAND, Map.of(
			ExpandArguments.ROW_INDEX, Integer.valueOf(rowIndex),
			ExpandArguments.EXPANDED, Boolean.valueOf(expanded)));
	}

	/** Sends the client's plain (unmodified) select command for the row at the given index. */
	private void select(int rowIndex) {
		_table.executeClientCommand(TableViewControl.CMD_SELECT, Map.of(SelectRowArguments.ROW_INDEX, Integer.valueOf(rowIndex)));
	}

	/** The rows as pushed to the client. */
	private List<Map<String, Object>> clientRows() {
		return clientRows(_table);
	}

	/** The rows the given table pushed to the client. */
	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> clientRows(TestTable table) {
		return (List<Map<String, Object>>) table.clientState(ROWS);
	}

	/** The rows as projected for a headless consumer, which carries the cell texts. */
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> agentRows() {
		return (List<Map<String, Object>>) _table.scriptingScalarState().get(ROWS);
	}

	/** The text of one cell of a projected row. */
	@SuppressWarnings("unchecked")
	private static String cell(Map<String, Object> row, String column) {
		return (String) ((Map<String, Object>) row.get("cells")).get(column);
	}

	/** The names shown by the given range of projected rows. */
	private List<String> memberNames(int from, int count) {
		List<Map<String, Object>> rows = agentRows();
		List<String> names = new ArrayList<>(count);
		for (int n = from; n < from + count; n++) {
			names.add(cell(rows.get(n), COLUMN_NAME));
		}
		return names;
	}

	/** What the status column displays for a status value. */
	private static String statusLabel(String status) {
		if (OPEN.equals(status)) {
			return OPEN_LABEL;
		}
		return CLOSED.equals(status) ? CLOSED_LABEL : "";
	}

	/** The three columns: the label, the grouped value, and a summed number. */
	private static List<Column<Item, ?>> columns() {
		return List.of(
			DefaultColumn.<Item, String> builder(COLUMN_NAME, Item::name)
				.sort(() -> String::compareTo)
				.build(),
			DefaultColumn.<Item, String> builder(COLUMN_STATUS, Item::status)
				.renderer(value -> CellContent.text(statusLabel(value)))
				.build(),
			DefaultColumn.<Item, Integer> builder(COLUMN_AMOUNT, Item::amount)
				.aggregate(group -> {
					int sum = 0;
					for (Item member : group.members()) {
						sum += member.amount();
					}
					return CellContent.text(String.valueOf(sum));
				})
				.build());
	}

	/** Suite requiring the resources the table builds its column headers from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableGrouping.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
