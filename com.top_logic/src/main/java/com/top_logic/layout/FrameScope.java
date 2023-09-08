/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.model.listen.ModelScope;

/**
 * Rendering scope that references a browser frame (client-side document).
 * 
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public interface FrameScope extends IdentifierSource, CompositeContentHandler, CommandListenerRegistry, WindowScopeProvider {

	/**
	 * This method adds a {@link ClientAction} to be executed during the next
	 * revalidation step.
	 */
	public void addClientAction(ClientAction update);

	/**
	 * A representation of the browser window that contains this
	 * {@link FrameScope}.
	 */
	@Override
	public WindowScope getWindowScope();
	
	/**
	 * Returns the {@link FrameScope} this frame scope is rendered in, or
	 * <code>null</code> if this {@link FrameScope} is the top level
	 * {@link FrameScope} of the {@link WindowScope} this scope belongs to, i.e.
	 * the returned scope is <code>null</code> iff
	 * {@link WindowScope#getTopLevelFrameScope()} of the window returned in
	 * {@link #getWindowScope()} is this {@link FrameScope}
	 */
	public FrameScope getEnclosingScope();

	/**
	 * The {@link ModelScope} of this session to listen for updates to the application model.
	 */
	ModelScope getModelScope();

	/**
	 * This method adds a client side reference to navigate from the window
	 * object of the client side document which is represented by the
	 * {@link #getEnclosingScope() parent frame scope} to the window object of
	 * the client side document represented by this {@link FrameScope}.
	 * 
	 * @param out
	 *        the {@link Appendable} to append the reference. never
	 *        <code>null</code>.
	 * 
	 * @return a reference to the given {@link Appendable}
	 * 
	 * @throws IOException
	 *         iff the given {@link Appendable} throws some
	 */
	public <T extends Appendable> T appendClientReference(T out) throws IOException;
}
