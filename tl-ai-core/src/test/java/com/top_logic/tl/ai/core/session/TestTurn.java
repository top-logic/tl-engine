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

import com.top_logic.tl.ai.core.message.TokenUsage;

/**
 * Tests for {@link Turn}.
 */
public class TestTurn extends TestCase {

	/** A turn starts empty and accumulates parts. */
	public void testAddParts() {
		Turn turn = new Turn("t1", 1);
		assertTrue(turn.assistantParts().isEmpty());

		Part.TextPart text = new Part.TextPart("p1", Instant.now(), "Hello");
		turn.addAssistantPart(text);
		assertEquals(1, turn.assistantParts().size());
		assertEquals("Hello", ((Part.TextPart) turn.assistantParts().get(0)).text());
	}

	/** Parts can be updated in-place by ID. */
	public void testUpdatePart() {
		Turn turn = new Turn("t1", 1);
		Part.ToolCallPart started = Part.ToolCallPart.started("tc1", Instant.now(),
			"readFile", "{}");
		turn.addAssistantPart(started);
		assertEquals(Part.ToolCallStatus.RUNNING,
			((Part.ToolCallPart) turn.findPart("tc1")).status());

		Part.ToolCallPart completed = started.completed("contents", Duration.ofMillis(50));
		turn.updatePart("tc1", completed);
		assertEquals(Part.ToolCallStatus.COMPLETED,
			((Part.ToolCallPart) turn.findPart("tc1")).status());
	}

	/** Observer is notified on add and update. */
	public void testObserverNotification() {
		Turn turn = new Turn("t1", 1);
		int[] counts = {0, 0}; // [added, updated]
		turn.addObserver(new Turn.Observer() {
			@Override
			public void onPartAdded(Turn t, Part part) { counts[0]++; }
			@Override
			public void onPartUpdated(Turn t, Part part) { counts[1]++; }
		});

		turn.addAssistantPart(new Part.TextPart("p1", Instant.now(), "Hi"));
		assertEquals(1, counts[0]);
		assertEquals(0, counts[1]);

		Part.ToolCallPart tc = Part.ToolCallPart.started("tc1", Instant.now(), "read", "{}");
		turn.addAssistantPart(tc);
		assertEquals(2, counts[0]);

		turn.updatePart("tc1", tc.completed("done", Duration.ofMillis(10)));
		assertEquals(2, counts[0]);
		assertEquals(1, counts[1]);
	}

	/** Model and provider metadata can be set. */
	public void testMetadata() {
		Turn turn = new Turn("t1", 1);
		turn.setModel("claude-sonnet-4-20250514");
		turn.setProvider("anthropic");
		turn.addUsage(new TokenUsage(100, 50, 0, 80, 20));

		assertEquals("claude-sonnet-4-20250514", turn.model());
		assertEquals("anthropic", turn.provider());
		assertEquals(100, turn.cumulativeUsage().inputTokens());
	}

	/** Usage accumulates across multiple AgentLoop turns within one Turn. */
	public void testUsageAccumulation() {
		Turn turn = new Turn("t1", 1);
		turn.addUsage(new TokenUsage(100, 50, 0, 0, 0));
		turn.addUsage(new TokenUsage(80, 30, 10, 0, 0));
		assertEquals(180, turn.cumulativeUsage().inputTokens());
		assertEquals(80, turn.cumulativeUsage().outputTokens());
		assertEquals(10, turn.cumulativeUsage().thinkingTokens());
	}

	/** User parts are set once at creation. */
	public void testUserParts() {
		Turn turn = new Turn("t1", 1);
		Part.TextPart userMsg = new Part.TextPart("u1", Instant.now(), "What is 2+2?");
		turn.setUserParts(List.of(userMsg));
		assertEquals(1, turn.userParts().size());
	}

	/** findPart returns null for unknown ID. */
	public void testFindPartMissing() {
		Turn turn = new Turn("t1", 1);
		assertNull(turn.findPart("nonexistent"));
	}
}
