/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;

/**
 * How hard a model may be asked to think, and where that setting belongs.
 *
 * <p>
 * The two providers that offer the setting spell it differently - Anthropic calls it thinking
 * effort and takes a fourth level, {@code OpenAI} and {@code xAI} call it reasoning effort - and a
 * model that cannot reason takes neither. A UI offering the choice and the run that applies it must
 * agree on all of that, so both ask here rather than each deciding for itself.
 * </p>
 *
 * @see AgentModelAssignment#thinkingEffort()
 * @see AgentModelAssignment#reasoningEffort()
 */
public final class EffortLevels {

	/** Anthropic's thinking effort. */
	public static final List<String> THINKING = List.of("low", "medium", "high", "max");

	/** {@code OpenAI} / {@code xAI} reasoning effort. */
	public static final List<String> REASONING = List.of("low", "medium", "high");

	private static final Set<String> THINKING_PROVIDERS = Set.of("anthropic");

	private static final Set<String> REASONING_PROVIDERS = Set.of("openai", "xai");

	/** Which of the two assignment fields a level belongs in. */
	public enum Kind {
		/** {@link AgentModelAssignment#thinkingEffort()}. */
		THINKING_EFFORT,

		/** {@link AgentModelAssignment#reasoningEffort()}. */
		REASONING_EFFORT,

		/** The model offers no effort setting. */
		NONE
	}

	private EffortLevels() {
		// Static utility.
	}

	/**
	 * Where an effort level for this model belongs.
	 *
	 * @param providerId
	 *        catalog provider ID, e.g. {@code "anthropic"}; {@code null} counts as unknown
	 * @param info
	 *        the model's catalog entry, or {@code null} when the model is not in the catalogue
	 * @return {@link Kind#NONE} when the model cannot reason or its provider offers no such setting
	 */
	public static Kind kindOf(String providerId, ModelInfo info) {
		if (providerId == null || info == null || info.capabilities() == null
			|| !info.capabilities().reasoning()) {
			return Kind.NONE;
		}
		String provider = providerId.toLowerCase(Locale.ROOT);
		if (THINKING_PROVIDERS.contains(provider)) {
			return Kind.THINKING_EFFORT;
		}
		if (REASONING_PROVIDERS.contains(provider)) {
			return Kind.REASONING_EFFORT;
		}
		return Kind.NONE;
	}

	/**
	 * The levels this model offers, in increasing order.
	 *
	 * @return an empty list when the model offers no effort setting
	 * @see #kindOf(String, ModelInfo)
	 */
	public static List<String> forModel(String providerId, ModelInfo info) {
		return switch (kindOf(providerId, info)) {
			case THINKING_EFFORT -> THINKING;
			case REASONING_EFFORT -> REASONING;
			case NONE -> List.of();
		};
	}

	/**
	 * Whether the given level is one this model offers.
	 *
	 * @param level
	 *        the level to check; {@code null} or blank is never valid
	 */
	public static boolean isValid(String providerId, ModelInfo info, String level) {
		if (level == null || level.isBlank()) {
			return false;
		}
		return forModel(providerId, info).contains(level.trim().toLowerCase(Locale.ROOT));
	}
}
