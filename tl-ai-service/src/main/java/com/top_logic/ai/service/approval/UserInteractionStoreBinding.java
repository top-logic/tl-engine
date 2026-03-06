/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.approval;

/**
 * Thread-local binding for {@link UserInteractionStore}.
 *
 * <p>
 * The store is bound before agent execution in {@code ChatProcessor#executeOrchestrator} and
 * cleared afterward. Sub-agents inherit the binding automatically since they execute on the same
 * thread.
 * </p>
 */
public class UserInteractionStoreBinding {

	private static final ThreadLocal<UserInteractionStore> STORE = new ThreadLocal<>();

	/**
	 * Sets the store for the current thread.
	 *
	 * @param store The store to use, or {@code null} to clear.
	 */
	public static void setStore(UserInteractionStore store) {
		if (store == null) {
			STORE.remove();
		} else {
			STORE.set(store);
		}
	}

	/**
	 * Gets the store for the current thread.
	 *
	 * @return The store, or {@code null} if not bound.
	 */
	public static UserInteractionStore getStore() {
		return STORE.get();
	}
}
