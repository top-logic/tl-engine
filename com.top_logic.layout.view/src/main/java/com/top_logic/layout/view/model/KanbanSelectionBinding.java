/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.model;

import java.util.LinkedHashSet;
import java.util.Set;

import com.top_logic.layout.react.control.kanban.ReactKanbanBoardControl;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.table.SelectionMode;

/**
 * {@link SelectionChannelBinding} for the selected cards of a {@link ReactKanbanBoardControl}.
 *
 * <p>
 * The keys of the binding are the objects the cards display. A board in the selection mode
 * {@link SelectionMode#MULTI} displays several selected cards, otherwise one at a time; an object
 * the board displays no card for is no selection there.
 * </p>
 *
 * <p>
 * The board reports a selection the user makes; a selection displayed for a channel value is
 * reported by the binding itself, as the echo {@link SelectionChannelBinding} recognizes.
 * </p>
 */
public class KanbanSelectionBinding extends SelectionChannelBinding {

	private final ReactKanbanBoardControl _board;

	/**
	 * Creates a {@link KanbanSelectionBinding} and applies the channel's current value to the
	 * board.
	 *
	 * @param board
	 *        The board whose selection is bound.
	 * @param channel
	 *        The channel holding the selection.
	 */
	public KanbanSelectionBinding(ReactKanbanBoardControl board, ViewChannel channel) {
		super(channel);
		_board = board;

		board.setSelectionListener(this::cardSelected);

		attach();
	}

	private void cardSelected(Set<Object> items) {
		selectionChanged(items);
	}

	@Override
	protected Set<Object> getSelectedKeys() {
		Set<Object> result = new LinkedHashSet<>();
		for (Object item : _board.getSelection()) {
			if (_board.displays(item)) {
				result.add(item);
			}
		}
		return result;
	}

	@Override
	protected void displaySelection(Set<?> keys) {
		Set<Object> items = new LinkedHashSet<>();
		if (canDisplaySeveral() || keys.size() == 1) {
			for (Object key : keys) {
				if (_board.displays(key)) {
					items.add(key);
				}
			}
		}
		_board.setSelection(items);

		// The board reports no programmatic selection; reported here as the echo the base class
		// expects, so that it knows what the board displays.
		selectionChanged(getSelectedKeys());
	}

	@Override
	protected boolean canDisplaySeveral() {
		return _board.getSelectionMode() == SelectionMode.MULTI;
	}

	@Override
	protected void detach() {
		_board.setSelectionListener(null);
	}

}
