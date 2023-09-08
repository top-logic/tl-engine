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
 * A {@link Format} for {@link Boolean} that accepts 'true' and 'false' (case sensitive) and the
 * empty string (means <code>null</code>) and serializes to those values.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class SimpleBooleanFormat extends Format {

	@Override
	public StringBuffer format(Object object, StringBuffer toAppendTo, FieldPosition pos) {
		if (object instanceof Boolean) {
			return toAppendTo.append(object.toString());
		} else {
			throw new IllegalArgumentException("This is not a Boolean: '"
				+ StringServices.getObjectDescription(object) + "'");
		}
	}

	@Override
	public Object parseObject(String source, ParsePosition position) {
		String relevantText = source.substring(position.getIndex());
		if (relevantText.equals("true")) {
			// If this is called by Format.parseObject, returning 0 will be interpreted as "failed"
			// and an exception will be thrown.
			position.setIndex(Math.max(source.length(), 1));
			return Boolean.TRUE;
		} else if (relevantText.equals("false")) {
			// See above
			position.setIndex(Math.max(source.length(), 1));
			return Boolean.FALSE;
		} else {
			throw new IllegalArgumentException("Could not parse '" + relevantText + "'.");
		}
	}

}