/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.memory;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.CompactionMarker;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.PdfContent;
import com.top_logic.tl.ai.core.message.SystemMessage;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ThinkingContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;

/**
 * Token estimator using UTF-8 byte length divided by a configurable ratio.
 *
 * <p>This heuristic (~0.25 tokens per byte for prose, ~0.5 for JSON) matches the
 * approach used by Claude Code, {@code OpenCode}, and Codex. It is fast and good enough
 * for compaction decisions. Images are estimated at a fixed 2000 tokens.</p>
 *
 * <p>Adds per-message overhead (4 tokens for role + envelope) and per-request
 * priming (3 tokens), matching the structural overhead that LLM APIs charge.</p>
 */
public class ByteBasedTokenEstimator implements TokenEstimator {

	/** Fixed token estimate for image content. */
	static final int IMAGE_TOKENS = 2000;

	/** Per-message overhead: role token + message envelope. */
	static final int MESSAGE_OVERHEAD = 4;

	/** Per-request priming tokens (added once to the total). */
	static final int REQUEST_PRIMING = 3;

	private static final int BYTES_PER_TOKEN = 4;

	/** JSON is denser — roughly 2 bytes per token due to single-char tokens. */
	private static final int JSON_BYTES_PER_TOKEN = 2;

	@Override
	public int estimateTokens(Message message) {
		if (message instanceof CompactionMarker) return 0;
		int content;
		if (message instanceof UserMessage m) content = estimateContentParts(m.content());
		else if (message instanceof AssistantMessage m) content = estimateContentParts(m.content());
		else if (message instanceof SystemMessage m) content = estimateText(m.content());
		else if (message instanceof ToolResultMessage m) content = estimateToolResult(m);
		else throw new IllegalArgumentException("Unknown message type: " + message.getClass());
		return content + MESSAGE_OVERHEAD;
	}

	@Override
	public int estimateTokens(List<Message> messages) {
		if (messages.isEmpty()) {
			return 0;
		}
		int total = REQUEST_PRIMING;
		for (Message msg : messages) {
			total += estimateTokens(msg);
		}
		return total;
	}

	private int estimateContentParts(List<ContentPart> parts) {
		int total = 0;
		for (ContentPart part : parts) {
			int partTokens;
			if (part instanceof TextContent t) partTokens = estimateText(t.text());
			else if (part instanceof ThinkingContent t) partTokens = estimateText(t.text());
			else if (part instanceof ImageContent) partTokens = IMAGE_TOKENS;
			else if (part instanceof ToolCall tc) partTokens = estimateText(tc.name()) + estimateJson(tc.arguments());
			else if (part instanceof PdfContent p) partTokens = estimateText(p.data());
			else throw new IllegalArgumentException("Unknown content part type: " + part.getClass());
			total += partTokens;
		}
		return total;
	}

	private int estimateToolResult(ToolResultMessage msg) {
		ToolState state = msg.state();
		if (state instanceof ToolState.Completed c) return estimateText(c.result());
		if (state instanceof ToolState.Failed f) return estimateText(f.error());
		if (state instanceof ToolState.Pending p) return estimateText(p.arguments());
		if (state instanceof ToolState.Running r) return estimateText(r.arguments());
		throw new IllegalArgumentException("Unknown tool state type: " + state.getClass());
	}

	private int estimateText(String text) {
		return text.getBytes(StandardCharsets.UTF_8).length / BYTES_PER_TOKEN;
	}

	private int estimateJson(String json) {
		return json.getBytes(StandardCharsets.UTF_8).length / JSON_BYTES_PER_TOKEN;
	}
}
