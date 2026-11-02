/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

/**
 * Streaming-first interface for LLM providers.
 *
 * <p>Implementations are thread-safe: a single instance handles concurrent
 * streams from multiple agent loops.</p>
 */
public interface LLMProvider {

	/**
	 * Stream a response from the LLM.
	 *
	 * <p>Blocks the calling thread until the stream completes or an error occurs.
	 * Events are pushed to the handler sequentially as they arrive from the API.</p>
	 *
	 * @param request the request to send
	 * @param handler receives stream events
	 * @throws ProviderException on API errors, network errors, or authentication failures
	 */
	void stream(ProviderRequest request, StreamHandler handler) throws ProviderException;

	/**
	 * Whether this provider can authenticate a request now.
	 *
	 * <p>
	 * Asked before each request a run would send, so that a provider without its credentials is
	 * never called - its failure would say nothing about the model - and is called as soon as they
	 * are provided.
	 * </p>
	 *
	 * @see LLMProviderConfig#isAvailable()
	 */
	default boolean isAvailable() {
		return true;
	}
}
