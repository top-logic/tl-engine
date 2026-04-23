/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.skills;

import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.tools.interaction.SkillTool;

/** */
public class TestSkillTool extends TestCase {

	private SkillTool _tool;

	@Override
	protected void setUp() {
		Map<String, Skill> skills = new LinkedHashMap<>();
		skills.put("test/frontmatter-skill",
			new Skill("test/frontmatter-skill", "A test skill with frontmatter", "FRONTMATTER RULE content"));
		skills.put("test/sample-skill",
			new Skill("test/sample-skill", "test/sample-skill", "SAMPLE RULE content"));
		_tool = new SkillTool(skills);
	}

	/** */
	public void testLoadSkillReturnsContent() {
		String result = _tool.loadSkill("test/frontmatter-skill");
		assertTrue("Should contain skill content", result.contains("FRONTMATTER RULE"));
	}

	/** */
	public void testLoadSkillRejectsUnregisteredSkill() {
		String result = _tool.loadSkill("nonexistent/skill");
		assertTrue("Should return error message", result.contains("not available"));
	}

	/** */
	public void testLoadSkillListsAvailableOnError() {
		String result = _tool.loadSkill("nonexistent/skill");
		assertTrue("Should list available skills", result.contains("test/frontmatter-skill"));
	}
}
