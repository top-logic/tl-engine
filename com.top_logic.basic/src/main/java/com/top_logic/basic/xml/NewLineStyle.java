/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.top_logic.basic.UnreachableAssertion;

/**
 * Strategy to encode line breaks in text files.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum NewLineStyle {

	/**
	 * Use the strategy defined by the server environment running the application.
	 */
	SYSTEM,

	/**
	 * Web/Unix style, use a single <code>line feed</code> character.
	 */
	LF,

	/**
	 * Windows style, use a combination of <code>carriage return</code> followed by a
	 * <code>line feed</code> character.
	 */
	CR_LF,

	/**
	 * Mac style, use only a <code>carriage return</code> character.
	 */
	CR;

	/**
	 * A {@link String} containing only a single encoded line break.
	 */
	public String getChars() {
		switch (this) {
			case SYSTEM:
				return systemNL();
			case LF:
				return "\n";
			case CR_LF:
				return "\r\n";
			case CR:
				return "\n\r";
		}
		throw new UnreachableAssertion("No such newline style: " + this);
	}

	private static String systemNL() {
		StringWriter buffer = new StringWriter();
		PrintWriter printer = new PrintWriter(buffer);
		printer.println();
		printer.flush();
		return buffer.toString();
	}

}
