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
 * <p>Anthropic models emit these when extended thinking is enabled. <code>OpenAI</code> reasoning
 * models emit them when reasoning summaries are configured. Kept separate from
 * {@link TextContent} so the agent loop can stream them differently (via
 * {@code onThinkingDelta}) and exclude them from tool result context.</p>
 *
 * @param text the thinking/summary text
 * @param continuityToken opaque provider token for multi-turn thinking continuity
 *        (Anthropic's signature, <code>OpenAI</code>'s encrypted_content); may be {@code null}
 *
 * @see ContentPart
 */
public record ThinkingContent(String text, String continuityToken) implements ContentPart {

	/**
	 * Creates a {@link ThinkingContent}.
	 */
	public ThinkingContent {
		Objects.requireNonNull(text, "Text must not be null.");
	}
}
