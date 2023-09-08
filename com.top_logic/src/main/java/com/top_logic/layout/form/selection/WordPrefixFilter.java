/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import com.top_logic.basic.col.Filter;

/**
 * {@link Filter} that checks that a value contains a given substring at a word beginning position.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
final class WordPrefixFilter implements Filter {

	private final String prefix;

	public WordPrefixFilter(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public boolean accept(Object anObject) {
		String value = (String) anObject;

		int testIndex = 0;
		while (true) {
			int matchIndex = value.indexOf(prefix, testIndex);
			if (matchIndex < 0) return false;

			if ((matchIndex == 0) || (! Character.isLetter(value.charAt(matchIndex- 1)))) {
				// The match happened either at the start of the tested
				// value, or immediately after a non-letter character.
				return true;
			}

			testIndex = matchIndex + 1;
		}
	}
}
