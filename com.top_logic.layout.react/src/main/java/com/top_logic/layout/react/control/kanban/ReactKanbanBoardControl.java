/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.kanban;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.RecordedCommand;
import com.top_logic.layout.react.control.ScriptingModelKey;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A board of columns, each holding a vertical list of cards (the client component
 * {@link #REACT_MODULE}).
 *
 * <p>
 * The board is model-agnostic: what it displays is handed to it as a list of {@link Column columns},
 * each standing for a column value and holding the {@link Card cards} of the objects in that column,
 * in display order. A card is an arbitrary control built by the caller around one object. The board
 * keeps the client keys stable: a column value and an object keep their key across
 * {@link #setColumns(List) updates} for as long as they are displayed, so the client reuses what it
 * renders for them, even for a card that changes its column.
 * </p>
 *
 * <p>
 * Clicking a card makes its object the {@link #getSelection() selection} and reports it to the
 * {@link #setSelectionListener(Consumer) selection listener}; the selected card is highlighted.
 * {@link #setSelection(Object)} highlights an object's card without a report, so a selection that
 * arrives from elsewhere is displayed as well.
 * </p>
 *
 * <p>
 * State:
 * </p>
 * <ul>
 * <li>{@link #COLUMNS}: the columns, each a map of {@link #COLUMN_KEY}, {@link #COLUMN_LABEL},
 * {@link #COLUMN_COUNT} and {@link #COLUMN_CARDS}, the latter a list of maps of {@link #CARD_KEY}
 * and {@link #CARD_CONTENT} (the card's child control).</li>
 * <li>{@link #SELECTED}: the key of the selected card, {@code null} when no displayed card is
 * selected.</li>
 * </ul>
 */
public class ReactKanbanBoardControl extends ReactControl {

	/** Name of the client component rendering the board. */
	public static final String REACT_MODULE = "TLKanbanBoard";

	/** State key of the list of column descriptors. */
	public static final String COLUMNS = "columns";

	/** State key of the key of the selected card. */
	public static final String SELECTED = "selected";

	/** Key of a column descriptor holding the column's stable key. */
	public static final String COLUMN_KEY = "key";

	/** Key of a column descriptor holding the column's label. */
	public static final String COLUMN_LABEL = "label";

	/** Key of a column descriptor holding the number of cards in the column. */
	public static final String COLUMN_COUNT = "count";

	/** Key of a column descriptor holding the list of card descriptors. */
	public static final String COLUMN_CARDS = "cards";

	/** Key of a card descriptor holding the card's stable key. */
	public static final String CARD_KEY = "key";

	/** Key of a card descriptor holding the card's content control. */
	public static final String CARD_CONTENT = "content";

	/** Command selecting a card by its key, see {@link SelectCardArguments}. */
	public static final String CMD_SELECT_CARD = "selectCard";

	/**
	 * Command selecting a card by the business identity of its object, see
	 * {@link SelectCardByKeyArguments}.
	 */
	public static final String CMD_SELECT_CARD_BY_KEY = "selectCardByKey";

	/**
	 * A column of the board.
	 *
	 * @param value
	 *        The value the column stands for; its key on the client stays the same as long as the
	 *        value is displayed.
	 * @param label
	 *        The label displayed in the column's header.
	 * @param cards
	 *        The cards of the column, in display order.
	 */
	public record Column(Object value, String label, List<Card> cards) {
		// Pure data.
	}

	/**
	 * A card of the board.
	 *
	 * @param item
	 *        The object the card displays; its key on the client stays the same as long as the
	 *        object is displayed.
	 * @param content
	 *        The control rendering the card. Its life cycle belongs to the caller: a content control
	 *        that is no longer displayed is not cleaned up by the board.
	 */
	public record Card(Object item, ReactControl content) {
		// Pure data.
	}

	/** Client keys of the displayed column values. */
	private Map<Object, String> _columnKeys = new HashMap<>();

	/** Client keys of the displayed objects. */
	private Map<Object, String> _cardKeys = new HashMap<>();

	/** Displayed column values by their client key. */
	private final Map<String, Object> _columnsByKey = new HashMap<>();

	/** Displayed objects by their card's client key. */
	private final Map<String, Object> _itemsByKey = new HashMap<>();

	/** Column value of each displayed object. */
	private final Map<Object, Object> _columnOfItem = new HashMap<>();

	private Object _selection;

	private Consumer<Object> _selectionListener;

	/**
	 * Creates an empty {@link ReactKanbanBoardControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 */
	public ReactKanbanBoardControl(ReactContext context) {
		super(context, null, REACT_MODULE);
		putState(COLUMNS, List.of());
		putState(SELECTED, null);
	}

	/**
	 * Replaces the displayed columns and cards.
	 *
	 * <p>
	 * A column value or an object that was displayed before keeps its client key. A card content
	 * that is no longer displayed is not cleaned up, since the caller may display it again; a caller
	 * dropping it for good calls {@link #cleanupTree()} on it.
	 * </p>
	 *
	 * @param columns
	 *        The columns to display, in display order.
	 */
	public void setColumns(List<Column> columns) {
		Map<Object, String> columnKeys = new HashMap<>();
		Map<Object, String> cardKeys = new HashMap<>();
		_columnsByKey.clear();
		_itemsByKey.clear();
		_columnOfItem.clear();

		List<Map<String, Object>> columnDescriptors = new ArrayList<>(columns.size());
		for (Column column : columns) {
			String columnKey = columnKeys.computeIfAbsent(column.value(), this::columnKey);
			_columnsByKey.put(columnKey, column.value());

			List<Map<String, Object>> cardDescriptors = new ArrayList<>(column.cards().size());
			for (Card card : column.cards()) {
				String cardKey = cardKeys.computeIfAbsent(card.item(), this::cardKey);
				_itemsByKey.put(cardKey, card.item());
				_columnOfItem.put(card.item(), column.value());

				Map<String, Object> cardDescriptor = new LinkedHashMap<>();
				cardDescriptor.put(CARD_KEY, cardKey);
				cardDescriptor.put(CARD_CONTENT, card.content());
				cardDescriptors.add(cardDescriptor);
			}

			Map<String, Object> columnDescriptor = new LinkedHashMap<>();
			columnDescriptor.put(COLUMN_KEY, columnKey);
			columnDescriptor.put(COLUMN_LABEL, column.label());
			columnDescriptor.put(COLUMN_COUNT, Integer.valueOf(cardDescriptors.size()));
			columnDescriptor.put(COLUMN_CARDS, cardDescriptors);
			columnDescriptors.add(columnDescriptor);
		}
		_columnKeys = columnKeys;
		_cardKeys = cardKeys;

		putState(COLUMNS, columnDescriptors);
		pushSelection();
	}

	private String columnKey(Object value) {
		String existing = _columnKeys.get(value);
		return existing != null ? existing : getReactContext().allocateId();
	}

	private String cardKey(Object item) {
		String existing = _cardKeys.get(item);
		return existing != null ? existing : getReactContext().allocateId();
	}

	/**
	 * The column value displayed under the given client key, {@code null} if none is.
	 */
	public Object columnValue(String columnKey) {
		return _columnsByKey.get(columnKey);
	}

	/**
	 * The object whose card is displayed under the given client key, {@code null} if none is.
	 */
	public Object item(String cardKey) {
		return _itemsByKey.get(cardKey);
	}

	/**
	 * Whether the board displays a card for the given object.
	 */
	public boolean displays(Object item) {
		return _columnOfItem.containsKey(item);
	}

	/**
	 * The column value the given object is displayed in, {@code null} if it is not displayed.
	 */
	public Object columnOf(Object item) {
		return _columnOfItem.get(item);
	}

	/**
	 * The selected object, {@code null} if there is none.
	 *
	 * <p>
	 * The selection is kept while the object's card is not displayed, and highlighted again once it
	 * is.
	 * </p>
	 */
	public Object getSelection() {
		return _selection;
	}

	/**
	 * Highlights the card of the given object, without reporting it to the
	 * {@link #setSelectionListener(Consumer) selection listener}.
	 *
	 * @param item
	 *        The selected object, or {@code null} for no selection.
	 */
	public void setSelection(Object item) {
		_selection = item;
		pushSelection();
	}

	/**
	 * Sets the listener informed when the user selects a card.
	 *
	 * @param listener
	 *        Receives the object of the selected card; {@code null} for no listener.
	 */
	public void setSelectionListener(Consumer<Object> listener) {
		_selectionListener = listener;
	}

	private void pushSelection() {
		putState(SELECTED, _selection == null ? null : _cardKeys.get(_selection));
	}

	/**
	 * Selects the card with the given key, as the user clicks it.
	 *
	 * <p>
	 * A key that names no displayed card - the card left the board while the click was on its way -
	 * changes nothing.
	 * </p>
	 */
	@ReactCommandHandler(CMD_SELECT_CARD)
	HandlerResult handleSelectCard(SelectCardArguments args) {
		Object item = _itemsByKey.get(args.getCard());
		if (item != null) {
			selectByUser(item);
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Selects the card of the object named by the given {@link ModelName key} - the replay-stable
	 * counterpart of {@link #handleSelectCard(SelectCardArguments)}.
	 */
	@ReactCommandHandler(CMD_SELECT_CARD_BY_KEY)
	HandlerResult handleSelectCardByKey(SelectCardByKeyArguments args) {
		ModelName name = args.getKey();
		Object item = ScriptingModelKey.locate(null, name);
		if (item == null || !_cardKeys.containsKey(item)) {
			// A recorded key that designates no displayed card is an explicit failure, never a
			// silent no-op.
			return HandlerResult.error(I18NConstants.ERROR_CARD_KEY_UNRESOLVED__KEY.fill(name));
		}
		selectByUser(item);
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Selects the given object as the user does, and reports it.
	 *
	 * <p>
	 * A report the listener refuses - unsaved changes the selection would replace - restores the
	 * selection displayed before and propagates.
	 * </p>
	 */
	private void selectByUser(Object item) {
		Object before = _selection;
		setSelection(item);
		if (_selectionListener != null) {
			try {
				_selectionListener.accept(item);
			} catch (RuntimeException ex) {
				setSelection(before);
				throw ex;
			}
		}
	}

	/**
	 * Records a card selection by the business identity of the card's object, since the card key
	 * is allocated per session.
	 */
	@Override
	public RecordedCommand recordCommand(String command, Map<String, Object> arguments) {
		if (CMD_SELECT_CARD.equals(command) && arguments != null
				&& arguments.get(SelectCardArguments.CARD) instanceof String cardKey) {
			Object item = _itemsByKey.get(cardKey);
			ModelName name = item == null ? null : ScriptingModelKey.name(null, item);
			if (name != null) {
				SelectCardByKeyArguments recorded = TypedConfiguration.newConfigItem(SelectCardByKeyArguments.class);
				recorded.setName(CMD_SELECT_CARD_BY_KEY);
				recorded.setKey(name);
				return new RecordedCommand(recorded);
			}
		}
		return super.recordCommand(command, arguments);
	}

}
