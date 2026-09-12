/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.dnd.DropArguments;
import com.top_logic.layout.react.control.dnd.DropPosition;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.table.DropTargetMode;
import com.top_logic.layout.view.table.TableDropBinding;
import com.top_logic.table.Column;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests the drop target a declared table's {@code <drop>}s produce: which type tags it accepts,
 * whether its rows are targets, and which declared drop applies a drop that arrives.
 *
 * <p>
 * The drop is driven through the control seam the client uses - the {@code drop} command of the
 * receiving table, naming the source table and its dragged row - so what is exercised is the same
 * path a dragged row takes.
 * </p>
 */
public class TestTableDropBinding extends TestCase {

	/** The type the rows of the source table are dragged as. */
	private static final String ROW_TYPE = "demo.test:Row";

	/** A type the source table never drags. */
	private static final String OTHER_TYPE = "demo.test:Other";

	private static final String COLUMN_VALUE = "value";

	private static final List<String> SOURCE_ROWS = List.of("a1", "a2", "a3");

	private static final List<String> TARGET_ROWS = List.of("b1", "b2");

	/** A {@link ViewAction} remembering what the chain handed it. */
	private static final class Recorder implements ViewAction {

		Object _input;

		boolean _executed;

		@Override
		public Object execute(ReactContext context, Object input) {
			_executed = true;
			_input = input;
			return input;
		}
	}

	private ReactContext _context;

	private TableViewControl<String> _source;

	private ViewChannel _targetChannel;

	private Recorder _onTable;

