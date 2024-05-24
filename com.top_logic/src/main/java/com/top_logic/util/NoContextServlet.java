/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.basic.SessionContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;

/**
 * {@link HttpServlet} that installs a {@link DisplayContext} and a {@link SessionContext} if not
 * already exists.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class NoContextServlet extends HttpServlet {

	private static final String ACCEPT_LANGUAGE_HEADER = "accept-language";

	@Override
	protected final void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		/* The type parameters are necessary here. Without them, Eclipse reports an error. */
		TopLogicServlet.<IOException, ServletException> withSessionIdLogMark(req,
			() -> executeInInteraction(req, resp));
	}

	private void executeInInteraction(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException {
		ServletContext servletContext = getServletContext();
		DefaultDisplayContext displayContext = DefaultDisplayContext.setupDisplayContext(servletContext, req, resp);
		try {
			installSessionContext(req, displayContext);
			doService(req, resp);
		} finally {
			DefaultDisplayContext.teardownDisplayContext(req);
		}
	}

	private void installSessionContext(HttpServletRequest req, DefaultDisplayContext displayContext) {
		TLContextManager contextManager = TLContextManager.getManager();
		ThreadContext newSubSessionContext = contextManager.newSubSessionContext();
		displayContext.installSubSessionContext(newSubSessionContext);

		SessionContext sessionContext = ensureSession(contextManager, req);
		newSubSessionContext.setSessionContext(sessionContext);
		newSubSessionContext.setContextId(sessionContext.getOriginalContextId());
		initLocale(newSubSessionContext, req);

		displayContext.installSessionContext(sessionContext);
		contextManager.registerSubSessionInSessionForInteraction(displayContext);
	}

	private void initLocale(ThreadContext subsession, HttpServletRequest req) {
		Locale locale = Resources.getAcceptLanguageLocale(req.getHeader(ACCEPT_LANGUAGE_HEADER));
		subsession.setCurrentLocale(locale);
	}

	private SessionContext ensureSession(TLContextManager contextManager, HttpServletRequest req) {
		SessionContext sessionContext = lookupSessionContext(req);
		if (sessionContext != null) {
			return sessionContext;
		}
		return createSession(contextManager);
	}

	private SessionContext createSession(TLContextManager contextManager) {
		SessionContext sessionContext = contextManager.newSessionContext();
		sessionContext.setOriginalContextId(NoContextServlet.class.getName());
		return sessionContext;
	}

	private SessionContext lookupSessionContext(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		if (session == null) {
			return null;
		}
		return SessionService.getInstance().getSession(session);
	}

	/**
	 * The actual {@link HttpServlet#service(HttpServletRequest, HttpServletResponse)} implementation.
	 */
	protected void doService(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.service(req, resp);
	}

}

