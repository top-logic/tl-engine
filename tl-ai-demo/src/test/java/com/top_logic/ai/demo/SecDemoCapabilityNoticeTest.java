/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import junit.framework.Test;

import com.top_logic.tl.ai.core.agent.AgentCapabilities;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.core.agent.SkillDefinition;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.skills.Skill;
import com.top_logic.tl.ai.core.tools.DynamicToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.interaction.SkillTool;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

/**
 * What a denial does to the agent's prompt and to {@code loadSkill}: skills that can do nothing are
 * not offered, partly denied skills load only their permitted subset, and the prompt names the
 * capabilities the acting persona lacks.
 *
 * <p>
 * All of this is <b>user experience, not a control</b>. Nothing here decides whether a tool may run -
 * the invocation gate does ({@link SecDemoToolAccessTest}) and it is unaffected by every assertion
 * below. What it does decide is whether the agent can tell the user "you lack {@code data.write}"
 * instead of a vague "I can't do that", and whether it wastes turns hunting for a skill it may also
 * not use.
 * </p>
 *
 * <p>
 * The skills are the real ones from {@code tl-ai-core.conf.config.xml}, resolved through
 * {@link AgentDefinitionQuery}, so the assertions are about the deployed skill catalog:
 * </p>
 * <ul>
 * <li>{@code TL_modelExploration} - eight {@code model.read} tools and three {@code data.read} ones,
 * so it is the partial case.</li>
 * <li>{@code TL_taskManagement} - three tools, all {@code workspace}, so it is the all-or-nothing
 * case.</li>
 * <li>{@code TL_promptModelExplorer} - no tools at all, so it must survive for everyone.</li>
 * </ul>
 *
 * @see AgentCapabilities
 */
public class SecDemoCapabilityNoticeTest extends SecDemoPersonaTest {

	/** Eight {@code model.read} tools, three {@code data.read} tools. */
	private static final String MIXED_SKILL = "TL_modelExploration";

	/** Three tools, all in {@code workspace}. */
	private static final String WORKSPACE_SKILL = "TL_taskManagement";

	/** Pure instruction text, no tools. */
	private static final String TOOLLESS_SKILL = "TL_promptModelExplorer";

	/** Four tools, all in the privileged {@code script} group. */
	private static final String SCRIPT_SKILL = "TL_tlScriptWriting";

	/** Precondition: the skills this test reasons about have the shape it assumes. */
	public void testFixtureSkillsHaveTheAssumedShape() {
		assertEquals("TL_modelExploration is the partial case and needs both read groups.",
			Set.of(ToolGroups.MODEL_READ, ToolGroups.DATA_READ), groupsOf(MIXED_SKILL));
		assertEquals("TL_taskManagement is the all-or-nothing case and must sit in one group.",
			Set.of(ToolGroups.WORKSPACE), groupsOf(WORKSPACE_SKILL));
		assertEquals("TL_tlScriptWriting must sit in the privileged script group.",
			Set.of(ToolGroups.SCRIPT), groupsOf(SCRIPT_SKILL));
		assertTrue("TL_promptModelExplorer must declare no tools.",
			skill(TOOLLESS_SKILL).tools().isEmpty());
	}

	/**
	 * A skill every one of whose tools is denied is not offered at all.
	 *
	 * <p>
	 * {@code secreader1} holds no {@code workspace}, so {@code TL_taskManagement} can do nothing for
	 * it. Offering it would cost a prompt line and a wasted {@code loadSkill} turn that comes back with
	 * no tools.
	 * </p>
	 */
	public void testFullyDeniedSkillIsDropped() {
		String reason = capabilitiesOf(READER).unusableReason(skill(WORKSPACE_SKILL));

		assertNotNull("TL_taskManagement must be dropped for a persona without workspace.", reason);
		assertTrue("The log line must name the capability an operator has to grant: " + reason,
			reason.contains(ToolGroups.WORKSPACE));
	}

