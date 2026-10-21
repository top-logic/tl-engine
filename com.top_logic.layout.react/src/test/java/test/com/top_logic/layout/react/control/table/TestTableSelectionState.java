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
import com.top_logic.layout.react.control.table.ScrollArguments;
import com.top_logic.layout.react.control.table.SelectAllArguments;
import com.top_logic.layout.react.control.table.SelectRowArguments;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.dirty.ChannelVetoException;
import com.top_logic.layout.react.dirty.StateHandler;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.table.Column;
import com.top_logic.table.Selection;
import com.top_logic.table.SelectionMode;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableViewState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Tests that a {@link TableViewControl} pushes a change of the selection as the selection state
 * alone, and the rows only when they change.
 *
 * <p>
 * Every row carries the state of all its cell controls, so pushing the row window for a selection
 * change would cost the whole window each time.
 * </p>
 */
public class TestTableSelectionState extends TestCase {

	/** The name of the single column, showing the row object itself. */
	private static final String COLUMN_VALUE = "value";

	/** The command behind the select-all checkbox. */
	private static final String CMD_SELECT_ALL = "selectAll";

	/** The command the client scrolls the viewport with. */
	private static final String CMD_SCROLL = "scroll";

	/** State key of the client's row list. */
	private static final String ROWS = "rows";

	/** State key of the indexes of the selected rows among the rows sent. */
	private static final String SELECTED_ROWS = "selectedRows";

	/** State key of the index of the first row sent. */
	private static final String VIEWPORT_START = "viewportStart";

	/** State key of the keyboard cursor row index. */
	private static final String CURSOR_INDEX = "cursorIndex";

	/** How many rows the table has: more than the default viewport and its prefetched rows. */
	private static final int ROW_COUNT = 200;

	/** A control recording the state keys it pushes, so a test can tell what a gesture sent. */
	private static final class TestTable extends TableViewControl<String> {

		/** The keys pushed since the last {@link #clearPushed()}, {@code null} while constructing. */
		private List<String> _pushed;

		TestTable(ReactContext context, com.top_logic.table.TableView<String> view) {
			super(context, view, false);
			_pushed = new ArrayList<>();
		}

		@Override
		protected void putState(String key, Object value) {
			if (_pushed != null) {
				_pushed.add(key);
			}
			super.putState(key, value);
		}

		Object clientState(String key) {
			return getState(key);
		}

		List<String> pushed() {
			return _pushed;
		}

		void clearPushed() {
			_pushed.clear();
		}
	}

	private TestTable _table;

