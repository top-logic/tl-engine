/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableView;
import com.top_logic.table.TableViewState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Tests what a {@link TableViewControl} tells the client about the texts its table says over and
 * above what it displays: the {@link CellContent#tooltip() tooltip} of a cell, and the description
 * of a column's label.
 *
 * <p>
 * The client decides where such a text is offered, so the control's part is to send the texts that
 * exist and none that does not - a cell displaying all it has to say is sent without one, and so is
 * a column whose label describes itself.
 * </p>
 */
public class TestTableTooltips extends TestCase {

	/** The name of the column whose cells are labeled, with a tooltip on a described row. */
	private static final String COLUMN_LABELED = "labeled";

	/** The name of the column whose cells are labeled without a tooltip. */
	private static final String COLUMN_PLAIN = "plain";

	/** The name of the column whose cells render a payload, with a tooltip on a described row. */
	private static final String COLUMN_RAW = "raw";

	/** The name of the column whose cells are plain text. */
	private static final String COLUMN_TEXT = "text";

	/** The tooltip the cells of {@link #COLUMN_LABELED} carry. */
	private static final String LABELED_TOOLTIP = "What the label stands for";

	/** The tooltip the cells of {@link #COLUMN_RAW} carry. */
	private static final String RAW_TOOLTIP = "What the rendered payload stands for";

	/** The description the label of {@link #COLUMN_LABELED} carries. */
	private static final String COLUMN_DESCRIPTION = "What this column holds";

	/** The name of the row whose cells carry a tooltip. */
	private static final String DESCRIBED_ROW = "a";

	/** The name of the row whose cells carry none. */
	private static final String PLAIN_ROW = "b";

	/** State key of the client's row list. */
	private static final String ROWS = "rows";

	/** Per-row state key of the row index. */
	private static final String ROW_INDEX = "index";

	/** Per-row state key of the cell tooltips, by column name. */
	private static final String ROW_TOOLTIPS = "tooltips";

	/** State key of the client's column list. */
	private static final String COLUMNS = "columns";

	/** Per-column state key of the column name. */
	private static final String NAME = "name";

	/** Per-column state key of the description of the column's label. */
	private static final String COLUMN_TOOLTIP = "tooltip";

	/**
	 * A row business object.
	 *
	 * @param name
	 *        The row label.
	 * @param described
	 *        Whether the row's cells say more than they display.
	 */
	private record Item(String name, boolean described) {
		// Pure value type.
	}

	/** A control exposing what it pushed to the client. */
	private static final class TestTable extends TableViewControl<Item> {

		TestTable(ReactContext context, TableView<Item> view) {
			super(context, view, false);
		}

		Object clientState(String key) {
			return getState(key);
		}
	}

	/**
	 * Tests that the client is told the tooltip of each cell that has one, whichever content the
	 * cell is built from.
	 */
	public void testCellTooltipsAreSent() {
		TestTable table = control();

		assertEquals(Map.of(COLUMN_LABELED, LABELED_TOOLTIP, COLUMN_RAW, RAW_TOOLTIP),
			tooltips(table, 0));
	}

	/**
	 * Tests that a cell displaying all it has to say is sent without a tooltip: the client then
	 * offers the cell's own text where it does not fit.
	 */
	public void testCellsWithoutATooltipSendNone() {
		Map<String, String> tooltips = tooltips(control(), 0);

		assertFalse("A labeled cell without a tooltip sends none.", tooltips.containsKey(COLUMN_PLAIN));
		assertFalse("A text cell sends none.", tooltips.containsKey(COLUMN_TEXT));
	}

	/**
	 * Tests that a row none of whose cells has a tooltip is sent without the tooltip map at all.
	 */
	public void testRowWithoutTooltipsSendsNone() {
		TestTable table = control();

		assertFalse("The row says no more than it displays.",
			rowState(table, 1).containsKey(ROW_TOOLTIPS));
	}

	/**
	 * Tests that the client is told the description of a column's label, and that a column whose
	 * label has none is sent without the key.
	 */
	public void testColumnDescriptionIsSent() {
		TestTable table = control();

		assertEquals(COLUMN_DESCRIPTION, columnState(table, COLUMN_LABELED).get(COLUMN_TOOLTIP));
		assertFalse("A label without a description sends none.",
			columnState(table, COLUMN_PLAIN).containsKey(COLUMN_TOOLTIP));
	}

	/** The cell tooltips the given table pushed for the row with the given index. */
	@SuppressWarnings("unchecked")
	private static Map<String, String> tooltips(TestTable table, int index) {
		Map<String, Object> rowState = rowState(table, index);
		Map<String, String> tooltips = (Map<String, String>) rowState.get(ROW_TOOLTIPS);
		assertNotNull("The row carries cell tooltips.", tooltips);
		return tooltips;
	}

	/** The state the given table pushed to the client for the row with the given index. */
	private static Map<String, Object> rowState(TestTable table, int index) {
		for (Map<String, Object> rowState : clientRows(table)) {
			if (Integer.valueOf(index).equals(rowState.get(ROW_INDEX))) {
				return rowState;
			}
		}
		throw new AssertionError("No state for row " + index + ".");
	}

	/** The rows the given table pushed to the client. */
	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> clientRows(TestTable table) {
		return (List<Map<String, Object>>) table.clientState(ROWS);
	}

	/** The column state the given table pushed to the client for the given column. */
	private static Map<String, Object> columnState(TestTable table, String column) {
		for (Map<String, Object> columnState : clientColumns(table)) {
			if (column.equals(columnState.get(NAME))) {
				return columnState;
			}
		}
		throw new AssertionError("No state for column '" + column + "'.");
	}

	/** The columns the given table pushed to the client. */
	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> clientColumns(TestTable table) {
		return (List<Map<String, Object>>) table.clientState(COLUMNS);
	}

	/** A control over a {@link #table()}. */
	private static TestTable control() {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
				new ReactWindowRegistry("test"));
		return new TestTable(context, table());
	}

	/** A table of a described row and a plain one. */
	private static TableView<Item> table() {
		List<Column<Item, ?>> columns = columns();
		TableViewState state = DefaultTableView.initialState(columns, SortSpec.NONE, List.of());
		ListRowSource<Item> rows = new ListRowSource<>(
			new ArrayList<>(List.of(new Item(DESCRIBED_ROW, true), new Item(PLAIN_ROW, false))), columns);
		return new DefaultTableView<>(columns, rows, state);
	}

	/** One column of each kind of content a cell tooltip can come from, and two that have none. */
	private static List<Column<Item, ?>> columns() {
		return List.of(
			DefaultColumn.<Item, Item> builder(COLUMN_LABELED, item -> item)
				.label(describedLabel())
				.renderer(item -> new CellContent.Labeled(item.name(),
					item.described() ? LABELED_TOOLTIP : null, null, null))
				.build(),
			DefaultColumn.<Item, String> builder(COLUMN_PLAIN, Item::name)
				.renderer(name -> CellContent.label(name))
				.build(),
			DefaultColumn.<Item, Item> builder(COLUMN_RAW, item -> item)
				.renderer(item -> item.described()
					? new CellContent.Raw(item.name(), RAW_TOOLTIP)
					: new CellContent.Raw(item.name()))
				.build(),
			DefaultColumn.<Item, String> builder(COLUMN_TEXT, Item::name)
				.renderer(name -> CellContent.text(name))
				.build());
	}

	/**
	 * A label carrying a {@link #COLUMN_DESCRIPTION description}, as a key whose translations it
	 * holds itself - so the test says what it tests without a resource bundle of its own.
	 */
	private static ResKey describedLabel() {
		ResKey.Builder builder = ResKey.builder(COLUMN_LABELED);
		builder.add(Locale.ENGLISH, "Labeled");
		builder.add(Locale.GERMAN, "Beschriftet");
		ResKey.Builder description = builder.suffix(ResKey.TOOLTIP);
		description.add(Locale.ENGLISH, COLUMN_DESCRIPTION);
		description.add(Locale.GERMAN, COLUMN_DESCRIPTION);
		return builder.build();
	}

	/** Suite requiring the resources the table builds its column headers from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableTooltips.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
