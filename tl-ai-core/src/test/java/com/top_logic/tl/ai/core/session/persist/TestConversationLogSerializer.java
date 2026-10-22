/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session.persist;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.CompactionMarker;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.PdfContent;
import com.top_logic.tl.ai.core.message.SystemMessage;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ThinkingContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.session.Citation;
import com.top_logic.tl.ai.core.session.CompactionFingerprint;
import com.top_logic.tl.ai.core.session.CompactionSidecar;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Tests for {@link ConversationLogSerializer}.
 */
public class TestConversationLogSerializer extends TestCase {

	/**
	 * What the host stated about a turn survives the round trip, apart from the user's text.
	 *
	 * <p>
	 * The two are stored separately on purpose: the bubble shows the one the user wrote, the
	 * transcript shows both.
	 * </p>
	 */
	public void testTheTurnContextRoundTrips() {
		String block = "<turn-context>\nTurn: 1\nOpen invoice: RE-2026-0042\n</turn-context>";
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		turn.setTurnContext(block);
		turn.setUserParts(List.of(
			new Part.TextPart("u1", Instant.ofEpochMilli(1000), "Is the payment term okay?")));
		log.completeTurn();

		Turn restored =
			ConversationLogSerializer.fromJson(ConversationLogSerializer.toJson(log)).turns().get(0);

		assertEquals(block, restored.turnContext());
		assertEquals("The user's text must carry no context.",
			"Is the payment term okay?", ((Part.TextPart) restored.userParts().get(0)).text());
	}

	/**
	 * A turn read back began when it began, not when it was read (#568).
	 *
	 * <p>
	 * The chat measures a finished turn's "worked for" line from the turn's start to its last part.
	 * Stamped with the moment of reading, every turn of a chat opened from the database started
	 * after its own answer, and no turn had a line.
	 * </p>
	 */
	public void testTheTurnKeepsItsStart() {
		Instant started = Instant.parse("2026-09-24T10:00:00Z");
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn(started);
		turn.setUserParts(List.of(new Part.TextPart("u1", started, "Hello")));
		turn.addAssistantPart(new Part.TextPart("a1", started.plusSeconds(19), "Hi."));
		log.completeTurn();

		Turn restored =
			ConversationLogSerializer.fromJson(ConversationLogSerializer.toJson(log)).turns().get(0);

		assertEquals(started, restored.timestamp());
		assertEquals(started.plusSeconds(19), restored.assistantParts().get(0).timestamp());
	}

