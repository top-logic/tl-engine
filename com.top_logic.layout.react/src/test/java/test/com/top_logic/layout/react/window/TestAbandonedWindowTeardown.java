/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.window;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

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
import com.top_logic.knowledge.service.db2.UpdateChainLink;
import com.top_logic.knowledge.service.db2.UpdateChainView;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.window.WindowEntry;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.util.TLContext;

/**
 * Tests that a window whose client went away is torn down after
 * {@link ReactWindowRegistry#DISCONNECT_GRACE_MILLIS}, through the seam its heartbeat uses
 * ({@link SSEUpdateQueue#checkAbandoned(long)}), and that it then keeps no later commit of the
 * knowledge base in memory.
 */
public class TestAbandonedWindowTeardown extends BasicTestCase {

	private static final String WINDOW = "vTab";

	/** Number of garbage collection attempts before a link is considered reachable. */
	private static final int GC_ATTEMPTS = 20;

	/** Pause between two garbage collection attempts in milliseconds. */
	private static final long GC_PAUSE_MS = 100;

	/** Number of commits made after the observed one, so that it is strictly behind the tail. */
	private static final int LATER_COMMITS = 3;

	/** A time span by which the tests move the clock past the grace period. */
	private static final long MARGIN_MILLIS = 5_000;

	private final List<Person> _created = new ArrayList<>();

	private int _personCount;

	/** The session the running test logged in, ended in {@link #tearDown()}. */
	private HttpSession _session;

	/** The window registry of {@link #_session}. */
	private ReactWindowRegistry _registry;

