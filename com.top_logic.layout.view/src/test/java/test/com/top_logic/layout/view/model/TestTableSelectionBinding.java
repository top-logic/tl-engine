/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.TableSelectionBinding;
import com.top_logic.table.Column;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Tests for {@link TableSelectionBinding}.
 *
 * <p>
 * Two tables over disjoint row sets share one selection channel, which is what the binding has to
 * get right: each table displays the value where it has a row for it, and neither table clears a
 * value the other one put there.
 * </p>
 */
public class TestTableSelectionBinding extends TestCase {

	/** The name of the single column, showing the row object itself. */
	private static final String COLUMN_VALUE = "value";

	/** A row of the first table. */
	private static final String A1 = "a1";

	/** A row of the first table. */
	private static final String A2 = "a2";

	/** A row of the second table. */
	private static final String B1 = "b1";

	/** An object no table has a row for. */
	private static final String ELSEWHERE = "elsewhere";

	/** A row that appears in the first table only after a refresh. */
	private static final String CREATED = "created";

	private ReactContext _context;

	private ViewChannel _channel;

	private ListRowSource<String> _rowsA;

	private ListRowSource<String> _rowsB;

	private TableViewControl<String> _tableA;

	private TableViewControl<String> _tableB;

	private TableSelectionBinding _bindingA;

	private TableSelectionBinding _bindingB;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultReactContext("", "test", new SSEUpdateQueue());
		_channel = new DefaultViewChannel("selection");

		_rowsA = rows(A1, A2);
		_tableA = table(_rowsA);
		_bindingA = new TableSelectionBinding(_tableA, _channel);

		_rowsB = rows(B1);
		_tableB = table(_rowsB);
		_bindingB = new TableSelectionBinding(_tableB, _channel);
	}

	/**
	 * Tests that a value written to the channel from outside is displayed by the table having a row
	 * for it, and by no other.
	 */
	public void testChannelValueSelectsTheRowThatHasIt() {
		_channel.set(A2);

		assertEquals(Set.of(A2), _tableA.getSelectedKeys());
		assertEquals("A table without such a row shows no selection.", Set.of(), _tableB.getSelectedKeys());
		assertEquals("The value is the selection, whoever displays it.", A2, _channel.get());
	}

	/**
	 * Tests that a selection made in one table becomes the channel value, and that the other table
	 * gives up its selection for it.
	 */
	public void testSelectionInOneTableReplacesTheValue() {
		_channel.set(A1);

		_tableB.selectRow(B1);

		assertEquals(B1, _channel.get());
		assertEquals(Set.of(B1), _tableB.getSelectedKeys());
		assertEquals("The row selected elsewhere is not the selection any more.",
			Set.of(), _tableA.getSelectedKeys());
	}

	/**
	 * Tests that a value no table has a row for stays on the channel - it is the selection of
	 * whoever wrote it, and the tables only fail to display it.
	 */
	public void testValueOfNoTableIsKept() {
		_channel.set(ELSEWHERE);

		assertEquals(ELSEWHERE, _channel.get());
		assertEquals(Set.of(), _tableA.getSelectedKeys());
		assertEquals(Set.of(), _tableB.getSelectedKeys());
	}

	/**
	 * Tests that a value whose row appears only with a refresh is displayed as soon as it does - the
	 * object a create command wrote to the channel before the rows caught up with it.
	 */
	public void testValueIsDisplayedWhenItsRowAppears() {
		_channel.set(CREATED);
		assertEquals("Not among the rows yet.", Set.of(), _tableA.getSelectedKeys());

		refreshA(A1, A2, CREATED);

		assertEquals(Set.of(CREATED), _tableA.getSelectedKeys());
		assertEquals(CREATED, _channel.get());
	}

	/**
	 * Tests that only the table that displayed the vanished row clears the channel: the other one
	 * refreshes without ever having had a row for the value, and leaves it alone.
	 */
	public void testVanishedDisplayedRowClearsTheValue() {
		_channel.set(A1);
		assertEquals(Set.of(A1), _tableA.getSelectedKeys());

		refreshB(B1);

		assertEquals("A table that never displayed the value has nothing to report about it.",
			A1, _channel.get());

		refreshA(A2);

		assertNull("The displayed row is gone, so nothing is selected any more.", _channel.get());
		assertEquals(Set.of(), _tableA.getSelectedKeys());
	}

	/**
	 * Tests that giving up the selection in the table clears the channel.
	 */
	public void testDeselectionClearsTheValue() {
		_channel.set(A1);

		_tableA.selectRow(null);

		assertNull(_channel.get());
	}

	/**
	 * Tests that a disposed binding leaves its table alone.
	 */
	public void testDisposedBindingDoesNotDisplayAnything() {
		_bindingA.dispose();

		_channel.set(A1);

		assertEquals(Set.of(), _tableA.getSelectedKeys());
		assertEquals("The remaining binding still works.", A1, _channel.get());
		assertEquals(Set.of(), _tableB.getSelectedKeys());
	}

	@Override
	protected void tearDown() throws Exception {
		_bindingA.dispose();
		_bindingB.dispose();
		super.tearDown();
	}

	/** Refreshes the first table's rows to the given elements, as its owner does. */
	private void refreshA(String... elements) {
		refresh(_rowsA, _tableA, _bindingA, elements);
	}

	/** Refreshes the second table's rows to the given elements, as its owner does. */
	private void refreshB(String... elements) {
		refresh(_rowsB, _tableB, _bindingB, elements);
	}

	private static void refresh(ListRowSource<String> rows, TableViewControl<String> table,
			TableSelectionBinding binding, String... elements) {
		rows.setElements(new ArrayList<>(List.of(elements)));
		table.refreshData();
		binding.rowsRefreshed();
	}

	/** A row source over the given elements, each being its own row key. */
	private static ListRowSource<String> rows(String... elements) {
		return new ListRowSource<>(new ArrayList<>(List.of(elements)), columns());
	}

	/** The single column showing the row object itself. */
	private static List<Column<String, ?>> columns() {
		return List.<Column<String, ?>> of(DefaultColumn.<String, String> builder(COLUMN_VALUE, row -> row).build());
	}

	/** A table over the given rows. */
	private TableViewControl<String> table(ListRowSource<String> rows) {
		return new TableViewControl<>(_context, DefaultTableView.create(columns(), rows), false);
	}

	/** Suite requiring the resources the table builds its column headers from. */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableSelectionBinding.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
