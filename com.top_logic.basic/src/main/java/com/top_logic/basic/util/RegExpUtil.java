/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.FileUtilities;

/**
 * Utility methods useful when working with {@link Pattern Regular Expressions}.
 * 
 * @author     <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class RegExpUtil {

	/**
	 * Regex matching a single whitespace.
	 * <p>
	 * Matches not just ASCII whitespace characters, but also unicode whitespace characters. It
	 * matches even those characters, which Java considers to be "spaces, but not whitespaces". For
	 * example the character {@link StringServices#NARROW_NO_BREAK_SPACE narrow no-break space} is a
	 * whitespace character according to Unicode, but only a space for Java. This regex matches it,
	 * too.
	 * </p>
	 * 
	 */
	public static final Pattern UNICODE_WHITESPACE = Pattern.compile("\\p{Blank}", Pattern.UNICODE_CHARACTER_CLASS);

	/**
	 * Replaces all whitespaces by normal spaces, even unicode whitespace.
	 * <p>
	 * See {@link #UNICODE_WHITESPACE} for the details about what is matched. Does not trim the
	 * whitespace: The result is always as long as the input.
	 * </p>
	 */
	public static String normalizeWhitespace(String text) {
		return UNICODE_WHITESPACE.matcher(text).replaceAll(" ");
	}

	/**
	 * Does the {@link CharSequence} contain text matching the {@link Pattern}?
	 */
	public static boolean contains(CharSequence text, Pattern regExp) {
		return regExp.matcher(text).find();
	}

	/**
	 * Replaces the only one text part matching the {@link Pattern} with the replacement.
	 * <p>
	 * If the {@link Pattern} matches more than once, a {@link RuntimeException} is thrown.
	 * "More than once" means: If the first match starts at index X, there is another match starting
	 * at X+1 or later. (Example: "ababa" is matched twice by "aba".)
	 * </p>
	 * 
	 * @param text
	 *        Must not be <code>null</code>.
	 * @param regExp
	 *        Must not be <code>null</code>.
	 * @param replacement
	 *        Must not be <code>null</code>.
	 */
	public static String replaceTheOnlyOne(CharSequence text, Pattern regExp, String replacement) {
		if ((text == null) || (regExp == null) || (replacement == null)) {
			throw new NullPointerException("No parameter of this method is allowed to be null!");
		}
		Matcher matcher = regExp.matcher(text);
		if (!matcher.find()) {
			String message = "Found no match for the given Pattern ('" + regExp
				+ "') in the given String: '" + text + "'";
			throw new RuntimeException(message);
		}
		String result = matcher.replaceFirst(replacement);
		if (hasMultipleMatches(matcher, text.length())) {
			String message = "Found more than one match for the given Pattern ('" + regExp
				+ "') in the given String: '" + text + "'";
			throw new RuntimeException(message);
		}
		return result;
	}

	private static boolean hasMultipleMatches(Matcher matcher, int textSize) {
		matcher.reset();
		if (!matcher.find()) {
			return false;
		}
		if (matcher.start() == textSize) {
			return false;
		}
		return matcher.find(matcher.start() + 1);
	}

	/**
	 * Reads the {@link File} with the given {@link Charset} and builds a {@link Matcher} from the
	 * given {@link Pattern} for it.
	 * <p>
	 * <b>Warning:</b> This is just a convenience method. It still reads the whole file to memory
	 * and builds a string for it!
	 * </p>
	 */
	public static Matcher buildFileContentMatcher(File file, Pattern pattern, Charset encoding) {
		try {
			String fileContent = FileUtilities.readFileToString(file, encoding);
			return pattern.matcher(fileContent);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * Returns the index after the last match of the given {@link Pattern} in the given
	 * {@link String}.
	 * <p>
	 * Example: If the text is "ababa" and the pattern is "aba", the return value is 5. <br/>
	 * For more examples take a look at the test cases:
	 * <code>TestRegExpUtil.testIndexAfterLastMatch</code> <br/>
	 * (Not linked, as links to the <code>test</code> package are forbidden.) <br/>
	 * Returns -1 if the pattern never matches. Both parameters must not be <code>null</code>.
	 * </p>
	 */
	public static int indexAfterLastMatch(String text, Pattern pattern) {
		Matcher matcher = pattern.matcher(text);
		int lastMatchEnd = -1;
		int lastMatchStart = -1;
		while (matcher.find(lastMatchStart + 1)) {
			lastMatchStart = matcher.start();
			lastMatchEnd = matcher.end();
			if (lastMatchStart == text.length()) {
				break;
			}
		}
		return lastMatchEnd;
	}

}
