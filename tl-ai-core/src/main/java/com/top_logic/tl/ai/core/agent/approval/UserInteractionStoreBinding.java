/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.approval;

/**
 * Thread-local binding for {@link UserInteractionStore}.
 *
 * <p>Prefer {@link #bind(UserInteractionStore)} with try-with-resources. The previous
 * value is captured at bind time and restored on close, making the binding safe
 * across cached thread pools.</p>
 */
public class UserInteractionStoreBinding {

	private static final ThreadLocal<UserInteractionStore> STORE = new ThreadLocal<>();

	/**
	 * Binds the given store on the current thread and returns a handle whose
	 * {@link AutoCloseable#close() close()} restores the previously bound value.
	 */
	public static Binding bind(UserInteractionStore store) {
		UserInteractionStore previous = STORE.get();
		if (store == null) {
			STORE.remove();
		} else {
			STORE.set(store);
		}
		return new Binding(previous);
	}

	/** Returns the bound store, or {@code null}. */
	public static UserInteractionStore getStore() {
		return STORE.get();
	}

	/** Handle returned by {@link UserInteractionStoreBinding#bind(UserInteractionStore)}. */
	public static final class Binding implements AutoCloseable {
		private final UserInteractionStore _previous;
		private boolean _closed;

		Binding(UserInteractionStore previous) {
			_previous = previous;
		}

		@Override
		public void close() {
			if (_closed) {
				return;
			}
			_closed = true;
			if (_previous == null) {
				STORE.remove();
			} else {
				STORE.set(_previous);
			}
		}
	}
}