	/**
	 * A partly denied skill survives, because its instructions still apply to the tools that remain.
	 */
	public void testPartiallyDeniedSkillSurvives() {
		assertNull("TL_modelExploration must survive for a persona holding model.read alone.",
			capabilitiesOf(VIEWER).unusableReason(skill(MIXED_SKILL)));
	}

	/**
	 * A partly denied skill hands over only the tools the persona may call.
	 *
	 * <p>
	 * {@code loadSkill} used to register and announce every resolvable tool of a skill; the next turn's
	 * schema filter then removed exactly the denied ones, so an authoritative tool result contradicted
	 * the tool list the model was working from.
	 * </p>
	 */
	public void testLoadSkillWithholdsTheDeniedTools() {
		SkillDefinition definition = skill(MIXED_SKILL);
		String loaded = loadSkillAs(VIEWER, definition);

		for (String toolName : definition.tools()) {
			Tool tool = ToolRegistryService.getInstance().getTool(toolName);
			assertNotNull("Precondition: " + toolName + " must be registered.", tool);

			if (ToolGroups.DATA_READ.equals(tool.group())) {
				assertFalse("A tool the persona may not call must not be announced: " + tool.name()
					+ "\n" + loaded, loaded.contains("- " + tool.name() + ":"));
			} else {
				assertTrue("A tool it may call must be announced: " + tool.name() + "\n" + loaded,
					loaded.contains("- " + tool.name() + ":"));
			}
		}
	}

	/** The same skill loaded by a persona holding both groups hands over every tool. */
	public void testLoadSkillHandsOverEverythingWhenNothingIsDenied() {
		SkillDefinition definition = skill(MIXED_SKILL);
		String loaded = loadSkillAs(READER, definition);

		for (String toolName : definition.tools()) {
			String bareName = ToolRegistryService.getInstance().getTool(toolName).name();
			assertTrue("secreader1 holds both capabilities, so " + bareName + " must be announced:\n"
				+ loaded, loaded.contains("- " + bareName + ":"));
		}
	}

	/**
	 * A skill with no tools at all is pure instruction text and survives for everyone.
	 *
	 * <p>
	 * "All zero of its tools are denied" is vacuously true, and a rule that deleted such a skill would
	 * silently remove instructions that never needed a capability.
	 * </p>
	 */
	public void testToollessSkillSurvivesForEveryPersona() {
		for (String login : List.of(BARE, VIEWER, READER, EDITOR, WRITER, AUTHOR)) {
			assertNull("A skill with no tools must survive for " + login + ".",
				capabilitiesOf(login).unusableReason(skill(TOOLLESS_SKILL)));
		}
	}

	/** The bare persona loses every skill that declares a tool, and keeps the ones that do not. */
	public void testBarePersonaKeepsOnlyToollessSkills() {
		AgentCapabilities capabilities = capabilitiesOf(BARE);

		assertNotNull(capabilities.unusableReason(skill(MIXED_SKILL)));
		assertNotNull(capabilities.unusableReason(skill(WORKSPACE_SKILL)));
		assertNotNull(capabilities.unusableReason(skill(SCRIPT_SKILL)));
		assertNull(capabilities.unusableReason(skill(TOOLLESS_SKILL)));
	}

	/**
	 * The capability notice names exactly the groups the persona lacks, among those the agent's tools
	 * and its skills' tools need.
	 *
	 * <p>
	 * Exactly: a capability the persona does hold must not appear, or the agent would tell the user it
	 * cannot do something it can.
	 * </p>
	 */
	public void testNoticeNamesExactlyTheLackingCapabilities() {
		Set<String> lacking = capabilitiesOf(VIEWER).lacking(List.of(MODEL_READ_TOOL, DATA_READ_TOOL,
			WORKSPACE_TOOL, AGENT_META_TOOL));

		assertEquals("secviewer1 holds model.read and nothing else.",
			Set.of(ToolGroups.DATA_READ, ToolGroups.WORKSPACE, ToolGroups.AGENT_META), lacking);
	}

