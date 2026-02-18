/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

import com.top_logic.ai.service.agents.ModelExplorer;

/**
 * Production configuration mapping agents to their ordered skill lists.
 *
 * <p>
 * Each agent is mapped to a list of skill names in composition order.
 * The {@link SkillRegistry} loads the corresponding {@code .md} resource files and
 * concatenates them to produce the agent's system prompt.
 * </p>
 */
public final class SkillConfig {

	private static final SkillRegistry INSTANCE = createRegistry();

	/**
	 * Returns the singleton production skill registry.
	 */
	public static SkillRegistry getInstance() {
		return INSTANCE;
	}

	private static SkillRegistry createRegistry() {
		SkillRegistry registry = new SkillRegistry();

		registry.registerAgentSkills(ModelExplorer.class,
			"explorer/identity",
			"core/tool-first-rule",
			"core/zero-knowledge",
			"explorer/exhaustiveness-mandate",
			"explorer/scope",
			"explorer/exploration-strategy",
			"explorer/context-resolution",
			"explorer/fact-rules",
			"explorer/output-format",
			"explorer/examples");

		return registry;
	}

	private SkillConfig() {
		// utility
	}
}
