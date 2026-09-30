/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.dirty.ChannelVetoException;
import com.top_logic.layout.react.dirty.StateHandler;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.table.Column;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Tests that a selection change a
 * {@link TableViewControl.SelectionListener selection listener} refuses leaves the
 * {@link TableViewControl} showing and holding the selection it had before.
 *
 * <p>
 * The refusal is the unsaved changes of a form the selection would replace, reaching the table as a
 * {@link ChannelVetoException}.
 * </p>
 */
public class TestTableSelectionVeto extends TestCase {

	/** The name of the single column, showing the row object itself. */
	private static final String COLUMN_VALUE = "value";

	/** The first row. */
	private static final String A = "a";

	/** The second row, the one the refused changes try to select. */
	private static final String B = "b";

	/** The third row. */
	private static final String C = "c";

	/**
	 * A {@link StateHandler} stub standing for the unsaved changes that block a selection change.
	 */
	private static class StubHandler implements StateHandler {

		boolean _dirty = true;

		@Override
		public boolean isDirty() {
			return _dirty;
		}

		@Override
		public boolean hasErrors() {
			return false;
		}

		@Override
		public void executeSave() {
			_dirty = false;
		}

		@Override
		public void executeDiscard() {
			_dirty = false;
		}

		@Override
		public String getDescription() {
			return "stub";
		}
	}

	private ListRowSource<String> _rows;

	private DefaultTableView<String> _view;

	private TableViewControl<String> _table;

	/** The unsaved changes the listener refuses a selection change with while they are dirty. */
	private StubHandler _handler;

	/** The selections the listener was told about, in notification order. */
	private List<Set<Object>> _notified;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_rows = new ListRowSource<>(new ArrayList<>(List.of(A, B, C)), columns());
		_view = DefaultTableView.create(columns(), _rows);
		_table = new TableViewControl<>(context, _view, false);

		_handler = new StubHandler();
		_notified = new ArrayList<>();
		_table.addSelectionListener(selectedKeys -> {
			if (_handler.isDirty()) {
				throw new ChannelVetoException(List.<StateHandler> of(_handler), () -> {
					// The retry the resolved unsaved changes run.
				});
			}
			_notified.add(new LinkedHashSet<>(selectedKeys));
		});
	}

	/**
	 * Tests that the refused change reaches the caller and leaves the table with the selection it
	 * displayed before, the {@link DefaultTableView} included.
	 */
	public void testRefusedChangeKeepsThePreviousSelection() {
		_handler.executeDiscard();
		_table.selectRow(A);
		_handler._dirty = true;

		try {
			_table.selectRow(B);
			fail("Expected ChannelVetoException");
		} catch (ChannelVetoException ex) {
			assertEquals("The unsaved changes that refused the selection are reported.",
				List.<StateHandler> of(_handler), ex.getDirtyHandlers());
		}

		assertEquals("The table holds the row it displays.", Set.of(A), _table.getSelectedKeys());
		assertEquals("The table view holds the row the table displays.",
			Set.<Object> of(A), _view.state().getSelection().keys());
		assertEquals("Only the accepted change was notified.", List.of(Set.of(A)), _notified);
	}

	/**
	 * Tests that the very first selection is refused back to no selection at all.
	 */
	public void testRefusedFirstChangeKeepsNothingSelected() {
		try {
			_table.selectRow(B);
			fail("Expected ChannelVetoException");
		} catch (ChannelVetoException ex) {
			// Expected.
		}

		assertEquals(Set.of(), _table.getSelectedKeys());
		assertEquals(Set.of(), _view.state().getSelection().keys());
	}

	/**
	 * Tests that the change goes through once the unsaved changes are resolved - what the retry
	 * after a discard does.
	 */
	public void testResolvedChangeSucceeds() {
		try {
			_table.selectRow(B);
			fail("Expected ChannelVetoException");
		} catch (ChannelVetoException ex) {
			// Expected.
		}

		_handler.executeDiscard();
		_table.selectRow(B);

		assertEquals(Set.of(B), _table.getSelectedKeys());
		assertEquals(Set.<Object> of(B), _view.state().getSelection().keys());
		assertEquals(List.of(Set.of(B)), _notified);
	}

	/**
	 * Tests that a listener refusing nothing leaves the selection exactly as it is made.
	 */
	public void testAcceptedChangesSelectAsAsked() {
		_handler.executeDiscard();

		_table.selectRow(A);
		_table.selectRow(C);

		assertEquals(Set.of(C), _table.getSelectedKeys());
		assertEquals(Set.<Object> of(C), _view.state().getSelection().keys());
		assertEquals(List.of(Set.of(A), Set.of(C)), _notified);
	}

	/**
	 * Tests that a row gone from the table does not come back through a refused change: the refresh
	 * drops it from the selection without asking the listeners, so it is not what the table
	 * displays any more.
	 */
	public void testVanishedRowIsNotRestored() {
		_handler.executeDiscard();
		_table.selectRow(A);

		_rows.setElements(new ArrayList<>(List.of(B, C)));
		_table.refreshData();
		assertEquals("The refresh gives up the row it cannot display.", Set.of(), _table.getSelectedKeys());

		_handler._dirty = true;
		try {
			_table.selectRow(B);
			fail("Expected ChannelVetoException");
		} catch (ChannelVetoException ex) {
			// Expected.
		}

		assertEquals("The vanished row must not be selected again.", Set.of(), _table.getSelectedKeys());
		assertEquals(Set.of(), _view.state().getSelection().keys());
	}

	/** The single column showing the row object itself. */
	private static List<Column<String, ?>> columns() {
		return List.<Column<String, ?>> of(DefaultColumn.<String, String> builder(COLUMN_VALUE, row -> row).build());
	}

	/** Suite requiring the resources the table builds its column headers from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableSelectionVeto.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
