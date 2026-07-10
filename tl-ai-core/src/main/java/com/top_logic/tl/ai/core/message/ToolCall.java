/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import java.util.Objects;

/**
 * A tool invocation requested by the LLM.
 *
 * <p>Appears as a {@link ContentPart} in {@link AssistantMessage} content. The
 * {@link #arguments()} field is a raw JSON string — the agent loop parses and validates
 * it when ready to execute the tool.</p>
 *
 * @param id        unique identifier for this tool call (from the LLM)
 * @param name      the tool name
 * @param arguments raw JSON string of tool arguments
 *
 * @see ToolResultMessage
 */
public record ToolCall(String id, String name, String arguments) implements ContentPart {

	/**
	 * Creates a {@link ToolCall}.
	 */
	public ToolCall {
		Objects.requireNonNull(id, "Tool call id must not be null.");
		Objects.requireNonNull(name, "Tool name must not be null.");
		Objects.requireNonNull(arguments, "Tool arguments must not be null.");
	}
}
