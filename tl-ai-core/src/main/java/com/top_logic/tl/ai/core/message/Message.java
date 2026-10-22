/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import java.time.Instant;

/**
 * A message in a conversation between user, assistant, and tools.
 *
 * <p>This is the foundation type for the agent loop, memory, compaction, and provider adapters.
 * Messages are immutable records — the conversation history stores only complete messages.</p>
 *
 * @see UserMessage
 * @see AssistantMessage
 * @see ToolResultMessage
 * @see SystemMessage
 * @see CompactionMarker
 */
public sealed interface Message
		permits UserMessage, AssistantMessage, ToolResultMessage, SystemMessage, CompactionMarker {

	/** Unique identifier for this message (UUID). */
	String id();

	/** When this message was created. */
	Instant timestamp();
}
