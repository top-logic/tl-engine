/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * What a registry says waits for a lead turn, and the delivery a lead turn started for it opens
 * with.
 */
public class TestSubagentNews extends TestCase {

	private SubagentJobRegistry _registry;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_registry = new SubagentJobRegistry(1);
	}

	@Override
	protected void tearDown() throws Exception {
		_registry.cancelAll();
		_registry.shutdown();
		super.tearDown();
	}

	/** Finished results and open questions are news; a cancelled job and a delivered result are not. */
	public void testWhatCountsAsNews() throws Exception {
		assertTrue(_registry.news().isEmpty());

		SubagentJob done = _registry.register("worker", "a", new CancellationToken());
		done.markTerminal(SubagentJobStatus.DONE, result("found"));
		SubagentJob failed = _registry.register("worker", "b", new CancellationToken());
		failed.markTerminal(SubagentJobStatus.FAILED, result("ERROR: broke"));
		SubagentJob cancelled = _registry.register("worker", "c", new CancellationToken());
		cancelled.markTerminal(SubagentJobStatus.CANCELLED, result("stopped"));
		SubagentJob asking = _registry.register("worker", "d", new CancellationToken());
		asking.markRunning();
		asking.askLead("Which year?");

		assertEquals(new SubagentJobRegistry.News(2, 1), _registry.news());

		_registry.takeUndeliveredResults();
		_registry.takeUndeliveredQuestions();
		assertTrue("Delivered is not news any more.", _registry.news().isEmpty());
	}

	/** An answered question is not news. */
	public void testAnAnsweredQuestionIsNoNews() {
		SubagentJob asking = _registry.register("worker", "d", new CancellationToken());
		asking.markRunning();
		asking.askLead("Which year?");
		asking.answerLead("2026");

		assertTrue(_registry.news().isEmpty());
	}

	/**
	 * A lead turn started for news opens with it as one message; with nothing to deliver, nothing is
	 * added and the caller is told so.
	 */
	public void testTheDeliveryALeadTurnOpensWith() {
		Conversation conversation = new Conversation();
		try (SubagentRegistryBinding.Binding bound = SubagentRegistryBinding.bind(_registry)) {
			assertFalse(AgentLoop.injectSubagentResults(conversation, null));
			assertTrue(conversation.messages().isEmpty());

			SubagentJob done = _registry.register("worker", "a", new CancellationToken());
			done.markTerminal(SubagentJobStatus.DONE, result("found it"));
			assertTrue(AgentLoop.injectSubagentResults(conversation, null));
		}
		assertEquals(1, conversation.messages().size());
		UserMessage delivery = (UserMessage) conversation.messages().get(0);
		String text = ((TextContent) delivery.content().get(0)).text();
		assertTrue(text, text.startsWith(SubagentResultDelivery.NOTICE));
		assertTrue(text, text.contains("found it"));
	}

	/** A spawn while the registry is at its cap is predicted to wait in the queue. */
	public void testASpawnAtTheCapWouldQueue() throws Exception {
		assertFalse(_registry.wouldQueue());

		CountDownLatch release = new CountDownLatch(1);
		CountDownLatch running = new CountDownLatch(1);
		SubagentJob busy = _registry.register("worker", "a", new CancellationToken());
		_registry.spawn(busy, job -> {
			running.countDown();
			release.await(10, TimeUnit.SECONDS);
			return result("ok");
		});
		assertTrue(running.await(5, TimeUnit.SECONDS));
		try {
			assertTrue(_registry.wouldQueue());
		} finally {
			release.countDown();
		}
	}

	private static SubagentResult result(String text) {
		return new SubagentResult(text, TokenUsage.EMPTY, 1L, 1, StopReason.COMPLETE);
	}

	/** The registry asks the installation's subagent ceiling, which reads the configuration. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(new TestSuite(TestSubagentNews.class), TypeIndex.Module.INSTANCE));
	}
}
