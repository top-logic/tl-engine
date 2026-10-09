/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.list;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.kanban.ReactKanbanBoardControl;
import com.top_logic.layout.react.control.kanban.ReactKanbanBoardControl.Card;
import com.top_logic.layout.react.control.kanban.ReactKanbanBoardControl.Column;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.KanbanSelectionBinding;

/**
 * What a {@link KanbanBoardElement} displays: the objects of the board distributed into its
 * columns, one card per object.
 *
 * <p>
 * Each object is placed in the column its column function names, in the order the objects are
 * given; an object whose column value is none of the columns is not displayed, and an object given
 * twice is displayed once. The cards are
 * {@link TemplateInstances instances} of the card content, kept by object: an object that stays on
 * the board keeps its card - also when it changes its column - and only the cards of added objects
 * are built, those of removed ones cleaned up.
 * </p>
 */
public class KanbanBoardCards {

	private final ViewContext _templateContext;

	private final TemplateInstances _cards;

	private final Function<Object, Object> _columnFunction;

	private final Function<Object, String> _labelFunction;

	private final ReactKanbanBoardControl _board;

	private KanbanSelectionBinding _selectionBinding;

	/**
	 * Creates {@link KanbanBoardCards}.
	 *
	 * @param templateContext
	 *        The context the cards are derived from.
	 * @param cardContent
	 *        The content instantiated once per displayed object.
	 * @param elementChannelName
	 *        Name of the per-card channel holding the card's object.
	 * @param columnFunction
	 *        Computes the column value of an object.
	 * @param labelFunction
	 *        Computes the header label of a column value.
	 */
	public KanbanBoardCards(ViewContext templateContext, List<UIElement> cardContent, String elementChannelName,
			Function<Object, Object> columnFunction, Function<Object, String> labelFunction) {
		_templateContext = templateContext;
		_cards = new TemplateInstances(cardContent, elementChannelName, "card");
		_columnFunction = columnFunction;
		_labelFunction = labelFunction;
		_board = new ReactKanbanBoardControl(templateContext);
	}

	/**
	 * The control displaying the board.
	 */
	public ReactKanbanBoardControl board() {
		return _board;
	}

	/**
	 * Binds the board's selected card to the given channel: selecting a card writes its object to
	 * the channel, and the card of the object the channel holds is displayed as selected.
	 *
	 * @param channel
	 *        The channel holding the selection.
	 */
	public void bindSelection(ViewChannel channel) {
		_selectionBinding = new KanbanSelectionBinding(_board, channel);
		_board.addCleanupAction(_selectionBinding::dispose);
	}

	/**
	 * Rebuilds the board for the given columns and objects.
	 *
	 * @param columns
	 *        The column values, in display order.
	 * @param items
	 *        The objects to distribute into the columns, in the order they are displayed within a
	 *        column.
	 */
	public void show(List<?> columns, List<?> items) {
		Map<Object, List<Object>> itemsByColumn = new LinkedHashMap<>();
		for (Object column : columns) {
			itemsByColumn.putIfAbsent(column, new ArrayList<>());
		}
		List<Object> displayed = new ArrayList<>(items.size());
		for (Object item : new LinkedHashSet<>(items)) {
			List<Object> columnItems = itemsByColumn.get(_columnFunction.apply(item));
			if (columnItems != null) {
				columnItems.add(item);
			}
		}
		for (List<Object> columnItems : itemsByColumn.values()) {
			displayed.addAll(columnItems);
		}

		_cards.update(_templateContext, displayed);

		List<Column> boardColumns = new ArrayList<>(itemsByColumn.size());
		for (Map.Entry<Object, List<Object>> entry : itemsByColumn.entrySet()) {
			List<Card> cards = new ArrayList<>(entry.getValue().size());
			for (Object item : entry.getValue()) {
				ReactControl content = _cards.get(item);
				cards.add(new Card(item, content));
			}
			Object column = entry.getKey();
			boardColumns.add(new Column(column, _labelFunction.apply(column), cards));
		}
		_board.setColumns(boardColumns);

		if (_selectionBinding != null) {
			_selectionBinding.refreshed();
		}
	}

}
