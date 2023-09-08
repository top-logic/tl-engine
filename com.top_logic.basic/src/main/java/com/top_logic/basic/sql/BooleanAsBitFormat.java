/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

/**
 * Format that stores {@link Boolean#TRUE} as 1 and {@link Boolean#FALSE} as 0.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class BooleanAsBitFormat extends Format {

	/** Singleton {@link BooleanAsBitFormat} instance. */
	public static final BooleanAsBitFormat INSTANCE = new BooleanAsBitFormat();

	private BooleanAsBitFormat() {
		// singleton instance
	}

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		if (((Boolean) obj).booleanValue()) {
			toAppendTo.append('1');
		} else {
			toAppendTo.append('0');
		}
		return toAppendTo;
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		int currIndex = pos.getIndex();
		char charAt = source.charAt(currIndex);
		switch (charAt) {
			case '1':
				pos.setIndex(currIndex + 1);
				return Boolean.TRUE;
			case '0':
				pos.setIndex(currIndex + 1);
				return Boolean.FALSE;
			default:
				pos.setErrorIndex(currIndex);
				return null;
		}
	}
}
