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
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.exception.I18NFailure;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;
import com.top_logic.util.error.ErrorHandlingHelper;
import com.top_logic.util.error.TopLogicException;

/**
 * The single place where a failed command becomes a message the user can read.
 *
 * <p>
 * A command fails by throwing: a {@link TopLogicException} for a failure the user can act on, any
 * other {@link RuntimeException} for an internal problem. {@link #report(ErrorSink, Throwable, String, Class)}
 * writes the matching log entry and shows the exception in the window's snackbar,
 * {@link #show(ErrorSink, Throwable)} only shows it. Every server-side entry point that runs
 * application code on behalf of the client - the command dispatch, the upload endpoint, a value
 * listener of a field - reports its failures this way, so a failure looks the same to the user
 * wherever it happened.
 * </p>
 */
public final class CommandErrors {

	/**
	 * CSS classes of the summary line of a command-error message, separating it from the detail
	 * messages listed below it: the title of the alert the snackbar is (design system package).
	 */
	private static final String CSS_SNACKBAR_TITLE = "tl-alert__title tl-type-body-strong";

	private CommandErrors() {
		// Utility class.
	}

	/**
	 * The refusal of a command that is not executable in the given state.
	 *
	 * <p>
	 * The refusal has {@link ErrorSeverity#WARNING warning} severity, so that the user sees it as
	 * a hint rather than as a malfunction. Its title says that the command could not be executed,
	 * its {@link I18NRuntimeException#getDetails() details} give the state's reason, unless that
	 * is one of the generic reasons {@link ExecutableState#NOT_EXEC_HIDDEN_REASON} and
	 * {@link ExecutableState#NOT_EXEC_DISABLED_REASON} that would only repeat the title.
	 * </p>
	 *
	 * @param state
	 *        The state that keeps the command from executing.
	 * @return The exception to throw.
	 */
	public static TopLogicException notExecutable(ExecutableState state) {
		TopLogicException result =
			TopLogicException.warning(com.top_logic.layout.basic.I18NConstants.ERROR_COMMAND_NOT_EXECUTABLE);
		ResKey reason = state.getI18NReasonKey();
		boolean hasReason = reason != null && reason != ResKey.NONE
			&& reason != ExecutableState.NOT_EXEC_HIDDEN_REASON && reason != ExecutableState.NOT_EXEC_DISABLED_REASON;
		if (hasReason) {
			result.initDetails(reason);
		}
		return result;
	}

	/**
	 * A failure the user can act on, with a summary line and detail messages listed below it.
	 *
	 * <p>
	 * The details are chained as causes of the returned exception, which is where
	 * {@link #show(ErrorSink, Throwable)} finds them. The exception has
	 * {@link ErrorSeverity#ERROR error} severity: it reports a refusal, not a malfunction.
	 * </p>
	 *
	 * @param title
	 *        The summary of the failure.
	 * @param details
	 *        What the user has to know or change, one message each.
	 * @return The exception to throw.
	 */
	public static TopLogicException error(ResKey title, List<ResKey> details) {
		TopLogicException cause = null;
		for (int n = details.size() - 1; n >= 0; n--) {
			cause = cause == null ? new TopLogicException(details.get(n)) : new TopLogicException(details.get(n), cause);
		}
		TopLogicException result = cause == null ? new TopLogicException(title) : new TopLogicException(title, cause);
		result.initSeverity(ErrorSeverity.ERROR);
		return result;
	}

	/**
	 * Logs an exception thrown by application code and shows it in the given window.
	 *
	 * @param sink
	 *        Where to display the message, see {@link #show(ErrorSink, Throwable)}.
	 * @param ex
	 *        The exception that terminated the operation.
	 * @param description
	 *        Names the operation that failed, see {@link #log(Throwable, String, Class)}.
	 * @param caller
	 *        The class to log the entry for.
	 */
	public static void report(ErrorSink sink, Throwable ex, String description, Class<?> caller) {
		log(ex, description, caller);
		show(sink, ex);
	}

	/**
	 * Writes the log entry for an exception thrown by application code.
	 *
	 * <p>
	 * Only {@linkplain ErrorHandlingHelper#isInternalError(Throwable) internal errors} are logged
	 * as errors; user-level failures (e.g. a rejected login) are logged at info level, since the
	 * window shows them to the user anyway.
	 * </p>
	 *
	 * @param ex
	 *        The exception that terminated the operation.
	 * @param description
	 *        Names the operation that failed, e.g. the annotated handler and the control it was
	 *        invoked on. Used as prefix of the log message.
	 * @param caller
	 *        The class to log the entry for.
	 */
	public static void log(Throwable ex, String description, Class<?> caller) {
		if (ErrorHandlingHelper.isInternalError(ex)) {
			Logger.error(description + " failed.", ex, caller);
		} else {
			// A user-level problem (e.g. invalid input, denied login) that the UI reports to
			// the user anyway - not a malfunction worth an error log entry.
			Logger.info(description + " rejected: " + ex.getMessage(), caller);
		}
	}

	/**
	 * Converts an exception thrown by application code into a failed {@link HandlerResult} and logs
	 * it.
	 *
	 * @param ex
	 *        The exception that terminated the operation.
	 * @param description
	 *        Names the operation that failed, see {@link #log(Throwable, String, Class)}.
	 * @param caller
	 *        The class to log the entry for.
	 * @return The result reporting the failure to the client, {@link #show(ErrorSink, HandlerResult)
	 *         shown} as the exception itself.
	 */
	public static HandlerResult failure(Throwable ex, String description, Class<?> caller) {
		log(ex, description, caller);
		HandlerResult result = new HandlerResult();
		result.setException(ex instanceof I18NRuntimeException runtime ? runtime
			: new I18NRuntimeException(message(ex), ex));
		result.setErrorSeverity(severity(ex));
		return result;
	}

