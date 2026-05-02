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
 * streams in. Lifecycle events come in triples: {@code *Start}, {@code *Delta},
 * {@code *End}. The stream always ends with a {@link MessageComplete} or
 * {@link Error} event.</p>
 */
public sealed interface StreamEvent {

	/** Emitted once when the stream connection is established. */
	record StreamStart() implements StreamEvent { /**/ }

	/** A text content block has started. */
	record TextStart(int contentIndex) implements StreamEvent { /**/ }

	/** A text delta from the LLM. */
	record TextDelta(int contentIndex, String text) implements StreamEvent { /**/ }

	/** A text content block has ended. */
	record TextEnd(int contentIndex) implements StreamEvent { /**/ }

	/** A thinking content block has started. */
	record ThinkingStart(int contentIndex) implements StreamEvent { /**/ }

	/** A thinking text delta from the LLM (extended thinking). */
	record ThinkingDelta(int contentIndex, String text) implements StreamEvent { /**/ }

	/** A thinking content block has ended. */
	record ThinkingEnd(int contentIndex) implements StreamEvent { /**/ }

	/** Start of a tool call in the response. */
	record ToolCallStart(int contentIndex, String id, String name) implements StreamEvent { /**/ }

	/** Partial JSON arguments for a tool call being streamed. */
	record ToolCallDelta(int contentIndex, String partialJson) implements StreamEvent { /**/ }

	/** A tool call has ended (arguments are fully accumulated). */
	record ToolCallEnd(int contentIndex) implements StreamEvent { /**/ }

	/**
	 * The complete response message, emitted once when the stream finishes successfully.
	 *
	 * <p>Contains the fully accumulated content parts and token usage.</p>
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

	/**
	 * A stream-level error from the provider.
	 *
	 * <p>Emitted for errors reported within the SSE stream (e.g. Anthropic's
	 * {@code error} event, OpenAI's {@code response.failed}). HTTP-level errors
	 * (non-200 status) are still thrown as {@link ProviderException}.</p>
	 */
	record Error(String message, ProviderException.Kind kind) implements StreamEvent { /**/ }
}
