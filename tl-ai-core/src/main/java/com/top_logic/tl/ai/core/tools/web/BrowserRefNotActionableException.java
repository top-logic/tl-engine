/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

/**
 * Thrown when the element a <em>current-generation</em> ref points at could not be acted on within
 * the bounded action timeout: it has been detached, covered by an overlay, disabled or moved since
 * the snapshot that minted the ref.
 *
 * <p>
 * This is the same-page counterpart of {@link StaleBrowserRefException}. A stale ref is rejected up
 * front by its generation tag, but a ref from the <em>current</em> generation can still go dead when
 * the page re-renders in place - the classic search box whose suggestion list replaces the submit
 * control after the query is typed. Playwright then waits for actionability until its action timeout
 * expires. Bounding that wait to a few seconds and reporting it as this exception turns a 30-second
 * dead end plus a raw {@code TimeoutError} dump into a fast, recoverable instruction.
 * </p>
 *
 * <p>
 * Recovery is deliberately left to the model rather than retried automatically: a re-capture of the
 * same page does <em>not</em> advance the page generation, so the same ref string can be re-minted
 * onto a <em>different</em> element after the re-render. An automatic retry could therefore act on
 * the wrong element - exactly the silent-wrong-click failure the generation tagging exists to
 * prevent. The model re-reads the page and picks the ref it actually wants.
 * </p>
 */
public class BrowserRefNotActionableException extends RuntimeException {

	/**
	 * Creates a {@link BrowserRefNotActionableException}.
	 *
	 * @param ref
	 *        The (current-generation) ref whose element could not be acted on.
	 * @param action
	 *        The attempted action, phrased for the message (e.g. {@code "clicked"},
	 *        {@code "typed into"}).
	 * @param timeoutMillis
	 *        The bounded action timeout that elapsed, in milliseconds.
	 */
	public BrowserRefNotActionableException(String ref, String action, int timeoutMillis) {
		super("the element for ref \"" + ref + "\" could not be " + action + " within "
			+ (timeoutMillis / 1000) + " s - it is no longer actionable (it was probably detached, "
			+ "covered or re-rendered when the page changed after the last snapshot). Call "
			+ "browserSnapshot and retry with a fresh ref from that page text.");
	}
}
