/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session.persist;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogReplay;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedConversationV2;
import com.top_logic.tl.ai.tool.api.ToolMode;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;

/**
 * Tests for {@link InterruptedTranscript}: how the checkpoint of a run the process ended is closed
 * (#598).
 */
public class TestInterruptedTranscript extends TestCase {

	private static final Instant T0 = Instant.parse("2026-09-30T08:00:00Z");

	/**
	 * The open parts are closed, and everything else - the user's message, the text, a finished
	 * step, the ids - is kept as the user saw it.
	 */
	public void testOpenPartsAreClosedAndTheRestIsKept() throws IOException {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn(T0);
		turn.setUserParts(List.of(new Part.TextPart("u1", T0, "Leg den Beleg an.")));
		turn.addAssistantPart(new Part.TextPart("a1", T0.plusSeconds(1), "Ich schaue nach."));
		turn.addAssistantPart(Part.ToolCallPart.started("read", T0.plusSeconds(2), "lookup", "{}", ToolMode.READ)
			.completed("3 Belege", Duration.ofMillis(40)));
		turn.addAssistantPart(Part.ToolCallPart.started("ask", T0.plusSeconds(3), "askUser", "{}", ToolMode.READ));
		turn.addAssistantPart(Part.ApprovalPart.pending("ap", T0.plusSeconds(4), "write", "create", "{}",
			"Changes shared data.", ToolMode.DATA_WRITE));
		String before = ConversationLogSerializer.toJson(log);

		String after = InterruptedTranscript.close(before);

		assertEquals("The turn keeps its id", turnId(before), turnId(after));
		Turn closed = ConversationLogSerializer.fromJson(after).turns().get(0);
		assertEquals("Leg den Beleg an.", ((Part.TextPart) closed.userParts().get(0)).text());
		List<Part> parts = closed.assistantParts();
		assertEquals(5, parts.size());
		assertEquals("Ich schaue nach.", ((Part.TextPart) parts.get(0)).text());

		Part.ToolCallPart read = (Part.ToolCallPart) parts.get(1);
		assertEquals(Part.ToolCallStatus.COMPLETED, read.status());
		assertEquals("3 Belege", read.result());

		Part.ToolCallPart asked = (Part.ToolCallPart) parts.get(2);
		assertEquals("A call that was running has an unknown outcome",
			Part.ToolCallStatus.FAILED, asked.status());
		assertEquals(InterruptedTranscript.INTERRUPTED_CALL, asked.errorMessage());

		Part.ApprovalPart approval = (Part.ApprovalPart) parts.get(3);
		assertEquals("ap", approval.id());
		assertEquals(Part.ApprovalDecision.INTERRUPTED, approval.decision());

		Part.SystemPart notice = (Part.SystemPart) parts.get(4);
		assertEquals(Part.SystemEventType.INTERRUPTED, notice.eventType());
		assertEquals("Stamped with the turn's last moment, not the time of the sweep",
			T0.plusSeconds(4), notice.timestamp());
	}

	/**
	 * The writes of a transaction that never reached its commit are retracted - including the
	 * write that opened it, which is recorded before the part saying it began - while the writes of
	 * an earlier iteration, whose transaction was committed, stay.
	 */
	public void testOpenTransactionIsRolledBack() throws IOException {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn(T0);
		turn.setUserParts(List.of(new Part.TextPart("u1", T0, "Leg zwei Belege an.")));
		// First iteration: a write whose transaction was committed.
		turn.addAssistantPart(written("first", T0.plusSeconds(1)));
		turn.addAssistantPart(new Part.TransactionPart("tx1", T0.plusSeconds(2),
			Part.TransactionType.BEGAN, -1, null));
		turn.addAssistantPart(new Part.TransactionPart("tx1c", T0.plusSeconds(3),
			Part.TransactionType.COMMITTED, 42, null));
		// Second iteration: a write that opened a transaction, then a read, then an approval the
		// process ended during.
		turn.addAssistantPart(written("second", T0.plusSeconds(4)));
		turn.addAssistantPart(new Part.TransactionPart("tx2", T0.plusSeconds(5),
			Part.TransactionType.BEGAN, -1, null));
		turn.addAssistantPart(Part.ToolCallPart.started("look", T0.plusSeconds(6), "lookup", "{}", ToolMode.READ)
			.completed("ok", Duration.ofMillis(5)));
		turn.addAssistantPart(Part.ApprovalPart.pending("ap", T0.plusSeconds(7), "third", "create", "{}",
			"Changes shared data.", ToolMode.DATA_WRITE));

		Turn closed = ConversationLogSerializer.fromJson(
			InterruptedTranscript.close(ConversationLogSerializer.toJson(log))).turns().get(0);

		assertEquals("A committed write is persisted and stays so",
			Part.ToolCallStatus.COMPLETED, call(closed, "first").status());
		Part.ToolCallPart second = call(closed, "second");
		assertEquals("The write of the open transaction never reached the database",
			Part.ToolCallStatus.ROLLED_BACK, second.status());
		assertEquals(InterruptedTranscript.INTERRUPTED_ROLLBACK, second.errorMessage());
		assertEquals("A read wrote nothing to take back",
			Part.ToolCallStatus.COMPLETED, call(closed, "look").status());

		List<Part> parts = closed.assistantParts();
		Part.TransactionPart rollback = (Part.TransactionPart) parts.get(parts.size() - 2);
		assertEquals(Part.TransactionType.ROLLED_BACK, rollback.type());
		assertEquals(Part.SystemEventType.INTERRUPTED,
			((Part.SystemPart) parts.get(parts.size() - 1)).eventType());

		// Continued later, the model is told the truth about the retracted write.
		Conversation replayed = new Conversation();
		ConversationLogReplay.rebuildMemory(ConversationLogSerializer.fromJson(
			InterruptedTranscript.close(ConversationLogSerializer.toJson(log))), replayed);
		ToolState secondResult = resultOf(replayed.messages(), "second");
		assertTrue("Replayed as a failure, not as a write that happened: " + secondResult,
			secondResult instanceof ToolState.Failed);
	}

