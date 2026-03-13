/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import com.top_logic.layout.react.control.ReactControl;

/**
 * Service for opening and managing a stack of modal dialogs.
 *
 * <p>
 * Dialogs are stacked: opening a new dialog while one is already open places the new dialog on top.
 * Closing a dialog cascades to close all dialogs above it.
 * </p>
 *
 * @see ReactDialogManagerControl
 */
public interface DialogManager {

	/**
	 * Opens a new dialog with the given child content.
	 *
	 * @param closeOnBackdrop
	 *        Whether clicking the backdrop dismisses the dialog.
	 * @param child
	 *        The content control to display inside the dialog overlay.
	 * @param handler
	 *        Called when the dialog is closed with a result.
	 * @return A handle that can be used to close this specific dialog.
	 */
	DialogHandle openDialog(boolean closeOnBackdrop, ReactControl child, DialogResultHandler<Void> handler);

	/**
	 * Closes the topmost dialog with the given result.
	 *
	 * @param result
	 *        The result to pass to the dialog's handler.
	 */
	void closeTopDialog(DialogResult<Void> result);

}
