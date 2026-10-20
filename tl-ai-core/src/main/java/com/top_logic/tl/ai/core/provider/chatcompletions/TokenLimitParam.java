/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.chatcompletions;

/**
 * JSON field name carrying the output-token limit in a Chat Completions request body.
 *
 * <p>The Chat Completions API historically used {@code max_tokens}, but GPT-5 and o-series
 * reasoning models reject it and require {@code max_completion_tokens} (which also accounts for
 * hidden reasoning tokens). Other <code>OpenAI</code>-compatible backends (Mistral, <code>xAI</code>, local servers) only
 * understand {@code max_tokens}, so the field name is selectable per provider preset.</p>
 */
public enum TokenLimitParam {

	/** Legacy {@code max_tokens} field, understood by Mistral, <code>xAI</code>, and local servers. */
	MAX_TOKENS,

	/**
	 * {@code max_completion_tokens} field, required by GPT-5/o-series reasoning models on Azure
	 * <code>OpenAI</code> and real <code>OpenAI</code> Chat Completions endpoints.
	 */
	MAX_COMPLETION_TOKENS,
}
