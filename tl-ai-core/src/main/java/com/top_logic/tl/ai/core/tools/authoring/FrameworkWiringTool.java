/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.authoring;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.tl.ai.core.agent.AgentDefinition;
import com.top_logic.tl.ai.core.agent.SkillDefinition;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.tools.authoring.messages.AgentDetail;
import com.top_logic.tl.ai.core.tools.authoring.messages.AgentListResult;
import com.top_logic.tl.ai.core.tools.authoring.messages.AgentSummary;
import com.top_logic.tl.ai.core.tools.authoring.messages.OperationResult;
import com.top_logic.tl.ai.core.tools.authoring.messages.SkillDetail;
import com.top_logic.tl.ai.core.tools.authoring.messages.SkillListResult;
import com.top_logic.tl.ai.core.tools.authoring.messages.SkillSummary;
import com.top_logic.tl.ai.core.tools.util.DataClassSerializer;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Tool for adding and removing tools, skills, and delegations on framework (TopLogic-provided)
 * agents and skills.
 *
 * <p>
 * Wiring operations write an additive config overlay and restart
 * {@link TopLogicAgentDefinitions} so changes take effect immediately. Query methods read the
 * running service state.
 * </p>
 */
public class FrameworkWiringTool {

	/**
	 * Configuration for {@link FrameworkWiringTool}.
	 */
	public interface Config extends PolymorphicConfiguration<FrameworkWiringTool> {
		// no additional properties
	}

	private final Consumer<Consumer<TopLogicAgentDefinitions.Config<?>>> _wireThenPersist;

	/**
	 * Creates a {@link FrameworkWiringTool} wired to the live application configuration.
	 */
	public FrameworkWiringTool() {
		_wireThenPersist = FrameworkWiringTool::livePersist;
	}

	/**
	 * Creates a {@link FrameworkWiringTool} from configuration.
	 */
	@SuppressWarnings("unused")
	@CalledByReflection
	public FrameworkWiringTool(InstantiationContext context, Config config) {
		this();
	}

	/**
	 * Creates a {@link FrameworkWiringTool} with an injected persistence strategy (for tests).
	 */
	public FrameworkWiringTool(Consumer<Consumer<TopLogicAgentDefinitions.Config<?>>> wireThenPersist) {
		_wireThenPersist = wireThenPersist;
	}

	// ---- Query methods (READ) ------------------------------------------------

	/**
	 * Lists all framework agents.
	 */
	@ToolMethod(description = "List all framework (TopLogic-provided) agents", mode = ToolMode.READ, group = ToolGroups.AGENT_META)
	public String listFrameworkAgents() {
		AgentListResult result = AgentListResult.create();
		for (AgentDefinition ad : service().getAllConfigAgents().values()) {
			AgentSummary summary = AgentSummary.create();
			summary.setName(ad.name());
			summary.setDescription(ad.description());
			result.addAgent(summary);
		}
		return DataClassSerializer.toJson(result, FrameworkWiringTool.class, "listFrameworkAgents()");
	}

	/**
	 * Gets a framework agent's tools, skills, and delegations.
	 */
	@ToolMethod(description = "Get a framework agent's tools, skills, and delegations",
		mode = ToolMode.READ, group = ToolGroups.AGENT_META)
	public String getFrameworkAgent(@P("Framework agent name") String agentName) {
		AgentDetail detail = AgentDetail.create();
		detail.setName(agentName);
		AgentDefinition ad = service().getConfigAgent(agentName);
		if (ad == null) {
			detail.setExists(false);
		} else {
			detail.setExists(true);
			detail.setDescription(ad.description());
			detail.setSystemPrompt(ad.systemPrompt() != null ? ad.systemPrompt() : "");
			detail.setTools(new ArrayList<>(ad.tools()));
			detail.setMcpServers(new ArrayList<>(ad.mcpServers()));
			detail.setSkills(new ArrayList<>(ad.skills()));
			detail.setDelegations(new ArrayList<>(ad.delegations()));
		}
		return DataClassSerializer.toJson(detail, FrameworkWiringTool.class, "getFrameworkAgent()");
	}