	/** Whether the selection listener refuses the next change. */
	private boolean _veto;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		List<String> elements = new ArrayList<>();
		for (int n = 0; n < ROW_COUNT; n++) {
			elements.add(name(n));
		}
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test"));
		ListRowSource<String> rows = new ListRowSource<>(elements, columns());
		TableViewState state = DefaultTableView.initialState(columns(), SortSpec.NONE, List.of());
		state.setSelection(Selection.none(SelectionMode.MULTI));
		_table = new TestTable(context, new DefaultTableView<>(columns(), rows, state));
		_table.addSelectionListener(selectedKeys -> {
			if (_veto) {
				throw new ChannelVetoException(List.<StateHandler> of(), () -> {
					// Nothing to retry.
				});
			}
		});
		_table.clearPushed();
	}

	/**
	 * Tests that a plain click pushes the selection without the rows.
	 */
	public void testClickPushesNoRows() {
		click(3, false, false);

		assertNoRows();
		assertSelectedRows(3);
		assertEquals(Integer.valueOf(3), _table.clientState(CURSOR_INDEX));
	}

	/**
	 * Tests that a ctrl-click adds a row to the selection without pushing the rows.
	 */
	public void testCtrlClickPushesNoRows() {
		click(3, false, false);
		_table.clearPushed();

		click(5, true, false);

		assertNoRows();
		assertSelectedRows(3, 5);

		_table.clearPushed();
		click(3, true, false);

		assertNoRows();
		assertSelectedRows(5);
	}

	/**
	 * Tests that a shift-click selects a range without pushing the rows.
	 */
	public void testShiftClickPushesNoRows() {
		click(2, false, false);
		_table.clearPushed();

		click(5, false, true);

		assertNoRows();
		assertSelectedRows(2, 3, 4, 5);
	}

	/**
	 * Tests that selecting all rows pushes the selection of the rows sent, not the rows.
	 */
	public void testSelectAllPushesNoRows() {
		_table.executeClientCommand(CMD_SELECT_ALL,
			Map.of(SelectAllArguments.SELECTED, Boolean.TRUE));

		assertNoRows();
		List<?> selected = selectedRows();
		int start = ((Integer) _table.clientState(VIEWPORT_START)).intValue();
		assertEquals("Each of the rows sent is selected.", rows().size(), selected.size());
		assertEquals(Integer.valueOf(start), selected.get(0));

		_table.clearPushed();
		_table.executeClientCommand(CMD_SELECT_ALL,
			Map.of(SelectAllArguments.SELECTED, Boolean.FALSE));

		assertNoRows();
		assertSelectedRows();
	}

	/**
	 * Tests that a programmatic selection of a displayed row pushes no rows.
	 */
	public void testSelectingADisplayedRowPushesNoRows() {
		_table.selectRows(List.of(name(7)));

		assertNoRows();
		assertSelectedRows(7);
	}

	/**
	 * Tests that a programmatic selection of a row outside the viewport scrolls it into view, which
	 * pushes the rows around it.
	 */
	public void testSelectingAFarRowPushesTheRows() {
		int far = ROW_COUNT - 10;
		_table.selectRows(List.of(name(far)));

		assertTrue("The rows are pushed for the scroll into view.", _table.pushed().contains(ROWS));
		assertTrue("The selected row is among the rows sent.",
			rows().stream().anyMatch(row -> Integer.valueOf(far).equals(row.get("index"))));
		assertSelectedRows(far);
	}

	/**
	 * Tests that scrolling pushes the rows and the selection among the rows sent then.
	 */
	public void testScrollPushesRowsAndSelection() {
		click(3, false, false);
		click(150, true, false);
		assertSelectedRows(3);
		_table.clearPushed();

		_table.executeClientCommand(CMD_SCROLL,
			Map.of(ScrollArguments.START, Integer.valueOf(140), ScrollArguments.COUNT, Integer.valueOf(20)));

		assertTrue(_table.pushed().contains(ROWS));
		assertEquals("The first row is no longer among the rows sent.", Integer.valueOf(120),
			_table.clientState(VIEWPORT_START));
		assertSelectedRows(150);
	}

	/**
	 * Tests that a refused selection change leaves the client with the selection it had.
	 */
	public void testVetoKeepsTheSelectionState() {
		click(3, false, false);
		_table.clearPushed();
		_veto = true;

		// The command dispatch answers the refusal with the dialog asking for the unsaved changes.
		click(5, false, false);

		assertEquals(Set.of(name(3)), _table.getSelectedKeys());
		assertNoRows();
		assertSelectedRows(3);
		assertEquals(Integer.valueOf(3), _table.clientState(CURSOR_INDEX));
	}

	private void click(int rowIndex, boolean ctrl, boolean shift) {
		_table.executeClientCommand(TableViewControl.CMD_SELECT, Map.of(
			SelectRowArguments.ROW_INDEX, Integer.valueOf(rowIndex),
			SelectRowArguments.CTRL_KEY, Boolean.valueOf(ctrl),
			SelectRowArguments.SHIFT_KEY, Boolean.valueOf(shift)));
	}

	private void assertNoRows() {
		assertFalse("A selection change pushes no rows: " + _table.pushed(), _table.pushed().contains(ROWS));
	}

	private void assertSelectedRows(int... indexes) {
		List<Integer> expected = new ArrayList<>();
		for (int index : indexes) {
			expected.add(Integer.valueOf(index));
		}
		assertEquals(expected, selectedRows());
	}

	private List<?> selectedRows() {
		return (List<?>) _table.clientState(SELECTED_ROWS);
	}

	/** The rows as pushed to the client. */
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> rows() {
		return (List<Map<String, Object>>) _table.clientState(ROWS);
	}

	private static String name(int n) {
		return String.format("r%03d", Integer.valueOf(n));
	}

	/** The single column showing the row object itself. */
	private static List<Column<String, ?>> columns() {
		return List.<Column<String, ?>> of(DefaultColumn.<String, String> builder(COLUMN_VALUE, row -> row).build());
	}

	/** Suite requiring the resources the table builds its column headers from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableSelectionState.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
