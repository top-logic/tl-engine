/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import java.time.Instant;
import java.util.Objects;

/**
 * The result of a tool execution.
 *
 * <p>Correlates to a {@link ToolCall} in a preceding {@link AssistantMessage}
 * via {@link #toolCallId()}. The {@link #state()} tracks the full lifecycle
 * of the execution.</p>
 *
 * @param id         unique identifier
 * @param timestamp  when this message was created
 * @param toolCallId the id of the {@link ToolCall} this result belongs to
 * @param toolName   the tool name (for readability)
 * @param state      the execution lifecycle state
 *
 * @see ToolCall
 * @see ToolState
 */
public record ToolResultMessage(String id, Instant timestamp,
		String toolCallId, String toolName,
		ToolState state) implements Message {

	public ToolResultMessage {
		Objects.requireNonNull(id, "Id must not be null.");
		Objects.requireNonNull(timestamp, "Timestamp must not be null.");
		Objects.requireNonNull(toolCallId, "Tool call id must not be null.");
		Objects.requireNonNull(toolName, "Tool name must not be null.");
		Objects.requireNonNull(state, "State must not be null.");
	}
}
