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
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.scripting.ReactWindowReplay;
import com.top_logic.layout.react.scripting.WindowAccess;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.Interaction;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.model.TLObject;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;

/**
 * Tests reaching the browser window of a sub-session from another thread, see
 * {@link ReactWindowReplay#inWindow(TLSubSessionContext, long, java.util.function.Consumer)} and
 * {@link ReactWindowRegistry#windowSubSessions()}.
 */
public class TestWindowOfSubSession extends BasicTestCase {

	private static final String WINDOW = "vSubSessionWindow";

	private static final String OTHER_WINDOW = "vSubSessionOther";

	/** Time in milliseconds a helper thread is given to do its part. */
	private static final long THREAD_TIMEOUT_MILLIS = 10_000;

	private final List<Person> _created = new ArrayList<>();

	private int _personCount;

	private HttpSession _session;

	private ReactWindowRegistry _registry;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_session = login(createPerson());
		_registry = ReactWindowRegistry.forSession(_session);
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
	 * The action runs in the window of a live sub-session: in that sub-session, with the window's
	 * root control, and with the window's model events synthesized before and after it.
	 */
	public void testActionRunsInLiveWindow() throws Exception {
		TLSubSessionContext subSession = displayWindow(WINDOW);
		ReactControl root = new ReactControl(
			new DefaultReactContext("", WINDOW, _registry.getQueue(WINDOW), _registry), null, "TLPanel");
		_registry.getQueue(WINDOW).setRootControl(root);

		Person before = createPerson();
		Person after = createPerson();
		Recorder recorder = observe(WINDOW, List.of(before, after));
		inOtherThread(() -> changeLanguage(before));

		AtomicReference<ReactControl> received = new AtomicReference<>();
		AtomicReference<TLSubSessionContext> installed = new AtomicReference<>();
		List<ModelChangeEvent> seenBefore = new ArrayList<>();
		WindowAccess access = inOtherThreadResult(() -> ReactWindowReplay.inWindow(subSession, 1_000, control -> {
			received.set(control);
			installed.set(TLContextManager.getSubSession());
			seenBefore.addAll(recorder.events());
			try {
				inOtherThread(() -> changeLanguage(after));
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}));

		assertEquals(WindowAccess.DONE, access);
		assertSame("The action must receive the window's root control.", root, received.get());
		assertSame("The action must run in the window's sub-session.", subSession, installed.get());
		assertTrue("The commit made before the action must be delivered before it runs.",
			seenBefore.stream().anyMatch(event -> updates(event, before)));
		assertTrue("The commit made during the action must be delivered after it.",
			recorder.events().stream().anyMatch(event -> updates(event, after)));
	}

	/**
	 * The caller's own context is restored after the action.
	 */
	public void testCallerContextRestored() throws Exception {
		TLSubSessionContext subSession = displayWindow(WINDOW);
		TLSubSessionContext otherSubSession = displayWindow(OTHER_WINDOW);
		ThreadContextManager.inContext(otherSubSession, () -> {
			assertEquals(WindowAccess.DONE, ReactWindowReplay.inWindow(subSession, 1_000,
				control -> assertSame(subSession, TLContextManager.getSubSession())));
			assertSame(otherSubSession, TLContextManager.getSubSession());
		});
	}

	/**
	 * A sub-session no window displays reaches no window.
	 */
	public void testForeignSubSessionHasNoWindow() throws Exception {
		displayWindow(WINDOW);
		TLContext foreign = new TLContext();
		foreign.setSessionContext(sessionContext(_session));

		assertEquals(WindowAccess.NO_WINDOW, ReactWindowReplay.inWindow(foreign, 1_000, control -> fail()));
		assertEquals(WindowAccess.NO_WINDOW, ReactWindowReplay.inWindow(null, 1_000, control -> fail()));
		assertNull(ReactWindowRegistry.registryDisplaying(foreign));
	}

	/**
	 * A window whose session interaction is held by another thread for longer than the timeout is
	 * busy; without a timeout the action waits for the interaction to end.
	 */
	public void testBusyWindow() throws Exception {
		TLSubSessionContext subSession = displayWindow(WINDOW);

		CountDownLatch held = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		Thread holder = new Thread(() -> {
			try (Interaction interaction = _registry.beginInteraction()) {
				held.countDown();
				release.await(THREAD_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		});
		holder.start();
		try {
			assertTrue(held.await(THREAD_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS));
			assertEquals(WindowAccess.BUSY, ReactWindowReplay.inWindow(subSession, 100, control -> fail()));

			Thread releaser = new Thread(() -> {
				try {
					Thread.sleep(200);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
				release.countDown();
			});
			releaser.start();
			boolean[] ran = { false };
			assertEquals(WindowAccess.DONE, ReactWindowReplay.inWindow(subSession, control -> ran[0] = true));
			assertTrue("Without a timeout the action must wait for the interaction.", ran[0]);
			releaser.join(THREAD_TIMEOUT_MILLIS);
		} finally {
			release.countDown();
			holder.join(THREAD_TIMEOUT_MILLIS);
		}
	}

	/**
	 * A closed window is not reached anymore.
	 */
	public void testClosedWindowHasNoWindow() throws Exception {
		TLSubSessionContext subSession = displayWindow(WINDOW);
		closeWindow(WINDOW);

		assertEquals(WindowAccess.NO_WINDOW, ReactWindowReplay.inWindow(subSession, 1_000, control -> fail()));
	}

	/**
	 * The enumeration lists the sub-sessions of the live windows only.
	 */
	public void testEnumeratesLiveWindows() throws Exception {
		TLSubSessionContext subSession = displayWindow(WINDOW);
		TLSubSessionContext otherSubSession = displayWindow(OTHER_WINDOW);
		List<TLSubSessionContext> all = ReactWindowRegistry.windowSubSessions();
		assertTrue(containsSame(all, subSession));
		assertTrue(containsSame(all, otherSubSession));

		closeWindow(OTHER_WINDOW);
		List<TLSubSessionContext> remaining = ReactWindowRegistry.windowSubSessions();
		assertTrue(containsSame(remaining, subSession));
		assertFalse(containsSame(remaining, otherSubSession));

		assertSame(_registry, ReactWindowRegistry.registryDisplaying(subSession));
		assertEquals(WINDOW, _registry.windowDisplaying(subSession));
	}

	private static boolean containsSame(List<?> list, Object element) {
		return list.stream().anyMatch(candidate -> candidate == element);
	}

	private static boolean updates(ModelChangeEvent event, TLObject object) {
		return event.getUpdated().anyMatch(object::equals);
	}

	/**
	 * Records the {@link ModelChangeEvent}s delivered to it.
	 */
	private static final class Recorder implements ModelListener {

		private final List<ModelChangeEvent> _events = new ArrayList<>();

		@Override
		public synchronized void notifyChange(ModelChangeEvent change) {
			_events.add(change);
		}

		/** A copy of the events delivered so far. */
		public synchronized List<ModelChangeEvent> events() {
			return new ArrayList<>(_events);
		}
	}

	/**
	 * Registers a {@link Recorder} on the given objects in the model scope of the given window,
	 * after the events pending so far have been delivered.
	 */
	private Recorder observe(String window, List<? extends TLObject> objects) {
		Recorder recorder = new Recorder();
		TLSubSessionContext subSession = sessionContext(_session).getSubSession(window);
		ThreadContextManager.inContext(subSession, () -> {
			_registry.getOrCreateModelScope(window);
			_registry.synthesizeModelEvents(window);
			for (TLObject object : objects) {
				_registry.getOrCreateModelScope(window).addModelListener(object, recorder);
			}
		});
		return recorder;
	}

	/**
	 * Creates the given window with its sub-session, as rendering its page does.
	 */
	private TLSubSessionContext displayWindow(String window) {
		TLSessionContext sessionContext = sessionContext(_session);
		SSEUpdateQueue queue = _registry.getOrCreateQueue(window);
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLContext subSession = new TLContext();
		subSession.setSessionContext(sessionContext);
		sessionContext.setIfAbsent(window, subSession);
		subSession.updateSessionRevision(kb.getHistoryManager(), ((UpdateChainView) kb.getUpdateChain()).current());
		queue.setWindowContext(window, sessionContext, _registry);
		return subSession;
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
		inOtherThreadResult(() -> {
			action.run();
			return null;
		});
	}

	/**
	 * Computes a result in a system context of a thread of its own, waits for it, and rethrows what
	 * the computation failed with.
	 */
	private static <T> T inOtherThreadResult(java.util.function.Supplier<T> computation) throws Exception {
		AtomicReference<T> result = new AtomicReference<>();
		AtomicReference<Throwable> problem = new AtomicReference<>();
		Thread thread = new Thread(() -> {
			try {
				ThreadContext.inSystemContext(TestWindowOfSubSession.class, () -> result.set(computation.get()));
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
		return result.get();
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
		return ThreadContext.inSystemContext(TestWindowOfSubSession.class,
			() -> SessionService.getInstance().loginUser(invocation.getRequest(), invocation.getResponse(), user));
	}

	private Person createPerson() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			Person result = Person.create(kb, "windowOfSubSessionUser" + (++_personCount),
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
		Test test = ServiceTestSetup.createSetup(new TestSuite(TestWindowOfSubSession.class),
			SessionService.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(test);
	}

}
