/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.io.File;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Maybe;

/**
 * Utilities for working with system properties.
 * 
 * @author     <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SystemPropertyUtil {
	
	/**
	 * Checks if the property does still begin with the canonical name of this class.
	 * 
	 * In case someone renames or moves classes containing properties:
	 * Please adapt the properties names.
	 * And of course every thing setting them!
	 * (launch configurations, ant tasks, '.sh' and '.bat' files, ...)
	 */
	public static void checkStartsWithCanonicalClassName(String property, Class<?> propertyClass) {
		assert property.startsWith(propertyClass.getCanonicalName()) :
			"The name of this property does no longer begin with the canonical name of its declaring class! Class name: '"
				+ propertyClass.getCanonicalName() + "'; Property: '" + property + "'";
	}
	
	/**
	 * Tries to parse the system property with the given name.
	 * 
	 * <ul>
	 * <li>Only "true" and "false" (both ignore case) are accepted as valid values.</li>
	 * <li>If it is empty (null or ""), the default value is used and a debug message logged.</li>
	 * <li>If it is not empty but not parsable, the default value is used and an error message
	 * is</li> logged.
	 * <li>If there is any problem (Exception thrown), the default value is used and an error
	 * message is logged.</li>
	 * </ul>
	 * 
	 * <p>
	 * The log messages are logged with the given logCaller as "caller" parameter.
	 * </p>
	 */
	public static boolean getSystemPropertyBoolean(String propertyName, boolean defaultValue, Class<?> logCaller) {
		try {
			String propertyValue = System.getProperty(propertyName);
			if (StringServices.isEmpty(propertyValue)) {
				logPropertyNotSet(propertyName, defaultValue, logCaller);
				return defaultValue;
			}
			Maybe<Boolean> maybeValue = StringServices.parseBooleanStrict(propertyValue);
			if (!maybeValue.hasValue()) {
				logPropertyParsingFailed(propertyName, propertyValue, defaultValue, logCaller);
				return defaultValue;
			}
			return maybeValue.get();
		} catch (Exception exception) {
			logPropertyGettingFailed(propertyName, defaultValue, exception, logCaller);
			return defaultValue;
		}
	}
	
	/**
	 * Tries to parse the system property with the given name. <br/>
	 * It is not checked whether the file exists. It's just given to <code>new File(...)</code>.
	 * If that or anything else fails (Exception thrown), the default value is used and an error message is logged.
	 * If it is empty (null or ""), the default value is used and a debug message logged.
	 * The log messages are logged with the given logCaller as "caller" parameter.
	 * 
	 * @see #getSystemPropertyFile(String, Class)
	 */
	public static File getSystemPropertyFile(String propertyName, File defaultValue, Class<?> logCaller) {
		try {
			String propertyValue = System.getProperty(propertyName);
			if (StringServices.isEmpty(propertyValue)) {
				logPropertyNotSet(propertyName, defaultValue, logCaller);
				return defaultValue;
			}
			return new File(propertyValue);
		} catch (Exception exception) {
			logPropertyGettingFailed(propertyName, defaultValue, exception, logCaller);
			return defaultValue;
		}
	}
	
	/**
	 * Tries to parse the system property with the given name. <br/>
	 * It is not checked whether the file exists. It's just given to <code>new File(...)</code>.
	 * If that or anything else fails (Exception thrown), an error message is logged.
	 * If it is empty (null or ""), a debug message logged.
	 * The log messages are logged with the given logCaller as "caller" parameter.
	 * 
	 * @see #getSystemPropertyFile(String, File, Class)
	 */
	public static Maybe<File> getSystemPropertyFile(String propertyName, Class<?> logCaller) {
		try {
			String propertyValue = System.getProperty(propertyName);
			if (StringServices.isEmpty(propertyValue)) {
				logPropertyNotSet(propertyName, logCaller);
				return Maybe.none();
			}
			return Maybe.some(new File(propertyValue));
		} catch (Exception exception) {
			logPropertyGettingFailed(propertyName, exception, logCaller);
			return Maybe.none();
		}
	}
	
	private static void logPropertyNotSet(String propertyName, Object defaultValue, Class<?> logCaller) {
		if (Logger.isDebugEnabled(logCaller)) {
			String message = "System property '" + propertyName + "' not set. Using default: " + defaultValue;
			Logger.debug(message, logCaller);
		}
	}
	
	private static void logPropertyNotSet(String propertyName, Class<?> logCaller) {
		if (Logger.isDebugEnabled(logCaller)) {
			String message = "System property '" + propertyName + "' not set. No default available.";
			Logger.debug(message, logCaller);
		}
	}
	
	private static void logPropertyParsingFailed(String propertyName, String propertyValue, boolean defaultValue,
			Class<?> logCaller) {
		String message = "Unparsable system property '" + propertyName + "'. Using default: " + defaultValue
			+ "; Unparsable value: '" + propertyValue + "'";
		Logger.error(message, logCaller);
	}
	
	private static void logPropertyGettingFailed(String propertyName, Object defaultValue, Throwable exception,
			Class<?> logCaller) {
		String message = "Failed to get system property '" + propertyName + "'. Using default: " + defaultValue
			+ "; Error message: " + exception.getMessage();
		Logger.error(message, exception, logCaller);
	}
	
	private static void logPropertyGettingFailed(String propertyName, Throwable exception,
			Class<?> logCaller) {
		String message = "Failed to get system property '" + propertyName
			+ "'.  No default available. Error message: " + exception.getMessage();
		Logger.error(message, exception, logCaller);
	}
	
}
