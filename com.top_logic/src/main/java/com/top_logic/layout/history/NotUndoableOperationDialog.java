/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.history;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.structure.DialogModel;

/**
 * The class {@link NotUndoableOperationDialog} opens a dialog which informs the
 * user that the browser back function can not be used (on server side there are
 * reasons that the server state cannot be rolled back).
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class NotUndoableOperationDialog extends WarnDialogTemplate {

	private static final String NOT_UNDOABLE_MESSAGE_HEADER = "notUndoableMessageHeader";
	private static final String NOT_UNDOABLE_MESSAGE = "notUndoableMessage";
	private static final String CLOSE_BUTTON_NAME = "closeButton";

	private static final DisplayValue NOT_UNDOABLE_TITLE_VIEW = new ResourceText(I18NConstants.NOT_UNDOABLE_DIALOG_TITLE);

	/**
	 * Opens a dialog to inform the user that the browser back action can not
	 * roll back the corresponding server action.
	 * 
	 * @param context
	 *        the context to open dialog in
	 * @param hControl
	 *        the current {@link HistoryControl}.
	 */
	public static void openDialog(DisplayContext context, final HistoryControl hControl) {
		WarnMode warnMode = HistoryControl.getConfig().getNotUndoableWarnMode();
		if (WarnMode.WARN_MODE_IGNORE == warnMode) {
			// nothing to do here
		} else {
			if (!hControl.warnDialogOpened) {
				final DialogModel dialogModel = createDialogModel(hControl, NOT_UNDOABLE_TITLE_VIEW);
				final CommandField closeField = createCancelButton(CLOSE_BUTTON_NAME, dialogModel.getCloseAction());
				dialogModel.setDefaultCommand(closeField);
				openDialog(context.getWindowScope(), dialogModel, NOT_UNDOABLE_MESSAGE_HEADER, NOT_UNDOABLE_MESSAGE,
					I18NConstants.HISTORY_DIALOGS, closeField);
				// prevent opening two warn dialogs
				hControl.warnDialogOpened = true;
			}
		}
	}

}
