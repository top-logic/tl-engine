/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import com.top_logic.basic.util.AbstractObservable;

/**
 * Implements listener handling of {@link TreeModelBase}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTreeModelBase<N> extends AbstractObservable<TreeModelListener, TreeModelEvent> implements TreeModelBase<N> {

	@Override
	public boolean addTreeModelListener(TreeModelListener listener) {
		return addListener(listener);
	}
	
	@Override
	public boolean removeTreeModelListener(TreeModelListener listener) {
		return removeListener(listener);
	}
	
	/**
	 * Fires a {@link TreeModelEvent} to the {@link #addTreeModelListener(TreeModelListener)
	 * listeners} of this model.
	 * 
	 * @param type
	 *        See {@link TreeModelEvent#getType()}.
	 * @param node
	 *        See {@link TreeModelEvent#getNode()}.
	 */
	protected final void fireTreeModelEvent(int type, Object node) {
		if (hasListeners()) {
			notifyListeners(new TreeModelEvent(this, type, node));
		}
	}
	
	@Override
	protected void sendEvent(TreeModelListener listener, TreeModelEvent event) {
		if (!hasListener(listener)) {
			// Quirks originally introduced with Ticket #3936: Check whether listener is still
			// active at the time the event is dispatched to it (another listener could have
			// unregistered it).
			return;
		}
		listener.handleTreeUIModelEvent(event);
	}
}
