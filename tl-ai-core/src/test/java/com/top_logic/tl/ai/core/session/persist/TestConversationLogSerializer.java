/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session.persist;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;

/**
 * Tests for {@link ConversationLogSerializer}.
 */
public class TestConversationLogSerializer extends TestCase {

	/** Round-trip: serialize then deserialize preserves all data. */
	public void testRoundTrip() {
		ConversationLog log = new ConversationLog();

		// Turn 1: user message + tool call + text response
		Turn turn1 = log.startTurn();
		turn1.setModel("claude-sonnet-4-20250514");
		turn1.setProvider("anthropic");
		turn1.setUserParts(List.of(
			new Part.TextPart("u1", Instant.ofEpochMilli(1000), "What is 2+2?")));

		Part.ToolCallPart tc = Part.ToolCallPart.started("tc1", Instant.ofEpochMilli(1001),
			"calculator", "{\"expr\":\"2+2\"}");
		turn1.addAssistantPart(tc.completed("4", Duration.ofMillis(10)));
		turn1.addAssistantPart(new Part.TextPart("p1", Instant.ofEpochMilli(1002), "The answer is 4."));
		turn1.addUsage(new TokenUsage(100, 50, 0, 80, 20));
		log.completeTurn();

		// Turn 2: approval + denial
		Turn turn2 = log.startTurn();
		turn2.setModel("claude-sonnet-4-20250514");
		turn2.setProvider("anthropic");
		turn2.setUserParts(List.of(
			new Part.TextPart("u2", Instant.ofEpochMilli(2000), "Delete /tmp/test")));

		Part.ApprovalPart approval = Part.ApprovalPart.pending("apr1",
			Instant.ofEpochMilli(2001), "tc2", "deleteFile", "{\"path\":\"/tmp/test\"}",
			"Delete file");
		turn2.addAssistantPart(approval.resolve(Part.ApprovalDecision.DENIED,
			"too risky", Instant.ofEpochMilli(2002)));
		turn2.addAssistantPart(new Part.TextPart("p2", Instant.ofEpochMilli(2003),
			"I won't delete that file."));
		turn2.addUsage(new TokenUsage(120, 30, 0, 100, 0));
		log.completeTurn();

		// Serialize
		String json = ConversationLogSerializer.toJson(log);
		assertNotNull(json);
		assertTrue(json.length() > 0);

		// Deserialize
		ConversationLog restored = ConversationLogSerializer.fromJson(json);
		assertEquals(2, restored.turns().size());

		// Verify turn 1
		Turn r1 = restored.turns().get(0);
		assertEquals(1, r1.turnNumber());
		assertEquals("claude-sonnet-4-20250514", r1.model());
		assertEquals("anthropic", r1.provider());
		assertEquals(100, r1.cumulativeUsage().inputTokens());
		assertEquals(1, r1.userParts().size());
		assertEquals("What is 2+2?", ((Part.TextPart) r1.userParts().get(0)).text());
		assertEquals(2, r1.assistantParts().size());

		Part.ToolCallPart rtc = (Part.ToolCallPart) r1.assistantParts().get(0);
		assertEquals("calculator", rtc.toolName());
		assertEquals(Part.ToolCallStatus.COMPLETED, rtc.status());
		assertEquals("4", rtc.result());
		assertEquals(10L, rtc.durationMs());

		// Verify turn 2
		Turn r2 = restored.turns().get(1);
		Part.ApprovalPart rap = (Part.ApprovalPart) r2.assistantParts().get(0);
		assertEquals(Part.ApprovalDecision.DENIED, rap.decision());
		assertEquals("too risky", rap.userComment());
	}

	/** Empty log round-trips correctly. */
	public void testEmptyLog() {
		ConversationLog log = new ConversationLog();
		String json = ConversationLogSerializer.toJson(log);
		ConversationLog restored = ConversationLogSerializer.fromJson(json);
		assertTrue(restored.turns().isEmpty());
	}

	/**
	 * A retracted tool call round-trips, including its reason and the result it retracts.
	 *
	 * <p>
	 * The persisted status is a string, so the new value is additive: an older log simply never
	 * carries it.
	 * </p>
	 */
	public void testRolledBackToolCallRoundTrip() {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		turn.setUserParts(List.of(new Part.TextPart("u1", Instant.now(), "Create a document")));
		Part.ToolCallPart tc = Part.ToolCallPart.started("tc1", Instant.ofEpochMilli(1000),
			"createDocument", "{\"name\":\"Q3\"}");
		turn.addAssistantPart(tc.completed("{uuid=4f2c}", Duration.ofMillis(10))
			.rolledBack("Database operation failed."));
		turn.addAssistantPart(new Part.TransactionPart("tx1", Instant.ofEpochMilli(1001),
			Part.TransactionType.ROLLED_BACK, -1, "Database operation failed."));
		turn.addUsage(TokenUsage.EMPTY);
		log.completeTurn();

		ConversationLog restored = ConversationLogSerializer.fromJson(
			ConversationLogSerializer.toJson(log));
		List<Part> parts = restored.turns().get(0).assistantParts();

		Part.ToolCallPart rtc = (Part.ToolCallPart) parts.get(0);
		assertEquals(Part.ToolCallStatus.ROLLED_BACK, rtc.status());
		assertEquals("Database operation failed.", rtc.errorMessage());
		assertEquals("{uuid=4f2c}", rtc.result());
		assertEquals(10L, rtc.durationMs());

		Part.TransactionPart rtx = (Part.TransactionPart) parts.get(1);
		assertEquals(Part.TransactionType.ROLLED_BACK, rtx.type());
		assertEquals("Database operation failed.", rtx.reason());
	}

	/** InteractionPart with options round-trips. */
	public void testInteractionRoundTrip() {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		turn.setUserParts(List.of(new Part.TextPart("u1", Instant.now(), "Help")));
		Part.InteractionPart ip = Part.InteractionPart.pending("i1", Instant.ofEpochMilli(3000),
			"choice", "Pick one", "Select", List.of("A", "B", "C"), false);
		turn.addAssistantPart(ip.resolve("B", Instant.ofEpochMilli(3001)));
		turn.addUsage(TokenUsage.EMPTY);
		log.completeTurn();

		ConversationLog restored = ConversationLogSerializer.fromJson(
			ConversationLogSerializer.toJson(log));
		Part.InteractionPart rip = (Part.InteractionPart) restored.turns().get(0).assistantParts().get(0);
		assertEquals("choice", rip.interactionType());
		assertEquals("Pick one", rip.question());
		assertEquals(3, rip.options().size());
		assertEquals("B", rip.userResponse());
	}
}
