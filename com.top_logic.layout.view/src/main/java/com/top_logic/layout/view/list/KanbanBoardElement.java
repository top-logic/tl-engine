/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.command.ActionScopeDefinition;
import com.top_logic.layout.view.dnd.DropArguments;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.kanban.ReactKanbanBoardControl;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.layout.view.channel.Supplies;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.dnd.DeclaredDrop;
import com.top_logic.layout.view.dnd.DropSignature;
import com.top_logic.layout.view.dnd.DragSourceBinding;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.table.SelectionMode;

/**
 * Declarative {@link UIElement} displaying objects as cards in columns (the
 * {@code <kanban-board>} tag) - e.g. tickets in the columns of their status.
 *
 * <p>
 * The board is model-agnostic: it is bound to any number of {@link Config#getInputs() inputs} and
 * computes from their values the {@link Config#getColumns() column values} - enumeration
 * classifiers, or arbitrary objects - and the {@link Config#getItems() displayed objects}. Each
 * object is placed in the column its {@link Config#getColumn() column function} computes, in the
 * order the objects are computed; an object whose column value is none of the columns is not
 * displayed. A column's header shows its {@link Config#getColumnLabel() label} and the number of
 * cards in it.
 * </p>
 *
 * <p>
 * For each object, the {@link Config#getCard() card content} is instantiated with the object
 * published on a local channel ({@link Config#getElementChannel()}), exactly like the item content of
 * an {@link ObjectListElement &lt;object-list&gt;}.
 * </p>
 *
 * <pre>
 * &lt;kanban-board
 *   columns="all(`demo.tickets:TicketStatus`)"
 *   items="all(`demo.tickets:Ticket`).sort(comparator(t -&gt; $t.get(`demo.tickets:Ticket#name`)))"
 *   column="$element.get(`demo.tickets:Ticket#status`)"
 *   observed-types="demo.tickets:Ticket"
 *   selection="ticket"
 * &gt;
 *   &lt;card&gt;
 *     &lt;text input="element"/&gt;
 *   &lt;/card&gt;
 * &lt;/kanban-board&gt;
 * </pre>
 *
 * <p>
 * Clicking a card writes its object to the {@link Config#getSelection() selection channel}; the card
 * of the object that channel holds is highlighted, whoever wrote it. In the
 * {@link Config#getSelectionMode() selection mode} {@link SelectionMode#MULTI}, several cards are
 * selected at once.
 * </p>
 *
 * <p>
 * The board follows the model: changes to an input, to displayed objects (e.g. the attribute their
 * column is computed from), or to objects of the {@link Config#getObservedTypes() observed types}
 * re-distribute the objects; a card of an object that stays on the board keeps its controls, also
 * when it moves to another column.
 * </p>
 *
 * <p>
 * With a {@link Config#getDrag() drag}, cards are dragged like the rows of a table - onto another
 * column, or onto any display accepting the drag's kind. Each column is a drop target for the
 * {@link Config#getDrops() drops}: a drop is made on the column, whose value is the target of the
 * drop and published on its {@code target-channel}. An {@link Config#getOnReorder() reorder
 * function} keeps the order within the columns: a drop within a column runs it alone, and after a
 * drop on another column has run its action chain, it runs with the new order of that column.
 * </p>
 *
 * <pre>
 * &lt;kanban-board ...
 *   on-reorder="count($objects.size()).foreach(i -&gt; $objects[$i].set(`demo.tickets:Ticket#order`, $i))"
 * &gt;
 *   &lt;drag kind="ticket"/&gt;
 *   &lt;drop accept="ticket" target-channel="dropColumn"&gt;
 *     ...
 *   &lt;/drop&gt;
 * &lt;/kanban-board&gt;
 * </pre>
 */
@InApp
public class KanbanBoardElement implements UIElement {

	/** The tag a {@link KanbanBoardElement} is written with. */
	public static final String TAG_NAME = "kanban-board";

	/**
	 * Configuration for {@link KanbanBoardElement}.
	 */
	@TagName(TAG_NAME)
	public interface Config extends UIElement.Config, Inputs, ActionScopeDefinition, ElementAsConfig {

		/** Access to the default methods of this interface for the configuration proxy. */
		Lookup LOOKUP = MethodHandles.lookup();

		/** Configuration name for {@link #getColumns()}. */
		String COLUMNS = "columns";

		/** Configuration name for {@link #getItems()}. */
		String ITEMS = "items";

		/** Configuration name for {@link #getColumn()}. */
		String COLUMN = "column";

		/** Configuration name for {@link #getColumnLabel()}. */
		String COLUMN_LABEL = "column-label";

		/** Configuration name for {@link #getObservedTypes()}. */
		String OBSERVED_TYPES = "observed-types";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		/** Configuration name for {@link #getSelectionMode()}. */
		String SELECTION_MODE = "selection-mode";

