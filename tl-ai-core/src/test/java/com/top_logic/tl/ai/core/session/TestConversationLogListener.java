/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.approval.ApprovalOutcome;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.message.ThinkingContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.tool.api.ToolMode;

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

		_listener.onEvent(new AgentEvent.ToolCallStarted("r1", 1, now, "tc1", "readFile", "{}", ToolMode.READ));

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
			"{\"path\":\"/tmp\"}", ToolMode.READ));

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
		_listener.onEvent(new AgentEvent.ToolCallStarted("r1", 1, now, "tc1", "writeFile", "{}",
			ToolMode.DATA_WRITE));
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

	/**
	 * A rolled-back turn retracts the tool calls that reported success inside its transaction.
	 *
	 * <p>
	 * A write tool commits only a transaction nested inside the turn's, which writes no SQL, so its
	 * {@code COMPLETED} was a claim about a commit that had not happened yet. A transcript reloaded
	 * from the database used to show a green write for a change that was never persisted.
	 * </p>
	 */
	public void testRolledBackTurnRetractsItsCompletedToolCalls() {
		Instant now = Instant.now();
		_log.startTurn();
		_listener.onEvent(new AgentEvent.RunStarted("r1", 0, now, "model"));
		_listener.onEvent(new AgentEvent.TurnStarted("r1", 1, now));
		_listener.onEvent(new AgentEvent.ToolCallStarted("r1", 1, now, "tc1", "createDocument", "{}",
			ToolMode.DATA_WRITE));
		_listener.onEvent(new AgentEvent.TransactionBegan("r1", 1, now));
		_listener.onEvent(new AgentEvent.ToolCallCompleted("r1", 1, now, "tc1", "createDocument",
			"{uuid=4f2c}", Duration.ofMillis(10)));
		_listener.onEvent(new AgentEvent.TransactionRolledBack("r1", 1, now,
			"Database operation failed."));

		Part.ToolCallPart retracted = (Part.ToolCallPart) _log.currentTurn().findPart("tc1");
		assertEquals("A write whose turn never committed must not stay COMPLETED.",
			Part.ToolCallStatus.ROLLED_BACK, retracted.status());
		assertEquals("The reason has to travel with the retraction.",
			"Database operation failed.", retracted.errorMessage());
		assertEquals("The result the tool claimed is kept, so a reader sees the claim next to its"
			+ " retraction.", "{uuid=4f2c}", retracted.result());
		assertEquals("The duration is not invalidated by the rollback.", 10L, retracted.durationMs());
	}

	/**
	 * Only the tool calls of the rolled-back transaction are retracted.
	 *
	 * <p>
	 * A {@link com.top_logic.tl.ai.core.agent.TransactionScope} lives for a whole run, one KB
	 * transaction per turn. The writes of an earlier turn whose own commit succeeded are genuinely
	 * persisted and must stay {@code COMPLETED} - the window starts at the most recent
	 * {@code BEGAN}.
	 * </p>
	 */
	public void testRollbackLeavesAnEarlierCommittedTurnAlone() {
		Instant now = Instant.now();
		_log.startTurn();
		_listener.onEvent(new AgentEvent.RunStarted("r1", 0, now, "model"));

		_listener.onEvent(new AgentEvent.TurnStarted("r1", 1, now));
		_listener.onEvent(new AgentEvent.ToolCallStarted("r1", 1, now, "tc1", "createDocument", "{}",
			ToolMode.DATA_WRITE));
		_listener.onEvent(new AgentEvent.TransactionBegan("r1", 1, now));
		_listener.onEvent(new AgentEvent.ToolCallCompleted("r1", 1, now, "tc1", "createDocument",
			"first", Duration.ofMillis(10)));
		_listener.onEvent(new AgentEvent.TransactionCommitted("r1", 1, now, 4711));

		_listener.onEvent(new AgentEvent.TurnStarted("r1", 2, now));
		_listener.onEvent(new AgentEvent.ToolCallStarted("r1", 2, now, "tc2", "createDocument", "{}",
			ToolMode.DATA_WRITE));
		_listener.onEvent(new AgentEvent.TransactionBegan("r1", 2, now));
		_listener.onEvent(new AgentEvent.ToolCallCompleted("r1", 2, now, "tc2", "createDocument",
			"second", Duration.ofMillis(10)));
		_listener.onEvent(new AgentEvent.TransactionRolledBack("r1", 2, now, "Commit failed"));

		Turn turn = _log.currentTurn();
		assertEquals("Revision 4711 persisted this write; retracting it would be the same lie in"
			+ " the other direction.", Part.ToolCallStatus.COMPLETED,
			((Part.ToolCallPart) turn.findPart("tc1")).status());
		assertEquals(Part.ToolCallStatus.ROLLED_BACK,
			((Part.ToolCallPart) turn.findPart("tc2")).status());
	}

	/**
	 * A rollback retracts the writes of its transaction and leaves the reads alone.
	 *
	 * <p>
	 * A turn that reads and then writes is ordinary. The read wrote nothing, so the rollback did
	 * not void it: its result is still what the tool found, and a reloaded conversation must keep
	 * it. Retracting it too would cost the model the data for no gain.
	 * </p>
	 */
	public void testRollbackRetractsTheWriteAndKeepsTheRead() {
		Instant now = Instant.now();
		_log.startTurn();
		_listener.onEvent(new AgentEvent.RunStarted("r1", 0, now, "model"));
		_listener.onEvent(new AgentEvent.TurnStarted("r1", 1, now));

		_listener.onEvent(new AgentEvent.ToolCallStarted("r1", 1, now, "tc1", "searchModel", "{}",
			ToolMode.READ));
		_listener.onEvent(new AgentEvent.TransactionBegan("r1", 1, now));
		_listener.onEvent(new AgentEvent.ToolCallCompleted("r1", 1, now, "tc1", "searchModel",
			"3 documents found", Duration.ofMillis(7)));

		_listener.onEvent(new AgentEvent.ToolCallStarted("r1", 1, now, "tc2", "createDocument", "{}",
			ToolMode.DATA_WRITE));
		_listener.onEvent(new AgentEvent.ToolCallCompleted("r1", 1, now, "tc2", "createDocument",
			"{uuid=4f2c}", Duration.ofMillis(10)));

		_listener.onEvent(new AgentEvent.TransactionRolledBack("r1", 1, now,
			"Database operation failed."));

		Turn turn = _log.currentTurn();
		Part.ToolCallPart read = (Part.ToolCallPart) turn.findPart("tc1");
		assertEquals("A read wrote nothing, so the rollback voided nothing to retract.",
			Part.ToolCallStatus.COMPLETED, read.status());
		assertEquals("The read's result is the data the model saw and must survive a reload.",
			"3 documents found", read.result());
		assertNull("A kept read carries no rollback reason.", read.errorMessage());

		assertEquals("The write's success was a claim about a commit that never happened.",
			Part.ToolCallStatus.ROLLED_BACK,
			((Part.ToolCallPart) turn.findPart("tc2")).status());
	}

	/**
	 * A call of unknown mode inside the rolled-back transaction is retracted.
	 *
	 * <p>
	 * The mode is unknown for a call restored from a log written before it was recorded, and for a
	 * tool the loop could not resolve. Unknown counts as a write: guessing the other way would tell
	 * the model a write landed that did not.
	 * </p>
	 */
	public void testRollbackRetractsACallOfUnknownMode() {
		Instant now = Instant.now();
		_log.startTurn();
		_listener.onEvent(new AgentEvent.RunStarted("r1", 0, now, "model"));
		_listener.onEvent(new AgentEvent.TurnStarted("r1", 1, now));
		_listener.onEvent(new AgentEvent.ToolCallStarted("r1", 1, now, "tc1", "unresolvedTool", "{}",
			null));
		_listener.onEvent(new AgentEvent.TransactionBegan("r1", 1, now));
		_listener.onEvent(new AgentEvent.ToolCallCompleted("r1", 1, now, "tc1", "unresolvedTool",
			"done", Duration.ofMillis(4)));
		_listener.onEvent(new AgentEvent.TransactionRolledBack("r1", 1, now,
			"Database operation failed."));

		Part.ToolCallPart part = (Part.ToolCallPart) _log.currentTurn().findPart("tc1");
		assertEquals("An unknown mode must be treated as a write, never risked as a read.",
			Part.ToolCallStatus.ROLLED_BACK, part.status());
		assertEquals("Database operation failed.", part.errorMessage());
	}

	/** A rollback does not resurrect a tool call that had already failed. */
	public void testRollbackKeepsAFailedToolCallFailed() {
		Instant now = Instant.now();
		_log.startTurn();
		_listener.onEvent(new AgentEvent.RunStarted("r1", 0, now, "model"));
		_listener.onEvent(new AgentEvent.TurnStarted("r1", 1, now));
		_listener.onEvent(new AgentEvent.ToolCallStarted("r1", 1, now, "tc1", "createDocument", "{}",
			ToolMode.DATA_WRITE));
		_listener.onEvent(new AgentEvent.TransactionBegan("r1", 1, now));
		_listener.onEvent(new AgentEvent.ToolCallFailed("r1", 1, now, "tc1", "createDocument",
			"Name too long", Duration.ofMillis(3)));
		_listener.onEvent(new AgentEvent.TransactionRolledBack("r1", 1, now,
			"Tool execution failed"));

		Part.ToolCallPart part = (Part.ToolCallPart) _log.currentTurn().findPart("tc1");
		assertEquals(Part.ToolCallStatus.FAILED, part.status());
		assertEquals("Name too long", part.errorMessage());
	}

	/** Approval events create ApprovalParts. */
	public void testApprovalLifecycle() {
		Instant now = Instant.now();
		_log.startTurn();
		_listener.onEvent(new AgentEvent.RunStarted("r1", 0, now, "model"));
		_listener.onEvent(new AgentEvent.TurnStarted("r1", 1, now));
		_listener.onEvent(new AgentEvent.ApprovalRequired("r1", 1, now, "tc1", "apr1",
			"writeFile", "{\"path\":\"/etc/hosts\"}", "Write to system file", Duration.ofMinutes(30)));

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
		_listener.onEvent(new AgentEvent.ToolCallStarted("r1", 1, now, "tc1", "readFile", "{}", ToolMode.READ));
		_listener.onEvent(new AgentEvent.ToolCallCompleted("r1", 1, now, "tc1", "readFile",
			"contents", Duration.ofMillis(10)));
		_listener.onEvent(new AgentEvent.TurnCompleted("r1", 1, now, new TokenUsage(50, 20, 0, 0, 0), List.of()));

		// AgentLoop turn 2: text + tool call
		_listener.onEvent(new AgentEvent.TurnStarted("r1", 2, now));
		_listener.onEvent(new AgentEvent.TextDelta("r1", 2, now, "Now writing..."));
		_listener.onEvent(new AgentEvent.ToolCallStarted("r1", 2, now, "tc2", "writeFile", "{}",
			ToolMode.DATA_WRITE));
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

	/**
	 * The continuity token of a completed turn is written into the recorded
	 * {@link Part.ThinkingPart}.
	 *
	 * <p>
	 * The provider only reveals the token when the thinking block is finished, which is after the
	 * deltas that built the part have been flushed. Without the backfill a reloaded session replays
	 * the thinking text without its token, and the provider can no longer continue its own
	 * reasoning.
	 * </p>
	 */
	public void testContinuityTokenBackfilledIntoThinkingPart() {
		Instant now = Instant.now();
		_log.startTurn();
		_listener.onEvent(new AgentEvent.RunStarted("r1", 0, now, "model"));
		_listener.onEvent(new AgentEvent.TurnStarted("r1", 1, now));
		_listener.onEvent(new AgentEvent.ThinkingDelta("r1", 1, now, "Let me "));
		_listener.onEvent(new AgentEvent.ThinkingDelta("r1", 1, now, "think..."));
		// Flushes the thinking buffer into a part, as the real event order does.
		_listener.onEvent(new AgentEvent.ToolCallStarted("r1", 1, now, "tc1", "readFile", "{}",
			ToolMode.READ));

		Part.ThinkingPart recorded = onlyThinkingPart(_log.currentTurn());
		assertNull("The token is unknown while the deltas stream.", recorded.continuityToken());

		_listener.onEvent(new AgentEvent.TurnCompleted("r1", 1, now, TokenUsage.EMPTY,
			List.of(new ThinkingContent("Let me think...", "sig-1"))));

		Part.ThinkingPart backfilled = onlyThinkingPart(_log.currentTurn());
		assertEquals("The provider's continuity token has to reach the recorded part.",
			"sig-1", backfilled.continuityToken());
		assertEquals("The part is corrected in place, not replaced by a new one.",
			recorded.id(), backfilled.id());
		assertEquals(recorded.timestamp(), backfilled.timestamp());
		assertEquals("Let me think...", backfilled.text());
	}

	/** A turn whose thinking carries no token leaves the recorded part untouched. */
	public void testMissingContinuityTokenLeavesThinkingPartUntouched() {
		Instant now = Instant.now();
		_log.startTurn();
		_listener.onEvent(new AgentEvent.RunStarted("r1", 0, now, "model"));
		_listener.onEvent(new AgentEvent.TurnStarted("r1", 1, now));
		_listener.onEvent(new AgentEvent.ThinkingDelta("r1", 1, now, "Thinking"));
		_listener.onEvent(new AgentEvent.ToolCallStarted("r1", 1, now, "tc1", "readFile", "{}",
			ToolMode.READ));

		Part.ThinkingPart recorded = onlyThinkingPart(_log.currentTurn());

		_listener.onEvent(new AgentEvent.TurnCompleted("r1", 1, now, TokenUsage.EMPTY,
			List.of(new ThinkingContent("Thinking", null))));

		Part.ThinkingPart after = onlyThinkingPart(_log.currentTurn());
		assertNull("Nothing to backfill means nothing to change.", after.continuityToken());
		assertEquals(recorded.id(), after.id());
		assertEquals("Thinking", after.text());
	}

	/**
	 * Thinking parts and thinking contents are paired positionally, as far as both sides reach.
	 *
	 * <p>
	 * A part is only recorded once its deltas are flushed, so the turn may hold fewer parts than
	 * the completed message holds thinking blocks. The surplus is dropped rather than mispaired.
	 * </p>
	 */
	public void testContinuityTokenBackfillPairsWhatBothSidesHave() {
		Instant now = Instant.now();
		_log.startTurn();
		_listener.onEvent(new AgentEvent.RunStarted("r1", 0, now, "model"));
		_listener.onEvent(new AgentEvent.TurnStarted("r1", 1, now));
		_listener.onEvent(new AgentEvent.ThinkingDelta("r1", 1, now, "First"));
		_listener.onEvent(new AgentEvent.ToolCallStarted("r1", 1, now, "tc1", "readFile", "{}",
			ToolMode.READ));

		_listener.onEvent(new AgentEvent.TurnCompleted("r1", 1, now, TokenUsage.EMPTY,
			List.of(new ThinkingContent("First", "sig-1"), new ThinkingContent("Second", "sig-2"))));

		List<Part.ThinkingPart> parts = thinkingParts(_log.currentTurn());
		assertEquals("The second block has no recorded part yet.", 1, parts.size());
		assertEquals("sig-1", parts.get(0).continuityToken());
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

	private static Part.ThinkingPart onlyThinkingPart(Turn turn) {
		List<Part.ThinkingPart> parts = thinkingParts(turn);
		assertEquals("Expected exactly one thinking part.", 1, parts.size());
		return parts.get(0);
	}

	private static List<Part.ThinkingPart> thinkingParts(Turn turn) {
		return turn.assistantParts().stream()
			.filter(Part.ThinkingPart.class::isInstance)
			.map(Part.ThinkingPart.class::cast)
			.toList();
	}
}
