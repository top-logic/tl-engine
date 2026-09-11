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
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.table.ActivateRowArguments;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.table.Column;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests the row activation of a {@link TableViewControl}: the gesture that opens a row (a
 * double-click, or {@code Enter} on the cursor row) selects that row and runs the
 * {@link TableViewControl#setActivationHandler(TableViewControl.ActivationHandler) activation
 * handler} with its business object.
 */
public class TestTableActivation extends TestCase {

	/** The name of the single column, showing the row object itself. */
	private static final String COLUMN_VALUE = "value";

	/** The first row. */
	private static final String A = "a";

	/** The second row, the one the tests open. */
	private static final String B = "b";

	/** The third row. */
	private static final String C = "c";

	/** An index no row has. */
	private static final int UNKNOWN_ROW = 17;

	private TableViewControl<String> _table;

	/** The rows handed to the activation handler, in activation order. */
	private List<String> _activated;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue());
		ListRowSource<String> rows = new ListRowSource<>(new ArrayList<>(List.of(A, B, C)), columns());
		_table = new TableViewControl<>(context, DefaultTableView.create(columns(), rows), false);

		_activated = new ArrayList<>();
		_table.setActivationHandler(row -> {
			_activated.add(row);
			return HandlerResult.DEFAULT_RESULT;
		});
	}

	/**
	 * Tests that activating a row makes it the selection and hands its business object to the
	 * handler - in that order, so the handler already sees the selection its command may read.
	 */
	public void testActivationSelectsTheRowAndRunsTheHandler() {
		HandlerResult result = activate(1);

		assertTrue(result.isSuccess());
		assertEquals(Set.of(B), _table.getSelectedKeys());
		assertEquals(List.of(B), _activated);
	}

	/**
	 * Tests that an index no row has activates nothing at all - neither the handler runs, nor does
	 * the selection change.
	 */
	public void testUnknownRowActivatesNothing() {
		activate(0);
		_activated.clear();

		activate(UNKNOWN_ROW);

		assertEquals("Nothing was opened.", List.of(), _activated);
		assertEquals("The selection of the row opened before is untouched.",
			Set.of(A), _table.getSelectedKeys());
	}

	/**
	 * Tests that a table without an activation handler still selects the activated row: opening a
	 * row is a selecting gesture, whether or not something is configured to answer it.
	 */
	public void testActivationWithoutHandlerSelects() {
		_table.setActivationHandler(null);

		HandlerResult result = activate(2);

		assertTrue(result.isSuccess());
		assertEquals(Set.of(C), _table.getSelectedKeys());
		assertEquals(List.of(), _activated);
	}

	/**
	 * Tests that what the handler reports is what the gesture reports: a command that fails makes
	 * the activation fail, so a scripted replay sees it.
	 */
	public void testHandlerFailureIsReported() {
		_table.setActivationHandler(row -> HandlerResult.error(ResKey.text("Denied.")));

		HandlerResult result = activate(1);

		assertFalse(result.isSuccess());
		assertEquals("The row is selected even though opening it failed.",
			Set.of(B), _table.getSelectedKeys());
	}

	/** Sends the client's activate command for the row at the given index. */
	private HandlerResult activate(int rowIndex) {
		return _table.executeClientCommand(TableViewControl.CMD_ACTIVATE,
			Map.of(ActivateRowArguments.ROW_INDEX, Integer.valueOf(rowIndex)));
	}

	/** The single column showing the row object itself. */
	private static List<Column<String, ?>> columns() {
		return List.<Column<String, ?>> of(DefaultColumn.<String, String> builder(COLUMN_VALUE, row -> row).build());
	}

	/** Suite requiring the resources the table builds its column headers from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableActivation.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
