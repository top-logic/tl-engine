/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;
import java.util.Collection;

import jakarta.servlet.ServletException;

import com.top_logic.basic.shared.collection.CollectionUtilShared;

/**
 * Adapter for a {@link CompositeContentHandler} that allows changing the identifier an
 * {@link AbstractScopedContentHandler} used in its call to
 * {@link #registerContentHandler(String, ContentHandler)}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class CompositeHandlerAdapter implements CompositeContentHandler {

	private final CompositeContentHandler _urlContext;

	private ContentHandler _impl;

	/**
	 * Creates a {@link CompositeHandlerAdapter} for the given URL context handler.
	 * 
	 * @param urlContext
	 *        The {@link CompositeContentHandler} to build an adapter for.
	 */
	public CompositeHandlerAdapter(CompositeContentHandler urlContext) {
		_urlContext = urlContext;
	}

	/**
	 * The {@link CompositeContentHandler} this one is an adapter for regarding the
	 * {@link #registerContentHandler(String, ContentHandler)} aspect.
	 */
	public CompositeContentHandler getUrlContext() {
		return _urlContext;
	}

	@Override
	public void handleContent(DisplayContext context, String id, URLParser url) throws IOException, ServletException {
		assert _impl != null : "Delegate content handler has not yet been registered.";
		_impl.handleContent(context, id, url);
	}

	/**
	 * Accept the given {@link ContentHandler} as (singleton) implementation handler.
	 * 
	 * @param id
	 *        The registration ID is ignored and replaced the identifier under which this
	 *        {@link CompositeHandlerAdapter} is registered within its {@link #getUrlContext()}.
	 * @param handler
	 *        The implementation handler.
	 * 
	 * @see CompositeContentHandler#registerContentHandler(String, ContentHandler)
	 */
	@Override
	public void registerContentHandler(String id, ContentHandler handler) {
		assert _impl == null;
		_impl = handler;
	}

	@Override
	public boolean deregisterContentHandler(ContentHandler handler) {
		boolean result = handler == _impl;
		if (result) {
			_impl = null;

			handleDeregister(handler);
		}
		return result;
	}

	@Override
	public Collection<? extends ContentHandler> inspectSubHandlers() {
		return CollectionUtilShared.singletonOrEmptySet(_impl);
	}

	/**
	 * Called after a registered implementation handler has requested to unregister.
	 * 
	 * @param handler
	 *        The formerly registered implmentation handler.
	 */
	protected void handleDeregister(ContentHandler handler) {
		// Hook for subclasses.
	}

	@Override
	public URLBuilder getURL(DisplayContext context, ContentHandler handler) {
		assert handler == _impl;

		return getURL(context);
	}

	@Override
	public URLBuilder getURL(DisplayContext context) {
		return _urlContext.getURL(context, this);
	}

}
