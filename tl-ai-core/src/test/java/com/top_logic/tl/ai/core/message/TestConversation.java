/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import java.time.Instant;
import java.util.List;

import junit.framework.TestCase;

/**
 * Tests for {@link Conversation}.
 */
public class TestConversation extends TestCase {

	private static final Instant T0 = Instant.parse("2026-04-04T12:00:00Z");
	private static final Instant T1 = Instant.parse("2026-04-04T12:00:01Z");
	private static final Instant T2 = Instant.parse("2026-04-04T12:00:02Z");

	/** New conversation is empty. */
	public void testEmpty() {
		Conversation conv = new Conversation();
		assertEquals(0, conv.size());
		assertTrue(conv.messages().isEmpty());
	}

	/** Add message increases size. */
	public void testAdd() {
		Conversation conv = new Conversation();
		conv.add(new SystemMessage("s1", T0, "prompt"));
		conv.add(new UserMessage("u1", T1, List.of(new TextContent("hi"))));
		assertEquals(2, conv.size());
	}

	/** Null message is rejected by add. */
	public void testAddNullRejected() {
		Conversation conv = new Conversation();
		try {
			conv.add(null);
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
	}

	/** Messages returns unmodifiable snapshot. */
	public void testMessagesUnmodifiable() {
		Conversation conv = new Conversation();
		conv.add(new SystemMessage("s1", T0, "prompt"));
		List<Message> snapshot = conv.messages();
		try {
			snapshot.add(new SystemMessage("s2", T1, "nope"));
			fail("Expected UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
			// expected
		}
	}

	/** replaceRange replaces a range of messages. */
	public void testReplaceRange() {
		Conversation conv = new Conversation();
		conv.add(new SystemMessage("s1", T0, "prompt"));
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("old1"))));
		conv.add(new AssistantMessage("a1", T0, List.of(new TextContent("old2")),
			TokenUsage.EMPTY, "stop"));
		conv.add(new UserMessage("u2", T1, List.of(new TextContent("keep"))));

		// Replace messages at index 1-2 with a summary + marker
		SystemMessage summary = new SystemMessage("sum", T2, "Summary of old conversation");
		CompactionMarker marker = new CompactionMarker("cm", T2, 2, 5000, "auto");
		conv.replaceRange(1, 3, List.of(marker, summary));

		assertEquals(4, conv.size());
		assertEquals("s1", conv.messages().get(0).id()); // original prompt
		assertEquals("cm", conv.messages().get(1).id()); // marker
		assertEquals("sum", conv.messages().get(2).id()); // summary
		assertEquals("u2", conv.messages().get(3).id()); // kept
	}

	/** replaceRange with empty replacement deletes messages. */
	public void testReplaceRangeDelete() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("a"))));
		conv.add(new UserMessage("u2", T1, List.of(new TextContent("b"))));
		conv.add(new UserMessage("u3", T2, List.of(new TextContent("c"))));

		conv.replaceRange(0, 2, List.of());
		assertEquals(1, conv.size());
		assertEquals("u3", conv.messages().get(0).id());
	}

	/** Invalid range throws IndexOutOfBoundsException. */
	public void testReplaceRangeInvalidRange() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("hi"))));
		try {
			conv.replaceRange(0, 5, List.of());
			fail("Expected IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			// expected
		}
	}

	/** Null replacement throws NullPointerException. */
	public void testReplaceRangeNullReplacement() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("hi"))));
		try {
			conv.replaceRange(0, 1, null);
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
	}

	/** lastAssistantUsage returns usage from most recent AssistantMessage. */
	public void testLastAssistantUsage() {
		Conversation conv = new Conversation();
		TokenUsage usage1 = new TokenUsage(100, 50, 0, 0, 0);
		TokenUsage usage2 = new TokenUsage(200, 80, 0, 0, 0);
		conv.add(new AssistantMessage("a1", T0, List.of(new TextContent("hi")),
			usage1, "stop"));
		conv.add(new UserMessage("u1", T1, List.of(new TextContent("next"))));
		conv.add(new AssistantMessage("a2", T2, List.of(new TextContent("hello")),
			usage2, "stop"));

		assertEquals(usage2, conv.lastAssistantUsage().orElse(null));
	}

	/** lastAssistantUsage returns empty for no assistant messages. */
	public void testLastAssistantUsageEmpty() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("hi"))));
		assertTrue(conv.lastAssistantUsage().isEmpty());
	}

	/** totalUsage sums all assistant message usages. */
	public void testTotalUsage() {
		Conversation conv = new Conversation();
		conv.add(new AssistantMessage("a1", T0, List.of(new TextContent("hi")),
			new TokenUsage(100, 50, 0, 10, 5), "stop"));
		conv.add(new AssistantMessage("a2", T1, List.of(new TextContent("hello")),
			new TokenUsage(200, 80, 50, 20, 10), "stop"));

		TokenUsage total = conv.totalUsage();
		assertEquals(300, total.inputTokens());
		assertEquals(130, total.outputTokens());
		assertEquals(50, total.thinkingTokens());
		assertEquals(30, total.cacheReadTokens());
		assertEquals(15, total.cacheWriteTokens());
	}
}
