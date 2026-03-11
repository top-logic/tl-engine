/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.task;

/**
 * Thread-local binding for {@link TaskStore} access from tool methods.
 *
 * <p>
 * Provides a mechanism for tool implementations to access the current {@link TaskStore} without
 * requiring it to be passed as a parameter. The store is bound to the current thread before agent
 * execution and cleared afterwards.
 * </p>
 *
 * @see com.top_logic.ai.service.tools.ToolCallTracking
 */
public class TaskStoreBinding {

	/**
	 * Thread-local storage for the current {@link TaskStore}.
	 */
	private static final ThreadLocal<TaskStore> STORE = new ThreadLocal<>();

	/**
	 * Sets the {@link TaskStore} for the current thread.
	 *
	 * @param store
	 *        The store to use, or null to remove the binding
	 */
	public static void setStore(TaskStore store) {
		if (store == null) {
			STORE.remove();
		} else {
			STORE.set(store);
		}
	}

	/**
	 * Gets the {@link TaskStore} for the current thread.
	 *
	 * @return The store, or null if not set
	 */
	public static TaskStore getStore() {
		return STORE.get();
	}

}
