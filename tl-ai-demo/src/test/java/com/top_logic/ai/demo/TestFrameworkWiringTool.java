/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.tl.ai.core.agent.ToolRef;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions.AgentConfig;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions.SkillConfig;
import com.top_logic.tl.ai.core.tools.authoring.FrameworkWiringPersistence;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link FrameworkWiringPersistence}.
 */
public class TestFrameworkWiringTool extends BasicTestCase {

	private static TopLogicAgentDefinitions.Config<?> configWithAgent(String agentName, String... tools) {
		TopLogicAgentDefinitions.Config<?> config =
			TypedConfiguration.newConfigItem(TopLogicAgentDefinitions.Config.class);
		AgentConfig agent = TypedConfiguration.newConfigItem(AgentConfig.class);
		agent.setName(agentName);
		for (String tool : tools) {
			ToolRef ref = TypedConfiguration.newConfigItem(ToolRef.class);
			ref.setName(tool);
			agent.getTools().add(ref);
		}
		config.getAgents().put(agentName, agent);
		return config;
	}

	private static TopLogicAgentDefinitions.Config<?> configWithSkill(String skillName, String... tools) {
		TopLogicAgentDefinitions.Config<?> config =
			TypedConfiguration.newConfigItem(TopLogicAgentDefinitions.Config.class);
		SkillConfig skill = TypedConfiguration.newConfigItem(SkillConfig.class);
		skill.setName(skillName);
		for (String tool : tools) {
			ToolRef ref = TypedConfiguration.newConfigItem(ToolRef.class);
			ref.setName(tool);
			skill.getTools().add(ref);
		}
		config.getSkills().put(skillName, skill);
		return config;
	}

	private static List<String> toolNames(AgentConfig agent) {
		return agent.getTools().stream().map(NamedConfigMandatory::getName).toList();
	}

	private static List<String> skillNames(AgentConfig agent) {
		return agent.getSkills().stream().map(NamedConfigMandatory::getName).toList();
	}

	private static List<String> delegationNames(AgentConfig agent) {
		return agent.getDelegations().stream().map(NamedConfigMandatory::getName).toList();
	}

	private static List<String> skillToolNames(SkillConfig skill) {
		return skill.getTools().stream().map(NamedConfigMandatory::getName).toList();
	}

	private static List<String> removedToolNames(AgentConfig agent) {
		return agent.getRemovedTools().stream().map(NamedConfigMandatory::getName).toList();
	}

	/** */
	public void testAddToolToAgent() {
		TopLogicAgentDefinitions.Config<?> overlay =
			TypedConfiguration.newConfigItem(TopLogicAgentDefinitions.Config.class);

		FrameworkWiringPersistence.addToolToAgent(overlay, "TL_TestAgent", "custom.echo");

		AgentConfig agent = overlay.getAgents().get("TL_TestAgent");
		assertNotNull(agent);
		assertTrue(toolNames(agent).contains("custom.echo"));
	}

	/** */
	public void testAddToolIdempotent() {
		TopLogicAgentDefinitions.Config<?> overlay = configWithAgent("TL_TestAgent", "custom.echo");

		FrameworkWiringPersistence.addToolToAgent(overlay, "TL_TestAgent", "custom.echo");

		assertEquals("should not duplicate", 1,
			overlay.getAgents().get("TL_TestAgent").getTools().size());
	}

	/** */
	public void testRemoveToolFromAgent() {
		TopLogicAgentDefinitions.Config<?> overlay = configWithAgent("TL_TestAgent", "custom.echo");

		FrameworkWiringPersistence.removeToolFromAgent(overlay, "TL_TestAgent", "custom.echo");

		assertTrue(overlay.getAgents().get("TL_TestAgent").getTools().isEmpty());
	}

	/** */
	public void testRemoveNonExistentToolIdempotent() {
		TopLogicAgentDefinitions.Config<?> overlay = configWithAgent("TL_TestAgent");

		// Should not throw
		FrameworkWiringPersistence.removeToolFromAgent(overlay, "TL_TestAgent", "nonexistent");
	}

	/** */
	public void testAddSkillToAgent() {
		TopLogicAgentDefinitions.Config<?> overlay =
			TypedConfiguration.newConfigItem(TopLogicAgentDefinitions.Config.class);

		FrameworkWiringPersistence.addSkillToAgent(overlay, "TL_TestAgent", "myAppSkill");

		assertTrue(skillNames(overlay.getAgents().get("TL_TestAgent")).contains("myAppSkill"));
	}

	/** */
	public void testAddDelegationToAgent() {
		TopLogicAgentDefinitions.Config<?> overlay =
			TypedConfiguration.newConfigItem(TopLogicAgentDefinitions.Config.class);

		FrameworkWiringPersistence.addDelegationToAgent(overlay, "TL_TestAgent", "TL_ModelEditor");

		assertTrue(delegationNames(overlay.getAgents().get("TL_TestAgent")).contains("TL_ModelEditor"));
	}

	/** */
	public void testAddToolToSkill() {
		TopLogicAgentDefinitions.Config<?> overlay =
			TypedConfiguration.newConfigItem(TopLogicAgentDefinitions.Config.class);

		FrameworkWiringPersistence.addToolToSkill(overlay, "TL_TestSkill", "custom.myTool");

		assertTrue(skillToolNames(overlay.getSkills().get("TL_TestSkill")).contains("custom.myTool"));
	}

