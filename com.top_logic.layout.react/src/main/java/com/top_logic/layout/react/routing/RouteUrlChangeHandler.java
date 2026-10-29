/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.routing;

/**
 * Callback for delivering URL changes to the client (e.g., via SSE).
 *
 * @see RouteManager#setUrlChangeHandler(RouteUrlChangeHandler)
 */
@FunctionalInterface
public interface RouteUrlChangeHandler {

	/**
	 * Called when the composed URL or the title of the page it names changes.
	 *
	 * <p>
	 * A change of the title alone - what the page displays is renamed, its address stays - is
	 * delivered with the URL the client shows already, as a replacement.
	 * </p>
	 *
	 * @param url
	 *        The new composed URL (without leading slash).
	 * @param title
	 *        The title of the page the URL names, empty where the display names none - see
	 *        {@link RouteManager#currentTitle()}.
	 * @param replace
	 *        Whether this URL change should replace the current history entry rather than pushing a
	 *        new one.
	 */
	void onUrlChange(String url, String title, boolean replace);
}
