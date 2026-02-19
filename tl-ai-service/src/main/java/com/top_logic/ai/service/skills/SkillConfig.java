/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

import com.top_logic.ai.service.agents.TLScriptAgent;

/**
 * Production configuration mapping agents to their available on-demand skills.
 *
 * <p>
 * Each agent is mapped to a list of skill names that it can load on demand via
 * {@link SkillTool}. The {@link SkillRegistry} loads the corresponding {@code .md}
 * resource files from the classpath.
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

		registry.registerAgentSkills(TLScriptAgent.class,
			"tlscript/enum-comparison");

		return registry;
	}

	private SkillConfig() {
		// utility
	}
}
