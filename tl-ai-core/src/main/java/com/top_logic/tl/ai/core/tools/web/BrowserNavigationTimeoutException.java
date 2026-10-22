/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

/**
 * Thrown when a page load did not reach {@code domcontentloaded} within the per-operation
 * navigation timeout - the site is slow, unreachable or stuck.
 *
 * <p>
 * The page itself stays usable (the session is <em>not</em> discarded; that is
 * {@link BrowserSessionBrokenException}, which is reserved for the hard outer deadline). Existing
 * as its own type is what keeps the raw, multi-line Playwright {@code TimeoutError} - complete with
 * a driver JavaScript stack trace - out of the model's context: the tool renders this exception's
 * short, actionable message instead.
 * </p>
 */
public class BrowserNavigationTimeoutException extends RuntimeException {

	/**
	 * Creates a {@link BrowserNavigationTimeoutException}.
	 *
	 * @param url
	 *        The URL that did not finish loading.
	 * @param timeoutMillis
	 *        The navigation timeout that elapsed, in milliseconds.
	 */
	public BrowserNavigationTimeoutException(String url, int timeoutMillis) {
		super("the page at " + url + " did not finish loading within " + (timeoutMillis / 1000)
			+ " s, so nothing could be read. The site may be slow, unreachable or blocking automated "
			+ "access. Retry once, try a different URL, or use webFetch if the page does not need "
			+ "JavaScript.");
	}
}
