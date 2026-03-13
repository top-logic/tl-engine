/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

/**
 * Callback for receiving the result of a modal dialog.
 *
 * @param <T>
 *        The type of the result value.
 *
 * @see DialogResult
 * @see DialogManager
 */
@FunctionalInterface
public interface DialogResultHandler<T> {

	/**
	 * Called when the dialog is closed with a result.
	 *
	 * @param result
	 *        The dialog result (OK with a value, or cancelled).
	 */
	void onResult(DialogResult<T> result);

}
