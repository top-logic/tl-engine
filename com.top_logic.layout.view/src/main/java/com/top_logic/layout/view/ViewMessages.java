/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.util.Resources;

/**
 * Tells the user something in passing - that what they asked for happened, or why it did not.
 *
 * <p>
 * The message goes to the {@link ErrorSink} of the window being served, which shows it where the
 * user is looking. A context without a sink - a command running outside a window - says nothing
 * rather than failing.
 * </p>
 *
 * @implNote Not {@link com.top_logic.event.infoservice.InfoService}: that one addresses the message
 *           area of a {@code MainLayout}, which a React window has none of, so it raises a
 *           {@link NullPointerException} while composing the message and emits a call to a script
 *           function this UI does not define.
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
