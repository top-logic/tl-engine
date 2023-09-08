/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.json;

import java.io.IOError;
import java.io.IOException;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Objects;

import com.top_logic.basic.json.JSON.DefaultValueAnalyzer;
import com.top_logic.basic.json.JSON.ParseException;

/**
 * {@link Format} which formats a {@link String} value that is in JSON format by outputting the
 * string nicely.
 * 
 * @see JSON#write(Appendable, com.top_logic.basic.json.JSON.ValueAnalyzer, Object, boolean)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PrettyPrintingJSONFormat extends Format {

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		Objects.requireNonNull(toAppendTo);
		Objects.requireNonNull(pos);
		String stringInJSONFormat = obj.toString();
		Object parsedJSON;
		try {
			parsedJSON = JSON.fromString(stringInJSONFormat);
		} catch (ParseException ex) {
			throw new IllegalArgumentException("Object does not contain valid JSON: " + stringInJSONFormat, ex);
		}
		try {
			JSON.write(toAppendTo, DefaultValueAnalyzer.INSTANCE, parsedJSON, true);
		} catch (IOException ex) {
			throw new IOError(ex);
		} catch (RuntimeException ex) {
			throw new IllegalArgumentException(ex);
		}
		return toAppendTo;
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		int idx = pos.getIndex();
		pos.setIndex(source.length());
		return source.substring(idx);
	}

}

