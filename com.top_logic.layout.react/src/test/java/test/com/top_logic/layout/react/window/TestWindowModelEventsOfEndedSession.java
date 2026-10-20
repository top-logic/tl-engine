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
import jakarta.servlet.http.HttpSessionBindingEvent;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.gui.layout.TLLayoutServlet;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.window.WindowEntry;
import com.top_logic.model.listen.ModelChangeEvent;

/**
 * Tests that {@link ReactWindowRegistry#synthesizeModelEvents(String)} delivers model events only
 * to the windows of a session that has a live user, and that a torn-down window observes the model
 * no longer.
 */
public class TestWindowModelEventsOfEndedSession extends BasicTestCase {

	private static final String WINDOW = "vTab";

	private final List<Person> _created = new ArrayList<>();

	private int _personCount;

	@Override
	protected void tearDown() throws Exception {
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
	 * The windows of a logged-in session follow the model.
	 */
	public void testLiveSession() throws Exception {
		HttpSession session = login(createPerson());
		ReactWindowRegistry registry = ReactWindowRegistry.forSession(session);
		List<ModelChangeEvent> events = observe(registry);

		createPerson();
		registry.synthesizeModelEvents(WINDOW);

		assertFalse("A live session must receive the model events.", events.isEmpty());
		SessionService.getInstance().terminateSession(session.getId());
	}

	/**
	 * The windows of a session dropped from the session table receive no more model events,
	 * although the HTTP session is still alive.
	 */
	public void testSessionNoLongerRegistered() throws Exception {
		HttpSession session = login(createPerson());
		ReactWindowRegistry registry = ReactWindowRegistry.forSession(session);
		List<ModelChangeEvent> events = observe(registry);

		SessionService.getInstance().invalidateSession(session.getId());
		createPerson();
		registry.synthesizeModelEvents(WINDOW);

		assertTrue("A session without a user must not receive model events.", events.isEmpty());
		session.invalidate();
	}

	/**
	 * The windows of a session whose account was deleted receive no more model events, even before
	 * the session is ended.
	 */
	public void testDeletedUser() throws Exception {
		Person user = createPerson();
		HttpSession session = login(user);
		ReactWindowRegistry registry = ReactWindowRegistry.forSession(session);
		List<ModelChangeEvent> events = observe(registry);

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			user.tDelete();
			tx.commit();
		}
		createPerson();
		registry.synthesizeModelEvents(WINDOW);

		assertTrue("The session of a deleted user must not receive model events.", events.isEmpty());
	}

	/**
	 * A closed window delivers no model events anymore, not even to whoever still holds its entry.
	 */
	public void testClosedWindowReleasesModelScope() throws Exception {
		HttpSession session = login(createPerson());
		ReactWindowRegistry registry = ReactWindowRegistry.forSession(session);
		List<ModelChangeEvent> events = observe(registry);
		WindowEntry entry = registry.getWindow(WINDOW);
		assertFollowsModel(entry, events);

		registry.windowClosed(WINDOW);
		createPerson();
		entry.synthesizeModelEvents();

		assertTrue("A closed window must not receive model events.", events.isEmpty());
		assertNoEventsAfterTeardown(entry);
		SessionService.getInstance().terminateSession(session.getId());
	}

	/**
	 * The windows of an ended session deliver no model events anymore, not even to whoever still
	 * holds their entries.
	 */
	public void testEndedSessionReleasesModelScopes() throws Exception {
		HttpSession session = login(createPerson());
		ReactWindowRegistry registry = ReactWindowRegistry.forSession(session);
		List<ModelChangeEvent> events = observe(registry);
		WindowEntry entry = registry.getWindow(WINDOW);
		assertFollowsModel(entry, events);

		// The test container does not report the end of a session to its attributes; the end is
		// reported as a servlet container does.
		registry.valueUnbound(new HttpSessionBindingEvent(session, ReactWindowRegistry.class.getName()));
		assertNull("The windows of an ended session must be torn down.", registry.getWindow(WINDOW));
		createPerson();
		entry.synthesizeModelEvents();

		assertTrue("A window of an ended session must not receive model events.", events.isEmpty());
		session.invalidate();
	}

	/**
	 * Asserts that the given window, observed by the given events, receives the events of a commit,
	 * and forgets them afterwards.
	 */
	private void assertFollowsModel(WindowEntry entry, List<ModelChangeEvent> events) {
		createPerson();
		entry.synthesizeModelEvents();
		assertFalse("An open window must receive the model events.", events.isEmpty());
		events.clear();
	}

	/**
	 * Asserts that a listener added to the model scope of the given torn-down window hears of no
	 * commit.
	 */
	private void assertNoEventsAfterTeardown(WindowEntry entry) {
		List<ModelChangeEvent> late = new ArrayList<>();
		entry.getOrCreateModelScope().addModelListener(late::add);
		createPerson();
		entry.synthesizeModelEvents();
		assertTrue("The scope of a torn-down window must deliver no events.", late.isEmpty());
	}

	/**
	 * Registers a listener on the model scope of {@link #WINDOW}, after the events pending so far
	 * have been delivered.
	 */
	private static List<ModelChangeEvent> observe(ReactWindowRegistry registry) {
		List<ModelChangeEvent> events = new ArrayList<>();
		registry.getOrCreateModelScope(WINDOW);
		registry.synthesizeModelEvents(WINDOW);
		registry.getOrCreateModelScope(WINDOW).addModelListener(events::add);
		return events;
	}

	private static HttpSession login(Person user) throws Exception {
		ServletRunner runner = new ServletRunner();
		runner.registerServlet("lpServlet", TLLayoutServlet.class.getName());
		InvocationContext invocation =
			runner.newClient().newInvocation(new PostMethodWebRequest("http://ignore/lpServlet"));
		// Keep the session context of the test thread.
		return ThreadContext.inSystemContext(TestWindowModelEventsOfEndedSession.class,
			() -> SessionService.getInstance().loginUser(invocation.getRequest(), invocation.getResponse(), user));
	}

	private Person createPerson() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			Person result = Person.create(kb, "windowEventUser" + (++_personCount),
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
		Test test = ServiceTestSetup.createSetup(new TestSuite(TestWindowModelEventsOfEndedSession.class),
			SessionService.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(test);
	}

}
