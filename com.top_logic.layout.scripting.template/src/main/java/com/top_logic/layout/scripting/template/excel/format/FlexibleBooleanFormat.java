/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.excel.format;

import java.text.ParseException;

import com.top_logic.basic.config.AbstractFormattedConfigurationValueProvider;
import com.top_logic.util.Utils;

/**
 * Flexible formatter for boolean values.
 * <p>
 * Parses (case insensitive): ja, nein, wahr, falsch, yes, true, no, false <br/>
 * Formats to: ja, nein <br/>
 * </p>
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class FlexibleBooleanFormat extends AbstractFormattedConfigurationValueProvider<Boolean> {

	/** Singleton instance to this class. */
	public static final FlexibleBooleanFormat INSTANCE = new FlexibleBooleanFormat();

	/** Creates a {@link FlexibleBooleanFormat}. */
	public FlexibleBooleanFormat() {
		super(Boolean.class);
	}

	@Override
	public Boolean parse(String valueString) throws ParseException {
		String theString = valueString.toLowerCase();

		if ("ja".equals(theString) || "wahr".equals(theString)
			|| "yes".equals(theString) || "true".equals(theString)) {
			return Boolean.TRUE;
		}
		else if ("nein".equals(theString) || "falsch".equals(theString)
			|| "no".equals(theString) || "false".equals(theString)) {
			return Boolean.FALSE;
		}
		throw new ParseException("Value '" + valueString + "' cannot be translated into a boolean value.", 0);
	}

	@Override
	public String format(Boolean aConfigValue) {
		return Utils.isTrue(aConfigValue) ? "ja" : "nein";
	}

}
