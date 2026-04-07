/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
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

	/** Simple ASCII text: bytes / 4. "Hello, world!" = 13 bytes / 4 = 3. */
	public void testUserMessageText() {
		UserMessage msg = new UserMessage("u1", T0,
			List.of(new TextContent("Hello, world!")));
		assertEquals(3, _estimator.estimateTokens(msg));
	}

	/** Multi-byte UTF-8: "Hello" = 6 bytes / 4 = 1. */
	public void testUtf8Text() {
		UserMessage msg = new UserMessage("u1", T0,
			List.of(new TextContent("H\u00e9llo")));
		assertEquals(1, _estimator.estimateTokens(msg));
	}

	/** Image content is a fixed 2000 tokens regardless of URL. */
	public void testImageContent() {
		UserMessage msg = new UserMessage("u1", T0,
			List.of(new ImageContent("data:image/png;base64,abc", "image/png")));
		assertEquals(2000, _estimator.estimateTokens(msg));
	}

	/** Mixed content: text + image sums up. */
	public void testMixedContent() {
		UserMessage msg = new UserMessage("u1", T0,
			List.of(new TextContent("test"), new ImageContent("url", "image/png")));
		assertEquals(2001, _estimator.estimateTokens(msg));
	}

	/** AssistantMessage with text and tool call. */
	public void testAssistantWithToolCall() {
		ToolCall tc = new ToolCall("tc1", "read_file", "{\"path\":\"a.txt\"}");
		AssistantMessage msg = new AssistantMessage("a1", T0,
			List.of(new TextContent("Hi"), tc),
			TokenUsage.EMPTY, "tool_use");
		assertEquals(6, _estimator.estimateTokens(msg));
	}

	/** ThinkingContent is estimated like text. */
	public void testThinkingContent() {
		AssistantMessage msg = new AssistantMessage("a1", T0,
			List.of(new ThinkingContent("01234567890123456789"), new TextContent("Hi")),
			TokenUsage.EMPTY, "stop");
		assertEquals(5, _estimator.estimateTokens(msg));
	}

	/** SystemMessage text content. */
	public void testSystemMessage() {
		SystemMessage msg = new SystemMessage("s1", T0, "You are helpful.");
		assertEquals(4, _estimator.estimateTokens(msg));
	}

	/** ToolResultMessage with completed result. */
	public void testToolResultCompleted() {
		ToolResultMessage msg = new ToolResultMessage("tr1", T0, "tc1", "read_file",
			new ToolState.Completed("The file contains: data", T0, T0, false));
		assertEquals(5, _estimator.estimateTokens(msg));
	}

	/** ToolResultMessage with failed state. */
	public void testToolResultFailed() {
		ToolResultMessage msg = new ToolResultMessage("tr1", T0, "tc1", "read_file",
			new ToolState.Failed("File not found", T0, T0));
		assertEquals(3, _estimator.estimateTokens(msg));
	}

	/** CompactionMarker is 0 tokens (metadata, not sent to LLM). */
	public void testCompactionMarker() {
		CompactionMarker marker = new CompactionMarker("cm1", T0, 5, 10000, "auto");
		assertEquals(0, _estimator.estimateTokens(marker));
	}

	/** List estimation sums all messages. */
	public void testListEstimation() {
		UserMessage u = new UserMessage("u1", T0,
			List.of(new TextContent("Hello, world!")));
		SystemMessage s = new SystemMessage("s1", T0, "You are helpful.");
		assertEquals(7, _estimator.estimateTokens(List.of(u, s)));
	}

	/** Empty message list is 0 tokens. */
	public void testEmptyList() {
		assertEquals(0, _estimator.estimateTokens(List.of()));
	}
}
