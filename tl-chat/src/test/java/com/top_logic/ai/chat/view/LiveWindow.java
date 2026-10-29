/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import jakarta.servlet.http.HttpSession;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.gui.layout.TLLayoutServlet;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.UpdateChainView;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.util.TLContext;

/**
 * A browser window of a logged-in session, as the engine knows it once the window's page was
 * served: a {@link ReactWindowRegistry} of a live session, holding a queue for the window, whose
 * session context holds the window's sub-session under the window's name.
 *
 * <p>
 * Test fixture. Requires {@link SessionService} and a knowledge base. {@link #close() Closing} it
 * ends the login.
 * </p>
 */
final class LiveWindow implements AutoCloseable {

	private final HttpSession _session;

	private final ReactWindowRegistry _registry;

	private final String _windowName;

	private final TLSubSessionContext _subSession;

	private LiveWindow(HttpSession session, ReactWindowRegistry registry, String windowName,
			TLSubSessionContext subSession) {
		_session = session;
		_registry = registry;
		_windowName = windowName;
		_subSession = subSession;
	}

	/**
	 * Logs in {@code account} and opens a window of that login displaying the given sub-session.
	 *
	 * @param account
	 *        The account to log in.
	 * @param windowName
	 *        The window's name, the key of its sub-session in its session context.
	 * @param subSession
	 *        The sub-session the window displays; it stays in its own session context.
	 *        <code>null</code> creates a new sub-session of the login, acting as {@code account}.
	 */
	static LiveWindow open(Person account, String windowName, TLSubSessionContext subSession) throws Exception {
		HttpSession session = login(account);
		ReactWindowRegistry registry = ReactWindowRegistry.forSession(session);
		TLSubSessionContext displayed = subSession;
		if (displayed == null) {
			TLSessionContext sessionContext = SessionService.getInstance().getSession(session);
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			TLContext created = new TLContext();
			created.setSessionContext(sessionContext);
			created.setPerson(account);
			created.updateSessionRevision(kb.getHistoryManager(), ((UpdateChainView) kb.getUpdateChain()).current());
			displayed = created;
		}
		TLSessionContext sessionContext = displayed.getSessionContext();
		sessionContext.setIfAbsent(windowName, displayed);
		SSEUpdateQueue queue = registry.getOrCreateQueue(windowName);
		queue.setWindowContext(windowName, sessionContext, registry);
		return new LiveWindow(session, registry, windowName, displayed);
	}

	/**
	 * The registry of the window's session.
	 */
	ReactWindowRegistry registry() {
		return _registry;
	}

	/**
	 * The window's name.
	 */
	String windowName() {
		return _windowName;
	}

	/**
	 * The sub-session the window displays.
	 */
	TLSubSessionContext subSession() {
		return _subSession;
	}

	@Override
	public void close() {
		SessionService.getInstance().terminateSession(_session.getId());
	}

	private static HttpSession login(Person account) throws Exception {
		ServletRunner runner = new ServletRunner();
		runner.registerServlet("lpServlet", TLLayoutServlet.class.getName());
		InvocationContext invocation =
			runner.newClient().newInvocation(new PostMethodWebRequest("http://ignore/lpServlet"));
		return ThreadContext.inSystemContext(LiveWindow.class,
			() -> SessionService.getInstance().loginUser(invocation.getRequest(), invocation.getResponse(), account));
	}
}
