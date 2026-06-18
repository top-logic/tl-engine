/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.authoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.core.model.Agent;
import com.top_logic.tl.ai.core.model.Skill;
import com.top_logic.tl.ai.core.model.TlAiCoreFactory;
import com.top_logic.tl.ai.core.model.impl.AgentBase;
import com.top_logic.tl.ai.core.model.impl.SkillBase;
import com.top_logic.tl.ai.core.tools.authoring.messages.AgentDetail;
import com.top_logic.tl.ai.core.tools.authoring.messages.AgentListResult;
import com.top_logic.tl.ai.core.tools.authoring.messages.AgentSummary;
import com.top_logic.tl.ai.core.tools.authoring.messages.OperationResult;
import com.top_logic.tl.ai.core.tools.authoring.messages.SkillDetail;
import com.top_logic.tl.ai.core.tools.authoring.messages.SkillListResult;
import com.top_logic.tl.ai.core.tools.authoring.messages.SkillSummary;
import com.top_logic.tl.ai.core.tools.authoring.messages.ToolListResult;
import com.top_logic.tl.ai.core.tools.authoring.messages.ToolSummary;
import com.top_logic.tl.ai.core.tools.util.DataClassSerializer;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

/**
 * Tool for creating, inspecting, updating, and deleting application Agents and Skills.
 *
 * <p>
 * Mutating methods inherit the default {@link ToolMode#STRUCTURAL} mode, so they require human
 * approval and are blocked in production. Query methods are {@link ToolMode#READ}. This tool only
 * sees application agents/skills; framework (TopLogic-provided) ones are not visible or editable here.
 * </p>
 */
public class AgentAuthoringTool {

	/**
	 * Configuration for {@link AgentAuthoringTool}.
	 */
	public interface Config extends PolymorphicConfiguration<AgentAuthoringTool> {
		// no additional properties
	}

	/**
	 * Creates a new {@link AgentAuthoringTool}.
	 */
	public AgentAuthoringTool() {
		// stateless tool
	}

	/**
	 * Creates a new {@link AgentAuthoringTool} from configuration.
	 */
	@SuppressWarnings("unused")
	@CalledByReflection
	public AgentAuthoringTool(InstantiationContext context, Config config) {
		// no-op for stateless tools
	}

	// ---- Skill mutations -------------------------------------------------

	/**
	 * Creates a new application skill (reusable instruction text).
	 *
	 * @param name
	 *        Unique skill name.
	 * @param description
	 *        Short description shown in the skill catalog.
	 * @param content
	 *        The skill's instruction content (markdown).
	 * @return JSON-serialized {@link OperationResult} describing the outcome.
	 */
	@ToolMethod(description = "Create a new skill (instruction text + optional tools) in the application")
	public String createSkill(
			@P("Unique skill name — do NOT use the TL_ prefix (reserved for TopLogic)") String name,
			@P("Short description that tells the agent WHEN to load this skill, not what it does internally") String description,
			@P("Skill content — use XML tags for sections (<core_rules>, <workflow>, etc.), flat imperative rules, no filler prose") String content,
			@P(value = "Qualified tool names this skill provides when loaded (see listAvailableTools); omit for none",
				required = false) List<String> tools) {
		try {
			OperationResult result = doCreateSkill(name, description, content, tools);
			return DataClassSerializer.toJson(result, AgentAuthoringTool.class, "createSkill()");
		} catch (Exception ex) {
			Logger.error("createSkill() error: " + ex.getMessage(), ex, AgentAuthoringTool.class);
			return DataClassSerializer.errorToJson("Failed to create skill: " + ex.getMessage(),
				AgentAuthoringTool.class, "createSkill()");
		}
	}

