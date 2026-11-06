/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.logger;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.util.ResKey;

/**
 * Logger for messages occurring during an import.
 *
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public interface ImportLogger {	
	
	/** Type of information messages. */
	public static String INFO="INFO";
	/** Type of warning messages. */
	public static String WARN="WARN";
	/** Type of error messages. */
	public static String ERROR="ERROR";
	/** Type of data alert messages. */
	public static String DATA_ALERT="DATA_ALERT";
	/** Type of data messages. */
	public static String DATA_MESSAGE="DATA_MESSAGE";
	
	
	/** Configuration options for an {@link ImportLogger}. */
	public interface Config extends PolymorphicConfiguration<ImportLogger> {
		/** Prefix for the resource keys of the logged messages. */
		String getResourcePrefix();
	}
	
	/**
	 * Logs an error.
	 *
	 * @param caller
	 *        The object reporting the message.
	 * @param key
	 *        The message.
	 * @param params
	 *        The message arguments.
	 */
	void error(Object caller, ResKey key, Object... params);	
	/**
	 * Logs an error caused by an exception.
	 *
	 * @param caller
	 *        The object reporting the message.
	 * @param key
	 *        The message.
	 * @param e
	 *        The problem, may be {@code null}.
	 * @param params
	 *        The message arguments.
	 */
	void error(Object caller, ResKey key, Throwable e, Object... params);	
	/**
	 * Logs a warning.
	 *
	 * @see #error(Object, ResKey, Object...)
	 */
	void warn(Object caller, ResKey key, Object... params);
	/**
	 * Logs an information.
	 *
	 * @see #error(Object, ResKey, Object...)
	 */
	void info(Object caller, ResKey key, Object... params);
}
