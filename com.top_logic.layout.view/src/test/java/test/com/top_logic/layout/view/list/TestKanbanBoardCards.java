/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.json.JSON;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.kanban.ReactKanbanBoardControl;
import com.top_logic.layout.react.control.kanban.SelectCardArguments;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.ChildControl;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.list.KanbanBoardCards;
import com.top_logic.table.SelectionMode;

/**
 * Tests how {@link KanbanBoardCards} distributes objects into the columns of a board, keeps their
 * cards, and binds the selected cards to a channel.
 */
public class TestKanbanBoardCards extends TestCase {

	/** Name of the channel holding the object a card displays. */
	private static final String ITEM_CHANNEL = "item";

	private static final String OPEN = "open";

	private static final String DOING = "doing";

	private static final String DONE = "done";

	private static final List<String> COLUMNS = List.of(OPEN, DOING, DONE);

	private Template _template;

	private KanbanBoardCards _cards;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		ViewContext context = new DefaultViewContext(new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test")));
		_template = new Template();
		_cards = new KanbanBoardCards(context, List.of(_template), ITEM_CHANNEL, item -> ((Ticket) item)._status,
			column -> "Column " + column);
	}

	/**
	 * The objects are placed in the columns their column values name, in the order they are given;
	 * an object whose column value is none of the columns is not displayed.
	 */
	public void testDistribution() {
		Ticket a = new Ticket(OPEN);
		Ticket b = new Ticket(DONE);
		Ticket c = new Ticket(OPEN);
		Ticket unknown = new Ticket("rejected");

		_cards.show(COLUMNS, List.of(a, b, c, unknown));

		List<Map<String, Object>> columns = columns();
		assertEquals("One column per column value.", 3, columns.size());
		assertEquals("Column " + OPEN, columns.get(0).get(ReactKanbanBoardControl.COLUMN_LABEL));
		assertEquals("Column " + DOING, columns.get(1).get(ReactKanbanBoardControl.COLUMN_LABEL));
		assertEquals("Column " + DONE, columns.get(2).get(ReactKanbanBoardControl.COLUMN_LABEL));

		assertEquals("The objects of a column keep their order.", List.of(card(a), card(c)), cardIds(columns.get(0)));
		assertEquals(List.of(), cardIds(columns.get(1)));
		assertEquals(List.of(card(b)), cardIds(columns.get(2)));

		assertEquals(2, count(columns.get(0)));
		assertEquals(0, count(columns.get(1)));
		assertEquals(1, count(columns.get(2)));

		assertFalse("An object of an unknown column is not displayed.", board().displays(unknown));
		assertEquals("No card was built for the object that is not displayed.", 3, _template.created().size());
	}

	/**
	 * An object that changes its column moves to the other column and keeps its card; the column
	 * keys stay the same.
	 */
	public void testMoveKeepsCard() {
		Ticket a = new Ticket(OPEN);
		Ticket b = new Ticket(OPEN);
		_cards.show(COLUMNS, List.of(a, b));
		ReactControl cardOfA = card(a);
		List<Object> keysBefore = columnKeys();

		a._status = DOING;
		_cards.show(COLUMNS, List.of(a, b));

		List<Map<String, Object>> columns = columns();
		assertEquals(List.of(card(b)), cardIds(columns.get(0)));
		assertEquals("The object is displayed in its new column.", List.of(cardOfA), cardIds(columns.get(1)));
		assertEquals(DOING, board().columnOf(a));
		assertEquals("No card was built anew.", 2, _template.created().size());
		assertEquals("The columns keep their keys.", keysBefore, columnKeys());
	}

	/**
	 * Each card's content is bound to a channel holding its object.
	 */
	public void testCardChannel() {
		Ticket a = new Ticket(OPEN);
		_cards.show(COLUMNS, List.of(a));

		assertSame(a, _template.channels().get(0).get());
	}

