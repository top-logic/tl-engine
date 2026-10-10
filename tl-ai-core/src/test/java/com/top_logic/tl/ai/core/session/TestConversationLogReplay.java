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

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Tests for {@link ConversationLogReplay}.
 */
public class TestConversationLogReplay extends TestCase {

	/** User text and assistant text are replayed into the conversation. */
	public void testBasicReplay() {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		turn.setUserParts(List.of(new Part.TextPart("u1", Instant.now(), "Hello")));
		turn.addAssistantPart(new Part.TextPart("p1", Instant.now(), "Hi there!"));
		turn.addUsage(TokenUsage.EMPTY);
		log.completeTurn();

		Conversation conv = new Conversation();
		ConversationLogReplay.rebuildMemory(log, conv);

		// user + assistant messages
		assertEquals(2, conv.messages().size());
		assertTrue(conv.messages().get(0) instanceof UserMessage);
		assertTrue(conv.messages().get(1) instanceof AssistantMessage);

		UserMessage um = (UserMessage) conv.messages().get(0);
		assertEquals("Hello", ((TextContent) um.content().get(0)).text());

		AssistantMessage am = (AssistantMessage) conv.messages().get(1);
		assertEquals("Hi there!", ((TextContent) am.content().get(0)).text());
	}

	/**
	 * A reloaded turn carries what the host stated about it, ahead of the user's text.
	 *
	 * <p>
	 * Exactly where the live run put it, because a reloaded conversation has to be the one the model
	 * already saw - the compaction sidecars are fingerprinted against this projection.
	 * </p>
	 */
	public void testTheTurnContextIsReplayedBesideTheUsersText() {
		String block = "<turn-context>\nTurn: 1\nOpen invoice: RE-2026-0042\n</turn-context>";
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		turn.setTurnContext(block);
		turn.setUserParts(List.of(new Part.TextPart("u1", Instant.now(), "Is the payment term okay?")));
		turn.addAssistantPart(new Part.TextPart("p1", Instant.now(), "Yes."));
		turn.addUsage(TokenUsage.EMPTY);
		log.completeTurn();

		Conversation conv = new Conversation();
		ConversationLogReplay.rebuildMemory(log, conv);

		UserMessage um = (UserMessage) conv.messages().get(0);
		assertEquals(2, um.content().size());
		assertEquals(block, ((TextContent) um.content().get(0)).text());
		assertEquals("Is the payment term okay?", ((TextContent) um.content().get(1)).text());
	}

	/**
	 * A turn no user message started - it opens with subagents' news - replays its context block on
	 * that delivery, the newest user message the live run had.
	 */
	public void testTheTurnContextOfATurnWithoutUserTextIsReplayedOnTheDelivery() {
		String block = "<turn-context>\nTurn: 2\n</turn-context>";
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		turn.setTurnContext(block);
		turn.setUserParts(List.of());
		turn.addAssistantPart(new Part.SystemPart("s1", Instant.now(), Part.SystemEventType.SUBAGENT_RESULT,
			"[Subagent results] sa-1 found it"));
		turn.addAssistantPart(new Part.TextPart("p1", Instant.now(), "The subagent found it."));
		turn.addUsage(TokenUsage.EMPTY);
		log.completeTurn();

		Conversation conv = new Conversation();
		ConversationLogReplay.rebuildMemory(log, conv);

		assertEquals(2, conv.messages().size());
		UserMessage delivery = (UserMessage) conv.messages().get(0);
		assertEquals(2, delivery.content().size());
		assertEquals(block, ((TextContent) delivery.content().get(0)).text());
		assertEquals("[Subagent results] sa-1 found it", ((TextContent) delivery.content().get(1)).text());
	}

	/** Tool calls produce assistant(tool_use) + tool_result pairs. */
	public void testToolCallReplay() {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		turn.setUserParts(List.of(new Part.TextPart("u1", Instant.now(), "Read /tmp")));
		Part.ToolCallPart tc = Part.ToolCallPart.started("tc1", Instant.now(),
			"readFile", "{\"path\":\"/tmp\"}", ToolMode.READ);
		turn.addAssistantPart(tc.completed("contents", Duration.ofMillis(10)));
		turn.addAssistantPart(new Part.TextPart("p1", Instant.now(), "Here it is."));
		turn.addUsage(TokenUsage.EMPTY);
		log.completeTurn();

		Conversation conv = new Conversation();
		ConversationLogReplay.rebuildMemory(log, conv);

		// user + assistant(with ToolCall) + toolResult + assistant(text)
		assertTrue(conv.messages().size() >= 3);

		// Second message should be assistant with tool call
		AssistantMessage am1 = (AssistantMessage) conv.messages().get(1);
		assertTrue(am1.content().stream().anyMatch(c -> c instanceof ToolCall));

		// Third message should be tool result
		assertTrue(conv.messages().get(2) instanceof ToolResultMessage);
		ToolResultMessage trm = (ToolResultMessage) conv.messages().get(2);
		assertEquals("tc1", trm.toolCallId());
		assertTrue(trm.state() instanceof ToolState.Completed);
	}

