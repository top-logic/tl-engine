/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import java.time.Instant;
import java.util.Objects;

/**
 * A system message (instructions or prompts for the LLM).
 *
 * @param id        unique identifier
 * @param timestamp when this message was created
 * @param content   the system prompt text
 */
public record SystemMessage(String id, Instant timestamp,
		String content) implements Message {

	/**
	 * Creates a {@link SystemMessage}.
	 */
	public SystemMessage {
		Objects.requireNonNull(id, "Id must not be null.");
		Objects.requireNonNull(timestamp, "Timestamp must not be null.");
		Objects.requireNonNull(content, "Content must not be null.");
	}
}
