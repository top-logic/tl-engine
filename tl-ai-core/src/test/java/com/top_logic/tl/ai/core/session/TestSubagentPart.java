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
		assertEquals(Part.SubagentStatus.RUNNING, started.status());
		assertEquals("The UI places the card by the same call id.", "call-1",
			((AgentEvent.SubagentStarted) forwarded.get(0)).parentToolCallId());

		// The lead's turn is over before the subagent ends; the part is ended where it was written.
		log.completeTurn();
		log.startTurn();
		listener.recordSubagentCompleted("sa-1",
			new SubagentResult("X is 42.", TokenUsage.EMPTY, 10L, 1, StopReason.COMPLETE), "run-1");

		Part.SubagentPart ended = onlySubagentPart(turn);
		assertEquals(Part.SubagentStatus.DONE, ended.status());
		assertEquals("X is 42.", ended.summary());
		assertEquals("run-1", ended.childRunId());
		assertEquals(started.id(), ended.id());
	}

	/**
	 * A subagent that waits for a free slot is recorded as queued, and as running once it starts, so a
	 * reloaded card does not call a waiting subagent busy (#834).
	 */
	public void testAQueuedSubagentIsRecordedAsQueuedUntilItStarts() {
		ConversationLog log = new ConversationLog();
		ConversationLogListener listener = new ConversationLogListener(log);
		Turn turn = log.startTurn();

		listener.recordSubagentStarted("sa-1", "researcher", "find X", "call-1", true);
		assertEquals(Part.SubagentStatus.QUEUED, onlySubagentPart(turn).status());

		listener.recordSubagentRunning("sa-1");
		assertEquals(Part.SubagentStatus.RUNNING, onlySubagentPart(turn).status());

		listener.recordSubagentCompleted("sa-1",
			new SubagentResult("X is 42.", TokenUsage.EMPTY, 10L, 1, StopReason.COMPLETE), "run-1");
		assertEquals(Part.SubagentStatus.DONE, onlySubagentPart(turn).status());
	}

	/**
	 * A queued subagent that gets its slot is announced, before any step of its own: its first step may
	 * be a request for approval, which the chat does not show on its card. A subagent that started at
	 * once is not announced a second time.
	 */
	public void testAQueuedSubagentIsAnnouncedWhenItStarts() {
		ConversationLog log = new ConversationLog();
		ConversationLogListener listener = new ConversationLogListener(log);
		List<AgentEvent> forwarded = new ArrayList<>();
		listener.setParentListener(forwarded::add);
		Turn turn = log.startTurn();
		listener.recordSubagentStarted("sa-1", "researcher", "find X", "call-1", true);
		listener.recordSubagentStarted("sa-2", "researcher", "find Y", "call-2", false);

		listener.recordSubagentRunning("sa-1");
		listener.recordSubagentRunning("sa-2");

		List<AgentEvent.SubagentRunning> running = new ArrayList<>();
		for (AgentEvent event : forwarded) {
			if (event instanceof AgentEvent.SubagentRunning e) {
				running.add(e);
			}
		}
		assertEquals(1, running.size());
		assertEquals("sa-1", running.get(0).subagentId());
		assertEquals("The UI finds the card by the call that started it.", "call-1",
			running.get(0).parentToolCallId());
		assertEquals("The part is written before the event is sent.", Part.SubagentStatus.RUNNING,
			((Part.SubagentPart) turn.assistantParts().get(0)).status());

		listener.recordSubagentRunning("sa-1");
		assertEquals("A start is announced once.", 3, forwarded.size());
	}

	/**
	 * A subagent started while no turn was current has no part, but the chat was told it waits; it is
	 * told when it starts as well.
	 */
	public void testAQueuedSubagentWithoutAPartIsAnnounced() {
		ConversationLogListener listener = new ConversationLogListener(new ConversationLog());
		List<AgentEvent> forwarded = new ArrayList<>();
		listener.setParentListener(forwarded::add);
		listener.recordSubagentStarted("sa-1", "researcher", "find X", "call-1", true);

		listener.recordSubagentRunning("sa-1");

		AgentEvent.SubagentRunning running = (AgentEvent.SubagentRunning) forwarded.get(1);
		assertEquals("sa-1", running.subagentId());
		assertEquals("", running.parentToolCallId());
	}

	/** A subagent cancelled while it waited never starts, and is not announced as running. */
	public void testASubagentCancelledInTheQueueIsNotAnnouncedAsRunning() {
		ConversationLog log = new ConversationLog();
		ConversationLogListener listener = new ConversationLogListener(log);
		List<AgentEvent> forwarded = new ArrayList<>();
		listener.setParentListener(forwarded::add);
		log.startTurn();
		listener.recordSubagentStarted("sa-1", "researcher", "find X", "call-1", true);
		listener.recordSubagentCancelled("sa-1", null);

		listener.recordSubagentRunning("sa-1");

		assertEquals(2, forwarded.size());
		assertTrue(forwarded.get(1) instanceof AgentEvent.SubagentCancelled);
	}

	/**
	 * The recorded names are the state's names, so transcripts written before the state was an enum
	 * load as they were; a name this version does not know is read as at work, which the startup
	 * sweep closes once its process is gone, never as an end it did not have.
	 */
	public void testTheRecordedNamesAreKept() {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		turn.addAssistantPart(new Part.SubagentPart("p-1", java.time.Instant.now(), "sa-1", "a", "t", "", null,
			Part.SubagentStatus.LIMIT, "partial"));
		log.completeTurn();
		String json = ConversationLogSerializer.toJson(log);
		assertTrue(json, json.contains("\"LIMIT\""));

		for (Part.SubagentStatus status : Part.SubagentStatus.values()) {
			String written = json.replace("\"LIMIT\"", "\"" + status.name() + "\"");
			assertEquals(status, onlySubagentPart(ConversationLogSerializer.fromJson(written).turns().get(0)).status());
		}
		String unknown = json.replace("\"LIMIT\"", "\"PAUSED\"");
		assertEquals(Part.SubagentStatus.RUNNING,
			onlySubagentPart(ConversationLogSerializer.fromJson(unknown).turns().get(0)).status());
		assertEquals(Part.SubagentStatus.RUNNING, Part.SubagentStatus.fromName(null));
	}

	/** Which states are an end - the set the startup sweep and the chat's cards both go by. */
	public void testWhichStatesHaveEnded() {
		assertFalse(Part.SubagentStatus.QUEUED.hasEnded());
		assertFalse(Part.SubagentStatus.RUNNING.hasEnded());
		assertTrue(Part.SubagentStatus.DONE.hasEnded());
		assertTrue(Part.SubagentStatus.LIMIT.hasEnded());
		assertTrue(Part.SubagentStatus.FAILED.hasEnded());
		assertTrue(Part.SubagentStatus.CANCELLED.hasEnded());
	}

	/** A subagent stopped in order at a limit is recorded as such, so its card says so (#786). */
	public void testAStopAtALimitIsRecordedAsSuch() {
		ConversationLog log = new ConversationLog();
		ConversationLogListener listener = new ConversationLogListener(log);
		Turn turn = log.startTurn();
		listener.recordSubagentStarted("sa-1", "researcher", "find X", "call-1");
		listener.recordSubagentStarted("sa-2", "researcher", "find Y", "call-2");

		listener.recordSubagentCompleted("sa-1",
			new SubagentResult("X is 4..", TokenUsage.EMPTY, 10L, 8, StopReason.MAX_TURNS), "run-1");
		listener.recordSubagentCompleted("sa-2",
			new SubagentResult("Y is 7.", TokenUsage.EMPTY, 10L, 1, StopReason.COMPLETE), "run-2");

		List<Part.SubagentStatus> states = new ArrayList<>();
		for (Part part : turn.assistantParts()) {
			if (part instanceof Part.SubagentPart subagent) {
				states.add(subagent.status());
			}
		}
		assertEquals(List.of(Part.SubagentStatus.LIMIT, Part.SubagentStatus.DONE), states);
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
			running.ended(Part.SubagentStatus.DONE, "X is 42.", "run-1"));

		Turn read = ConversationLogSerializer.fromJson(rewritten).turns().get(0);
		Part.SubagentPart ended = onlySubagentPart(read);
		assertEquals(Part.SubagentStatus.DONE, ended.status());
		assertEquals("run-1", ended.childRunId());
		assertEquals(running.id(), ended.id());
		assertEquals("Bitte zählen", ((Part.TextPart) read.userParts().get(0)).text());
		assertEquals(2, read.assistantParts().size());

		assertNull("A transcript without the part is left alone.",
			ConversationLogSerializer.replacePart(recorded,
				new Part.SubagentPart("other", java.time.Instant.now(), "sa-9", "a", "t", "", null,
					Part.SubagentStatus.DONE, null)));
	}

	/** A long report is cut: the part points at the transcript, it does not copy the report. */
	public void testThePartKeepsOnlyTheStartOfTheReport() {
		ConversationLog log = new ConversationLog();
		ConversationLogListener listener = new ConversationLogListener(log);
		Turn turn = log.startTurn();
		listener.recordSubagentStarted("sa-1", "researcher", "find X", "call-1");

		listener.recordSubagentFailed("sa-1", "e".repeat(1000), null);

		Part.SubagentPart ended = onlySubagentPart(turn);
		assertEquals(Part.SubagentStatus.FAILED, ended.status());
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
		assertEquals(Part.SubagentStatus.CANCELLED, part.status());

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