	/**
	 * A turn whose transactions were all closed gets no rollback.
	 */
	public void testClosedTransactionIsNotRolledBack() throws IOException {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn(T0);
		turn.addAssistantPart(written("first", T0.plusSeconds(1)));
		turn.addAssistantPart(new Part.TransactionPart("tx1", T0.plusSeconds(2),
			Part.TransactionType.BEGAN, -1, null));
		turn.addAssistantPart(new Part.TransactionPart("tx1c", T0.plusSeconds(3),
			Part.TransactionType.COMMITTED, 42, null));

		Turn closed = ConversationLogSerializer.fromJson(
			InterruptedTranscript.close(ConversationLogSerializer.toJson(log))).turns().get(0);

		assertEquals(Part.ToolCallStatus.COMPLETED, call(closed, "first").status());
		assertFalse(closed.assistantParts().stream().anyMatch(part -> part instanceof Part.TransactionPart tx
			&& tx.type() == Part.TransactionType.ROLLED_BACK));
	}

	/**
	 * A swept run's subagent that was still at work is ended as failed: no subagent survives a
	 * restart, and its card must not say it is working for ever.
	 */
	public void testARunningSubagentIsEnded() throws IOException {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn(T0);
		turn.setUserParts(List.of(new Part.TextPart("u1", T0, "Recherchiere im Hintergrund.")));
		turn.addAssistantPart(subagent("running", "sa-1", "RUNNING"));
		turn.addAssistantPart(subagent("done", "sa-2", "DONE"));

		Turn closed = ConversationLogSerializer.fromJson(
			InterruptedTranscript.close(ConversationLogSerializer.toJson(log))).turns().get(0);

		Part.SubagentPart running = (Part.SubagentPart) closed.findPart("running");
		assertEquals("FAILED", running.status());
		assertEquals(InterruptedTranscript.INTERRUPTED_SUBAGENT, running.summary());
		assertEquals("run-sa-1", running.childRunId());
		assertEquals("DONE", ((Part.SubagentPart) closed.findPart("done")).status());
	}

	/**
	 * A conversation read back from the database: a subagent part an earlier process left at work is
	 * ended, one this process recorded is left to its subagent.
	 */
	public void testOnlySubagentsOfAnEarlierProcessAreEnded() {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn(T0);
		turn.addAssistantPart(subagent("old", "sa-1", "RUNNING"));
		turn.addAssistantPart(new Part.SubagentPart("new", T0.plusSeconds(3600), "sa-2", "worker", "task", "",
			null, "RUNNING", null));

		int ended = InterruptedTranscript.endInterruptedSubagents(log, T0.plusSeconds(60));

		assertEquals(1, ended);
		assertEquals("FAILED", ((Part.SubagentPart) turn.findPart("old")).status());
		assertEquals("RUNNING", ((Part.SubagentPart) turn.findPart("new")).status());
	}

	private static Part.SubagentPart subagent(String id, String subagentId, String status) {
		return new Part.SubagentPart(id, T0.plusSeconds(1), subagentId, "worker", "task", "call-1",
			"run-" + subagentId, status, "DONE".equals(status) ? "found it" : null);
	}

	/**
	 * Nothing recorded, nothing to close: a row without a checkpoint stays without one.
	 */
	public void testNoTranscript() throws IOException {
		assertNull(InterruptedTranscript.close(null));
		assertEquals("", InterruptedTranscript.close(""));
	}

	private static Part.ToolCallPart written(String id, Instant at) {
		return Part.ToolCallPart.started(id, at, "create", "{}", ToolMode.DATA_WRITE)
			.completed("created", Duration.ofMillis(10));
	}

	private static Part.ToolCallPart call(Turn turn, String id) {
		return (Part.ToolCallPart) turn.findPart(id);
	}

	private static ToolState resultOf(List<Message> messages, String toolCallId) {
		for (Message message : messages) {
			if (message instanceof ToolResultMessage result && result.toolCallId().equals(toolCallId)) {
				return result.state();
			}
		}
		fail("No result for " + toolCallId);
		return null;
	}

	private static String turnId(String transcript) throws IOException {
		return PersistedConversationV2.readPersistedConversationV2(new JsonReader(new StringR(transcript)))
			.getTurns().get(0).getId();
	}
}
