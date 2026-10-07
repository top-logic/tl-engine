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
	 * {@link DialogResult#cancelled()}. The close is refused while this dialog or any dialog above
	 * it is not {@link #isClosable() closable}.
	 * </p>
	 *
	 * @param result
	 *        The result to pass to the dialog's handler.
	 */
	void close(DialogResult<Void> result);

	/**
	 * Sets whether this dialog can be closed.
	 *
	 * <p>
	 * While a dialog is not closable, every close of it is refused: Escape, the close button, a
	 * backdrop click, {@link #close(DialogResult)} and the cascade of closing a dialog below it. A
	 * dialog is closable unless marked otherwise, so an opener that suspends its dialog on some
	 * ongoing work marks it closable again when that work has settled.
	 * </p>
	 *
	 * @param closable
	 *        Whether the dialog may be closed.
	 */
	void setClosable(boolean closable);

	/**
	 * Whether this dialog can be closed.
	 *
	 * @see #setClosable(boolean)
	 */
	boolean isClosable();

}
