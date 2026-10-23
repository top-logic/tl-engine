/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.history;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.cost.Money;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.RunRecording;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogReplay;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.session.persist.ConversationLogSerializer;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.util.model.ModelService;

/**
 * A conversation whose runs were produced outside the agent loop and recorded through
 * {@link RunRecording} reloads through {@link ChatHistoryService} like any other.
 */
public class TestExternalRunHistory extends BasicTestCase {

	private static final String AGENT = "claude-code";

	/**
	 * Two external runs of one session load as two turns, in run order, with their parts, and
	 * replay into the model conversation a resumed chat continues from.
	 */
	public void testExternalRunsReload() {
		AgentSession session =
			AgentExecutionStore.getInstance().createSession(SessionKind.CHAT, null, "external-history");
		ConversationLog log = new ConversationLog();

		record(session, log, "Wie spät ist es?", "date", "12:00", "Es ist zwölf.");
		record(session, log, "Und morgen?", "date", "12:00", "Morgen auch.");

		HistoryUtils.updateSessionRevision();
		String json = ChatHistoryService.loadConversationJson(session);
		assertNotNull(json);
		List<Turn> turns = ConversationLogSerializer.fromJson(json).turns();
		assertEquals(2, turns.size());
		assertEquals("Wie spät ist es?", ((Part.TextPart) turns.get(0).userParts().get(0)).text());
		assertEquals("Und morgen?", ((Part.TextPart) turns.get(1).userParts().get(0)).text());
		List<Part> parts = turns.get(1).assistantParts();
		assertEquals("Parts: " + parts, 2, parts.size());
		assertEquals(Part.ToolCallStatus.COMPLETED, ((Part.ToolCallPart) parts.get(0)).status());
		assertEquals("Morgen auch.", ((Part.TextPart) parts.get(1)).text());

		Conversation conversation = new Conversation();
		ConversationLogReplay.rebuildMemory(ConversationLogSerializer.fromJson(json), conversation);
		List<Message> messages = conversation.messages();
		assertEquals("Messages: " + messages, 2, messages.stream().filter(UserMessage.class::isInstance).count());
	}

	private static void record(AgentSession session, ConversationLog log, String question, String tool,
			String toolResult, String answer) {
		Turn turn = log.startTurn();
		turn.setUserParts(List.of(new Part.TextPart("user-" + System.nanoTime(), Instant.now(), question)));

		RunRecording recording = RunRecording.open(AGENT, session, null, null, log, null);
		String runId = "ext-" + System.nanoTime();
		Instant now = Instant.now();
		TokenUsage usage = new TokenUsage(100, 20, 0, 0, 0);
		recording.onEvent(new AgentEvent.RunStarted(runId, 0, now, "claude-opus-5-5"));
		recording.onEvent(new AgentEvent.TurnStarted(runId, 1, now));
		recording.onEvent(new AgentEvent.ToolCallStarted(runId, 1, now, "call-" + runId, tool, "{}", ToolMode.READ));
		recording.onEvent(new AgentEvent.ToolCallCompleted(runId, 1, now, "call-" + runId, tool, toolResult,
			Duration.ofMillis(5)));
		recording.onEvent(new AgentEvent.TextDelta(runId, 1, now, answer));
		recording.onEvent(new AgentEvent.TurnCompleted(runId, 1, now, usage, List.of()));
		recording.onEvent(new AgentEvent.RunCompleted(runId, 1, now, StopReason.COMPLETE, usage,
			new Money(0.002, Money.USD)));
		recording.finish(answer);
	}

	/** Creates the test suite: KB around the store. */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(new TestSuite(TestExternalRunHistory.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
