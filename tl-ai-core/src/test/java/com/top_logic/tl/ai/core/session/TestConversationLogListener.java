/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.approval.ApprovalOutcome;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.message.TokenUsage;

/**
 * Tests for {@link ConversationLogListener}.
 */
public class TestConversationLogListener extends TestCase {

	private ConversationLog _log;
	private ConversationLogListener _listener;

	@Override
	protected void setUp() {
		_log = new ConversationLog();
		_listener = new ConversationLogListener(_log);
	}

	/**
	 * Turn is created by ChatProcessor (startTurn), then RunStarted
	 * reuses it. RunCompleted completes it.
	 */
	public void testRunLifecycleReusesExistingTurn() {
		Instant now = Instant.now();
		Turn turn = _log.startTurn();
		turn.setUserParts(List.of(new Part.TextPart("u1", now, "Hello")));

		_listener.onEvent(new AgentEvent.RunStarted("r1", 0, now, "claude-sonnet-4-20250514"));
		assertEquals(1, _log.turns().size());
		assertEquals("claude-sonnet-4-20250514", _log.currentTurn().model());

		_listener.onEvent(new AgentEvent.TurnStarted("r1", 1, now));
		assertEquals(1, _log.turns().size());

		_listener.onEvent(new AgentEvent.TurnCompleted("r1", 1, now, new TokenUsage(100, 50, 0, 0, 0), List.of()));
		assertNotNull(_log.currentTurn());

		_listener.onEvent(new AgentEvent.TurnStarted("r1", 2, now));
		_listener.onEvent(new AgentEvent.TurnCompleted("r1", 2, now, new TokenUsage(80, 30, 0, 0, 0), List.of()));
		assertEquals(1, _log.turns().size());

		assertEquals(180, _log.currentTurn().cumulativeUsage().inputTokens());
		assertEquals(80, _log.currentTurn().cumulativeUsage().outputTokens());

		_listener.onEvent(new AgentEvent.RunCompleted("r1", 2, now,
			StopReason.COMPLETE, new TokenUsage(180, 80, 0, 0, 0)));
		assertNull(_log.currentTurn());
		assertEquals(1, _log.turns().size());
	}

	/** If no turn exists when RunStarted fires, listener creates one. */
	public void testRunStartedCreatesTurnIfMissing() {
		Instant now = Instant.now();
		_listener.onEvent(new AgentEvent.RunStarted("r1", 0, now, "claude-sonnet-4-20250514"));
		assertNotNull(_log.currentTurn());
		assertEquals(1, _log.turns().size());
	}

	/** TextDelta events accumulate, flushed before first tool call. */
	public void testTextFlushedBeforeToolCall() {
		Instant now = Instant.now();
		_log.startTurn();
		_listener.onEvent(new AgentEvent.RunStarted("r1", 0, now, "claude-sonnet-4-20250514"));
		_listener.onEvent(new AgentEvent.TurnStarted("r1", 1, now));

		_listener.onEvent(new AgentEvent.TextDelta("r1", 1, now, "I'll read "));
		_listener.onEvent(new AgentEvent.TextDelta("r1", 1, now, "that file."));

		_listener.onEvent(new AgentEvent.ToolCallStarted("r1", 1, now, "tc1", "readFile", "{}"));

		Turn turn = _log.currentTurn();
		List<Part> parts = turn.assistantParts();
		assertTrue(parts.get(0) instanceof Part.TextPart);
		assertEquals("I'll read that file.", ((Part.TextPart) parts.get(0)).text());
		assertTrue(parts.get(1) instanceof Part.ToolCallPart);
	}

	/** TextDelta without tool calls flushed at RunCompleted. */
	public void testTextFlushedAtRunEnd() {
		Instant now = Instant.now();
		_log.startTurn();
		_listener.onEvent(new AgentEvent.RunStarted("r1", 0, now, "model"));
		_listener.onEvent(new AgentEvent.TurnStarted("r1", 1, now));
		_listener.onEvent(new AgentEvent.TextDelta("r1", 1, now, "Hello world"));
		_listener.onEvent(new AgentEvent.TurnCompleted("r1", 1, now, TokenUsage.EMPTY, List.of()));
		_listener.onEvent(new AgentEvent.RunCompleted("r1", 1, now, StopReason.COMPLETE, TokenUsage.EMPTY));

		Turn turn = _log.turns().get(0);
		boolean hasText = turn.assistantParts().stream()
			.anyMatch(p -> p instanceof Part.TextPart tp && tp.text().equals("Hello world"));
		assertTrue("Expected accumulated text part", hasText);
	}

