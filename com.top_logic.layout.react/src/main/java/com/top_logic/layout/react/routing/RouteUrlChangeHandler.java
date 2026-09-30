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
	 * Called when the composed URL changes.
	 *
	 * @param url
	 *        The new composed URL (without leading slash).
	 * @param replace
	 *        Whether this URL change should replace the current history entry rather than pushing a
	 *        new one.
	 */
	void onUrlChange(String url, boolean replace);
}
