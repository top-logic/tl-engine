/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.model;

import java.util.Set;

import com.top_logic.layout.react.control.kanban.ReactKanbanBoardControl;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * {@link SelectionChannelBinding} for the selected card of a {@link ReactKanbanBoardControl}.
 *
 * <p>
 * The keys of the binding are the objects the cards display. A board displays one selected card at
 * a time; an object the board displays no card for is no selection there.
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

	private void cardSelected(Object item) {
		selectionChanged(item == null ? Set.of() : Set.of(item));
	}

	@Override
	protected Set<Object> getSelectedKeys() {
		Object selection = _board.getSelection();
		if (selection == null || !_board.displays(selection)) {
			return Set.of();
		}
		return Set.of(selection);
	}

	@Override
	protected void displaySelection(Set<?> keys) {
		Object item = keys.size() == 1 ? keys.iterator().next() : null;
		_board.setSelection(item != null && _board.displays(item) ? item : null);

		// The board reports no programmatic selection; reported here as the echo the base class
		// expects, so that it knows what the board displays.
		selectionChanged(getSelectedKeys());
	}

	@Override
	protected boolean canDisplaySeveral() {
		return false;
	}

	@Override
	protected void detach() {
		_board.setSelectionListener(null);
	}

}