	private OperationResult doCreateSkill(String name, String description, String content,
			List<String> tools) {
		requireName(name, "Skill");
		if (findSkill(name) != null) {
			throw new IllegalArgumentException("A skill named '" + name + "' already exists.");
		}
		validateSkillTools(tools);
		try (Transaction tx = PersistencyLayer.getKnowledgeBase()
				.beginTransaction(ResKey.text("Create skill: " + name))) {
			Skill skill = TlAiCoreFactory.getInstance().createSkill();
			skill.setName(name);
			skill.setDescription(description);
			skill.setContent(content);
			skill.setTools(toSet(tools));
			tx.commit();
		}
		return success("Skill '" + name + "' created.");
	}

	/**
	 * Updates an existing application skill. Omitted fields are left unchanged.
	 *
	 * @param name
	 *        Name of the skill to update.
	 * @param description
	 *        New description, or {@code null} to leave unchanged.
	 * @param content
	 *        New content, or {@code null} to leave unchanged.
	 * @return JSON-serialized {@link OperationResult} describing the outcome.
	 */
	@ToolMethod(description = "Update an existing application skill. Omitted fields are left unchanged.")
	public String updateSkill(
			@P("Name of the skill to update") String name,
			@P(value = "New description — tells the agent WHEN to load (omit to leave unchanged)", required = false) String description,
			@P(value = "New content — use XML tags, flat imperative rules, no filler (omit to leave unchanged)", required = false) String content,
			@P(value = "New tool list (omit to leave unchanged; empty replaces with none)",
				required = false) List<String> tools) {
		try {
			OperationResult result = doUpdateSkill(name, description, content, tools);
			return DataClassSerializer.toJson(result, AgentAuthoringTool.class, "updateSkill()");
		} catch (Exception ex) {
			Logger.error("updateSkill() error: " + ex.getMessage(), ex, AgentAuthoringTool.class);
			return DataClassSerializer.errorToJson("Failed to update skill: " + ex.getMessage(),
				AgentAuthoringTool.class, "updateSkill()");
		}
	}

	private OperationResult doUpdateSkill(String name, String description, String content,
			List<String> tools) {
		Skill skill = findSkill(name);
		if (skill == null) {
			throw new IllegalArgumentException("No application skill named '" + name + "'.");
		}
		if (tools != null) {
			validateSkillTools(tools);
		}
		try (Transaction tx = PersistencyLayer.getKnowledgeBase()
				.beginTransaction(ResKey.text("Update skill: " + name))) {
			if (description != null) {
				skill.setDescription(description);
			}
			if (content != null) {
				skill.setContent(content);
			}
			if (tools != null) {
				skill.setTools(toSet(tools));
			}
			tx.commit();
		}
		return success("Skill '" + name + "' updated.");
	}

	/**
	 * Deletes a application skill by name.
	 *
	 * @param name
	 *        Name of the skill to delete.
	 * @return JSON-serialized {@link OperationResult} describing the outcome.
	 */
	@ToolMethod(description = "Delete a application skill by name")
	public String deleteSkill(@P("Name of the skill to delete") String name) {
		try {
			OperationResult result = doDeleteSkill(name);
			return DataClassSerializer.toJson(result, AgentAuthoringTool.class, "deleteSkill()");
		} catch (Exception ex) {
			Logger.error("deleteSkill() error: " + ex.getMessage(), ex, AgentAuthoringTool.class);
			return DataClassSerializer.errorToJson("Failed to delete skill: " + ex.getMessage(),
				AgentAuthoringTool.class, "deleteSkill()");
		}
	}

	private OperationResult doDeleteSkill(String name) {
		Skill skill = findSkill(name);
		if (skill == null) {
			throw new IllegalArgumentException("No application skill named '" + name + "'.");
		}
		try (Transaction tx = PersistencyLayer.getKnowledgeBase()
				.beginTransaction(ResKey.text("Delete skill: " + name))) {
			skill.tDelete();
			tx.commit();
		}
		return success("Skill '" + name + "' deleted.");
	}

	// ---- Skill queries ---------------------------------------------------

