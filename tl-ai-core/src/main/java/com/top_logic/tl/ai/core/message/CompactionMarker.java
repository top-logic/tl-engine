/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import java.time.Instant;
import java.util.Objects;

/**
 * Marks a boundary where context compaction occurred.
 *
 * <p>When the agent loop compacts the conversation history, it replaces a range of
 * messages with a {@link SystemMessage} summary and inserts this marker. The marker
 * preserves metadata about the compaction for debugging and observability.</p>
 *
 * @param id                  unique identifier
 * @param timestamp           when compaction occurred
 * @param messagesSummarized  how many messages were replaced
 * @param tokensBefore        token count before compaction
 * @param trigger             what triggered compaction ("auto" or "manual")
 */
public record CompactionMarker(String id, Instant timestamp,
		int messagesSummarized, int tokensBefore,
		String trigger) implements Message {

	/**
	 * 
	 */
	public CompactionMarker {
		Objects.requireNonNull(id, "Id must not be null.");
		Objects.requireNonNull(timestamp, "Timestamp must not be null.");
		Objects.requireNonNull(trigger, "Trigger must not be null.");
	}
}
