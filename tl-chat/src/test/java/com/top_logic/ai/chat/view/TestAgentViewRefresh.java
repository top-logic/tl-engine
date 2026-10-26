/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;
import junit.framework.TestSuite;

import jakarta.servlet.http.HttpSession;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.top_logic.ai.chat.view.AgentViewRefreshService.WindowAccess;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.knowledge.gui.layout.TLLayoutServlet;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.protocol.PatchEvent;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.Interaction;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.tl.ai.core.agent.RunWindow;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.MemoryKind;
import com.top_logic.tl.ai.core.execution.model.TlAiExecutionFactory;
import com.top_logic.tl.ai.core.execution.model.UserMemory;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;
import com.top_logic.basic.thread.ThreadContext;

/**
 * That a row an agent writes reaches the open window, and keeps reaching it while the agent writes
 * on.
 *
 * <p>
 * The chat's rail sections are ordinary {@code <data-list>}s, and a list follows the model by
 * listening on its window's {@link ModelScope} for the types it observes. Nothing pushes into that
 * scope by itself when a background thread commits - {@link AgentViewRefreshService} is what does,
 * and these cases drive the real path: a window registered the way a rendering chat registers one,
 * a listener standing in for the list, and a real {@link AgentExecutionStore#createMemory} commit
 * on the store's writer thread.
 * </p>
 *
 * @see PendingRefreshes
 */
public class TestAgentViewRefresh extends BasicTestCase {

	/** How long a case waits for the refresh to arrive, in milliseconds. */
	private static final long TIMEOUT = 20_000;

	/** The window this test's sub-session shows. */
	private static final String WINDOW = "refreshWindow";

	private static Person user;

	/**
	 * What a list re-evaluating on the window's notification would find.
	 *
	 * <p>
	 * Collected <em>in</em> the listener, which is where a {@code <data-list>} runs its rows query:
	 * on the refresh's thread, in the window's sub-session, at the revision the refresh has just
	 * advanced it to. Asking from the test thread would ask a view of the database fixed before the
	 * writer thread committed (#405) and would say nothing about what the window sees.
	 * </p>
	 */
	private final List<String> _seenByTheList = new CopyOnWriteArrayList<>();

	private HttpSession _session;

	private ReactWindowRegistry _registry;

	private ModelScope _scope;

