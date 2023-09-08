/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.link;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;

/**
 * Renderer for a button-like object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface LinkRenderer extends Renderer<Link> {

	@Override
	default void write(DisplayContext context, TagWriter out, Link value) throws IOException {
		writeLink(context, out, value);
	}

	/**
	 * Renders the visual representation of the given button-like object.
	 * 
	 * @param context
	 *        The rendering context.
	 * @param out
	 *        The writer to write to.
	 * @param button
	 *        The button-like object.
	 * @throws IOException
	 *         If writing to the output fails.
	 */
	void writeLink(DisplayContext context, TagWriter out, Link button) throws IOException;

}
