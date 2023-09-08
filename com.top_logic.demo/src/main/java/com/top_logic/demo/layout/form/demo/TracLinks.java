/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo;

import static com.top_logic.mig.html.HTMLConstants.*;

import com.top_logic.basic.xml.TagWriter;

/**
 * Utilities to create links to the development wiki from demo pages.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TracLinks {

	/**
	 * Writes a link to the given wiki name.
	 */
	public static void writeWikiLink(TagWriter out, String wikiName) {
		out.beginBeginTag(ANCHOR);
		out.writeAttribute(TARGET_ATTR, "trac");
		out.writeAttribute(HREF_ATTR, "http://tl/trac/wiki/" + wikiName);
		out.endBeginTag();
		out.writeText("wiki:" + wikiName);
		out.endTag(ANCHOR);
	}

}