	/**
	 * Selecting a card writes its object to the selection channel and highlights it; a value written
	 * to the channel from elsewhere is highlighted as well.
	 */
	public void testSelection() {
		Ticket a = new Ticket(OPEN);
		Ticket b = new Ticket(DONE);
		_cards.show(COLUMNS, List.of(a, b));
		DefaultViewChannel selection = new DefaultViewChannel("selection");
		_cards.bindSelection(selection);

		assertEquals("Nothing is selected initially.", List.of(), state().get(ReactKanbanBoardControl.SELECTED));

		String keyOfA = cardKey(a);
		board().executeCommand(ReactKanbanBoardControl.CMD_SELECT_CARD, Map.of(SelectCardArguments.CARD, keyOfA));

		assertSame("Selecting a card writes its object to the channel.", a, selection.get());
		assertEquals("The selected card is highlighted.", List.of(keyOfA), state().get(ReactKanbanBoardControl.SELECTED));

		selection.set(b);

		assertEquals("A selection written from elsewhere is highlighted.", List.of(cardKey(b)),
			state().get(ReactKanbanBoardControl.SELECTED));

		select(b, true, false);

		assertNull("A toggle of the selected card gives the single selection up.", selection.get());

		selection.set(Set.of(a, b));

		assertEquals("A single selection displays no set.", List.of(), state().get(ReactKanbanBoardControl.SELECTED));
	}

	/**
	 * In the selection mode {@link SelectionMode#MULTI}, a toggle adds a card or takes it out again,
	 * a range adds the cards from the one clicked last in display order, and the selected cards are
	 * dragged together.
	 */
	public void testMultiSelection() {
		Ticket a = new Ticket(OPEN);
		Ticket b = new Ticket(OPEN);
		Ticket c = new Ticket(DOING);
		Ticket d = new Ticket(DONE);
		_cards.show(COLUMNS, List.of(a, b, c, d));
		board().setSelectionMode(SelectionMode.MULTI);
		DefaultViewChannel selection = new DefaultViewChannel("selection");
		_cards.bindSelection(selection);
		assertEquals(Boolean.TRUE, state().get(ReactKanbanBoardControl.MULTI_SELECT));

		select(b, false, false);
		assertSame("One selected card is written as its object.", b, selection.get());

		select(d, true, false);
		assertEquals("A toggle adds the card.", Set.of(b, d), selection.get());
		assertEquals(Set.of(cardKey(b), cardKey(d)), Set.copyOf(selectedKeys()));
		assertEquals("The selection is dragged in display order.", List.of(b, d), board().dragSelection());

		select(b, true, false);
		assertSame("A toggle of a selected card takes it out.", d, selection.get());

		select(a, false, false);
		select(c, false, true);
		assertEquals("A range adds the cards from the one clicked last, across columns.", Set.of(a, b, c),
			selection.get());

		select(d, false, false);
		assertSame("A plain selection replaces the selection.", d, selection.get());

		selection.set(Set.of(a, c));
		assertEquals("A set written from elsewhere is highlighted.", Set.of(cardKey(a), cardKey(c)),
			Set.copyOf(selectedKeys()));
	}

	private void select(Ticket ticket, boolean toggle, boolean range) {
		board().executeCommand(ReactKanbanBoardControl.CMD_SELECT_CARD, Map.of(
			SelectCardArguments.CARD, cardKey(ticket),
			SelectCardArguments.TOGGLE, Boolean.valueOf(toggle),
			SelectCardArguments.RANGE, Boolean.valueOf(range)));
	}

	@SuppressWarnings("unchecked")
	private List<Object> selectedKeys() {
		return (List<Object>) state().get(ReactKanbanBoardControl.SELECTED);
	}

