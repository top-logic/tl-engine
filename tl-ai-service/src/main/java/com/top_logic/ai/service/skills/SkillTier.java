/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

/**
 * Classification of skills into loading tiers.
 *
 * <p>
 * {@link #CORE} skills are always included in the system prompt.
 * {@link #ON_DEMAND} skills are discoverable via {@link SkillTool} and loaded by the AI when needed.
 * </p>
 */
public enum SkillTier {

	/** Always included in the agent's system prompt. */
	CORE("core"),

	/** Discoverable via tool; loaded on demand by the AI. */
	ON_DEMAND("on-demand");

	private final String _key;

	SkillTier(String key) {
		_key = key;
	}

	/**
	 * Parses a tier string from YAML frontmatter.
	 *
	 * @param value
	 *        The frontmatter value (e.g. {@code "core"} or {@code "on-demand"}).
	 *        {@code null} defaults to {@link #CORE}.
	 * @return The matching tier.
	 * @throws IllegalArgumentException
	 *         if the value does not match any tier.
	 */
	public static SkillTier fromString(String value) {
		if (value == null) {
			return CORE;
		}
		String normalized = value.strip().toLowerCase();
		for (SkillTier tier : values()) {
			if (tier._key.equals(normalized)) {
				return tier;
			}
		}
		throw new IllegalArgumentException("Unknown skill tier: " + value);
	}
}
