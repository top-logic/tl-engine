/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.memory;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.LLMProviderService;

/**
 * The compaction strategy a run gets when nobody chose one.
 *
 * <p>
 * One place for the chat's session memory and for the memory a child run gets, so that a worker
 * compacts the way the conversation that started it does.
 * </p>
 */
public final class CompactionStrategies {

	/** Output budget of one summarization call. */
	public static final int SUMMARIZATION_MAX_OUTPUT_TOKENS = 2048;

	private CompactionStrategies() {
		// Static factory.
	}

	/**
	 * A {@link SummarizingStrategy} on the runtime's default model ({@link AgentRuntimeService}), or a
	 * {@link TruncatingStrategy} when there is none to summarize with.
	 *
	 * <p>
	 * Truncation is the fallback rather than a failure: a run that can no longer summarize must still
	 * fit its window, and losing the oldest messages is better than being refused by the provider.
	 * </p>
	 */
	public static CompactionStrategy forDefaultModel() {
		LLMProviderService registry = LLMProviderService.getInstance();
		AgentRuntimeService runtime = AgentRuntimeService.getInstance();
		if (registry == null || runtime == null) {
			return new TruncatingStrategy();
		}
		String defaultModel = runtime.getDefaultModel();
		if (defaultModel == null || defaultModel.isBlank()) {
			Logger.warn("No default model configured, falling back to truncation.", CompactionStrategies.class);
			return new TruncatingStrategy();
		}
		LLMProvider provider;
		try {
			provider = registry.buildProvider(defaultModel, runtime.getDefaultProvider());
		} catch (IllegalStateException ex) {
			Logger.warn("Default model '" + defaultModel + "' not found in any provider, falling back to truncation.",
				CompactionStrategies.class);
			return new TruncatingStrategy();
		}
		return new SummarizingStrategy(provider, defaultModel, SUMMARIZATION_MAX_OUTPUT_TOKENS);
	}
}