	/**
	 * Lists all application skills.
	 *
	 * @return JSON-serialized {@link SkillListResult} with one summary per skill.
	 */
	@ToolMethod(description = "List all application skills", mode = ToolMode.READ)
	public String listSkills() {
		SkillListResult result = SkillListResult.create();
		for (Skill skill : allKBSkills()) {
			SkillSummary summary = SkillSummary.create();
			summary.setName(skill.getName());
			summary.setDescription(skill.getDescription());
			summary.setTools(new ArrayList<>(skill.getTools()));
			result.addSkill(summary);
		}
		return DataClassSerializer.toJson(result, AgentAuthoringTool.class, "listSkills()");
	}

	/**
	 * Gets the full definition of a application skill.
	 *
	 * @param name
	 *        Skill name.
	 * @return JSON-serialized {@link SkillDetail}; {@code exists} is {@code false} if no such skill.
	 */
	@ToolMethod(description = "Get the full definition of a application skill", mode = ToolMode.READ)
	public String getSkill(@P("Skill name") String name) {
		SkillDetail detail = SkillDetail.create();
		detail.setName(name);
		Skill skill = findSkill(name);
		if (skill == null) {
			detail.setExists(false);
		} else {
			detail.setExists(true);
			detail.setDescription(skill.getDescription());
			detail.setContent(skill.getContent());
			detail.setTools(new ArrayList<>(skill.getTools()));
		}
		return DataClassSerializer.toJson(detail, AgentAuthoringTool.class, "getSkill()");
	}

	// ---- Agent mutations -------------------------------------------------

	/**
	 * Creates a new agent in the application.
	 *
	 * @param name
	 *        Unique agent name.
	 * @param description
	 *        Short description of the agent.
	 * @param systemPrompt
	 *        System prompt for the agent.
	 * @param tools
	 *        Qualified tool names the agent may use, or {@code null} for none.
	 * @param mcpServers
	 *        MCP server names the agent may use, or {@code null} for none.
	 * @param skills
	 *        Skill names the agent loads, or {@code null} for none.
	 * @param delegations
	 *        Names of agents this agent may delegate to, or {@code null} for none.
	 * @return JSON-serialized {@link OperationResult} describing the outcome.
	 */
	@ToolMethod(description = "Create a new agent in the application")
	public String createAgent(
			@P("Unique agent name — do NOT use the TL_ prefix (reserved for TopLogic)") String name,
			@P("Short description of the agent") String description,
			@P("System prompt — use XML tags for sections, flat imperative rules, start with identity sentence") String systemPrompt,
			@P(value = "Qualified tool names the agent may use (see listAvailableTools); omit for none",
				required = false) List<String> tools,
			@P(value = "MCP server names the agent may use; omit for none", required = false) List<String> mcpServers,
			@P(value = "Skill names the agent loads; omit for none", required = false) List<String> skills,
			@P(value = "Names of agents this agent may delegate to; omit for none",
				required = false) List<String> delegations) {
		try {
			OperationResult result =
				doCreateAgent(name, description, systemPrompt, tools, mcpServers, skills, delegations);
			return DataClassSerializer.toJson(result, AgentAuthoringTool.class, "createAgent()");
		} catch (Exception ex) {
			Logger.error("createAgent() error: " + ex.getMessage(), ex, AgentAuthoringTool.class);
			return DataClassSerializer.errorToJson("Failed to create agent: " + ex.getMessage(),
				AgentAuthoringTool.class, "createAgent()");
		}
	}

	private OperationResult doCreateAgent(String name, String description, String systemPrompt,
			List<String> tools, List<String> mcpServers, List<String> skills, List<String> delegations) {
		requireName(name, "Agent");
		if (findAgent(name) != null) {
			throw new IllegalArgumentException("An agent named '" + name + "' already exists.");
		}
		List<String> warnings = validateReferences(tools, skills, delegations);
		try (Transaction tx = PersistencyLayer.getKnowledgeBase()
				.beginTransaction(ResKey.text("Create agent: " + name))) {
			Agent agent = TlAiCoreFactory.getInstance().createAgent();
			agent.setName(name);
			agent.setDescription(description);
			agent.setSystemPrompt(systemPrompt);
			agent.setTools(toSet(tools));
			agent.setMcpServers(toSet(mcpServers));
			agent.setSkills(toSet(skills));
			agent.setDelegations(toSet(delegations));
			tx.commit();
		}
		return result("Agent '" + name + "' created.", warnings);
	}