	/** */
	public void testOverlayAccumulation() {
		TopLogicAgentDefinitions.Config<?> overlay =
			TypedConfiguration.newConfigItem(TopLogicAgentDefinitions.Config.class);

		FrameworkWiringPersistence.addToolToAgent(overlay, "TL_TestAgent", "custom.toolA");
		FrameworkWiringPersistence.addToolToAgent(overlay, "TL_TestAgent", "custom.toolB");
		FrameworkWiringPersistence.addSkillToAgent(overlay, "TL_TestAgent", "mySkill");

		AgentConfig agent = overlay.getAgents().get("TL_TestAgent");
		assertEquals(2, agent.getTools().size());
		assertEquals(1, agent.getSkills().size());
	}

	/** */
	public void testOverlaySerializationRoundTrip() throws Exception {
		TopLogicAgentDefinitions.Config<?> overlay =
			TypedConfiguration.newConfigItem(TopLogicAgentDefinitions.Config.class);
		FrameworkWiringPersistence.addToolToAgent(overlay, "TL_TestAgent", "custom.echo");
		FrameworkWiringPersistence.addSkillToAgent(overlay, "TL_TestAgent", "mySkill");

		java.io.File tmp = java.io.File.createTempFile("fw-wiring-overlay", ".config.xml");
		tmp.deleteOnExit();
		FrameworkWiringPersistence.persistTo(tmp, overlay);

		String content = java.nio.file.Files.readString(tmp.toPath());
		assertTrue("should contain agent name", content.contains("TL_TestAgent"));
		assertTrue("should contain tool ref", content.contains("custom.echo"));
		assertTrue("should contain skill ref", content.contains("mySkill"));
		assertTrue("should be multi-line (pretty-printed)", content.split("\n").length > 3);
	}

	/**
	 * Regression: adding a skill and then removing a tool across separate persist/load cycles must
	 * preserve the earlier skill addition. Previously the second operation rebuilt an empty overlay
	 * and overwrote the file, discarding the skill.
	 */
	public void testPersistLoadAccumulatesDeltas() throws Exception {
		java.io.File tmp = java.io.File.createTempFile("fw-wiring-accumulate", ".config.xml");
		tmp.deleteOnExit();

		TopLogicAgentDefinitions.Config<?> first = FrameworkWiringPersistence.newOverlay();
		FrameworkWiringPersistence.addSkillToAgent(first, "TL_TestAgent", "mySkill");
		FrameworkWiringPersistence.persistTo(tmp, first);

		TopLogicAgentDefinitions.Config<?> second = FrameworkWiringPersistence.load(tmp);
		FrameworkWiringPersistence.removeToolFromAgent(second, "TL_TestAgent", "custom.echo");
		FrameworkWiringPersistence.persistTo(tmp, second);

		TopLogicAgentDefinitions.Config<?> reloaded = FrameworkWiringPersistence.load(tmp);
		AgentConfig agent = reloaded.getAgents().get("TL_TestAgent");
		assertNotNull("agent entry survived", agent);
		assertTrue("earlier skill add survived later remove", skillNames(agent).contains("mySkill"));
		assertTrue("removal recorded", removedToolNames(agent).contains("custom.echo"));
	}

	/** A removal is recorded as an additive marker; re-adding the same name clears the marker. */
	public void testRemoveThenAddClearsMarker() {
		TopLogicAgentDefinitions.Config<?> overlay = FrameworkWiringPersistence.newOverlay();

		FrameworkWiringPersistence.removeToolFromAgent(overlay, "TL_TestAgent", "custom.echo");
		AgentConfig agent = overlay.getAgents().get("TL_TestAgent");
		assertTrue("removal marker recorded", removedToolNames(agent).contains("custom.echo"));

		FrameworkWiringPersistence.addToolToAgent(overlay, "TL_TestAgent", "custom.echo");
		assertFalse("re-add clears removal marker", removedToolNames(agent).contains("custom.echo"));
		assertTrue("re-add restores the tool", toolNames(agent).contains("custom.echo"));
	}

	/** {@link FrameworkWiringPersistence#load} of a missing file yields an empty overlay. */
	public void testLoadMissingFile() throws Exception {
		java.io.File missing = java.io.File.createTempFile("fw-wiring-missing", ".config.xml");
		assertTrue("delete to simulate absence", missing.delete());

		TopLogicAgentDefinitions.Config<?> overlay = FrameworkWiringPersistence.load(missing);
		assertTrue("empty overlay for missing file", overlay.getAgents().isEmpty());
	}

	/** {@link TopLogicAgentDefinitions#effectiveNames} subtracts removed names, keeping order. */
	public void testEffectiveNamesSubtractsRemoved() {
		AgentConfig present = TypedConfiguration.newConfigItem(AgentConfig.class);
		for (String name : new String[] {"a", "b", "c"}) {
			ToolRef ref = TypedConfiguration.newConfigItem(ToolRef.class);
			ref.setName(name);
			present.getTools().add(ref);
		}
		ToolRef removed = TypedConfiguration.newConfigItem(ToolRef.class);
		removed.setName("b");
		present.getRemovedTools().add(removed);

		assertEquals(List.of("a", "c"),
			TopLogicAgentDefinitions.effectiveNames(present.getTools(), present.getRemovedTools()));
	}

	/** */
	public static Test suite() {
		BasicRuntimeModule<?>[] modules = {
			ModelService.Module.INSTANCE,
			LabelProviderService.Module.INSTANCE,
		};
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(new TestSuite(TestFrameworkWiringTool.class), modules));
	}
}
