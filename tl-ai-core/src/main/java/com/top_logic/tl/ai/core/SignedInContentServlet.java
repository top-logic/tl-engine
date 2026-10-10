/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.UpdateChainView;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.TopLogicServlet;

/**
 * A route that hands stored content to a signed-in account: what the login is, and how a request
 * without one is refused.
 *
 * <p>
 * Sits on {@link TopLogicServlet}, the framework's "a valid session is required" base. A request
 * carrying no login is answered 401 - except for the framework's one-time cookie probe, which
 * redirects a request with no cookie at all to the start page before the route ever sees it (see
 * {@link #handleNoSession}). Whom the content belongs to is the subclass's question, asked in its
 * {@code doGet} against {@link #account(HttpServletRequest)}.
 * </p>
 */
public abstract class SignedInContentServlet extends TopLogicServlet {

	/** Answered to a request that carries no login, in place of the content. */
	protected static final String NOT_AUTHENTICATED = "Not authenticated.";

	/**
	 * Name under which this route keeps its sub-session in the HTTP session.
	 *
	 * <p>
	 * The UI keys a sub-session by browser window, because each window holds its own control tree.
	 * A content route holds no state at all, so one per session is enough - and one shared name
	 * keeps the session from growing a sub-session per request.
	 * </p>
	 */
	protected abstract String subSessionName();

	/**
	 * Puts the acting account's sub-session on the thread before the route reads anything.
	 *
	 * <p>
	 * A session alone puts neither a sub-session nor a person on the thread:
	 * {@code TLContextManager#inInteraction(TLSessionContext, ...)}, which {@link TopLogicServlet}
	 * calls, installs the <em>session</em> context and nothing else, while
	 * {@code TLContext#getPerson()} reads the sub-session. Without one the knowledge base has no
	 * revision to read at and the lookup fails outright. {@code ViewServlet} builds a sub-session per
	 * browser window for the same reason; a content route shares one.
	 * </p>
	 */
	@Override
	protected void doService(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		TLSessionContext session = TLContextManager.getSession();
		if (session != null) {
			installSubSession(request, session);
		}

		super.doService(request, response);
	}

	/**
	 * Answers a request that reached the application without any session at all.
	 *
	 * <p>
	 * {@link TopLogicServlet} sends such a request to the start page once to find out whether the
	 * client takes a cookie; it arrives here only when it came back without one. That probe is
	 * {@code private} and {@code final} in the framework, so the first cookie-less request is a 302
	 * whatever this class says; the second one is answered here, and 401 rather than another
	 * redirect, which an image tag could only fail on.
	 * </p>
	 */
	@Override
	protected void handleNoSession(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		Logger.info("Refused " + describe(request, null) + ": no session.", getClass());
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, NOT_AUTHENTICATED);
	}

	/**
	 * The account this request acts as, or <code>null</code> when nobody is signed in.
	 *
	 * <p>
	 * Read off the request's own HTTP session, which is where the login is recorded - not off the
	 * thread, which carries a person only once something has put one there. The <em>anonymous</em>
	 * account is nobody: an application that lets visitors in gives them a perfectly valid session,
	 * and that is not a login.
	 * </p>
	 */
	protected static Person account(HttpServletRequest request) {
		SessionService sessions = SessionService.getInstance();
		HttpSession httpSession = sessions.getSession(request);
		if (httpSession == null) {
			return null;
		}
		TLSessionContext session = sessions.getSession(httpSession);
		Person account = session == null ? null : session.getOriginalUser();
		return account == null || PersonManager.getManager().isAnonymous(account) ? null : account;
	}

	/** Who asked for what, for the log. Never the session id - a reader could take the session over. */
	protected static String describe(HttpServletRequest request, Person account) {
		String who = account == null ? "an unauthenticated request" : "account '" + account.getName() + "'";
		return who + " " + request.getMethod() + " " + request.getRequestURI();
	}

	/** The first path segment of {@code /route/{id}}, or <code>null</code> when there is none. */
	protected static String extractId(String pathInfo) {
		if (pathInfo == null || pathInfo.length() < 2) {
			return null;
		}
		String id = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
		int slash = id.indexOf('/');
		if (slash >= 0) {
			id = id.substring(0, slash);
		}
		return id.isEmpty() ? null : id;
	}

	/** Installs the session's own sub-session on this thread, creating it on first use. */
	private void installSubSession(HttpServletRequest request, TLSessionContext session) {
		TLSubSessionContext subSession = session.getSubSession(subSessionName());
		if (subSession == null) {
			subSession = (TLSubSessionContext) ThreadContextManager.getManager().newSubSessionContext();
			subSession.setSessionContext(session);
			subSession = session.setIfAbsent(subSessionName(), subSession);
		}
		subSession.setPerson(session.getOriginalUser());

		// Every request reads the newest stand: content stored a moment ago must be deliverable, and
		// a sub-session that outlives the request would otherwise keep the revision of its first one.
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		subSession.updateSessionRevision(kb.getHistoryManager(), ((UpdateChainView) kb.getUpdateChain()).current());

		DefaultDisplayContext.getDisplayContext(request).installSubSessionContext(subSession);
	}
}
