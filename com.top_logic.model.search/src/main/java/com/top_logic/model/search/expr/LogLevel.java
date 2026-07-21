/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.Logger;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.util.error.TopLogicException;

/**
 * Severity of a message reported from <i>TLScript</i> through the {@link Log} or {@link Info}
 * function.
 *
 * <p>
 * A level both selects the {@link Logger} method used by {@link Log} and the {@link InfoService}
 * display style used by {@link Info}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
enum LogLevel {

	/** Diagnostic message, only relevant during development. */
	DEBUG,

	/** Informational message. */
	INFO,

	/** Message about a potential problem. */
	WARN,

	/** Message about a problem that prevented an operation from completing. */
	ERROR,

	/** Message about a problem that renders the application unusable. */
	FATAL;

	/**
	 * Conventional name of the <i>TLScript</i> function argument selecting a {@link LogLevel}.
	 */
	public static final String ARGUMENT = "level";

	/**
	 * Writes the given message to the application log using the {@link Logger} method matching this
	 * level.
	 *
	 * @param message
	 *        The message to write.
	 * @param caller
	 *        The caller to report to the {@link Logger}.
	 */
	public void log(String message, Object caller) {
		switch (this) {
			case DEBUG:
				Logger.debug(message, caller);
				break;
			case INFO:
				Logger.info(message, caller);
				break;
			case WARN:
				Logger.warn(message, caller);
				break;
			case ERROR:
				Logger.error(message, caller);
				break;
			case FATAL:
				Logger.fatal(message, caller);
				break;
		}
	}

	/**
	 * Displays the given fragment on the user interface using the {@link InfoService} style matching
	 * this level.
	 *
	 * <p>
	 * As the {@link InfoService} only distinguishes info, warning and error, {@link #DEBUG} is
	 * displayed as {@link #INFO} and {@link #FATAL} as {@link #ERROR}.
	 * </p>
	 *
	 * @param item
	 *        The message fragment to display.
	 */
	public void showInUI(HTMLFragment item) {
		switch (this) {
			case DEBUG:
			case INFO:
				InfoService.showInfo(item);
				break;
			case WARN:
				InfoService.showWarning(item);
				break;
			case ERROR:
			case FATAL:
				InfoService.showError(item);
				break;
		}
	}

	/**
	 * Resolves the {@link LogLevel} for the given <i>TLScript</i> value.
	 *
	 * @param value
	 *        The level name (case-insensitive), or <code>null</code> for {@link #INFO}.
	 * @return The resolved {@link LogLevel}.
	 * @throws TopLogicException
	 *         If the given value does not name a {@link LogLevel}.
	 */
	public static LogLevel parse(Object value) {
		if (value == null) {
			return INFO;
		}
		String name = value.toString().trim();
		for (LogLevel level : values()) {
			if (level.name().equalsIgnoreCase(name)) {
				return level;
			}
		}
		throw new TopLogicException(I18NConstants.ERROR_INVALID_LOG_LEVEL__VALUE_OPTIONS.fill(name, options()));
	}

	/**
	 * The default level name to use when no level is given.
	 */
	public static String defaultName() {
		return INFO.name().toLowerCase();
	}

	private static String options() {
		return Arrays.stream(values()).map(level -> level.name().toLowerCase()).collect(Collectors.joining(", "));
	}
}
