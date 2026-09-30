/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.infoservice;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.Logger;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.HTMLUtil;

/**
 * Converter of {@link InfoService} items, to be able to transmit them to client.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class InfoServiceXMLStringConverter {

	/**
	 * javascript invocation of client-side part of {@link InfoService}, including
	 *         serialized info items, which shall be displayed.
	 */
	public static String getJSInvocation(DisplayContext context, List<HTMLFragment> infoItems) {
		StringBuilder builder = new StringBuilder();
		boolean isNotFirst = false;
		builder.append("showInfoArea(\"");
		for (HTMLFragment htmlFragment : infoItems) {
			if (isNotFirst) {
				builder.append("\", \"");
			} else {
				isNotFirst = true;
			}
			try {
				builder.append(HTMLUtil.encodeJS(renderItemBox(context, htmlFragment)));
			} catch (IOException ex) {
				// Ignore
			}
		}
		builder.append("\");");
		return builder.toString();
	}

	/**
	 * Renders a single info area item to the HTML that the client-side part of the
	 * {@link InfoService} displays.
	 * 
	 * <p>
	 * The result is one argument of the client-side <code>showInfoArea</code> function, which
	 * inserts it into the info area of the top-level window. A response that reports a problem
	 * outside of an {@link InfoService} update transmits such an item box as its body, so that the
	 * receiving script can display it in the very same way.
	 * </p>
	 * 
	 * @param context
	 *        The context of the current rendering.
	 * @param htmlFragment
	 *        The item to render, e.g. created by {@link InfoService#errorItem(HTMLFragment)}.
	 * 
	 * @return The HTML source of the given item.
	 * 
	 * @throws IOException
	 *         If writing the item fails.
	 */
	public static String renderItemBox(DisplayContext context, HTMLFragment htmlFragment) throws IOException {
		StringWriter buffer = new StringWriter();
		TagWriter out = new TagWriter(buffer);
		try {
			htmlFragment.write(context, out);
		} catch (Throwable throwable) {
			Logger.error("Could not create info message!", throwable, InfoServiceXMLStringConverter.class);
			out.endAll();
		}
		return buffer.toString();
	}

}
