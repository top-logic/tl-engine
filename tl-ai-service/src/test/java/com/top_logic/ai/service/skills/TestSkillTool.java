/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

import junit.framework.TestCase;

public class TestSkillTool extends TestCase {

	private SkillRegistry _registry;

	@Override
	protected void setUp() {
		_registry = new SkillRegistry();
		_registry.registerAgentSkills(TestAgent.class,
			"test/frontmatter-skill", "test/on-demand-skill");
	}

	public void testListAvailableSkillsReturnsOnDemandOnly() {
		SkillTool tool = new SkillTool(_registry, TestAgent.class);
		String result = tool.listAvailableSkills();
		assertTrue("Should list on-demand skill", result.contains("test/on-demand-skill"));
		assertTrue("Should include description", result.contains("An on-demand test skill"));
		assertFalse("Should NOT list core skill", result.contains("test/frontmatter-skill"));
	}

	public void testLoadSkillReturnsContent() {
		SkillTool tool = new SkillTool(_registry, TestAgent.class);
		String result = tool.loadSkill("test/on-demand-skill");
		assertTrue("Should contain skill content", result.contains("ON-DEMAND CONTENT"));
		assertFalse("Should NOT contain frontmatter", result.contains("description:"));
	}

	public void testLoadSkillRejectsUnregisteredSkill() {
		SkillTool tool = new SkillTool(_registry, TestAgent.class);
		String result = tool.loadSkill("nonexistent/skill");
		assertTrue("Should return error message", result.contains("not available"));
	}

	public void testLoadSkillRejectsCoreSkill() {
		SkillTool tool = new SkillTool(_registry, TestAgent.class);
		String result = tool.loadSkill("test/frontmatter-skill");
		assertTrue("Should reject core skill",
			result.contains("not available") || result.contains("already loaded"));
	}

	public void testListAvailableSkillsEmptyWhenNoOnDemand() {
		SkillRegistry registry = new SkillRegistry();
		registry.registerAgentSkills(TestAgent.class, "test/frontmatter-skill");
		SkillTool tool = new SkillTool(registry, TestAgent.class);
		String result = tool.listAvailableSkills();
		assertTrue("Should indicate no skills available",
			result.contains("No on-demand skills"));
	}

	interface TestAgent {
		String chat(String msg);
	}
}
