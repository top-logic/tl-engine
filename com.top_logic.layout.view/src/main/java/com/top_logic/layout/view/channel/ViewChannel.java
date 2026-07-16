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
	 * <p>
	 * Listeners are notified synchronously on the writing thread, iterating a <em>snapshot</em> of
	 * the listener list: adding or removing listeners during the notification does not affect which
	 * listeners the running notification still calls. A listener that replaces and disposes a
	 * control subtree in reaction to the change must therefore defer the disposal via
	 * {@link ChannelNotificationScope#afterNotification(Runnable)} — controls of the old subtree
	 * may still be pending in the snapshot.
	 * </p>
	 *
	 * @param newValue
	 *        The new value (may be {@code null}).
	 * @return {@code true} if the value actually changed (was different from the previous value).
	 */
	boolean set(Object newValue);

	/**
	 * Adds a listener that is notified when this channel's value changes.
	 *
	 * <p>
	 * A channel typically outlives the controls observing it (it belongs to the enclosing view,
	 * while presentations are rebuilt e.g. on selection changes). A control registering a listener
	 * must therefore {@link #removeListener(ChannelListener) remove} it again when the control is
	 * disposed, typically via a cleanup action:
	 * </p>
	 *
	 * <pre>
	 * channel.addListener(listener);
	 * control.addCleanupAction(() -&gt; channel.removeListener(listener));
	 * </pre>
	 *
	 * <p>
	 * Even with proper removal, the snapshot semantics of {@link #set(Object)} mean the listener
	 * can fire once more within the very notification that disposed its control; listener
	 * implementations touching more than the control's React state must guard against running on a
	 * disposed control (state updates on a disposed control are dropped by the control itself).
	 * </p>
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

	/**
	 * Observer that can block a pending value change on a {@link ViewChannel}.
	 *
	 * <p>
	 * Veto listeners are checked <em>before</em> the value is updated and before regular
	 * {@link ChannelListener}s are notified. If any veto listener returns a non-{@code null}
	 * {@link com.top_logic.layout.view.form.StateHandler}, the change is blocked and a
	 * {@link com.top_logic.layout.view.channel.ChannelVetoException} is thrown collecting all dirty
	 * handlers.
	 * </p>
	 *
	 * @see #addVetoListener(VetoListener)
	 */
	interface VetoListener {

		/**
		 * Checks whether the pending value change should be blocked.
		 *
		 * @param sender
		 *        The channel about to change.
		 * @param oldValue
		 *        The current value.
		 * @param newValue
		 *        The proposed new value.
		 * @return A dirty {@link com.top_logic.layout.view.form.StateHandler} if this listener
		 *         vetoes the change, or {@code null} to allow it.
		 */
		com.top_logic.layout.view.form.StateHandler checkVeto(ViewChannel sender, Object oldValue,
			Object newValue);
	}

	/**
	 * Adds a listener that is consulted before value changes.
	 *
	 * @param listener
	 *        The veto listener to add.
	 *
	 * @see VetoListener
	 */
	void addVetoListener(VetoListener listener);

	/**
	 * Removes a previously added veto listener.
	 *
	 * @param listener
	 *        The veto listener to remove.
	 */
	void removeVetoListener(VetoListener listener);
}
