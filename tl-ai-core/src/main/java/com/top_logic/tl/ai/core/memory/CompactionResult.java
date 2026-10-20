/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.memory;

import java.util.List;
import java.util.Objects;

import com.top_logic.tl.ai.core.message.Message;

/**
 * Result of a compaction operation.
 *
 * @param replacementMessages messages to replace the entire conversation with
 * @param messagesSummarized  how many original messages were replaced
 * @param tokensBefore        estimated tokens before compaction
 * @param tokensAfter         estimated tokens after compaction
 * @param trigger             what triggered compaction ("auto" or "manual")
 */
public record CompactionResult(
		List<Message> replacementMessages,
		int messagesSummarized,
		int tokensBefore,
		int tokensAfter,
		String trigger) {

	/** */
	public CompactionResult {
		Objects.requireNonNull(replacementMessages, "Replacement messages must not be null.");
		Objects.requireNonNull(trigger, "Trigger must not be null.");
		if (messagesSummarized < 0) {
			throw new IllegalArgumentException("messagesSummarized must not be negative.");
		}
		if (tokensBefore < 0) {
			throw new IllegalArgumentException("tokensBefore must not be negative.");
		}
		if (tokensAfter < 0) {
			throw new IllegalArgumentException("tokensAfter must not be negative.");
		}
		replacementMessages = List.copyOf(replacementMessages);
	}
}
