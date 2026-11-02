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
import com.top_logic.layout.react.control.table.ScrollArguments;
import com.top_logic.layout.react.control.table.SearchArguments;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.table.Column;
import com.top_logic.table.Selection;
import com.top_logic.table.SelectionMode;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableView;
import com.top_logic.table.TableViewState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Tests the window of rows a {@link TableViewControl} sends for the viewport its client reports.
 */
public class TestTableViewport extends TestCase {

	/** The name of the only column. */
	private static final String COLUMN_NAME = "name";

	/** State key of the rows sent. */
	private static final String ROWS = "rows";

	/** State key of the index of the first row sent. */
	private static final String VIEWPORT_START = "viewportStart";

	/** State key of the first row of the viewport. */
	private static final String SCROLL_START = "scrollStart";

	/** The client's command reporting the rows the viewport shows. */
	private static final String CMD_SCROLL = "scroll";

	/** The rows sent before and after the viewport. */
	private static final int PREFETCH_ROWS = 20;

	/** The number of rows of the table, more than a viewport may show. */
	private static final int ROW_COUNT = 5000;

	/** A control exposing what it pushed to the client. */
	private static final class TestTable extends TableViewControl<String> {

		TestTable(ReactContext context, TableView<String> view) {
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
		List<String> items = new ArrayList<>(ROW_COUNT);
		for (int n = 0; n < ROW_COUNT; n++) {
			items.add("row " + n);
		}
		List<Column<String, ?>> columns =
			List.of(DefaultColumn.<String, String> builder(COLUMN_NAME, item -> item).build());
		TableViewState state = DefaultTableView.initialState(columns, SortSpec.NONE, List.of());
		state.setSelection(Selection.none(SelectionMode.MULTI));
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test"));
		_table = new TestTable(context,
			new DefaultTableView<>(columns, new ListRowSource<>(items, columns), state));
	}

	/**
	 * Tests that the rows of a viewport fitting into a table body are sent with the prefetched rows
	 * around them.
	 */
	public void testSendsViewportWithPrefetch() {
		scroll(1000, 19);
		assertEquals(1000 - PREFETCH_ROWS, _table.clientState(VIEWPORT_START));
		assertEquals(19 + 2 * PREFETCH_ROWS, rows().size());
	}

	/**
	 * Tests that a viewport as high as all rows - reported by a body no container bounds - sends no
	 * more than {@link TableViewControl#MAX_VIEWPORT_ROWS} rows plus the prefetched ones, also with
	 * the updates following it.
	 */
	public void testLimitsViewport() {
		scroll(0, ROW_COUNT);
		assertEquals(TableViewControl.MAX_VIEWPORT_ROWS + PREFETCH_ROWS, rows().size());

		scroll(1000, ROW_COUNT);
		assertEquals(TableViewControl.MAX_VIEWPORT_ROWS + 2 * PREFETCH_ROWS, rows().size());

		_table.executeClientCommand(TableViewControl.CMD_SEARCH, Map.of(SearchArguments.TERM, "row"));
		assertTrue(rows().size() <= TableViewControl.MAX_VIEWPORT_ROWS + 2 * PREFETCH_ROWS);
	}

	/**
	 * Tests that a negative start is taken as the first row and an empty viewport as one row.
	 */
	public void testNormalizesInvalidViewport() {
		scroll(-5, 0);
		assertEquals(0, _table.clientState(SCROLL_START));
		assertEquals(1 + PREFETCH_ROWS, rows().size());
	}

	private void scroll(int start, int count) {
		_table.executeClientCommand(CMD_SCROLL,
			Map.of(ScrollArguments.START, Integer.valueOf(start), ScrollArguments.COUNT, Integer.valueOf(count)));
	}

	private List<?> rows() {
		return (List<?>) _table.clientState(ROWS);
	}

	/** Suite requiring the resources the table builds its texts from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableViewport.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
