/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import com.top_logic.tl.ai.tool.api.CancellationToken;

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

	/**
	 * Cancellation token that, once cancelled, should abort the in-flight streaming request
	 * promptly instead of letting it run to completion.
	 *
	 * <p>
	 * The reading side of a streaming call blocks on the network socket while the model
	 * generates its response; a run cancelled mid-turn would otherwise not take effect until
	 * that whole response has streamed in and the loop reaches its next between-turn check. A
	 * provider observes this token (e.g. by closing the response stream when it fires) so a user
	 * cancel ends the current turn within a fraction of a second.
	 * </p>
	 *
	 * <p>
	 * Defaults to {@link CancellationToken#UNCANCELLABLE} for handlers that do not participate in
	 * cancellation (e.g. one-shot model-discovery probes).
	 * </p>
	 */
	default CancellationToken cancellation() {
		return CancellationToken.UNCANCELLABLE;
	}
}
