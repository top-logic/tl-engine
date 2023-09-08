/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.fuzzy;

import java.text.ParseException;
import java.util.Date;

/**
 * Utility class to parse "fuzzy".
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FuzzyUtil {

	/**
	 * Parses the given value depending on the expected type.
	 * 
	 * @param expectedClass
	 *        Must not be <code>null</code>.
	 * @param formattedValue
	 *        Is allowed to be <code>null</code>.
	 * @return <code>null</code>, if the given value is <code>null</code> or represents it.
	 */
	public static <T> T parseFuzzy(Class<T> expectedClass, String formattedValue) throws ParseException {
		if (formattedValue == null) {
			return null;
		}
		Object expectedFuzzy = parseIntern(expectedClass, formattedValue);
		return expectedClass.cast(expectedFuzzy);
	}

	private static Object parseIntern(Class<?> expectedClass, String formattedValue) throws ParseException {
		if (expectedClass == Boolean.class) {
			return FlexibleFormatKind.BOOLEAN.parseValue(formattedValue);
		}
		if (expectedClass == Date.class) {
			return FlexibleFormatKind.DATE.parseValue(formattedValue);
		}
		if (expectedClass == String.class) {
			return formattedValue;
		}
		try {
			if (expectedClass == Long.class) {
				return Long.valueOf(formattedValue);
			}
			if (expectedClass == Integer.class) {
				return Integer.valueOf(formattedValue);
			}
			if (expectedClass == Double.class) {
				return Double.valueOf(formattedValue);
			}
			if (expectedClass == Float.class) {
				return Float.valueOf(formattedValue);
			}
			if (expectedClass == Short.class) {
				return Short.valueOf(formattedValue);
			}
			if (expectedClass == Byte.class) {
				return Byte.valueOf(formattedValue);
			}
		} catch (NumberFormatException ex) {
			throw (ParseException) new ParseException("Unable to parse '" + formattedValue + "' as "
				+ expectedClass.getName() + ".", 0).initCause(ex);
		}
		throw new IllegalArgumentException("Unable to fuzzy-parse value to type " + expectedClass + ".");
	}

}
