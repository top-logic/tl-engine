/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.ExecutionStatus;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.session.persist.ConversationLogSerializer;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.util.model.ModelService;

/**
 * Test of {@link RunRecording} driven by a synthetic event stream, the way a producer outside of
 * the agent loop drives it: no {@link com.top_logic.tl.ai.core.agent.AgentLoop}, no LLM provider,
 * just the {@link AgentEvent}s, against a knowledge base with the execution store running.
 */
public class TestRunRecording extends BasicTestCase {

	private static final String AGENT = "claude-code";

	private static final String MODEL = "claude-opus-5-5";

	private static final String QUESTION = "Baue das Modul und berichte.";

	/**
	 * A completed run leaves a finished execution row with agent, model, tokens and cost, and a
	 * transcript holding the user's message, the streamed text and the tool call.
	 */
	public void testCompletedRun() {
		AgentSession session = newSession("external-complete");
		ConversationLog log = logWithQuestion();
		List<AgentEvent> forwarded = new ArrayList<>();

		RunRecording recording = RunRecording.open(AGENT, session, null, null, log, forwarded::add);

		String runId = "ext-" + System.nanoTime();
		Instant now = Instant.now();
		TokenUsage turn1 = new TokenUsage(1000, 200, 0, 300, 50);
		TokenUsage turn2 = new TokenUsage(1500, 100, 0, 1200, 0);
		TokenUsage total = new TokenUsage(2500, 300, 0, 1500, 50);
		List<AgentEvent> events = List.of(
			new AgentEvent.RunStarted(runId, 0, now, MODEL),
			new AgentEvent.TurnStarted(runId, 1, now),
			new AgentEvent.TextDelta(runId, 1, now, "Ich baue "),
			new AgentEvent.TextDelta(runId, 1, now, "das Modul."),
			new AgentEvent.ToolCallStarted(runId, 1, now, "call-1", "Bash", "{\"command\":\"mvn install\"}",
				ToolMode.READ),
			new AgentEvent.ToolCallCompleted(runId, 1, now, "call-1", "Bash", "BUILD SUCCESS",
				Duration.ofSeconds(42)),
			new AgentEvent.TurnCompleted(runId, 1, now, turn1, List.of()),
			new AgentEvent.TurnStarted(runId, 2, now),
			new AgentEvent.TextDelta(runId, 2, now, "Der Build ist grün."),
			new AgentEvent.TurnCompleted(runId, 2, now, turn2, List.of()),
			new AgentEvent.RunCompleted(runId, 2, now, StopReason.COMPLETE, total, new Money(0.125, Money.USD)));
		events.forEach(recording::onEvent);

		assertEquals("Every event reaches the caller's listener.", events, forwarded);
		assertEquals(runId, recording.handle().runInfo().runId());
		assertEquals(AGENT, recording.handle().runInfo().agentName());
		assertEquals(MODEL, recording.handle().runInfo().model());
		assertNotNull(recording.execution());
		assertSame(recording.execution(), recording.handle().get());
		assertEquals(StopReason.COMPLETE, recording.result().stopReason());

		recording.finish("Der Build ist grün.");

		AgentExecution row = reread(runId);
		assertEquals(AGENT, row.getAgentName());
		assertEquals(MODEL, row.getModel());
		assertEquals(ExecutionStatus.COMPLETE.name(), row.getStatus().getName());
		assertEquals(StopReason.COMPLETE.name(), row.getStopReason());
		assertNull(row.getErrorMessage());
		assertEquals(Long.valueOf(2500), row.getInputTokens());
		assertEquals(Long.valueOf(300), row.getOutputTokens());
		assertEquals(Long.valueOf(1500), row.getCacheReadTokens());
		assertEquals(Long.valueOf(50), row.getCacheWriteTokens());
		assertEquals(Double.valueOf(0.125), row.getCost());
		assertEquals("Der Build ist grün.", row.getResponse());
		assertNotNull(row.getEndedAt());

		Turn turn = onlyTurnOf(session);
		assertEquals(QUESTION, ((Part.TextPart) turn.userParts().get(0)).text());
		assertEquals(MODEL, turn.model());
		List<Part> parts = turn.assistantParts();
		assertEquals("Parts: " + parts, 3, parts.size());
		assertEquals("Ich baue das Modul.", ((Part.TextPart) parts.get(0)).text());
		Part.ToolCallPart call = (Part.ToolCallPart) parts.get(1);
		assertEquals("Bash", call.toolName());
		assertEquals(Part.ToolCallStatus.COMPLETED, call.status());
		assertEquals("BUILD SUCCESS", call.result());
		assertEquals("Der Build ist grün.", ((Part.TextPart) parts.get(2)).text());
		assertEquals(total.inputTokens(), turn.cumulativeUsage().inputTokens());
	}

