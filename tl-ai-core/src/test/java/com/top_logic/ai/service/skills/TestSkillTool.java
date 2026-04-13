/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

public class TestSkillTool extends TestCase {

	private SkillRegistry _registry;

	@Override
	protected void setUp() {
		_registry = new SkillRegistry();
		_registry.registerAgentSkills("test-agent",
			List.of("test/frontmatter-skill", "test/sample-skill"));
	}

	public void testListAvailableSkills() {
		SkillTool tool = new SkillTool(_registry, "test-agent");
		String result = tool.listAvailableSkills();
		assertTrue("Should list frontmatter skill", result.contains("test/frontmatter-skill"));
		assertTrue("Should include description", result.contains("A test skill with frontmatter"));
		assertTrue("Should list sample skill", result.contains("test/sample-skill"));
	}

	public void testLoadSkillReturnsContent() {
		SkillTool tool = new SkillTool(_registry, "test-agent");
		String result = tool.loadSkill("test/frontmatter-skill");
		assertTrue("Should contain skill content", result.contains("FRONTMATTER RULE"));
		assertFalse("Should NOT contain frontmatter", result.contains("description:"));
	}

	public void testLoadSkillRejectsUnregisteredSkill() {
		SkillTool tool = new SkillTool(_registry, "test-agent");
		String result = tool.loadSkill("nonexistent/skill");
		assertTrue("Should return error message", result.contains("not available"));
	}

	public void testListAvailableSkillsEmptyWhenNoneRegistered() {
		SkillRegistry registry = new SkillRegistry();
		registry.registerAgentSkills("empty-agent", Collections.emptyList());
		SkillTool tool = new SkillTool(registry, "empty-agent");
		String result = tool.listAvailableSkills();
		assertTrue("Should indicate no skills available",
			result.contains("No on-demand skills"));
	}
}
