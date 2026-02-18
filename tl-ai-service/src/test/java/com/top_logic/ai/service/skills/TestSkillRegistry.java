/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

import junit.framework.TestCase;

public class TestSkillRegistry extends TestCase {

	public void testLoadSkillFromResource() {
		SkillRegistry registry = new SkillRegistry();
		Skill skill = registry.getSkill("test/sample-skill");
		assertNotNull(skill);
		assertTrue(skill.content().contains("SAMPLE RULE"));
	}

	public void testLoadNonExistentSkillThrows() {
		SkillRegistry registry = new SkillRegistry();
		try {
			registry.getSkill("nonexistent/skill");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// ok
		}
	}

	public void testComposeSystemMessage() {
		SkillRegistry registry = new SkillRegistry();
		registry.registerAgentSkills(TestAgent.class, "test/sample-skill");
		String message = registry.composeSystemMessage(TestAgent.class);
		assertTrue(message.contains("SAMPLE RULE"));
	}

	public void testComposeMultipleSkills() {
		SkillRegistry registry = new SkillRegistry();
		registry.registerAgentSkills(TestAgent.class, "test/sample-skill", "test/sample-skill");
		String message = registry.composeSystemMessage(TestAgent.class);
		int firstIdx = message.indexOf("SAMPLE RULE");
		int secondIdx = message.indexOf("SAMPLE RULE", firstIdx + 1);
		assertTrue("Should contain skill content twice", secondIdx > firstIdx);
		assertTrue("Should contain separator", message.contains("---"));
	}

	interface TestAgent {
		String chat(String msg);
	}
}
