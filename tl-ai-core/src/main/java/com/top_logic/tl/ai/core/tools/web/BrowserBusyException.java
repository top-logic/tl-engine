/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

/**
 * Thrown when a new browser session cannot be started because the concurrency cap is
 * exhausted and no idle session can be reclaimed within the bounded acquire timeout.
 *
 * <p>
 * This is the fail-fast signal that replaces the old unbounded {@code ReentrantLock} wait:
 * rather than parking a caller thread indefinitely (a whole-application thread-exhaustion
 * risk), the browser tool rejects the request promptly so the model can retry later. It is a
 * transient, retryable condition - not a programming error.
 * </p>
 */
public class BrowserBusyException extends RuntimeException {

	/**
	 * Creates a {@link BrowserBusyException}.
	 *
	 * @param message
	 *        The human-readable reason, surfaced to the model as a retryable tool error.
	 */
	public BrowserBusyException(String message) {
		super(message);
	}
}
