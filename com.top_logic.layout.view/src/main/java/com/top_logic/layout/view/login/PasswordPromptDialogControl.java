/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.MessageButtons;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.form.ReactPasswordInputControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.layout.ReactInsetControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.ReactWindowControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Builds and opens a dialog asking the user to enter a password, and reports whether the password
 * they entered was accepted.
 *
 * <p>
 * The dialog is a message, a masked password input and the standard OK/Cancel pair, composed of
 * the same React controls a form is built from. What "accepted" means is not decided here: the
 * caller passes the check as a predicate, so the same prompt serves whatever needs a password
 * confirmed.
 * </p>
 *
 * <p>
 * A rejected password leaves the dialog open with the input emptied and the rejection shown under
 * it, so the user can try again. Only an accepted password reports success; every other way of
 * leaving the dialog - the cancel button, the window's close button, Escape - reports a cancel, and
 * exactly one of the two outcomes is reported.
 * </p>
 */
public class PasswordPromptDialogControl {

	/** Width of the prompt window. */
	private static final DisplayDimension WIDTH = DisplayDimension.px(420);

	/**
	 * Opens a dialog asking for a password.
	 *
	 * @param context
	 *        The React context.
	 * @param dialogManager
	 *        The dialog manager for opening and closing the dialog.
	 * @param title
	 *        The dialog window title.
	 * @param message
	 *        The explanation shown above the input, saying what the password is asked for.
	 * @param fieldLabel
	 *        The label of the password input.
	 * @param verifier
	 *        Decides whether the entered password is accepted. Called with the characters the user
	 *        entered, which this dialog owns and clears once the check has returned - a verifier
	 *        keeping the password must copy it.
	 * @param onVerified
	 *        Runs once the user has entered a password the {@code verifier} accepts.
	 * @param onCancel
	 *        Runs when the user leaves the dialog without an accepted password, through any path
	 *        (cancel button, the window's close button, Escape). May be {@code null}.
	 */
	public static void openDialog(ReactContext context, DialogManager dialogManager, String title, String message,
			String fieldLabel, Predicate<char[]> verifier, Runnable onVerified, Runnable onCancel) {
		// One-shot guard so exactly one outcome is reported, regardless of how the dialog is left:
		// every dismissal funnels through the result handler, which must not report a cancel for the
		// dialog the accepted password has just closed.
		boolean[] verified = { false };

		AbstractFieldModel password = new AbstractFieldModel(null);
		password.setMandatory(true);
		password.setNullable(false);

		ReactPasswordInputControl input = new ReactPasswordInputControl(context, password);
		// The chrome follows the field's error, so a rejected password is drawn under the input.
		ReactControl field = new ReactFormFieldChromeControl(context, fieldLabel, input);

		Runnable closeHandler = () -> dialogManager.closeTopDialog(DialogResult.cancelled());
		ReactWindowControl window = new ReactWindowControl(context, title, WIDTH, closeHandler);
		// A question and one input have one size; nothing here is worth resizing.
		window.setResizable(false);
		// The window body is flush by design ("content owns its inset"), so the message and the field
		// are wrapped in a padded inset to match the padded header/footer.
		window.setChild(new ReactInsetControl(context,
			new ReactStackControl(context, List.of(new ReactTextControl(context, message), field))));

		List<ReactControl> actions = new ArrayList<>();
		actions.add(MessageButtons.cancel(context, ctx -> {
			dialogManager.closeTopDialog(DialogResult.cancelled());
			return HandlerResult.DEFAULT_RESULT;
		}));
		ReactButtonControl okButton = MessageButtons.ok(context, ctx -> {
			if (!accepted(password, verifier)) {
				return HandlerResult.DEFAULT_RESULT;
			}
			verified[0] = true;
			dialogManager.closeTopDialog(DialogResult.ok(null));
			onVerified.run();
			return HandlerResult.DEFAULT_RESULT;
		});
		// Confirming is the dialog's default action: primary-styled and Enter-bound, so a password
		// typed and confirmed with Enter submits.
		okButton.markAsDefault();
		actions.add(okButton);
		window.setActions(actions);

		dialogManager.openDialog(false, window, result -> {
			// Any dismissal that is not the accepted password counts as cancel.
			if (!verified[0] && onCancel != null) {
				onCancel.run();
			}
		});
	}

	/**
	 * Hands the entered password to the verifier and reports its verdict, emptying the input and
	 * marking it with the rejection when the password is not accepted.
	 */
	private static boolean accepted(AbstractFieldModel model, Predicate<char[]> verifier) {
		Object value = model.getValue();
		char[] entered = value == null ? new char[0] : value.toString().toCharArray();
		boolean accepted;
		try {
			accepted = verifier.test(entered);
		} finally {
			Arrays.fill(entered, (char) 0);
		}
		if (!accepted) {
			model.setValue(null);
			model.setError(I18NConstants.ERROR_WRONG_PASSWORD);
		}
		return accepted;
	}

}
