/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.memory;

import java.time.Instant;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.CompactionMarker;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.SystemMessage;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ThinkingContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;

/**
 * Tests for {@link ByteBasedTokenEstimator}.
 */
public class TestByteBasedTokenEstimator extends TestCase {

	private static final Instant T0 = Instant.parse("2026-04-06T12:00:00Z");

	private final TokenEstimator _estimator = new ByteBasedTokenEstimator();

	// Per-message overhead = 4, per-request priming = 3
	// Single message estimate = content + 4 overhead
	// List estimate = 3 priming + sum(per-message)

	/** Simple ASCII text: bytes / 4 + overhead. "Hello, world!" = 13/4=3 + 4=7. */
	public void testUserMessageText() {
		UserMessage msg = new UserMessage("u1", T0,
			List.of(new TextContent("Hello, world!")));
		assertEquals(3 + 4, _estimator.estimateTokens(msg));
	}

	/** Multi-byte UTF-8: "H\u00e9llo" = 6 bytes / 4 = 1 + 4 overhead = 5. */
	public void testUtf8Text() {
		UserMessage msg = new UserMessage("u1", T0,
			List.of(new TextContent("H\u00e9llo")));
		assertEquals(1 + 4, _estimator.estimateTokens(msg));
	}

	/** Image content is a fixed 2000 tokens + overhead. */
	public void testImageContent() {
		UserMessage msg = new UserMessage("u1", T0,
			List.of(new ImageContent("data:image/png;base64,abc", "image/png")));
		assertEquals(2000 + 4, _estimator.estimateTokens(msg));
	}

	/** Mixed content: text + image + overhead. */
	public void testMixedContent() {
		UserMessage msg = new UserMessage("u1", T0,
			List.of(new TextContent("test"), new ImageContent("url", "image/png")));
		// "test" = 4/4=1, image = 2000, overhead = 4
		assertEquals(1 + 2000 + 4, _estimator.estimateTokens(msg));
	}

	/** AssistantMessage with text and tool call. JSON args use 2 bytes/token. */
	public void testAssistantWithToolCall() {
		// {"path":"a.txt"} = 16 bytes / 2 = 8 (JSON ratio)
		// "read_file" = 9/4=2, "Hi" = 2/4=0
		ToolCall tc = new ToolCall("tc1", "read_file", "{\"path\":\"a.txt\"}");
		AssistantMessage msg = new AssistantMessage("a1", T0,
			List.of(new TextContent("Hi"), tc),
			TokenUsage.EMPTY, "tool_use");
		int expected = 0 + 2 + 8 + 4; // Hi(0) + name(2) + args-json(8) + overhead(4)
		assertEquals(expected, _estimator.estimateTokens(msg));
	}

	/** ThinkingContent is estimated like text + overhead. */
	public void testThinkingContent() {
		AssistantMessage msg = new AssistantMessage("a1", T0,
			List.of(new ThinkingContent("01234567890123456789", null), new TextContent("Hi")),
			TokenUsage.EMPTY, "stop");
		// thinking = 20/4=5, Hi = 0, overhead = 4
		assertEquals(5 + 0 + 4, _estimator.estimateTokens(msg));
	}

	/** SystemMessage text content + overhead. */
	public void testSystemMessage() {
		SystemMessage msg = new SystemMessage("s1", T0, "You are helpful.");
		// "You are helpful." = 16/4=4, overhead = 4
		assertEquals(4 + 4, _estimator.estimateTokens(msg));
	}

	/** ToolResultMessage with completed result + overhead. */
	public void testToolResultCompleted() {
		ToolResultMessage msg = new ToolResultMessage("tr1", T0, "tc1", "read_file",
			new ToolState.Completed("The file contains: data", T0, T0, false));
		// "The file contains: data" = 23/4=5, overhead = 4
		assertEquals(5 + 4, _estimator.estimateTokens(msg));
	}

	/** ToolResultMessage with failed state + overhead. */
	public void testToolResultFailed() {
		ToolResultMessage msg = new ToolResultMessage("tr1", T0, "tc1", "read_file",
			new ToolState.Failed("File not found", T0, T0));
		// "File not found" = 14/4=3, overhead = 4
		assertEquals(3 + 4, _estimator.estimateTokens(msg));
	}

	/** CompactionMarker is 0 tokens (metadata, not sent to LLM). */
	public void testCompactionMarker() {
		CompactionMarker marker = new CompactionMarker("cm1", T0, 5, 10000, "auto");
		assertEquals(0, _estimator.estimateTokens(marker));
	}

	/** List estimation: priming(3) + sum(per-message with overhead). */
	public void testListEstimation() {
		UserMessage u = new UserMessage("u1", T0,
			List.of(new TextContent("Hello, world!")));
		SystemMessage s = new SystemMessage("s1", T0, "You are helpful.");
		// priming(3) + user(3+4) + system(4+4) = 3 + 7 + 8 = 18
		assertEquals(18, _estimator.estimateTokens(List.of(u, s)));
	}

	/** Empty message list is 0 tokens (no priming). */
	public void testEmptyList() {
		assertEquals(0, _estimator.estimateTokens(List.of()));
	}
}
