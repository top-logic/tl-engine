/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.List;

/**
 * Immutable definition of a skill from config or KB.
 *
 * @param name        skill name (unique)
 * @param description human-readable description
 * @param content     prompt text
 * @param tools       qualified tool names this skill provides (may be empty)
 * @param source      where this definition came from
 */
public record SkillDefinition(
		String name,
		String description,
		String content,
		List<String> tools,
		AgentSource source) {

	/** */
	public SkillDefinition {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Skill name must not be blank.");
		}
		tools = tools != null ? List.copyOf(tools) : List.of();
	}
}
