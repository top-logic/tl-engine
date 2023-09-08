/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.renderers;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link Renderer} for {@link StackTraceElement} arrays.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StackTraceRenderer implements Renderer<StackTraceElement[]> {

	/**
	 * Singleton {@link StackTraceRenderer} instance.
	 */
	public static final StackTraceRenderer INSTANCE = new StackTraceRenderer();

	private StackTraceRenderer() {
		// Singleton constructor.
	}

	@Override
	public void write(DisplayContext context, TagWriter out, StackTraceElement[] stackTrace) throws IOException {
		out.beginTag(PRE);
		for (StackTraceElement line : stackTrace) {
			out.writeText("   ");
			String className = line.getClassName();
			int dotIndex = className.lastIndexOf('.');
			if (dotIndex > 0) {
				out.writeText(className.subSequence(0, dotIndex + 1));
			}
			out.beginTag(HTMLConstants.BOLD);
			out.writeText(className.substring(dotIndex + 1));
			out.writeText('.');
			out.writeText(line.getMethodName());
			out.endTag(HTMLConstants.BOLD);
			String fileName = line.getFileName();
			if (fileName != null) {
				out.writeText('(');
				out.writeText(fileName);
				int lineNumber = line.getLineNumber();
				if (lineNumber >= 0) {
					out.writeText(':');
					out.writeInt(lineNumber);
				}
				out.writeText(')');
			}
			out.nl();
		}
		out.endTag(PRE);
	}

}
