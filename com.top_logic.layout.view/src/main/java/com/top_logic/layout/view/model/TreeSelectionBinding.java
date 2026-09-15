/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.model;

import java.util.LinkedHashSet;
import java.util.Set;

import com.top_logic.layout.component.model.SelectionEvent;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.tree.model.TLTreeModelUtil;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.mig.html.SelectionModel;

/**
 * Writes the selection of a tree to a {@link ViewChannel}.
 *
 * <p>
 * What lands on the channel are the {@link TLTreeNode#getBusinessObject() business objects} of the
 * selected nodes, not the nodes themselves: the channel is read by displays and commands that work
 * with the application's objects and know nothing about the tree showing them. One selected node is
 * written as its object, several as the {@link Set} of their objects, none as <code>null</code> - so
 * a display bound to the channel works with a tree selecting a single node as well as with one
 * selecting any number, and only one that is to show several objects at once has to expect a set.
 * </p>
 *
 * <p>
 * The binding writes the channel; it does not read it. A tree following a value another writer put
 * on the channel reveals and selects the node for it itself, which takes the tree model this only
 * listens to.
 * </p>
 *
 * @param <T>
 *        The type of the selected nodes.
 */
public class TreeSelectionBinding<T> implements SelectionListener<T> {

	private final ViewChannel _channel;

	/**
	 * Creates a {@link TreeSelectionBinding} writing to the given channel.
	 *
	 * <p>
	 * To be registered as a selection listener of the tree's {@link SelectionModel}.
	 * </p>
	 *
	 * @param channel
	 *        The channel holding the selection.
	 */
	public TreeSelectionBinding(ViewChannel channel) {
		_channel = channel;
	}

	@Override
	public void notifySelectionChanged(SelectionModel<T> model, SelectionEvent<T> event) {
		Set<? extends T> selection = event.getNewSelection();
		switch (selection.size()) {
			case 0:
				_channel.set(null);
				break;
			case 1:
				_channel.set(TLTreeModelUtil.getInnerBusinessObject(selection.iterator().next()));
				break;
			default:
				Set<Object> objects = new LinkedHashSet<>();
				for (T node : selection) {
					objects.add(TLTreeModelUtil.getInnerBusinessObject(node));
				}
				_channel.set(objects);
				break;
		}
	}

}
