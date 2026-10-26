/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.window;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;
import junit.framework.TestSuite;

import jakarta.servlet.http.HttpSession;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.react.servlet.FakeAsyncRequest;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.gui.layout.TLLayoutServlet;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.UpdateChainView;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.Interaction;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.util.TLContext;

/**
 * Tests that the heartbeat of a window ({@link SSEUpdateQueue#sendHeartbeat()}) delivers model
 * events only within an {@link Interaction}, and the
 * {@link ReactWindowRegistry#tryBeginInteraction(long)} it begins that interaction with.
 */
public class TestHeartbeatInteraction extends BasicTestCase {

	private static final String WINDOW = "vTab";

	/** Time in milliseconds a helper thread is given to do its part. */
	private static final long THREAD_TIMEOUT_MILLIS = 10_000;

	/** A short time in milliseconds {@link ReactWindowRegistry#tryBeginInteraction(long)} waits. */
	private static final long SHORT_WAIT_MILLIS = 50;

	private final List<Person> _created = new ArrayList<>();

	private int _personCount;

	/** The session the running test logged in, ended in {@link #tearDown()}. */
	private HttpSession _session;

	/** The window registry of {@link #_session}. */
	private ReactWindowRegistry _registry;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_session = login(createPerson());
		_registry = ReactWindowRegistry.forSession(_session);
	}

	@Override
	protected void tearDown() throws Exception {
		TLSubSessionContext subSession = sessionContext(_session).getSubSession(WINDOW);
		if (subSession != null) {
			ThreadContextManager.inContext(subSession, () -> _registry.windowClosed(WINDOW));
		} else {
			_registry.windowClosed(WINDOW);
		}
		SessionService.getInstance().terminateSession(_session.getId());
		_session = null;
		_registry = null;
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
	 * A heartbeat while a request holds the window's interaction delivers no commit made in the
	 * meantime. The commit reaches a listener the request registered on the changed object, once
	 * the interaction has ended.
	 */
	public void testHeartbeatLeavesBusyWindowAlone() throws Exception {
		SSEUpdateQueue queue = displayWindow();
		queue.setConnection(new FakeAsyncRequest().context());
		Person changed = createPerson();
		List<ModelChangeEvent> global = observe();
		List<ModelChangeEvent> ofChanged = new ArrayList<>();

		try (Interaction interaction = _registry.beginInteraction()) {
			changeLanguage(changed);
			inOtherThread(queue::sendHeartbeat);
			assertTrue("A heartbeat must not deliver model events to a window within an interaction.",
				global.isEmpty());

			// The request builds the page: it starts observing the object it displays.
			_registry.getOrCreateModelScope(WINDOW).addModelListener(changed, ofChanged::add);
		}
		inOtherThread(queue::sendHeartbeat);

		assertFalse("The commit must be delivered to the listener registered within the interaction.",
			ofChanged.isEmpty());
		assertFalse("The commit must be delivered once the interaction has ended.", global.isEmpty());
	}

	/**
	 * {@link ReactWindowRegistry#tryBeginInteraction(long)} answers no interaction while another
	 * thread holds one, a nested one to the thread holding one, and an interaction once the other
	 * thread released its own.
	 */
	public void testTryBeginInteraction() throws Exception {
		CountDownLatch held = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		AtomicReference<Throwable> problem = new AtomicReference<>();
		Thread holder = new Thread(() -> {
			try (Interaction interaction = _registry.beginInteraction()) {
				held.countDown();
				release.await(THREAD_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
			} catch (Throwable ex) {
				problem.set(ex);
			}
		});
		holder.start();
		assertTrue(held.await(THREAD_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS));

		assertNull("No interaction while another thread holds one.", _registry.tryBeginInteraction(0));
		assertNull("No interaction while another thread holds one for longer than the timeout.",
			_registry.tryBeginInteraction(SHORT_WAIT_MILLIS));

		release.countDown();
		holder.join(THREAD_TIMEOUT_MILLIS);
		assertNull(problem.get());

		try (Interaction interaction = _registry.tryBeginInteraction(0)) {
			assertNotNull("An interaction once the other thread released its own.", interaction);
			try (Interaction nested = _registry.tryBeginInteraction(0)) {
				assertNotNull("A nested interaction for the thread holding one.", nested);
			}
			inOtherThread(() -> assertNull("The outer interaction still holds the lock.",
				_registry.tryBeginInteraction(0)));
		}

		inOtherThread(() -> {
			try (Interaction interaction = _registry.tryBeginInteraction(0)) {
				assertNotNull("A closed interaction must release the lock.", interaction);
			}
		});
	}

	/**
	 * A thread interrupted while it waits for an interaction gets none and keeps its interrupt
	 * flag.
	 */
	public void testInterruptedWhileWaiting() throws Exception {
		try (Interaction interaction = _registry.beginInteraction()) {
			inOtherThread(() -> {
				Thread.currentThread().interrupt();
				assertNull(_registry.tryBeginInteraction(THREAD_TIMEOUT_MILLIS));
				assertTrue("The interrupt flag must stay set.", Thread.interrupted());
			});
		}
	}

	/**
	 * Runs the given action in a thread of its own, waits for it to complete, and rethrows what it
	 * failed with.
	 */
	private static void inOtherThread(Runnable action) throws Exception {
		AtomicReference<Throwable> problem = new AtomicReference<>();
		Thread thread = new Thread(() -> {
			try {
				action.run();
			} catch (Throwable ex) {
				problem.set(ex);
			}
		});
		thread.start();
		thread.join(THREAD_TIMEOUT_MILLIS);
		assertFalse("The helper thread did not complete in time.", thread.isAlive());
		Throwable ex = problem.get();
		if (ex instanceof Error error) {
			throw error;
		}
		if (ex instanceof Exception exception) {
			throw exception;
		}
	}

	/**
	 * Creates the window {@link #WINDOW} with its subsession, as rendering its page does, and
	 * connects its queue to the registry, as connecting its stream does.
	 */
	private SSEUpdateQueue displayWindow() {
		TLSessionContext sessionContext = sessionContext(_session);
		SSEUpdateQueue queue = _registry.getOrCreateQueue(WINDOW);
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLContext subSession = new TLContext();
		subSession.setSessionContext(sessionContext);
		sessionContext.setIfAbsent(WINDOW, subSession);
		subSession.updateSessionRevision(kb.getHistoryManager(), ((UpdateChainView) kb.getUpdateChain()).current());
		queue.setWindowContext(WINDOW, sessionContext, _registry);
		return queue;
	}

	/**
	 * Registers a listener on the model scope of {@link #WINDOW}, after the events pending so far
	 * have been delivered.
	 */
	private List<ModelChangeEvent> observe() {
		List<ModelChangeEvent> events = new ArrayList<>();
		_registry.getOrCreateModelScope(WINDOW);
		_registry.synthesizeModelEvents(WINDOW);
		_registry.getOrCreateModelScope(WINDOW).addModelListener(events::add);
		return events;
	}

	private static void changeLanguage(Person person) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			person.setLanguage(Locale.GERMAN.equals(person.getLanguage()) ? Locale.ENGLISH : Locale.GERMAN);
			tx.commit();
		}
	}

	private static TLSessionContext sessionContext(HttpSession session) {
		return SessionService.getInstance().getSession(session);
	}

	private static HttpSession login(Person user) throws Exception {
		ServletRunner runner = new ServletRunner();
		runner.registerServlet("lpServlet", TLLayoutServlet.class.getName());
		InvocationContext invocation =
			runner.newClient().newInvocation(new PostMethodWebRequest("http://ignore/lpServlet"));
		// Keep the session context of the test thread.
		return ThreadContext.inSystemContext(TestHeartbeatInteraction.class,
			() -> SessionService.getInstance().loginUser(invocation.getRequest(), invocation.getResponse(), user));
	}

	private Person createPerson() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			Person result = Person.create(kb, "heartbeatUser" + (++_personCount),
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
		Test test = ServiceTestSetup.createSetup(new TestSuite(TestHeartbeatInteraction.class),
			SessionService.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(test);
	}

}
