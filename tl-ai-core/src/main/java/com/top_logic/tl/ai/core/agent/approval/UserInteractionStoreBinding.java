/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.approval;

import com.top_logic.tl.ai.core.binding.ThreadLocalBinding;

/**
 * Thread-local binding for {@link UserInteractionStore}.
 *
 * <p>Prefer {@link #bind(UserInteractionStore)} with try-with-resources. The previous
 * value is captured at bind time and restored on close, making the binding safe
 * across cached thread pools.</p>
 */
public class UserInteractionStoreBinding {

	private static final ThreadLocalBinding<UserInteractionStore> STORE = new ThreadLocalBinding<>();

	/**
	 * Binds the given store on the current thread and returns a handle whose
	 * {@link AutoCloseable#close() close()} restores the previously bound value.
	 */
	public static Binding bind(UserInteractionStore store) {
		return new Binding(STORE.bind(store));
	}

	/** Returns the bound store, or {@code null}. */
	public static UserInteractionStore getStore() {
		return STORE.get();
	}

	/** Handle returned by {@link UserInteractionStoreBinding#bind(UserInteractionStore)}. */
	public static final class Binding implements AutoCloseable {
		private final ThreadLocalBinding<UserInteractionStore>.Handle _handle;

		Binding(ThreadLocalBinding<UserInteractionStore>.Handle handle) {
			_handle = handle;
		}

		@Override
		public void close() {
			_handle.close();
		}
	}
}
