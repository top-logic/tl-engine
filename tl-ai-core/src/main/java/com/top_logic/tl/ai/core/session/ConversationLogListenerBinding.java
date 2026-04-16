/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session;

/**
 * Thread-local binding for {@link ConversationLogListener}.
 *
 * <p>Set before an agent run starts, cleared in the finally block.
 * Used by {@code AskUserTool} to record interaction parts in the
 * conversation log.</p>
 */
public class ConversationLogListenerBinding {

	private static final ThreadLocal<ConversationLogListener> LISTENER = new ThreadLocal<>();

	/**
	 *
	 */
	public static void setListener(ConversationLogListener listener) {
		LISTENER.set(listener);
	}

	/**
	 *
	 */
	public static ConversationLogListener getListener() {
		return LISTENER.get();
	}

	/**
	 *
	 */
	public static void clear() {
		LISTENER.remove();
	}
}
