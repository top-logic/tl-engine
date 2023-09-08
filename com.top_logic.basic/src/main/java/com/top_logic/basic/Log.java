/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.util.I18NBundle;
import com.top_logic.basic.util.ResKey;

/**
 * Light-weight logging interface for service classes.
 * 
 * <p>
 * {@link Log} supports both, reporting detailed information about non-fatal errors and feedback
 * about the occurrence of (potentially multiple) non-fatal errors to the caller.
 * </p>
 * 
 * <p>
 * Using the {@link Log} interface is especially useful for tooling implementations that inform the
 * the programmer of multiple errors e.g. in a configuration file. Stopping with a (fatal) exception
 * at the first encountered problem results in inconvenient usage characteristics, because the tool
 * has to be restarted after fixing each single problem. At the other hand, it is absolutely
 * necessary that the tool implementation terminates abnormally, if any error has occurred, to
 * ensure that a tool chain can interrupt.
 * </p>
 * 
 * @see Protocol
 * @see I18NLog
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Log extends com.top_logic.basic.core.log.Log {

	/**
	 * The declared exception that was passed to {@link #error(String, Throwable)}.
	 */
	Throwable getFirstProblem();

	/**
	 * Whether some fatal or non-fatal errors have already occurred.
	 */
	boolean hasErrors();

	/**
	 * Upgrades this {@link Log} to an {@link I18NLog}.
	 * 
	 * @param bundle
	 *        The {@link I18NBundle} used to write messages to this log.
	 */
	default I18NLog asI18NLog(I18NBundle bundle) {
		return new I18NLog() {
			@Override
			public void log(Level level, ResKey message, Throwable ex) {
				switch (level) {
					case ERROR:
					case FATAL:
						Log.this.error(bundle.getString(message), ex);
						break;

					case WARN:
						Log.this.info(bundle.getString(message), Log.WARN);
						break;

					case INFO:
						Log.this.info(bundle.getString(message), Log.INFO);
						break;

					case DEBUG:
						Log.this.info(bundle.getString(message), Log.DEBUG);
						break;
				}
			}
		};
	}

}
