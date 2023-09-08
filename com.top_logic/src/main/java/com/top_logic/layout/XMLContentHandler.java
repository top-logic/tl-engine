/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * The class {@link XMLContentHandler} is a {@link ContentHandler} which writes
 * a xml response.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class XMLContentHandler implements ContentHandler {

	/**
	 * Dispatches to
	 * {@link #handleXMLResponse(DisplayContext, TagWriter, URLParser)}
	 */
	@Override
	public final void handleContent(DisplayContext context, String id, URLParser url) throws IOException {
		setContentType(context);
		TagWriter out = MainLayout.getTagWriter(context.asRequest(), context.asResponse());
		handleXMLResponse(context, out, url);
		out.flushBuffer();
	}

	/**
	 * Sets the content type of the response. Is called before anything happens.
	 * 
	 * @param context
	 *        the context which the actual content handling happens.
	 * 
	 * @see XMLContentHandler#handleXMLResponse(DisplayContext, TagWriter, URLParser)
	 */
	protected void setContentType(DisplayContext context) {
		context.asResponse().setContentType("text/xml;charset=utf-8");
	}

	/**
	 * Actually handles the request and writes an xml response to the given
	 * {@link TagWriter}.
	 * 
	 * @param context
	 *        the context to get {@link DisplayContext#asRequest() request from}
	 * @param out
	 *        the {@link TagWriter} to write output to
	 * @param url
	 *        the {@link URLParser} currently pointing to this
	 *        {@link CompositeContentHandler}
	 * @throws IOException
	 *         iff <code>out</code> throws some
	 */
	protected abstract void handleXMLResponse(DisplayContext context, TagWriter out, URLParser url) throws IOException;

}