	/**
	 * Updates an existing application agent. Omitted fields are left unchanged.
	 *
	 * @param name
	 *        Name of the agent to update.
	 * @param description
	 *        New description, or {@code null} to leave unchanged.
	 * @param systemPrompt
	 *        New system prompt, or {@code null} to leave unchanged.
	 * @param tools
	 *        New tool list, or {@code null} to leave unchanged (an empty list replaces with none).
	 * @param mcpServers
	 *        New MCP server list, or {@code null} to leave unchanged (an empty list replaces with none).
	 * @param skills
	 *        New skill list, or {@code null} to leave unchanged (an empty list replaces with none).
	 * @param delegations
	 *        New delegation list, or {@code null} to leave unchanged (an empty list replaces with none).
	 * @return JSON-serialized {@link OperationResult} describing the outcome.
	 */
	@ToolMethod(description = "Update an existing application agent. Omitted fields are left unchanged.")
	public String updateAgent(
			@P("Name of the agent to update") String name,
			@P(value = "New description (omit to leave unchanged)", required = false) String description,
			@P(value = "New system prompt (omit to leave unchanged)", required = false) String systemPrompt,
			@P(value = "New tool list (omit to leave unchanged; empty replaces with none)",
				required = false) List<String> tools,
			@P(value = "New MCP server list (omit to leave unchanged; empty replaces with none)",
				required = false) List<String> mcpServers,
			@P(value = "New skill list (omit to leave unchanged; empty replaces with none)",
				required = false) List<String> skills,
			@P(value = "New delegation list (omit to leave unchanged; empty replaces with none)",
				required = false) List<String> delegations) {
		try {
			OperationResult result =
				doUpdateAgent(name, description, systemPrompt, tools, mcpServers, skills, delegations);
			return DataClassSerializer.toJson(result, AgentAuthoringTool.class, "updateAgent()");
		} catch (Exception ex) {
			Logger.error("updateAgent() error: " + ex.getMessage(), ex, AgentAuthoringTool.class);
			return DataClassSerializer.errorToJson("Failed to update agent: " + ex.getMessage(),
				AgentAuthoringTool.class, "updateAgent()");
		}
	}

	private OperationResult doUpdateAgent(String name, String description, String systemPrompt,
			List<String> tools, List<String> mcpServers, List<String> skills, List<String> delegations) {
		Agent agent = findAgent(name);
		if (agent == null) {
			throw new IllegalArgumentException("No application agent named '" + name + "'.");
		}
		List<String> warnings = validateReferences(tools, skills, delegations);
		try (Transaction tx = PersistencyLayer.getKnowledgeBase()
				.beginTransaction(ResKey.text("Update agent: " + name))) {
			if (description != null) {
				agent.setDescription(description);
			}
			if (systemPrompt != null) {
				agent.setSystemPrompt(systemPrompt);
			}
			if (tools != null) {
				agent.setTools(toSet(tools));
			}
			if (mcpServers != null) {
				agent.setMcpServers(toSet(mcpServers));
			}
			if (skills != null) {
				agent.setSkills(toSet(skills));
			}
			if (delegations != null) {
				agent.setDelegations(toSet(delegations));
			}
			tx.commit();
		}
		return result("Agent '" + name + "' updated.", warnings);
	}

	/**
	 * Deletes a application agent by name.
	 *
	 * @param name
	 *        Name of the agent to delete.
	 * @return JSON-serialized {@link OperationResult} describing the outcome.
	 */
	@ToolMethod(description = "Delete a application agent by name")
	public String deleteAgent(@P("Name of the agent to delete") String name) {
		try {
			OperationResult result = doDeleteAgent(name);
			return DataClassSerializer.toJson(result, AgentAuthoringTool.class, "deleteAgent()");
		} catch (Exception ex) {
			Logger.error("deleteAgent() error: " + ex.getMessage(), ex, AgentAuthoringTool.class);
			return DataClassSerializer.errorToJson("Failed to delete agent: " + ex.getMessage(),
				AgentAuthoringTool.class, "deleteAgent()");
		}
	}

