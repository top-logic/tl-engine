/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

import java.util.List;

import com.top_logic.ai.service.agents.ModelEditor;
import com.top_logic.ai.service.agents.ModelExplorer;
import com.top_logic.ai.service.agents.OrchestratorRouter;
import com.top_logic.ai.service.agents.TLScriptAgent;

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

	public void testAllAgentsHaveSkills() {
		SkillRegistry registry = SkillConfig.getInstance();
		assertFalse("ModelExplorer should have skills",
			registry.getSkillsFor(ModelExplorer.class).isEmpty());
		assertFalse("OrchestratorRouter should have skills",
			registry.getSkillsFor(OrchestratorRouter.class).isEmpty());
		assertFalse("TLScriptAgent should have skills",
			registry.getSkillsFor(TLScriptAgent.class).isEmpty());
		assertFalse("ModelEditor should have skills",
			registry.getSkillsFor(ModelEditor.class).isEmpty());
	}

	public void testAllSkillResourcesLoadSuccessfully() {
		SkillRegistry registry = SkillConfig.getInstance();
		Class<?>[] agents = {ModelExplorer.class, OrchestratorRouter.class,
			TLScriptAgent.class, ModelEditor.class};
		for (Class<?> agentClass : agents) {
			for (Skill skill : registry.getSkillsFor(agentClass)) {
				assertNotNull("Skill content should not be null: " + skill.name(), skill.content());
				assertFalse("Skill content should not be empty: " + skill.name(),
					skill.content().isEmpty());
			}
		}
	}

	public void testNoAgentInterfaceHasSystemMessageAnnotation() {
		Class<?>[] agents = {ModelExplorer.class, OrchestratorRouter.class,
			TLScriptAgent.class, ModelEditor.class};
		for (Class<?> agentClass : agents) {
			for (java.lang.reflect.Method method : agentClass.getDeclaredMethods()) {
				assertNull("Agent " + agentClass.getSimpleName() + "." + method.getName()
					+ " should not have @SystemMessage",
					method.getAnnotation(dev.langchain4j.service.SystemMessage.class));
			}
		}
	}

	public void testModelExplorerPromptContainsKeySections() {
		String prompt = SkillConfig.getInstance().composeSystemMessage(ModelExplorer.class);
		assertTrue(prompt.contains("TopLogic Model Explorer"));
		assertTrue(prompt.contains("TOOL-FIRST RULE"));
		assertTrue(prompt.contains("ZERO PRIOR KNOWLEDGE"));
		assertTrue(prompt.contains("EXHAUSTIVENESS MANDATE"));
	}

	public void testOrchestratorPromptContainsKeySections() {
		String prompt = SkillConfig.getInstance().composeSystemMessage(OrchestratorRouter.class);
		assertTrue(prompt.contains("Orchestrator"));
		assertTrue(prompt.contains("MANDATORY DELEGATION"));
		assertTrue(prompt.contains("WORK ORDER DISCIPLINE"));
	}

	public void testTLScriptPromptContainsKeySections() {
		String prompt = SkillConfig.getInstance().composeSystemMessage(TLScriptAgent.class);
		assertTrue(prompt.contains("TLScript Specialist"));
		assertTrue(prompt.contains("TOOL-FIRST RULE"));
		assertTrue(prompt.contains("MANDATORY VERIFICATION RULES"));
	}

	public void testModelEditorPromptContainsKeySections() {
		String prompt = SkillConfig.getInstance().composeSystemMessage(ModelEditor.class);
		assertTrue(prompt.contains("TopLogic Model Creator"));
		assertTrue(prompt.contains("MANDATORY CREATION ORDER"));
		assertTrue(prompt.contains("IDEMPOTENT CREATION"));
	}

	public void testSkillCompositionOrder() {
		String prompt = SkillConfig.getInstance().composeSystemMessage(ModelExplorer.class);
		int identityIdx = prompt.indexOf("TopLogic Model Explorer");
		int toolFirstIdx = prompt.indexOf("TOOL-FIRST RULE");
		int zeroKnowledgeIdx = prompt.indexOf("ZERO PRIOR KNOWLEDGE");
		assertTrue("identity before tool-first", identityIdx < toolFirstIdx);
		assertTrue("tool-first before zero-knowledge", toolFirstIdx < zeroKnowledgeIdx);
	}

	public void testFrontmatterParsing() {
		SkillRegistry registry = new SkillRegistry();
		Skill skill = registry.getSkill("test/frontmatter-skill");
		assertEquals("A test skill with frontmatter", skill.description());
		assertEquals(SkillTier.CORE, skill.tier());
		assertTrue(skill.content().contains("FRONTMATTER RULE"));
		assertFalse("Frontmatter should be stripped from content", skill.content().contains("description:"));
	}

	public void testOnDemandFrontmatter() {
		SkillRegistry registry = new SkillRegistry();
		Skill skill = registry.getSkill("test/on-demand-skill");
		assertEquals("An on-demand test skill", skill.description());
		assertEquals(SkillTier.ON_DEMAND, skill.tier());
		assertTrue(skill.content().contains("ON-DEMAND CONTENT"));
	}

	public void testSkillWithoutFrontmatterDefaultsToCore() {
		SkillRegistry registry = new SkillRegistry();
		Skill skill = registry.getSkill("test/sample-skill");
		assertEquals(SkillTier.CORE, skill.tier());
		assertEquals("test/sample-skill", skill.description());
	}

	public void testGetOnDemandSkillsFor() {
		SkillRegistry registry = new SkillRegistry();
		registry.registerAgentSkills(TestAgent.class,
			"test/frontmatter-skill", "test/on-demand-skill");
		List<Skill> onDemand = registry.getOnDemandSkillsFor(TestAgent.class);
		assertEquals(1, onDemand.size());
		assertEquals("test/on-demand-skill", onDemand.get(0).name());
		assertEquals(SkillTier.ON_DEMAND, onDemand.get(0).tier());
	}

	public void testComposeSystemMessageOnlyIncludesCoreSkillContent() {
		SkillRegistry registry = new SkillRegistry();
		registry.registerAgentSkills(TestAgent.class,
			"test/frontmatter-skill", "test/on-demand-skill");
		String message = registry.composeSystemMessage(TestAgent.class);
		assertTrue("Should contain core skill content", message.contains("FRONTMATTER RULE"));
		assertFalse("Should NOT contain on-demand skill body", message.contains("ON-DEMAND CONTENT"));
	}

	public void testComposeSystemMessageIncludesSkillCatalog() {
		SkillRegistry registry = new SkillRegistry();
		registry.registerAgentSkills(TestAgent.class,
			"test/frontmatter-skill", "test/on-demand-skill");
		String message = registry.composeSystemMessage(TestAgent.class);
		assertTrue("Should contain catalog header", message.contains("Available Skills"));
		assertTrue("Should contain on-demand skill name in catalog",
			message.contains("test/on-demand-skill"));
		assertTrue("Should contain on-demand skill description in catalog",
			message.contains("An on-demand test skill"));
		assertTrue("Should contain loadSkill instruction",
			message.contains("loadSkill"));
	}

	public void testAllAgentPromptsContainSkillCatalog() {
		SkillRegistry registry = SkillConfig.getInstance();
		Class<?>[] agents = {ModelExplorer.class, OrchestratorRouter.class,
			TLScriptAgent.class, ModelEditor.class};
		for (Class<?> agentClass : agents) {
			String prompt = registry.composeSystemMessage(agentClass);
			assertTrue(agentClass.getSimpleName() + " prompt should contain skill catalog",
				prompt.contains("Available Skills"));
			assertTrue(agentClass.getSimpleName() + " prompt should mention loadSkill",
				prompt.contains("loadSkill"));
		}
	}

	public void testOnDemandSkillsAvailableForAllAgents() {
		SkillRegistry registry = SkillConfig.getInstance();
		assertFalse("ModelExplorer should have on-demand skills",
			registry.getOnDemandSkillsFor(ModelExplorer.class).isEmpty());
		assertFalse("OrchestratorRouter should have on-demand skills",
			registry.getOnDemandSkillsFor(OrchestratorRouter.class).isEmpty());
		assertFalse("TLScriptAgent should have on-demand skills",
			registry.getOnDemandSkillsFor(TLScriptAgent.class).isEmpty());
		assertFalse("ModelEditor should have on-demand skills",
			registry.getOnDemandSkillsFor(ModelEditor.class).isEmpty());
	}

	public void testAllSkillDescriptionsAreNotJustNames() {
		SkillRegistry registry = SkillConfig.getInstance();
		Class<?>[] agents = {ModelExplorer.class, OrchestratorRouter.class,
			TLScriptAgent.class, ModelEditor.class};
		for (Class<?> agentClass : agents) {
			for (Skill skill : registry.getSkillsFor(agentClass)) {
				assertFalse("Skill " + skill.name() + " should have a real description (not just its name)",
					skill.name().equals(skill.description()));
			}
		}
	}

	interface TestAgent {
		String chat(String msg);
	}
}
