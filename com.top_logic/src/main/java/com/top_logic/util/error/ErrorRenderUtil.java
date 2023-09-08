/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.error;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.util.regex.Pattern;

import com.top_logic.basic.xml.TagWriter;

/**
 * Utility class for error message rendering.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ErrorRenderUtil {

	/**
	 * Pattern that describes a line break.
	 * 
	 * <p>
	 * Note: For compatibility, unencoded, or HTML encoded line breaks are accepted.
	 * </p>
	 */
	public static final Pattern NEWLINE_PATTERN =
		Pattern.compile(("\r" + "?" + "\n" + "|" + "<br" + "\\s" + "*" + "/" + "?" + ">"));

	/**
	 * Writes the lines in <code>localizedMessage</code> using &lt;br /&gt; as separator
	 */
	public static void writeLines(TagWriter out, String localizedMessage) {
		boolean first = true;
		if (localizedMessage == null) {
			return;
		}
		for (String line : NEWLINE_PATTERN.split(localizedMessage)) {
			if (first) {
				first = false;
			} else {
				out.emptyTag(BR);
			}
			out.writeText(line);
		}
	}

}
