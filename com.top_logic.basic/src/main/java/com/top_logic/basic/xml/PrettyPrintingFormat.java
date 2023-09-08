/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Objects;

/**
 * {@link Format} formatting well-formed XML.
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PrettyPrintingFormat extends Format {

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		Objects.requireNonNull(toAppendTo);
		Objects.requireNonNull(pos);

		String stringInXMLFormat = obj.toString();
		String prettyPrint;
		try {
			prettyPrint = XMLPrettyPrinter.prettyPrint(stringInXMLFormat);
		} catch (RuntimeException ex) {
			throw new RuntimeException("Unable to pretty print given str", ex);
		}
		toAppendTo.append(prettyPrint);
		return toAppendTo;
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		int idx = pos.getIndex();
		pos.setIndex(source.length());
		return source.substring(idx);
	}

}

