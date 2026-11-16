/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.table.ColumnDef;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnAlignment;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableView;
import com.top_logic.table.TableViewState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Tests that the client is told the {@link Column#alignment() alignment} of a column, and only
 * when it is not the start of the cell.
 */
public class TestColumnAlignment extends TestCase {

	/** The name of the column aligned to the start. */
	private static final String COLUMN_NAME = "name";

	/** The name of the column aligned to the end. */
	private static final String COLUMN_AMOUNT = "amount";

	/** State key of the client's column list. */
	private static final String COLUMNS = "columns";

	/** Per-column state key of the column name. */
	private static final String NAME = "name";

	/** Per-column state key of the alignment. */
	private static final String ALIGN = "align";

	/**
	 * A row business object.
	 *
	 * @param name
	 *        The row label.
	 * @param amount
	 *        A number displayed next to it.
	 */
	private record Item(String name, int amount) {
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
	 * Tests that a column ending at the end of its cells is sent with {@code align: "end"}, and a
	 * column starting at the start without the key.
	 */
	public void testClientIsToldAnEndAlignment() {
		TestTable table = control();

		assertEquals("end", columnState(table, COLUMN_AMOUNT).get(ALIGN));
		assertFalse("A column aligned to the start sends no key.",
			columnState(table, COLUMN_NAME).containsKey(ALIGN));
	}

	/** Tests that the state of a column definition carries the alignment to the end only. */
	public void testColumnDefSendsTheEndOnly() {
		assertEquals("end", new ColumnDef("a", "A").setAlignment(ColumnAlignment.END).toStateMap().get(ALIGN));
		assertFalse(new ColumnDef("a", "A").setAlignment(ColumnAlignment.START).toStateMap().containsKey(ALIGN));
		assertFalse("Without an alignment, the start applies.",
			new ColumnDef("a", "A").toStateMap().containsKey(ALIGN));
	}

	/** The column state the given table pushed to the client for the given column. */
	@SuppressWarnings("unchecked")
	private static Map<String, Object> columnState(TestTable table, String column) {
		for (Map<String, Object> columnState : (List<Map<String, Object>>) table.clientState(COLUMNS)) {
			if (column.equals(columnState.get(NAME))) {
				return columnState;
			}
		}
		throw new AssertionError("No state for column '" + column + "'.");
	}

	/** A control over a table with a text column and a column aligned to the end. */
	private static TestTable control() {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
				new ReactWindowRegistry("test"));
		List<Column<Item, ?>> columns = List.of(
			DefaultColumn.<Item, String> builder(COLUMN_NAME, Item::name)
				.build(),
			DefaultColumn.<Item, Integer> builder(COLUMN_AMOUNT, Item::amount)
				.alignment(ColumnAlignment.END)
				.build());
		TableViewState state = DefaultTableView.initialState(columns, SortSpec.NONE, List.of());
		ListRowSource<Item> rows =
			new ListRowSource<>(new ArrayList<>(List.of(new Item("a", 1), new Item("b", 2))), columns);
		return new TestTable(context, new DefaultTableView<>(columns, rows, state));
	}

	/** Suite requiring the resources the table builds its column headers from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestColumnAlignment.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
