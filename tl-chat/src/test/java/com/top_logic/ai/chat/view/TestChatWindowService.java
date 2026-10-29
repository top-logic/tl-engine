/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatSessionManager;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.scripting.WindowAccess;
import com.top_logic.layout.react.window.Interaction;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.RunWindow;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * What {@link ChatWindowService} does with the browser windows of the chat: the {@link RunWindow}
 * of a run, a tool's write with a bounded wait, and dropping a deleted chat out of the windows of
 * its owner.
 */
public class TestChatWindowService extends BasicTestCase {

	/** How long a case waits for a helper thread, in milliseconds. */
	private static final long TIMEOUT = 20_000;

	private static Person owner;

	private static Person other;

	private final List<LiveWindow> _windows = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (owner == null) {
			owner = TestPerson.createPerson("chat-window-owner");
			other = TestPerson.createPerson("chat-window-other");
		}
	}

	@Override
	protected void tearDown() throws Exception {
		for (LiveWindow window : _windows) {
			window.close();
		}
		_windows.clear();
		super.tearDown();
	}

	/**
	 * The {@link RunWindow} a run asks reaches the window of its sub-session from a thread with no
	 * context of its own, and runs the action in that sub-session.
	 */
	public void testTheRunWindowReachesTheWindow() throws Exception {
		LiveWindow window = open(owner, "runWindow");
		AtomicReference<TLSubSessionContext> seen = new AtomicReference<>();

		boolean reached = inOtherThread(() -> RunWindow.get().inWindow(window.subSession(),
			root -> seen.set(TLContextManager.getSubSession())));

		assertTrue(reached);
		assertSame("The action runs in the window's sub-session.", window.subSession(), seen.get());
	}

	/** A sub-session no window displays - a headless or scheduled run - reaches nothing. */
	public void testARunWithoutAWindowReachesNothing() {
		AtomicBoolean ran = new AtomicBoolean();

		assertFalse(RunWindow.get().inWindow(TLContextManager.getSubSession(), root -> ran.set(true)));
		assertFalse(RunWindow.get().inWindow(null, root -> ran.set(true)));
		assertFalse(ran.get());
		assertFalse(ChatWindowService.hasWindow(TLContextManager.getSubSession()));
	}

	/** A write from a thread with no context of its own runs in the window's sub-session. */
	public void testAWriteRunsInTheWindowsSubSession() throws Exception {
		LiveWindow window = open(owner, "writeWindow");
		AtomicReference<TLSubSessionContext> seen = new AtomicReference<>();

		assertTrue(ChatWindowService.hasWindow(window.subSession()));
		WindowAccess access = inOtherThread(() -> ChatWindowService.inWindow(window.subSession(),
			() -> seen.set(TLContextManager.getSubSession())));

		assertEquals(WindowAccess.DONE, access);
		assertSame("The write runs in the window's sub-session.", window.subSession(), seen.get());
	}

	/**
	 * A window another thread keeps busy for longer than a write waits is reported as busy, and the
	 * write does not run: the caller tells its model that nothing was written.
	 */
	public void testABusyWindowSkipsTheWrite() throws Exception {
		LiveWindow window = open(owner, "busyWindow");
		CountDownLatch holding = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		Thread command = new Thread(() -> {
			try (Interaction interaction = window.registry().beginInteraction()) {
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
			access = ChatWindowService.inWindow(window.subSession(), runs::incrementAndGet);
		} finally {
			release.countDown();
			command.join(TIMEOUT);
		}

		assertEquals(WindowAccess.BUSY, access);
		assertEquals(0, runs.get());
	}

	/** What a write throws reaches the caller. */
	public void testAFailingWriteReachesTheCaller() throws Exception {
		LiveWindow window = open(owner, "failingWindow");
		try {
			ChatWindowService.inWindow(window.subSession(), () -> {
				throw new IllegalStateException("broken channel");
			});
			fail("The failure was swallowed.");
		} catch (IllegalStateException ex) {
			assertEquals("broken channel", ex.getMessage());
		}
	}

	/**
	 * A deleted chat is let go of in every window of its owner, and only there: a window of another
	 * account that holds a chat of the same id keeps it.
	 */
	public void testADeletedChatLeavesTheWindowsOfItsOwnerOnly() throws Exception {
		TLObject session = ChatHistoryService.createSession("Wird gelöscht", owner);
		HistoryUtils.updateSessionRevision();
		String id = session.tHandle().getObjectName().toExternalForm();
		ChatSessionManager ownersPage = showing(open(owner, "ownersPage"), "page", id);
		ChatSessionManager ownersDock = showing(open(owner, "ownersDock"), "dock", id);
		ChatSessionManager othersPage = showing(open(other, "othersPage"), "page", id);

		ChatHistoryService.deleteSession(session);

		assertNull("The owner's page still holds the deleted chat.", ownersPage.getActiveSessionId());
		assertNull("The owner's dock still holds the deleted chat.", ownersDock.getActiveSessionId());
		assertEquals("A window of another account lost its chat.", id, othersPage.getActiveSessionId());
	}

	// --- Fixture ---

	private LiveWindow open(Person account, String windowName) throws Exception {
		LiveWindow window = LiveWindow.open(account, windowName, null);
		_windows.add(window);
		return window;
	}

	/**
	 * Registers a conversation in the given window that has the given chat open, as a rendered chat
	 * element does.
	 */
	private static ChatSessionManager showing(LiveWindow window, String conversation, String sessionId) {
		TaskProgressTracker tracker = new TaskProgressTracker();
		ChatSessionRegistry.register(window.subSession(), conversation, tracker);
		ChatSessionManager manager = tracker.getSessionManager();
		assertTrue("The test needs the chat to be open.", manager.switchSession(sessionId, owner));
		return manager;
	}

	/**
	 * Computes a result in a system context of a thread of its own, as an agent thread does, and
	 * rethrows what the computation failed with.
	 */
	private static <T> T inOtherThread(Supplier<T> computation) throws Exception {
		AtomicReference<T> result = new AtomicReference<>();
		AtomicReference<Throwable> problem = new AtomicReference<>();
		Thread thread = new Thread(() -> {
			try {
				ThreadContext.inSystemContext(TestChatWindowService.class, () -> result.set(computation.get()));
			} catch (Throwable ex) {
				problem.set(ex);
			}
		}, "agent");
		thread.start();
		thread.join(TIMEOUT);
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

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatWindowService.class),
				ModelService.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ChatWindowService.Module.INSTANCE));
	}
}
