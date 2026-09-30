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
import com.top_logic.basic.logging.Level;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.util.error.TopLogicException;

/**
 * Support for the selectable message {@link Level} of the {@link Log} and {@link Info} functions.
 *
 * <p>
 * A {@link Level} selects both the {@link Logger} method used by {@link Log} and the
 * {@link InfoService} display style used by {@link Info}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class ScriptLog {

	/**
	 * Conventional name of the <i>TLScript</i> function argument selecting a {@link Level}.
	 */
	public static final String ARGUMENT = "level";

	/**
	 * Displays the given fragment on the user interface using the {@link InfoService} style matching
	 * the given {@link Level}.
	 *
	 * <p>
	 * As the {@link InfoService} only distinguishes info, warning and error, {@link Level#DEBUG} is
	 * displayed as {@link Level#INFO} and {@link Level#FATAL} as {@link Level#ERROR}.
	 * </p>
	 *
	 * @param level
	 *        The severity to display with.
	 * @param item
	 *        The message fragment to display.
	 */
	public static void showInUI(Level level, HTMLFragment item) {
		switch (level) {
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
	 * Resolves the {@link Level} for the given <i>TLScript</i> value.
	 *
	 * @param value
	 *        The level name (case-insensitive), or <code>null</code> for {@link Level#INFO}.
	 * @return The resolved {@link Level}.
	 * @throws TopLogicException
	 *         If the given value does not name a {@link Level}.
	 */
	public static Level parse(Object value) {
		if (value == null) {
			return Level.INFO;
		}
		String name = value.toString().trim();
		for (Level level : Level.values()) {
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
		return Level.INFO.name().toLowerCase();
	}

	private static String options() {
		return Arrays.stream(Level.values()).map(level -> level.name().toLowerCase())
			.collect(Collectors.joining(", "));
	}
}
