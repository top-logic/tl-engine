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
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
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
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelPartRef;

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
 * published on a local channel ({@link Config#getItemChannel()}), exactly like the item content of
 * an {@link ObjectListElement &lt;object-list&gt;}.
 * </p>
 *
 * <pre>
 * &lt;kanban-board
 *   columns="all(`demo.tickets:TicketStatus`)"
 *   items="all(`demo.tickets:Ticket`).sort(comparator(t -&gt; $t.get(`demo.tickets:Ticket#name`)))"
 *   column="t -&gt; $t.get(`demo.tickets:Ticket#status`)"
 *   observed-types="demo.tickets:Ticket"
 *   selection="ticket"
 * &gt;
 *   &lt;card&gt;
 *     &lt;text input="item"/&gt;
 *   &lt;/card&gt;
 * &lt;/kanban-board&gt;
 * </pre>
 *
 * <p>
 * Clicking a card writes its object to the {@link Config#getSelection() selection channel}; the card
 * of the object that channel holds is highlighted, whoever wrote it.
 * </p>
 *
 * <p>
 * The board follows the model: changes to an input, to displayed objects (e.g. the attribute their
 * column is computed from), or to objects of the {@link Config#getObservedTypes() observed types}
 * re-distribute the objects; a card of an object that stays on the board keeps its controls, also
 * when it moves to another column.
 * </p>
 */
@InApp
public class KanbanBoardElement implements UIElement {

	/**
	 * Configuration for {@link KanbanBoardElement}.
	 */
	@TagName("kanban-board")
	public interface Config extends UIElement.Config, Inputs {

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

		/** Configuration name for {@link #getItemChannel()}. */
		String ITEM_CHANNEL = "item-channel";

		/** Configuration name for {@link #getCard()}. */
		String CARD = "card";

		@Override
		@ClassDefault(KanbanBoardElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * TL-Script function computing the column values from the values of the inputs:
		 * {@code ...inputs -> columns}.
		 *
		 * <p>
		 * One column is displayed per value, in the order computed: the classifiers of an
		 * enumeration, or arbitrary objects.
		 * </p>
		 */
		@Name(COLUMNS)
		@Mandatory
		Expr getColumns();

		/**
		 * TL-Script function computing the displayed objects from the values of the inputs:
		 * {@code ...inputs -> objects}.
		 *
		 * <p>
		 * Within a column, the objects are displayed in the order computed here. An input whose
		 * object was deleted meanwhile is passed as nothing.
		 * </p>
		 */
		@Name(ITEMS)
		@Mandatory
		Expr getItems();

		/**
		 * TL-Script function computing the column value of a displayed object:
		 * {@code object -> column}.
		 *
		 * <p>
		 * An object whose column value is none of the {@link #getColumns() columns} is not
		 * displayed.
		 * </p>
		 */
		@Name(COLUMN)
		@Mandatory
		Expr getColumn();

		/**
		 * TL-Script function computing the label of a column's header from its column value:
		 * {@code column -> label}.
		 *
		 * <p>
		 * Without it, a column is labeled with the label of its column value.
		 * </p>
		 */
		@Name(COLUMN_LABEL)
		Expr getColumnLabel();

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
		 * Name of the channel publishing a card's object to the card content.
		 */
		@Name(ITEM_CHANNEL)
		@StringDefault("item")
		String getItemChannel();

		/**
		 * The content instantiated once per displayed object, with the object published on the
		 * {@link #getItemChannel() item channel}.
		 *
		 * <p>
		 * Ordinary view content: typically a single {@link com.top_logic.layout.view.ReferenceElement
		 * &lt;view-ref&gt;} binding the item channel, or elements written inline. Multiple entries
		 * are stacked vertically.
		 * </p>
		 */
		@Name(CARD)
		@Mandatory
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends UIElement>> getCard();
	}

	private final Config _config;

	private final QueryExecutor _columnsExecutor;

	private final QueryExecutor _itemsExecutor;

	private final QueryExecutor _columnExecutor;

	private final QueryExecutor _columnLabelExecutor;

	private final List<UIElement> _cardContent;

	/**
	 * Creates a {@link KanbanBoardElement} from configuration.
	 */
	@CalledByReflection
	public KanbanBoardElement(InstantiationContext context, Config config) {
		_config = config;
		_columnsExecutor = QueryExecutor.compile(config.getColumns());
		_itemsExecutor = QueryExecutor.compile(config.getItems());
		_columnExecutor = QueryExecutor.compile(config.getColumn());
		_columnLabelExecutor = QueryExecutor.compileOptional(config.getColumnLabel());
		_cardContent = config.getCard().stream()
			.map(context::getInstance)
			.collect(Collectors.toList());
	}

	@Override
	public List<ChildGroup> getChildGroups() {
		return List.of(ChildGroup.elements(_cardContent));
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<ViewChannel> inputs = ChannelInputs.resolve(context, _config.getInputs());

		QueryExecutor columnExecutor = _columnExecutor;
		KanbanBoardCards cards = new KanbanBoardCards(context, _cardContent, _config.getItemChannel(),
			item -> columnExecutor.execute(item), this::columnLabel);

		List<Object> initialItems = toList(_itemsExecutor, ChannelInputs.arguments(inputs));
		cards.show(toList(_columnsExecutor, ChannelInputs.arguments(inputs)), initialItems);

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
		// Observe the model only while the board is displayed.
		board.addAttachListener(() -> observer.attach(context.getModelScope()));
		board.addDetachListener(observer::detach);
		return board;
	}

	/**
	 * The label of a column's header.
	 */
	private String columnLabel(Object column) {
		Object label = _columnLabelExecutor == null ? column : _columnLabelExecutor.execute(column);
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
