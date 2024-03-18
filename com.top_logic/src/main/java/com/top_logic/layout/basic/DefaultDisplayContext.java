/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.logging.LogConfigurator;
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

	/**
	 * The name of log mark for the session id.
	 * <p>
	 * <em>When the value is changed, the Log4j configuration files have to be updated, as they use
	 * the value.</em>
	 * </p>
	 * <p>
	 * The value of this constant is used in Log4J configuration files to extract and log the
	 * session id with every log statement.
	 * </p>
	 */
	private static final String TL_SESSION_ID_LOG_MARK = "tl-session-id";

	private static final String DISPLAY_CONTEXT_REQUEST_ATTRIBUTE = DisplayContext.class.getName();

	private final ServletContext servletContext;
	private final HttpServletRequest   request;

	private HttpServletResponse response;

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
		super();
        this.servletContext          = servletContext;
        this.request                 = request;
        this.response                = response;
    }

	@Override
	public String getContextPath() {
		checkNotInvalid();
		return request.getContextPath();
	}
	
	@Override
	public String getCharacterEncoding() {
		checkNotInvalid();
		return response.getCharacterEncoding();
	}

	@Override
	public Object getAttribute(String name) {
		checkNotInvalid();
		return request.getAttribute(name);
	}

	@Override
	public UserAgent getUserAgent() {
		checkNotInvalid();
		return UserAgent.getUserAgent(request);
	}

	@Override
	public HttpServletRequest asRequest() {
		checkNotInvalid();
		return request;
	}

	@Override
	public HttpServletResponse asResponse() {
		checkNotInvalid();
		return response;
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
		if (request != null) {
			return (DisplayContext) request.getAttribute(DISPLAY_CONTEXT_REQUEST_ATTRIBUTE);
		} else {
			return (DisplayContext) ThreadContextManager.getInteraction();
		}
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
	 * Note: It is essential to call
	 * {@link #teardownDisplayContext(HttpServletRequest, AbstractDisplayContext)} in a following
	 * try-finally-block.
	 * </p>
	 * 
	 * @see #setupDisplayContext(TLSubSessionContext, ServletContext, HttpServletRequest, HttpServletResponse)
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
	 * Note: It is essential to call
	 * {@link #teardownDisplayContext(HttpServletRequest, AbstractDisplayContext)} in a following
	 * try-finally-block.
	 * </p>
	 */
	public static DefaultDisplayContext setupDisplayContext(TLSubSessionContext sessionContext, ServletContext servletContext,
			HttpServletRequest request, HttpServletResponse response) {
		DefaultDisplayContext context =
			(DefaultDisplayContext) TLContextManager.getManager().newInteraction(servletContext, request, response);
		context.installSubSessionContext(sessionContext);
		setupDisplayContext(request, context);
		return context;
	}
	
	/**
	 * Installs the given display context for the current thread.
	 * 
	 * <p>
	 * Note: It is essential to call
	 * {@link #teardownDisplayContext(HttpServletRequest, AbstractDisplayContext)} in a following
	 * try-finally-block.
	 * </p>
	 */
	public static void setupDisplayContext(HttpServletRequest request, DisplayContext context) {
		// Note: The display context is linked both to the current thread *and* the current request
		// for safety reasons: If a context is set up but forgotten to tear down, it
		// must not be accidentally reused by the next request. This is ensured by the setup
		// mechanism in TopLogicServlet only depending on the request attribute, not on the thread
		// local variable.
		if (request != null) {
			request.setAttribute(DISPLAY_CONTEXT_REQUEST_ATTRIBUTE, context);
		}
		ThreadContextManager.getManager().setInteraction(context);
		addSessionIdLogMark(request);
	}

	/**
	 * Invalidates the given {@link AbstractDisplayContext} and removes it from the corresponding
	 * request.
	 * 
	 * @param request
	 *        The request for which the given {@link DisplayContext} was
	 *        {@link #setupDisplayContext(TLSubSessionContext, ServletContext, HttpServletRequest, HttpServletResponse)
	 *        set up}.
	 */
	public static void teardownDisplayContext(HttpServletRequest request, AbstractDisplayContext displayContext) {
		if (request != null) {
			request.removeAttribute(DISPLAY_CONTEXT_REQUEST_ATTRIBUTE);
		}
		ThreadContextManager.getManager().removeInteraction();
		removeSessionIdLogMark();
	}

	private static void addSessionIdLogMark(HttpServletRequest request) {
		String sessionId = getSessionId(request);
		if (StringServices.isEmpty(sessionId)) {
			return;
		}
		String logSnippet = "S(" + sessionId + ") ";
		LogConfigurator.getInstance().addLogMark(TL_SESSION_ID_LOG_MARK, logSnippet);
	}

	private static void removeSessionIdLogMark() {
		LogConfigurator.getInstance().removeLogMark(TL_SESSION_ID_LOG_MARK);
	}

	private static String getSessionId(HttpServletRequest request) {
		if (request == null) {
			/* This happens in tests. */
			return null;
		}
		HttpSession session = request.getSession(false);
		return session == null ? null : session.getId();
	}

	/**
	 * The {@link DisplayContext} of the current request.
	 * 
	 * <p>
	 * Note: This method is only for convenience for the application. The framework servlets must
	 * only rely on {@link #getDisplayContext(ServletRequest)} for safety reasons (see
	 * {@link #setupDisplayContext(HttpServletRequest, DisplayContext)}).
	 * </p>
	 */
	public static DisplayContext getDisplayContext() {
		return getDisplayContext((HttpServletRequest) null);
	}

	@Override
	public ServletContext asServletContext() {
		checkNotInvalid();
		return servletContext;
	}

	/**
	 * Updates the underlying {@link HttpServletResponse}.
	 * 
	 * @param response
	 *        The new response to use.
	 */
	@FrameworkInternal
	public void setResponse(HttpServletResponse response) {
		this.response = response;
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