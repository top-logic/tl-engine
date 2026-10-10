/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.subagent.SubagentResult;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.session.persist.ConversationLogSerializer;

/**
 * The lead's log keeps one {@link Part.SubagentPart} per background subagent its turn started
 * (issue #702): written when the subagent starts, ended in place when it ends - also once the turn is
 * over - and kept across a reload. It is a pointer for the chat's card, not conversation content.
 */
public class TestSubagentPart extends TestCase {

	/** Started, the part names the subagent and the call that started it; ended, how it ended. */
	public void testThePartFollowsTheSubagent() {
		ConversationLog log = new ConversationLog();
		ConversationLogListener listener = new ConversationLogListener(log);
		List<AgentEvent> forwarded = new ArrayList<>();
		listener.setParentListener(forwarded::add);
		Turn turn = log.startTurn();

		listener.recordSubagentStarted("sa-1", "researcher", "find X", "call-1");

		Part.SubagentPart started = onlySubagentPart(turn);
		assertEquals("sa-1", started.subagentId());
		assertEquals("call-1", started.parentToolCallId());
		assertEquals("RUNNING", started.status());
		assertEquals("The UI places the card by the same call id.", "call-1",
			((AgentEvent.SubagentStarted) forwarded.get(0)).parentToolCallId());

		// The lead's turn is over before the subagent ends; the part is ended where it was written.
		log.completeTurn();
		log.startTurn();
		listener.recordSubagentCompleted("sa-1",
			new SubagentResult("X is 42.", TokenUsage.EMPTY, 10L, 1, StopReason.COMPLETE), "run-1");

		Part.SubagentPart ended = onlySubagentPart(turn);
		assertEquals("DONE", ended.status());
		assertEquals("X is 42.", ended.summary());
		assertEquals("run-1", ended.childRunId());
		assertEquals(started.id(), ended.id());
	}

	/** A long report is cut: the part points at the transcript, it does not copy the report. */
	public void testThePartKeepsOnlyTheStartOfTheReport() {
		ConversationLog log = new ConversationLog();
		ConversationLogListener listener = new ConversationLogListener(log);
		Turn turn = log.startTurn();
		listener.recordSubagentStarted("sa-1", "researcher", "find X", "call-1");

		listener.recordSubagentFailed("sa-1", "e".repeat(1000), null);

		Part.SubagentPart ended = onlySubagentPart(turn);
		assertEquals("FAILED", ended.status());
		assertTrue(ended.summary().length() < 300);
	}

	/** The part survives the persisted form, and replay does not turn it into a message. */
	public void testThePartIsPersistedAndNotReplayed() {
		ConversationLog log = new ConversationLog();
		ConversationLogListener listener = new ConversationLogListener(log);
		log.startTurn();
		listener.recordSubagentStarted("sa-1", "researcher", "find X", "call-1");
		listener.recordSubagentCancelled("sa-1", "run-1");
		log.completeTurn();

		ConversationLog reloaded = ConversationLogSerializer.fromJson(ConversationLogSerializer.toJson(log));

		Part.SubagentPart part = onlySubagentPart(reloaded.turns().get(0));
		Part.SubagentPart written = onlySubagentPart(log.turns().get(0));
		// The persisted form keeps milliseconds, so the parts are compared by what they say.
		assertEquals(written.id(), part.id());
		assertEquals(written.timestamp().toEpochMilli(), part.timestamp().toEpochMilli());
		assertEquals("sa-1", part.subagentId());
		assertEquals("researcher", part.agentName());
		assertEquals("find X", part.task());
		assertEquals("call-1", part.parentToolCallId());
		assertEquals("run-1", part.childRunId());
		assertEquals("CANCELLED", part.status());

		Conversation conversation = new Conversation();
		ConversationLogReplay.rebuildMemory(reloaded, conversation);
		assertTrue("A pointer for the chat is not conversation content: " + conversation.messages(),
			conversation.messages().isEmpty());
	}

	private static Part.SubagentPart onlySubagentPart(Turn turn) {
		List<Part.SubagentPart> parts = new ArrayList<>();
		for (Part part : turn.assistantParts()) {
			if (part instanceof Part.SubagentPart subagent) {
				parts.add(subagent);
			}
		}
		assertEquals(1, parts.size());
		return parts.get(0);
	}
}
