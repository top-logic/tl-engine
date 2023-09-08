/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.i18n.log;

import com.top_logic.basic.Log;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.util.ResKey;

/**
 * {@link Log} based on an {@link I18NLog} creating uninternationalized messages.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class I18NLogAsLog implements Log {

	private I18NLog _log;

	private boolean _hasErrors;

	private Throwable _firstProblem;

	/**
	 * Creates a {@link I18NLogAsLog}.
	 */
	public I18NLogAsLog(I18NLog log) {
		_log = log;
	}

	@Override
	public void error(String message, Throwable ex) {
		log(Level.ERROR, message, ex);
		_hasErrors = true;
		if (_firstProblem == null) {
			_firstProblem = ex;
		}
	}

	@Override
	public Throwable getFirstProblem() {
		return _firstProblem;
	}

	@Override
	public boolean hasErrors() {
		return _hasErrors;
	}

	@Override
	public void info(String message, int verbosityLevel) {
		log(fromVerbosityLevel(verbosityLevel), message, null);
	}

	private void log(Level level, String message, Throwable ex) {
		_log.log(level, wrap(message), ex);
	}

	private static ResKey wrap(String message) {
		return ResKey.text(message);
	}

	private static Level fromVerbosityLevel(int verbosityLevel) {
		switch (verbosityLevel) {
			case Log.DEBUG:
				return Level.DEBUG;
			case Log.VERBOSE:
				return Level.DEBUG;
			case Log.INFO:
				return Level.INFO;
			case Log.WARN:
				return Level.WARN;
		}
		return Level.INFO;
	}

}