	/** A transcript written before the field existed has no context, not an empty one. */
	public void testATurnWithoutContextRestoresNone() {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		turn.setUserParts(List.of(new Part.TextPart("u1", Instant.ofEpochMilli(1000), "Hello")));
		log.completeTurn();

		assertNull(ConversationLogSerializer.fromJson(ConversationLogSerializer.toJson(log))
			.turns().get(0).turnContext());
	}

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
			"calculator", "{\"expr\":\"2+2\"}", ToolMode.READ);
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
			"Delete file", ToolMode.DATA_WRITE);
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
		assertEquals(ToolMode.READ, rtc.mode());
		assertEquals(Part.ToolCallStatus.COMPLETED, rtc.status());
		assertEquals("4", rtc.result());
		assertEquals(10L, rtc.durationMs());

		// Verify turn 2
		Turn r2 = restored.turns().get(1);
		Part.ApprovalPart rap = (Part.ApprovalPart) r2.assistantParts().get(0);
		assertEquals(Part.ApprovalDecision.DENIED, rap.decision());
		assertEquals("too risky", rap.userComment());
	}

	/**
	 * A synchronous delegation keeps the lead's call it belongs to, so the chat draws its card there
	 * after a reload (#703).
	 */
	public void testDelegationKeepsItsCall() {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		Part.DelegationPart started = Part.DelegationPart.started("d1", Instant.now(), "lead", "researcher",
			"find X", "call-7");
		started.childParts().add(Part.ToolCallPart.started("t1", Instant.now(), "webSearch", "{}", ToolMode.READ));
		turn.addAssistantPart(started.completed("X is 42.", "child-run", 1200L, 2,
			new TokenUsage(100, 50, 0, 0, 0), com.top_logic.tl.ai.core.agent.StopReason.COMPLETE));
		log.completeTurn();

		ConversationLog restored = ConversationLogSerializer.fromJson(ConversationLogSerializer.toJson(log.turns()));

		Part.DelegationPart part = (Part.DelegationPart) restored.turns().get(0).assistantParts().get(0);
		assertEquals("call-7", part.parentToolCallId());
		assertEquals("X is 42.", part.resultSummary());
		assertEquals(1, part.childParts().size());
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
			"createDocument", "{\"name\":\"Q3\"}", ToolMode.DATA_WRITE);
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
		assertEquals(ToolMode.DATA_WRITE, rtc.mode());
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

	/**
	 * A tool call read from a log that predates the mode field restores with an unknown mode.
	 *
	 * <p>
	 * The literal below is the shape a log written before
	 * {@link com.top_logic.tl.ai.core.session.Part.ToolCallPart#mode()} existed has on disk: the
	 * {@code toolCallData} object simply has no {@code mode} member. Deserializing it must not fail
	 * and must not invent a mode - {@code null} is what
	 * {@code ConversationLogListener#retractCompletedWrites} treats as a write.
	 * </p>
	 */
	public void testToolCallWithoutModeFieldRestoresAsUnknown() {
		String legacyJson = "{\"turns\":[{"
			+ "\"id\":\"t1\",\"turnNumber\":1,\"timestamp\":1000,"
			+ "\"assistantParts\":[{"
			+ "\"id\":\"tc1\",\"timestamp\":1001,\"type\":\"tool_call\","
			+ "\"toolCallData\":{\"toolName\":\"createDocument\",\"argsJson\":\"{}\","
			+ "\"status\":\"COMPLETED\",\"result\":\"{uuid=4f2c}\",\"durationMs\":10}"
			+ "}]}]}";

		ConversationLog restored = ConversationLogSerializer.fromJson(legacyJson);
		Part.ToolCallPart rtc =
			(Part.ToolCallPart) restored.turns().get(0).assistantParts().get(0);
		assertEquals("createDocument", rtc.toolName());
		assertEquals(Part.ToolCallStatus.COMPLETED, rtc.status());
		assertNull("A log without the field must not claim to know the mode.", rtc.mode());
	}

	/**
	 * Every {@link ToolMode} survives a round trip, the newest constant included.
	 *
	 * <p>
	 * The persisted shape carries the mode as its {@link Enum#name()} and reads it back with
	 * {@code ToolMode.valueOf}, so a constant added to the enum needs nothing here - but only as
	 * long as nobody replaces that with a hand-written mapping. Asserted over
	 * {@link ToolMode#values()} so the guarantee is the enum's, not a list's.
	 * </p>
	 */
	public void testEveryModeRoundTrips() {
		for (ToolMode mode : ToolMode.values()) {
			ConversationLog log = new ConversationLog();
			Turn turn = log.startTurn();
			turn.setUserParts(List.of(new Part.TextPart("u1", Instant.ofEpochMilli(1000), "Do it")));
			turn.addAssistantPart(Part.ToolCallPart.started("tc1", Instant.ofEpochMilli(1001),
				"aTool", "{}", mode).completed("done", Duration.ofMillis(3)));
			turn.addUsage(TokenUsage.EMPTY);
			log.completeTurn();

			ConversationLog restored = ConversationLogSerializer.fromJson(
				ConversationLogSerializer.toJson(log));
			Part.ToolCallPart rtc =
				(Part.ToolCallPart) restored.turns().get(0).assistantParts().get(0);

			assertEquals(mode, rtc.mode());
			assertEquals(Part.ToolCallStatus.COMPLETED, rtc.status());
		}
	}

	/** An unknown mode survives a round trip as unknown rather than becoming a value. */
	public void testUnknownModeRoundTripsAsUnknown() {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		turn.setUserParts(List.of(new Part.TextPart("u1", Instant.ofEpochMilli(1000), "Do it")));
		turn.addAssistantPart(Part.ToolCallPart.started("tc1", Instant.ofEpochMilli(1001),
			"unresolvedTool", "{}", null));
		turn.addUsage(TokenUsage.EMPTY);
		log.completeTurn();

		ConversationLog restored = ConversationLogSerializer.fromJson(
			ConversationLogSerializer.toJson(log));
		Part.ToolCallPart rtc =
			(Part.ToolCallPart) restored.turns().get(0).assistantParts().get(0);
		assertNull(rtc.mode());
	}

	/**
	 * A compaction's sidecar round-trips, with every message shape a preserve zone can hold.
	 *
	 * <p>
	 * The replacement messages are the one thing in the log that cannot be derived from
	 * anything else - they are what the summarizer wrote - so a lossy round trip here loses
	 * history outright rather than merely costing tokens.
	 * </p>
	 */
	public void testCompactionSidecarRoundTrip() {
		List<Message> replacement = List.of(
			new CompactionMarker("m1", Instant.ofEpochMilli(5000), 12, 9000, "proactive"),
			new UserMessage("s1", Instant.ofEpochMilli(5001),
				List.of(new TextContent("Summary of the earlier conversation."),
					new ImageContent("https://example.org/chart.png", "image/png"),
					new PdfContent("JVBERi0=", "application/pdf"))),
			new AssistantMessage("a1", Instant.ofEpochMilli(5002),
				List.of(new ThinkingContent("Considering.", "sig-42"),
					new TextContent("Calling the tool."),
					new ToolCall("tc9", "search", "{\"q\":\"x\"}")),
				new TokenUsage(7, 8, 9, 10, 11), "tool_use"),
			new ToolResultMessage("r1", Instant.ofEpochMilli(5003), "tc9", "search",
				new ToolState.Completed("found", Instant.ofEpochMilli(5004),
					Instant.ofEpochMilli(5005), true, "shortened", 128,
					List.of(new TextContent("found")))),
			new ToolResultMessage("r2", Instant.ofEpochMilli(5006), "tc8", "write",
				new ToolState.Failed("denied", Instant.ofEpochMilli(5007),
					Instant.ofEpochMilli(5008), true)),
			new SystemMessage("y1", Instant.ofEpochMilli(5009), "Sub-agent finished."));

		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		turn.setUserParts(List.of(new Part.TextPart("u1", Instant.ofEpochMilli(1000), "Go")));
		turn.addAssistantPart(new Part.CompactionPart("c1", Instant.ofEpochMilli(1001),
			9000, 900, "proactive",
			new CompactionSidecar(CompactionFingerprint.VERSION, "abc123", replacement)));
		turn.addUsage(TokenUsage.EMPTY);
		log.completeTurn();

		ConversationLog restored = ConversationLogSerializer.fromJson(
			ConversationLogSerializer.toJson(log));
		Part.CompactionPart rcp =
			(Part.CompactionPart) restored.turns().get(0).assistantParts().get(0);
		assertEquals(9000, rcp.tokensBefore());
		assertEquals(900, rcp.tokensAfter());
		assertEquals("proactive", rcp.trigger());

		CompactionSidecar sidecar = rcp.sidecar();
		assertNotNull("The sidecar must survive storage - it is the only copy.", sidecar);
		assertEquals(CompactionFingerprint.VERSION, sidecar.fingerprintVersion());
		assertEquals("abc123", sidecar.fingerprint());
		assertEquals(replacement, sidecar.replacementMessages());
	}

	/**
	 * A compaction read from a log that predates the sidecar restores without one.
	 *
	 * <p>
	 * The literal below is the shape a log written before
	 * {@link com.top_logic.tl.ai.core.session.Part.CompactionPart#sidecar()} existed has on disk:
	 * {@code compactionData} simply has no {@code sidecar} member. Deserializing it must not fail
	 * and must not invent a sidecar - {@code null} is what makes {@code ConversationLogReplay}
	 * rebuild the full history instead.
	 * </p>
	 */
	public void testCompactionWithoutSidecarFieldRestoresWithoutOne() {
		String legacyJson = "{\"turns\":[{"
			+ "\"id\":\"t1\",\"turnNumber\":1,\"timestamp\":1000,"
			+ "\"assistantParts\":[{"
			+ "\"id\":\"c1\",\"timestamp\":1001,\"type\":\"compaction\","
			+ "\"compactionData\":{\"tokensBefore\":9000,\"tokensAfter\":900,"
			+ "\"trigger\":\"proactive\"}"
			+ "}]}]}";

		ConversationLog restored = ConversationLogSerializer.fromJson(legacyJson);
		Part.CompactionPart rcp =
			(Part.CompactionPart) restored.turns().get(0).assistantParts().get(0);
		assertEquals(9000, rcp.tokensBefore());
		assertEquals("proactive", rcp.trigger());
		assertNull("A log without the field must not claim to have a sidecar.", rcp.sidecar());
	}

	/** A compaction whose sidecar is still unwritten round-trips without one. */
	public void testRunningCompactionRoundTripsWithoutSidecar() {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		turn.setUserParts(List.of(new Part.TextPart("u1", Instant.ofEpochMilli(1000), "Go")));
		turn.addAssistantPart(Part.CompactionPart.started("c1", Instant.ofEpochMilli(1001),
			9000, "reactive"));
		turn.addUsage(TokenUsage.EMPTY);
		log.completeTurn();

		ConversationLog restored = ConversationLogSerializer.fromJson(
			ConversationLogSerializer.toJson(log));
		Part.CompactionPart rcp =
			(Part.CompactionPart) restored.turns().get(0).assistantParts().get(0);
		assertNull(rcp.sidecar());
		assertEquals(0, rcp.tokensAfter());
	}

	/**
	 * A sidecar this version cannot read costs the session its compacted size, not its log.
	 *
	 * <p>
	 * The literal carries a message type from a hypothetical newer writer. Dropping just the
	 * sidecar leaves the canonical history intact, which is all a replay actually needs;
	 * letting the failure out of the codec would lose the whole session instead.
	 * </p>
	 */
	public void testUnreadableSidecarIsDroppedNotFatal() {
		String json = "{\"turns\":[{"
			+ "\"id\":\"t1\",\"turnNumber\":1,\"timestamp\":1000,"
			+ "\"userParts\":[{\"id\":\"u1\",\"timestamp\":1000,\"type\":\"text\","
			+ "\"textData\":{\"text\":\"Go\"}}],"
			+ "\"assistantParts\":[{"
			+ "\"id\":\"c1\",\"timestamp\":1001,\"type\":\"compaction\","
			+ "\"compactionData\":{\"tokensBefore\":9000,\"tokensAfter\":900,"
			+ "\"trigger\":\"proactive\",\"sidecar\":{\"fingerprintVersion\":1,"
			+ "\"fingerprint\":\"abc\",\"replacementMessages\":["
			+ "{\"id\":\"m1\",\"timestamp\":1002,\"type\":\"telepathy\"}]}}"
			+ "}]}]}";

		ConversationLog restored = ConversationLogSerializer.fromJson(json);
		assertEquals(1, restored.turns().size());
		Part.CompactionPart rcp =
			(Part.CompactionPart) restored.turns().get(0).assistantParts().get(0);
		assertNull("An unreadable sidecar must be dropped.", rcp.sidecar());
		assertEquals("The canonical log around it must survive intact.", "Go",
			((Part.TextPart) restored.turns().get(0).userParts().get(0)).text());
	}

	/** Citations on a text part survive a serialize/deserialize round-trip. */
	public void testCitationsRoundTrip() {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		turn.setUserParts(List.of(new Part.TextPart("u1", Instant.ofEpochMilli(1000), "Cite this")));
		turn.addAssistantPart(new Part.TextPart("p1", Instant.ofEpochMilli(1001), "Fact [1].",
			List.of(new Citation(1, "cite.cite", "c1", "web", "https://ok.example", "Ok",
				"https://ok.example"))));
		log.completeTurn();

		ConversationLog restored = ConversationLogSerializer.fromJson(ConversationLogSerializer.toJson(log));
		Part.TextPart text = (Part.TextPart) restored.turns().get(0).assistantParts().get(0);
		assertEquals(1, text.citations().size());
		Citation citation = text.citations().get(0);
		assertEquals(1, citation.index());
		assertEquals("web", citation.kind());
		assertEquals("https://ok.example", citation.id());
		assertEquals("Ok", citation.label());
	}
}