	@Override
	protected void tearDown() throws Exception {
		if (_session != null) {
			endSession(_session, _registry);
			_session = null;
			_registry = null;
		}
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
	 * A window whose stream is connected is kept, however late it is asked.
	 */
	public void testPresentClientIsKept() throws Exception {
		HttpSession session = startSession();
		ReactWindowRegistry registry = _registry;
		SSEUpdateQueue queue = displayWindow(session, registry);
		queue.setConnection(new FakeAsyncRequest().context());

		queue.checkAbandoned(System.currentTimeMillis() + 10 * ReactWindowRegistry.DISCONNECT_GRACE_MILLIS);

		assertNotNull("A window with a client must be kept.", registry.getWindow(WINDOW));
		assertNotNull("The subsession of a kept window must be kept.",
			sessionContext(session).getSubSession(WINDOW));
	}

	/**
	 * A window whose client is away for less than the grace period is kept.
	 */
	public void testClientAwayWithinGraceIsKept() throws Exception {
		HttpSession session = startSession();
		ReactWindowRegistry registry = _registry;
		SSEUpdateQueue queue = displayWindow(session, registry);
		loseClient(queue);

		queue.checkAbandoned(
			System.currentTimeMillis() + ReactWindowRegistry.DISCONNECT_GRACE_MILLIS - MARGIN_MILLIS);

		assertNotNull("A window whose client may still come back must be kept.", registry.getWindow(WINDOW));
	}

	/**
	 * A window whose client is away for longer than the grace period is torn down: its entry and
	 * its subsession are gone, its model scope delivers no more events, and no commit made after the
	 * client went away is kept in memory.
	 */
	public void testAbandonedWindowIsTornDown() throws Exception {
		HttpSession session = startSession();
		ReactWindowRegistry registry = _registry;
		SSEUpdateQueue queue = displayWindow(session, registry);
		List<ModelChangeEvent> events = observe(registry);
		WindowEntry entry = registry.getWindow(WINDOW);
		loseClient(queue);
		WeakReference<UpdateChainLink> link = commitBehindWindow();

		queue.checkAbandoned(System.currentTimeMillis() + ReactWindowRegistry.DISCONNECT_GRACE_MILLIS + MARGIN_MILLIS);

		assertNull("An abandoned window must be torn down.", registry.getWindow(WINDOW));
		assertNull("The subsession of an abandoned window must be removed.",
			sessionContext(session).getSubSession(WINDOW));
		assertTrue("An abandoned window must keep no commit in memory.", isCollected(link));

		createPerson();
		entry.synthesizeModelEvents();
		assertTrue("A torn-down window must not receive model events.", events.isEmpty());
	}

	/**
	 * A window whose client went away keeps every later commit in memory as long as it is not torn
	 * down.
	 */
	public void testWindowNotTornDownKeepsCommits() throws Exception {
		HttpSession session = startSession();
		ReactWindowRegistry registry = _registry;
		SSEUpdateQueue queue = displayWindow(session, registry);
		observe(registry);
		loseClient(queue);
		WeakReference<UpdateChainLink> link = commitBehindWindow();

		assertFalse("A window that is not torn down keeps later commits in memory.", isCollected(link));
	}

	/**
	 * Logs a new user in and remembers the session to end it after the test.
	 */
	private HttpSession startSession() throws Exception {
		_session = login(createPerson());
		_registry = ReactWindowRegistry.forSession(_session);
		return _session;
	}

	/**
	 * Ends the given session, closing its window first if it is still open.
	 *
	 * <p>
	 * The test container does not report the end of a session to its attributes, so a window left
	 * open would keep its cursors on the update chain and every commit of the following tests with
	 * them.
	 * </p>
	 */
	private static void endSession(HttpSession session, ReactWindowRegistry registry) {
		TLSubSessionContext subSession = sessionContext(session).getSubSession(WINDOW);
		if (subSession != null) {
			ThreadContextManager.inContext(subSession, () -> registry.windowClosed(WINDOW));
		} else {
			registry.windowClosed(WINDOW);
		}
		SessionService.getInstance().terminateSession(session.getId());
	}

	/**
	 * Creates the window {@link #WINDOW} with its subsession, as rendering its page does, and
	 * connects its queue to the given session's registry, as connecting its stream does.
	 */
	private static SSEUpdateQueue displayWindow(HttpSession session, ReactWindowRegistry registry) {
		TLSessionContext sessionContext = sessionContext(session);
		SSEUpdateQueue queue = registry.getOrCreateQueue(WINDOW);
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLContext subSession = new TLContext();
		subSession.setSessionContext(sessionContext);
		sessionContext.setIfAbsent(WINDOW, subSession);
		subSession.updateSessionRevision(kb.getHistoryManager(), ((UpdateChainView) kb.getUpdateChain()).current());
		queue.setWindowContext(WINDOW, sessionContext, registry);
		return queue;
	}

	/**
	 * Connects a stream to the given queue and ends it, as a client that goes away does.
	 */
	private static void loseClient(SSEUpdateQueue queue) {
		FakeAsyncRequest stream = new FakeAsyncRequest();
		queue.setConnection(stream.context());
		queue.clearConnection(stream.context());
	}

	/**
	 * Commits, then commits further, so that the {@link UpdateChainLink} of the first commit is
	 * behind the tail of the update chain and behind the session revision of the test.
	 *
	 * @return A weak reference to the {@link UpdateChainLink} of the first commit.
	 */
	private WeakReference<UpdateChainLink> commitBehindWindow() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		createPerson();
		WeakReference<UpdateChainLink> result =
			new WeakReference<>(((UpdateChainView) kb.getUpdateChain()).current());
		for (int n = 0; n < LATER_COMMITS; n++) {
			createPerson();
		}
		kb.getHistoryManager().updateSessionRevision();
		return result;
	}

	/**
	 * Runs the garbage collector until the given reference is cleared or the attempts are
	 * exhausted.
	 */
	private static boolean isCollected(WeakReference<?> ref) throws InterruptedException {
		for (int n = 0; n < GC_ATTEMPTS; n++) {
			System.gc();
			if (ref.get() == null) {
				return true;
			}
			Thread.sleep(GC_PAUSE_MS);
		}
		return ref.get() == null;
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

	private static TLSessionContext sessionContext(HttpSession session) {
		return SessionService.getInstance().getSession(session);
	}

	private static HttpSession login(Person user) throws Exception {
		ServletRunner runner = new ServletRunner();
		runner.registerServlet("lpServlet", TLLayoutServlet.class.getName());
		InvocationContext invocation =
			runner.newClient().newInvocation(new PostMethodWebRequest("http://ignore/lpServlet"));
		// Keep the session context of the test thread.
		return ThreadContext.inSystemContext(TestAbandonedWindowTeardown.class,
			() -> SessionService.getInstance().loginUser(invocation.getRequest(), invocation.getResponse(), user));
	}

	private Person createPerson() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			Person result = Person.create(kb, "abandonedWindowUser" + (++_personCount),
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
		Test test = ServiceTestSetup.createSetup(new TestSuite(TestAbandonedWindowTeardown.class),
			SessionService.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(test);
	}

}
