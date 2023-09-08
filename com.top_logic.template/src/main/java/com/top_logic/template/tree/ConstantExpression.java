/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.tree;


/**
 * Represents a constant {@link Expression}. (A literal within an {@link Expression}.)
 * <p>
 * If {@link #isBoolean()} is <code>true</code>, use {@link #getBooleanValue()} to get the value.
 * Otherwise, use {@link #getStringValue()}. If the wrong getter is used, <code>null</code> is
 * returned.
 * </p>
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class ConstantExpression extends Expression {

	private final String stringValue;
	private final Boolean booleanValue;

	/**
	 * Creates a new {@link ConstantExpression} representing a {@link Boolean} value.
	 */
	public ConstantExpression(Boolean aValue) {
		this.booleanValue = aValue;
		this.stringValue  = null;
	}

	/**
	 * Decodes the given {@link String} in the encoded form accepted as string literal.
	 * 
	 * <p>
	 * I.e. the substrings "\\", "\n", "\r", "\t", "\b", "\f", "\"", and "\'", are replaced by the
	 * characters '\', '\n', '\r', '\t', '\b', '\f', '"', and ''', resp. Moreover Encoded octal
	 * numbers are replaces by its unicode character.
	 * </p>
	 * 
	 * @param input
	 *        May be <code>null</code>. If input is <code>null</code>, <code>null</code> is
	 *        returned.
	 */
	public static String decode(String input) {
		if (input == null) {
			return null;
		}
		int length = input.length();
		StringBuilder result = new StringBuilder(length);
		int i = 0;
		while (i < length) {
			char currChar = input.charAt(i++);
			if (currChar != '\\') {
				result.append(currChar);
				continue;
			}
			char escapedChar = input.charAt(i++);
			switch (escapedChar) {
				case 'n':
					result.append('\n');
					break;
				case 'r':
					result.append('\r');
					break;
				case 'b':
					result.append('\b');
					break;
				case 'f':
					result.append('\f');
					break;
				case 't':
					result.append('\t');
					break;
				case '\'':
					result.append('\'');
					break;
				case '\\':
					result.append('\\');
					break;
				case '"':
					result.append('"');
					break;
				case '0':
				case '1':
				case '2':
				case '3':
					int octalNumber = 8 * 8 * asOctalDigit(escapedChar, input);
					octalNumber += 8 * asOctalDigit(input.charAt(i++), input);
					octalNumber += asOctalDigit(input.charAt(i++), input);
					result.append((char) octalNumber);
					break;
				default:
					throw new IllegalArgumentException("Unexpected escaped character " + escapedChar + " in String "
						+ input);
			}

		}
		return result.toString();
	}

	private static int asOctalDigit(char currChar, String input) {
		if (currChar > '7' || currChar < '0') {
			throw new IllegalArgumentException("Invalid octal character " + currChar + " in input " + input);
		}
		return Character.digit(currChar, 10);
	}

	/**
	 * Creates a new {@link ConstantExpression} representing a {@link String} value.
	 */
	public ConstantExpression(String aValue) {
		this.booleanValue = null;
		this.stringValue  = aValue;
	}
	
	@Override
	public <R, A> R visit(TemplateVisitor<R, A> v, A arg) {
		return v.visitConstantExpression(this, arg);
	}

	/** 
	 * the string value, might be <code>null</code>.
	 */
	public String getStringValue() {
		return stringValue;
	}
	
	/** 
	 * the boolean value, might be <code>null</code>.
	 */
	public Boolean getBooleanValue() {
		return booleanValue;
	}

	/**
	 * Is this {@link ConstantExpression} a boolean {@link Expression}? If not, it's a string
	 * {@link Expression}.
	 */
	public boolean isBoolean() {
		return this.booleanValue != null;
	}
}