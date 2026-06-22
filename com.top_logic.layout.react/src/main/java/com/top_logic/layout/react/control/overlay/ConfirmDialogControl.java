/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ButtonAppearance;
import com.top_logic.layout.react.control.button.MessageButtons;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.layout.ReactInsetControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Builds and opens a simple yes/no confirmation dialog composed of standard React controls.
 *
 * <p>
 * Shows a message with an affirmative and a declining button (defaulting to "Yes"/"No", but the
 * labels are configurable); the affirmative button is styled as the dialog's primary action. The
 * given {@code onConfirm} action runs only when the user confirms; all other ways of closing the
 * dialog (declining button, the window's close button, Escape, backdrop) cancel without side
 * effects.
 * </p>
 */
public class ConfirmDialogControl {

	/**
	 * Opens a yes/no confirmation dialog.
	 *
	 * @param context
	 *        The React context.
	 * @param dialogManager
	 *        The dialog manager for opening/closing the dialog.
	 * @param title
	 *        The dialog window title.
	 * @param message
	 *        The confirmation question shown in the dialog body.
	 * @param confirmLabel
	 *        The affirmative button label, or {@code null} for the standard "Yes" label.
	 * @param cancelLabel
	 *        The declining button label, or {@code null} for the standard "No" label.
	 * @param onConfirm
	 *        Runs when the user confirms (presses the affirmative button).
	 * @param onCancel
	 *        Runs when the user declines through any path (cancel button, the window's close button,
	 *        Escape, or backdrop). May be {@code null}.
	 */
	public static void openDialog(ReactContext context, DialogManager dialogManager, String title, String message,
			String confirmLabel, String cancelLabel, Runnable onConfirm, Runnable onCancel) {
		// One-shot guard so a given cancel-or-confirm outcome fires exactly once, regardless of how
		// the dialog is dismissed (button, X, Escape, backdrop all funnel through the result handler).
		boolean[] confirmed = { false };

		Runnable closeHandler = () -> dialogManager.closeTopDialog(DialogResult.cancelled());

		ReactWindowControl window = new ReactWindowControl(context, title, DisplayDimension.px(450), closeHandler);
		// The window body is flush by design ("content owns its inset"), so a bare text would glue to
		// the edges - wrap it in a padded inset to match the padded header/footer.
		window.setChild(new ReactInsetControl(context, new ReactTextControl(context, message)));

		List<ReactControl> actions = new ArrayList<>();
		actions.add(MessageButtons.button(context, ButtonType.NO, cancelLabel, ctx -> {
			dialogManager.closeTopDialog(DialogResult.cancelled());
			return HandlerResult.DEFAULT_RESULT;
		}));
		ReactButtonControl confirmButton = MessageButtons.button(context, ButtonType.YES, confirmLabel, ctx -> {
			confirmed[0] = true;
			dialogManager.closeTopDialog(DialogResult.ok(null));
			onConfirm.run();
			return HandlerResult.DEFAULT_RESULT;
		});
		// Emphasize the affirmative action as the dialog's primary button and make Enter trigger it.
		confirmButton.setAppearance(ButtonAppearance.PRIMARY);
		confirmButton.setKeyGesture("ENTER");
		actions.add(confirmButton);
		window.setActions(actions);

		dialogManager.openDialog(false, window, result -> {
			// Any dismissal that is not the confirmed path counts as cancel.
			if (!confirmed[0] && onCancel != null) {
				onCancel.run();
			}
		});
	}
}
