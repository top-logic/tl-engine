/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.model;

import java.util.LinkedHashSet;
import java.util.Set;

import com.top_logic.layout.react.control.kanban.ReactKanbanBoardControl;
import com.top_logic.layout.react.control.layout.ReactLayoutControl;
import com.top_logic.layout.react.control.selection.ItemSelector;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.table.SelectionMode;

/**
 * {@link SelectionChannelBinding} for the selected items of an {@link ItemSelector}: the cards of a
 * {@link ReactKanbanBoardControl}, or the items of a {@link ReactLayoutControl#isSelectable()
 * selectable} {@link ReactLayoutControl} such as the elements of an object list.
 *
 * <p>
 * The keys of the binding are the objects the items display. A selector in the selection mode
 * {@link SelectionMode#MULTI} displays several selected items, otherwise one at a time; an object
 * the selector displays no item for is no selection there.
 * </p>
 *
 * <p>
 * The selector reports a selection the user makes; a selection displayed for a channel value is
 * reported by the binding itself, as the echo {@link SelectionChannelBinding} recognizes.
 * </p>
 */
public class ItemSelectionBinding extends SelectionChannelBinding {

	private final ItemSelector _selector;

	/**
	 * Creates an {@link ItemSelectionBinding} and applies the channel's current value to the
	 * selector.
	 *
	 * @param selector
	 *        The control whose selection is bound.
	 * @param channel
	 *        The channel holding the selection.
	 */
	public ItemSelectionBinding(ItemSelector selector, ViewChannel channel) {
		super(channel);
		_selector = selector;

		selector.setSelectionListener(this::itemSelected);

		attach();
	}

	private void itemSelected(Set<Object> items) {
		selectionChanged(items);
	}

	@Override
	protected Set<Object> getSelectedKeys() {
		Set<Object> result = new LinkedHashSet<>();
		for (Object item : _selector.getSelection()) {
			if (_selector.displays(item)) {
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
				if (_selector.displays(key)) {
					items.add(key);
				}
			}
		}
		_selector.setSelection(items);

		// The selector reports no programmatic selection; reported here as the echo the base class
		// expects, so that it knows what the selector displays.
		selectionChanged(getSelectedKeys());
	}

	@Override
	protected boolean canDisplaySeveral() {
		return _selector.getSelectionMode() == SelectionMode.MULTI;
	}

	@Override
	protected void detach() {
		_selector.setSelectionListener(null);
	}

}
