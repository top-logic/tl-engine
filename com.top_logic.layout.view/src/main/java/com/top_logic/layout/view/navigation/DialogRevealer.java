/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.navigation;

import com.top_logic.layout.react.control.overlay.DialogHandle;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.reveal.ChildRevealer;

/**
 * Brings an open dialog into view by closing the dialogs stacked on top of it.
 *
 * @param dialogs
 *        The stack the dialog belongs to.
 * @param dialog
 *        Handle of the dialog to display.
 */
public record DialogRevealer(DialogManager dialogs, DialogHandle dialog) implements ChildRevealer {

	/**
	 * Displays the dialog, whatever key the caller addresses it by: a dialog holds a single view.
	 */
	@Override
	public void revealChild(String key) {
		dialogs.closeDialogsAbove(dialog);
	}
}
