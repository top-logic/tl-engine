/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.mig.html.HTMLConstants;

/**
 * A {@link Renderer} that writes a link to Google.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
class DemoLinkToGoogleRenderer implements Renderer<Object> {

	/** The {@link DemoLinkToGoogleRenderer} instance. */
	public static final DemoLinkToGoogleRenderer INSTANCE = new DemoLinkToGoogleRenderer();

	private static final String LINK_TARGET = "http://www.google.de";

	@Override
	public void write(DisplayContext context, TagWriter out, Object value) {
		out.beginBeginTag(HTMLConstants.ANCHOR);
		out.writeAttribute(HTMLConstants.HREF_ATTR, LINK_TARGET);
		out.endBeginTag();
		out.writeText("Link");
		out.endTag(HTMLConstants.ANCHOR);
	}

}
