/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.tool.execution.ExecutableState;

/**
 * The class {@link ExecutabilityModel} is an observable model for being informed about the change
 * of an {@link ExecutableState} value.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ExecutabilityModel extends ExecutabilityPolling {

	/**
	 * {@link EventType} for registering a {@link ExecutabilityListener}.
	 */
	EventType<ExecutabilityListener, ExecutabilityModel, ExecutableState> EXECUTABILITY =
		new EventType<>("executability") {
			@Override
			public Bubble dispatch(ExecutabilityListener listener,
					ExecutabilityModel sender, ExecutableState oldValue, ExecutableState newValue) {
				listener.handleExecutabilityChange(sender, oldValue, newValue);
				return Bubble.BUBBLE;
			}
		};

	/**
	 * {@link PropertyListener} for observing changes to the
	 * {@link ExecutabilityModel#getExecutability()} property.
	 */
	interface ExecutabilityListener extends PropertyListener {
		/**
		 * Informs about a change to the {@link ExecutabilityModel#getExecutability()} of the given
		 * model.
		 * 
		 * @param sender
		 *        The changed model.
		 * @param oldState
		 *        The old {@link ExecutableState}.
		 * @param newState
		 *        The new {@link ExecutableState}.
		 */
		void handleExecutabilityChange(ExecutabilityModel sender, ExecutableState oldState, ExecutableState newState);
	}

	/**
	 * Returns the current state of this {@link ExecutabilityModel}. never
	 * <code>null</code>
	 */
	ExecutableState getExecutability();

	/**
	 * Adds the given listener to inform about a change of the
	 * {@link #getExecutability() executability}.
	 * 
	 * @param listener
	 *        the listener to inform. must not be <code>null</code>
	 */
	void addExecutabilityListener(ExecutabilityListener listener);

	/**
	 * removes the given listener as listener for the executability.
	 */
	void removeExecutabilityListener(ExecutabilityListener listener);

}
