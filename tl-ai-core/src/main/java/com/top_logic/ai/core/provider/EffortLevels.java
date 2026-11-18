/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.provider;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.top_logic.ai.core.provider.catalog.ModelInfo;

/**
 * How hard a model may be asked to think, and where that setting belongs.
 *
 * <p>
 * The two API families that offer the setting spell it differently - Anthropic calls it thinking
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

	private static final Set<String> THINKING_FAMILIES = Set.of(ApiFamilies.ANTHROPIC);

	private static final Set<String> REASONING_FAMILIES = Set.of(ApiFamilies.OPENAI, ApiFamilies.XAI);

	/** Which of the two assignment fields a level belongs in. */
	public enum Kind {
		/**
		 * The model spends effort on thinking before it answers, as Anthropic models do.
		 *
		 * @see AgentModelAssignment#thinkingEffort()
		 */
		THINKING_EFFORT,

		/**
		 * The model spends effort on reasoning before it answers, as {@code OpenAI} and {@code xAI}
		 * models do.
		 *
		 * @see AgentModelAssignment#reasoningEffort()
		 */
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
	 * @param family
	 *        the model's {@link ApiFamilies API family}, e.g. {@link ApiFamilies#ANTHROPIC};
	 *        {@code null} counts as unknown
	 * @param info
	 *        the model's catalog entry, or {@code null} when the model is not in the catalogue
	 * @return {@link Kind#NONE} when the model cannot reason or its family offers no such setting
	 */
	public static Kind kindOf(String family, ModelInfo info) {
		if (family == null || info == null || info.capabilities() == null
			|| !info.capabilities().reasoning()) {
			return Kind.NONE;
		}
		String normalized = family.toLowerCase(Locale.ROOT);
		if (THINKING_FAMILIES.contains(normalized)) {
			return Kind.THINKING_EFFORT;
		}
		if (REASONING_FAMILIES.contains(normalized)) {
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
	public static List<String> forModel(String family, ModelInfo info) {
		return switch (kindOf(family, info)) {
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
	public static boolean isValid(String family, ModelInfo info, String level) {
		if (level == null || level.isBlank()) {
			return false;
		}
		return forModel(family, info).contains(level.trim().toLowerCase(Locale.ROOT));
	}
}
