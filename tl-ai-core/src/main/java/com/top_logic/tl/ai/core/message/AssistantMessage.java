/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * A response from the LLM.
 *
 * <p>Content may interleave {@link TextContent} and {@link ToolCall} parts,
 * matching how LLM APIs emit responses.</p>
 *
 * @param id           unique identifier
 * @param timestamp    when this message was created
 * @param content      the response content (text and/or tool calls)
 * @param usage        token usage from the provider
 * @param finishReason why the LLM stopped ("stop", "tool_calls", "max_tokens")
 */
public record AssistantMessage(String id, Instant timestamp,
		List<ContentPart> content, TokenUsage usage,
		String finishReason) implements Message {

	/**
	 * 
	 */
	public AssistantMessage {
		Objects.requireNonNull(id, "Id must not be null.");
		Objects.requireNonNull(timestamp, "Timestamp must not be null.");
		Objects.requireNonNull(content, "Content must not be null.");
		Objects.requireNonNull(usage, "Usage must not be null.");
		Objects.requireNonNull(finishReason, "Finish reason must not be null.");
		content = List.copyOf(content);
	}
}
