/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import java.util.Objects;

/**
 * An extended thinking block from the LLM.
 *
 * <p>Anthropic models emit these when extended thinking is enabled. Kept separate
 * from {@link TextContent} so the agent loop can stream them differently
 * (via {@code onThinkingDelta}) and exclude them from tool result context.</p>
 *
 * @param text the thinking text
 *
 * @see ContentPart
 */
public record ThinkingContent(String text) implements ContentPart {

	/** */
	public ThinkingContent {
		Objects.requireNonNull(text, "Text must not be null.");
	}
}