	private Recorder _onRow;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// One context, hence one control registry: the drop resolves its source control out of it.
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue());
		_source = newTable(SOURCE_ROWS);
		_source.setDragSource(ROW_TYPE);
		_targetChannel = new DefaultViewChannel("dropTarget");
		_onTable = new Recorder();
		_onRow = new Recorder();
	}

	private TableViewControl<String> newTable(List<String> rows) {
		List<Column<String, ?>> columns =
			List.of(DefaultColumn.<String, String> builder(COLUMN_VALUE, value -> value).build());
		return new TableViewControl<>(_context,
			DefaultTableView.create(columns, new ListRowSource<>(rows, columns)), false);
	}

	/**
	 * A table accepting the given drops.
	 */
	private TableViewControl<String> newTargetTable(List<TableDropBinding.Drop> drops) {
		TableViewControl<String> target = newTable(TARGET_ROWS);
		target.setDropTarget(new TableDropBinding(_context, drops));
		return target;
	}

	private TableDropBinding.Drop tableDrop(String acceptedTag, Recorder action) {
		return new TableDropBinding.Drop(Set.of(acceptedTag), DropTargetMode.TABLE, null, List.of(action));
	}

	private TableDropBinding.Drop rowDrop(String acceptedTag, Recorder action) {
		return new TableDropBinding.Drop(Set.of(acceptedTag), DropTargetMode.ROW, _targetChannel,
			List.of(action));
	}

	/** The client-side key of the row at the given index of a table. */
	private static String rowKey(int rowIndex) {
		return "row_" + rowIndex;
	}

	/**
	 * Drops the source table's row {@code sourceIndex} on the given table - on its row
	 * {@code targetIndex}, or on the table itself when that is negative.
	 */
	private HandlerResult drop(TableViewControl<String> target, int sourceIndex, int targetIndex) {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put(DropArguments.SOURCE, _source.getID());
		arguments.put(DropArguments.KEYS, rowKey(sourceIndex));
		arguments.put(DropArguments.SELECTION, Boolean.FALSE);
		if (targetIndex >= 0) {
			arguments.put(DropArguments.TARGET_KEY, rowKey(targetIndex));
			arguments.put(DropArguments.POSITION, DropPosition.ONTO.wireName());
		} else {
			arguments.put(DropArguments.POSITION, DropPosition.NONE.wireName());
		}
		return target.executeClientCommand("drop", arguments);
	}

	/**
	 * The tags the table accepts are those of all its drops, and its rows are targets as soon as one
	 * of them targets rows.
	 */
	public void testAcceptedTagsAndRowTargeting() {
		TableDropBinding both = new TableDropBinding(_context,
			List.of(tableDrop(ROW_TYPE, _onTable), rowDrop(OTHER_TYPE, _onRow)));

		assertEquals(Set.of(ROW_TYPE, OTHER_TYPE), Set.copyOf(both.acceptedTypes()));
		assertTrue("A drop targeting rows makes the rows targets.", both.dropOnRows());

		TableDropBinding onTableOnly =
			new TableDropBinding(_context, List.of(tableDrop(ROW_TYPE, _onTable)));
		assertEquals(Set.of(ROW_TYPE), Set.copyOf(onTableOnly.acceptedTypes()));
		assertFalse("Without a row drop the rows are no targets.", onTableOnly.dropOnRows());
	}

	/**
	 * A drop on the table runs the chain of the table drop, with the dragged objects as its input.
	 */
	public void testTableDropRunsItsChain() {
		TableViewControl<String> target = newTargetTable(List.of(tableDrop(ROW_TYPE, _onTable)));

		assertTrue(drop(target, 1, -1).isSuccess());

		assertTrue("The chain of the table drop must run.", _onTable._executed);
		assertEquals("The chain receives the dragged objects.", List.of(SOURCE_ROWS.get(1)), _onTable._input);
		assertNull("A table drop has no target row to publish.", _targetChannel.get());
	}

	/**
	 * A drop on a row runs the chain of the row drop, and the row dropped on reaches the chain
	 * through the drop's target channel.
	 */
	public void testRowDropPublishesTheTargetRow() {
		TableViewControl<String> target =
			newTargetTable(List.of(tableDrop(ROW_TYPE, _onTable), rowDrop(ROW_TYPE, _onRow)));

		assertTrue(drop(target, 2, 1).isSuccess());

		assertTrue("The chain of the row drop must run.", _onRow._executed);
		assertFalse("The table drop must not run as well.", _onTable._executed);
		assertEquals(List.of(SOURCE_ROWS.get(2)), _onRow._input);
		assertEquals("The row dropped on is published before the chain runs.",
			TARGET_ROWS.get(1), _targetChannel.get());
	}

	/**
	 * A drop on a row whose objects no row drop accepts falls back to the table drop - a table
	 * accepting one kind of object on its rows still accepts another as a whole, wherever the
	 * pointer happened to be.
	 */
	public void testARowDropFallsBackToTheTableDrop() {
		TableViewControl<String> target =
			newTargetTable(List.of(rowDrop(OTHER_TYPE, _onRow), tableDrop(ROW_TYPE, _onTable)));

		assertTrue(drop(target, 0, 1).isSuccess());

		assertTrue("The table drop applies what the row drop does not accept.", _onTable._executed);
		assertFalse("The row drop accepts another type.", _onRow._executed);
		assertNull("The fallback is a drop on the table, which publishes no row.", _targetChannel.get());
	}

	/**
	 * A drag of a type no drop accepts is refused by the table before any chain is asked - the
	 * client is told the accepted tags, so it never offers such a drop in the first place.
	 */
	public void testAnUnacceptedTypeRunsNothing() {
		TableViewControl<String> target =
			newTargetTable(List.of(tableDrop(OTHER_TYPE, _onTable), rowDrop(OTHER_TYPE, _onRow)));

		assertFalse("A drop of an unaccepted type must be refused.", drop(target, 0, -1).isSuccess());

		assertFalse(_onTable._executed);
		assertFalse(_onRow._executed);
	}

	/**
	 * Dragging a selected row drags the whole selection, and the chain gets all of it.
	 */
	public void testTheSelectionIsDropped() {
		TableViewControl<String> target = newTargetTable(List.of(tableDrop(ROW_TYPE, _onTable)));
		_source.selectRow(SOURCE_ROWS.get(0));

		Map<String, Object> arguments = new HashMap<>();
		arguments.put(DropArguments.SOURCE, _source.getID());
		arguments.put(DropArguments.KEYS, rowKey(0));
		arguments.put(DropArguments.SELECTION, Boolean.TRUE);
		arguments.put(DropArguments.POSITION, DropPosition.NONE.wireName());
		assertTrue(target.executeClientCommand("drop", arguments).isSuccess());

		assertEquals(List.of(SOURCE_ROWS.get(0)), _onTable._input);
	}

	/**
	 * The test suite, started with the resource bundles a table's column labels need.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableDropBinding.class, ResourcesModule.Module.INSTANCE));
	}

}
