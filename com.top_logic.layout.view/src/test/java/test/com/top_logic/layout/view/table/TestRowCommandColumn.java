/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.table.RowCommandColumn;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Tests the columns {@link RowCommandColumn} builds: one button per row, running a command with
 * that row, offered according to the command's executability for it.
 */
public class TestRowCommandColumn extends TestCase {

	/** The client command a button press sends. */
	private static final String CMD_CLICK = "click";

	/** A row the command accepts. */
	private static final String ACCEPTED = "accepted";

	/** A row the command rejects. */
	private static final String REJECTED = "rejected";

	/** A row the command hides itself for. */
	private static final String HIDDEN = "hidden";

	/** The icon a command without one of its own falls back to. */
	private static final ThemeImage DEFAULT_IMAGE = ThemeImage.icon("css:fas fa-chevron-right");

	/** The label a command without one of its own falls back to. */
	private static final ResKey DEFAULT_LABEL = ResKey.text("Open");

	/** The rows the command was run with, in execution order. */
	private List<Object> _executed;

	private ReactContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_executed = new ArrayList<>();
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue());
	}

	/**
	 * Tests that the column trails the table and is none of the user's to arrange: pinned to the
	 * end, not offered by the column selection, never frozen, and as wide as one icon button.
	 */
	public void testColumnIsPinnedAndNotArrangeable() {
		Column<String, String> column = column(ViewExecutabilityRule.ALWAYS_EXECUTABLE);

		assertTrue("A command column is pinned to the end of the table.", column.pinnedEnd());
		assertFalse("A command column holds no data to choose.", column.selectable());
		assertFalse("A pinned column is never part of the frozen prefix.", column.frozenEligible());
		assertEquals(RowCommandColumn.WIDTH, column.defaultWidth());
		assertEquals("A command column carries no header label.", ResKey.text(""), column.label());
	}

	/**
	 * Tests that pressing the button of a row runs the command with that row.
	 */
	public void testButtonRunsTheCommandWithItsRow() {
		Column<String, String> column = column(ViewExecutabilityRule.ALWAYS_EXECUTABLE);

		HandlerResult result = click(button(column, ACCEPTED));

		assertTrue(result.isSuccess());
		assertEquals(List.of(ACCEPTED), _executed);
	}

	/**
	 * Tests that a row the command's rules reject shows a button that cannot be pressed.
	 */
	public void testRejectedRowShowsADisabledButton() {
		Column<String, String> column = column(rule());

		HandlerResult result = click(button(column, REJECTED));

		assertFalse("The button of a rejected row is disabled.", result.isSuccess());
		assertEquals("A disabled button runs nothing.", List.of(), _executed);
	}

	/**
	 * Tests that a row the command's rules hide it for shows no button at all.
	 */
	public void testHiddenRowShowsNoButton() {
		Column<String, String> column = column(rule());

		assertEquals("A row the command is hidden for has an empty cell.",
			CellContent.EMPTY, column.renderCell(HIDDEN));
	}

	/**
	 * Tests that the executability is decided per row, not once for the column: the same column
	 * offers the command on one row and withholds it on the next.
	 */
	public void testExecutabilityIsDecidedPerRow() {
		Column<String, String> column = column(rule());

		assertTrue(click(button(column, ACCEPTED)).isSuccess());
		assertFalse(click(button(column, REJECTED)).isSuccess());
		assertEquals(CellContent.EMPTY, column.renderCell(HIDDEN));
		assertEquals(List.of(ACCEPTED), _executed);
	}

	/**
	 * Tests that every command becomes a column of its own, in the given order.
	 */
	public void testOneColumnPerCommand() {
		List<Column<String, String>> columns = RowCommandColumn.columns(_context,
			List.of(rowCommand(ViewExecutabilityRule.ALWAYS_EXECUTABLE),
				rowCommand(ViewExecutabilityRule.ALWAYS_EXECUTABLE)));

		assertEquals("A cell holds one button, so two commands are two columns.", 2, columns.size());
		assertFalse("The columns are told apart by their names.",
			columns.get(0).name().equals(columns.get(1).name()));
		for (Column<String, String> column : columns) {
			assertTrue(column.pinnedEnd());
		}
	}

	/** The rule deciding by the row: {@link #REJECTED} disabled, {@link #HIDDEN} invisible. */
	private static ViewExecutabilityRule rule() {
		return input -> {
			if (REJECTED.equals(input)) {
				return ExecutableState.NOT_EXEC_DISABLED;
			}
			if (HIDDEN.equals(input)) {
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
			return ExecutableState.EXECUTABLE;
		};
	}

	/** The single column offering the command guarded by the given rule. */
	private Column<String, String> column(ViewExecutabilityRule rule) {
		List<Column<String, String>> columns = RowCommandColumn.columns(_context, List.of(rowCommand(rule)));
		assertEquals(1, columns.size());
		return columns.get(0);
	}

	/** The command recording the rows it is run with, guarded by the given rule. */
	private RowCommandColumn.RowCommand rowCommand(ViewExecutabilityRule rule) {
		ViewCommand command = (context, input) -> {
			_executed.add(input);
			return HandlerResult.DEFAULT_RESULT;
		};
		ViewCommandModel model = new ViewCommandModel(command,
			TypedConfiguration.newConfigItem(ViewCommand.Config.class), null, rule);
		return new RowCommandColumn.RowCommand(model, DEFAULT_IMAGE, DEFAULT_LABEL);
	}

	/** The button the column's cell holds for the given row. */
	private ReactButtonControl button(Column<String, String> column, String row) {
		CellContent content = column.renderCell(row);
		assertTrue("The cell carries a control factory: " + content, content instanceof CellContent.Raw);
		Object payload = ((CellContent.Raw) content).payload();
		assertTrue("The payload builds the cell control: " + payload, payload instanceof CellControlFactory);

		ReactControl control = ((CellControlFactory) payload).create(_context);
		assertTrue("The cell control is a button: " + control, control instanceof ReactButtonControl);
		return (ReactButtonControl) control;
	}

	/** Presses the given button the way the client does. */
	private static HandlerResult click(ReactButtonControl button) {
		return button.executeClientCommand(CMD_CLICK, Map.of());
	}

	/** Suite requiring the resources a button resolves its label from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestRowCommandColumn.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
