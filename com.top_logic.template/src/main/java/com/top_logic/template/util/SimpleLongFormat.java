/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.util;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import com.top_logic.basic.StringServices;

/**
 * A simple {@link Format} for {@link Long} that uses {@link Long#toString()} and
 * {@link Long#parseLong(String)}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class SimpleLongFormat extends Format {

	@Override
	public StringBuffer format(Object object, StringBuffer toAppendTo, FieldPosition pos) {
		if (object instanceof Long) {
			return toAppendTo.append(object.toString());
		} else {
			throw new IllegalArgumentException("This is not a Long: '"
				+ StringServices.getObjectDescription(object) + "'");
		}
	}

	@Override
	public Object parseObject(String source, ParsePosition position) {
		String relevantText = source.substring(position.getIndex());
		long result = Long.parseLong(relevantText);
		// If this is called by Format.parseObject, returning 0 will be interpreted as "failed"
		// and an exception will be thrown.
		position.setIndex(Math.max(source.length(), 1));
		return result;
	}

}
