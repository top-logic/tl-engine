/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

/**
 * Shared instance that dispatches to the actual thread-local variant of
 * {@link HTMLFormatter#formatObject(Object)}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HTMLFormatterFormat extends Format {
	
	/**
	 * Singleton {@link HTMLFormatterFormat} instance.
	 */
	public static final HTMLFormatterFormat INSTANCE = new HTMLFormatterFormat();
	
	private HTMLFormatterFormat() {
		// Singleton constructor.
	}

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		return toAppendTo.append(HTMLFormatter.formatObject(obj));
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		throw new UnsupportedOperationException();
	}
}