/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.Logger;
import com.top_logic.mig.html.HTMLConstants;

/**
 * The class {@link DispatchingContentHandler} dispatches its
 * {@link #handleContent(DisplayContext, String, URLParser)} to the
 * {@link ServletContext#getRequestDispatcher(String) request dispatcher} of the
 * given {@link DisplayContext}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DispatchingContentHandler implements ContentHandler {

	private String path;

	/**
	 * Creates a {@link DispatchingContentHandler}.
	 */
	public DispatchingContentHandler() {
	}

	/**
	 * Sets the path to dispatch to.
	 * 
	 * @return a reference to this content handler
	 */
	public DispatchingContentHandler setPath(String path) {
		this.path = path;
		return this;
	}

	@Override
	public void handleContent(DisplayContext context, String id, URLParser url) throws IOException {
		final HttpServletRequest request = context.asRequest();
		final HttpServletResponse response = context.asResponse();
		response.setContentType(HTMLConstants.CONTENT_TYPE_TEXT_HTML_UTF_8);
		try {
			context.asServletContext().getRequestDispatcher(path).include(request, response);
		} catch (ServletException ex) {
			Logger.error("Failure on '" + path + "'", ex.getRootCause(), DispatchingContentHandler.class);
			throw (IOException) new IOException().initCause(ex);
		}
	}

}
