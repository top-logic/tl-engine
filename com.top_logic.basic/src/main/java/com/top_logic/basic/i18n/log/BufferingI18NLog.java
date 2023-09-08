/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.i18n.log;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.logging.Level;
import com.top_logic.basic.util.ResKey;

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
	 * Whether {@link #getEntries()} contains {@link Entry} instances with {@link Level}
	 * {@link Level#ERROR} or above.
	 */
	public boolean hasEntries() {
		return !_entries.isEmpty();
	}

}
