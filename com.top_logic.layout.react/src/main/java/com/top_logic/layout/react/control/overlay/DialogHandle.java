/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

/**
 * Handle for closing a specific dialog in the dialog stack.
 *
 * @see DialogManager#openDialog(boolean, com.top_logic.layout.react.control.ReactControl,
 *      DialogResultHandler)
 */
public interface DialogHandle {

	/**
	 * Closes this dialog with the given result.
	 *
	 * <p>
	 * If this dialog is not the topmost dialog, all dialogs above it are cascade-closed with
	 * {@link DialogResult#cancelled()}.
	 * </p>
	 *
	 * @param result
	 *        The result to pass to the dialog's handler.
	 */
	void close(DialogResult<Void> result);

}
