/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

/**
 * Source-agnostic representation of a skill definition.
 *
 * @param name        unique skill identifier
 * @param description human-readable description shown in the skill catalog
 * @param content     the actual prompt text loaded on demand
 * @param source      where this definition originated
 */
public record SkillDefinition(
		String name,
		String description,
		String content,
		AgentSource source) {

	/** */
	public SkillDefinition {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Skill name must not be blank.");
		}
	}
}