	/**
	 * A selected object that leaves the board gives up the selection channel.
	 */
	public void testSelectedObjectLeaves() {
		Ticket a = new Ticket(OPEN);
		_cards.show(COLUMNS, List.of(a));
		DefaultViewChannel selection = new DefaultViewChannel("selection");
		selection.set(a);
		_cards.bindSelection(selection);
		assertEquals(List.of(cardKey(a)), state().get(ReactKanbanBoardControl.SELECTED));

		a._status = "rejected";
		_cards.show(COLUMNS, List.of(a));

		assertNull("The selection names nothing displayed any more.", selection.get());
		assertEquals(List.of(), state().get(ReactKanbanBoardControl.SELECTED));
	}

	private ReactKanbanBoardControl board() {
		return _cards.board();
	}

	/**
	 * The card content control of the given object, as the template built it.
	 */
	private ReactControl card(Ticket ticket) {
		List<ViewChannel> channels = _template.channels();
		for (int n = 0; n < channels.size(); n++) {
			if (channels.get(n).get() == ticket) {
				return (ReactControl) _template.created().get(n);
			}
		}
		throw new AssertionError("No card built for: " + ticket);
	}

	/**
	 * The client key of the card of the given object.
	 */
	private String cardKey(Ticket ticket) {
		String controlId = card(ticket).getID();
		for (Map<String, Object> column : columns()) {
			for (Map<String, Object> card : cards(column)) {
				if (controlId.equals(contentId(card))) {
					return (String) card.get(ReactKanbanBoardControl.CARD_KEY);
				}
			}
		}
		throw new AssertionError("No card for: " + ticket);
	}

	private List<Object> columnKeys() {
		List<Object> result = new ArrayList<>();
		for (Map<String, Object> column : columns()) {
			result.add(column.get(ReactKanbanBoardControl.COLUMN_KEY));
		}
		return result;
	}

	/**
	 * The content controls of the cards of the given column, in display order.
	 */
	private List<ReactControl> cardIds(Map<String, Object> column) {
		List<ReactControl> result = new ArrayList<>();
		for (Map<String, Object> card : cards(column)) {
			String contentId = contentId(card);
			_template.created().stream()
				.filter(control -> ((ReactControl) control).getID().equals(contentId))
				.forEach(control -> result.add((ReactControl) control));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static String contentId(Map<String, Object> card) {
		Map<String, Object> descriptor = (Map<String, Object>) card.get(ReactKanbanBoardControl.CARD_CONTENT);
		return (String) descriptor.get(ChildControl.CONTROL_ID__PROP);
	}

	private static int count(Map<String, Object> column) {
		return ((Number) column.get(ReactKanbanBoardControl.COLUMN_COUNT)).intValue();
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> cards(Map<String, Object> column) {
		return (List<Map<String, Object>>) column.get(ReactKanbanBoardControl.COLUMN_CARDS);
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> columns() {
		return (List<Map<String, Object>>) state().get(ReactKanbanBoardControl.COLUMNS);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> state() {
		try {
			return (Map<String, Object>) JSON.fromString(board().stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError(ex);
		}
	}

	/**
	 * An object of the board; its column value is what the tests switch.
	 */
	private static class Ticket {

		String _status;

		Ticket(String status) {
			_status = status;
		}
	}

	/**
	 * Template creating a bare control per instantiation, remembering the channel it is bound to.
	 */
	private static class Template implements UIElement {

		private final List<IReactControl> _created = new ArrayList<>();

		private final List<ViewChannel> _channels = new ArrayList<>();

		@Override
		public IReactControl createControl(ViewContext context) {
			_channels.add(context.resolveChannel(new ChannelRef(ITEM_CHANNEL)));

			ReactControl control = new ReactControl(context, null, "TLPanel");
			_created.add(control);
			return control;
		}

		/**
		 * The controls created so far, in creation order.
		 */
		public List<IReactControl> created() {
			return _created;
		}

		/**
		 * The channels the created controls are bound to, in creation order.
		 */
		public List<ViewChannel> channels() {
			return _channels;
		}
	}

	/**
	 * The suite of tests.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestKanbanBoardCards.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE));
	}

}
