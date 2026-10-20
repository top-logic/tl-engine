/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

/**
 * Thread-local flag signalling that the session's cached tool registry should be discarded and
 * rebuilt on the next turn.
 *
 * <p>
 * Set by persistence classes (e.g. {@code FrameworkWiringPersistence},
 * {@code ScriptToolConfigPersistence}) after restarting a service that affects the tool catalog.
 * Consumed by the chat processor after the agent run completes.
 * </p>
 */
public class ToolRegistryReloadFlag {

	private static final ThreadLocal<Boolean> FLAG = new ThreadLocal<>();

	/**
	 * Requests that the session's tool registry be rebuilt after the current agent turn.
	 */
	public static void request() {
		FLAG.set(Boolean.TRUE);
	}

	/**
	 * Returns {@code true} if a reload was requested, and clears the flag.
	 */
	public static boolean consume() {
		Boolean requested = FLAG.get();
		FLAG.remove();
		return Boolean.TRUE.equals(requested);
	}
}
