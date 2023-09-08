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

/**
 * {@link Renderer} for displaying {@link Throwable}s inline.
 * 
 * @see ThrowableColumnRenderer
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ThrowableRenderer implements Renderer<Throwable> {

	/**
	 * Singleton {@link ThrowableRenderer} instance.
	 */
	public static final ThrowableRenderer INSTANCE = new ThrowableRenderer();

	private ThrowableRenderer() {
		// Singleton constructor.
	}

	@Override
	public void write(DisplayContext context, TagWriter out, Throwable ex) throws IOException {
		out.beginTag(PRE);
		append(context, out, ex);
		out.endTag(PRE);
	}

	private void append(DisplayContext context, TagWriter out, Throwable ex) throws IOException {
		String message = ex.getMessage();
		out.writeText(ex.getClass().getName());
		if (message != null) {
			out.writeText(": ");
			out.writeText(message);
		}
		out.nl();

		StackTraceRenderer.INSTANCE.write(context, out, ex.getStackTrace());

		if (ex.getCause() != null) {
			append(context, out, ex.getCause());
		}
	}

}