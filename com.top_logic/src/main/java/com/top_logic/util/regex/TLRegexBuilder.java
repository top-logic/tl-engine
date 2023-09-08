/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.regex;

import java.util.regex.Pattern;

/**
 * Creating regular expressions in a readable way.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TLRegexBuilder {

	private static final String JAVASCRIPT_SCOPE_SEPARATOR = "/";

	private StringBuilder _prefix = new StringBuilder();

	private StringBuilder _source = new StringBuilder();

	private StringBuilder _suffix = new StringBuilder();

	private TLRegexBuilder add(StringBuilder builder, String regex) {
		builder.append(regex);

		return this;
	}

	/**
	 * Adds the given regular expression without escaping.
	 */
	public TLRegexBuilder add(String regex) {
		return add(_source, regex);
	}

	/**
	 * Regular expression for the beginning of a line.
	 */
	public TLRegexBuilder startOfLine() {
		return add(_source, "^");
	}

	/**
	 * Regular expression for a java identifier.
	 * 
	 * Note: In Java code the special character classes <code>\p{javaJavaIdentifierStart}</code> and
	 * <code>\p{javaJavaIdentifierPart}</code> are used to match an identifier. In JavaScript, this
	 * expression is not valid.
	 */
	public TLRegexBuilder javaIdentifier() {
		return add(_source, "(?:[a-zA-Z_$][a-zA-Z\\d_$]*)?");
	}

	/**
	 * Concatenate regular expression.
	 */
	public TLRegexBuilder then(String value) {
		return add(_source, "(?:" + escape(value) + ")");
	}

	/**
	 * Optional regular expression.
	 */
	public TLRegexBuilder maybe(String value) {
		return then(value).add(_source, "?");
	}

	/**
	 * Optional regular expression.
	 */
	public TLRegexBuilder maybe() {
		return add(_source, "?");
	}

	/**
	 * Regular expression for any character sequence.
	 * 
	 * Note: Empty is also allowed.
	 */
	public TLRegexBuilder anything() {
		return add(_source, "(?:.*)");
	}

	/**
	 * Regular expression for any character sequence of length at least 1.
	 */
	public TLRegexBuilder something() {
		return add(_source, "(?:.+)");
	}

	/**
	 * Regular expression character class. Any character of the given characters.
	 */
	public TLRegexBuilder anyOf(String value) {
		return add(_source, "[" + escape(value) + "]");
	}

	/**
	 * Character sequence with none not of the given characters.
	 * 
	 * Note: Empty is also allowed.
	 */
	public TLRegexBuilder anythingBut(String value) {
		return add(_source, "(?:[^" + escape(value) + "]*)");
	}

	/**
	 * Character sequence with none not of the given characters with length at least 1.
	 */
	public TLRegexBuilder somethingBut(String value) {
		return add(_source, "(?:[^" + escape(value) + "]+)");
	}

	/**
	 * Regular expression word with arbitrary length.
	 */
	public TLRegexBuilder emptyWord() {
		return add(_source, "(?:\\w*)");
	}

	/**
	 * Regular expression word with length at least 1.
	 */
	public TLRegexBuilder word() {
		return add(_source, "(?:\\w+)");
	}

	/**
	 * Regular expression non word with length at least 1.
	 */
	public TLRegexBuilder nonWord() {
		return add(_source, "(?:\\W+)");
	}

	/**
	 * Regular expression for any digit.
	 */
	public TLRegexBuilder digit() {
		return add(_source, "(?:\\d)");
	}

	/**
	 * Regular expression for any space character.
	 */
	public TLRegexBuilder space() {
		return add(_source, "(?:\\s)");
	}

	/**
	 * Regular expression for the end of a line.
	 */
	public TLRegexBuilder endOfLine() {
		return add(_suffix, "$");
	}

	/**
	 * Regular expression for arbitrary occurrences of the preceding expression.
	 */
	public TLRegexBuilder zeroOrMore() {
		return add(_source, "*");
	}

	/**
	 * Regular expression for one or more occurrences of the preceding expression.
	 */
	public TLRegexBuilder oneOrMore() {
		return add(_source, "+");
	}

	/**
	 * Regular expression to choose between the preceding and the given expression.
	 */
	public TLRegexBuilder or(String value) {
		return add(_prefix, "(?:").add(_source, ")|(?:").then(value).add(_suffix, ")");
	}

	/**
	 * Regular expression to choose between multiple expressions.
	 */
	public TLRegexBuilder or() {
		return add(_source, "|");
	}

	/**
	 * Regular expression to begin grouping an expression. No capturing.
	 */
	public TLRegexBuilder group() {
		return add(_source, "(?:");
	}

	/**
	 * Regular expression to end grouping an expression. No capturing.
	 */
	public TLRegexBuilder endGroup() {
		return add(_source, ")");
	}

	/**
	 * Regular expression to begin grouping an expression.
	 */
	public TLRegexBuilder capture() {
		return add(_source, "(");
	}

	/**
	 * Regular expression to end grouping an expression.
	 */
	public TLRegexBuilder endCapture() {
		return add(_source, ")");
	}

	private String escape(String value) {
		return value.replaceAll("[\\W]", "\\\\$0");
	}

	@Override
	public String toString() {
		return new StringBuilder(_prefix).append(_source).append(_suffix).toString();
	}

	/**
	 * Encloses the regular expression into a javascript specific regex scope separator.
	 */
	public String toJavascriptString() {
		return JAVASCRIPT_SCOPE_SEPARATOR + toString() + JAVASCRIPT_SCOPE_SEPARATOR;
	}

	/**
	 * Creates a {@link Pattern} for the build regular expression.
	 */
	public Pattern build() {
		return Pattern.compile(toString());
	}

}
