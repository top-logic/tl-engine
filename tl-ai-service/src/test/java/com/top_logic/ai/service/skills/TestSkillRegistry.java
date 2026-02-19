/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

import com.top_logic.ai.service.agents.TLScriptAgent;

import dev.langchain4j.service.SystemMessage;

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

	public void testGetSkillsFor() {
		SkillRegistry registry = new SkillRegistry();
		registry.registerAgentSkills(TestAgent.class, "test/sample-skill");
		assertEquals(1, registry.getSkillsFor(TestAgent.class).size());
		assertTrue(registry.getSkillsFor(TestAgent.class).get(0).content().contains("SAMPLE RULE"));
	}

	public void testGetSkillsForUnregisteredAgent() {
		SkillRegistry registry = new SkillRegistry();
		assertTrue(registry.getSkillsFor(TestAgent.class).isEmpty());
	}

	public void testFrontmatterParsing() {
		SkillRegistry registry = new SkillRegistry();
		Skill skill = registry.getSkill("test/frontmatter-skill");
		assertEquals("test/frontmatter-skill", skill.id());
		assertEquals("A test skill with frontmatter", skill.description());
		assertTrue(skill.content().contains("FRONTMATTER RULE"));
		assertFalse("Frontmatter should be stripped from content", skill.content().contains("description:"));
	}

	public void testSkillWithoutFrontmatterDefaultsIdAsDescription() {
		SkillRegistry registry = new SkillRegistry();
		Skill skill = registry.getSkill("test/sample-skill");
		assertEquals("test/sample-skill", skill.id());
		assertEquals("test/sample-skill", skill.description());
	}

	public void testAllProductionSkillsLoadSuccessfully() {
		SkillRegistry registry = SkillConfig.getInstance();
		for (Skill skill : registry.getSkillsFor(TLScriptAgent.class)) {
			assertNotNull("Skill content should not be null: " + skill.id(), skill.content());
			assertFalse("Skill content should not be empty: " + skill.id(),
				skill.content().isEmpty());
		}
	}

	public void testTLScriptAgentHasSkills() {
		SkillRegistry registry = SkillConfig.getInstance();
		assertFalse("TLScriptAgent should have skills",
			registry.getSkillsFor(TLScriptAgent.class).isEmpty());
	}

	public void testBuildSystemPromptWithoutSkills() {
		SkillRegistry registry = new SkillRegistry();
		String prompt = registry.buildSystemPrompt(TestAgent.class);
		assertTrue("Should contain base system message", prompt.contains("TEST SYSTEM PROMPT"));
		assertFalse("Should NOT contain skill catalog", prompt.contains("Available Skills"));
	}

	public void testBuildSystemPromptWithSkills() {
		SkillRegistry registry = new SkillRegistry();
		registry.registerAgentSkills(TestAgent.class, "test/frontmatter-skill");
		String prompt = registry.buildSystemPrompt(TestAgent.class);
		assertTrue("Should contain base system message", prompt.contains("TEST SYSTEM PROMPT"));
		assertTrue("Should contain skill catalog header", prompt.contains("Available Skills"));
		assertTrue("Should contain skill id", prompt.contains("test/frontmatter-skill"));
		assertTrue("Should contain skill description", prompt.contains("A test skill with frontmatter"));
		assertTrue("Should mention loadSkill", prompt.contains("loadSkill"));
	}

	public void testBuildSystemPromptForTLScriptAgent() {
		SkillRegistry registry = SkillConfig.getInstance();
		String prompt = registry.buildSystemPrompt(TLScriptAgent.class);
		assertTrue("Should contain TLScript system message", prompt.contains("TLScript Specialist"));
		assertTrue("Should contain skill catalog", prompt.contains("Available Skills"));
		assertTrue("Should contain enum-comparison skill", prompt.contains("tlscript/enum-comparison"));
	}

	interface TestAgent {
		@SystemMessage("TEST SYSTEM PROMPT")
		String chat(String msg);
	}
}
