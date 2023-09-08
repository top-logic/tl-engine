/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.model;

import com.top_logic.basic.util.AbstractObservable;

/**
 * Base class for senders of {@link ModelChangeEvent}s.
 * 
 * @see ModelChangeListener
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ModelChangeObservable extends AbstractObservable<ModelChangeListener, ModelChangeEvent> {

	/**
	 * Sends a {@link ModelChangeEvent} to registered receivers.
	 * 
	 * @param sender
	 *        See {@link ModelChangeEvent#getSender()}.
	 * @param oldModel
	 *        See {@link ModelChangeEvent#getOldModel()}.
	 * @param newModel
	 *        See {@link ModelChangeEvent#getNewModel()}.
	 */
	protected final void notifyListeners(Object sender, Object oldModel, Object newModel) {
		if (!hasListeners()) {
			return;
		}

		notifyListeners(new ModelChangeEvent(sender, oldModel, newModel));
	}

	@Override
	protected void sendEvent(ModelChangeListener listener, ModelChangeEvent event) {
		listener.modelChanged(event.getSender(), event.getOldModel(), event.getNewModel());
	}

}
