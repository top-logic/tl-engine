/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.i18n.log;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.util.I18NBundle;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;

/**
 * {@link I18NLog} buffering logged messages.
 * 
 * @see #getEntries()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BufferingI18NLog implements I18NLog {

	/**
	 * A buffered log entry from an {@link I18NLog}.
	 * 
	 * @see #getMessage()
	 */
	public static class Entry {

		private final Level _level;

		private final ResKey _message;

		private final Throwable _ex;

		/**
		 * Creates an {@link Entry}.
		 * 
		 * @param level
		 *        See {@link #getLevel()}.
		 * @param message
		 *        See {@link #getMessage()}.
		 * @param ex
		 *        See {@link #getProblem()}.
		 */
		public Entry(Level level, ResKey message, Throwable ex) {
			_level = level;
			_message = message;
			_ex = ex;
		}
		
		/**
		 * The log {@link Level} of the {@link #getMessage()}.
		 */
		public Level getLevel() {
			return _level;
		}

		/**
		 * The internationalizable {@link I18NLog log} message.
		 */
		public ResKey getMessage() {
			return _message;
		}
		
		/**
		 * An optional stack trace identifying the source code location from where the message was
		 * issued.
		 */
		public Throwable getProblem() {
			return _ex;
		}

		/**
		 * Sends the contents of this log entry to the application log.
		 * 
		 * @param source
		 *        The log source to use.
		 */
		public void forwardToApplicationLog(Class<?> source) {
			I18NBundle logBundle = ResourcesModule.getInstance().getLogBundle();
			String msg = logBundle.getString(getMessage());
			Throwable ex = getProblem();

			switch (getLevel()) {
				case DEBUG:
					Logger.error(msg, ex, source);
					break;
				case INFO:
					Logger.info(msg, ex, source);
					break;
				case WARN:
					Logger.warn(msg, ex, source);
					break;
				case ERROR:
					Logger.error(msg, ex, source);
					break;
				case FATAL:
					Logger.fatal(msg, ex, source);
					break;
			}
		}

	}

	private List<Entry> _entries = new ArrayList<>();

	/**
	 * Creates a {@link BufferingI18NLog}.
	 */
	public BufferingI18NLog() {
		super();
	}

	@Override
	public void log(Level level, ResKey message, Throwable ex) {
		_entries.add(new Entry(level, message, ex));
	}

	/**
	 * All buffered log {@link Entry} instances.
	 */
	public List<Entry> getEntries() {
		return _entries;
	}

	/**
	 * Whether {@link #getEntries()} contains any {@link Entry} instances.
	 */
	public boolean hasEntries() {
		return !_entries.isEmpty();
	}

	/**
	 * Whether {@link #getEntries()} contains {@link Entry} instances with log level
	 * {@link Level#ERROR} or above.
	 */
	public boolean hasErrors() {
		return hasEntries(Level.ERROR);
	}

	/**
	 * Whether {@link #getEntries()} contains {@link Entry} instances with the given log level or
	 * above.
	 */
	public boolean hasEntries(Level level) {
		for (Entry entry : _entries) {
			if (entry.getLevel().is(level)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Converts entries into an exception with the given message and the entries as details.
	 */
	public I18NRuntimeException asException(ResKey message) {
		// Hack to get a structured error display: Join multiple errors in a chain of
		// TopLogicException instances.
		Throwable details = null;
		List<Entry> entries = getEntries();
		for (int n = entries.size() - 1; n >= 0; n--) {
			Entry e = entries.get(n);
			details = new I18NRuntimeException(e.getMessage(), details);
		}
		return new I18NRuntimeException(message, details).initSeverity(ErrorSeverity.WARNING);
	}

	/**
	 * Forwards all collected entries to the application log.
	 * 
	 * @param source
	 *        The log source to use.
	 */
	public void forwardToApplicationLog(Class<?> source) {
		for (Entry entry : _entries) {
			entry.forwardToApplicationLog(source);
		}
	}

}
