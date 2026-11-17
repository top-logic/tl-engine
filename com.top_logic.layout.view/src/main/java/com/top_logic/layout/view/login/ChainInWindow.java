/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import com.top_logic.basic.Logger;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.CommandErrors;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.scripting.ReactWindowReplay;
import com.top_logic.layout.view.command.Continuation;

/**
 * Takes up a command chain that waits in a browser window, from a request of another window.
 *
 * <p>
 * An action that waits for an external identity provider suspends its chain in the window the
 * command was run in; the answer of the provider arrives on the thread of a request of another
 * window of the same session. These are the steps that carry the rest of the chain over into the
 * waiting window, shared by the actions waiting this way.
 * </p>
 *
 * @see VerifyIdentityAction
 * @see IdentifyExternallyAction
 */
public final class ChainInWindow {

	/**
	 * Names the operation a failure is reported for: the command chain taken up again after the
	 * external identity provider has answered.
	 */
	private static final String RESUME_DESCRIPTION = "Continuing the command chain after the identity confirmation";

	private ChainInWindow() {
		// Static utility.
	}

	/**
	 * Runs the given action in the window of the given context, from whatever thread is calling.
	 *
	 * <p>
	 * The action is run in the window's sub-session and as one interaction of the session, so that
	 * its display updates reach that window's page and its texts are resolved in that window's
	 * language. A window whose page is gone is not acted on.
	 * </p>
	 *
	 * @return Whether the action has been run, {@code false} when the window is gone.
	 */
	public static boolean runInWindow(ReactContext context, Runnable action) {
		String windowName = context.getWindowName();
		if (!ReactWindowReplay.inWindow(context.getWindowRegistry(), windowName, action)) {
			Logger.warn("Window '" + windowName + "' is gone, the command chain waiting in it does not continue.",
				ChainInWindow.class);
			return false;
		}
		return true;
	}

	/**
	 * Continues the chain, reporting what it throws in the window the chain belongs to.
	 *
	 * <p>
	 * The failure travels the way a failed command travels: an internal error is logged as one and
	 * shown generically, a user-level failure carries its own message into the window's snackbar,
	 * see {@link CommandErrors#report(ErrorSink, Throwable, String, Class)}.
	 * </p>
	 *
	 * @param context
	 *        The React context of the window the chain was suspended in.
	 * @param input
	 *        The value the chain continues with.
	 * @param continuation
	 *        The suspended chain.
	 * @param caller
	 *        The action that suspended the chain, under which a failure is logged.
	 */
	public static void resume(ReactContext context, Object input, Continuation continuation, Class<?> caller) {
		try {
			continuation.resume(input);
		} catch (Throwable ex) {
			CommandErrors.report(context.getErrorSink(), ex, RESUME_DESCRIPTION, caller);
		}
	}

}
