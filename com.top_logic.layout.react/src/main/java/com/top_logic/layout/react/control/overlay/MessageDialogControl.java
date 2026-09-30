/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import java.util.List;

import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.MessageButtons;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.layout.ReactInsetControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Builds and opens a dialog that tells the user something, composed of standard React controls.
 *
 * <p>
 * Shows a message with a single acknowledging button (defaulting to "OK"), styled as the dialog's
 * primary action. There is only one outcome: the given {@code onClose} action runs once the dialog
 * is gone, no matter which way the user left it (the button, the window's close button, Escape,
 * backdrop).
 * </p>
 */
public class MessageDialogControl {

	/**
	 * Opens a dialog showing a message the user acknowledges.
	 *
	 * @param context
	 *        The React context.
	 * @param dialogManager
	 *        The dialog manager for opening/closing the dialog.
	 * @param title
	 *        The dialog window title.
	 * @param message
	 *        The message shown in the dialog body.
	 * @param okLabel
	 *        The acknowledging button label, or {@code null} for the standard "OK" label.
	 * @param onClose
	 *        Runs exactly once, when the dialog has been closed through any path (the button, the
	 *        window's close button, Escape, or backdrop). May be {@code null}.
	 */
	public static void openDialog(ReactContext context, DialogManager dialogManager, String title, String message,
			String okLabel, Runnable onClose) {
		Runnable closeHandler = () -> dialogManager.closeTopDialog(DialogResult.ok(null));

		ReactWindowControl window = new ReactWindowControl(context, title, DisplayDimension.px(450), closeHandler);
		// A message and its acknowledgement have one size; nothing here is worth resizing or maximizing.
		window.setResizable(false);
		// The window body is flush by design ("content owns its inset"), so a bare text would glue to
		// the edges - wrap it in a padded inset to match the padded header/footer.
		window.setChild(new ReactInsetControl(context, new ReactTextControl(context, message)));

		ReactButtonControl okButton = MessageButtons.button(context, ButtonType.OK, okLabel, ctx -> {
			// Closing is all the button does; what follows is the business of the result handler, which
			// every way of leaving the dialog passes through.
			dialogManager.closeTopDialog(DialogResult.ok(null));
			return HandlerResult.DEFAULT_RESULT;
		});
		// The acknowledgement is the dialog's default: primary-styled and Enter-bound.
		okButton.markAsDefault();
		window.setActions(List.of(okButton));

		dialogManager.openDialog(false, window, result -> {
			if (onClose != null) {
				onClose.run();
			}
		});
	}
}
