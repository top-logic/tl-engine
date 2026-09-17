/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ButtonAppearance;
import com.top_logic.layout.react.control.button.MessageButtons;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.layout.ReactInsetControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.overlay.DialogHandle;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.ReactWindowControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Builds and opens a dialog that sends the user to an external sign-in and waits for them to come
 * back.
 *
 * <p>
 * The dialog is a message and a button opening the sign-in in a browser window of its own, so that
 * the page holding the dialog keeps running while the user authenticates elsewhere. Nothing in the
 * dialog confirms anything: what the user does at the provider comes back on a request of its own,
 * and the caller closes the dialog when it arrives. Until then the user may leave the dialog - the
 * cancel button, the window's close button, Escape - which reports a cancel, and exactly one of the
 * two outcomes is reported.
 * </p>
 *
 * <p>
 * The sign-in window is opened from the user's click on the button, which is what tells a browser
 * that it is a window the user asked for: a page cannot open one on its own because an event from
 * the server told it to.
 * </p>
 */
public class ReauthenticationPromptDialogControl {

	/** Width of the prompt window. */
	private static final DisplayDimension WIDTH = DisplayDimension.px(460);

	/**
	 * Opens a dialog asking the user to sign in again at an external identity provider.
	 *
	 * @param context
	 *        The React context.
	 * @param dialogManager
	 *        The dialog manager for opening and closing the dialog.
	 * @param title
	 *        The dialog window title.
	 * @param message
	 *        The explanation, saying where the user signs in and that the dialog closes by itself.
	 * @param buttonLabel
	 *        The label of the button opening the sign-in.
	 * @param reauthenticationUrl
	 *        Where the identity provider authenticates the user again.
	 * @param onCancel
	 *        Runs when the user leaves the dialog without the confirmation having arrived, through
	 *        any path (cancel button, the window's close button, Escape). May be {@code null}.
	 * @return Closes the dialog as confirmed, reporting no cancel. To be run once the confirmation
	 *         has arrived, from the thread of the request that brought it and within that window's
	 *         interaction. Running it more than once closes the dialog once.
	 */
	public static Runnable openDialog(ReactContext context, DialogManager dialogManager, String title, String message,
			String buttonLabel, String reauthenticationUrl, Runnable onCancel) {
		// One-shot guard so exactly one outcome is reported: the dismissal that the arriving
		// confirmation causes is not a cancel, and that confirmation arrives on another thread than
		// the one opening the dialog.
		AtomicBoolean confirmed = new AtomicBoolean();

		ReactWindowControl window = new ReactWindowControl(context, title, WIDTH,
			() -> dialogManager.closeTopDialog(DialogResult.cancelled()));
		// A message and one button have one size; nothing here is worth resizing.
		window.setResizable(false);
		// The window body is flush by design ("content owns its inset"), so the message is wrapped in
		// a padded inset to match the padded header/footer.
		window.setChild(new ReactInsetControl(context,
			new ReactStackControl(context, List.of(new ReactTextControl(context, message)))));

		ReactButtonControl signIn = new ReactButtonControl(context, buttonLabel, ctx -> HandlerResult.DEFAULT_RESULT);
		// The sign-in is a page of the identity provider, reached without a server round trip, and it
		// gets a window of its own so that this one stays and can be closed from here. The window
		// belongs to this page, which is what lets the page the provider sends the user back to close
		// it once the confirmation has been carried here.
		signIn.setNavigateUrl(reauthenticationUrl);
		signIn.setNavigateNewWindow(true);
		signIn.setAppearance(ButtonAppearance.PRIMARY);

		List<ReactControl> actions = new ArrayList<>();
		actions.add(MessageButtons.cancel(context, ctx -> {
			dialogManager.closeTopDialog(DialogResult.cancelled());
			return HandlerResult.DEFAULT_RESULT;
		}));
		actions.add(signIn);
		window.setActions(actions);

		DialogHandle handle = dialogManager.openDialog(false, window, result -> {
			// Any dismissal before the confirmation has arrived counts as cancel.
			if (!confirmed.get() && onCancel != null) {
				onCancel.run();
			}
		});
		return () -> {
			if (confirmed.compareAndSet(false, true)) {
				handle.close(DialogResult.ok(null));
			}
		};
	}

}
