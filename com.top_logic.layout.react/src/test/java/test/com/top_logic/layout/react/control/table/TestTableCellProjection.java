/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.react.control.InteractionWithoutSession;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.identity.ProjectionMode;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.Selection;
import com.top_logic.table.SelectionMode;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableViewState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Tests the cells of the rows in the headless projection of a {@link TableViewControl}: a cell
 * shows text in the projection whether its column renders the text itself or a control built by a
 * {@link CellControlFactory}.
 */
public class TestTableCellProjection extends TestCase {

	/** Column rendering its value as text. */
	private static final String TEXT = "text";

	/** Column rendering its value by a control, with the text the cell shows declared. */
	private static final String CONTROL = "control";

	/** Column rendering its value by a control, without a text. */
	private static final String CONTROL_WITHOUT_TEXT = "controlWithoutText";

	private static final String ROWS = "rows";

	private static final String CELLS = "cells";

	private TableViewControl<String> _table;

	/**
	 * Runs the test within an interaction of a session, as the naming of an object - which asks
	 * the session for script variables naming it - happens in one.
	 */
	@Override
	protected void runTest() throws Throwable {
		InteractionWithoutSession.inSession().runWithContext((ComputationEx2<Void, Throwable, RuntimeException>) () -> {
			super.runTest();
			return null;
		});
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test"));
		List<Column<String, ?>> columns = columns();
		ListRowSource<String> rows = new ListRowSource<>(List.of("Alpha", "Beta"), columns);
		TableViewState state = DefaultTableView.initialState(columns, SortSpec.NONE, List.of());
		state.setSelection(Selection.none(SelectionMode.SINGLE));
		_table = new TableViewControl<>(context, new DefaultTableView<>(columns, rows, state), false);
	}

	/**
	 * A cell rendered by a control is projected by the text its column declares for it, in both
	 * projection modes; a text cell by its text.
	 */
	public void testCellsShowTheirText() {
		for (ProjectionMode mode : ProjectionMode.values()) {
			Map<String, Object> cells = cells(mode, 1);
			assertEquals(mode.name(), "Beta", cells.get(TEXT));
			assertEquals(mode.name(), "shown Beta", cells.get(CONTROL));
		}
	}

	/** A cell rendered by a control whose column declares no text is projected empty. */
	public void testControlCellWithoutTextIsEmpty() {
		assertEquals("", cells(ProjectionMode.AGENT, 0).get(CONTROL_WITHOUT_TEXT));
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> cells(ProjectionMode mode, int row) {
		List<Map<String, Object>> rows = (List<Map<String, Object>>) _table.scriptingScalarState(mode).get(ROWS);
		return (Map<String, Object>) rows.get(row).get(CELLS);
	}

	private static List<Column<String, ?>> columns() {
		return List.<Column<String, ?>> of(
			DefaultColumn.<String, String> builder(TEXT, row -> row).build(),
			DefaultColumn.<String, String> builder(CONTROL, row -> row)
				.renderer(value -> controlCell(value))
				.searchText(value -> "shown " + value)
				.build(),
			DefaultColumn.<String, String> builder(CONTROL_WITHOUT_TEXT, row -> row)
				.renderer(value -> controlCell(value))
				.build());
	}

	private static CellContent controlCell(String value) {
		return new CellContent.Raw((CellControlFactory) context -> new ReactTextControl(context, value));
	}

	/**
	 * The test suite, started with the resource bundles the table's column labels need, and the
	 * {@link ModelResolver} the keys of the rows are built with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableCellProjection.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
				ModelResolver.Module.INSTANCE));
	}

}
