/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.basic.util.AbstractObservable;
import com.top_logic.layout.component.model.SingleSelectionEvent;
import com.top_logic.layout.component.model.SingleSelectionListener;

/**
 * The class {@link AbstractSingleSelectionModel} implements the listener part of an {@link SingleSelectionModel}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractSingleSelectionModel extends
		AbstractObservable<SingleSelectionListener, SingleSelectionEvent> implements SingleSelectionModel {

	/**
	 * Dispatches a {@link SingleSelectionEvent} to registered listeners.
	 * 
	 * @param formerlySelectedObject
	 *        See {@link SingleSelectionEvent#getFormerlySelectedObject()}.
	 * @param selectedObject
	 *        See {@link SingleSelectionEvent#getNewlySelectedObject()}.
	 */
	protected void fireSingleSelectionChanged(Object formerlySelectedObject, Object selectedObject) {
		notifyListeners(new SingleSelectionEvent(this, formerlySelectedObject, selectedObject));
	}

	@Override
	protected void sendEvent(SingleSelectionListener listener, SingleSelectionEvent event) {
		listener.notifySelectionChanged(event.getSender(), event.getFormerlySelectedObject(),
			event.getNewlySelectedObject());
	}

	@Override
	public boolean addSingleSelectionListener(SingleSelectionListener listener) {
		return addListener(listener);
	}

	@Override
	public boolean removeSingleSelectionListener(SingleSelectionListener listener) {
		return removeListener(listener);
	}

	/**
	 * Whether a {@link SingleSelectionListener} is currently
	 * {@link #addSingleSelectionListener(SingleSelectionListener) added}.
	 */
	protected boolean hasSingleSelectionListeners() {
		return hasListeners();
	}

}
