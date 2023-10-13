/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.top_logic.base.context.TLInteractionContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.basic.Command;
import com.top_logic.mig.html.Media;
import com.top_logic.mig.html.UserAgent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Collection of application-relevant properties during page rendering and
 * command execution.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DisplayContext extends TLInteractionContext, WindowScopeProvider {

	/**
	 * The application's context path.
	 */
	public String getContextPath();

	/**
	 * The encoding of the response.
	 */
	public String getCharacterEncoding();
	
	/**
	 * The {@link Resources} for the user of the current request.
	 */
	public Resources getResources();
	
	/**
	 * Any attribute that is associated with the current request.
	 */
	public Object getAttribute(String name);

	/**
	 * An abstraction of the requesting user agent.
	 */
	public UserAgent getUserAgent();
	
	/**
	 * Provide a view of this context as {@link HttpServletRequest} object.
	 * 
	 * <p style="color: red;">
	 * <b>Note:</b> This method is only for backwards compatibility with APIs
	 * that request a direct reference to the request object. Avoid using this
	 * method whenever possible.
	 * </p>
	 * 
	 * <p>
	 * This method should be deprecated as soon as possible.
	 * </p>
	 */
	public HttpServletRequest asRequest();

	public ServletContext asServletContext();
	
	public HttpServletResponse asResponse();

	/**
	 * The scope which is currently being rendered or revalidated.
	 * 
	 * <p>
	 * This scope must be used as scope of new {@link Control}s created during rendering or
	 * revalidation.
	 * </p>
	 * 
	 * TODO BHU: Split interface {@link ControlScope} to remove the possibility of the scope being
	 * <code>null</code>.
	 * 
	 * @return the current scope. <code>null</code>, if the current context is not able to deal with
	 *         controls.
	 */
	public ControlScope getExecutionScope();
	
	/**
	 * Installs the global {@link ControlScope scope} of this
	 * {@link DisplayContext}. This method must only called once, i.e. only if
	 * no control scope is already given.
	 * 
	 * @param initialScope
	 *        the first {@link ControlScope}.
	 */
	public void initScope(ControlScope initialScope);
	
	/**
	 * Perform the rendering of the given value using the given renderer in the
	 * given scope.
	 * 
	 * <p>
	 * The {@link #getExecutionScope() current scope} is switched to the given
	 * scope for only the specified rendering operation. The
	 * {@link DisplayContext} implementation is responsible for switching back
	 * to the original scope after this call returns.
	 * </p>
	 * 
	 * @param scope
	 *        must not be <code>null</code>
	 * @see Renderer#write(DisplayContext, TagWriter, Object)
	 */
	public <T> void renderScoped(ControlScope scope, Renderer<T> renderer, TagWriter out, T value) throws IOException;
	
	/**
	 * Perform the revalidation of the given object using the given validator in the given scope.
	 * <p>
	 * The {@link #getExecutionScope() current scope} is switched to the given scope for only the
	 * specified revalidation operation. The {@link DisplayContext} implementation is responsible
	 * for switching back to the original scope after this call returns.
	 * </p>
	 * 
	 * @param scope
	 *        must not be <code>null</code>
	 * @see Validator#validate(DisplayContext, UpdateQueue, Object)
	 */
	public <T> void validateScoped(ControlScope scope, Validator<? super T> validator, UpdateQueue queue, T obj);
	
	/**
	 * Executes the given command within the given scope.
	 * 
	 * <p>
	 * The {@link #getExecutionScope() current scope} is switched to the given scope for only the
	 * specified command execution. The {@link DisplayContext} implementation is responsible for
	 * switching back to the original scope after this call returns.
	 * </p>
	 * 
	 * @param executionScope
	 *        The {@link ControlScope} to return in {@link #getExecutionScope()} during command
	 *        execution.
	 * @param command
	 *        The command to execute.
	 * 
	 * @return The result of the given command.
	 */
	HandlerResult executeScoped(ControlScope executionScope, Command command);

	/**
	 * Returns the {@link WindowScope} of the currently processed request.
	 */
	@Override
	public WindowScope getWindowScope();

	/**
	 * The {@link LayoutContext} the current request is targeted to.
	 * 
	 * @return The current {@link LayoutContext}, or <code>null</code>, if the current request is
	 *         does not target a layout tree.
	 */
	LayoutContext getLayoutContext();

	/**
	 * Media to which output is rendered.
	 */
	default Media getOutputMedia() {
		return Media.BROWSER;
	}

}
