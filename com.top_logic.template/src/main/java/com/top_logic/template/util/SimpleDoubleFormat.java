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
 * A simple {@link Format} for {@link Double} that uses {@link Double#toString()} and
 * {@link Double#parseDouble(String)}.
 * <p>
 * It additionally accepts the infinity symbol on parsing for positive or negative infinity.
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class SimpleDoubleFormat extends Format {

	@Override
	public StringBuffer format(Object object, StringBuffer toAppendTo, FieldPosition pos) {
		if (object instanceof Double) {
			return toAppendTo.append(object.toString());
		} else {
			throw new IllegalArgumentException("This is not a Double: '"
				+ StringServices.getObjectDescription(object) + "'");
		}
	}

	@Override
	public Object parseObject(String source, ParsePosition position) {
		String relevantText = source.substring(position.getIndex());
		// \u221e is the infinity symbol. It cannot be represented in the Cp1252 encoding used for
		// java files in <i>TopLogic</i>.
		if (relevantText.equals("\u221e")) {
			position.setIndex(1);
			return Double.POSITIVE_INFINITY;
		}
		if (relevantText.equals("-\u221e")) {
			position.setIndex(2);
			return Double.NEGATIVE_INFINITY;
		}
		double result = Double.parseDouble(relevantText);
		// If this is called by Format.parseObject, returning 0 will be interpreted as "failed"
		// and an exception will be thrown.
		position.setIndex(Math.max(source.length(), 1));
		return result;
	}

}