	/**
	 * Lists all framework skills.
	 */
	@ToolMethod(description = "List all framework (TopLogic-provided) skills", mode = ToolMode.READ, group = ToolGroups.AGENT_META)
	public String listFrameworkSkills() {
		SkillListResult result = SkillListResult.create();
		for (SkillDefinition sd : service().getAllConfigSkills().values()) {
			SkillSummary summary = SkillSummary.create();
			summary.setName(sd.name());
			summary.setDescription(sd.description());
			summary.setTools(new ArrayList<>(sd.tools()));
			result.addSkill(summary);
		}
		return DataClassSerializer.toJson(result, FrameworkWiringTool.class, "listFrameworkSkills()");
	}

	/**
	 * Gets a framework skill's tools.
	 */
	@ToolMethod(description = "Get a framework skill's tools", mode = ToolMode.READ, group = ToolGroups.AGENT_META)
	public String getFrameworkSkill(@P("Framework skill name") String skillName) {
		SkillDetail detail = SkillDetail.create();
		detail.setName(skillName);
		SkillDefinition sd = service().getConfigSkill(skillName);
		if (sd == null) {
			detail.setExists(false);
		} else {
			detail.setExists(true);
			detail.setDescription(sd.description());
			detail.setContent(sd.content() != null ? sd.content() : "");
			detail.setTools(new ArrayList<>(sd.tools()));
		}
		return DataClassSerializer.toJson(detail, FrameworkWiringTool.class, "getFrameworkSkill()");
	}

	// ---- Agent wiring (STRUCTURAL) -------------------------------------------

	/**
	 * Adds a tool to a framework agent.
	 */
	@ToolMethod(description = "Add a tool to a framework agent. Takes effect immediately.", group = ToolGroups.AGENT_AUTHORING)
	public String addToolToAgent(
			@P("Framework agent name") String agentName,
			@P("Qualified tool name to add") String toolName) {
		return wire("addToolToAgent", agentName,
			overlay -> FrameworkWiringPersistence.addToolToAgent(overlay, agentName, toolName),
			"Tool '" + toolName + "' added to agent '" + agentName + "'.");
	}

	/**
	 * Removes a tool from a framework agent.
	 */
	@ToolMethod(description = "Remove a tool from a framework agent. Takes effect immediately.", group = ToolGroups.AGENT_AUTHORING)
	public String removeToolFromAgent(
			@P("Framework agent name") String agentName,
			@P("Qualified tool name to remove") String toolName) {
		return wire("removeToolFromAgent", agentName,
			overlay -> FrameworkWiringPersistence.removeToolFromAgent(overlay, agentName, toolName),
			"Tool '" + toolName + "' removed from agent '" + agentName + "'.");
	}

	/**
	 * Adds a skill to a framework agent.
	 */
	@ToolMethod(description = "Add a skill to a framework agent. Takes effect immediately.", group = ToolGroups.AGENT_AUTHORING)
	public String addSkillToAgent(
			@P("Framework agent name") String agentName,
			@P("Skill name to add") String skillName) {
		return wire("addSkillToAgent", agentName,
			overlay -> FrameworkWiringPersistence.addSkillToAgent(overlay, agentName, skillName),
			"Skill '" + skillName + "' added to agent '" + agentName + "'.");
	}

	/**
	 * Removes a skill from a framework agent.
	 */
	@ToolMethod(description = "Remove a skill from a framework agent. Takes effect immediately.", group = ToolGroups.AGENT_AUTHORING)
	public String removeSkillFromAgent(
			@P("Framework agent name") String agentName,
			@P("Skill name to remove") String skillName) {
		return wire("removeSkillFromAgent", agentName,
			overlay -> FrameworkWiringPersistence.removeSkillFromAgent(overlay, agentName, skillName),
			"Skill '" + skillName + "' removed from agent '" + agentName + "'.");
	}

	/**
	 * Adds a delegation target to a framework agent.
	 */
	@ToolMethod(description = "Add a delegation target to a framework agent. Takes effect immediately.", group = ToolGroups.AGENT_AUTHORING)
	public String addDelegationToAgent(
			@P("Framework agent name") String agentName,
			@P("Delegation target agent name to add") String delegationName) {
		return wire("addDelegationToAgent", agentName,
			overlay -> FrameworkWiringPersistence.addDelegationToAgent(overlay, agentName, delegationName),
			"Delegation '" + delegationName + "' added to agent '" + agentName + "'.");
	}

