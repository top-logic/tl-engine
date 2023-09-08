/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.listener.PropertyListener;

/**
 * Handles closing of a dialog.
 * 
 * @see DialogModel#CLOSED_PROPERTY
 * @see PopupDialogModel#POPUP_DIALOG_CLOSED_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface DialogClosedListener extends PropertyListener {

	/**
	 * Handles upcoming close of the given dialog.
	 * 
	 * @param sender
	 *        The dialog that is closed.
	 * @param oldValue
	 *        Whether the dialog was closed before.
	 * @param newValue
	 *        Whether the dialog is now closed.
	 */
	void handleDialogClosed(Object sender, Boolean oldValue, Boolean newValue);

}

