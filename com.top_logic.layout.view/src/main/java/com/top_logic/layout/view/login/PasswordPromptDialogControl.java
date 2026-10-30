/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.MessageButtons;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.form.ReactPasswordInputControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.layout.ReactInsetControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.ReactWindowControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Builds and opens a dialog asking the user to enter a secret - a password or a one-time code - and
 * reports whether the entry was accepted.
 *
 * <p>
 * The dialog is a message, one input and the standard OK/Cancel pair, composed of the same React
 * controls a form is built from. The {@link EntryKind} decides the input: a masked one for a
 * password, a plain text input for a one-time code that is read off another device and typed in.
 * What "accepted" means is not decided here: the caller passes the check as a {@link Verifier}, so
 * the same prompt serves whatever needs a password or a code confirmed.
 * </p>
 *
 * <p>
 * A rejected entry leaves the dialog open with the input emptied and the reason the verifier gave
 * for the rejection shown under it, so the user can try again. Only an accepted entry reports
 * success; every other way of leaving the dialog - the cancel button, the window's close button,
 * Escape - reports a cancel, and exactly one of the two outcomes is reported.
 * </p>
 */
public class PasswordPromptDialogControl {

	/**
	 * The check a password or code entered in the prompt has to pass.
	 */
	@FunctionalInterface
	public interface Verifier {

		/**
		 * Checks the entered password or code.
		 *
		 * @param password
		 *        The characters the user entered, which the dialog owns and clears once the check
		 *        has returned - a verifier keeping them must copy them.
		 * @return {@code null} if the entry is accepted, otherwise the reason for the rejection,
		 *         shown under the input.
		 */
		ResKey verify(char[] password);
	}

	/**
	 * What the user enters in the prompt, deciding the input that asks for it.
	 */
	public enum EntryKind {

		/** A password, entered in a masked input. */
		PASSWORD,

		/**
		 * A one-time code, entered in a plain text input: it is short-lived, read off another
		 * device, and the user checks what they typed against it.
		 */
		ONE_TIME_CODE;
	}

	/** Width of the prompt window. */
	private static final DisplayDimension WIDTH = DisplayDimension.px(420);

	/**
	 * Opens a dialog asking for a password, in a masked input.
	 *
	 * @see #openDialog(ReactContext, DialogManager, String, String, String, EntryKind, Verifier,
	 *      Runnable, Runnable)
	 */
	public static void openDialog(ReactContext context, DialogManager dialogManager, String title, String message,
			String fieldLabel, Verifier verifier, Runnable onVerified, Runnable onCancel) {
		openDialog(context, dialogManager, title, message, fieldLabel, EntryKind.PASSWORD, verifier, onVerified,
			onCancel);
	}

	/**
	 * Opens a dialog asking for a secret entry of the given kind.
	 *
	 * @param context
	 *        The React context.
	 * @param dialogManager
	 *        The dialog manager for opening and closing the dialog.
	 * @param title
	 *        The dialog window title.
	 * @param message
	 *        The explanation shown above the input, saying what the entry is asked for.
	 * @param fieldLabel
	 *        The label of the input.
	 * @param kind
	 *        What is entered, deciding the input that asks for it.
	 * @param verifier
	 *        Decides whether the entry is accepted, and why not.
	 * @param onVerified
	 *        Runs once the user has entered something the {@code verifier} accepts.
	 * @param onCancel
	 *        Runs when the user leaves the dialog without an accepted entry, through any path
	 *        (cancel button, the window's close button, Escape). May be {@code null}.
	 */
	public static void openDialog(ReactContext context, DialogManager dialogManager, String title, String message,
			String fieldLabel, EntryKind kind, Verifier verifier, Runnable onVerified, Runnable onCancel) {
		// One-shot guard so exactly one outcome is reported, regardless of how the dialog is left:
		// every dismissal funnels through the result handler, which must not report a cancel for the
		// dialog the accepted password has just closed.
		boolean[] verified = { false };

		AbstractFieldModel password = new AbstractFieldModel(null);
		password.setMandatory(true);
		password.setNullable(false);

		ReactFormFieldControl input = createInput(context, password, kind);
		// The chrome follows the field's error, so a rejected entry is drawn under the input.
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
	 * The input asking for an entry of the given kind.
	 */
	private static ReactFormFieldControl createInput(ReactContext context, AbstractFieldModel model,
			EntryKind kind) {
		switch (kind) {
			case PASSWORD:
				return new ReactPasswordInputControl(context, model);
			case ONE_TIME_CODE:
				return new ReactTextInputControl(context, model);
		}
		throw new IllegalArgumentException("Unknown entry kind: " + kind);
	}

	/**
	 * Hands the entry to the verifier and reports its verdict, emptying the input and marking it
	 * with the reason for the rejection when the entry is not accepted.
	 */
	private static boolean accepted(AbstractFieldModel model, Verifier verifier) {
		Object value = model.getValue();
		char[] entered = value == null ? new char[0] : value.toString().toCharArray();
		ResKey rejection;
		try {
			rejection = verifier.verify(entered);
		} finally {
			Arrays.fill(entered, (char) 0);
		}
		if (rejection != null) {
			model.setValue(null);
			model.setError(rejection);
			return false;
		}
		return true;
	}

}
