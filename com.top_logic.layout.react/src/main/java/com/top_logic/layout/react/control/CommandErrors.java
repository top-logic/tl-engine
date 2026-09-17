/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.Logger;
import com.top_logic.basic.exception.I18NFailure;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.error.ErrorHandlingHelper;

/**
 * The single place where a failed command becomes a message the user can read.
 *
 * <p>
 * {@link #failure(Throwable, String, Class)} turns an exception into a
 * {@link HandlerResult#isSuccess() failed} {@link HandlerResult} and writes the matching log entry,
 * {@link #show(ErrorSink, HandlerResult)} renders such a result into the window's snackbar. Every
 * server-side entry point that runs application code on behalf of the client - the command
 * dispatch, the upload endpoint - reports its failures through these two methods, so a failure
 * looks the same to the user wherever it happened.
 * </p>
 */
public final class CommandErrors {

	/**
	 * CSS class of the summary line of a command-error message, separating it from the detail
	 * messages listed below it.
	 */
	private static final String CSS_SNACKBAR_TITLE = "tlSnackbar__title";

	private CommandErrors() {
		// Utility class.
	}

	/**
	 * Converts an exception thrown by application code into a failed {@link HandlerResult} and logs
	 * it.
	 *
	 * <p>
	 * An {@link I18NFailure} anywhere in the cause chain contributes its
	 * {@linkplain I18NFailure#getErrorKey() user-visible error message}; any other exception
	 * produces a generic error result with the exception message.
	 * </p>
	 *
	 * <p>
	 * Only {@linkplain ErrorHandlingHelper#isInternalError(Throwable) internal errors} are logged
	 * as errors; user-level failures (e.g. a rejected login) are logged at info level, since the
	 * error result already reports them to the user.
	 * </p>
	 *
	 * @param ex
	 *        The exception that terminated the operation.
	 * @param description
	 *        Names the operation that failed, e.g. the annotated handler and the control it was
	 *        invoked on. Used as prefix of the log message.
	 * @param caller
	 *        The class to log the entry for.
	 * @return The result reporting the failure to the client.
	 */
	public static HandlerResult failure(Throwable ex, String description, Class<?> caller) {
		if (ErrorHandlingHelper.isInternalError(ex)) {
			Logger.error(description + " failed.", ex, caller);
		} else {
			// A user-level problem (e.g. invalid input, denied login) that the UI reports to
			// the user anyway - not a malfunction worth an error log entry.
			Logger.info(description + " rejected: " + ex.getMessage(), caller);
		}
		I18NFailure i18n = findI18NFailure(ex);
		if (i18n != null) {
			return HandlerResult.error(i18n.getErrorKey(), ex);
		}
		return HandlerResult.error(ResKey.text(ex.getMessage()), ex);
	}

	/**
	 * Searches the exception cause chain for an {@link I18NFailure}.
	 *
	 * @param ex
	 *        The exception to start the search at.
	 * @return The first {@link I18NFailure} found, or <code>null</code> if the chain holds none.
	 */
	public static I18NFailure findI18NFailure(Throwable ex) {
		Throwable current = ex;
		while (current != null) {
			if (current instanceof I18NFailure) {
				return (I18NFailure) current;
			}
			current = current.getCause();
		}
		return null;
	}

	/**
	 * Shows a failed {@link HandlerResult} in the snackbar of the given {@link ErrorSink}.
	 *
	 * <p>
	 * Instead of answering the request with an error status, the error message from the
	 * {@link HandlerResult} is forwarded to the snackbar so the user sees what went wrong. Detail
	 * messages chained as exception causes (e.g. the individual constraint violations behind a
	 * vetoed commit) are listed below the summary, so the user learns which value on which object
	 * was rejected and where one message ends and the next begins.
	 * </p>
	 *
	 * @param sink
	 *        Where to display the message, <code>null</code> if the failing operation has no window
	 *        to report to. In that case, the failure is only logged.
	 * @param result
	 *        The failed result to report.
	 */
	public static void show(ErrorSink sink, HandlerResult result) {
		if (sink == null) {
			Logger.warn("No ErrorSink available to show command error: " + result.getErrorTitle(),
				CommandErrors.class);
			return;
		}

		ResKey titleKey = result.getErrorTitle();
		HTMLFragment title = Fragments.div(CSS_SNACKBAR_TITLE,
			titleKey != null ? Fragments.message(titleKey) : Fragments.message(I18NConstants.ERROR_COMMAND_FAILED));

		sink.showError(Fragments.concat(title, Fragments.messageList(errorDetails(result))));
	}

	/**
	 * The detail messages of a failed command, each describing one aspect of the failure.
	 *
	 * <p>
	 * The same message can arrive through several routes at once: title and message both fall back
	 * to the exception's error key, and the original exception reappears as cause of the wrapper
	 * created by {@link HandlerResult#error(ResKey, Throwable)}. Each distinct message is therefore
	 * reported only once, and a message already shown as the summary is dropped.
	 * </p>
	 */
	private static List<ResKey> errorDetails(HandlerResult result) {
		Resources resources = Resources.getInstance();

		Set<String> seen = new HashSet<>();
		ResKey titleKey = result.getErrorTitle();
		if (titleKey != null) {
			seen.add(resources.getString(titleKey));
		}

		List<ResKey> details = new ArrayList<>();
		addDetail(details, seen, resources, result.getErrorMessage());
		// The list HandlerResult#error(ResKey) fills - the plainest way for a command to fail.
		// Without it, the snackbar shows the generic "command failed" title and no detail at all.
		for (ResKey error : result.getEncodedErrors()) {
			addDetail(details, seen, resources, error);
		}
		if (result.getException() != null) {
			for (Throwable cause = result.getException().getCause(); cause != null; cause = cause.getCause()) {
				if (cause instanceof I18NFailure failure) {
					addDetail(details, seen, resources, failure.getErrorKey());
				}
			}
		}
		return details;
	}

	/**
	 * Appends the given message unless it is empty or was already reported.
	 */
	private static void addDetail(List<ResKey> details, Set<String> seen, Resources resources, ResKey messageKey) {
		if (messageKey == null) {
			return;
		}
		String message = resources.getString(messageKey);
		if (message == null || message.isEmpty() || message.equals("null")) {
			return;
		}
		if (seen.add(message)) {
			details.add(messageKey);
		}
	}
}
