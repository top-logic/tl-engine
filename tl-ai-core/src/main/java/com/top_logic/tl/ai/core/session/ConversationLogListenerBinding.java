/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session;

import com.top_logic.tl.ai.core.binding.ThreadLocalBinding;

/**
 * Thread-local binding for {@link ConversationLogListener}.
 *
 * <p>Prefer {@link #bind(ConversationLogListener)} with try-with-resources. The
 * previous value is captured at bind time and restored on close, making the
 * binding safe across cached thread pools.</p>
 */
public class ConversationLogListenerBinding {

	private static final ThreadLocalBinding<ConversationLogListener> LISTENER = new ThreadLocalBinding<>();

	/**
	 * Binds the given listener on the current thread and returns a handle whose
	 * {@link AutoCloseable#close() close()} restores the previously bound value.
	 */
	public static Binding bind(ConversationLogListener listener) {
		return new Binding(LISTENER.bind(listener));
	}

	/** Returns the bound listener, or {@code null}. */
	public static ConversationLogListener getListener() {
		return LISTENER.get();
	}

	/** Handle returned by {@link ConversationLogListenerBinding#bind(ConversationLogListener)}. */
	public static final class Binding implements AutoCloseable {
		private final ThreadLocalBinding<ConversationLogListener>.Handle _handle;

		Binding(ThreadLocalBinding<ConversationLogListener>.Handle handle) {
			_handle = handle;
		}

		@Override
		public void close() {
			_handle.close();
		}
	}
}
