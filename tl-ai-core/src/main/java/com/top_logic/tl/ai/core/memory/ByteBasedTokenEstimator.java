/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.memory;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.CompactionMarker;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.SystemMessage;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ThinkingContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;

/**
 * Token estimator using UTF-8 byte length divided by 4.
 *
 * <p>This heuristic (~0.25 tokens per byte) matches the approach used by Claude Code,
 * OpenCode, and Codex. It is fast and good enough for compaction decisions. Images
 * are estimated at a fixed 2000 tokens.</p>
 */
public class ByteBasedTokenEstimator implements TokenEstimator {

	/** Fixed token estimate for image content. */
	static final int IMAGE_TOKENS = 2000;

	private static final int BYTES_PER_TOKEN = 4;

	@Override
	public int estimateTokens(Message message) {
		return switch (message) {
			case UserMessage m -> estimateContentParts(m.content());
			case AssistantMessage m -> estimateContentParts(m.content());
			case SystemMessage m -> estimateText(m.content());
			case ToolResultMessage m -> estimateToolResult(m);
			case CompactionMarker m -> 0;
		};
	}

	@Override
	public int estimateTokens(List<Message> messages) {
		int total = 0;
		for (Message msg : messages) {
			total += estimateTokens(msg);
		}
		return total;
	}

	private int estimateContentParts(List<ContentPart> parts) {
		int total = 0;
		for (ContentPart part : parts) {
			total += switch (part) {
				case TextContent t -> estimateText(t.text());
				case ThinkingContent t -> estimateText(t.text());
				case ImageContent i -> IMAGE_TOKENS;
				case ToolCall tc -> estimateText(tc.name()) + estimateText(tc.arguments());
			};
		}
		return total;
	}

	private int estimateToolResult(ToolResultMessage msg) {
		return switch (msg.state()) {
			case ToolState.Completed c -> estimateText(c.result());
			case ToolState.Failed f -> estimateText(f.error());
			case ToolState.Pending p -> estimateText(p.arguments());
			case ToolState.Running r -> estimateText(r.arguments());
		};
	}

	private int estimateText(String text) {
		return text.getBytes(StandardCharsets.UTF_8).length / BYTES_PER_TOKEN;
	}
}
