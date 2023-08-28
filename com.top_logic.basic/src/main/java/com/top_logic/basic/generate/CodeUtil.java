/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.generate;

import com.top_logic.basic.StringServices;

/**
 * Utilities for code generation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CodeUtil {

	/**
	 * Converts a '-', '.', or '_' separated name to camel case.
	 */
	public static String toCamelCase(String name) {
		StringBuffer result = new StringBuffer();
		for (String part : simpleNameParts(name)) {
			result.append(CodeUtil.toUpperCaseStart(part));
		}
		
		return result.toString();
	}

	/**
	 * Converts an all-upper-case name separated by '-', '.', or '_' to camel case.
	 */
	public static String toCamelCaseFromAllUpperCase(String name) {
		StringBuffer result = new StringBuffer();
		for (String part : simpleNameParts(name)) {
			result.append(CodeUtil.toUpperCaseStart(part.toLowerCase()));
		}

		return result.toString();
	}

	/**
	 * Splits the given name into its parts separated by separator characters.
	 */
	public static String[] simpleNameParts(String name) {
		return name.split("[-\\._/]");
	}

	/**
	 * Converts the first character to upper case.
	 */
	public static String toUpperCaseStart(String name) {
		if (name.length() == 0) {
			return "";
		}
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

	/**
	 * Converts the first character to lower case.
	 */
	public static String toLowerCaseStart(String name) {
		int firstLower = 0;
		int length = name.length();
		while (firstLower < length && Character.isUpperCase(name.charAt(firstLower))) {
			firstLower++;
		}
		if (firstLower < length) {
			// A non-upper-case character was found.
			if (firstLower == 0) {
				// No upper case at all.
				return name;
			}
			if (firstLower == 1) {
				// Only a single uppper case letter was found.
				return Character.toLowerCase(name.charAt(0)) + name.substring(1);
			}
			// Convert all but the last upper case character to lower case.
			int lastUpper = firstLower - 1;
			return name.substring(0, lastUpper).toLowerCase() + name.substring(lastUpper);
		} else {
			// All upper case.
			return name.toLowerCase();
		}
	}

	/**
	 * Converts the given string to a Java string literal.
	 */
	public static String toStringLiteral(String name) {
		return '"' + name.replaceAll("\\" + '"', "\\\\\\" + '"').replaceAll("\n", "\\\\n").replaceAll("\t", "\\\\t") + '"';
	}

	/**
	 * Converts the given camel case name into an all upper case name with '_'
	 * as part separator.
	 */
	public static String toAllUpperCase(String name) {
		return StringServices.join(nameParts(name), '_').toUpperCase();
	}
	
	/**
	 * Converts the given camel case name into an all upper case name with '_' as part separator.
	 */
	public static String toXMLTag(String name) {
		return StringServices.join(nameParts(name), '-').toLowerCase();
	}

	/**
	 * Converts the given singular name to its plural form.
	 */
	public static String toPluralName(String name) {
		if (name.length() >= "child".length() && "child".equalsIgnoreCase(name.substring(name.length() - "child".length(), name.length()))) {
			return name + "ren";
		}
		return name + "s";
	}

	/**
	 * Derives a suitable English label from an English technical name in camel-case or
	 * all-upper-case notation.
	 */
	public static String englishLabel(String technicalName) {
		boolean allUpperCase = isAllUpperCase(technicalName);

		StringBuilder buffer = new StringBuilder();
		boolean first = true;
		for (String part : CodeUtil.nameParts(technicalName)) {
			boolean toUpperCase = first;
			if (first) {
				first = false;
			} else {
				buffer.append(' ');
			}

			if (!allUpperCase && isAllUpperCase(part) && part.length() > 1) {
				// Looks like an abbreviation.
				buffer.append(part);
			} else {
				if (toUpperCase) {
					buffer.append(CodeUtil.toUpperCaseStart(part.toLowerCase()));
				} else {
					buffer.append(part.toLowerCase());
				}
			}
		}
		return buffer.toString();
	}

	private static boolean isAllUpperCase(String s) {
		for (int n = 0, cnt = s.length(); n < cnt; n++) {
			char ch = s.charAt(n);
			if (ch != Character.toUpperCase(ch)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * The parts of a name split at locations where multiple words are joined by either a separator
	 * or a lower-upper-case boundary.
	 *
	 * @param name
	 *        The technical name.
	 * @return The parts of the name.
	 */
	public static String[] nameParts(String name) {
		return name.split("(?<=[a-z])(?=[A-Z])|[-\\._/]+|(?<=[A-Z0-9])(?=[A-Z][a-z])");
	}

	/**
	 * Heuristic to create a singular name variant for the given name of a list property.
	 */
	public static String singularName(String pluralName) {
		return singularName(pluralName, pluralName);
	}

	/**
	 * Heuristic to create a singular name variant for the given name of a list property.
	 * 
	 * @param defaultName
	 *        The default name to use, if no heuristic applies.
	 */
	public static String singularName(String pluralName, String defaultName) {
		final int length = pluralName.length();
		if (pluralName.endsWith("s")) {
			if (pluralName.endsWith("ies")) {
				//Replace "ies" by "y", e.g. "...Properties" by "...Property"
				return pluralName.substring(0, length - 3) + "y";
			} else {
				return pluralName.substring(0, length - 1);
			}
		} else {
			if (pluralName.endsWith(/* C or c */"hildren")) {
				// Replace "Children" by "Child" and "children" by "child"
				return pluralName.substring(0, length - 3);
			} else {
				return defaultName;
			}
		}
	}
	
}
