/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.skills;

import java.util.List;

/**
 * Immutable runtime representation of a skill available to an agent.
 *
 * @param id          skill identifier
 * @param description human-readable description
 * @param content     full prompt text
 * @param tools       qualified tool names delivered by this skill (may be empty)
 */
public record Skill(String id, String description, String content, List<String> tools) {

	/** */
	public Skill {
		tools = tools != null ? List.copyOf(tools) : List.of();
	}
}
