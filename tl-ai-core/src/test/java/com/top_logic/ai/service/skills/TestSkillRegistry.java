/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

import java.util.List;

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
		registry.registerAgentSkills("test-agent", List.of("test/sample-skill"));
		assertEquals(1, registry.getSkillsFor("test-agent").size());
		assertTrue(registry.getSkillsFor("test-agent").get(0).content().contains("SAMPLE RULE"));
	}

	public void testGetSkillsForUnregisteredAgent() {
		SkillRegistry registry = new SkillRegistry();
		assertTrue(registry.getSkillsFor("test-agent").isEmpty());
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
		SkillRegistry registry = new SkillRegistry();
		registry.registerAgentSkills("tlscript", List.of("shared/prompting-model-explorer"));
		for (Skill skill : registry.getSkillsFor("tlscript")) {
			assertNotNull("Skill content should not be null: " + skill.id(), skill.content());
			assertFalse("Skill content should not be empty: " + skill.id(),
				skill.content().isEmpty());
		}
	}

	public void testAgentWithSkillsRegistered() {
		SkillRegistry registry = new SkillRegistry();
		registry.registerAgentSkills("tlscript", List.of("shared/prompting-model-explorer"));
		assertFalse("Agent should have skills",
			registry.getSkillsFor("tlscript").isEmpty());
	}

	public void testBuildSystemPromptWithoutSkills() {
		SkillRegistry registry = new SkillRegistry();
		String prompt = registry.buildSystemPrompt("test-agent", "TEST SYSTEM PROMPT");
		assertTrue("Should contain base system message", prompt.contains("TEST SYSTEM PROMPT"));
		assertFalse("Should NOT contain skill catalog", prompt.contains("Available Skills"));
	}

	public void testBuildSystemPromptWithSkills() {
		SkillRegistry registry = new SkillRegistry();
		registry.registerAgentSkills("test-agent", List.of("test/frontmatter-skill"));
		String prompt = registry.buildSystemPrompt("test-agent", "TEST SYSTEM PROMPT");
		assertTrue("Should contain base system message", prompt.contains("TEST SYSTEM PROMPT"));
		assertTrue("Should contain skill catalog header", prompt.contains("Available Skills (IMPORTANT)"));
		assertTrue("Should contain skill id", prompt.contains("test/frontmatter-skill"));
		assertTrue("Should contain skill description", prompt.contains("A test skill with frontmatter"));
		assertTrue("Should mention loadSkill", prompt.contains("loadSkill"));
		assertFalse("Should NOT contain skill content inline", prompt.contains("FRONTMATTER RULE"));
	}

	public void testBuildSystemPromptWithMultipleSkills() {
		SkillRegistry registry = new SkillRegistry();
		registry.registerAgentSkills("test-agent", List.of("test/auto-skill", "test/frontmatter-skill"));
		String prompt = registry.buildSystemPrompt("test-agent", "TEST SYSTEM PROMPT");
		// Both skills appear in catalog
		assertTrue("Should contain catalog", prompt.contains("Available Skills (IMPORTANT)"));
		assertTrue("Should contain auto-skill id", prompt.contains("test/auto-skill"));
		assertTrue("Should contain frontmatter-skill id", prompt.contains("test/frontmatter-skill"));
		// Neither skill's content is inlined
		assertFalse("Should NOT contain auto skill content inline", prompt.contains("AUTO RULE"));
		assertFalse("Should NOT contain frontmatter skill content inline", prompt.contains("FRONTMATTER RULE"));
	}

	public void testBuildSystemPromptWithProductionSkills() {
		SkillRegistry registry = new SkillRegistry();
		registry.registerAgentSkills("tlscript", List.of("shared/prompting-model-explorer"));
		String prompt = registry.buildSystemPrompt("tlscript", "You are a TLScript Specialist Agent.");
		assertTrue("Should contain base system message", prompt.contains("TLScript Specialist"));
		// Skill appears as catalog entry, not inlined
		assertTrue("Should contain skill catalog", prompt.contains("Available Skills (IMPORTANT)"));
		assertTrue("Should contain skill id", prompt.contains("shared/prompting-model-explorer"));
	}

	public void testPlaceholderInjectionWithSkill() {
		SkillRegistry registry = new SkillRegistry();
		registry.registerAgentSkills("placeholder-agent", List.of("test/frontmatter-skill"));
		String prompt = registry.buildSystemPrompt("placeholder-agent",
			"TOOLS SECTION\n<<AVAILABLE_SKILLS>>\nWORK ORDER SECTION");
		assertTrue("Should contain base text before placeholder",
			prompt.contains("TOOLS SECTION"));
		assertTrue("Should contain skill catalog at placeholder position",
			prompt.contains("Available Skills (IMPORTANT)"));
		assertTrue("Should contain text after placeholder",
			prompt.contains("WORK ORDER SECTION"));
		assertFalse("Placeholder marker should be removed",
			prompt.contains("<<AVAILABLE_SKILLS>>"));

		// Verify skills are injected between TOOLS and WORK ORDER sections
		int toolsIndex = prompt.indexOf("TOOLS SECTION");
		int skillsIndex = prompt.indexOf("Available Skills");
		int workOrderIndex = prompt.indexOf("WORK ORDER SECTION");
		assertTrue("Skills should appear after TOOLS", skillsIndex > toolsIndex);
		assertTrue("Skills should appear before WORK ORDER", skillsIndex < workOrderIndex);
	}

	public void testPlaceholderRemovedWhenNoSkills() {
		SkillRegistry registry = new SkillRegistry();
		// No skills registered for placeholder-agent
		String prompt = registry.buildSystemPrompt("placeholder-agent",
			"TOOLS SECTION\n<<AVAILABLE_SKILLS>>\nWORK ORDER SECTION");
		assertFalse("Placeholder should be removed even without skills",
			prompt.contains("<<AVAILABLE_SKILLS>>"));
		assertFalse("Should not contain skill catalog",
			prompt.contains("Available Skills"));
	}

	public void testFrontmatterModeIgnored() {
		SkillRegistry registry = new SkillRegistry();
		// auto-skill.md has "mode: auto" in frontmatter — should be silently ignored
		Skill autoSkill = registry.getSkill("test/auto-skill");
		assertNotNull(autoSkill);
		assertEquals("An auto-injected test skill", autoSkill.description());
		assertTrue(autoSkill.content().contains("AUTO RULE"));

		// frontmatter-skill.md has no mode — should also work
		Skill onDemandSkill = registry.getSkill("test/frontmatter-skill");
		assertNotNull(onDemandSkill);
		assertEquals("A test skill with frontmatter", onDemandSkill.description());

		// sample-skill.md has no frontmatter at all
		Skill noFrontmatter = registry.getSkill("test/sample-skill");
		assertNotNull(noFrontmatter);
		assertEquals("test/sample-skill", noFrontmatter.description());
	}
}
