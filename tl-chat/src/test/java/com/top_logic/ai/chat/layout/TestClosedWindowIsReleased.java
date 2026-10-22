/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobStatus;
import com.top_logic.tl.ai.core.agent.subagent.SubagentResult;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * A closed window lets go of its chats, although their subagents - kept by the process - work on.
 *
 * <p>
 * The window's waker and the register of its questions listen to the chat's subagent registry, and
 * both hold the window's whole state: its tracker, its cached chats, their logs. Without letting go
 * when the window ends, every window that ever started a subagent stayed in memory as long as the
 * registry did.
 * </p>
 */
public class TestClosedWindowIsReleased extends BasicTestCase {

	private static Person owner;

	private static String chat;

	private final CountDownLatch _release = new CountDownLatch(1);

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (owner == null) {
			owner = TestPerson.createPerson("closes-her-window");
			chat = ChatHistoryService.createSession("Closed window", owner).tHandle().getObjectName()
				.toExternalForm();
			HistoryUtils.updateSessionRevision();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_release.countDown();
		super.tearDown();
	}

	/** The window ends while its subagent works: the window can be collected, the subagent goes on. */
	public void testAClosedWindowCanBeCollected() throws Exception {
		TaskProgressTracker window = new TaskProgressTracker();
		SubagentJob job = spawnIn(window);
		WeakReference<TaskProgressTracker> ref = new WeakReference<>(window);

		window.close();
		window = null;

		assertTrue("Nothing may hold the closed window's chat any more.", collected(ref));
		assertEquals("Its subagent works on.", SubagentJobStatus.RUNNING, job.status());
	}

	/** The end of the window's sub-session is what closes its chats. */
	public void testTheEndOfTheWindowClosesItsChats() throws Exception {
		TLContext subSession = new TLContext();
		TaskProgressTracker window = new TaskProgressTracker();
		ChatSessionRegistry.register(subSession, "chat", window);
		spawnIn(window);

		subSession.informUnboundListeners();

		assertNull("The closed window listens to no subagent any more.",
			window.getSessionManager().getSubagentWaker(chat));
		window.getActiveSubagentRegistry();
		assertNull("A run of the closed window that ends later does not attach it again.",
			window.getSessionManager().getSubagentWaker(chat));
	}

	/** Spawns a blocking subagent in the window's chat. */
	private SubagentJob spawnIn(TaskProgressTracker tracker) throws InterruptedException {
		assertTrue(tracker.getSessionManager().switchSession(chat, owner));
		SubagentJobRegistry registry = tracker.getActiveSubagentRegistry();
		assertNotNull("The window listens to its chat's subagents.",
			tracker.getSessionManager().getSubagentWaker(chat));
		SubagentJob job = registry.register("worker", "task", new CancellationToken());
		CountDownLatch running = new CountDownLatch(1);
		registry.spawn(job, j -> {
			running.countDown();
			while (!_release.await(20, TimeUnit.MILLISECONDS)) {
				if (j.cancellationToken().isCancelled()) {
					throw new java.util.concurrent.CancellationException("Subagent cancelled.");
				}
			}
			return new SubagentResult("done", TokenUsage.EMPTY, 1L, 0, StopReason.COMPLETE);
		});
		assertTrue(running.await(5, TimeUnit.SECONDS));
		return job;
	}

	private static boolean collected(WeakReference<?> ref) throws InterruptedException {
		for (int i = 0; i < 200 && ref.get() != null; i++) {
			System.gc();
			Thread.sleep(50);
		}
		return ref.get() == null;
	}

	/** */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestClosedWindowIsReleased.class),
				ModelService.Module.INSTANCE,
				SchedulerService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
