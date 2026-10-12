/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.listener.Registration;
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
	 * Listeners are notified synchronously on the writing thread. The notification calls the
	 * listeners registered when it starts: a listener registered during the notification is first
	 * called by the next one, and a listener whose {@link Registration} is
	 * {@link Registration#dispose() disposed} before the notification reaches it is not called
	 * anymore. A listener that replaces and disposes a control subtree in reaction to the change
	 * defers the disposal via {@link ChannelNotificationScope#afterNotification(Runnable)}, so that
	 * the old subtree is torn down once the notification is complete.
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
	 * must therefore {@link Registration#dispose() dispose} the registration when the control is
	 * disposed, typically via a cleanup action:
	 * </p>
	 *
	 * <pre>
	 * Registration registration = channel.addListener(listener);
	 * control.addCleanupAction(registration::dispose);
	 * </pre>
	 *
	 * <p>
	 * A disposed registration is skipped also by a notification that is running when it is
	 * disposed, so the listener is not called on a control that was disposed by an earlier listener
	 * of the same notification.
	 * </p>
	 *
	 * <p>
	 * Each call creates a registration of its own: a listener added twice is notified twice.
	 * </p>
	 *
	 * @param listener
	 *        The listener to add.
	 * @return The handle that ends the registration when {@link Registration#dispose() disposed}.
	 */
	Registration addListener(ChannelListener listener);

	/**
	 * Removes all registrations of the given listener.
	 *
	 * @param listener
	 *        The listener to remove.
	 *
	 * @deprecated Use {@link Registration#dispose()} on the result of
	 *             {@link #addListener(ChannelListener)}.
	 */
	@Deprecated
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

	/**
	 * Releases everything this channel registered on other channels and on the model, when the one
	 * who created it stops using it.
	 *
	 * <p>
	 * A channel computed from other channels subscribes to them, and those inputs usually outlive
	 * it: a channel declared by a view is created with the view's controls, while its inputs may
	 * belong to an enclosing view. Releasing the channel removes these subscriptions, so that the
	 * inputs neither keep it reachable nor keep recomputing its value. The channel is not used
	 * afterwards.
	 * </p>
	 *
	 * <p>
	 * The owner of a channel declared in a {@code .view.xml} is the view instance creating it: the
	 * channel is released when the root control of that instance is disposed. A channel holding a
	 * value of its own registers nothing elsewhere and has nothing to release.
	 * </p>
	 */
	default void release() {
		// A channel holding its own value subscribes to nothing.
	}
}
