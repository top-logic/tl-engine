/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;

/**
 * Base class for {@link ContentHandler} implementations that dispatches content generation of URL
 * sub paths to other content handlers.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractDispatchingContentHandler implements ContentHandler {

	/**
	 * If the given URL is not empty then the call is dispatched to the the {@link ContentHandler}
	 * registered under the next {@link URLParser#removeResource() resource}.
	 * 
	 * @see ContentHandler#handleContent(DisplayContext, String, URLParser)
	 */
	@Override
	public void handleContent(DisplayContext context, String id, URLParser url) throws IOException {
		String resource = resourceToHandle(url);

		if (resource == null) {
			internalHandleContent(context, url);
			return;
		}

		final ContentHandler registeredHandler = getContentHandler(resource);
		if (registeredHandler == null) {
			handleNotFound(context, resource);
		} else {
			registeredHandler.handleContent(context, resource, url);
		}
	}

	/**
	 * Returns the requested resource name and updates the given {@link URLParser}
	 * 
	 * @param url
	 *        the parser to take information from
	 * @return a resource under which a {@link ContentHandler} was registered or <code>null</code>
	 *         if {@link #internalHandleContent(DisplayContext, URLParser)} should handle the
	 *         request.
	 */
	protected String resourceToHandle(URLParser url) {
		if (url.isEmpty()) {
			return null;
		}
		return url.removeResource();
	}

	/**
	 * This method locally handles a request that for is not dispatched to another handler.
	 * 
	 * @param context
	 *        the {@link DisplayContext} hold information about request and response
	 * @param url
	 *        the Parser which points to this {@link ContentHandler}
	 * 
	 * @throws IOException
	 *         if some error occurs
	 */
	protected abstract void internalHandleContent(DisplayContext context, URLParser url) throws IOException;

	/**
	 * Returns the {@link ContentHandler} registered under the given id, <code>null</code> if
	 * nothing was registered under the given id.
	 * 
	 * @param id
	 *        The resource name produced by {@link #resourceToHandle(URLParser)}.
	 */
	protected abstract ContentHandler getContentHandler(String id);

	/**
	 * Produce content for a sub-location for which no handler was registered.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}
	 * @param resource
	 *        The requested {@link #resourceToHandle(URLParser) resource} for which no handler was
	 *        {@link #getContentHandler(String) found}.
	 */
	protected void handleNotFound(DisplayContext context, String resource) throws IOException {
		ErrorPage.showPage(context, "dynamicContentNotFoundErrorPage");
	}

}
