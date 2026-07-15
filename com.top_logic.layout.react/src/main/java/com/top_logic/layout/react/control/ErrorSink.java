/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import com.top_logic.base.services.simpleajax.HTMLFragment;

/**
 * Sink for user-visible error, warning, and info messages.
 *
 * <p>
 * Implementations typically forward messages to a UI notification component such as a snackbar. The
 * sink is propagated through the view context so that controls can report errors to whatever
 * notification component is in scope.
 * </p>
 */
public interface ErrorSink {

	/**
	 * Shows an error message to the user.
	 *
	 * @param content
	 *        The error message content.
	 */
	void showError(HTMLFragment content);

	/**
	 * Shows a warning message to the user.
	 *
	 * @param content
	 *        The warning message content.
	 */
	void showWarning(HTMLFragment content);

	/**
	 * Shows an informational message to the user.
	 *
	 * @param content
	 *        The info message content.
	 */
	void showInfo(HTMLFragment content);
}
