/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

/**
 * Sink for user-visible error, warning, and info messages.
 *
 * <p>
 * Implementations typically forward messages to a UI notification component such as a snackbar. The
 * sink is propagated through the view context so that controls can report errors to whatever
 * notification component is in scope.
 * </p>
 *
 * <p>
 * Message content is passed as pre-rendered HTML so that server-generated formatting and structure
 * is preserved on the client.
 * </p>
 */
public interface ErrorSink {

	/**
	 * Shows an error message to the user.
	 *
	 * @param htmlContent
	 *        The error message as rendered HTML.
	 */
	void showError(String htmlContent);

	/**
	 * Shows a warning message to the user.
	 *
	 * @param htmlContent
	 *        The warning message as rendered HTML.
	 */
	void showWarning(String htmlContent);

	/**
	 * Shows an informational message to the user.
	 *
	 * @param htmlContent
	 *        The info message as rendered HTML.
	 */
	void showInfo(String htmlContent);
}
