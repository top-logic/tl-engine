/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml.log;

import javax.xml.stream.Location;

import com.top_logic.basic.Log;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.util.ResKey;

/**
 * Internationalizable error reporting during XML stream parsing (and writing).
 * 
 * @see I18NLog
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface XMLStreamLog {

	/**
	 * Reports an {@link Level#ERROR} message.
	 *
	 * @param location
	 *        The {@link Location} where the error occurred in the current input.
	 * @param message
	 *        The error message.
	 */
	default void error(Location location, ResKey message) {
		error(location, message, null);
	}

	/**
	 * Reports an {@link Level#ERROR} message.
	 *
	 * @param location
	 *        The {@link Location} where the error occurred in the current input.
	 * @param message
	 *        The error message.
	 * @param ex
	 *        An optional stack trace where the problem occurred.
	 */
	default void error(Location location, ResKey message, Throwable ex) {
		log(Level.ERROR, location, message, ex);
	}

	/**
	 * Reports an {@link Level#INFO} message.
	 *
	 * @param location
	 *        The {@link Location} where the error occurred in the current input.
	 * @param message
	 *        The error message.
	 */
	default void info(Location location, ResKey message) {
		log(Level.INFO, location, message);
	}

	/**
	 * Reports a log message with a given {@link Level severity}.
	 *
	 * @param location
	 *        The {@link Location} where the error occurred in the current input.
	 * @param message
	 *        The error message.
	 */
	default void log(Level level, Location location, ResKey message) {
		log(level, location, message, null);
	}

	/**
	 * Reports a log message with a given {@link Level severity}.
	 *
	 * @param location
	 *        The {@link Location} where the error occurred in the current input.
	 * @param message
	 *        The error message.
	 * @param ex
	 *        An optional stack trace where the problem occurred.
	 */
	void log(Level level, Location location, ResKey message, Throwable ex);

	/**
	 * Down-grades this {@link XMLStreamLog} to a simple {@link Log}.
	 * 
	 * <p>
	 * Warning: When writing to the resulting {@link Log}, this {@link I18NLog} produces
	 * uninternationalized messages. Therefore the down-grade is only useful in test cases, for
	 * writing to the system log, or if the user of the resulting {@link Log} internally cares for
	 * internationalization and the resulting log messages are only used in one locale.
	 * </p>
	 * 
	 * @see I18NLog#asLog()
	 */
	default Log asLog(Provider<Location> locationProvider) {
		return asI18NLog(locationProvider).asLog();
	}

	/**
	 * Down-grades this {@link XMLStreamLog} to an {@link I18NLog}.
	 * 
	 * @param locationProvider
	 *        {@link Provider} that is requested for a {@link Location} description each time a log
	 *        message is logged to the resulting {@link I18NLog}.
	 */
	default I18NLog asI18NLog(Provider<Location> locationProvider) {
		return new I18NLog() {
			@Override
			public void log(Level level, ResKey message, Throwable ex) {
				XMLStreamLog.this.log(level, locationProvider.get(), message, ex);
			}
		};
	}

	/**
	 * Upgrades an {@link I18NLog} to a {@link XMLStreamLog}.
	 * 
	 * <p>
	 * {@link Location} information logged to the resulting {@link XMLStreamLog} is automatically
	 * encoded in the log messages using an {@link I18NConstants#AT_LOCATION__FILE_LINE_COL_DETAIL}
	 * message.
	 * </p>
	 * 
	 * @param log
	 *        The {@link I18NLog} to upgrade.
	 * @return A {@link XMLStreamLog} for parsing.
	 */
	static XMLStreamLog fromI18NLog(I18NLog log) {
		return new I18NLogAsXMLStreamLog(log);
	}

}
