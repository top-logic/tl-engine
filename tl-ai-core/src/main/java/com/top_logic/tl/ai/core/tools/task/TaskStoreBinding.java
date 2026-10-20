/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.task;

import com.top_logic.tl.ai.core.binding.ThreadLocalBinding;

/**
 * Thread-local binding for {@link TaskStore} access from tool methods.
 *
 * <p>Prefer {@link #bind(TaskStore)} with try-with-resources. The previous value is
 * captured at bind time and restored on close, making the binding safe across
 * cached thread pools.</p>
 */
public class TaskStoreBinding {

	private static final ThreadLocalBinding<TaskStore> STORE = new ThreadLocalBinding<>();

	/**
	 * Binds the given store on the current thread and returns a handle whose
	 * {@link AutoCloseable#close() close()} restores the previously bound value
	 * (or absence).
	 */
	public static Binding bind(TaskStore store) {
		return new Binding(STORE.bind(store));
	}

	/**
	 * Returns the {@link TaskStore} bound to the current thread, or {@code null}.
	 */
	public static TaskStore getStore() {
		return STORE.get();
	}

	/**
	 * Handle returned by {@link TaskStoreBinding#bind(TaskStore)}.
	 */
	public static final class Binding implements AutoCloseable {
		private final ThreadLocalBinding<TaskStore>.Handle _handle;

		Binding(ThreadLocalBinding<TaskStore>.Handle handle) {
			_handle = handle;
		}

		@Override
		public void close() {
			_handle.close();
		}
	}
}
