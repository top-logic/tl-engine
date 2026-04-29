/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

/**
 * Receives events from an LLM streaming response.
 *
 * @see LLMProvider#stream(ProviderRequest, StreamHandler)
 */
@FunctionalInterface
public interface StreamHandler {

	/**
	 * Handle a streaming event.
	 *
	 * <p>Called sequentially on the thread that initiated the stream.
	 * Implementations should return quickly to avoid blocking the stream.</p>
	 */
	void onEvent(StreamEvent event);
}
