/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.Collections;
import java.util.List;

import com.top_logic.layout.view.form.StateHandler;

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
	 *
	 * <p>
	 * A listener that writes another channel in reaction to a change must forward that channel's
	 * veto to the notifying channel with {@link VetoForwarder}, so that the veto is raised before
	 * the notifying channel is written. A veto raised from within a notification unwinds through
	 * that notification and leaves the notifying channel half-notified: its value is already set,
	 * the listeners after the writing one are never told, and the
	 * {@link ChannelVetoException#getContinuation() continuation} of the exception retries only the
	 * nested write.
	 * </p>
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
	 * {@link ChannelListener}s are notified. If any veto listener answers with a non-empty list of
	 * {@link StateHandler}s, the change is blocked and a {@link ChannelVetoException} is thrown
	 * collecting the handlers of all veto listeners.
	 * </p>
	 *
	 * <p>
	 * A listener answers with everything that objects to the change, including the unsaved changes
	 * of a channel that is written in reaction to this one changing: {@link VetoForwarder} answers
	 * with the {@link ViewChannel#dirtyHandlers()} of such a channel, so that the question is asked
	 * before the first channel is written.
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
		 * @return The {@link StateHandler}s holding unsaved changes that this listener objects with,
		 *         an empty list to allow the change.
		 */
		List<StateHandler> checkVeto(ViewChannel sender, Object oldValue, Object newValue);

		/**
		 * Checks whether <em>any</em> value change would currently be blocked.
		 *
		 * <p>
		 * Asked before starting an interaction that is going to change the channel (e.g. before
		 * opening a dialog whose commands write it), so the user decides about the unsaved changes
		 * while the answer can still prevent wasted input. The default assumes that the veto does
		 * not depend on the concrete new value.
		 * </p>
		 *
		 * @param sender
		 *        The channel that is about to be changed.
		 * @return The {@link StateHandler}s holding unsaved changes that this listener would object
		 *         with, an empty list to allow the change.
		 */
		default List<StateHandler> checkDirty(ViewChannel sender) {
			return checkVeto(sender, sender.get(), null);
		}
	}

	/**
	 * The handlers holding unsaved changes that would block a value change of this channel right
	 * now, empty if the value can be changed without asking.
	 *
	 * <p>
	 * The answer is transitive: it consists of the handlers of this channel's own
	 * {@link VetoListener}s, which include the handlers forwarded from the channels that are
	 * written when this one changes. A component writing another channel from a
	 * {@link ChannelListener} of this one contributes the latter with a {@link VetoForwarder}.
	 * </p>
	 *
	 * @see VetoListener#checkDirty(ViewChannel)
	 */
	default List<StateHandler> dirtyHandlers() {
		return Collections.emptyList();
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
