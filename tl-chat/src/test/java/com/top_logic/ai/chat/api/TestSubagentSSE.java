/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.subagent.SubagentInbox;
import com.top_logic.tl.ai.core.cost.Money;
import com.top_logic.tl.ai.core.agent.subagent.SubagentNews;
import com.top_logic.tl.ai.core.agent.subagent.SubagentResult;
import com.top_logic.tl.ai.core.message.TokenUsage;

/**
 * Verifies that subagent {@link AgentEvent} variants map to the correct subagent_* SSE event names.
 */
public class TestSubagentSSE extends TestCase {

	/** */
	public void testSubagentStartedMapsToSse() {
		List<String> emitted = new ArrayList<>();
		ChatSSEHandler handler = ChatSSEHandler.recordingForTest(emitted::add);

		handler.onEventForTest(new AgentEvent.SubagentStarted(
			"run1", 1, Instant.now(), "sa-1", "TL_ModelExplorer", "find X", "call-1", false));
		handler.flushForTest();
		assertTrue("Expected subagent_started event", emitted.contains("subagent_started"));

		handler.onEventForTest(new AgentEvent.SubagentCompleted(
			"run1", 2, Instant.now(), "sa-1",
			new SubagentResult("done", TokenUsage.EMPTY, 12L, 1, StopReason.COMPLETE), "call-1", "child-1"));
		handler.flushForTest();
		assertTrue("Expected subagent_completed event", emitted.contains("subagent_completed"));
	}

	/** */
	public void testSubagentFailedMapsToSse() {
		List<String> emitted = new ArrayList<>();
		ChatSSEHandler handler = ChatSSEHandler.recordingForTest(emitted::add);

		handler.onEventForTest(new AgentEvent.SubagentFailed(
			"run1", 1, Instant.now(), "sa-2", "timeout", "call-2", ""));
		assertTrue("Expected subagent_failed event", emitted.contains("subagent_failed"));
	}

	/** */
	public void testSubagentCancelledMapsToSse() {
		List<String> emitted = new ArrayList<>();
		ChatSSEHandler handler = ChatSSEHandler.recordingForTest(emitted::add);

		handler.onEventForTest(new AgentEvent.SubagentCancelled(
			"run1", 1, Instant.now(), "sa-3", "", ""));
		assertTrue("Expected subagent_cancelled event", emitted.contains("subagent_cancelled"));
	}

	/** A subagent's end says which call started it and its run, so a window that did not see it start finds the card. */
	public void testTheEndSaysWhereTheCardIs() {
		List<String> frames = new ArrayList<>();
		ChatSSEHandler handler = frames(frames);

		handler.onEventForTest(new AgentEvent.SubagentCompleted("run1", 2, Instant.now(), "sa-1",
			new SubagentResult("done", TokenUsage.EMPTY, 12L, 1, StopReason.COMPLETE), "call-1", "child-1"));

		assertEquals(1, frames.size());
		assertTrue(frames.get(0), frames.get(0).startsWith("subagent_completed "));
		assertTrue(frames.get(0), frames.get(0).contains("\"parentToolCallId\":\"call-1\""));
		assertTrue(frames.get(0), frames.get(0).contains("\"childRunId\":\"child-1\""));
	}

	/** A lead turn the subagents started says which ones and why. */
	public void testTheWakeNamesTheSubagents() {
		List<String> frames = new ArrayList<>();
		ChatSSEHandler handler = frames(frames);

		handler.onEventForTest(new AgentEvent.LeadWoken("", 0, Instant.now(), 1, 1, List.of(
			new SubagentNews("sa-1", "Counter", SubagentNews.Reason.FINISHED, "call-1", ""),
			new SubagentNews("sa-2", "Writer", SubagentNews.Reason.ASKS, "call-2", "Welche?"))));

		assertEquals(1, frames.size());
		String frame = frames.get(0);
		assertTrue(frame, frame.startsWith("lead_woken "));
		assertTrue(frame, frame.contains("\"subagentId\":\"sa-1\""));
		assertTrue(frame, frame.contains("\"reason\":\"FINISHED\""));
		assertTrue(frame, frame.contains("\"reason\":\"ASKS\""));
		assertTrue(frame, frame.contains("\"question\":\"Welche?\""));
		assertTrue(frame, frame.contains("\"parentToolCallId\":\"call-2\""));
	}

	/** What the user wrote from a card has been read: the card is told which messages, by id. */
	public void testAReadUserMessageReachesTheCard() {
		List<String> frames = new ArrayList<>();
		ChatSSEHandler handler = frames(frames);
		SubagentInbox.Message message = SubagentInbox.Message.of(SubagentInbox.Sender.USER, "Nur \"2026\".");

		handler.onEventForTest(new AgentEvent.DelegatedEvent("child", 2, Instant.now(), "sa-1", "",
			new AgentEvent.UserMessageDelivered("child", 2, Instant.now(), "[Message from the user] ...",
				List.of(message))));

		assertEquals(1, frames.size());
		String frame = frames.get(0);
		assertTrue(frame, frame.startsWith("delegation_event "));
		assertTrue(frame, frame.contains("\"subagentId\":\"sa-1\""));
		assertTrue(frame, frame.contains("\"eventType\":\"user_message_read\""));
		assertTrue("The card's receipt is keyed by the id the send answered with: " + frame,
			frame.contains(message.id()));
	}

	/** What a subagent's run cost reaches the browser with its end, for the panel's sum (#783). */
	public void testASubagentsRunCostReachesThePanel() {
		List<String> frames = new ArrayList<>();
		ChatSSEHandler handler = frames(frames);

		handler.onEventForTest(new AgentEvent.DelegatedEvent("child", 2, Instant.now(), "sa-1", "",
			new AgentEvent.RunCompleted("child", 2, Instant.now(), StopReason.COMPLETE, TokenUsage.EMPTY,
				new Money(0.0125, "EUR"))));

		assertEquals(1, frames.size());
		String frame = frames.get(0);
		assertTrue(frame, frame.contains("run_completed"));
		assertTrue(frame, frame.contains("totalCost") && frame.contains("0.0125"));
		assertTrue(frame, frame.contains("EUR"));
	}

	private static ChatSSEHandler frames(List<String> sink) {
		return new ChatSSEHandler(null, null, null, null, null, "test") {
			@Override
			void sendEvent(String eventType, String data) {
				sink.add(eventType + " " + data);
			}
		};
	}
}
