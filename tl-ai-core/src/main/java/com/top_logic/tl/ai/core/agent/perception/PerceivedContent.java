/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.perception;

import java.util.Objects;

import com.top_logic.tl.ai.core.message.ContentPart;

/**
 * A piece of content a tool asked the agent loop to place into the model's context.
 *
 * @param description
 *        Human-readable caption telling the model what it is about to see and where it came from.
 *        Emitted as a text part directly in front of {@link #content()}.
 * @param content
 *        The multimodal content part itself, typically an
 *        {@link com.top_logic.tl.ai.core.message.ImageContent} or
 *        {@link com.top_logic.tl.ai.core.message.PdfContent}.
 *
 * @see PerceptionContext#offer(PerceivedContent)
 */
public record PerceivedContent(String description, ContentPart content) {

	/** */
	public PerceivedContent {
		Objects.requireNonNull(description, "Description must not be null.");
		Objects.requireNonNull(content, "Content must not be null.");
	}
}
