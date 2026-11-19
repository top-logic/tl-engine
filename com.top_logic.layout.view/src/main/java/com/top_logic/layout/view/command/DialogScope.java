/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.layout.react.control.overlay.DialogHandle;
import com.top_logic.layout.view.ViewContext;

/**
 * The dialog a region of the view is displayed in.
 *
 * <p>
 * Established by {@link OpenDialogAction} as a scope of the dialog's {@link ViewContext}, so that
 * everything built inside the dialog can address the dialog itself rather than whichever dialog
 * happens to be on top of the stack. The dialog's content is built before the dialog is opened, so
 * the {@link #handle() handle} is known only once the dialog is displayed.
 * </p>
 *
 * @see ViewContext#getScope(Class)
 */
public class DialogScope {

	private DialogHandle _handle;

	/**
	 * The handle of the dialog, {@code null} while it is not opened yet, or when it could not be
	 * opened.
	 */
	public synchronized DialogHandle handle() {
		return _handle;
	}

	/**
	 * Records the handle of the opened dialog.
	 */
	synchronized void setHandle(DialogHandle handle) {
		_handle = handle;
	}
}
