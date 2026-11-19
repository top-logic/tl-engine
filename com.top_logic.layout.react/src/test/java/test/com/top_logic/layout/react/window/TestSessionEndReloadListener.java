/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.window;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import jakarta.servlet.http.HttpSession;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.AssertNoErrorLogListener;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.gui.layout.TLLayoutServlet;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.window.SessionEndReloadListener;

/**
 * Tests that {@link SessionEndReloadListener} reloads the windows of a session that ends, also
 * while the {@link SessionService} shuts down.
 */
public class TestSessionEndReloadListener extends BasicTestCase {

	private static final String WINDOW = "vReloadTab";

	private final List<Person> _created = new ArrayList<>();

	private int _personCount;

	@Override
	protected void tearDown() throws Exception {
		ModuleUtil.INSTANCE.startUp(SessionService.Module.INSTANCE);
		for (Person person : _created) {
			if (person.tValid()) {
				KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
				try (Transaction tx = kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
					person.tDelete();
					tx.commit();
				}
			}
		}
		_created.clear();
		super.tearDown();
	}

	/**
	 * A session dropped from the session table while its HTTP session lives on reloads its windows.
	 */
	public void testSessionDroppedFromSessionTable() throws Exception {
		HttpSession session = login(createPerson());
		SSEUpdateQueue queue = ReactWindowRegistry.forSession(session).getOrCreateQueue(WINDOW);
		int eventsBefore = queue.pendingEventCount();

		SessionService.getInstance().invalidateSession(session.getId());

		assertEquals("The window of the ended session must be told to reload.", eventsBefore + 1,
			queue.pendingEventCount());
		session.invalidate();
	}

	/**
	 * Shutting down the {@link SessionService} ends all sessions without logging an error and tells
	 * their windows to reload.
	 */
	public void testShutdownOfSessionService() throws Exception {
		HttpSession session = login(createPerson());
		SSEUpdateQueue queue = ReactWindowRegistry.forSession(session).getOrCreateQueue(WINDOW);
		int eventsBefore = queue.pendingEventCount();

		AssertNoErrorLogListener log = new AssertNoErrorLogListener(false);
		try {
			ModuleUtil.INSTANCE.shutDown(SessionService.Module.INSTANCE);
		} finally {
			log.deactivate();
		}

		log.assertNoErrorLogged("Shutting down the session service with a logged-in session.");
		// The test container does not report the invalidation of the session to its attributes, so
		// the reload is the one requested by the listener.
		assertEquals("The window of the ended session must be told to reload.", eventsBefore + 1,
			queue.pendingEventCount());
	}

	private static HttpSession login(Person user) throws Exception {
		ServletRunner runner = new ServletRunner();
		runner.registerServlet("lpServlet", TLLayoutServlet.class.getName());
		InvocationContext invocation =
			runner.newClient().newInvocation(new PostMethodWebRequest("http://ignore/lpServlet"));
		// Keep the session context of the test thread.
		return ThreadContext.inSystemContext(TestSessionEndReloadListener.class,
			() -> SessionService.getInstance().loginUser(invocation.getRequest(), invocation.getResponse(), user));
	}

	private Person createPerson() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			Person result = Person.create(kb, "sessionEndReloadUser" + (++_personCount),
				PersonManager.getManager().getRoot().getAuthenticationDevice());
			tx.commit();
			_created.add(result);
			return result;
		}
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		Test test = ServiceTestSetup.createSetup(new TestSuite(TestSessionEndReloadListener.class),
			SessionService.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(test);
	}

}
