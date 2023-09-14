/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.shared.string;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Utilities for working with {@link String}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class StringServicesShared {

	/** the default charset used to construct Strings and byte[] */
	public static final String UTF8 = "utf-8";

	/** Constant for the UTF-8 {@link Charset}. */
	public static final Charset CHARSET_UTF_8 = Charset.forName("UTF-8");

	/**
	 * Check if a String is empty, i.e. null or <code>""</code>.
	 *
	 * @return true, if the String is null or <code>""</code>.
	 */
	public static final boolean isEmpty(CharSequence aString) {
		return ((aString == null) || aString.length() == 0);
	}

	/**
	 * Checks, whether two strings are both empty or one equal to the other.
	 *
	 * @return <code>true</code>, if both strings are empty, or one is equal to the other.
	 */
	public static boolean equalsEmpty(String s1, String s2) {
		if (isEmpty(s1)) {
			return isEmpty(s2);
		} else {
			return s1.equals(s2);
		}
	}

	/**
	 * Converts the given string into a non-<code>null</code> string by replacing <code>null</code>
	 * with <code>""</code>.
	 * 
	 * @param input
	 *        The source string.
	 * @return Either the given string, or <code>""</code>, if if the given string was
	 *         <code>null</code>.
	 */
	public static String nonNull(String input) {
		return input == null ? "" : input;
	}

	/** Null safe {@link String#strip()}. */
	public static String stripNullsafe(String nullable) {
		if (nullable == null) {
			return null;
		}
		return nullable.strip();
	}

	/**
	 * Creates a debug representation of the object.
	 * <p>
	 * Use this method for writing objects to log messages.
	 * </p>
	 * 
	 * @param value
	 *        Is allowed to be null.
	 * @return Is never null.
	 */
	public static String debug(Object value) {
		try {
			return "'" + debugValue(value) + "' (Class: " + getClassNameNullsafe(value) + ")";
		} catch (RuntimeException ex) {
			return "toString() failed: " + ex.getMessage();
		}
	}

	/**
	 * Null-safe variant of {@link Object#getClass()}.
	 * 
	 * @param object
	 *        Is allowed to be null.
	 * @return Never null.
	 */
	public static String getClassNameNullsafe(Object object) {
		if (object == null) {
			return "null";
		}
		return getNameNullsafe(object.getClass());
	}

	/**
	 * Returns the {@link Class#getName() name} of the class.
	 */
	public static String getNameNullsafe(Class<?> classObject) {
		if (classObject == null) {
			return "null";
		}
		return classObject.getName();
	}

	/**
	 * Creates a {@link String} representation of the given value for debugging purpose.
	 * 
	 * @param value
	 *        Is allowed to be null.
	 * @return A debugging string representation of the given value (excluding it's class).
	 * 
	 * @see #debug(Object)
	 */
	public static String debugValue(Object value) {
		if (value == null) {
			return "null";
		}
		if (value instanceof Object[]) {
			return Arrays.toString((Object[]) value);
		}
		// arrays of primitive types are not instances of Object[]
		if (value instanceof boolean[]) {
			return Arrays.toString((boolean[]) value);
		}
		if (value instanceof byte[]) {
			return Arrays.toString((byte[]) value);
		}
		if (value instanceof short[]) {
			return Arrays.toString((short[]) value);
		}
		if (value instanceof char[]) {
			return Arrays.toString((char[]) value);
		}
		if (value instanceof int[]) {
			return Arrays.toString((int[]) value);
		}
		if (value instanceof long[]) {
			return Arrays.toString((long[]) value);
		}
		if (value instanceof float[]) {
			return Arrays.toString((float[]) value);
		}
		if (value instanceof double[]) {
			return Arrays.toString((double[]) value);
		}
		return value.toString();
	}

}
