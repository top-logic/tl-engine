/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.react.control.CommandFailures;
import test.com.top_logic.layout.react.control.InteractionWithoutSession;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ModelKeyArguments;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ScriptingModelKey;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.identity.ObjectIdTranslation;
import com.top_logic.layout.react.control.identity.ProjectionMode;
import com.top_logic.layout.react.control.kanban.ReactKanbanBoardControl;
import com.top_logic.layout.react.control.kanban.ReactKanbanBoardControl.Card;
import com.top_logic.layout.react.control.kanban.ReactKanbanBoardControl.Column;
import com.top_logic.layout.react.control.kanban.SelectCardArguments;
import com.top_logic.layout.react.control.layout.ReactGridControl;
import com.top_logic.layout.react.control.layout.ReactLayoutControl;
import com.top_logic.layout.react.control.layout.ReactLayoutControl.Item;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackGap;
import com.top_logic.layout.react.control.layout.SelectItemArguments;
import com.top_logic.layout.react.control.selection.ItemSelection;
import com.top_logic.layout.react.control.selection.SelectionModifiers;
import com.top_logic.layout.react.scripting.ScriptRecorder;
import com.top_logic.layout.react.scripting.ScriptingSession;
import com.top_logic.layout.react.scripting.ScriptingTreeProjector;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests the client keys of the items of a selectable {@link ReactLayoutControl} and the cards of a
 * {@link ReactKanbanBoardControl} as object ids, see {@link ItemSelection#identities()}: a recorded
 * selection carries the key of the selected object, replays in a fresh session by the client key
 * the object has there, and fails for an object that is not displayed; the agent projection lists
 * the items and cards by their client keys.
 */
public class TestItemSelectionObjectIds extends TestCase {

	private static final List<String> OBJECTS = List.of("percent", "scale", "angle");

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

	/**
	 * An item selection recorded with a client key carries the key of the item's object and selects
	 * the same item in a fresh session, whose client keys differ.
	 */
	public void testItemRecordAndReplayInFreshSession() {
		Grid recording = new Grid(context(0), OBJECTS);
		String clientKey = recording.itemKeys().get(2);
		ReactCommand step = record(recording, ReactLayoutControl.CMD_SELECT_ITEM, Map.of(
			SelectItemArguments.KEY, clientKey,
			SelectionModifiers.TOGGLE, Boolean.FALSE,
			SelectionModifiers.RANGE, Boolean.FALSE));

		assertEquals(ReactLayoutControl.CMD_SELECT_ITEM, step.getName());
		String recorded = ((SelectItemArguments) step).getKey();
		assertNotNull("A key in place of the client key: " + recorded, ObjectIdTranslation.parseKey(recorded));
		assertEquals(Set.of("angle"), recording.getSelection());

		Grid replay = new Grid(context(1000), OBJECTS);
		assertFalse("The client keys differ between the sessions.", replay.itemKeys().contains(clientKey));
		ScriptingSession.forRoot(replay).replay(step);
		assertEquals(Set.of("angle"), replay.getSelection());
		assertEquals(List.of(replay.itemKeys().get(2)), replay.state(ReactLayoutControl.SELECTED));
	}

	/**
	 * Replaying a recorded key of an object the container does not display fails, and the
	 * selection stays as it is.
	 */
	public void testItemReplayOfObjectNotDisplayedFails() {
		Grid recording = new Grid(context(0), OBJECTS);
		ReactCommand step = record(recording, ReactLayoutControl.CMD_SELECT_ITEM, Map.of(
			SelectItemArguments.KEY, recording.itemKeys().get(2)));

		Grid replay = new Grid(context(1000), List.of("percent", "scale"));
		replay.setSelection(List.of("scale"));
		TopLogicException failure = CommandFailures.failure("A key of an object not displayed must fail.",
			() -> ScriptingSession.forRoot(replay).replay(step));
		assertEquals(I18NConstants.ERROR_OBJECT_NOT_DISPLAYED__KEY.getKey(), failure.getErrorKey().plain().getKey());
		assertEquals(Set.of("scale"), replay.getSelection());
	}

	/**
	 * The agent projection lists the items by the client keys the selection command takes, with the
	 * labels of their objects.
	 */
	@SuppressWarnings("unchecked")
	public void testItemAgentProjection() {
		Grid grid = new Grid(context(0), OBJECTS);
		grid.setSelection(List.of("scale"));

		Map<String, Object> state = ScriptingTreeProjector.nodeState(grid, ProjectionMode.AGENT);
		List<Map<String, Object>> items = (List<Map<String, Object>>) state.get(ReactLayoutControl.ITEM_KEYS);
		assertEquals(3, items.size());
		for (int n = 0; n < OBJECTS.size(); n++) {
			assertEquals(grid.itemKeys().get(n), items.get(n).get(ReactLayoutControl.ITEM_KEY));
			assertEquals(OBJECTS.get(n), items.get(n).get(ScriptingModelKey.LABEL));
		}
		assertEquals(List.of(grid.itemKeys().get(1)), state.get(ReactLayoutControl.SELECTED));
	}

	/**
	 * A card selection recorded with a card key carries the key of the card's object and selects
	 * the same card in a fresh session, whose card keys differ.
	 */
	public void testCardRecordAndReplayInFreshSession() {
		Board recording = new Board(context(0), OBJECTS);
		String cardKey = recording.cardKey("scale");
		ReactCommand step = record(recording, ReactKanbanBoardControl.CMD_SELECT_CARD, Map.of(
			SelectCardArguments.CARD, cardKey,
			SelectionModifiers.TOGGLE, Boolean.FALSE,
			SelectionModifiers.RANGE, Boolean.FALSE));

		assertEquals(ReactKanbanBoardControl.CMD_SELECT_CARD, step.getName());
		String recorded = ((SelectCardArguments) step).getCard();
		assertNotNull("A key in place of the card key: " + recorded, ObjectIdTranslation.parseKey(recorded));
		assertEquals(Set.of("scale"), recording.getSelection());

		Board replay = new Board(context(1000), OBJECTS);
		assertFalse("The card keys differ between the sessions.", cardKey.equals(replay.cardKey("scale")));
		ScriptingSession.forRoot(replay).replay(step);
		assertEquals(Set.of("scale"), replay.getSelection());
		assertEquals("Both sessions assert the same selection.",
			ScriptingTreeProjector.nodeState(recording).get(ReactKanbanBoardControl.SELECTED),
			ScriptingTreeProjector.nodeState(replay).get(ReactKanbanBoardControl.SELECTED));
	}

	/**
	 * Replaying a recorded key of an object the board displays no card for fails, and the selection
	 * stays as it is.
	 */
	public void testCardReplayOfObjectNotDisplayedFails() {
		Board recording = new Board(context(0), OBJECTS);
		ReactCommand step = record(recording, ReactKanbanBoardControl.CMD_SELECT_CARD, Map.of(
			SelectCardArguments.CARD, recording.cardKey("angle")));

		Board replay = new Board(context(1000), List.of("percent", "scale"));
		TopLogicException failure = CommandFailures.failure("A key of an object without a card must fail.",
			() -> ScriptingSession.forRoot(replay).replay(step));
		assertEquals(I18NConstants.ERROR_OBJECT_NOT_DISPLAYED__KEY.getKey(), failure.getErrorKey().plain().getKey());
		assertEquals(Set.of(), replay.getSelection());
	}

	/**
	 * The agent projection lists the cards of each column by the keys the card selection takes,
	 * with the labels of their objects, so an agent selects a card it observes.
	 */
	@SuppressWarnings("unchecked")
	public void testCardAgentProjectionListsCards() {
		Board board = new Board(context(0), OBJECTS);

		Map<String, Object> state = ScriptingTreeProjector.nodeState(board, ProjectionMode.AGENT);
		List<Map<String, Object>> columns = (List<Map<String, Object>>) state.get(ReactKanbanBoardControl.COLUMNS);
		assertEquals(1, columns.size());
		Map<String, Object> column = columns.get(0);
		assertEquals("To do", column.get(ReactKanbanBoardControl.COLUMN_LABEL));
		assertEquals(Integer.valueOf(3), column.get(ReactKanbanBoardControl.COLUMN_COUNT));
		List<Map<String, Object>> cards = (List<Map<String, Object>>) column.get(ReactKanbanBoardControl.COLUMN_CARDS);
		assertEquals(3, cards.size());
		Map<String, Object> angle = cards.get(2);
		assertEquals(board.cardKey("angle"), angle.get(ReactKanbanBoardControl.CARD_KEY));
		assertEquals("angle", angle.get(ScriptingModelKey.LABEL));
		assertFalse("No card control in the projection: " + angle,
			angle.containsKey(ReactKanbanBoardControl.CARD_CONTENT));

		new InteractionWithoutSession().runWithContext(() -> board.executeCommand(
			ReactKanbanBoardControl.CMD_SELECT_CARD,
			Map.of(SelectCardArguments.CARD, angle.get(ReactKanbanBoardControl.CARD_KEY))));
		assertEquals(Set.of("angle"), board.getSelection());
		assertEquals(List.of(board.cardKey("angle")),
			ScriptingTreeProjector.nodeState(board, ProjectionMode.AGENT).get(ReactKanbanBoardControl.SELECTED));
	}

	/**
	 * The key projection of a card carries no client key of an object with a business key.
	 */
	@SuppressWarnings("unchecked")
	public void testCardKeyProjection() {
		Board board = new Board(context(0), OBJECTS);

		Map<String, Object> column = ((List<Map<String, Object>>) ScriptingTreeProjector.nodeState(board)
			.get(ReactKanbanBoardControl.COLUMNS)).get(0);
		Map<String, Object> card = ((List<Map<String, Object>>) column.get(ReactKanbanBoardControl.COLUMN_CARDS)).get(0);
		assertEquals(ScriptingModelKey.toKey(null, "percent"), card.get(ModelKeyArguments.KEY));
		assertFalse(card.containsKey(ScriptingModelKey.LABEL));
	}

	/**
	 * Dispatches a command as the browser does and returns the recorded step.
	 */
	private static ReactCommand record(ReactControl control, String command, Map<String, Object> arguments) {
		ScriptRecorder recorder = new ScriptRecorder();
		recorder.start();
		recorder.capture(control, control, command, arguments);
		control.executeClientCommand(command, arguments);
		recorder.stop();

		List<ReactCommand> steps = recorder.steps();
		assertEquals(1, steps.size());
		return steps.get(0);
	}

	/** A context of a session that has allocated the given number of ids before. */
	private static ReactContext context(int allocated) {
		ReactContext result =
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		for (int n = 0; n < allocated; n++) {
			result.allocateId();
		}
		return result;
	}

	/**
	 * A selectable grid of string objects, exposing its state.
	 */
	private static final class Grid extends ReactGridControl {

		Grid(ReactContext context, List<String> objects) {
			super(context, "16rem", null, StackGap.DEFAULT, List.of());
			List<Item> items = new ArrayList<>();
			for (String object : objects) {
				items.add(new Item(object, new ReactTextControl(context, object)));
			}
			setItems(items);
		}

		Object state(String key) {
			return getState(key);
		}

		@SuppressWarnings("unchecked")
		List<String> itemKeys() {
			return (List<String>) getState(ITEM_KEYS);
		}
	}

	/**
	 * A board with one column of cards of string objects, exposing the keys of its cards.
	 */
	private static final class Board extends ReactKanbanBoardControl {

		private final ReactContext _context;

		Board(ReactContext context, List<String> objects) {
			super(context);
			_context = context;
			List<Card> cards = new ArrayList<>();
			for (String object : objects) {
				cards.add(new Card(object, new ReactTextControl(_context, object)));
			}
			setColumns(List.of(new Column("todo", "To do", cards)));
		}

		@SuppressWarnings("unchecked")
		String cardKey(String object) {
			for (Map<String, Object> column : (List<Map<String, Object>>) getState(COLUMNS)) {
				for (Map<String, Object> card : (List<Map<String, Object>>) column.get(COLUMN_CARDS)) {
					if (object.equals(item((String) card.get(CARD_KEY)))) {
						return (String) card.get(CARD_KEY);
					}
				}
			}
			throw new AssertionError("No card of '" + object + "'.");
		}
	}

	/**
	 * The test suite, started with the resource bundles the controls need and the
	 * {@link ModelResolver} naming the displayed objects.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestItemSelectionObjectIds.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE, ModelResolver.Module.INSTANCE));
	}

}
