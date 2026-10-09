/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.dnd.DropArguments;
import com.top_logic.layout.react.control.dnd.DropPosition;
import com.top_logic.layout.react.control.dnd.DropProbeArguments;
import com.top_logic.layout.react.control.dnd.DropSupport;
import com.top_logic.layout.react.control.kanban.ReactKanbanBoardControl;
import com.top_logic.layout.react.control.kanban.ReactKanbanBoardControl.Card;
import com.top_logic.layout.react.control.kanban.ReactKanbanBoardControl.Column;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.dnd.DropBinding;
import com.top_logic.layout.view.dnd.DropScope;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Tests dragging cards of a {@link ReactKanbanBoardControl} onto its columns: a drop on another
 * column runs the action chain of the matching declared drop with the column value as its target,
 * and a reorder function receives the new order of the column - after the chain for a drop on
 * another column, alone for a drop within a column.
 *
 * <p>
 * The drop is driven through the control seam the client uses - the {@code drop} command of the
 * board, naming the board as the source of the dragged card.
 * </p>
 */
public class TestKanbanBoardDragDrop extends TestCase {

	/** The type the cards are dragged as. */
	private static final String CARD_TYPE = "demo.test:Card";

	/** A type the board never drags. */
	private static final String OTHER_TYPE = "demo.test:Other";

	private static final String OPEN = "open";

	private static final String DONE = "done";

	private static final String A1 = "a1";

	private static final String A2 = "a2";

	private static final String B1 = "b1";

	/** A {@link ViewAction} remembering what the chain handed it, and when it ran. */
	private final class Recorder implements ViewAction {

		Object _input;

		boolean _executed;

		@Override
		public Object execute(ReactContext context, Object input) {
			_executed = true;
			_input = input;
			_sequence.add("chain");
			return input;
		}
	}

	/** What ran, in order: the drop chain and the reorder function. */
	private final List<Object> _sequence = new ArrayList<>();

	private ReactContext _context;

	private ReactKanbanBoardControl _board;

	private ViewChannel _targetChannel;