	/**
	 * The message an exception gives the user: the {@link I18NFailure#getErrorKey() message} of
	 * the first {@link I18NFailure} in its cause chain, its technical message for an exception
	 * without one.
	 *
	 * @param ex
	 *        The exception to describe.
	 * @return The message, never {@code null}.
	 */
	public static ResKey message(Throwable ex) {
		I18NFailure failure = findI18NFailure(ex);
		return failure != null ? failure.getErrorKey() : ResKey.text(ex.getMessage());
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
	 * Shows an exception thrown by a command in the snackbar of the given {@link ErrorSink}.
	 *
	 * <p>
	 * The first {@link I18NFailure} of the cause chain gives the summary line, its
	 * {@link I18NRuntimeException#getDetails() details} and the messages of the {@link I18NFailure}s
	 * chained below it as causes (e.g. the individual constraint violations behind a vetoed commit)
	 * are listed below the summary, so the user learns which value on which object was rejected
	 * and where one message ends and the next begins. An exception without such a message is shown
	 * as a generic failure, with its technical message as detail.
	 * </p>
	 *
	 * <p>
	 * The {@link I18NRuntimeException#getSeverity() severity} of the exception selects how the
	 * message is shown: {@link ErrorSeverity#INFO} as {@link ErrorSink#showInfo(HTMLFragment) info},
	 * {@link ErrorSeverity#WARNING} as {@link ErrorSink#showWarning(HTMLFragment) warning} (e.g. a
	 * command refused by its executability rule, see {@link #notExecutable(ExecutableState)}), and
	 * everything else as {@link ErrorSink#showError(HTMLFragment) error}.
	 * </p>
	 *
	 * @param sink
	 *        Where to display the message, <code>null</code> if the failing operation has no window
	 *        to report to. In that case, the failure is only logged.
	 * @param ex
	 *        The exception to report.
	 */
	public static void show(ErrorSink sink, Throwable ex) {
		show(sink, ex, severity(ex));
	}

	/**
	 * Shows a failed {@link HandlerResult} in the snackbar of the given {@link ErrorSink}.
	 *
	 * <p>
	 * A result carrying an {@link HandlerResult#getException() exception} is shown as that
	 * exception, see {@link #show(ErrorSink, Throwable)}; otherwise, its error title gives the
	 * summary line and its error message and encoded errors the details, shown with the result's
	 * {@link HandlerResult#getErrorSeverity() severity}.
	 * </p>
	 *
	 * @param sink
	 *        Where to display the message, <code>null</code> if the failing operation has no window
	 *        to report to. In that case, the failure is only logged.
	 * @param result
	 *        The failed result to report.
	 */
	public static void show(ErrorSink sink, HandlerResult result) {
		Throwable exception = result.getException();
		if (exception != null) {
			show(sink, exception, result.getErrorSeverity());
			return;
		}
		ResKey titleKey = result.getErrorTitle();
		List<ResKey> details = new ArrayList<>();
		Set<String> seen = new HashSet<>();
		Resources resources = Resources.getInstance();
		if (titleKey != null) {
			seen.add(resources.getString(titleKey));
		}
		addDetail(details, seen, resources, result.getErrorMessage());
		for (ResKey error : result.getEncodedErrors()) {
			addDetail(details, seen, resources, error);
		}
		display(sink, titleKey, details, result.getErrorSeverity());
	}

	private static void show(ErrorSink sink, Throwable ex, ErrorSeverity severity) {
		I18NFailure failure = findI18NFailure(ex);
		ResKey titleKey = failure != null ? failure.getErrorKey() : null;

		Resources resources = Resources.getInstance();
		Set<String> seen = new HashSet<>();
		if (titleKey != null) {
			seen.add(resources.getString(titleKey));
		}
		List<ResKey> details = new ArrayList<>();
		if (failure == null) {
			addDetail(details, seen, resources, message(ex));
		} else {
			if (failure instanceof I18NRuntimeException runtime) {
				addDetail(details, seen, resources, runtime.getDetails());
			}
			for (Throwable cause = ((Throwable) failure).getCause(); cause != null; cause = cause.getCause()) {
				if (cause instanceof I18NFailure causeFailure) {
					addDetail(details, seen, resources, causeFailure.getErrorKey());
				}
			}
		}
		display(sink, titleKey, details, severity);
	}

	private static void display(ErrorSink sink, ResKey titleKey, List<ResKey> details, ErrorSeverity severity) {
		if (sink == null) {
			Logger.warn("No ErrorSink available to show command error: " + titleKey, CommandErrors.class);
			return;
		}

		HTMLFragment title = Fragments.div(CSS_SNACKBAR_TITLE,
			titleKey != null ? Fragments.message(titleKey) : Fragments.message(I18NConstants.ERROR_COMMAND_FAILED));

		HTMLFragment message = Fragments.concat(title, Fragments.messageList(details));
		switch (severity) {
			case INFO -> sink.showInfo(message);
			case WARNING -> sink.showWarning(message);
			case ERROR, SYSTEM_FAILURE -> sink.showError(message);
		}
	}

	/**
	 * The severity of the first {@link I18NRuntimeException} in the cause chain of the given
	 * exception, {@link ErrorSeverity#ERROR} for an exception without one.
	 */
	private static ErrorSeverity severity(Throwable ex) {
		for (Throwable current = ex; current != null; current = current.getCause()) {
			if (current instanceof I18NRuntimeException runtime) {
				return runtime.getSeverity();
			}
			if (current instanceof I18NFailure) {
				return ErrorSeverity.ERROR;
			}
		}
		return ErrorSeverity.ERROR;
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
