/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.kanban;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.RecordedCommand;
import com.top_logic.layout.react.control.ScriptingModelKey;
import com.top_logic.layout.react.control.dnd.DragSourceControl;
import com.top_logic.layout.react.control.dnd.DropArguments;
import com.top_logic.layout.react.control.dnd.DropEvent;
import com.top_logic.layout.react.control.dnd.DropObjectsArguments;
import com.top_logic.layout.react.control.dnd.DropPosition;
import com.top_logic.layout.react.control.dnd.DropProbeArguments;
import com.top_logic.layout.react.control.dnd.DropSupport;
import com.top_logic.layout.react.control.dnd.DropTarget;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.runtime.ActionContext;
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
 * <li>{@link #DRAG_ENABLED}, {@link #DRAG_TYPE}: whether and under which type tag cards are
 * dragged; while they are, each card descriptor tells by {@link #CARD_DRAGGABLE} whether that
 * card may be dragged.</li>
 * <li>{@link #DROP_ACCEPTS}: the type tags a drop on the board is accepted of;
 * {@link #REORDER}: whether a drop within a column reorders it; {@link #DROP_VERDICTS}: the
 * answers to the probes of the running drag.</li>
 * </ul>
 *
 * <p>
 * Cards are dragged like the rows of a table (the board is a {@link DragSourceControl}), and a
 * column is a drop target: a drop names either the column (appending to it) or a card of it and the
 * position before or after that card. A drop of objects coming from another column, or from
 * elsewhere, is applied by the {@link #setDropTarget(DropTarget) drop target} with the column value
 * as its {@link DropEvent#target() target}. Where the board has a {@link #setReorder(Reorder)
 * reorder function}, it then receives the column's objects in their new order, the dropped objects
 * placed where they were dropped; a drop within the column the objects are already in is applied by
 * the reorder function alone, and is not accepted without one.
 * </p>
 */
public class ReactKanbanBoardControl extends ReactControl implements DragSourceControl {

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

	/** Key of a card descriptor telling whether the card may be dragged, present while cards are. */
	public static final String CARD_DRAGGABLE = "draggable";

	/** @see DropSupport#DRAG_ENABLED */
	public static final String DRAG_ENABLED = DropSupport.DRAG_ENABLED;

	/** @see DropSupport#DRAG_TYPE */
	public static final String DRAG_TYPE = DropSupport.DRAG_TYPE;

	/** @see DropSupport#DROP_ACCEPTS */
	public static final String DROP_ACCEPTS = DropSupport.DROP_ACCEPTS;

	/** @see DropSupport#DROP_VERDICTS */
	public static final String DROP_VERDICTS = DropSupport.DROP_VERDICTS;

	/** State key telling the client whether a drop within a column reorders it. */
	public static final String REORDER = "reorder";

	/** @see DropSupport#CMD_DROP */
	public static final String CMD_DROP = DropSupport.CMD_DROP;

	/** @see DropSupport#CMD_DROP_PROBE */
	public static final String CMD_DROP_PROBE = DropSupport.CMD_DROP_PROBE;

	/** @see DropSupport#CMD_DROP_OBJECTS */
	public static final String CMD_DROP_OBJECTS = DropSupport.CMD_DROP_OBJECTS;

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

	/**
	 * Puts the objects of a column into a new order.
	 */
	@FunctionalInterface
	public interface Reorder {

		/**
		 * Gives the objects of a column the given order.
		 *
		 * @param column
		 *        The column value.
		 * @param objects
		 *        The objects of the column in their new order: the objects it displayed when the drop
		 *        was made, with the dropped objects at the position they were dropped at.
		 */
		void reorder(Object column, List<?> objects);

	}

	/**
	 * A drop resolved to what it does on the board: either the event to hand to the drop target
	 * together with the new order of the target column, or the reason it cannot be made.
	 *
	 * @param event
	 *        The drop with the column value as its target, {@code null} if it is refused.
	 * @param order
	 *        The objects of the target column in the order the drop gives them, {@code null} if it is
	 *        refused.
	 * @param refusal
	 *        Why the drop cannot be made, {@code null} if it can.
	 */
	private record BoardDrop(DropEvent event, List<Object> order, ResKey refusal) {

		static BoardDrop refused(ResKey reason) {
			return new BoardDrop(null, null, reason);
		}

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

	/** The displayed objects of each column, in display order. */
	private final Map<Object, List<Object>> _itemsOfColumn = new HashMap<>();

	/** The displayed columns, see {@link #setColumns(List)}. */
	private List<Column> _columns = List.of();

	/** The type tag cards are dragged under, {@code null} while they are not draggable. */
	private String _dragType;

	/** Which objects may be dragged while {@link #_dragType} is set, {@code null} for all. */
	private Predicate<Object> _draggable;

	/** What a drop of objects from another column is done with, {@code null} for nothing. */
	private DropTarget _dropTarget;

	/** What a drop gives a column a new order with, {@code null} while columns are not reordered. */
	private Reorder _reorder;

	/** The drop protocol shared with every control accepting drops. */
	private final DropSupport _dropSupport = new DropSupport(this);

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
		putState(DRAG_ENABLED, Boolean.FALSE);
		putState(DROP_ACCEPTS, List.of());
		putState(REORDER, Boolean.FALSE);
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
		_columns = List.copyOf(columns);
		pushColumns();
	}

	/**
	 * Publishes the {@link #_columns displayed columns} to the client.
	 */
	private void pushColumns() {
		List<Column> columns = _columns;
		Map<Object, String> columnKeys = new HashMap<>();
		Map<Object, String> cardKeys = new HashMap<>();
		_columnsByKey.clear();
		_itemsByKey.clear();
		_columnOfItem.clear();
		_itemsOfColumn.clear();

		List<Map<String, Object>> columnDescriptors = new ArrayList<>(columns.size());
		for (Column column : columns) {
			String columnKey = columnKeys.computeIfAbsent(column.value(), this::columnKey);
			_columnsByKey.put(columnKey, column.value());
			List<Object> columnItems = new ArrayList<>(column.cards().size());
			_itemsOfColumn.put(column.value(), columnItems);

			List<Map<String, Object>> cardDescriptors = new ArrayList<>(column.cards().size());
			for (Card card : column.cards()) {
				String cardKey = cardKeys.computeIfAbsent(card.item(), this::cardKey);
				_itemsByKey.put(cardKey, card.item());
				_columnOfItem.put(card.item(), column.value());
				columnItems.add(card.item());

				Map<String, Object> cardDescriptor = new LinkedHashMap<>();
				cardDescriptor.put(CARD_KEY, cardKey);
				cardDescriptor.put(CARD_CONTENT, card.content());
				if (_dragType != null) {
					cardDescriptor.put(CARD_DRAGGABLE, Boolean.valueOf(isDraggable(card.item())));
				}
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
	 * Records a card selection by the business identity of the card's object, and a drop as a
	 * {@link #CMD_DROP_OBJECTS} naming the dropped objects and the column value or card object it was
	 * made on, since the client keys are allocated per session.
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
		if (CMD_DROP.equals(command) && arguments != null) {
			RecordedCommand recorded = _dropSupport.recordDrop(arguments, this::targetOf);
			if (recorded != null) {
				return recorded;
			}
		}
		return super.recordCommand(command, arguments);
	}

	// -- Drag and drop --

	/**
	 * Makes the cards draggable, announcing them under the given type tag.
	 *
	 * <p>
	 * A card the given predicate refuses offers no drag (see
	 * {@link DragSourceControl#isDraggable(Object)}).
	 * </p>
	 *
	 * @param dragType
	 *        The {@link #dragType() type tag}, or {@code null} to make the cards undraggable again.
	 * @param draggable
	 *        Which objects may be dragged, {@code null} for all of them. Asked whenever the columns
	 *        are published; when its answer changes for other reasons, call
	 *        {@link #refreshDragSource()}.
	 */
	public void setDragSource(String dragType, Predicate<Object> draggable) {
		_dragType = dragType;
		_draggable = draggable;
		putState(DRAG_ENABLED, Boolean.valueOf(dragType != null));
		putState(DRAG_TYPE, dragType);
		pushColumns();
		refreshDropTarget();
	}

	/**
	 * Asks the {@link #setDragSource(String, Predicate) draggable predicate} again for the displayed
	 * cards, after its answer may have changed.
	 */
	public void refreshDragSource() {
		pushColumns();
	}

	/**
	 * Makes the columns accept a drop of the objects the given target accepts, and applies a drop
	 * of objects coming from another column, or from elsewhere, through it.
	 *
	 * @param dropTarget
	 *        What dropped objects are done with, {@code null} to accept no drop from outside a
	 *        column again.
	 */
	public void setDropTarget(DropTarget dropTarget) {
		_dropTarget = dropTarget;
		refreshDropTarget();
	}

	/**
	 * Sets the function giving a column a new order on a drop.
	 *
	 * @param reorder
	 *        Receives the new order of the column a drop was made on; {@code null} for none, so that
	 *        a drop within a column is not accepted and a drop from elsewhere leaves the order to the
	 *        {@link #setDropTarget(DropTarget) drop target}.
	 */
	public void setReorder(Reorder reorder) {
		_reorder = reorder;
		putState(REORDER, Boolean.valueOf(reorder != null));
		refreshDropTarget();
	}

	/**
	 * Announces the {@link #acceptedTypes() accepted type tags} to the client again, after the
	 * {@link #setDropTarget(DropTarget) drop target's} answers changed.
	 */
	public void refreshDropTarget() {
		putState(DROP_ACCEPTS, List.copyOf(acceptedTypes()));
	}

	/**
	 * The type tags a drop on the board is accepted of: those of the
	 * {@link #setDropTarget(DropTarget) drop target}, and the board's own {@link #dragType()} where
	 * a drop reorders a column.
	 */
	private Collection<String> acceptedTypes() {
		Set<String> result = new LinkedHashSet<>();
		if (_dropTarget != null) {
			result.addAll(_dropTarget.acceptedTypes());
		}
		if (_reorder != null && _dragType != null) {
			result.add(_dragType);
		}
		return result;
	}

	@Override
	public String dragType() {
		return _dragType;
	}

	@Override
	public boolean isDraggable(Object object) {
		return _dragType != null && (_draggable == null || _draggable.test(object));
	}

	@Override
	public List<?> dragObjects(List<String> keys) {
		if (keys == null) {
			return List.of();
		}
		List<Object> result = new ArrayList<>(keys.size());
		for (String key : keys) {
			Object item = _itemsByKey.get(key);
			if (item != null) {
				result.add(item);
			}
		}
		return result;
	}

	@Override
	public List<?> dragSelection() {
		return _selection != null && displays(_selection) ? List.of(_selection) : List.of();
	}

	/**
	 * Applies a drop the client made on a column of the board.
	 *
	 * <p>
	 * A drop the resolution or the check refuses is answered with a warning naming the reason and
	 * not applied.
	 * </p>
	 */
	@ReactCommandHandler(CMD_DROP)
	HandlerResult handleDrop(DropArguments args) {
		return applyDrop(resolveDrop(args));
	}

	/**
	 * Answers whether a drop right where a drag hovers would be accepted, without applying it.
	 *
	 * <p>
	 * The verdict is added to {@link #DROP_VERDICTS} under the
	 * {@link DropProbeArguments#getProbe() probe's identifier}. The probe is technical: it is neither
	 * recorded nor offered as an action.
	 * </p>
	 */
	@ReactCommandHandler(value = CMD_DROP_PROBE, technical = true)
	void handleDropProbe(DropProbeArguments args) {
		BoardDrop drop = resolveDrop(args);
		ResKey refusal = drop.refusal() != null ? drop.refusal() : refusal(drop);
		putState(DROP_VERDICTS, _dropSupport.answerProbe(args, refusal));
	}

	/**
	 * Applies a drop of the objects named by their {@link ScriptingModelKey business identity} on the
	 * column value or the card object named the same way - the form a drop is recorded in.
	 */
	@ReactCommandHandler(CMD_DROP_OBJECTS)
	HandlerResult handleDropObjects(DropObjectsArguments args) {
		DropPosition position = DropPosition.fromWire(args.getPosition());
		if (position == null) {
			return DropSupport.refused(I18NConstants.ERROR_DROP_NOT_ACCEPTED);
		}
		ActionContext actionContext = ScriptingModelKey.newActionContextOrNull();
		List<ModelName> unresolved = new ArrayList<>();
		List<Object> objects = DropSupport.locateAll(actionContext, args.getObjects(), unresolved);
		ModelName targetName = args.getTargetObject();
		Object target = ScriptingModelKey.locate(actionContext, null, targetName);
		if (target == null || !(displays(target) || _columnKeys.containsKey(target))) {
			// The target must be a column or a card of this board: a recorded drop that lands
			// somewhere else is a drift, not a drop.
			unresolved.add(targetName);
		}
		if (!unresolved.isEmpty() || objects.isEmpty()) {
			return HandlerResult.error(I18NConstants.ERROR_DROP_UNRESOLVED__OBJECTS.fill(unresolved));
		}
		return applyDrop(boardDrop(null, objects, target, position));
	}

	/**
	 * Resolves the client-side identities a drop names: the dragged objects through the source
	 * control, the target through this board.
	 */
	private BoardDrop resolveDrop(DropArguments args) {
		DropSupport.Dragged dragged = _dropSupport.dragged(acceptedTypes(), args);
		if (dragged.refusal() != null) {
			return BoardDrop.refused(dragged.refusal());
		}
		DropPosition position = DropPosition.fromWire(args.getPosition());
		String targetKey = args.getTargetKey();
		Object target = targetKey == null ? null : targetOf(targetKey);
		if (position == null || target == null) {
			return BoardDrop.refused(I18NConstants.ERROR_DROP_UNRESOLVED__OBJECTS.fill(targetKey));
		}
		return boardDrop(dragged.source(), dragged.objects(), target, position);
	}

	/**
	 * The object the board displays under the given client-side target key: the object of a card,
	 * or the value of a column; {@code null} if the key designates neither.
	 */
	private Object targetOf(String targetKey) {
		Object item = _itemsByKey.get(targetKey);
		return item != null ? item : _columnsByKey.get(targetKey);
	}

	/**
	 * The drop of the given objects on the given target.
	 *
	 * @param target
	 *        The object of a card - the drop is made beside it, in the card's column - or a column
	 *        value - the drop appends to the column.
	 * @param position
	 *        For a card target, whether the drop is made {@link DropPosition#BEFORE before} or after
	 *        the card.
	 */
	private BoardDrop boardDrop(ReactControl source, List<?> objects, Object target, DropPosition position) {
		Object column;
		Object reference;
		if (displays(target)) {
			column = columnOf(target);
			reference = target;
		} else if (_columnKeys.containsKey(target)) {
			column = target;
			reference = null;
		} else {
			return BoardDrop.refused(I18NConstants.ERROR_DROP_UNRESOLVED__OBJECTS.fill(target));
		}
		List<Object> order = newOrder(column, objects, reference, position == DropPosition.BEFORE);
		return new BoardDrop(new DropEvent(source, objects, column, DropPosition.NONE), order, null);
	}

	/**
	 * The objects of the given column in the order a drop gives them: the displayed objects without
	 * the dropped ones, with the dropped ones inserted beside the reference card, or appended
	 * without one.
	 *
	 * @param reference
	 *        The object of the card the drop was made beside, {@code null} for a drop on the column.
	 * @param before
	 *        Whether the drop was made before the reference card rather than after it.
	 */
	private List<Object> newOrder(Object column, List<?> objects, Object reference, boolean before) {
		Set<Object> dropped = new HashSet<>(objects);
		List<Object> result = new ArrayList<>();
		int index = -1;
		for (Object item : _itemsOfColumn.getOrDefault(column, List.of())) {
			boolean isReference = reference != null && reference.equals(item);
			boolean moved = dropped.contains(item);
			if (isReference && (before || moved)) {
				index = result.size();
			}
			if (!moved) {
				result.add(item);
			}
			if (isReference && !before && !moved) {
				index = result.size();
			}
		}
		result.addAll(index < 0 ? result.size() : index, objects);
		return result;
	}

	/**
	 * Whether the given drop moves only objects that are displayed in its target column already.
	 */
	private boolean withinColumn(BoardDrop drop) {
		Object column = drop.event().target();
		for (Object object : drop.event().objects()) {
			if (!displays(object) || !Objects.equals(columnOf(object), column)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Why the given resolved drop cannot be made, {@code null} if it can.
	 *
	 * <p>
	 * A drop within a column is accepted exactly when the board reorders its columns; any other drop
	 * is decided by the {@link #setDropTarget(DropTarget) drop target}.
	 * </p>
	 */
	private ResKey refusal(BoardDrop drop) {
		if (withinColumn(drop)) {
			return _reorder != null ? null : I18NConstants.ERROR_DROP_NOT_ACCEPTED;
		}
		if (_dropTarget == null) {
			return I18NConstants.ERROR_DROP_NOT_ACCEPTED;
		}
		return _dropTarget.check(drop.event()).reason();
	}

	/**
	 * Applies the given resolved drop, unless it is refused.
	 *
	 * <p>
	 * Order of application: a drop within a column runs the {@link #setReorder(Reorder) reorder
	 * function} alone. Any other drop is applied by the {@link #setDropTarget(DropTarget) drop
	 * target} first; once that is applied, the reorder function - where there is one - receives the
	 * target column in its new order.
	 * </p>
	 */
	private HandlerResult applyDrop(BoardDrop drop) {
		ResKey refusal = drop.refusal() != null ? drop.refusal() : refusal(drop);
		if (refusal != null) {
			return DropSupport.refused(refusal);
		}
		Object column = drop.event().target();
		Reorder reorder = _reorder;
		if (withinColumn(drop)) {
			if (!drop.order().equals(_itemsOfColumn.get(column))) {
				reorder.reorder(column, drop.order());
			}
		} else if (reorder == null) {
			_dropTarget.onDrop(drop.event());
		} else {
			List<Object> order = drop.order();
			_dropTarget.onDrop(drop.event(), () -> reorder.reorder(column, order));
		}
		return HandlerResult.DEFAULT_RESULT;
	}

}
