/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Thread-safe store for pending {@link ReviewNotification}s.
 *
 * <p>
 * Notifications are added by the background review agent and drained by the SSE handler. Uses a
 * {@link ConcurrentLinkedQueue} for lock-free thread safety with atomic drain semantics.
 * </p>
 */
public class ReviewNotificationStore {

	private final ConcurrentLinkedQueue<ReviewNotification> _notifications = new ConcurrentLinkedQueue<>();

	/**
	 * Adds a notification to the store.
	 *
	 * @param notification
	 *        The notification to add.
	 */
	public void add(ReviewNotification notification) {
		_notifications.add(notification);
	}

	/**
	 * Returns and removes all pending notifications.
	 *
	 * <p>
	 * Each notification is returned exactly once even under concurrent access.
	 * </p>
	 *
	 * @return A list of all pending notifications; never null, may be empty.
	 */
	public List<ReviewNotification> drain() {
		List<ReviewNotification> result = new ArrayList<>();
		ReviewNotification n;
		while ((n = _notifications.poll()) != null) {
			result.add(n);
		}
		return result;
	}

}
