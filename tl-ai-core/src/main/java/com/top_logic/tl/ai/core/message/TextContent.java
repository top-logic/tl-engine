/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import java.util.Objects;

/**
 * A text content block within a message.
 *
 * @see ContentPart
 */
public record TextContent(String text) implements ContentPart {

	/**
	 *
	 */
	public TextContent {
		Objects.requireNonNull(text, "Text must not be null.");
	}
}
