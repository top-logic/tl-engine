/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

/**
 * Thrown when a browser operation exceeds its hard outer deadline (the page is presumed hung) so
 * the session must be discarded rather than reused.
 *
 * <p>
 * This is distinct from an ordinary Playwright error (a missing element, a navigation failure):
 * those leave the page usable and the session is kept - discarding it would throw away accumulated
 * state such as a login. Only this exception tells the pool to tear the session down.
 * </p>
 */
public class BrowserSessionBrokenException extends RuntimeException {

	/**
	 * Creates a {@link BrowserSessionBrokenException}.
	 *
	 * @param message
	 *        The human-readable reason.
	 */
	public BrowserSessionBrokenException(String message) {
		super(message);
	}
}
