/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;
import java.net.URLEncoder;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.provider.DefaultLabelProvider;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Renders an arbitrary object as text (provided by a {@link LabelProvider})
 * decorated with a search link that looks up the object's label qin the Google search
 * engine.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GoogleSearchRenderer implements Renderer<Object>, HTMLConstants {
	
	/**
	 * Default instance using a {@link DefaultLabelProvider}.
	 */
	public static final Renderer<Object> INSTANCE =
		new GoogleSearchRenderer(DefaultLabelProvider.INSTANCE);
	
	private final LabelProvider labelProvider;
	
	public GoogleSearchRenderer(LabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	@Override
	public void write(DisplayContext context, TagWriter out, Object value)
			throws IOException {
		
		String text = labelProvider.getLabel(value);
		
		out.beginBeginTag(ANCHOR);
		out.writeAttribute(HREF_ATTR, "http://www.google.com/search?q=" + URLEncoder.encode(text, "UTF-8"));
		out.writeAttribute(TARGET_ATTR, BLANK_VALUE);
		out.endBeginTag();
		out.writeText(text);
		out.endTag(ANCHOR);
	}

}