	/**
	 * A run that ends with a provider error is recorded as failed, with the provider's message.
	 */
	public void testProviderErrorIsRecordedAsFailed() {
		AgentSession session = newSession("external-error");
		ConversationLog log = logWithQuestion();
		RunRecording recording = RunRecording.open(AGENT, session, null, null, log, null);

		String runId = "ext-err-" + System.nanoTime();
		Instant now = Instant.now();
		TokenUsage usage = new TokenUsage(400, 0, 0, 0, 0);
		recording.onEvent(new AgentEvent.RunStarted(runId, 0, now, MODEL));
		recording.onEvent(new AgentEvent.TurnStarted(runId, 1, now));
		recording.onEvent(new AgentEvent.ProviderError(runId, 1, now, "OVERLOADED", "Overloaded"));
		recording.onEvent(new AgentEvent.RunCompleted(runId, 1, now, StopReason.ERROR, usage,
			new Money(0.01, Money.USD)));
		recording.finish(null);

		AgentExecution row = reread(runId);
		assertEquals(ExecutionStatus.ERROR.name(), row.getStatus().getName());
		assertEquals(StopReason.ERROR.name(), row.getStopReason());
		assertEquals("Overloaded", row.getErrorMessage());
		assertEquals(Long.valueOf(400), row.getInputTokens());
		assertEquals(Double.valueOf(0.01), row.getCost());
		assertEquals("OVERLOADED", recording.result().errorKind());
		assertEquals(QUESTION, ((Part.TextPart) onlyTurnOf(session).userParts().get(0)).text());
	}

	/**
	 * A producer that dies before completing the run records it as failed, and the user's message
	 * stays on record.
	 */
	public void testFailedProducerIsRecordedAsFailed() {
		AgentSession session = newSession("external-died");
		ConversationLog log = logWithQuestion();
		RunRecording recording = RunRecording.open(AGENT, session, null, null, log, null);

		String runId = "ext-died-" + System.nanoTime();
		Instant now = Instant.now();
		recording.onEvent(new AgentEvent.RunStarted(runId, 0, now, null));
		recording.onEvent(new AgentEvent.TextDelta(runId, 1, now, "Ich fange an"));
		recording.fail(new IllegalStateException("Container exited with code 137."));

		AgentExecution row = reread(runId);
		assertEquals(AGENT, row.getAgentName());
		assertNull("No model was named.", row.getModel());
		assertEquals(ExecutionStatus.ERROR.name(), row.getStatus().getName());
		assertEquals(StopReason.ERROR.name(), row.getStopReason());
		assertEquals("Container exited with code 137.", row.getErrorMessage());
		assertNull(row.getCost());
		assertEquals(QUESTION, ((Part.TextPart) onlyTurnOf(session).userParts().get(0)).text());
	}

	/**
	 * Without a session the events still reach the log and the listener, and nothing is recorded.
	 */
	public void testWithoutSessionNothingIsRecorded() {
		ConversationLog log = logWithQuestion();
		List<AgentEvent> forwarded = new ArrayList<>();
		RunRecording recording = RunRecording.open(AGENT, null, null, null, log, forwarded::add);

		Instant now = Instant.now();
		recording.onEvent(new AgentEvent.RunStarted("unrecorded", 0, now, MODEL));
		recording.onEvent(new AgentEvent.TextDelta("unrecorded", 1, now, "Hallo"));
		recording.onEvent(new AgentEvent.RunCompleted("unrecorded", 1, now, StopReason.COMPLETE,
			TokenUsage.EMPTY, Money.zero(Money.USD)));
		recording.finish("Hallo");

		assertEquals(3, forwarded.size());
		assertNull(recording.execution());
		assertNull(recording.result());
		Turn turn = log.turns().get(0);
		assertEquals("Hallo", ((Part.TextPart) turn.assistantParts().get(0)).text());
	}

	private static AgentSession newSession(String title) {
		return AgentExecutionStore.getInstance().createSession(SessionKind.CHAT, null, title);
	}

	/** A log whose open turn holds the user's message, as the chat starts it before a run. */
	private static ConversationLog logWithQuestion() {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		turn.setUserParts(List.of(new Part.TextPart("user-1", Instant.now(), QUESTION)));
		return log;
	}

	private static AgentExecution reread(String runId) {
		HistoryUtils.updateSessionRevision();
		return AgentExecutionStore.getInstance().findExecutionByRunId(runId);
	}

	/** The only turn of the session, read back from its recorded transcripts. */
	private static Turn onlyTurnOf(AgentSession session) {
		HistoryUtils.updateSessionRevision();
		List<AgentExecution> executions = AgentExecutionStore.getInstance().executionsOf(session);
		assertEquals(1, executions.size());
		String transcript = executions.get(0).getTranscript();
		assertNotNull("The row must hold a transcript.", transcript);
		List<Turn> turns = ConversationLogSerializer.fromJson(transcript).turns();
		assertEquals(1, turns.size());
		return turns.get(0);
	}

	/**
	 * The suite, on a knowledge base with the execution store running.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestRunRecording.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
