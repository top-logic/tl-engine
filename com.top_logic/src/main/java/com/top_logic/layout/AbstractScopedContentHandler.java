/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.mig.html.layout.LayoutComponentScope;

/**
 * Base class for {@link CompositeContentHandler}s that are scoped within
 * another {@link CompositeContentHandler}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractScopedContentHandler extends AbstractCompositeContentHandler {
	
	/**
	 * @see #getUrlContext()
	 */
	private CompositeContentHandler urlContext;

	/**
	 * Creates a {@link AbstractScopedContentHandler}.
	 * 
	 * @param urlContext
	 *        See {@link #getUrlContext()}
	 */
	public AbstractScopedContentHandler(CompositeContentHandler urlContext) {
		this.urlContext = urlContext;
	}
	
	/**
	 * The parent {@link CompositeContentHandler} in whose URL namespace this
	 * {@link ContentHandler} lives.
	 */
	public final CompositeContentHandler getUrlContext() {
		return urlContext;
	}

	/**
	 * Sets the enclosing frame scope and registers (resp. deregisters) itself
	 * if the given scope is not <code>null</code> (resp. <code>null</code>).
	 * 
	 * @param newUrlContext
	 *        may be <code>null</code>.
	 * 
	 * @throws IllegalStateException
	 *         iff there is already a non <code>null</code> enclosing scope and
	 *         someone tries to set a different non <code>null</code> enclosing
	 *         scope.
	 * 
	 * @see LayoutComponentScope#getUrlContext()
	 * @see LayoutComponentScope#getEnclosingScope()
	 */
	public void setUrlContext(CompositeContentHandler newUrlContext, String pathName) {
		dropUrlContext();
		
		if (newUrlContext != null) {
			newUrlContext.registerContentHandler(pathName, this);

			// Note: The former URL context must be kept, if the context is reset to null, because
			// access to the WindowScope of an invalidated component is otherwise not possible, see
			// Ticket #7447.
			this.urlContext = newUrlContext;
		}
	}

	/**
	 * Sets the URL context using the internal {@link #getPathName()}.
	 * 
	 * @see #setUrlContext(CompositeContentHandler, String)
	 */
	public final void setUrlContext(CompositeContentHandler newUrlContext) {
		setUrlContext(newUrlContext, getPathName());
	}

	/**
	 * Unregisters this {@link ContentHandler} from its {@link #getUrlContext()}.
	 */
	public void dropUrlContext() {
		CompositeContentHandler formerUrlContext = getUrlContext();
		
		if (formerUrlContext != null) {
			formerUrlContext.deregisterContentHandler(this);
		}
	}

	/**
	 * The local name under which this {@link AbstractScopedContentHandler} is
	 * visible within its {@link #getUrlContext()}.
	 */
	protected abstract String getPathName();

	@Override
	public URLBuilder getURL(DisplayContext context) {
		return urlContext.getURL(context, this);
	}

}