	private ModelListener _listener;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (user == null) {
			user = TestPerson.createPerson("refreshee");
		}
		// The run's account has to be the account the window is logged in as - that is what decides
		// whether a change reaches it, and it is the whole point of the register.
		subSession().setPerson(user);
		// A logged-in session: the engine delivers model events only to the windows of a session
		// that has a live user (since 8.0.0-alpha10).
		_session = login(user);
		_registry = ReactWindowRegistry.forSession(_session);
		_scope = _registry.getOrCreateModelScope(WINDOW);
		_listener = event -> {
			for (UserMemory memory : memories()) {
				_seenByTheList.add(memory.getText());
			}
		};
		_scope.addModelListener(TlAiExecutionFactory.getUserMemoryType(), _listener);
		// The sub-session a browser window has: the one its session holds under the window's name.
		subSession().getSessionContext().setIfAbsent(WINDOW, subSession());
		AgentViewRefreshService.registerWindow(subSession(), WINDOW, _registry);
	}

	@Override
	protected void tearDown() throws Exception {
		AgentViewRefreshService.duringPass = () -> {
			// The production no-op; a case that replaced it must not leak it into the next one.
		};
		_scope.removeModelListener(TlAiExecutionFactory.getUserMemoryType(), _listener);
		SessionService.getInstance().terminateSession(_session.getId());
		super.tearDown();
	}

	/**
	 * A view rendered for no browser window - the check of a view that holds a chat - does not
	 * replace the window the sub-session shows, so a run still reaches the user's page.
	 */
	public void testAHeadlessRenderDoesNotReplaceTheWindow() {
		AgentViewRefreshService.registerWindow(subSession(), "view-check", new ReactWindowRegistry("view-check"));

		assertTrue(RunWindow.get().inWindow(subSession(), root -> {
			// Only whether the window is reached.
		}));
	}

	/**
	 * A write from a thread with no context of its own runs in the window's sub-session.
	 */
	public void testAWriteRunsInTheWindowsSubSession() throws Exception {
		TLSubSessionContext window = subSession();
		AtomicReference<TLSubSessionContext> seen = new AtomicReference<>();
		AtomicReference<WindowAccess> access = new AtomicReference<>();

		Thread agent = new Thread(
			() -> access.set(AgentViewRefreshService.inWindow(window, () -> seen.set(TLContextManager.getSubSession()))),
			"agent");
		agent.start();
		agent.join(TIMEOUT);

		assertEquals(WindowAccess.DONE, access.get());
		assertSame("The write runs in the window's sub-session.", window, seen.get());
	}

	/**
	 * A window another thread keeps busy for longer than a write waits is reported as busy, and the
	 * write does not run: the caller tells its model that nothing was written.
	 */
	public void testABusyWindowSkipsTheWrite() throws Exception {
		CountDownLatch holding = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		Thread command = new Thread(() -> {
			try (Interaction interaction = _registry.beginInteraction()) {
				holding.countDown();
				release.await(TIMEOUT, TimeUnit.MILLISECONDS);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}, "browser-command");
		command.start();
		AtomicInteger runs = new AtomicInteger();
		WindowAccess access;
		try {
			assertTrue(holding.await(TIMEOUT, TimeUnit.MILLISECONDS));
			access = AgentViewRefreshService.inWindow(subSession(), runs::incrementAndGet);
		} finally {
			release.countDown();
			command.join(TIMEOUT);
		}

		assertEquals(WindowAccess.BUSY, access);
		assertEquals(0, runs.get());
	}

	/**
	 * A caller that already holds the window's interaction - an agent run synchronously from a
	 * button, on the request thread - writes within it at once instead of waiting for itself.
	 */
	public void testTheHolderOfTheInteractionWritesAtOnce() {
		AtomicInteger runs = new AtomicInteger();

		WindowAccess access;
		long start = System.currentTimeMillis();
		try (Interaction command = _registry.beginInteraction()) {
			access = AgentViewRefreshService.inWindow(subSession(), runs::incrementAndGet);
		}
		long elapsed = System.currentTimeMillis() - start;

		assertEquals(WindowAccess.DONE, access);
		assertEquals(1, runs.get());
		assertTrue("The write waited for its own interaction: " + elapsed + " ms", elapsed < 2_000);
	}

	/** What a write throws reaches the caller. */
	public void testAFailingWriteReachesTheCaller() {
		try {
			AgentViewRefreshService.inWindow(subSession(), () -> {
				throw new IllegalStateException("broken channel");
			});
			fail("The failure was swallowed.");
		} catch (IllegalStateException ex) {
			assertEquals("broken channel", ex.getMessage());
		}
	}

	/**
	 * The plain case: a commit on the store's writer thread reaches the window without anybody
	 * touching the browser, and the query the list runs sees the row at the window's revision.
	 */
	public void testACommitReachesTheOpenWindow() {
		String text = "Prefers short answers.";

		remember(text);

		assertTrue("The window was never told about the new row.", awaitNotification());
		assertTrue("The list the window renders does not see the row the run wrote: " + _seenByTheList,
			_seenByTheList.contains(text));
	}

	/**
	 * A change that arrives <em>while</em> a refresh is running is refreshed too.
	 *
	 * <p>
	 * This is the case the chat actually lives in: a run writes throughout, and a pass takes as
	 * long as it takes to enter every registered window and wait for its interaction. Before
	 * {@link PendingRefreshes} the pending set was cleared before the work and no second pass was
	 * armed while the first one was running, so a write landing in that gap was dropped - the row
	 * then stood still until an unrelated browser command or the 30 s heartbeat happened to
	 * synthesize the window's model events (#497).
	 * </p>
	 */
	public void testAChangeDuringARefreshIsNotLost() {
		String text = "Works in accounting.";
		List<String> writtenDuringThePass = new CopyOnWriteArrayList<>();
		AgentViewRefreshService.duringPass = () -> {
			if (writtenDuringThePass.isEmpty()) {
				writtenDuringThePass.add(text);
				remember(text);
			}
		};

		// Any change arms the pass; what the pass then writes is the change under test.
		AgentViewRefreshService.getInstance().onAgentDataChanged(user);

		assertTrue("The window was never told about the row written during the refresh.",
			awaitNotification());
		assertEquals("The fixture did not write during the pass.", 1, writtenDuringThePass.size());
		assertTrue("The list the window renders does not see the row written during the refresh: "
			+ _seenByTheList, _seenByTheList.contains(text));
	}

	/**
	 * A window whose browser takes nothing from its event stream is not refreshed, and is refreshed
	 * again once its browser has taken what waited.
	 *
	 * <p>
	 * A browser that dies without reporting that its page is gone leaves its window behind until the
	 * session ends, and the window's queue keeps every event until a stream connects again. Each
	 * refresh made the window's lists send their rows again, so such a window grew by the rows of
	 * every list it showed for every change a run made - 930 MB in eleven windows of a ten-hour run,
	 * until the JVM ran out of memory (Pietro #738).
	 * </p>
	 */
	public void testAWindowWithUndeliveredEventsIsNotRefreshed() throws InterruptedException {
		SSEUpdateQueue queue = _registry.getQueue(WINDOW);
		// What an earlier refresh sent the window, and no browser took.
		queue.enqueue(PatchEvent.create().setControlId("rows").setPatch("{}"));
		CountDownLatch passes = new CountDownLatch(1);
		AgentViewRefreshService.duringPass = passes::countDown;

		remember("Answers in German.");

		assertTrue("No refresh pass ran.", passes.await(TIMEOUT, TimeUnit.MILLISECONDS));
		assertTrue("A window nobody reads was refreshed: " + _seenByTheList, _seenByTheList.isEmpty());

		// The page is rendered again: what waited is replaced by the full state.
		queue.discardPendingEvents();
		String text = "Reads the summary first.";
		remember(text);

		assertTrue("The window was not refreshed again once it delivered what waited.", awaitNotification());
		assertTrue("The list the window renders does not see the row the run wrote: " + _seenByTheList,
			_seenByTheList.contains(text));
	}

	// --- Fixture ---

	private UserMemory remember(String text) {
		return AgentExecutionStore.getInstance().createMemory(user, MemoryKind.PREFERENCE, text, null);
	}

	private List<UserMemory> memories() {
		return AgentExecutionStore.getInstance().memoriesOf(user, 0);
	}

	/** Waits for the window's scope to deliver a notification, as a rendered list would receive it. */
	private boolean awaitNotification() {
		long deadline = System.currentTimeMillis() + TIMEOUT;
		while (System.currentTimeMillis() < deadline) {
			if (!_seenByTheList.isEmpty()) {
				return true;
			}
			try {
				Thread.sleep(25);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				return false;
			}
		}
		return false;
	}

	/** Logs the given account in, as the login page does, and keeps the test thread's context. */
	private static HttpSession login(Person account) throws Exception {
		ServletRunner runner = new ServletRunner();
		runner.registerServlet("lpServlet", TLLayoutServlet.class.getName());
		InvocationContext invocation =
			runner.newClient().newInvocation(new PostMethodWebRequest("http://ignore/lpServlet"));
		return ThreadContext.inSystemContext(TestAgentViewRefresh.class,
			() -> SessionService.getInstance().loginUser(invocation.getRequest(), invocation.getResponse(), account));
	}

	private static TLSubSessionContext subSession() {
		return TLContextManager.getSubSession();
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestAgentViewRefresh.class),
				ModelService.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				SchedulerService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				AgentViewRefreshService.Module.INSTANCE));
	}
}
