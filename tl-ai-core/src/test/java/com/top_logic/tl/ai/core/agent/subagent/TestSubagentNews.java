/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.time.Instant;
import java.util.List;
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
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.session.ConversationLogReplay;
import com.top_logic.tl.ai.core.session.Part;
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
			assertTrue(AgentLoop.injectSubagentNews(conversation, null).isEmpty());
			assertTrue(conversation.messages().isEmpty());

			SubagentJob done = _registry.register("worker", "a", new CancellationToken());
			done.markTerminal(SubagentJobStatus.DONE, result("found it"));
			assertFalse(AgentLoop.injectSubagentNews(conversation, null).isEmpty());
		}
		assertEquals(1, conversation.messages().size());
		UserMessage delivery = (UserMessage) conversation.messages().get(0);
		String text = ((TextContent) delivery.content().get(0)).text();
		assertTrue(text, text.startsWith(SubagentResultDelivery.NOTICE));
		assertTrue(text, text.contains("found it"));
	}

	/**
	 * The delivery says who reported back and why - the ended ones in start order, then the asking
	 * ones - so that a chat can name them and point at their cards.
	 */
	public void testTheDeliverySaysWhoReportedBackAndWhy() {
		SubagentJob done = _registry.register("Counter", "a", new CancellationToken());
		done.setParentToolCallId("call-1");
		done.markTerminal(SubagentJobStatus.DONE, result("12"));
		SubagentJob failed = _registry.register("Lister", "b", new CancellationToken());
		failed.setParentToolCallId("call-2");
		failed.markTerminal(SubagentJobStatus.FAILED, result("ERROR: broke"));
		SubagentJob asking = _registry.register("Writer", "c", new CancellationToken());
		asking.setParentToolCallId("call-3");
		asking.markRunning();
		asking.askLead("Für welche Veranstaltung? " + "x".repeat(500));

		List<SubagentNews> news;
		try (SubagentRegistryBinding.Binding bound = SubagentRegistryBinding.bind(_registry)) {
			news = AgentLoop.injectSubagentNews(new Conversation(), null);
		}

		assertEquals(List.of(
			new SubagentNews(done.subagentId(), "Counter", SubagentNews.Reason.FINISHED, "call-1", ""),
			new SubagentNews(failed.subagentId(), "Lister", SubagentNews.Reason.FAILED, "call-2", "")),
			news.subList(0, 2));
		SubagentNews question = news.get(2);
		assertEquals(SubagentNews.Reason.ASKS, question.reason());
		assertEquals("call-3", question.parentToolCallId());
		assertTrue(question.question(), question.question().startsWith("Für welche Veranstaltung?"));
		assertTrue("A line, not the whole question.",
			question.question().length() <= SubagentNews.QUESTION_CHARS + 3);
	}

	/**
	 * A subagent that stopped at a limit did not finish its task: the news says so, rather than that it
	 * is done (#790).
	 */
	public void testASubagentStoppedAtItsLimitIsNotFinished() {
		SubagentJob limited = _registry.register("Counter", "a", new CancellationToken());
		limited.setParentToolCallId("call-1");
		limited.markTerminal(SubagentJobStatus.DONE,
			new SubagentResult("3 of 40 read", TokenUsage.EMPTY, 1L, 2, StopReason.MAX_TURNS));

		List<SubagentNews> news;
		try (SubagentRegistryBinding.Binding bound = SubagentRegistryBinding.bind(_registry)) {
			news = AgentLoop.injectSubagentNews(new Conversation(), null);
		}

		assertEquals(List.of(
			new SubagentNews(limited.subagentId(), "Counter", SubagentNews.Reason.LIMIT, "call-1", "")), news);
		String json = SubagentNews.toJson(news);
		assertTrue(json, json.contains("\"reason\":\"LIMIT\""));
	}

	/** What a lead turn's log keeps of the news: every field of every item, for the chat to read. */
	public void testTheNewsAsJson() {
		List<SubagentNews> news = List.of(
			new SubagentNews("sa-1", "Counter", SubagentNews.Reason.CANCELLED, "call-1", ""),
			new SubagentNews("sa-2", "Writer", SubagentNews.Reason.ASKS, "", "Welche \"Veranstaltung\"?"));

		assertEquals("[{\"subagentId\":\"sa-1\",\"agentName\":\"Counter\",\"reason\":\"CANCELLED\","
			+ "\"parentToolCallId\":\"call-1\",\"question\":\"\"},"
			+ "{\"subagentId\":\"sa-2\",\"agentName\":\"Writer\",\"reason\":\"ASKS\","
			+ "\"parentToolCallId\":\"\",\"question\":\"Welche \\\"Veranstaltung\\\"?\"}]",
			SubagentNews.toJson(news));
	}

	/** The lead's log keeps the news on the turn, as a part the model is never shown again. */
	public void testTheNewsIsRecordedOnTheTurn() {
		ConversationLog log = new ConversationLog();
		ConversationLogListener listener = new ConversationLogListener(log);
		log.startTurn();
		List<SubagentNews> news = List.of(new SubagentNews("sa-1", "Counter", SubagentNews.Reason.FINISHED, "call-1", ""));

		listener.onEvent(new AgentEvent.LeadWoken("", 0, Instant.now(), 1, 0, news));

		Part.SystemPart part = (Part.SystemPart) log.currentTurn().assistantParts().get(0);
		assertEquals(Part.SystemEventType.LEAD_WOKEN, part.eventType());
		assertEquals(SubagentNews.toJson(news), part.details());

		Conversation replayed = new Conversation();
		ConversationLogReplay.rebuildMemory(log, replayed);
		assertTrue("Not conversation content.", replayed.messages().isEmpty());
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
