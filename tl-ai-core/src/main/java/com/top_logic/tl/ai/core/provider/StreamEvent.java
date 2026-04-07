/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.List;
import java.util.Objects;

import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.TokenUsage;

/**
 * An event from an LLM streaming response.
 *
 * <p>Events are delivered sequentially to a {@link StreamHandler} as the response
 * streams in. The stream always ends with a {@link MessageComplete} event on success.</p>
 */
public sealed interface StreamEvent {

	/**
	 * A text delta from the LLM.
	 *
	 * @param contentIndex index of the content block in the response
	 * @param text         the text fragment
	 */
	record TextDelta(int contentIndex, String text) implements StreamEvent { /**/ }

	/**
	 * Start of a tool call in the response.
	 *
	 * @param contentIndex index of the content block
	 * @param id           tool call identifier (from the LLM)
	 * @param name         tool name
	 */
	record ToolCallStart(int contentIndex, String id, String name) implements StreamEvent { /**/ }

	/**
	 * Partial JSON arguments for a tool call being streamed.
	 *
	 * @param contentIndex index of the content block
	 * @param partialJson  a fragment of the JSON arguments string
	 */
	record ToolCallDelta(int contentIndex, String partialJson) implements StreamEvent { /**/ }

	/**
	 * The complete response message, emitted once when the stream finishes.
	 *
	 * <p>Contains the fully accumulated content parts and token usage.</p>
	 *
	 * @param id           message identifier from the provider
	 * @param model        model that generated the response
	 * @param finishReason why the LLM stopped ("end_turn", "tool_use", "max_tokens")
	 * @param usage        token usage
	 * @param content      the complete content parts (text and/or tool calls)
	 */
	record MessageComplete(
			String id,
			String model,
			String finishReason,
			TokenUsage usage,
			List<ContentPart> content) implements StreamEvent {

		/** */
		public MessageComplete {
			Objects.requireNonNull(usage, "Usage must not be null.");
			Objects.requireNonNull(content, "Content must not be null.");
			content = List.copyOf(content);
		}
	}
}
