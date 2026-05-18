/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.task;

/**
 * Thread-local binding for {@link TaskStore} access from tool methods.
 *
 * <p>Prefer {@link #bind(TaskStore)} with try-with-resources. The previous value is
 * captured at bind time and restored on close, making the binding safe across
 * cached thread pools.</p>
 */
public class TaskStoreBinding {

	private static final ThreadLocal<TaskStore> STORE = new ThreadLocal<>();

	/**
	 * Binds the given store on the current thread and returns a handle whose
	 * {@link AutoCloseable#close() close()} restores the previously bound value
	 * (or absence).
	 */
	public static Binding bind(TaskStore store) {
		TaskStore previous = STORE.get();
		if (store == null) {
			STORE.remove();
		} else {
			STORE.set(store);
		}
		return new Binding(previous);
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
		private final TaskStore _previous;
		private boolean _closed;

		Binding(TaskStore previous) {
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
