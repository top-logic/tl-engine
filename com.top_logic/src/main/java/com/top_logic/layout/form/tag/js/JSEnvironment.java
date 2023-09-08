/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag.js;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;

/**
 * Representation of a JavaScript evaluation environment.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class JSEnvironment {

	/**
	 * Converts the given string into a JavaScript string literal.
	 * 
	 * @param s
	 *        The string to quote.
	 * @return The JavaScript quoted string.
	 * 
	 * @deprecated Use {@link TagWriter#writeJsString(CharSequence)}.
	 */
	@Deprecated
	public static String toStringLiteral(String s) {
		if (s == null) {
			return "null";
		}
		s = StringServices.replace(s, "\\", "\\\\");
		s = StringServices.replace(s, "\"", "\\\"");
		s = StringServices.replace(s, "\n", "\\n");
		return '"' + s + '"';
	}

}