	/** The notice text carries those names, so the agent can quote one to the user. */
	public void testNoticeTextCarriesTheCapabilityNames() {
		String notice = capabilitiesOf(READER).notice(
			List.of(MODEL_READ_TOOL, DATA_READ_TOOL, WORKSPACE_TOOL),
			List.of(skill(MIXED_SKILL), skill(WORKSPACE_SKILL)));

		assertTrue(notice, notice.contains("Capability Limits"));
		assertTrue("The capability secreader1 lacks must be named: " + notice,
			notice.contains(ToolGroups.WORKSPACE));
		assertFalse("A capability it holds must not be named as lacking: " + notice,
			notice.contains(ToolGroups.DATA_READ));
	}

	/**
	 * A persona holding every capability its agent needs is told so, rather than being told nothing.
	 *
	 * <p>
	 * The distinction matters: silence would read to the model as "the list may be incomplete", which
	 * is the guessing this section exists to stop.
	 * </p>
	 */
	public void testNoticeSaysSoWhenNothingIsWithheld() {
		String notice = capabilitiesOf(READER).notice(
			List.of(MODEL_READ_TOOL, DATA_READ_TOOL), List.of(skill(MIXED_SKILL)));

		assertTrue(notice, notice.contains("holds every capability"));
	}

	/**
	 * An ungrouped tool is not reported as a missing capability.
	 *
	 * <p>
	 * "You lack the capability of a tool that has none" is not actionable, and the deployment's answer
	 * to an unclassified tool is a {@code <tool-groups>} entry rather than a sentence in a prompt.
	 * </p>
	 */
	public void testUngroupedToolContributesNoCapabilityName() {
		assertEquals("An unclassified script tool names no capability.",
			Set.of(), capabilitiesOf(WRITER).lacking(List.of(UNCLASSIFIED_SCRIPT_TOOL)));
	}

	/**
	 * A tool the registry cannot resolve contributes nothing either - a typo is not a denial.
	 */
	public void testUnresolvableToolNameContributesNothing() {
		assertEquals(Set.of(), capabilitiesOf(BARE).lacking(List.of("no-such-factory.noSuchTool")));
	}

	/**
	 * The capability view of a run acting as the given persona, over the real tool registry.
	 */
	private AgentCapabilities capabilitiesOf(String login) {
		return AgentCapabilities.forRun(securityFor(login));
	}

	/**
	 * Loads the given skill through a real {@link SkillTool} acting as the given persona, and returns
	 * the message the tool hands the model.
	 */
	private String loadSkillAs(String login, SkillDefinition definition) {
		AgentCapabilities capabilities = capabilitiesOf(login);
		Skill skill = new Skill(definition.name(), definition.description(), definition.content(),
			definition.tools());
		DynamicToolCatalog catalog = new DynamicToolCatalog(new ToolCatalog().freeze());
		SkillTool skillTool = new SkillTool(Map.of(skill.id(), skill), catalog,
			name -> ToolRegistryService.getInstance().getTool(name), capabilities.permitted(), null);

		return skillTool.loadSkill(skill.id());
	}

	/** The tool groups a skill's tools sit in, for the precondition check. */
	private static Set<String> groupsOf(String skillName) {
		return skill(skillName).tools().stream()
			.map(name -> ToolRegistryService.getInstance().getTool(name))
			.filter(tool -> tool != null)
			.map(Tool::group)
			.collect(Collectors.toSet());
	}

	private static SkillDefinition skill(String name) {
		SkillDefinition definition = AgentDefinitionQuery.getInstance().findSkillByName(name);
		assertNotNull("The framework skill '" + name + "' must exist; this test bed assumes the"
			+ " skills shipped in 'tl-ai-core.conf.config.xml'.", definition);
		return definition;
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoCapabilityNoticeTest.class, TopLogicAgentDefinitions.Module.INSTANCE);
	}

}
