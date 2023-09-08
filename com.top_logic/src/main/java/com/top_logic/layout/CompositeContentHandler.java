/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.util.Collection;

import com.top_logic.basic.annotation.FrameworkInternal;

/**
 * The {@link CompositeContentHandler} is a special {@link ContentHandler} which
 * may have &quot;sub&quot; {@link ContentHandler} to which the request may be
 * dispatched.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface CompositeContentHandler extends ContentHandler {

	/**
	 * Registers a {@link ContentHandler} as &quot;child&quot; of this
	 * {@link CompositeContentHandler} under the given id.
	 * 
	 * @param id
	 *        the resource for which the handler shall be registered. May be
	 *        <code>null</code> if no well-known ID is required. In that case, 
	 *        a unique ID is generated.
	 * @param handler
	 *        the provider to register. Must not be <code>null</code>. Moreover
	 *        no other {@link ContentHandler} with the same well-known ID must be
	 *        registered.
	 */
	void registerContentHandler(String id, ContentHandler handler);

	/**
	 * Deregisters the given {@link ContentHandler} from this
	 * {@link CompositeContentHandler}.
	 * 
	 * @param handler
	 *        the provider to deregister.
	 * @return <code>true</code> iff deregistering of the given provider works.
	 */
	boolean deregisterContentHandler(ContentHandler handler);

	/**
	 * Creates a {@link URLBuilder} which currently points to this {@link ContentHandler}, i.e. if
	 * the URL of the returned {@link URLBuilder} is invoked, then this {@link ContentHandler} will
	 * be called.
	 * 
	 * @param context
	 *        {@link DisplayContext} in which rendering occurs
	 */
	URLBuilder getURL(DisplayContext context);

	/**
	 * Returns an {@link URLBuilder url} to identify the given
	 * {@link ContentHandler} at the GUI, i.e. if the URL of the given
	 * {@link URLBuilder} is invoked then
	 * {@link ContentHandler#handleContent(DisplayContext, String, URLParser)} of the
	 * given {@link ContentHandler} is called.
	 * 
	 * <p>
	 * Must only be called during rendering.
	 * </p>
	 * 
	 * @param context
	 *        {@link DisplayContext} in which rendering occurs
	 * @param handler
	 *        a formerly registered {@link ContentHandler} for which the URL is
	 *        requested
	 * 
	 * @return the {@link URLBuilder} which currently contains a URL for the
	 *         given {@link ContentHandler}
	 */
	URLBuilder getURL(DisplayContext context, ContentHandler handler);

	/**
	 * All registered sub-handlers.
	 */
	@FrameworkInternal
	Collection<? extends ContentHandler> inspectSubHandlers();

}
