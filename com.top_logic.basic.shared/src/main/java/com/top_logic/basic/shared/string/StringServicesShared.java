/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.shared.string;

import java.io.IOException;
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

	/**
	 * First index of <code>target</code> within <code>source</code>.
	 * 
	 * <p>
	 * This is the generalization of {@link String#indexOf(String)} to {@link CharSequence}.
	 * </p>
	 * 
	 * @param source
	 *        The characters to search in.
	 * @param target
	 *        The characters to search for.
	 * 
	 * @return The position relative to <code>sourceOffset</code> of the first match, or
	 *         <code>-1</code> if not found.
	 * 
	 * @see String#indexOf(String)
	 */
	public static int indexOf(CharSequence source, String target) {
		if (source instanceof String) {
			return ((String) source).indexOf(target);
		} else {
			return indexOf(source, target, 0);
		}
	}

	/**
	 * First index of <code>target</code> within <code>source</code>.
	 * 
	 * <p>
	 * This is the generalization of {@link String#indexOf(String)} to {@link CharSequence}.
	 * </p>
	 * 
	 * @param source
	 *        The characters to search in.
	 * @param target
	 *        The characters to search for.
	 * 
	 * @return The position relative to <code>sourceOffset</code> of the first match, or
	 *         <code>-1</code> if not found.
	 * 
	 * @see String#indexOf(String)
	 */
	public static int indexOf(char[] source, char[] target) {
		return indexOf(source, target, 0);
	}

	/**
	 * Index of <code>target</code> within <code>source</code> when starting the search at
	 * <code>fromIndex</code>.
	 * 
	 * <p>
	 * This is the generalization of {@link String#indexOf(String, int)} to {@link CharSequence}.
	 * </p>
	 * 
	 * @param source
	 *        The characters to search in.
	 * @param target
	 *        The characters to search for.
	 * @param fromIndex
	 *        The first index in the source to consider for the match.
	 * 
	 * @return The position relative to <code>sourceOffset</code> of the first match, or
	 *         <code>-1</code> if not found.
	 * 
	 * @see String#indexOf(String, int)
	 */
	public static int indexOf(CharSequence source, String target, int fromIndex) {
		if (source instanceof String) {
			return ((String) source).indexOf(target, fromIndex);
		} else {
			return indexOf(source, 0, source.length(), target, 0, target.length(), fromIndex);
		}
	}

	/**
	 * Index of <code>target</code> within <code>source</code> when starting the search at
	 * <code>fromIndex</code>.
	 * 
	 * <p>
	 * This is the generalization of {@link String#indexOf(String, int)} to {@link CharSequence}.
	 * </p>
	 * 
	 * @param source
	 *        The characters to search in.
	 * @param target
	 *        The characters to search for.
	 * @param fromIndex
	 *        The first index in the source to consider for the match.
	 * 
	 * @return The position relative to <code>sourceOffset</code> of the first match, or
	 *         <code>-1</code> if not found.
	 * 
	 * @see String#indexOf(String, int)
	 */
	public static int indexOf(char[] source, char[] target, int fromIndex) {
		return indexOf(source, 0, source.length, target, 0, target.length, fromIndex);
	}

	/**
	 * Searches within the range <code>[sourceOffset, sourceOffset + sourceCount)</code> of
	 * <code>source</code> for an occurrence of the character range
	 * <code>[targetOffset, targetOffset + targetCount)</code> of <code>target</code> starting at
	 * the position <code>fromIndex</code> (relative to <code>sourceOffset</code>).
	 * 
	 * @param source
	 *        The characters to search in.
	 * @param sourceOffset
	 *        The index of the first character to consider for a potential match.
	 * @param sourceCount
	 *        The number of characters starting at <code>sourceOffset</code> to consider for a
	 *        potential match.
	 * @param target
	 *        The characters to search for.
	 * @param targetOffset
	 *        The index of the first character to search for.
	 * @param targetCount
	 *        The number of characters in <code>target</code> to try to match agains
	 *        <code>source</code>.
	 * @param fromIndex
	 *        The first index in the source characters (the part of the source sequence described by
	 *        the source offset and source length) to consider for the match.
	 * 
	 * @return The position relative to <code>sourceOffset</code> of the first match, or
	 *         <code>-1</code> if not found.
	 */
	public static int indexOf(CharSequence source, int sourceOffset, int sourceCount,
			CharSequence target, int targetOffset, int targetCount, int fromIndex) {
		if (fromIndex >= sourceCount) {
			return (targetCount == 0 ? sourceCount : -1);
		}
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		if (targetCount == 0) {
			return fromIndex;
		}

		char firstChar = target.charAt(targetOffset);
		int sourceStartMax = sourceOffset + (sourceCount - targetCount);

		for (int sourceStartPos = sourceOffset + fromIndex; sourceStartPos <= sourceStartMax; sourceStartPos++) {
			// Look for first character.
			if (source.charAt(sourceStartPos) != firstChar) {
				while (++sourceStartPos <= sourceStartMax && source.charAt(sourceStartPos) != firstChar) {
					// Just increment position.
				}
			}

			// Found first character, now look at the rest of target.
			if (sourceStartPos <= sourceStartMax) {
				int sourcePos = sourceStartPos + 1;
				int sourceStop = sourcePos + targetCount - 1;
				for (int targetPos = targetOffset + 1; sourcePos < sourceStop
					&& source.charAt(sourcePos) == target.charAt(targetPos); sourcePos++, targetPos++) {
					// Just increment position.
				}

				if (sourcePos == sourceStop) {
					// Found whole string.
					return sourceStartPos - sourceOffset;
				}
			}
		}
		return -1;
	}

	/**
	 * Searches within the range <code>[sourceOffset, sourceOffset + sourceCount)</code> of
	 * <code>source</code> for an occurrence of the character range
	 * <code>[targetOffset, targetOffset + targetCount)</code> of <code>target</code> starting at
	 * the position <code>fromIndex</code> (relative to <code>sourceOffset</code>).
	 * 
	 * @param source
	 *        The characters to search in.
	 * @param sourceOffset
	 *        The index of the first character to consider for a potential match.
	 * @param sourceCount
	 *        The number of characters starting at <code>sourceOffset</code> to consider for a
	 *        potential match.
	 * @param target
	 *        The characters to search for.
	 * @param targetOffset
	 *        The index of the first character to search for.
	 * @param targetCount
	 *        The number of characters in <code>target</code> to try to match agains
	 *        <code>source</code>.
	 * @param fromIndex
	 *        The first index in the source to consider for the match.
	 * 
	 * @return The position relative to <code>sourceOffset</code> of the first match, or
	 *         <code>-1</code> if not found.
	 */
	public static int indexOf(char[] source, int sourceOffset, int sourceCount,
			char[] target, int targetOffset, int targetCount, int fromIndex) {
		if (fromIndex >= sourceCount) {
			return (targetCount == 0 ? sourceCount : -1);
		}
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		if (targetCount == 0) {
			return fromIndex;
		}

		char firstChar = target[targetOffset];
		int sourceStartMax = sourceOffset + (sourceCount - targetCount);

		for (int sourceStartPos = sourceOffset + fromIndex; sourceStartPos <= sourceStartMax; sourceStartPos++) {
			// Look for first character.
			if (source[sourceStartPos] != firstChar) {
				while (++sourceStartPos <= sourceStartMax && source[sourceStartPos] != firstChar) {
					// Just increment position.
				}
			}

			// Found first character, now look at the rest of target.
			if (sourceStartPos <= sourceStartMax) {
				int sourcePos = sourceStartPos + 1;
				int sourceStop = sourcePos + targetCount - 1;
				for (int targetPos = targetOffset + 1; sourcePos < sourceStop
					&& source[sourcePos] == target[targetPos]; sourcePos++, targetPos++) {
					// Just increment position.
				}

				if (sourcePos == sourceStop) {
					// Found whole string.
					return sourceStartPos - sourceOffset;
				}
			}
		}
		return -1;
	}

	/**
	 * Allocation-free append the given decimal value to the given writer.
	 * 
	 * @param out
	 *        The writer to write to.
	 * @param value
	 *        The number to write.
	 * @throws IOException
	 *         If output fails.
	 */
	public static void append(Appendable out, int value) throws IOException {
		if (value < 0) {
			if (value == Integer.MIN_VALUE) {
				out.append("-2147483648");
				return;
			}

			out.append('-');
			value = -value;
		}

		appendPositiveInt(out, value);
	}

	private final static char[] DIGIT_TENS = {
		'0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
		'1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
		'2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
		'3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
		'4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
		'5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
		'6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
		'7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
		'8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
		'9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
	};

	private final static char[] DIGIT_ONES = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	};

	private static final int[] INT_DIVISORS = { 100000000, 1000000, 10000, 100, 1 };

	private static final int INT_DIVISORS_LENGTH = INT_DIVISORS.length;

	private static void appendPositiveInt(Appendable out, int value) throws IOException {
		int n = 0;
		while (n < INT_DIVISORS_LENGTH) {
			int divisor = INT_DIVISORS[n++];
			int twoDigits = value / divisor;

			if (twoDigits != 0) {
				value -= divisor * twoDigits;

				char tensDigit = DIGIT_TENS[twoDigits];
				if (tensDigit != '0') {
					out.append(tensDigit);
				}

				out.append(DIGIT_ONES[twoDigits]);
				if (n == INT_DIVISORS_LENGTH) {
					return;
				}

				break;
			}
		}

		if (n < INT_DIVISORS_LENGTH) {
			appendIntTail(out, value, n);
		} else {
			out.append('0');
		}
	}

	private static void appendIntTail(Appendable out, int value, int n) throws IOException {
		do {
			int divisor = INT_DIVISORS[n++];
			int twoDigits = value / divisor;
			value -= divisor * twoDigits;

			out.append(DIGIT_TENS[twoDigits]);
			out.append(DIGIT_ONES[twoDigits]);
		} while (n < INT_DIVISORS_LENGTH);
	}

	private static final long[] LONG_DIVISORS = {
		1000000000000000000L,
		10000000000000000L,
		100000000000000L,
		1000000000000L,
		10000000000L,
		100000000L
	};

	private static final int LONG_DIVISORS_LENGTH = LONG_DIVISORS.length;

	/**
	 * Allocation-free append the given decimal value to the given writer.
	 * 
	 * @param out
	 *        The writer to write to.
	 * @param value
	 *        The number to write.
	 * @throws IOException
	 *         If output fails.
	 */
	public static void append(Appendable out, long value) throws IOException {
		if (value < 0) {
			if (value == Long.MIN_VALUE) {
				out.append("-9223372036854775808");
				return;
			}

			out.append('-');
			value = -value;
		}

		if (value <= Integer.MAX_VALUE) {
			appendPositiveInt(out, (int) value);
			return;
		}

		int n = 0;
		while (n < LONG_DIVISORS_LENGTH) {
			long divisor = LONG_DIVISORS[n++];
			int twoDigits = (int) (value / divisor);

			if (twoDigits != 0) {
				value -= divisor * twoDigits;

				char tensDigit = DIGIT_TENS[twoDigits];
				if (tensDigit != '0') {
					out.append(tensDigit);
				}

				out.append(DIGIT_ONES[twoDigits]);
				if (n == LONG_DIVISORS_LENGTH) {
					appendIntTail(out, (int) value, 1);
					return;
				}

				break;
			}
		}

		if (n < LONG_DIVISORS_LENGTH) {
			do {
				long divisor = LONG_DIVISORS[n++];
				int twoDigits = (int) (value / divisor);
				value -= divisor * twoDigits;

				out.append(DIGIT_TENS[twoDigits]);
				out.append(DIGIT_ONES[twoDigits]);
			} while (n < LONG_DIVISORS_LENGTH);

			appendIntTail(out, (int) value, 1);
		} else {
			// Still potentially null.
			appendPositiveInt(out, (int) value);
		}
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
