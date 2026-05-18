/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session;

/**
 * Thread-local binding for {@link ConversationLogListener}.
 *
 * <p>Prefer {@link #bind(ConversationLogListener)} with try-with-resources. The
 * previous value is captured at bind time and restored on close, making the
 * binding safe across cached thread pools.</p>
 */
public class ConversationLogListenerBinding {

	private static final ThreadLocal<ConversationLogListener> LISTENER = new ThreadLocal<>();

	/**
	 * Binds the given listener on the current thread and returns a handle whose
	 * {@link AutoCloseable#close() close()} restores the previously bound value.
	 */
	public static Binding bind(ConversationLogListener listener) {
		ConversationLogListener previous = LISTENER.get();
		if (listener == null) {
			LISTENER.remove();
		} else {
			LISTENER.set(listener);
		}
		return new Binding(previous);
	}

	/** Returns the bound listener, or {@code null}. */
	public static ConversationLogListener getListener() {
		return LISTENER.get();
	}

	/** Handle returned by {@link ConversationLogListenerBinding#bind(ConversationLogListener)}. */
	public static final class Binding implements AutoCloseable {
		private final ConversationLogListener _previous;
		private boolean _closed;

		Binding(ConversationLogListener previous) {
			_previous = previous;
		}

		@Override
		public void close() {
			if (_closed) {
				return;
			}
			_closed = true;
			if (_previous == null) {
				LISTENER.remove();
			} else {
				LISTENER.set(_previous);
			}
		}
	}
}