	/** Denied tool calls produce denial messages for the LLM. */
	public void testDeniedToolReplay() {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		turn.setUserParts(List.of(new Part.TextPart("u1", Instant.now(), "Delete it")));
		turn.addAssistantPart(Part.ToolCallPart.denied("tc1", Instant.now(),
			"deleteFile", "{}", ToolMode.DATA_WRITE, "User denied: too dangerous"));
		turn.addAssistantPart(new Part.TextPart("p1", Instant.now(), "OK."));
		turn.addUsage(TokenUsage.EMPTY);
		log.completeTurn();

		Conversation conv = new Conversation();
		ConversationLogReplay.rebuildMemory(log, conv);

		// Should have a tool result with denial text
		boolean hasDenial = false;
		for (var m : conv.messages()) {
			if (m instanceof ToolResultMessage trm && trm.state() instanceof ToolState.Failed f) {
				if (f.error().contains("denied")) {
					hasDenial = true;
				}
			}
		}
		assertTrue("Expected denied tool result", hasDenial);
	}

	/**
	 * A retracted tool call is replayed as a failure, so a reloaded session does not hand the model
	 * a successful write for a change that was never persisted.
	 *
	 * <p>
	 * This is what makes the correction survive a page reload. The live
	 * {@code [Framework notice]} is not recorded as a part, so without this the model would re-read
	 * a green {@code createDocument} and cite its identifier again.
	 * </p>
	 */
	public void testRolledBackToolReplaysAsFailure() {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		turn.setUserParts(List.of(new Part.TextPart("u1", Instant.now(), "Create a document")));
		Part.ToolCallPart tc = Part.ToolCallPart.started("tc1", Instant.now(),
			"createDocument", "{\"name\":\"Q3\"}", ToolMode.DATA_WRITE);
		turn.addAssistantPart(tc.completed("{uuid=4f2c}", Duration.ofMillis(10))
			.rolledBack("Database operation failed."));
		turn.addUsage(TokenUsage.EMPTY);
		log.completeTurn();

		Conversation conv = new Conversation();
		ConversationLogReplay.rebuildMemory(log, conv);

		ToolResultMessage result = null;
		for (var m : conv.messages()) {
			if (m instanceof ToolResultMessage trm) {
				result = trm;
			}
		}
		assertNotNull("The tool call itself must still be replayed - the model made it.", result);
		assertTrue("A write that never landed must not come back as a success: " + result.state(),
			result.state() instanceof ToolState.Failed);
		String error = ((ToolState.Failed) result.state()).error();
		assertTrue("The replayed failure must say the changes were not persisted: " + error,
			error.contains("NOT persisted"));
		assertTrue("The reason must survive the reload: " + error,
			error.contains("Database operation failed."));
	}

	/** Approval parts are NOT replayed to the LLM. */
	public void testApprovalNotReplayed() {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		turn.setUserParts(List.of(new Part.TextPart("u1", Instant.now(), "Write")));
		turn.addAssistantPart(Part.ApprovalPart.pending("a1", Instant.now(), "tc1",
			"writeFile", "{}", "Write", ToolMode.DATA_WRITE).resolve(
				Part.ApprovalDecision.APPROVED, null, Instant.now()));
		turn.addAssistantPart(new Part.TextPart("p1", Instant.now(), "Done."));
		turn.addUsage(TokenUsage.EMPTY);
		log.completeTurn();

		Conversation conv = new Conversation();
		ConversationLogReplay.rebuildMemory(log, conv);

		// Should have user + assistant only, no approval content
		assertEquals(2, conv.messages().size());
		boolean hasApproval = false;
		for (var m : conv.messages()) {
			if (m instanceof AssistantMessage am) {
				for (var c : am.content()) {
					if (c instanceof TextContent tc && tc.text().contains("approval")) {
						hasApproval = true;
					}
				}
			}
		}
		assertFalse("Approvals should not be in LLM context", hasApproval);
	}
}