	private OperationResult doDeleteAgent(String name) {
		Agent agent = findAgent(name);
		if (agent == null) {
			throw new IllegalArgumentException("No application agent named '" + name + "'.");
		}
		try (Transaction tx = PersistencyLayer.getKnowledgeBase()
				.beginTransaction(ResKey.text("Delete agent: " + name))) {
			agent.tDelete();
			tx.commit();
		}
		return success("Agent '" + name + "' deleted.");
	}

	// ---- Agent queries ---------------------------------------------------

	/**
	 * Lists all application agents.
	 *
	 * @return JSON-serialized {@link AgentListResult} with one summary per agent.
	 */
	@ToolMethod(description = "List all application agents", mode = ToolMode.READ)
	public String listAgents() {
		AgentListResult result = AgentListResult.create();
		for (Agent agent : allKBAgents()) {
			AgentSummary summary = AgentSummary.create();
			summary.setName(agent.getName());
			summary.setDescription(agent.getDescription());
			result.addAgent(summary);
		}
		return DataClassSerializer.toJson(result, AgentAuthoringTool.class, "listAgents()");
	}

	/**
	 * Gets the full definition of a application agent.
	 *
	 * @param name
	 *        Agent name.
	 * @return JSON-serialized {@link AgentDetail}; {@code exists} is {@code false} if no such agent.
	 */
	@ToolMethod(description = "Get the full definition of a application agent", mode = ToolMode.READ)
	public String getAgent(@P("Agent name") String name) {
		AgentDetail detail = AgentDetail.create();
		detail.setName(name);
		Agent agent = findAgent(name);
		if (agent == null) {
			detail.setExists(false);
		} else {
			detail.setExists(true);
			detail.setDescription(agent.getDescription());
			detail.setSystemPrompt(agent.getSystemPrompt());
			detail.setTools(new ArrayList<>(agent.getTools()));
			detail.setMcpServers(new ArrayList<>(agent.getMcpServers()));
			detail.setSkills(new ArrayList<>(agent.getSkills()));
			detail.setDelegations(new ArrayList<>(agent.getDelegations()));
		}
		return DataClassSerializer.toJson(detail, AgentAuthoringTool.class, "getAgent()");
	}

	/**
	 * Lists all tools available to give to an agent (qualified names and descriptions).
	 *
	 * @return JSON-serialized {@link ToolListResult} with one summary per registered tool.
	 */
	@ToolMethod(description = "List all tools available to give to an agent (qualified names + descriptions)",
		mode = ToolMode.READ)
	public String listAvailableTools() {
		ToolListResult result = ToolListResult.create();
		ToolRegistryService registry = ToolRegistryService.getInstance();
		List<String> names = new ArrayList<>(registry.getToolNames());
		names.sort(String::compareTo);
		for (String name : names) {
			Tool tool = registry.getTool(name);
			ToolSummary summary = ToolSummary.create();
			summary.setName(name);
			summary.setDescription(tool.description());
			summary.setMode(tool.mode().name());
			result.addTool(summary);
		}
		return DataClassSerializer.toJson(result, AgentAuthoringTool.class, "listAvailableTools()");
	}

	// ---- Shared helpers --------------------------------------------------

	private static Skill findSkill(String name) {
		List<Skill> skills = WrapperFactory.getWrappersByAttribute(
			AgentDefinitionQuery.SKILL_TABLE, PersistencyLayer.getKnowledgeBase(), SkillBase.NAME_ATTR, name);
		return skills.isEmpty() ? null : skills.get(0);
	}

	private static Collection<Skill> allKBSkills() {
		return WrapperFactory.getWrappersForKOs(Skill.class,
			PersistencyLayer.getKnowledgeBase().getAllKnowledgeObjects(AgentDefinitionQuery.SKILL_TABLE));
	}

