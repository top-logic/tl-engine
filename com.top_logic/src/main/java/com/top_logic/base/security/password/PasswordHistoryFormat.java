/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.password;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;

/**
 * Format used to store the password history.
 * 
 * <p>
 * Note: This format is thread-safe.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PasswordHistoryFormat extends Format {

	private static final char PASSWORD_HASH_SEPARATOR = '!';

	private static final char PASSWORD_HASH_ESCAPE = ':';

	/** Singleton {@link PasswordHistoryFormat} instance. */
	public static final PasswordHistoryFormat INSTANCE = new PasswordHistoryFormat();

	/**
	 * Creates a new {@link PasswordHistoryFormat}.
	 */
	protected PasswordHistoryFormat() {
		// singleton instance
	}

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		int startLength = toAppendTo.length();
		if (!(obj instanceof List)) {
			throw new IllegalArgumentException(obj + " is not a list of Strings.");
		}
		List<?> pwdHistory = (List<?>) obj;
		boolean first = true;
		for (Object pwd : pwdHistory) {
			if (first) {
				first = false;
			} else {
				toAppendTo.append(PASSWORD_HASH_SEPARATOR);
			}
			if (!(pwd instanceof String)) {
				toAppendTo.setLength(startLength);
				throw new IllegalArgumentException(obj + " is not a list of Strings: " + pwd);
			}
			addPwdEscaped(toAppendTo, (CharSequence) pwd);
		}
		return toAppendTo;
	}

	private void addPwdEscaped(StringBuffer out, CharSequence pwd) {
		for (int index = 0, size = pwd.length(); index < size; index++) {
			char curr = pwd.charAt(index);
			if (curr == PASSWORD_HASH_SEPARATOR || curr == PASSWORD_HASH_ESCAPE) {
				out.append(PASSWORD_HASH_ESCAPE);
			}
			out.append(curr);
		}

	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		List<String> result = new ArrayList<>();
		int len = source.length();
		if (len <= pos.getIndex()) {
			pos.setIndex(len + 1);
			return result;
		}
		StringBuilder tmp = new StringBuilder();
		for (int i = pos.getIndex(); i < len; i++) {
			char currChar = source.charAt(i);
			switch (currChar) {
				case PASSWORD_HASH_ESCAPE:
					i++;
					if (i < len) {
						char nextChar = source.charAt(i);
						switch (nextChar) {
							case PASSWORD_HASH_ESCAPE:
							case PASSWORD_HASH_SEPARATOR:
								tmp.append(nextChar);
								break;
							default:
								// char must not be escaped.
								pos.setErrorIndex(i);
								return null;
						}
					} else {
						// Unescaped password hash escape....
						pos.setErrorIndex(i);
						return null;
					}
					break;
				case PASSWORD_HASH_SEPARATOR:
					result.add(tmp.toString());
					tmp.setLength(0);
					break;
				default:
					tmp.append(currChar);
			}
		}
		pos.setIndex(len + 1);
		result.add(tmp.toString());
		return result;
	}


}
