/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.UserAgent;
import com.top_logic.util.TLContextManager;

/**
 * Default implementation of a {@link com.top_logic.layout.DisplayContext} based on a
 * {@link HttpServletRequest}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultDisplayContext extends AbstractDisplayContext {

	private long _sessionRevision;

    /**
	 * Creates a new {@link DefaultDisplayContext}.
	 * 
	 * <p>
	 * Applications must call
	 * {@link TLContextManager#newInteraction(ServletContext, HttpServletRequest, HttpServletResponse)}
	 * </p>
	 */
	@FrameworkInternal
	public DefaultDisplayContext(ServletContext servletContext, HttpServletRequest request,
			HttpServletResponse response) {
		super(servletContext, request, response);
    }

	@Override
	public String getContextPath() {
		checkNotInvalid();
		return asRequest().getContextPath();
	}
	
	@Override
	public String getCharacterEncoding() {
		checkNotInvalid();
		return asResponse().getCharacterEncoding();
	}

	@Override
	public Object getAttribute(String name) {
		checkNotInvalid();
		return asRequest().getAttribute(name);
	}

	@Override
	public UserAgent getUserAgent() {
		checkNotInvalid();
		return UserAgent.getUserAgent(asRequest());
	}
	
	/**
	 * Lookup the {@link DisplayContext}, in which rendering happens.
	 */
	public static DisplayContext getDisplayContext(ServletRequest request) {
		DisplayContext result = lookupDisplayContext(request);

		/* must not be null. Otherwise the request the way of the request to come into the system
		 * does not use the {@link TopLogicServlet}. */
		assert result != null : "No displayContext available";

		return result;
	}

	private static DisplayContext lookupDisplayContext(ServletRequest request) {
		return (DisplayContext) ThreadContextManager.lookupInteractionContext(request);
	}

	/**
	 * Lookup or creates an abstraction for the context, in which rendering happens.
	 */
	public static DisplayContext getDisplayContext(PageContext pageContext) {
		return getDisplayContext(pageContext.getRequest());
	}
	
	/**
	 * Whether a {@link DisplayContext} is currently available.
	 */
	public static boolean hasDisplayContext() {
		return hasDisplayContext(null);
	}

	/**
	 * Whether a {@link DisplayContext} is available for the given request.
	 */
	public static boolean hasDisplayContext(HttpServletRequest request) {
		return lookupDisplayContext(request) != null;
	}
	
	/**
	 * Creates an abstraction for the context, in which rendering happens and installs it for the
	 * current thread.
	 * 
	 * <p>
	 * Note: It is essential to call {@link #teardownDisplayContext(HttpServletRequest)} in a
	 * following try-finally-block.
	 * </p>
	 * 
	 * @see #setupDisplayContext(TLSubSessionContext, ServletContext, HttpServletRequest,
	 *      HttpServletResponse)
	 */
	@CalledFromJSP
	public static DefaultDisplayContext setupDisplayContext(ServletContext servletContext,
			HttpServletRequest request, HttpServletResponse response) {
		return setupDisplayContext(null, servletContext, request, response);
	}

	/**
	 * Creates an abstraction for the context, in which rendering happens and installs it for the
	 * current thread.
	 * 
	 * <p>
	 * Note: It is essential to call {@link #teardownDisplayContext(HttpServletRequest)} in a
	 * following try-finally-block.
	 * </p>
	 */
	public static DefaultDisplayContext setupDisplayContext(TLSubSessionContext sessionContext, ServletContext servletContext,
			HttpServletRequest request, HttpServletResponse response) {
		InteractionContext context = ThreadContextManager.setupInteractionContext(sessionContext, servletContext, request, response);
		return (DefaultDisplayContext) context;
	}

	/**
	 * Installs the given display context for the current thread.
	 * 
	 * <p>
	 * Note: It is essential to call {@link #teardownDisplayContext(HttpServletRequest)} in a
	 * following try-finally-block.
	 * </p>
	 */
	public static void setupDisplayContext(HttpServletRequest request, InteractionContext context) {
		ThreadContextManager.setupInteractionContext(request, context);
	}

	/**
	 * Invalidates the {@link DisplayContext} and removes it from the corresponding request.
	 * 
	 * @param request
	 *        The request for which the given {@link DisplayContext} was
	 *        {@link #setupDisplayContext(TLSubSessionContext, ServletContext, HttpServletRequest, HttpServletResponse)
	 *        set up}.
	 */
	public static void teardownDisplayContext(HttpServletRequest request) {
		ThreadContextManager.teardownInteractionContext(request);
	}

	/**
	 * The {@link DisplayContext} of the current request.
	 * 
	 * <p>
	 * Note: This method is only for convenience for the application. The framework servlets must
	 * only rely on {@link #getDisplayContext(ServletRequest)} for safety reasons (see
	 * {@link #setupDisplayContext(HttpServletRequest, InteractionContext)}).
	 * </p>
	 */
	public static DisplayContext getDisplayContext() {
		return getDisplayContext((HttpServletRequest) null);
	}





	@Override
	public long getInteractionRevision(HistoryManager historyManager) {
		return _sessionRevision;
	}

	@Override
	public long updateInteractionRevision(HistoryManager historyManager, long newSessionRevision) {
		long formerInteractionRevision = _sessionRevision;
		_sessionRevision = newSessionRevision;
		return formerInteractionRevision;
	}

}