	private static Agent findAgent(String name) {
		List<Agent> agents = WrapperFactory.getWrappersByAttribute(
			AgentDefinitionQuery.AGENT_TABLE, PersistencyLayer.getKnowledgeBase(), AgentBase.NAME_ATTR, name);
		return agents.isEmpty() ? null : agents.get(0);
	}

	private static Collection<Agent> allKBAgents() {
		return WrapperFactory.getWrappersForKOs(Agent.class,
			PersistencyLayer.getKnowledgeBase().getAllKnowledgeObjects(AgentDefinitionQuery.AGENT_TABLE));
	}

	private static Set<String> toSet(List<String> values) {
		return values == null ? Set.of() : new LinkedHashSet<>(values);
	}

	private static OperationResult result(String message, List<String> warnings) {
		OperationResult result = OperationResult.create();
		result.setSuccess(true);
		result.setResultMessage(message);
		for (String warning : warnings) {
			result.addWarning(warning);
		}
		return result;
	}

	private List<String> validateReferences(List<String> tools, List<String> skills, List<String> delegations) {
		Set<String> validTools = (tools == null || tools.isEmpty())
			? Set.of()
			: ToolRegistryService.getInstance().getToolNames();
		Predicate<String> skillExists =
			skillName -> AgentDefinitionQuery.getInstance().findSkillByName(skillName) != null;
		Predicate<String> agentExists =
			agentName -> AgentDefinitionQuery.getInstance().findAgentByName(agentName) != null;
		return checkReferences(tools, skills, delegations, validTools, skillExists, agentExists);
	}

	/**
	 * Validates references on an agent definition.
	 *
	 * @param validToolNames
	 *        Qualified tool names known to the registry. Unknown tools are a hard error.
	 * @param skillExists
	 *        Predicate telling whether a skill name resolves. Unknown skills are warnings.
	 * @param agentExists
	 *        Predicate telling whether a delegation target resolves. Unknown targets are warnings.
	 * @return The list of non-fatal warnings.
	 * @throws IllegalArgumentException
	 *         If any tool name is not in {@code validToolNames}.
	 */
	public static List<String> checkReferences(List<String> tools, List<String> skills, List<String> delegations,
			Set<String> validToolNames, Predicate<String> skillExists, Predicate<String> agentExists) {
		if (tools != null) {
			List<String> unknown = new ArrayList<>();
			for (String tool : tools) {
				if (!validToolNames.contains(tool)) {
					unknown.add(tool);
				}
			}
			if (!unknown.isEmpty()) {
				throw new IllegalArgumentException("Unknown tool(s): " + unknown
					+ ". Call listAvailableTools to see valid qualified tool names.");
			}
		}
		List<String> warnings = new ArrayList<>();
		if (skills != null) {
			for (String skill : skills) {
				if (!skillExists.test(skill)) {
					warnings.add("Skill '" + skill + "' does not exist yet.");
				}
			}
		}
		if (delegations != null) {
			for (String delegation : delegations) {
				if (!agentExists.test(delegation)) {
					warnings.add("Delegation target '" + delegation + "' does not exist yet.");
				}
			}
		}
		return warnings;
	}

	private static void validateSkillTools(List<String> tools) {
		if (tools == null || tools.isEmpty()) {
			return;
		}
		Set<String> validTools = ToolRegistryService.getInstance().getToolNames();
		List<String> unknown = new ArrayList<>();
		for (String tool : tools) {
			if (!validTools.contains(tool)) {
				unknown.add(tool);
			}
		}
		if (!unknown.isEmpty()) {
			throw new IllegalArgumentException("Unknown tool(s): " + unknown
				+ ". Call listAvailableTools to see valid qualified tool names.");
		}
	}

	private static void requireName(String name, String kind) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException(kind + " name is required.");
		}
	}

	private static OperationResult success(String message) {
		OperationResult result = OperationResult.create();
		result.setSuccess(true);
		result.setResultMessage(message);
		return result;
	}

}
