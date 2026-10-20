/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import java.util.Objects;

/**
 * A text content block within a message.
 *
 * @param text the text content
 *
 * @see ContentPart
 */
public record TextContent(String text) implements ContentPart {

	/**
	 * Creates a {@link TextContent}.
	 */
	public TextContent {
		Objects.requireNonNull(text, "Text must not be null.");
	}
}