	private Recorder _onColumn;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_board = new ReactKanbanBoardControl(_context);
		_board.setColumns(List.of(
			new Column(OPEN, OPEN, List.of(card(A1), card(A2))),
			new Column(DONE, DONE, List.of(card(B1)))));
		_board.setDragSource(CARD_TYPE, null);
		// The board is displayed: only a displayed control can be addressed as the source of a drag.
		_board.attach();
		_targetChannel = new DefaultViewChannel("dropColumn");
		_onColumn = new Recorder();
	}

	private Card card(String item) {
		return new Card(item, new ReactControl(_context, null, "TLText"));
	}

	private DropBinding.Drop columnDrop(String acceptedTag) {
		return new DropBinding.Drop(Set.of(acceptedTag), DropScope.ITEM, _targetChannel, List.of(_onColumn));
	}

	/**
	 * A drop on another column runs the chain of the column drop with the dragged objects, and the
	 * column value is published on the drop's target channel before.
	 */
	public void testDropOnColumnRunsChain() {
		_board.setDropTarget(new DropBinding(_context, List.of(columnDrop(CARD_TYPE))));

		assertTrue(drop(A1, columnKey(1), DropPosition.NONE).isSuccess());

		assertTrue("The chain of the column drop must run.", _onColumn._executed);
		assertEquals("The chain receives the dragged objects.", List.of(A1), _onColumn._input);
		assertEquals("The column value is the target of the drop.", DONE, _targetChannel.get());
	}

	/**
	 * A refusal function refuses the drop with its reason: the chain does not run, and a probe
	 * answers the reason.
	 */
	public void testRefuseIfBlocksWithReason() {
		_board.setDropTarget(new DropBinding(_context, List.of(new DropBinding.Drop(
			Set.of(CARD_TYPE), DropScope.ITEM, _targetChannel, List.of(_onColumn),
			() -> ExecutableState.EXECUTABLE, ViewExecutabilityRule.ALWAYS_EXECUTABLE,
			(column, objects) -> DONE.equals(column) ? "Not here." : null))));

		assertFalse("The drop must be refused.", drop(A1, columnKey(1), DropPosition.NONE).isSuccess());
		assertFalse("A refused drop runs no chain.", _onColumn._executed);

		Map<?, ?> verdict = probe(A1, columnKey(1), DropPosition.NONE);
		assertEquals(Boolean.FALSE, verdict.get(DropSupport.VERDICT_ACCEPTED));
		assertEquals("Not here.", verdict.get(DropSupport.VERDICT_REASON));
	}

	/**
	 * A drag of a type no drop accepts is refused, and no chain runs.
	 */
	public void testUnacceptedTypeIsRefused() {
		_board.setDropTarget(new DropBinding(_context, List.of(columnDrop(OTHER_TYPE))));

		assertFalse(drop(A1, columnKey(1), DropPosition.NONE).isSuccess());
		assertFalse(_onColumn._executed);
		assertEquals(Boolean.FALSE, probe(A1, columnKey(1), DropPosition.NONE).get(DropSupport.VERDICT_ACCEPTED));
	}

	/**
	 * A drop on another column beside a card runs the chain first and then the reorder function,
	 * which receives the column with the dropped object at the drop position.
	 */
	public void testColumnChangeThenReorder() {
		List<Object> reorders = new ArrayList<>();
		_board.setDropTarget(new DropBinding(_context, List.of(columnDrop(CARD_TYPE))));
		_board.setReorder((column, objects) -> {
			_sequence.add("reorder");
			reorders.add(column);
			reorders.add(objects);
		});

		assertTrue(drop(A1, cardKey(B1), DropPosition.BEFORE).isSuccess());

		assertEquals("The chain runs before the reorder function.", List.of("chain", "reorder"), _sequence);
		assertEquals(List.of(DONE, List.of(A1, B1)), reorders);
		assertEquals(DONE, _targetChannel.get());
	}

	/**
	 * A drop within a column runs the reorder function alone; without one it is refused.
	 */
	public void testReorderWithinColumn() {
		_board.setDropTarget(new DropBinding(_context, List.of(columnDrop(CARD_TYPE))));

		assertFalse("Without a reorder function, a drop within a column is refused.",
			drop(A2, cardKey(A1), DropPosition.BEFORE).isSuccess());
		assertFalse(_onColumn._executed);

		List<Object> reorders = new ArrayList<>();
		_board.setReorder((column, objects) -> {
			reorders.add(column);
			reorders.add(objects);
		});

		assertTrue(drop(A2, cardKey(A1), DropPosition.BEFORE).isSuccess());
		assertFalse("A drop within a column runs no drop chain.", _onColumn._executed);
		assertEquals(List.of(OPEN, List.of(A2, A1)), reorders);

		reorders.clear();
		assertTrue(drop(A1, columnKey(0), DropPosition.NONE).isSuccess());
		assertEquals("A drop on the column appends.", List.of(OPEN, List.of(A2, A1)), reorders);

		reorders.clear();
		assertTrue(drop(A2, cardKey(A1), DropPosition.AFTER).isSuccess());
		assertEquals("A drop that keeps the order changes nothing.", List.of(), reorders);
	}

	/**
	 * The board announces its own type as accepted while it reorders, so a drop within a column is
	 * offered, and the types of its drop target in any case.
	 */
	public void testAcceptedTypes() {
		assertEquals(List.of(), state().get(ReactKanbanBoardControl.DROP_ACCEPTS));

		_board.setReorder((column, objects) -> {
			// Nothing to do.
		});
		assertEquals(List.of(CARD_TYPE), state().get(ReactKanbanBoardControl.DROP_ACCEPTS));
		assertEquals(Boolean.TRUE, state().get(ReactKanbanBoardControl.REORDER));

		_board.setDropTarget(new DropBinding(_context, List.of(columnDrop(OTHER_TYPE))));
		assertEquals(List.of(OTHER_TYPE, CARD_TYPE), state().get(ReactKanbanBoardControl.DROP_ACCEPTS));
	}

	private HandlerResult drop(String item, String targetKey, DropPosition position) {
		return _board.executeClientCommand(ReactKanbanBoardControl.CMD_DROP, dropArguments(item, targetKey, position));
	}

	private Map<?, ?> probe(String item, String targetKey, DropPosition position) {
		Map<String, Object> arguments = dropArguments(item, targetKey, position);
		arguments.put(DropProbeArguments.DRAG, "drag1");
		arguments.put(DropProbeArguments.PROBE, "probe1");
		_board.executeClientCommand(ReactKanbanBoardControl.CMD_DROP_PROBE, arguments);
		return (Map<?, ?>) ((Map<?, ?>) state().get(ReactKanbanBoardControl.DROP_VERDICTS)).get("probe1");
	}

	private Map<String, Object> dropArguments(String item, String targetKey, DropPosition position) {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put(DropArguments.SOURCE, _board.getID());
		arguments.put(DropArguments.KEYS, cardKey(item));
		arguments.put(DropArguments.SELECTION, Boolean.FALSE);
		arguments.put(DropArguments.TARGET_KEY, targetKey);
		arguments.put(DropArguments.POSITION, position.wireName());
		return arguments;
	}

	/** The client key of the column at the given index. */
	private String columnKey(int index) {
		return (String) columns().get(index).get(ReactKanbanBoardControl.COLUMN_KEY);
	}

	/** The client key of the card of the given object. */
	private String cardKey(String item) {
		for (Map<?, ?> column : columns()) {
			for (Object card : (List<?>) column.get(ReactKanbanBoardControl.COLUMN_CARDS)) {
				String key = (String) ((Map<?, ?>) card).get(ReactKanbanBoardControl.CARD_KEY);
				if (item.equals(_board.item(key))) {
					return key;
				}
			}
		}
		throw new AssertionError("No card for: " + item);
	}

	@SuppressWarnings("unchecked")
	private List<Map<?, ?>> columns() {
		return (List<Map<?, ?>>) state().get(ReactKanbanBoardControl.COLUMNS);
	}

	private Map<?, ?> state() {
		try {
			return (Map<?, ?>) JSON.fromString(_board.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError(ex);
		}
	}

	/**
	 * The test suite, started with the resource bundles a refusal reason is resolved with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestKanbanBoardDragDrop.class, ResourcesModule.Module.INSTANCE));
	}

}