	/**
	 * Removes a delegation target from a framework agent.
	 */
	@ToolMethod(description = "Remove a delegation target from a framework agent. Takes effect immediately.", group = ToolGroups.AGENT_AUTHORING)
	public String removeDelegationFromAgent(
			@P("Framework agent name") String agentName,
			@P("Delegation target agent name to remove") String delegationName) {
		return wire("removeDelegationFromAgent", agentName,
			overlay -> FrameworkWiringPersistence.removeDelegationFromAgent(overlay, agentName, delegationName),
			"Delegation '" + delegationName + "' removed from agent '" + agentName + "'.");
	}

	// ---- Skill wiring (STRUCTURAL) -------------------------------------------

	/**
	 * Adds a tool to a framework skill.
	 */
	@ToolMethod(description = "Add a tool to a framework skill. Takes effect immediately.", group = ToolGroups.AGENT_AUTHORING)
	public String addToolToSkill(
			@P("Framework skill name") String skillName,
			@P("Qualified tool name to add") String toolName) {
		return wireSkill("addToolToSkill", skillName,
			overlay -> FrameworkWiringPersistence.addToolToSkill(overlay, skillName, toolName),
			"Tool '" + toolName + "' added to skill '" + skillName + "'.");
	}

	/**
	 * Removes a tool from a framework skill.
	 */
	@ToolMethod(description = "Remove a tool from a framework skill. Takes effect immediately.", group = ToolGroups.AGENT_AUTHORING)
	public String removeToolFromSkill(
			@P("Framework skill name") String skillName,
			@P("Qualified tool name to remove") String toolName) {
		return wireSkill("removeToolFromSkill", skillName,
			overlay -> FrameworkWiringPersistence.removeToolFromSkill(overlay, skillName, toolName),
			"Tool '" + toolName + "' removed from skill '" + skillName + "'.");
	}

	// ---- Internals -----------------------------------------------------------

	private String wire(String operation, String agentName,
			Consumer<TopLogicAgentDefinitions.Config<?>> mutation, String successMsg) {
		if (service().getConfigAgent(agentName) == null) {
			return error("No framework agent named '" + agentName + "'. Call listFrameworkAgents "
				+ "to see available agents.", operation);
		}
		return doWire(operation, mutation, successMsg);
	}

	private String wireSkill(String operation, String skillName,
			Consumer<TopLogicAgentDefinitions.Config<?>> mutation, String successMsg) {
		if (service().getConfigSkill(skillName) == null) {
			return error("No framework skill named '" + skillName + "'. Call listFrameworkSkills "
				+ "to see available skills.", operation);
		}
		return doWire(operation, mutation, successMsg);
	}

	private String doWire(String operation,
			Consumer<TopLogicAgentDefinitions.Config<?>> mutation, String successMsg) {
		try {
			_wireThenPersist.accept(mutation);
			return ok(successMsg, operation);
		} catch (Exception ex) {
			Logger.error(operation + " error: " + ex.getMessage(), ex, FrameworkWiringTool.class);
			return error("Failed: " + ex.getMessage(), operation);
		}
	}

	private static void livePersist(Consumer<TopLogicAgentDefinitions.Config<?>> mutation) {
		TopLogicAgentDefinitions.Config<?> overlay = FrameworkWiringPersistence.loadOverlay();
		mutation.accept(overlay);
		FrameworkWiringPersistence.persist(overlay);
	}

	private static TopLogicAgentDefinitions service() {
		return TopLogicAgentDefinitions.getInstance();
	}

	private static String ok(String message, String operation) {
		OperationResult result = OperationResult.create();
		result.setSuccess(true);
		result.setResultMessage(message);
		return DataClassSerializer.toJson(result, FrameworkWiringTool.class, operation);
	}

	private static String error(String message, String operation) {
		return DataClassSerializer.errorToJson(message, FrameworkWiringTool.class, operation);
	}
}
