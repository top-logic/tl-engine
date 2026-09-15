/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
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
 * @param provider catalog ID of the provider that produced this block (e.g. {@code "anthropic"}),
 *        or {@code null} when it is not known. A continuity token only means something to the
 *        provider that minted it: replaying an Anthropic signature to <code>OpenAI</code>, or a
 *        block whose origin is unknown to anyone, is what makes a conversation unusable after a
 *        model switch. Read by
 *        {@link com.top_logic.tl.ai.core.provider.MessageHistorySanitizer}.
 *
 * @see ContentPart
 */
public record ThinkingContent(String text, String continuityToken, String provider) implements ContentPart {

	/**
	 * Creates a {@link ThinkingContent}.
	 */
	public ThinkingContent {
		Objects.requireNonNull(text, "Text must not be null.");
	}

	/** A thinking block whose origin is not recorded. */
	public ThinkingContent(String text, String continuityToken) {
		this(text, continuityToken, null);
	}
}
