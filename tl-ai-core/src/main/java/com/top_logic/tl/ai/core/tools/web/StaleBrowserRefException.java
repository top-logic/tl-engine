/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

/**
 * Thrown when a ref-consuming verb (click/type) is given a ref that was minted by an earlier
 * snapshot than the session's current one.
 *
 * <p>
 * Refs are tagged with the page generation they belong to ({@code s<gen>e<index>}); moving to a
 * different page (a navigation) bumps the generation. A carried-over ref from a previous page
 * therefore no longer matches the current generation. Rejecting it explicitly - rather than
 * resolving it - is what prevents a stale ref from silently acting on whatever element now happens to
 * occupy that index (or dead-ending in a long locator timeout). The message tells the model exactly
 * how to recover.
 * </p>
 */
public class StaleBrowserRefException extends RuntimeException {

	/**
	 * Creates a {@link StaleBrowserRefException}.
	 *
	 * @param ref
	 *        The stale ref the caller passed.
	 */
	public StaleBrowserRefException(String ref) {
		super("This ref (\"" + ref + "\") is from an earlier snapshot and is no longer valid; the page has "
			+ "changed since it was captured. Call browserSnapshot to get fresh refs, then act on those.");
	}
}
