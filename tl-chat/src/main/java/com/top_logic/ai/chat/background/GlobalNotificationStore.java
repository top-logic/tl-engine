/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.background;

/**
 * Singleton notification store for application-scoped background agent notifications.
 *
 * <p>
 * Used by scheduled and on-demand agents that run outside chat sessions.
 * </p>
 */
public class GlobalNotificationStore extends BackgroundNotificationStore {

	private static final GlobalNotificationStore INSTANCE = new GlobalNotificationStore();

	private GlobalNotificationStore() {
		// Singleton.
	}

	/**
	 * Returns the singleton instance.
	 */
	public static GlobalNotificationStore getInstance() {
		return INSTANCE;
	}

}
