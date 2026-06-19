/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.MessageButtons;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Builds and opens a simple yes/no confirmation dialog composed of standard React controls.
 *
 * <p>
 * Shows a message with a {@link MessageButtons#yes(ReactContext, com.top_logic.layout.react.control.button.ButtonAction)
 * Yes} and a {@link MessageButtons#no(ReactContext, com.top_logic.layout.react.control.button.ButtonAction) No}
 * button. The given {@code onConfirm} action runs only when the user confirms; all other ways of
 * closing the dialog (No, the window's close button, Escape, backdrop) cancel without side effects.
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
	 * @param onConfirm
	 *        Runs when the user confirms (presses Yes).
	 * @param onCancel
	 *        Runs when the user declines through any path (No button, the window's close button,
	 *        Escape, or backdrop). May be {@code null}.
	 */
	public static void openDialog(ReactContext context, DialogManager dialogManager, String title, String message,
			Runnable onConfirm, Runnable onCancel) {
		// One-shot guard so a given cancel-or-confirm outcome fires exactly once, regardless of how
		// the dialog is dismissed (button, X, Escape, backdrop all funnel through the result handler).
		boolean[] confirmed = { false };

		Runnable closeHandler = () -> dialogManager.closeTopDialog(DialogResult.cancelled());

		ReactWindowControl window = new ReactWindowControl(context, title, DisplayDimension.px(450), closeHandler);
		window.setChild(new ReactTextControl(context, message));

		List<ReactControl> actions = new ArrayList<>();
		actions.add(MessageButtons.no(context, ctx -> {
			dialogManager.closeTopDialog(DialogResult.cancelled());
			return HandlerResult.DEFAULT_RESULT;
		}));
		actions.add(MessageButtons.yes(context, ctx -> {
			confirmed[0] = true;
			dialogManager.closeTopDialog(DialogResult.ok(null));
			onConfirm.run();
			return HandlerResult.DEFAULT_RESULT;
		}));
		window.setActions(actions);

		dialogManager.openDialog(false, window, result -> {
			// Any dismissal that is not the confirmed path counts as cancel.
			if (!confirmed[0] && onCancel != null) {
				onCancel.run();
			}
		});
	}
}
