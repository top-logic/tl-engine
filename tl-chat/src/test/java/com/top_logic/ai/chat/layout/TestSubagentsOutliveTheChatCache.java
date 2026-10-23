/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.ArrayList;
import java.util.List;
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
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobStatus;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistries;
import com.top_logic.tl.ai.core.agent.subagent.SubagentResult;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.ExecutionRecorder;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.util.model.ModelService;

/**
 * A chat window keeps a handful of conversations in memory and drops the oldest; that used to shut
 * down the dropped conversation's subagent registry and interrupt its running subagents. They now
 * work on, and coming back to the conversation finds them and what they finished.
 */
public class TestSubagentsOutliveTheChatCache extends BasicTestCase {

	private static Person owner;

	private static final List<String> CHATS = new ArrayList<>();

	private final CountDownLatch _release = new CountDownLatch(1);

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (owner == null) {
			owner = TestPerson.createPerson("lena-with-many-chats");
			for (int i = 0; i < 8; i++) {
				TLObject chat = ChatHistoryService.createSession("Chat " + i, owner);
				CHATS.add(chat.tHandle().getObjectName().toExternalForm());
			}
			HistoryUtils.updateSessionRevision();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_release.countDown();
		super.tearDown();
	}

	/** Dropped from the cache while its subagent works: the subagent finishes, and its result waits. */
	public void testADroppedChatsSubagentWorksOn() throws Exception {
		TaskProgressTracker window = new TaskProgressTracker();
		ChatSessionManager chats = window.getSessionManager();
		assertTrue(chats.switchSession(CHATS.get(0), owner));
		SubagentJobRegistry registry = window.getActiveSubagentRegistry();
		SubagentJob job = startBlocking(registry);

		// Five more chats push the first one out of the window's cache.
		for (int i = 1; i <= 5; i++) {
			assertTrue(chats.switchSession(CHATS.get(i), owner));
			window.getActiveSubagentRegistry();
		}
		Thread.sleep(200);
		assertEquals("Still at work after its chat was dropped.", SubagentJobStatus.RUNNING, job.status());

		_release.countDown();
		awaitTerminal(job);
		assertEquals(SubagentJobStatus.DONE, job.status());

		assertTrue(chats.switchSession(CHATS.get(0), owner));
		SubagentJobRegistry again = window.getActiveSubagentRegistry();
		assertSame("Coming back finds the same subagents.", registry, again);
		List<SubagentJob> results = again.takeUndeliveredResults();
		assertEquals("Its result waits for the next lead turn.", List.of(job), results);
	}

	/** A deleted chat's subagents are stopped: there is no conversation left to report to. */
	public void testADeletedChatsSubagentsAreStopped() throws Exception {
		AgentSession chat = newChat("Wird gelöscht");
		TaskProgressTracker window = new TaskProgressTracker();
		assertTrue(window.getSessionManager().switchSession(idOf(chat), owner));
		SubagentJob job = startBlocking(window.getActiveSubagentRegistry());

		ChatHistoryService.deleteSession(chat);

		awaitTerminal(job);
		assertEquals(SubagentJobStatus.CANCELLED, job.status());
	}

	/**
	 * Deleting a chat stops its subagents wherever they run: in a window that dropped the chat from
	 * its cache, in another window, and in a run that has no window at all.
	 */
	public void testADeletedChatsSubagentsAreStoppedEverywhere() throws Exception {
		AgentSession chat = newChat("Überall gelöscht");
		String id = idOf(chat);

		TaskProgressTracker first = new TaskProgressTracker();
		assertTrue(first.getSessionManager().switchSession(id, owner));
		SubagentJob dropped = startBlocking(first.getActiveSubagentRegistry());
		// Five more chats push it out of the first window's cache.
		for (int i = 0; i < 5; i++) {
			assertTrue(first.getSessionManager().switchSession(CHATS.get(i), owner));
		}

		TaskProgressTracker second = new TaskProgressTracker();
		assertTrue(second.getSessionManager().switchSession(id, owner));
		SubagentJob otherWindow = startBlocking(second.getActiveSubagentRegistry());

		String headlessKey = SubagentRegistries.conversationKey(ExecutionRecorder.conversationIdOf(chat));
		SubagentJob headless = startBlocking(SubagentRegistries.forKey(headlessKey));

		ChatHistoryService.deleteSession(chat);

		for (SubagentJob job : List.of(dropped, otherWindow, headless)) {
			awaitTerminal(job);
			assertEquals(job.subagentId(), SubagentJobStatus.CANCELLED, job.status());
		}
		assertTrue("Nothing is kept for the deleted chat.", SubagentRegistries.ofConversation(id).isEmpty());
		assertNull(SubagentRegistries.find(headlessKey));
	}

	/** Each window keeps its own conversation, so it keeps its own subagents too. */
	public void testTwoWindowsKeepTheirOwnSubagents() throws Exception {
		TaskProgressTracker first = new TaskProgressTracker();
		TaskProgressTracker second = new TaskProgressTracker();
		assertTrue(first.getSessionManager().switchSession(CHATS.get(6), owner));
		assertTrue(second.getSessionManager().switchSession(CHATS.get(6), owner));

		assertNotSame(first.getActiveSubagentRegistry(), second.getActiveSubagentRegistry());
	}

	private static AgentSession newChat(String title) {
		AgentSession chat = (AgentSession) ChatHistoryService.createSession(title, owner);
		HistoryUtils.updateSessionRevision();
		return chat;
	}

	private static String idOf(TLObject chat) {
		return chat.tHandle().getObjectName().toExternalForm();
	}

	private SubagentJob startBlocking(SubagentJobRegistry registry) throws InterruptedException {
		SubagentJob job = registry.register("worker", "task", new CancellationToken());
		CountDownLatch running = new CountDownLatch(1);
		registry.spawn(job, j -> {
			running.countDown();
			while (!_release.await(20, TimeUnit.MILLISECONDS)) {
				if (j.cancellationToken().isCancelled()) {
					throw new java.util.concurrent.CancellationException("Subagent cancelled.");
				}
			}
			return new SubagentResult("found it", TokenUsage.EMPTY, 1L, 0, StopReason.COMPLETE);
		});
		assertTrue(running.await(5, TimeUnit.SECONDS));
		return job;
	}

	private static void awaitTerminal(SubagentJob job) throws InterruptedException {
		long deadline = System.currentTimeMillis() + 10_000;
		while (!job.status().isTerminal() && System.currentTimeMillis() < deadline) {
			Thread.sleep(10);
		}
		assertTrue("The job must have ended.", job.status().isTerminal());
	}

	/** */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestSubagentsOutliveTheChatCache.class),
				ModelService.Module.INSTANCE,
				SchedulerService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
