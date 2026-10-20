/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

/**
 * Callback for reporting progress from long-running tool executions.
 *
 * <p>Tools call {@link #progress(String)} to signal status (e.g., "processing 50 of 200
 * records") so the UI can display a live progress indicator.</p>
 *
 * @see ToolExecutionContext#progressReporter()
 */
@FunctionalInterface
public interface ProgressReporter {

	/**
	 * Reports a progress message.
	 *
	 * @param message human-readable status message
	 */
	void progress(String message);

	/** No-op reporter that discards all messages. */
	ProgressReporter NOOP = message -> {
		/** empty noop */
	};
}
