/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.model;

import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.listener.PropertyObservable;

/**
 * The class {@link ModelChangeListener} is a listener interface to inform that the model of some
 * object has changed.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ModelChangeListener extends PropertyListener {

	/**
	 * {@link EventType} used to notify about a model change.
	 * 
	 * @see ModelChangeListener
	 */
	EventType<ModelChangeListener, Object, Object> MODEL_CHANGED =
		new EventType<>("modelChanged") {

			@Override
			public Bubble dispatch(ModelChangeListener listener, Object sender, Object oldValue, Object newValue) {
				listener.modelChanged(sender, oldValue, newValue);
				return Bubble.BUBBLE;
			}
		};

	/**
	 * This method is called if the model of an object has changed.
	 * 
	 * @param sender
	 *        the object whose model has changed
	 * @param oldModel
	 *        the model before the change.
	 * @param newModel
	 *        the model after the change
	 * @return Whether the event should also be delivered to the ancestors of the
	 *         {@link PropertyObservable}.
	 */
	Bubble modelChanged(Object sender, Object oldModel, Object newModel);

}
