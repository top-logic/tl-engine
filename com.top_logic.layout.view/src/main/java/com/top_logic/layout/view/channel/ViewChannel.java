/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

/**
 * A named reactive value within a view.
 *
 * <p>
 * Channels hold a current value and notify listeners when it changes. UI elements bind to channels
 * to receive input and propagate output.
 * </p>
 *
 * <p>
 * Channels are per-session state, created during
 * {@link com.top_logic.layout.view.UIElement#createControl} and registered on the
 * {@link com.top_logic.layout.view.ViewContext}.
 * </p>
 */
public interface ViewChannel {

	/**
	 * The current value of this channel (may be {@code null}).
	 */
	Object get();

	/**
	 * Updates the value of this channel, notifying all listeners if the value changed.
	 *
	 * @param newValue
	 *        The new value (may be {@code null}).
	 * @return {@code true} if the value actually changed (was different from the previous value).
	 */
	boolean set(Object newValue);

	/**
	 * Adds a listener that is notified when this channel's value changes.
	 *
	 * @param listener
	 *        The listener to add.
	 */
	void addListener(ChannelListener listener);

	/**
	 * Removes a previously added listener.
	 *
	 * @param listener
	 *        The listener to remove.
	 */
	void removeListener(ChannelListener listener);

	/**
	 * Observer of a {@link ViewChannel}.
	 */
	interface ChannelListener {

		/**
		 * Called when the channel's value changes.
		 *
		 * @param sender
		 *        The channel whose value changed.
		 * @param oldValue
		 *        The previous value.
		 * @param newValue
		 *        The new value.
		 */
		void handleNewValue(ViewChannel sender, Object oldValue, Object newValue);
	}
}