	/** Tool call lifecycle: Started then Completed creates and updates a ToolCallPart. */
	public void testToolCallLifecycle() {
		Instant now = Instant.now();
		_log.startTurn();
		_listener.onEvent(new AgentEvent.RunStarted("r1", 0, now, "model"));
		_listener.onEvent(new AgentEvent.TurnStarted("r1", 1, now));
		_listener.onEvent(new AgentEvent.ToolCallStarted("r1", 1, now, "tc1", "readFile",
			"{\"path\":\"/tmp\"}"));

		Turn turn = _log.currentTurn();
		Part.ToolCallPart running = (Part.ToolCallPart) turn.findPart("tc1");
		assertNotNull(running);
		assertEquals(Part.ToolCallStatus.RUNNING, running.status());

		_listener.onEvent(new AgentEvent.ToolCallCompleted("r1", 1, now, "tc1", "readFile",
			"file contents", Duration.ofMillis(42)));

		Part.ToolCallPart completed = (Part.ToolCallPart) turn.findPart("tc1");
		assertEquals(Part.ToolCallStatus.COMPLETED, completed.status());
		assertEquals(42L, completed.durationMs());
	}

	/** Failed tool calls are recorded with error message. */
	public void testToolCallFailed() {
		Instant now = Instant.now();
		_log.startTurn();
		_listener.onEvent(new AgentEvent.RunStarted("r1", 0, now, "model"));
		_listener.onEvent(new AgentEvent.TurnStarted("r1", 1, now));
		_listener.onEvent(new AgentEvent.ToolCallStarted("r1", 1, now, "tc1", "writeFile", "{}"));
		_listener.onEvent(new AgentEvent.ToolCallFailed("r1", 1, now, "tc1", "writeFile",
			"Permission denied", Duration.ofMillis(5)));

		Part.ToolCallPart failed = (Part.ToolCallPart) _log.currentTurn().findPart("tc1");
		assertEquals(Part.ToolCallStatus.FAILED, failed.status());
		assertEquals("Permission denied", failed.errorMessage());
	}

	/** Denied tool calls are recorded (no ToolCallStarted precedes ToolCallDenied). */
	public void testToolCallDenied() {
		Instant now = Instant.now();
		_log.startTurn();
		_listener.onEvent(new AgentEvent.RunStarted("r1", 0, now, "model"));
		_listener.onEvent(new AgentEvent.TurnStarted("r1", 1, now));
		_listener.onEvent(new AgentEvent.ToolCallDenied("r1", 1, now, "tc1", "deleteFile",
			"User denied: too risky"));

		Part.ToolCallPart denied = (Part.ToolCallPart) _log.currentTurn().findPart("tc1");
		assertEquals(Part.ToolCallStatus.DENIED, denied.status());
		assertEquals("User denied: too risky", denied.denyReason());
	}

	/** Approval events create ApprovalParts. */
	public void testApprovalLifecycle() {
		Instant now = Instant.now();
		_log.startTurn();
		_listener.onEvent(new AgentEvent.RunStarted("r1", 0, now, "model"));
		_listener.onEvent(new AgentEvent.TurnStarted("r1", 1, now));
		_listener.onEvent(new AgentEvent.ApprovalRequired("r1", 1, now, "tc1", "apr1",
			"writeFile", Map.of("path", "/etc/hosts"), "Write to system file"));

		Part.ApprovalPart pending = (Part.ApprovalPart) _log.currentTurn().findPart("apr1");
		assertNotNull(pending);
		assertNull(pending.decision());

		_listener.onEvent(new AgentEvent.ApprovalResolved("r1", 1, now, "tc1", "apr1",
			ApprovalOutcome.APPROVED_FOR_SESSION));

		Part.ApprovalPart resolved = (Part.ApprovalPart) _log.currentTurn().findPart("apr1");
		assertEquals(Part.ApprovalDecision.APPROVED_FOR_SESSION, resolved.decision());
	}

	/** ThinkingDelta events accumulate into a ThinkingPart at run end. */
	public void testThinkingAccumulation() {
		Instant now = Instant.now();
		_log.startTurn();
		_listener.onEvent(new AgentEvent.RunStarted("r1", 0, now, "model"));
		_listener.onEvent(new AgentEvent.TurnStarted("r1", 1, now));
		_listener.onEvent(new AgentEvent.ThinkingDelta("r1", 1, now, "Let me "));
		_listener.onEvent(new AgentEvent.ThinkingDelta("r1", 1, now, "think..."));
		_listener.onEvent(new AgentEvent.TurnCompleted("r1", 1, now, TokenUsage.EMPTY, List.of()));
		_listener.onEvent(new AgentEvent.RunCompleted("r1", 1, now, StopReason.COMPLETE, TokenUsage.EMPTY));

		Turn turn = _log.turns().get(0);
		boolean hasThinking = turn.assistantParts().stream()
			.anyMatch(p -> p instanceof Part.ThinkingPart tp && tp.text().equals("Let me think..."));
		assertTrue("Expected accumulated thinking part", hasThinking);
	}

