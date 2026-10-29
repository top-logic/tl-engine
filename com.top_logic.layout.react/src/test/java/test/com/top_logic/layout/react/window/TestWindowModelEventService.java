/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

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
import com.top_logic.layout.react.protocol.PageNavigationEvent;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.Interaction;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.window.WindowModelEventService;
import com.top_logic.model.TLObject;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.util.TLContext;

/**
 * Tests that {@link WindowModelEventService} delivers a commit made by another thread to the
 * windows observing the changed objects, without any request of those windows.
 */
public class TestWindowModelEventService extends BasicTestCase {

	private static final String WINDOW = "vTab";

	private static final String OTHER_WINDOW = "vOther";

	/**
	 * Time in milliseconds a delivery may take beyond the configured interval, for a loaded build
	 * machine.
	 */
	private static final long TOLERANCE_MILLIS = 2_000;

	/** Time in milliseconds a helper thread is given to do its part. */
	private static final long THREAD_TIMEOUT_MILLIS = 10_000;

	private final List<Person> _created = new ArrayList<>();

	private int _personCount;

	/** The session the running test logged in, ended in {@link #tearDown()}. */
	private HttpSession _session;

	/** The window registry of {@link #_session}. */
	private ReactWindowRegistry _registry;

	/** The configured minimum time between two deliveries to a window. */
	private long _interval;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_session = login(createPerson());
		_registry = ReactWindowRegistry.forSession(_session);
		_interval = WindowModelEventService.getInstance().getSyncInterval();
	}

	@Override
	protected void tearDown() throws Exception {
		closeWindow(WINDOW);
		closeWindow(OTHER_WINDOW);
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
	 * A commit made by another thread reaches a listener of a window observing the changed object
	 * within the configured interval, without a request of the window.
	 */
	public void testForeignCommitReachesWindow() throws Exception {
		displayWindow(WINDOW);
		Person changed = createPerson();
		Recorder recorder = observe(WINDOW, changed);

		inOtherThread(() -> changeLanguage(changed));
		long committed = System.currentTimeMillis();

		assertTrue("The commit of another thread must reach the window.",
			recorder.await(event -> updates(event, changed), _interval + TOLERANCE_MILLIS));
		long delay = System.currentTimeMillis() - committed;
		assertTrue("The commit must be delivered within the interval, took " + delay + "ms.",
			delay <= _interval + TOLERANCE_MILLIS);
	}

	/**
	 * A window whose model scope does not observe the changed object is not synchronized: the
	 * commit stays pending in its scope.
	 */
	public void testUnaffectedWindowIsLeftAlone() throws Exception {
		displayWindow(WINDOW);
		displayWindow(OTHER_WINDOW);
		Person changed = createPerson();
		Person unchanged = createPerson();
		Recorder affected = observe(WINDOW, changed);
		Recorder unaffected = observe(OTHER_WINDOW, unchanged);

		inOtherThread(() -> changeLanguage(changed));

		assertTrue("The observing window must receive the commit.",
			affected.await(event -> updates(event, changed), _interval + TOLERANCE_MILLIS));
		// Give a synchronization of the other window, if one was requested, the time to complete.
		Thread.sleep(_interval);
		assertTrue("The window not observing the changed object must hear of nothing.",
			unaffected.events().isEmpty());

		// Had the other window been synchronized, its scope would have passed the commit already.
		Recorder late = new Recorder();
		inWindow(OTHER_WINDOW, () -> {
			_registry.getOrCreateModelScope(OTHER_WINDOW).addModelListener(changed, late);
			_registry.synthesizeModelEvents(OTHER_WINDOW);
		});
		assertTrue("The commit must still be pending in the window not observing the changed object.",
			late.events().stream().anyMatch(event -> updates(event, changed)));
	}

	/**
	 * Many commits in quick succession are delivered in few deliveries, the last commit included.
	 */
	public void testBurstOfCommitsIsCoalesced() throws Exception {
		displayWindow(WINDOW);
		int commits = 20;
		List<Person> changed = new ArrayList<>();
		for (int n = 0; n < commits; n++) {
			changed.add(createPerson());
		}
		Recorder recorder = observe(WINDOW, changed);

		long start = System.currentTimeMillis();
		inOtherThread(() -> changed.forEach(TestWindowModelEventService::changeLanguage));
		long duration = System.currentTimeMillis() - start;
		Person last = changed.get(changed.size() - 1);

		assertTrue("The last commit of the burst must be delivered.",
			recorder.await(event -> updates(event, last), duration + _interval + TOLERANCE_MILLIS));
		// Deliveries still under way may follow.
		Thread.sleep(_interval);

		Set<TLObject> delivered = new HashSet<>();
		for (ModelChangeEvent event : recorder.events()) {
			event.getUpdated().forEach(delivered::add);
		}
		assertTrue("Every commit of the burst must be delivered.", delivered.containsAll(changed));
		long bound = duration / _interval + 2;
		// The listener is notified once per observed object an event updates.
		Set<ModelChangeEvent> distinct = Collections.newSetFromMap(new IdentityHashMap<>());
		distinct.addAll(recorder.events());
		int deliveries = distinct.size();
		assertTrue("A burst of " + commits + " commits within " + duration + "ms must be delivered in at most "
			+ bound + " deliveries, got " + deliveries + ".", deliveries <= bound);
	}

	/**
	 * A window that is busy when its commit is to be delivered receives it about one interval after
	 * it is no longer busy.
	 */
	public void testBusyWindowReceivesCommitAfterInteraction() throws Exception {
		displayWindow(WINDOW);
		Person changed = createPerson();
		Recorder recorder = observe(WINDOW, changed);

		long released;
		try (Interaction interaction = _registry.beginInteraction()) {
			inOtherThread(() -> changeLanguage(changed));
			Thread.sleep(2 * _interval);
			assertTrue("A busy window must not receive model events.", recorder.events().isEmpty());
			released = System.currentTimeMillis();
		}

		assertTrue("The commit must be delivered once the window is no longer busy.",
			recorder.await(event -> updates(event, changed), _interval + TOLERANCE_MILLIS));
		long delay = System.currentTimeMillis() - released;
		assertTrue("The commit must be delivered within about one interval after the interaction, took "
			+ delay + "ms.", delay <= _interval + TOLERANCE_MILLIS);
	}

	/**
	 * A synchronization that finds the window busy writes nothing to the client: the events the
	 * busy interaction holds back are delivered when it closes, after the controls it retires are
	 * known, and never by the synchronization in the middle of it.
	 */
	public void testBusyWindowIsNotFlushed() throws Exception {
		FakeAsyncRequest stream = displayWindow(WINDOW);
		SSEUpdateQueue queue = _registry.getQueue(WINDOW);
		Person changed = createPerson();
		Recorder recorder = observe(WINDOW, changed);

		String written;
		try (Interaction interaction = _registry.beginInteraction()) {
			written = stream.body();
			queue.enqueue(PageNavigationEvent.create());
			inOtherThread(() -> changeLanguage(changed));
			Thread.sleep(2 * _interval);
			assertEquals("A synchronization must not write while another thread's interaction holds the window.",
				written, stream.body());
		}
		assertFalse("The interaction must deliver its events when it closes.", written.equals(stream.body()));
		assertTrue("The commit must be delivered once the window is no longer busy.",
			recorder.await(event -> updates(event, changed), _interval + TOLERANCE_MILLIS));
	}

	private static boolean updates(ModelChangeEvent event, TLObject object) {
		return event.getUpdated().anyMatch(object::equals);
	}

	/**
	 * Records the {@link ModelChangeEvent}s delivered to it, which arrive on another thread.
	 */
	private static final class Recorder implements ModelListener {

		private final List<ModelChangeEvent> _events = new ArrayList<>();

		@Override
		public synchronized void notifyChange(ModelChangeEvent change) {
			_events.add(change);
			notifyAll();
		}

		/** A copy of the events delivered so far. */
		public synchronized List<ModelChangeEvent> events() {
			return new ArrayList<>(_events);
		}

		/**
		 * Waits until an event matching the given test was delivered.
		 *
		 * @return Whether such an event arrived within the given time.
		 */
		public synchronized boolean await(Predicate<ModelChangeEvent> test, long timeoutMillis)
				throws InterruptedException {
			long deadline = System.currentTimeMillis() + timeoutMillis;
			while (true) {
				if (_events.stream().anyMatch(test)) {
					return true;
				}
				long remaining = deadline - System.currentTimeMillis();
				if (remaining <= 0) {
					return false;
				}
				wait(remaining);
			}
		}
	}

	/**
	 * Registers a {@link Recorder} on the given object in the model scope of the given window, after
	 * the events pending so far have been delivered.
	 */
	private Recorder observe(String window, TLObject object) {
		return observe(window, List.of(object));
	}

	/**
	 * Registers a {@link Recorder} on the given objects in the model scope of the given window,
	 * after the events pending so far have been delivered.
	 */
	private Recorder observe(String window, List<? extends TLObject> objects) {
		Recorder recorder = new Recorder();
		inWindow(window, () -> {
			_registry.getOrCreateModelScope(window);
			_registry.synthesizeModelEvents(window);
			for (TLObject object : objects) {
				_registry.getOrCreateModelScope(window).addModelListener(object, recorder);
			}
		});
		return recorder;
	}

	/**
	 * Creates the given window with its subsession, as rendering its page does, and connects a
	 * client to its queue, as connecting its stream does.
	 *
	 * @return The connected stream, whose body is what the client received.
	 */
	private FakeAsyncRequest displayWindow(String window) {
		TLSessionContext sessionContext = sessionContext(_session);
		SSEUpdateQueue queue = _registry.getOrCreateQueue(window);
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLContext subSession = new TLContext();
		subSession.setSessionContext(sessionContext);
		sessionContext.setIfAbsent(window, subSession);
		subSession.updateSessionRevision(kb.getHistoryManager(), ((UpdateChainView) kb.getUpdateChain()).current());
		queue.setWindowContext(window, sessionContext, _registry);
		FakeAsyncRequest stream = new FakeAsyncRequest();
		queue.setConnection(stream.context());
		return stream;
	}

	private void inWindow(String window, Runnable action) {
		TLSubSessionContext subSession = sessionContext(_session).getSubSession(window);
		ThreadContextManager.inContext(subSession, action::run);
	}

	private void closeWindow(String window) {
		TLSubSessionContext subSession = sessionContext(_session).getSubSession(window);
		if (subSession != null) {
			ThreadContextManager.inContext(subSession, () -> _registry.windowClosed(window));
		} else {
			_registry.windowClosed(window);
		}
	}

	/**
	 * Runs the given action in a system context of a thread of its own, waits for it to complete,
	 * and rethrows what it failed with.
	 */
	private static void inOtherThread(Runnable action) throws Exception {
		AtomicReference<Throwable> problem = new AtomicReference<>();
		Thread thread = new Thread(() -> {
			try {
				ThreadContext.inSystemContext(TestWindowModelEventService.class, action::run);
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
		return ThreadContext.inSystemContext(TestWindowModelEventService.class,
			() -> SessionService.getInstance().loginUser(invocation.getRequest(), invocation.getResponse(), user));
	}

	private Person createPerson() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			Person result = Person.create(kb, "windowSyncUser" + (++_personCount),
				PersonManager.getManager().getRoot().getAuthenticationDevice());
			tx.commit();
			synchronized (_created) {
				_created.add(result);
			}
			return result;
		}
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		Test test = ServiceTestSetup.createSetup(new TestSuite(TestWindowModelEventService.class),
			SessionService.Module.INSTANCE, WindowModelEventService.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(test);
	}

}
