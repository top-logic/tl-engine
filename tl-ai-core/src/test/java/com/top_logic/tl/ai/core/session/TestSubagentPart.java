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

	/** A subagent stopped in order at a limit is recorded as such, so its card says so (#786). */
	public void testAStopAtALimitIsRecordedAsSuch() {
		ConversationLog log = new ConversationLog();
		ConversationLogListener listener = new ConversationLogListener(log);
		Turn turn = log.startTurn();
		listener.recordSubagentStarted("sa-1", "researcher", "find X", "call-1");
		listener.recordSubagentStarted("sa-2", "researcher", "find Y", "call-2");

		listener.recordSubagentCompleted("sa-1",
			new SubagentResult("X is 4..", TokenUsage.EMPTY, 10L, 8, StopReason.MAX_TOOL_CALLS), "run-1");
		listener.recordSubagentCompleted("sa-2",
			new SubagentResult("Y is 7.", TokenUsage.EMPTY, 10L, 1, StopReason.COMPLETE), "run-2");

		List<String> states = new ArrayList<>();
		for (Part part : turn.assistantParts()) {
			if (part instanceof Part.SubagentPart subagent) {
				states.add(subagent.status());
			}
		}
		assertEquals(List.of("LIMIT", "DONE"), states);
	}

	/**
	 * The end is announced with the call that started the subagent and its run, so a window that did
	 * not see the start finds the card and the transcript.
	 */
	public void testTheEndSaysWhereTheSubagentWasStarted() {
		ConversationLog log = new ConversationLog();
		ConversationLogListener listener = new ConversationLogListener(log);
		List<AgentEvent> forwarded = new ArrayList<>();
		listener.setParentListener(forwarded::add);
		log.startTurn();
		listener.recordSubagentStarted("sa-1", "researcher", "find X", "call-1");
		listener.recordSubagentStarted("sa-2", "researcher", "find Y", "call-2");
		listener.recordSubagentStarted("sa-3", "researcher", "find Z", "call-3");

		listener.recordSubagentCompleted("sa-1",
			new SubagentResult("X is 42.", TokenUsage.EMPTY, 10L, 1, StopReason.COMPLETE), "run-1");
		listener.recordSubagentFailed("sa-2", "boom", "run-2");
		listener.recordSubagentCancelled("sa-3", null);

		AgentEvent.SubagentCompleted completed = (AgentEvent.SubagentCompleted) forwarded.get(3);
		assertEquals("call-1", completed.parentToolCallId());
		assertEquals("run-1", completed.childRunId());
		AgentEvent.SubagentFailed failed = (AgentEvent.SubagentFailed) forwarded.get(4);
		assertEquals("call-2", failed.parentToolCallId());
		assertEquals("run-2", failed.childRunId());
		AgentEvent.SubagentCancelled cancelled = (AgentEvent.SubagentCancelled) forwarded.get(5);
		assertEquals("call-3", cancelled.parentToolCallId());
		assertEquals("A subagent that never ran has no run.", "", cancelled.childRunId());
	}

	/**
	 * A recorded transcript takes the ended part in place of the running one, and keeps everything
	 * else as it was written.
	 */
	public void testTheEndedPartReplacesTheRecordedOne() throws Exception {
		ConversationLog log = new ConversationLog();
		ConversationLogListener listener = new ConversationLogListener(log);
		Turn turn = log.startTurn();
		turn.setUserParts(List.of(new Part.TextPart("u-1", java.time.Instant.now(), "Bitte zählen")));
		listener.recordSubagentStarted("sa-1", "researcher", "find X", "call-1");
		turn.addAssistantPart(new Part.TextPart("t-1", java.time.Instant.now(), "Ich habe gestartet."));
		log.completeTurn();
		String recorded = ConversationLogSerializer.toJson(log.turns());
		Part.SubagentPart running = onlySubagentPart(turn);

		String rewritten = ConversationLogSerializer.replacePart(recorded,
			running.ended("DONE", "X is 42.", "run-1"));

		Turn read = ConversationLogSerializer.fromJson(rewritten).turns().get(0);
		Part.SubagentPart ended = onlySubagentPart(read);
		assertEquals("DONE", ended.status());
		assertEquals("run-1", ended.childRunId());
		assertEquals(running.id(), ended.id());
		assertEquals("Bitte zählen", ((Part.TextPart) read.userParts().get(0)).text());
		assertEquals(2, read.assistantParts().size());

		assertNull("A transcript without the part is left alone.",
			ConversationLogSerializer.replacePart(recorded,
				new Part.SubagentPart("other", java.time.Instant.now(), "sa-9", "a", "t", "", null, "DONE", null)));
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
