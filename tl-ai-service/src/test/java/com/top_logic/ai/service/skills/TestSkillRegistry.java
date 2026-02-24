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
		registry.registerAgentSkills("tlscript", List.of("shared/prompting-ModelExplorer"));
		for (Skill skill : registry.getSkillsFor("tlscript")) {
			assertNotNull("Skill content should not be null: " + skill.id(), skill.content());
			assertFalse("Skill content should not be empty: " + skill.id(),
				skill.content().isEmpty());
		}
	}

	public void testAgentWithSkillsRegistered() {
		SkillRegistry registry = new SkillRegistry();
		registry.registerAgentSkills("tlscript", List.of("shared/prompting-ModelExplorer"));
		assertFalse("Agent should have skills",
			registry.getSkillsFor("tlscript").isEmpty());
	}

	public void testBuildSystemPromptWithoutSkills() {
		SkillRegistry registry = new SkillRegistry();
		String prompt = registry.buildSystemPrompt("test-agent", "TEST SYSTEM PROMPT");
		assertTrue("Should contain base system message", prompt.contains("TEST SYSTEM PROMPT"));
		assertFalse("Should NOT contain skill catalog", prompt.contains("Available Skills"));
	}

	public void testBuildSystemPromptWithOnDemandSkills() {
		SkillRegistry registry = new SkillRegistry();
		registry.registerAgentSkills("test-agent", List.of("test/frontmatter-skill"));
		String prompt = registry.buildSystemPrompt("test-agent", "TEST SYSTEM PROMPT");
		assertTrue("Should contain base system message", prompt.contains("TEST SYSTEM PROMPT"));
		assertTrue("Should contain skill catalog header", prompt.contains("Available Skills (IMPORTANT)"));
		assertTrue("Should contain skill id", prompt.contains("test/frontmatter-skill"));
		assertTrue("Should contain skill description", prompt.contains("A test skill with frontmatter"));
		assertTrue("Should mention loadSkill", prompt.contains("loadSkill"));
		assertFalse("Should NOT contain on-demand skill content", prompt.contains("FRONTMATTER RULE"));
	}

	public void testBuildSystemPromptWithAutoSkills() {
		SkillRegistry registry = new SkillRegistry();
		registry.registerAgentSkills("test-agent", List.of("test/auto-skill"));
		String prompt = registry.buildSystemPrompt("test-agent", "TEST SYSTEM PROMPT");
		assertTrue("Should contain base system message", prompt.contains("TEST SYSTEM PROMPT"));
		assertTrue("Should contain auto skill content", prompt.contains("AUTO RULE"));
		assertTrue("Should contain auto skill description as heading",
			prompt.contains("An auto-injected test skill"));
		assertFalse("Should NOT contain on-demand catalog", prompt.contains("Available Skills (IMPORTANT)"));
		assertFalse("Should NOT mention loadSkill", prompt.contains("loadSkill"));
	}

	public void testBuildSystemPromptWithMixedSkills() {
		SkillRegistry registry = new SkillRegistry();
		registry.registerAgentSkills("test-agent", List.of("test/auto-skill", "test/frontmatter-skill"));
		String prompt = registry.buildSystemPrompt("test-agent", "TEST SYSTEM PROMPT");
		// Auto skill content is injected
		assertTrue("Should contain auto skill content", prompt.contains("AUTO RULE"));
		// On-demand skill is in catalog
		assertTrue("Should contain on-demand catalog", prompt.contains("Available Skills (IMPORTANT)"));
		assertTrue("Should contain on-demand skill id", prompt.contains("test/frontmatter-skill"));
		assertFalse("Should NOT contain on-demand skill content", prompt.contains("FRONTMATTER RULE"));
		// Auto content appears before on-demand catalog
		int autoIndex = prompt.indexOf("AUTO RULE");
		int catalogIndex = prompt.indexOf("Available Skills");
		assertTrue("Auto content should appear before on-demand catalog", autoIndex < catalogIndex);
	}

	public void testBuildSystemPromptWithProductionSkills() {
		SkillRegistry registry = new SkillRegistry();
		registry.registerAgentSkills("tlscript", List.of("shared/prompting-ModelExplorer"));
		String prompt = registry.buildSystemPrompt("tlscript", "You are a TLScript Specialist Agent.");
		assertTrue("Should contain base system message", prompt.contains("TLScript Specialist"));
		// prompting-ModelExplorer is now auto mode — content should be injected
		assertTrue("Should contain auto-injected skill content",
			prompt.contains("How to Prompt the Model Explorer"));
	}

	public void testPlaceholderInjectionWithOnDemandSkill() {
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

	public void testPlaceholderInjectionWithAutoSkill() {
		SkillRegistry registry = new SkillRegistry();
		registry.registerAgentSkills("placeholder-agent", List.of("test/auto-skill"));
		String prompt = registry.buildSystemPrompt("placeholder-agent",
			"TOOLS SECTION\n<<AVAILABLE_SKILLS>>\nWORK ORDER SECTION");
		assertTrue("Auto skill content should be injected at placeholder",
			prompt.contains("AUTO RULE"));
		assertFalse("Placeholder marker should be removed",
			prompt.contains("<<AVAILABLE_SKILLS>>"));

		int toolsIndex = prompt.indexOf("TOOLS SECTION");
		int autoIndex = prompt.indexOf("AUTO RULE");
		int workOrderIndex = prompt.indexOf("WORK ORDER SECTION");
		assertTrue("Auto content should appear after TOOLS", autoIndex > toolsIndex);
		assertTrue("Auto content should appear before WORK ORDER", autoIndex < workOrderIndex);
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

	public void testSkillModeParsingFromFrontmatter() {
		SkillRegistry registry = new SkillRegistry();
		Skill autoSkill = registry.getSkill("test/auto-skill");
		assertEquals(Skill.Mode.AUTO, autoSkill.mode());

		Skill onDemandSkill = registry.getSkill("test/frontmatter-skill");
		assertEquals(Skill.Mode.ON_DEMAND, onDemandSkill.mode());

		Skill noFrontmatter = registry.getSkill("test/sample-skill");
		assertEquals(Skill.Mode.ON_DEMAND, noFrontmatter.mode());
	}
}
