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
 * A message from the user.
 *
 * @param id        unique identifier
 * @param timestamp when this message was created
 * @param content   the user's input (text and/or images)
 */
public record UserMessage(String id, Instant timestamp,
		List<ContentPart> content) implements Message {

	/**
	 *
	 */
	public UserMessage {
		Objects.requireNonNull(id, "Id must not be null.");
		Objects.requireNonNull(timestamp, "Timestamp must not be null.");
		Objects.requireNonNull(content, "Content must not be null.");
		content = List.copyOf(content);
	}
}
