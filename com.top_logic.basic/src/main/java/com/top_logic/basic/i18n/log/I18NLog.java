/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.i18n.log;

import com.top_logic.basic.Log;
import com.top_logic.basic.func.IFunction3;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.util.ResKey;

/**
 * A sink of internationalizable log messages.
 * 
 * @see Log
 * @see BufferingI18NLog
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface I18NLog {

	/**
	 * Logs an {@link Level#ERROR} message.
	 * 
	 * @see #log(Level, ResKey, Throwable)
	 */
	default void error(ResKey message) {
		log(Level.ERROR, message, null);
	}

	/**
	 * Logs an {@link Level#ERROR} or {@link Level#FATAL} message (depending on the existence of a
	 * given stacktrace).
	 * 
	 * @see #log(Level, ResKey, Throwable)
	 */
	default void fatal(ResKey message, Throwable ex) {
		log(Level.FATAL, message, ex);
	}

	/**
	 * Logs an {@link Level#INFO} message.
	 * 
	 * @see #log(Level, ResKey, Throwable)
	 */
	default void info(ResKey message) {
		log(Level.INFO, message);
	}

	/**
	 * Logs a message with a certain log {@link Level}.
	 *
	 * @param level
	 *        The severity level.
	 * @param message
	 *        The log message.
	 */
	default void log(Level level, ResKey message) {
		log(level, message, null);
	}

	/**
	 * Logs a message with a certain log {@link Level}.
	 *
	 * @param level
	 *        The severity level.
	 * @param message
	 *        The log message.
	 * @param ex
	 *        An optional stacktrace identifying the source code location that is the original cause
	 *        of the message.
	 */
	void log(Level level, ResKey message, Throwable ex);

	/**
	 * Down-grades this {@link I18NLog} to a simple {@link Log}.
	 * 
	 * <p>
	 * Warning: When writing to the resulting {@link Log}, this {@link I18NLog} produces
	 * uninternationalized messages. Therefore the down-grade is only useful in test cases, for
	 * writing to the system log, or if the user of the resulting {@link Log} internally cares for
	 * internationalization and the resulting log messages are only used in one locale.
	 * </p>
	 */
	default Log asLog() {
		return new I18NLogAsLog(this);
	}

	/**
	 * Creates a new {@link I18NLog} that logs to this log and to the other given log in parallel.
	 *
	 * @param other
	 *        The other {@link I18NLog} to forward log messages to.
	 * @return A log logging to both, this and the other log.
	 */
	default I18NLog tee(I18NLog other) {
		I18NLog self = this;
		return new I18NLog() {
			@Override
			public void log(Level level, ResKey message, Throwable ex) {
				self.log(level, message, ex);
				other.log(level, message, ex);
			}
		};
	}

	/**
	 * Creates a filtering {@link I18NLog} only forwarding messages matching the given log level to
	 * this log.
	 *
	 * @param logLevel
	 *        The log level incoming messages must at least have to be forwarded to this log.
	 * @return A filtering log.
	 * 
	 * @see Level#is(Level)
	 */
	default I18NLog filter(Level logLevel) {
		return filter((level, message, ex) -> level.is(logLevel));
	}

	/**
	 * Creates a filtering {@link I18NLog} only forwarding messages matching the given predicate to
	 * this log.
	 *
	 * @param predicate
	 *        A predicate function to test log messages with.
	 * @return A filtering log.
	 */
	default I18NLog filter(IFunction3<Boolean, Level, ResKey, Throwable> predicate) {
		I18NLog target = this;
		return new I18NLog() {
			@Override
			public void log(Level level, ResKey message, Throwable ex) {
				if (predicate.apply(level, message, ex)) {
					target.log(level, message, ex);
				}
			}
		};
	}

}
