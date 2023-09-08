/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.renderers;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link Renderer} that encloses the actual content with a {@link HTMLConstants#PRE} tag.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class PreformattedRenderer<T> implements Renderer<T> {

	@Override
	public void write(DisplayContext context, TagWriter out, T value) throws IOException {
		if (StringServices.isEmpty(value)) {
			return;
		}
		out.beginTag(PRE);
		writeContent(context, out, value);
		out.endTag(PRE);
	}

	/**
	 * Writes the actual content.
	 * 
	 * @param context
	 *        The context in which the rendering occurs.
	 * @param out
	 *        The {@link TagWriter} output to which the output should be generated.
	 * @param value
	 *        The value to render. Not <code>null</code>.
	 */
	protected abstract void writeContent(DisplayContext context, TagWriter out, T value) throws IOException;

}

