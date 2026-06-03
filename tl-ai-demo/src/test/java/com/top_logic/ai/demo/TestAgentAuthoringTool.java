/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.core.model.Skill;
import com.top_logic.tl.ai.core.tools.authoring.AgentAuthoringTool;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link AgentAuthoringTool}.
 */
public class TestAgentAuthoringTool extends BasicTestCase {

	private AgentAuthoringTool _tool;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_tool = new AgentAuthoringTool();
	}

	public void testCreateGetListUpdateDeleteSkill() throws Exception {
		String create = _tool.createSkill("authoring-test-skill", "A test skill", "Step 1. Do the thing.");
		assertTrue("create should report success: " + create, create.contains("\"success\":true"));

		Skill skill = findSkill("authoring-test-skill");
		assertNotNull("skill must be created", skill);
		assertEquals("Step 1. Do the thing.", skill.getContent());
		assertEquals("A test skill", skill.getDescription());

		String detail = _tool.getSkill("authoring-test-skill");
		assertTrue("getSkill should contain content: " + detail, detail.contains("Do the thing"));

		String list = _tool.listSkills();
		assertTrue("listSkills should include the new skill: " + list, list.contains("authoring-test-skill"));

		String update = _tool.updateSkill("authoring-test-skill", null, "Step 1. Do it better.");
		assertTrue("update should report success: " + update, update.contains("\"success\":true"));
		assertEquals("content replaced", "Step 1. Do it better.", findSkill("authoring-test-skill").getContent());
		assertEquals("description unchanged", "A test skill", findSkill("authoring-test-skill").getDescription());

		String delete = _tool.deleteSkill("authoring-test-skill");
		assertTrue("delete should report success: " + delete, delete.contains("\"success\":true"));
		assertNull("skill must be deleted", findSkill("authoring-test-skill"));
	}

	private static Skill findSkill(String name) {
		List<Skill> skills = WrapperFactory.getWrappersByAttribute(
			AgentDefinitionQuery.SKILL_TABLE, PersistencyLayer.getKnowledgeBase(), "name", name);
		return skills.isEmpty() ? null : skills.get(0);
	}

	public void testCreateGetListUpdateDeleteAgent() throws Exception {
		String create = _tool.createAgent("authoring-test-agent", "A test agent", "You are a test.",
			List.of(), List.of(), List.of(), List.of());
		assertTrue("create should report success: " + create, create.contains("\"success\":true"));

		com.top_logic.tl.ai.core.model.Agent agent = findAgent("authoring-test-agent");
		assertNotNull("agent must be created", agent);
		assertEquals("You are a test.", agent.getSystemPrompt());
		assertTrue("tools empty", agent.getTools().isEmpty());

		String detail = _tool.getAgent("authoring-test-agent");
		assertTrue("getAgent should contain the prompt: " + detail, detail.contains("You are a test."));

		String list = _tool.listAgents();
		assertTrue("listAgents should include the new agent: " + list, list.contains("authoring-test-agent"));

		String update = _tool.updateAgent("authoring-test-agent", "Updated desc", null,
			null, null, null, null);
		assertTrue("update should report success: " + update, update.contains("\"success\":true"));
		assertEquals("desc replaced", "Updated desc", findAgent("authoring-test-agent").getDescription());
		assertEquals("prompt unchanged", "You are a test.",
			findAgent("authoring-test-agent").getSystemPrompt());

		String delete = _tool.deleteAgent("authoring-test-agent");
		assertTrue("delete should report success: " + delete, delete.contains("\"success\":true"));
		assertNull("agent must be deleted", findAgent("authoring-test-agent"));
	}

	private static com.top_logic.tl.ai.core.model.Agent findAgent(String name) {
		List<com.top_logic.tl.ai.core.model.Agent> agents = WrapperFactory.getWrappersByAttribute(
			AgentDefinitionQuery.AGENT_TABLE, PersistencyLayer.getKnowledgeBase(), "name", name);
		return agents.isEmpty() ? null : agents.get(0);
	}

	public void testValidateReferencesHardFailsUnknownTool() {
		java.util.Set<String> valid = java.util.Set.of("task.listTasks");
		try {
			AgentAuthoringTool.checkReferences(
				List.of("task.listTasks", "bogus.tool"), List.of(), List.of(),
				valid, s -> true, a -> true);
			fail("Expected IllegalArgumentException for unknown tool");
		} catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage().contains("bogus.tool"));
		}
	}

	public void testValidateReferencesWarnsUnknownSkillAndDelegation() {
		java.util.Set<String> valid = java.util.Set.of("task.listTasks");
		List<String> warnings = AgentAuthoringTool.checkReferences(
			List.of("task.listTasks"), List.of("missingSkill"), List.of("missingAgent"),
			valid, s -> false, a -> false);
		assertEquals(2, warnings.size());
		assertTrue(warnings.toString().contains("missingSkill"));
		assertTrue(warnings.toString().contains("missingAgent"));
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestAgentAuthoringTool.class),
				ModelService.Module.INSTANCE));
	}

}