		/** Configuration name for {@link #getElementChannel()}. */
		String ELEMENT_CHANNEL = "element-channel";

		/** Configuration name for {@link #getCard()}. */
		String CARD = "card";

		/** Configuration name for {@link #getDrag()}. */
		String DRAG = "drag";

		/** Configuration name for {@link #getDrops()}. */
		String DROPS = "drops";

		/** Configuration name for {@link #getOnReorder()}. */
		String ON_REORDER = "on-reorder";

		/** Configuration name for {@link #getColumnAs()}. */
		String COLUMN_AS = "column-as";

		/** Name of the variable holding the column value when no other name is given. */
		String DEFAULT_COLUMN_AS = "column";

		/** Name of the variable holding the objects of a column in their new order. */
		String OBJECTS_VARIABLE = "objects";

		@Override
		@ClassDefault(KanbanBoardElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * TL-Script expression computing the column values from the values of the inputs.
		 *
		 * <p>
		 * The expression sees the values of the {@link #getInputs() inputs} as variables named after
		 * their channels, or by their {@code as} names. One column is displayed per value, in the order computed: the classifiers of an
		 * enumeration, or arbitrary objects.
		 * </p>
		 */
		@Name(COLUMNS)
		@Mandatory
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getColumns();

		/**
		 * TL-Script expression computing the displayed objects from the values of the inputs.
		 *
		 * <p>
		 * The expression sees the values of the {@link #getInputs() inputs} as variables named after
		 * their channels, or by their {@code as} names. Within a column, the objects are displayed in
		 * the order computed here. An input whose object was deleted meanwhile has no value.
		 * </p>
		 */
		@Name(ITEMS)
		@Mandatory
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getItems();

		/**
		 * TL-Script expression computing the column value of a displayed object.
		 *
		 * <p>
		 * The expression sees the values of the {@link #getInputs() inputs} as variables named after
		 * their channels, or by their {@code as} names, and the object as the variable
		 * {@value ElementAsConfig#DEFAULT_ELEMENT_AS}, or by the name given with
		 * {@value ElementAsConfig#ELEMENT_AS}:
		 * </p>
		 *
		 * <pre>
		 * $element.get(`my:Ticket#status`)
		 * </pre>
		 *
		 * <p>
		 * An object whose column value is none of the {@link #getColumns() columns} is not
		 * displayed.
		 * </p>
		 */
		@Name(COLUMN)
		@Mandatory
		@Supplies(value = ElementAsConfig.DEFAULT_ELEMENT_AS, renamedBy = ElementAsConfig.ELEMENT_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getColumn();

		/**
		 * TL-Script expression computing the label of a column's header.
		 *
		 * <p>
		 * The expression sees the values of the {@link #getInputs() inputs} as variables named after
		 * their channels, or by their {@code as} names, and the column value as the variable
		 * {@value #DEFAULT_COLUMN_AS}, or by the name given with {@value #COLUMN_AS}. Without it, a
		 * column is labeled with the label of its column value.
		 * </p>
		 */
		@Name(COLUMN_LABEL)
		@Supplies(value = DEFAULT_COLUMN_AS, renamedBy = COLUMN_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getColumnLabel();

		/**
		 * The name of the variable holding the column value in the scripts evaluated for a column:
		 * the {@link #getColumnLabel() column label} and the {@link #getOnReorder() reorder
		 * function}.
		 *
		 * <p>
		 * Unset, the variable is named {@value #DEFAULT_COLUMN_AS}. An input of the same name is a
		 * configuration error and is renamed with <code>as</code>.
		 * </p>
		 */
		@Name(COLUMN_AS)
		@Nullable
		String getColumnAs();

		/**
		 * Types whose object changes (create / update / delete) re-distribute the objects of the
		 * board, so it refreshes automatically.
		 *
		 * <p>
		 * Changes of the displayed objects are followed in any case; the types are needed for
		 * objects that are created and then belong on the board.
		 * </p>
		 */
		@Name(OBSERVED_TYPES)
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getObservedTypes();

		/**
		 * Optional {@link ViewChannel} the object of the selected card is written to.
		 *
		 * <p>
		 * The card of the object the channel holds is displayed as selected.
		 * </p>
		 */
		@Name(SELECTION)
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();

		/**
		 * Whether the user may select one card at a time, or any number of them.
		 *
		 * <p>
		 * {@link SelectionMode#SINGLE} (the default) replaces the selection with every click, and a
		 * click on the selected card with {@code Ctrl} gives it up again.
		 * </p>
		 *
		 * <p>
		 * {@link SelectionMode#MULTI} adds a card to the selection, or takes it out again, by a click
		 * with {@code Ctrl} or a long press on a touch screen; a click with {@code Shift} adds the
		 * cards from the one clicked last up to the clicked one, the columns read from left to
		 * right. Dragging a selected card drags all selected cards.
		 * </p>
		 *
		 * <p>
		 * The {@link #getSelection() selection channel} holds the object of the selected card while
		 * exactly one is selected, the set of the selected objects while there are several, and
		 * nothing while there is none.
		 * </p>
		 */
		@Name(SELECTION_MODE)
		@ComplexDefault(SelectionMode.SingleDefault.class)
		SelectionMode getSelectionMode();

		/**
		 * Name of the channel publishing a card's object to the card content.
		 */
		@Name(ELEMENT_CHANNEL)
		@StringDefault("element")
		String getElementChannel();

		/**
		 * The content instantiated once per displayed object, with the object published on the
		 * {@link #getElementChannel() element channel}.
		 *
		 * <p>
		 * Ordinary view content: typically a single {@link com.top_logic.layout.view.ReferenceElement
		 * &lt;view-ref&gt;} binding the element channel, or elements written inline. Multiple entries
		 * are stacked vertically.
		 * </p>
		 *
		 * @implNote Keyed by {@link UIElement.Config#ID} so that a same-path overlay can address
		 *           an element.
		 */
		@Key(UIElement.Config.ID)
		@Name(CARD)
		@Mandatory
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<UIElement.Config> getCard();

		/**
		 * Makes the cards of this board draggable, so they can be dropped on another column or on a
		 * display that accepts the drag's kind.
		 *
		 * <p>
		 * Unset (default) leaves the cards undraggable.
		 * </p>
		 */
		@Name(DRAG)
		KanbanDragConfig getDrag();

		/**
		 * What the columns of this board accept a drop of, and what is done with the dropped
		 * objects.
		 *
		 * <p>
		 * A drop is made on a column: the column value is the target of the drop - written to the
		 * drop's {@code target-channel} before its actions run, and the input of its
		 * {@code target-executability} rules. A drop of objects that the column already displays is
		 * no drop of this kind; it is applied by the {@link #getOnReorder() reorder function} alone.
		 * Empty (default) leaves the columns accepting no drop from elsewhere.
		 * </p>
		 */
		@Name(DROPS)
		@DefaultContainer
		@TreeProperty
		List<KanbanDropConfig> getDrops();

		/**
		 * A {@link #getDrops() drop} runs its actions with the {@link DropArguments arguments} of
		 * its signature.
		 */
		@Override
		default ActionScope actionScope(ConfigurationItem part, ActionScope outer) {
			ActionScope drop = DropArguments.scopeOf(part, getDrops(), config -> DropSignature.ONTO);
			if (drop != null) {
				return drop;
			}
			return outer;
		}

		/**
		 * TL-Script expression giving a column a new order on a drop, run in a transaction.
		 *
		 * <p>
		 * The expression sees the values of the {@link #getInputs() inputs} as variables named after
		 * their channels, or by their {@code as} names, the column value as the variable
		 * {@value #DEFAULT_COLUMN_AS} (or by the name given with {@value #COLUMN_AS}), and the
		 * objects of that column in their new order as the variable {@value #OBJECTS_VARIABLE} -
		 * those the column displayed when the drop was made, with the dropped objects placed where
		 * they were dropped. It typically numbers the objects in an attribute that the
		 * {@link #getItems() items} are sorted by:
		 * </p>
		 *
		 * <pre>
		 * count($objects.size()).foreach(i -&gt; $objects[$i].set(`my:Ticket#order`, $i))
		 * </pre>
		 *
		 * <p>
		 * A drop within a column runs this function alone. A drop on another column runs the action
		 * chain of the matching {@link #getDrops() drop} first, and this function once the chain has
		 * run to its end - not after a chain that was aborted. Without the function, a drop within a
		 * column is not accepted.
		 * </p>
		 */
		@Name(ON_REORDER)
		@Supplies(value = DEFAULT_COLUMN_AS, renamedBy = COLUMN_AS)
		@Supplies(OBJECTS_VARIABLE)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getOnReorder();
	}

	private final Config _config;

	private final QueryExecutor _columnsExecutor;

	private final QueryExecutor _itemsExecutor;

	private final QueryExecutor _columnExecutor;

	private final QueryExecutor _columnLabelExecutor;

	private final List<UIElement> _cardContent;

	/** The declared {@link Config#getDrops() drops} with their actions, in declaration order. */
	private final List<DeclaredDrop> _drops;

	private final QueryExecutor _onReorder;

	/**
	 * Creates a {@link KanbanBoardElement} from configuration.
	 */
	@CalledByReflection
	public KanbanBoardElement(InstantiationContext context, Config config) {
		_config = config;
		_columnsExecutor = InputScript.compile(config, Config.COLUMNS);
		_itemsExecutor = InputScript.compile(config, Config.ITEMS);
		_columnExecutor = InputScript.compile(config, Config.COLUMN);
		_columnLabelExecutor = InputScript.compile(config, Config.COLUMN_LABEL);
		_cardContent = config.getCard().stream()
			.map(context::getInstance)
			.collect(Collectors.toList());
		_drops = config.getDrops().stream()
			.map(drop -> DeclaredDrop.compile(context, drop, DropSignature.ONTO))
			.toList();
		_onReorder = InputScript.compile(config, Config.ON_REORDER);
	}

	@Override
	public List<ChildGroup> getChildGroups() {
		return List.of(ChildGroup.elements(_cardContent));
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<ViewChannel> inputs = ChannelInputs.resolve(context, _config.getInputs());

		QueryExecutor columnExecutor = _columnExecutor;
		KanbanBoardCards cards = new KanbanBoardCards(context, _cardContent, _config.getElementChannel(),
			item -> columnExecutor.execute(arguments(inputs, item)), column -> columnLabel(inputs, column));

		List<Object> initialItems = toList(_itemsExecutor, ChannelInputs.arguments(inputs));
		cards.show(toList(_columnsExecutor, ChannelInputs.arguments(inputs)), initialItems);

		cards.board().setSelectionMode(_config.getSelectionMode());
		ChannelRef selection = _config.getSelection();
		if (selection != null) {
			cards.bindSelection(context.resolveChannel(selection));
		}

		QueryExecutor itemsExecutor = _itemsExecutor;
		QueryExecutor columnsExecutor = _columnsExecutor;
		RowSourceObserver<Object> observer = new RowSourceObserver<>(
			initialItems,
			args -> toList(itemsExecutor, args),
			ObservedTypes.resolve(_config.getObservedTypes()),
			inputs,
			items -> cards.show(toList(columnsExecutor, ChannelInputs.arguments(inputs)), items));

		ReactKanbanBoardControl board = cards.board();
		board.setCssClass(_config.getCssClass());
		if (_config.getDrag() != null) {
			installDragSource(context, board);
		}
		if (!_drops.isEmpty()) {
			board.setDropTarget(DeclaredDrop.bind(context, board, board::refreshDropTarget, _drops));
		}
		QueryExecutor onReorder = _onReorder;
		if (onReorder != null) {
			board.setReorder(
				(column, objects) -> inTransaction(() -> onReorder.execute(arguments(inputs, column, objects))));
		}
		// Observe the model only while the board is displayed.
		board.addAttachListener(() -> observer.attach(context.getModelScope()));
		board.addDetachListener(observer::detach);
		return board;
	}

	/**
	 * Makes the cards of the given board draggable as the {@link Config#getDrag() drag} declares,
	 * the {@link KanbanDragConfig#getCardExecutability() card rules} deciding per card.
	 */
	private void installDragSource(ViewContext context, ReactKanbanBoardControl board) {
		KanbanDragConfig drag = _config.getDrag();
		DragSourceBinding.Source source = new DragSourceBinding.Source() {
			@Override
			public boolean isDragEnabled() {
				return board.isDragEnabled();
			}

			@Override
			public void setDragSource(String dragKind, Predicate<Object> draggable) {
				board.setDragSource(dragKind, draggable);
			}

			@Override
			public void setDragEnabled(boolean enabled) {
				board.setDragEnabled(enabled);
			}

			@Override
			public void refreshDragSource() {
				board.refreshDragSource();
			}
		};
		DragSourceBinding.install(context, board, source, drag, drag.getKind(), KanbanDragConfig.CARD_EXECUTABILITY);
	}

	private static void inTransaction(Runnable action) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			action.run();
			tx.commit();
		}
	}

	/**
	 * The values a script of the board is evaluated with: the current values of the inputs, followed
	 * by the given supplied values.
	 */
	private static Object[] arguments(List<ViewChannel> inputs, Object... supplied) {
		return InputValues.alive(ChannelInputs.arguments(inputs, supplied));
	}

	/**
	 * The label of a column's header.
	 */
	private String columnLabel(List<ViewChannel> inputs, Object column) {
		Object label =
			_columnLabelExecutor == null ? column : _columnLabelExecutor.execute(arguments(inputs, column));
		if (label instanceof String text) {
			return text;
		}
		return MetaLabelProvider.INSTANCE.getLabel(label);
	}

	/**
	 * The objects the given function yields for the given input values, as a list.
	 *
	 * @param inputValues
	 *        The values of the input channels, in declaration order; an object that was deleted is
	 *        passed on as nothing.
	 */
	private static List<Object> toList(QueryExecutor executor, Object[] inputValues) {
		Object result = executor.execute(InputValues.alive(inputValues));
		if (result instanceof Collection<?> collection) {
			return new ArrayList<>(collection);
		}
		return result == null ? Collections.emptyList() : Collections.singletonList(result);
	}

}
