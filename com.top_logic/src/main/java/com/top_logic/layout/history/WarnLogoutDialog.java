/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.history;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.component.configuration.LogoutView;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The class {@link WarnLogoutDialog} is a dialog which can be displayed when
 * the user tries to left the application using the browser back command.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class WarnLogoutDialog extends WarnDialogTemplate {

	private static final String LOGOUT_BUTTON_NAME = "logoutButton";
	private static final String CANCEL_BUTTON_NAME = "cancelButton";
	private static final String LOGOUT_MESSAGE_HEADER = "logoutMessageHeader";
	private static final String LOGOUT_MESSAGE = "logoutMessage";

	private static final DisplayValue WARN_LOGOUT_DIALOG_TITLE = new ResourceText(I18NConstants.LOGOUT_DIALOG_TITLE);

	/**
	 * Opens a dialog to inform the user that it will be logged out. The user
	 * has the possibility to leave the application or to stay in the
	 * application.
	 * 
	 * @param context
	 *        the context to open dialog in
	 * @param hControl
	 *        the current historyControl
	 */
	public static void openDialog(DisplayContext context, final HistoryControl hControl) {
		WarnMode warnMode = HistoryControl.getConfig().getLogoutWarnMode();
		switch (warnMode) {
			case WARN_MODE_IGNORE: {
				// nothing to do here
				break;
			}
			case WARN_MODE_CONTINUE: {
				LogoutView.logout(context.getWindowScope());
				break;
			}
			case WARN_MODE_DIALOG: {
				if (!hControl.warnDialogOpened) {
					DialogModel dialogModel = createDialogModel(hControl, WARN_LOGOUT_DIALOG_TITLE);
					final Command closeAction = dialogModel.getCloseAction();

					CommandField logoutButton = FormFactory.newCommandField(LOGOUT_BUTTON_NAME, new Command() {

						@Override
						public HandlerResult executeCommand(DisplayContext callbackContext) {
							closeAction.executeCommand(callbackContext);
							LogoutView.logout(callbackContext.getWindowScope());
							return HandlerResult.DEFAULT_RESULT;
						}
					});
					CommandField cancelButton = createCancelButton(CANCEL_BUTTON_NAME, closeAction);

					openDialog(context.getWindowScope(), dialogModel, LOGOUT_MESSAGE_HEADER, LOGOUT_MESSAGE,
						I18NConstants.HISTORY_DIALOGS,
						logoutButton, cancelButton);
					// prevent opening two warn dialogs
					hControl.warnDialogOpened = true;
				}
				break;
			}
			default:
				throw WarnMode.noSuchMode(warnMode);
		}
	}

}
