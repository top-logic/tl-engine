/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import java.time.Instant;
import java.util.List;

import junit.framework.TestCase;

/**
 * Tests for {@link Message} sealed interface and all 5 subtypes.
 */
public class TestMessage extends TestCase {

	private static final Instant NOW = Instant.parse("2026-04-04T12:00:00Z");

	/** UserMessage stores id, timestamp, and content. */
	public void testUserMessage() {
		List<ContentPart> content = List.of(new TextContent("Hello"));
		UserMessage msg = new UserMessage("u1", NOW, content);
		assertEquals("u1", msg.id());
		assertEquals(NOW, msg.timestamp());
		assertEquals(1, msg.content().size());
		assertEquals("Hello", ((TextContent) msg.content().get(0)).text());
	}

	/** UserMessage content list is unmodifiable. */
	public void testUserMessageContentUnmodifiable() {
		UserMessage msg = new UserMessage("u1", NOW, List.of(new TextContent("hi")));
		try {
			msg.content().add(new TextContent("nope"));
			fail("Expected UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
			// expected
		}
	}

	/** AssistantMessage stores content, usage, and finishReason. */
	public void testAssistantMessage() {
		List<ContentPart> content = List.of(
			new TextContent("I'll read the file"),
			new ToolCall("tc_1", "read_file", "{\"path\":\"foo.txt\"}")
		);
		TokenUsage usage = new TokenUsage(100, 50, 0, 0, 0);
		AssistantMessage msg = new AssistantMessage("a1", NOW, content, usage, "tool_calls");

		assertEquals("a1", msg.id());
		assertEquals(2, msg.content().size());
		assertEquals(usage, msg.usage());
		assertEquals("tool_calls", msg.finishReason());
	}

	/** ToolResultMessage stores toolCallId, toolName, and state. */
	public void testToolResultMessage() {
		Instant start = Instant.parse("2026-04-04T12:00:01Z");
		Instant end = Instant.parse("2026-04-04T12:00:02Z");
		ToolState state = new ToolState.Completed("file contents", start, end, false);
		ToolResultMessage msg = new ToolResultMessage("tr1", NOW, "tc_1", "read_file", state);

		assertEquals("tr1", msg.id());
		assertEquals("tc_1", msg.toolCallId());
		assertEquals("read_file", msg.toolName());
		assertTrue(msg.state() instanceof ToolState.Completed);
	}

	/** SystemMessage stores content string. */
	public void testSystemMessage() {
		SystemMessage msg = new SystemMessage("s1", NOW, "You are a helpful assistant.");
		assertEquals("s1", msg.id());
		assertEquals("You are a helpful assistant.", msg.content());
	}

	/** CompactionMarker stores metadata. */
	public void testCompactionMarker() {
		CompactionMarker msg = new CompactionMarker("c1", NOW, 15, 50000, "auto");
		assertEquals("c1", msg.id());
		assertEquals(15, msg.messagesSummarized());
		assertEquals(50000, msg.tokensBefore());
		assertEquals("auto", msg.trigger());
	}

	/** All 5 types are Message instances. */
	public void testSealedInterface() {
		Message[] messages = {
			new UserMessage("u1", NOW, List.of(new TextContent("hi"))),
			new AssistantMessage("a1", NOW, List.of(new TextContent("hello")),
				TokenUsage.EMPTY, "stop"),
			new ToolResultMessage("tr1", NOW, "tc_1", "tool",
				new ToolState.Pending("{}")),
			new SystemMessage("s1", NOW, "prompt"),
			new CompactionMarker("c1", NOW, 5, 10000, "manual")
		};
		for (Message msg : messages) {
			assertNotNull(msg.id());
			assertNotNull(msg.timestamp());
		}
	}

	/** Switch exhaustiveness over Message subtypes. */
	public void testSwitchExhaustive() {
		Message msg = new UserMessage("u1", NOW, List.of(new TextContent("test")));
		String type = switch (msg) {
			case UserMessage u -> "user";
			case AssistantMessage a -> "assistant";
			case ToolResultMessage t -> "tool_result";
			case SystemMessage s -> "system";
			case CompactionMarker c -> "compaction";
		};
		assertEquals("user", type);
	}
}