	/** Multi-turn agent run: text between tool calls flushed as separate TextParts. */
	public void testMultiTurnRun() {
		Instant now = Instant.now();
		_log.startTurn();
		_listener.onEvent(new AgentEvent.RunStarted("r1", 0, now, "model"));

		// AgentLoop turn 1: text + tool call
		_listener.onEvent(new AgentEvent.TurnStarted("r1", 1, now));
		_listener.onEvent(new AgentEvent.TextDelta("r1", 1, now, "Reading file..."));
		_listener.onEvent(new AgentEvent.ToolCallStarted("r1", 1, now, "tc1", "readFile", "{}"));
		_listener.onEvent(new AgentEvent.ToolCallCompleted("r1", 1, now, "tc1", "readFile",
			"contents", Duration.ofMillis(10)));
		_listener.onEvent(new AgentEvent.TurnCompleted("r1", 1, now, new TokenUsage(50, 20, 0, 0, 0), List.of()));

		// AgentLoop turn 2: text + tool call
		_listener.onEvent(new AgentEvent.TurnStarted("r1", 2, now));
		_listener.onEvent(new AgentEvent.TextDelta("r1", 2, now, "Now writing..."));
		_listener.onEvent(new AgentEvent.ToolCallStarted("r1", 2, now, "tc2", "writeFile", "{}"));
		_listener.onEvent(new AgentEvent.ToolCallCompleted("r1", 2, now, "tc2", "writeFile",
			"ok", Duration.ofMillis(20)));
		_listener.onEvent(new AgentEvent.TurnCompleted("r1", 2, now, new TokenUsage(40, 15, 0, 0, 0), List.of()));

		// AgentLoop turn 3: final text response
		_listener.onEvent(new AgentEvent.TurnStarted("r1", 3, now));
		_listener.onEvent(new AgentEvent.TextDelta("r1", 3, now, "Done!"));
		_listener.onEvent(new AgentEvent.TurnCompleted("r1", 3, now, new TokenUsage(30, 10, 0, 0, 0), List.of()));
		_listener.onEvent(new AgentEvent.RunCompleted("r1", 3, now, StopReason.COMPLETE,
			new TokenUsage(120, 45, 0, 0, 0)));

		// All in ONE ConversationLog turn
		assertEquals(1, _log.turns().size());
		Turn turn = _log.turns().get(0);

		// Parts: text1, tc1, text2, tc2, text3
		List<Part> parts = turn.assistantParts();
		assertEquals(5, parts.size());
		assertTrue(parts.get(0) instanceof Part.TextPart);
		assertEquals("Reading file...", ((Part.TextPart) parts.get(0)).text());
		assertTrue(parts.get(1) instanceof Part.ToolCallPart);
		assertTrue(parts.get(2) instanceof Part.TextPart);
		assertEquals("Now writing...", ((Part.TextPart) parts.get(2)).text());
		assertTrue(parts.get(3) instanceof Part.ToolCallPart);
		assertTrue(parts.get(4) instanceof Part.TextPart);
		assertEquals("Done!", ((Part.TextPart) parts.get(4)).text());

		// Cumulative usage
		assertEquals(120, turn.cumulativeUsage().inputTokens());
		assertEquals(45, turn.cumulativeUsage().outputTokens());
	}

	/** recordInteraction() creates and resolves InteractionParts. */
	public void testInteractionRecording() {
		_log.startTurn();
		Instant now = Instant.now();
		_listener.onEvent(new AgentEvent.RunStarted("r1", 0, now, "model"));

		String id = _listener.recordInteractionStarted("choice", "Pick a DB",
			"Select", List.of("Postgres", "MySQL"), false);
		Part.InteractionPart pending = (Part.InteractionPart) _log.currentTurn().findPart(id);
		assertNotNull(pending);
		assertNull(pending.userResponse());

		_listener.resolveInteraction(id, "Postgres");
		Part.InteractionPart resolved = (Part.InteractionPart) _log.currentTurn().findPart(id);
		assertEquals("Postgres", resolved.userResponse());
	}
}
