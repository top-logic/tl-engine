/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

/**
 * Thrown when a navigation the browser actually attempted was refused by the security policy - SSRF
 * protection or the administrator-owned domain blocklist/allowlist - rather than by a network or
 * page error.
 *
 * <p>
 * The pre-flight check in {@code BrowserTool} catches the ordinary case (the model asks for an
 * off-policy URL) before a browser is even touched. This exception covers the cases the pre-flight
 * check cannot see, because the off-policy origin only appears once the page is running: a
 * server-side redirect off the allowlist, a client-side redirect, or a click on a link that leaves
 * the permitted origin. Such a navigation is stopped by the per-context security route (which aborts
 * the request) and/or by the final-URL re-validation; both surface as this exception, so the model
 * gets the same clear, actionable wording either way instead of a raw Playwright
 * {@code net::ERR_FAILED} / {@code TimeoutError} dump.
 * </p>
 *
 * <p>
 * This never relaxes the policy: the navigation stays blocked. Only the error <em>surface</em>
 * differs.
 * </p>
 */
public class BrowserPolicyException extends RuntimeException {

	/**
	 * Creates a {@link BrowserPolicyException}.
	 *
	 * @param message
	 *        The human-readable, actionable reason, phrased for the model.
	 */
	public BrowserPolicyException(String message) {
		super(message);
	}
}
