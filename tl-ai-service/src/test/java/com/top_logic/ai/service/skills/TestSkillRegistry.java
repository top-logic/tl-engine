/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

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
		assertTrue(prompt.contains("OUTPUT FORMAT"));
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
		assertTrue(prompt.contains("TLScript RULES"));
	}

	public void testModelEditorPromptContainsKeySections() {
		String prompt = SkillConfig.getInstance().composeSystemMessage(ModelEditor.class);
		assertTrue(prompt.contains("TopLogic Model Creator"));
		assertTrue(prompt.contains("MANDATORY CREATION ORDER"));
		assertTrue(prompt.contains("OUTPUT FORMAT"));
	}

	public void testSkillCompositionOrder() {
		String prompt = SkillConfig.getInstance().composeSystemMessage(ModelExplorer.class);
		int identityIdx = prompt.indexOf("TopLogic Model Explorer");
		int toolFirstIdx = prompt.indexOf("TOOL-FIRST RULE");
		int zeroKnowledgeIdx = prompt.indexOf("ZERO PRIOR KNOWLEDGE");
		assertTrue("identity before tool-first", identityIdx < toolFirstIdx);
		assertTrue("tool-first before zero-knowledge", toolFirstIdx < zeroKnowledgeIdx);
	}

	interface TestAgent {
		String chat(String msg);
	}
}
