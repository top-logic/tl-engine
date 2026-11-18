/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.CommandErrors;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * Tells the user something in passing - that what they asked for happened, or why it did not.
 *
 * <p>
 * The message goes to the {@link ErrorSink} of the window being served, which shows it where the
 * user is looking. A context without a sink - a command running outside a window - says nothing
 * rather than failing.
 * </p>
 */
public class ViewMessages {

	/**
	 * Reports that something the user asked for has happened.
	 */
	public static void info(ReactContext context, ResKey message) {
		show(context, message, ErrorSink::showInfo);
	}

	/**
	 * Reports something the user should be aware of without it having stopped them.
	 */
	public static void warning(ReactContext context, ResKey message) {
		show(context, message, ErrorSink::showWarning);
	}

	/**
	 * Reports that something the user asked for did not happen.
	 */
	public static void error(ReactContext context, ResKey message) {
		show(context, message, ErrorSink::showError);
	}

	/**
	 * Reports that something the user asked for did not happen, and what stood in the way.
	 *
	 * @param details
	 *        What went wrong underneath - typically the message of an exception, which says
	 *        something to whoever reports the problem even when it says little to the user.
	 */
	public static void error(ReactContext context, ResKey message, ResKey details) {
		error(context, ResKey.message(message, details));
	}

	/**
	 * Reports that an object was not edited, because someone else holds its lock.
	 *
	 * @param title
	 *        What became of the user's edit, e.g. that the object is shown read-only.
	 * @param conflict
	 *        The failure of acquiring the lock, whose details say who holds it (where the
	 *        application reports the holder of a lock).
	 */
	public static void lockConflict(ReactContext context, ResKey title, TopLogicException conflict) {
		TopLogicException info = new TopLogicException(title);
		info.initDetails(conflict.getDetails());
		info.initSeverity(ErrorSeverity.INFO);
		CommandErrors.show(context == null ? null : context.getErrorSink(), info);
	}

	private static void show(ReactContext context, ResKey message, Report report) {
		ErrorSink sink = context == null ? null : context.getErrorSink();
		if (sink == null) {
			return;
		}
		report.to(sink, Fragments.text(Resources.getInstance().getString(message)));
	}

	/**
	 * One of the {@link ErrorSink} methods, so that the three kinds of message differ in nothing
	 * but which of them is called.
	 */
	private interface Report {
		/**
		 * Shows the given content on the given sink.
		 */
		void to(ErrorSink sink, HTMLFragment content);
	}

}
