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
import com.top_logic.table.Selection;
import com.top_logic.table.SelectionMode;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableViewState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Tests for {@link TableSelectionBinding}.
 *
 * <p>
 * Tables over disjoint row sets share one selection channel, which is what the binding has to get
 * right: each table displays the value where it has a row for it, and no table clears a value
 * another one put there. A table selecting any number of rows displays a set of keys as the
 * selection of those rows; one selecting a single row cannot display such a value at all.
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

	/** A row of the table selecting any number of rows. */
	private static final String M1 = "m1";

	/** A row of the table selecting any number of rows. */
	private static final String M2 = "m2";

	private ReactContext _context;

	private ViewChannel _channel;

	private ListRowSource<String> _rowsA;

	private ListRowSource<String> _rowsB;

	private TableViewControl<String> _tableA;

	private TableViewControl<String> _tableB;

	private TableSelectionBinding _bindingA;

	private TableSelectionBinding _bindingB;

	private ListRowSource<String> _rowsM;

	private TableViewControl<String> _tableM;

	private TableSelectionBinding _bindingM;

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
	 * Tests that a set of row keys is displayed as the selection of all those rows by a table
	 * selecting any number of them, and that the table's echo does not rewrite the value.
	 */
	public void testSetSelectsEveryRowThatHasIt() {
		setUpMultiTable(M1, M2);

		_channel.set(Set.of(M1, M2));

		assertEquals(Set.of(M1, M2), _tableM.getSelectedKeys());
		assertEquals("The value is the selection, and the table only displays it.",
			Set.of(M1, M2), _channel.get());
	}

	/**
	 * Tests that a set naming a row the table does not have selects the rows it does have, and
	 * leaves the value alone - the missing row is somebody else's to display.
	 */
	public void testSetWithForeignKeySelectsTheRowsThatArePresent() {
		setUpMultiTable(M1, M2);

		Set<String> value = Set.of(M1, ELSEWHERE);
		_channel.set(value);

		assertEquals(Set.of(M1), _tableM.getSelectedKeys());
		assertEquals("A key no table has a row for is nobody's to drop.", value, _channel.get());
	}

	/**
	 * Tests how a selection made in a table selecting any number of rows reaches the channel: as
	 * the set while there are several, as the object while there is one, as nothing while there is
	 * none.
	 */
	public void testSelectionOfSeveralRowsIsWrittenAsASet() {
		setUpMultiTable(M1, M2);

		_tableM.selectRows(Set.of(M1, M2));
		assertEquals(Set.of(M1, M2), _channel.get());

		_tableM.selectRow(M2);
		assertEquals("One selected row is the object, whatever the table's mode.", M2, _channel.get());

		_tableM.selectRow(null);
		assertNull(_channel.get());
	}

	/**
	 * Tests that a table selecting one row at a time cannot display a set - not even one naming its
	 * own rows - and does not take it from the table that can.
	 */
	public void testTableSelectingOneRowIgnoresASet() {
		setUpMultiTable(M1, M2);

		Set<String> value = Set.of(A1, A2);
		_channel.set(value);

		assertEquals("A table selecting one row at a time has no way of showing several.",
			Set.of(), _tableA.getSelectedKeys());
		assertEquals("The value belongs to whoever wrote it.", value, _channel.get());
	}

	/**
	 * Tests that a refresh that takes some of the displayed selected rows away rewrites the value
	 * to what is left of the selection, and clears it once nothing is left.
	 */
	public void testVanishedRowsLeaveTheSurvivingSelection() {
		setUpMultiTable(M1, M2);
		_tableM.selectRows(Set.of(M1, M2));

		refreshM(M1);

		assertEquals("One row of the selection is left, so it is the selection.", M1, _channel.get());
		assertEquals(Set.of(M1), _tableM.getSelectedKeys());

		refreshM();

		assertNull("Nothing of the selection is left.", _channel.get());
		assertEquals(Set.of(), _tableM.getSelectedKeys());
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
		if (_bindingM != null) {
			_bindingM.dispose();
		}
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

	/** A table over the given rows, selecting one row at a time. */
	private TableViewControl<String> table(ListRowSource<String> rows) {
		return table(rows, SelectionMode.SINGLE);
	}

	/** A table over the given rows, selecting rows in the given mode. */
	private TableViewControl<String> table(ListRowSource<String> rows, SelectionMode mode) {
		List<Column<String, ?>> columns = columns();
		TableViewState state = DefaultTableView.initialState(columns, SortSpec.NONE, Set.of());
		state.setSelection(Selection.none(mode));
		return new TableViewControl<>(_context, new DefaultTableView<>(columns, rows, state), false);
	}

	/**
	 * Adds a table selecting any number of rows over the given elements to the shared channel.
	 */
	private void setUpMultiTable(String... elements) {
		_rowsM = rows(elements);
		_tableM = table(_rowsM, SelectionMode.MULTI);
		_bindingM = new TableSelectionBinding(_tableM, _channel);
	}

	/** Refreshes the rows of the table selecting any number of rows, as its owner does. */
	private void refreshM(String... elements) {
		refresh(_rowsM, _tableM, _bindingM, elements);
	}

	/** Suite requiring the resources the table builds its column headers from. */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableSelectionBinding.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
