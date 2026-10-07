/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.util.AbstractListeners;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;

/**
 * The {@link ChannelListener}s registered on a {@link ViewChannel}.
 *
 * <p>
 * A notification calls the listeners whose registrations exist when it starts, and skips each one
 * whose registration is {@link com.top_logic.basic.listener.Registration#dispose() disposed}
 * before the notification reaches it. A listener registered during a notification is first called
 * by the next one. The notification runs within the {@link ChannelNotificationScope} of the
 * current thread.
 * </p>
 */
final class ChannelListeners extends AbstractListeners<ChannelListener, ChannelListeners.Change> {

	/**
	 * A value change of a {@link ViewChannel}.
	 *
	 * @param oldValue
	 *        The value before the change.
	 * @param newValue
	 *        The value after the change.
	 */
	record Change(Object oldValue, Object newValue) {
		// Pure value.
	}

	private final ViewChannel _sender;

	/**
	 * Creates {@link ChannelListeners}.
	 *
	 * @param sender
	 *        The channel the listeners observe.
	 */
	ChannelListeners(ViewChannel sender) {
		_sender = sender;
	}

	/**
	 * Tells all listeners about a value change of the channel.
	 *
	 * @param oldValue
	 *        The value before the change.
	 * @param newValue
	 *        The value after the change.
	 */
	void notifyChange(Object oldValue, Object newValue) {
		ChannelNotificationScope scope = ChannelNotificationScope.current();
		scope.enter();
		try {
			notifyListeners(new Change(oldValue, newValue));
		} finally {
			scope.exit();
		}
	}

	@Override
	protected void sendEvent(ChannelListener listener, Change event) {
		listener.handleNewValue(_sender, event.oldValue(), event.newValue());
	}

}
