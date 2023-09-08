/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.doclet;

import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Iterator finding the Camel case words within a given text.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class CamelCaseIterator implements Iterator<String> {

	/** Pattern finding camel case words to log a warning. */
	public static final Pattern CAMEL_CASE_WORD =
		Pattern.compile("\\b[\\w]*[a-z][A-Z][\\w]*\\b");

	private final Matcher _matcher;

	private final String _input;

	private String _next;

	/**
	 * Creates an iterator finding the camel case words within the given input text.
	 */
	public static Iterator<String> newIterator(String input) {
		Matcher matcher = CAMEL_CASE_WORD.matcher(input);
		String firstResult = findNext(input, matcher);
		if (firstResult == null) {
			return Collections.emptyIterator();
		}
		return new CamelCaseIterator(matcher, input, firstResult);
	}

	private CamelCaseIterator(Matcher matcher, String input, String firstMatch) {
		_matcher = matcher;
		_input = input;
		_next = firstMatch;
	}

	@Override
	public boolean hasNext() {
		return _next != null;
	}

	@Override
	public String next() {
		String result = _next;
		_next = findNext(_input, _matcher);
		return result;
	}

	private static String findNext(String input, Matcher matcher) {
		if (!matcher.find()) {
			return null;
		}
		int start = matcher.start() - 1;
		boolean startQuote = false;
		while (start >= 0) {
			char c = input.charAt(start);
			if (c == ' ' || c == '\t') {
				break;
			}
			if (c == '"') {
				startQuote = true;
				break;
			}
			start--;
		}
		int inputLength = input.length();
		int end = matcher.end();
		boolean endQuote = false;
		while (end < inputLength) {
			char c = input.charAt(end);
			if (c == ' ' || c == '\t') {
				break;
			}
			if (c == '"') {
				endQuote = true;
				break;
			}
			end++;
		}
		if (startQuote && endQuote) {
			return findNext(input, matcher);
		} else {
			return matcher.group(0);
		}
	}
}
