/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.tools.authoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.top_logic.ai.tool.api.AnswerMarking;
import com.top_logic.ai.tool.api.P;
import com.top_logic.ai.tool.api.Tool;
import com.top_logic.ai.tool.api.ToolGroups;
import com.top_logic.ai.tool.api.ToolMethod;
import com.top_logic.ai.tool.api.ToolMode;
import com.top_logic.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.TLContext;
import com.top_logic.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.ai.core.agent.ToolContext;
import com.top_logic.ai.core.model.Agent;
import com.top_logic.ai.core.model.Skill;
import com.top_logic.ai.core.model.TlAiCoreFactory;
import com.top_logic.ai.core.security.ModelSecurity;
import com.top_logic.ai.core.security.ToolSecurityException;
import com.top_logic.ai.core.tools.I18NConstants;
import com.top_logic.ai.core.tools.authoring.messages.AgentDetail;
import com.top_logic.ai.core.tools.authoring.messages.AgentListResult;
import com.top_logic.ai.core.tools.authoring.messages.AgentSummary;
import com.top_logic.ai.core.tools.authoring.messages.OperationResult;
import com.top_logic.ai.core.tools.authoring.messages.SkillDetail;
import com.top_logic.ai.core.tools.authoring.messages.SkillListResult;
import com.top_logic.ai.core.tools.authoring.messages.SkillSummary;
import com.top_logic.ai.core.tools.authoring.messages.ToolListResult;
import com.top_logic.ai.core.tools.authoring.messages.ToolSummary;
import com.top_logic.ai.core.tools.util.DataClassSerializer;

/**
 * Tool for creating, inspecting, updating, and deleting application Agents and Skills.
 *
 * <p>
 * Mutating methods inherit the default {@link ToolMode#STRUCTURAL} mode, so they require human
 * approval and are blocked in production. Query methods are {@link ToolMode#READ}. This tool only
 * sees application agents/skills; framework (TopLogic-provided) ones are not visible or editable here.
 * </p>
 *
 * <p>
 * Every method checks the model rights of the run ({@link ToolContext#modelSecurity()}), as a form
 * of the application would: a stored agent or skill the run may not read is unknown, and creating,
 * changing and deleting one needs Create, Write and Delete. A change of an attribute with rights of
 * its own (the MCP servers of an agent) needs Write on that attribute, and an answer holds only the
 * attributes the run may read. A name is free only when no stored record has it, also one the run
 * may not read, because names are unique in the whole application. A change without a user account
 * or in a system context is refused.
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
	 * The commit message of {@link #createSkill(String, String, String, List, ToolContext)}.
	 */
	@CalledByReflection
	ResKey createSkillMessage(String name, String description, String content, List<String> tools,
			ToolContext context) {
		return I18NConstants.SKILL_CREATED__NAME.fill(name);
	}

	/**
	 * Creates a new application skill (reusable instruction text).
	 *
	 * @param name
	 *        Unique skill name.
	 * @param description
	 *        Short description shown in the skill catalog.
	 * @param content
	 *        The skill's instruction content (markdown).
	 * @param context
	 *        The run, whose model rights decide.
	 * @return JSON-serialized {@link OperationResult} describing the outcome.
	 */
	@ToolMethod(description = "Create a new skill (instruction text + optional tools) in the application", group = ToolGroups.AGENT_AUTHORING, marking = AnswerMarking.NONE, commitMessage = "createSkillMessage")
	public String createSkill(
			@P("Unique skill name — do NOT use the TL_ prefix (reserved for TopLogic)") String name,
			@P("Short description that tells the agent WHEN to load this skill, not what it does internally") String description,
			@P("Skill content — use XML tags for sections (<core_rules>, <workflow>, etc.), flat imperative rules, no filler prose") String content,
			@P(value = "Qualified tool names this skill provides when loaded (see listAvailableTools); omit for none",
				required = false) List<String> tools,
			ToolContext context) {
		return perform("createSkill()", "Failed to create skill: ",
			() -> doCreateSkill(name, description, content, tools, context.modelSecurity()));
	}

	private OperationResult doCreateSkill(String name, String description, String content,
			List<String> tools, ModelSecurity rights) {
		requireName(name, "Skill");
		// The right first: who may not create a skill learns nothing about the names that exist.
		if (!rights.canCreate(TlAiCoreFactory.getSkillType(), null)) {
			throw new ToolSecurityException(SimpleBoundCommandGroup.CREATE,
				TLModelUtil.qualifiedName(TlAiCoreFactory.getSkillType()));
		}
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
			requireInitial(rights, skill, TlAiCoreFactory.getNameSkillAttr(), name);
			requireInitial(rights, skill, TlAiCoreFactory.getDescriptionSkillAttr(), description);
			requireInitial(rights, skill, TlAiCoreFactory.getContentSkillAttr(), content);
			requireInitial(rights, skill, TlAiCoreFactory.getToolsSkillAttr(), tools);
			tx.commit();
		}
		return success("Skill '" + name + "' created.");
	}

	/**
	 * The commit message of {@link #updateSkill(String, String, String, List, ToolContext)}.
	 */
	@CalledByReflection
	ResKey updateSkillMessage(String name, String description, String content, List<String> tools,
			ToolContext context) {
		return I18NConstants.SKILL_UPDATED__NAME.fill(name);
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
	 * @param context
	 *        The run, whose model rights decide.
	 * @return JSON-serialized {@link OperationResult} describing the outcome.
	 */
	@ToolMethod(description = "Update an existing application skill. Omitted fields are left unchanged.", group = ToolGroups.AGENT_AUTHORING, marking = AnswerMarking.NONE, commitMessage = "updateSkillMessage")
	public String updateSkill(
			@P("Name of the skill to update") String name,
			@P(value = "New description — tells the agent WHEN to load (omit to leave unchanged)", required = false) String description,
			@P(value = "New content — use XML tags, flat imperative rules, no filler (omit to leave unchanged)", required = false) String content,
			@P(value = "New tool list (omit to leave unchanged; empty replaces with none)",
				required = false) List<String> tools,
			ToolContext context) {
		return perform("updateSkill()", "Failed to update skill: ",
			() -> doUpdateSkill(name, description, content, tools, context.modelSecurity()));
	}

	private OperationResult doUpdateSkill(String name, String description, String content,
			List<String> tools, ModelSecurity rights) {
		Skill skill = readable(findSkill(name), rights);
		if (skill == null) {
			throw new IllegalArgumentException("No application skill named '" + name + "'.");
		}
		if (tools != null) {
			validateSkillTools(tools);
		}
		requireWrite(rights, skill);
		requireWrite(rights, skill, TlAiCoreFactory.getDescriptionSkillAttr(), description);
		requireWrite(rights, skill, TlAiCoreFactory.getContentSkillAttr(), content);
		requireWrite(rights, skill, TlAiCoreFactory.getToolsSkillAttr(), tools);
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
	 * The commit message of {@link #deleteSkill(String, ToolContext)}.
	 */
	@CalledByReflection
	ResKey deleteSkillMessage(String name, ToolContext context) {
		return I18NConstants.SKILL_DELETED__NAME.fill(name);
	}

	/**
	 * Deletes a application skill by name.
	 *
	 * @param name
	 *        Name of the skill to delete.
	 * @param context
	 *        The run, whose model rights decide.
	 * @return JSON-serialized {@link OperationResult} describing the outcome.
	 */
	@ToolMethod(description = "Delete a application skill by name", group = ToolGroups.AGENT_AUTHORING, marking = AnswerMarking.NONE, commitMessage = "deleteSkillMessage")
	public String deleteSkill(@P("Name of the skill to delete") String name, ToolContext context) {
		return perform("deleteSkill()", "Failed to delete skill: ",
			() -> doDeleteSkill(name, context.modelSecurity()));
	}

	private OperationResult doDeleteSkill(String name, ModelSecurity rights) {
		Skill skill = readable(findSkill(name), rights);
		if (skill == null) {
			throw new IllegalArgumentException("No application skill named '" + name + "'.");
		}
		requireDelete(rights, skill);
		try (Transaction tx = PersistencyLayer.getKnowledgeBase()
				.beginTransaction(ResKey.text("Delete skill: " + name))) {
			skill.tDelete();
			tx.commit();
		}
		return success("Skill '" + name + "' deleted.");
	}

	// ---- Skill queries ---------------------------------------------------

	/**
	 * Lists all application skills the run may read.
	 *
	 * @param context
	 *        The run, whose model rights decide.
	 * @return JSON-serialized {@link SkillListResult} with one summary per skill.
	 */
	@ToolMethod(description = "List all application skills", mode = ToolMode.READ, group = ToolGroups.AGENT_META)
	public String listSkills(ToolContext context) {
		ModelSecurity rights = context.modelSecurity();
		SkillListResult result = SkillListResult.create();
		for (Skill skill : rights.filterReadable(allKBSkills())) {
			SkillSummary summary = SkillSummary.create();
			summary.setName(skill.getName());
			if (rights.canRead(skill, TlAiCoreFactory.getDescriptionSkillAttr())) {
				summary.setDescription(skill.getDescription());
			}
			if (rights.canRead(skill, TlAiCoreFactory.getToolsSkillAttr())) {
				summary.setTools(new ArrayList<>(skill.getTools()));
			}
			result.addSkill(summary);
		}
		return DataClassSerializer.toJson(result, AgentAuthoringTool.class, "listSkills()");
	}

	/**
	 * Gets the full definition of a application skill.
	 *
	 * @param name
	 *        Skill name.
	 * @param context
	 *        The run, whose model rights decide.
	 * @return JSON-serialized {@link SkillDetail}; {@code exists} is {@code false} if the run knows
	 *         no such skill.
	 */
	@ToolMethod(description = "Get the full definition of a application skill", mode = ToolMode.READ, group = ToolGroups.AGENT_META)
	public String getSkill(@P("Skill name") String name, ToolContext context) {
		ModelSecurity rights = context.modelSecurity();
		SkillDetail detail = SkillDetail.create();
		detail.setName(name);
		Skill skill = readable(findSkill(name), rights);
		if (skill == null) {
			detail.setExists(false);
		} else {
			// Each attribute only where the run may read it: an application can hide the text of a
			// skill with a <part> grant.
			detail.setExists(true);
			if (rights.canRead(skill, TlAiCoreFactory.getDescriptionSkillAttr())) {
				detail.setDescription(skill.getDescription());
			}
			if (rights.canRead(skill, TlAiCoreFactory.getContentSkillAttr())) {
				detail.setContent(skill.getContent());
			}
			if (rights.canRead(skill, TlAiCoreFactory.getToolsSkillAttr())) {
				detail.setTools(new ArrayList<>(skill.getTools()));
			}
		}
		return DataClassSerializer.toJson(detail, AgentAuthoringTool.class, "getSkill()");
	}

	// ---- Agent mutations -------------------------------------------------

	/**
	 * The commit message of
	 * {@link #createAgent(String, String, String, List, List, List, List, ToolContext)}.
	 */
	@CalledByReflection
	ResKey createAgentMessage(String name, String description, String systemPrompt, List<String> tools,
			List<String> mcpServers, List<String> skills, List<String> delegations, ToolContext context) {
		return I18NConstants.AGENT_CREATED__NAME.fill(name);
	}

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
	 * @param context
	 *        The run, whose model rights decide.
	 * @return JSON-serialized {@link OperationResult} describing the outcome.
	 */
	@ToolMethod(description = "Create a new agent in the application", group = ToolGroups.AGENT_AUTHORING, marking = AnswerMarking.NONE, commitMessage = "createAgentMessage")
	public String createAgent(
			@P("Unique agent name — do NOT use the TL_ prefix (reserved for TopLogic)") String name,
			@P("Short description of the agent") String description,
			@P("System prompt — use XML tags for sections, flat imperative rules, start with identity sentence") String systemPrompt,
			@P(value = "Qualified tool names the agent may use (see listAvailableTools); omit for none",
				required = false) List<String> tools,
			@P(value = "MCP server names the agent may use; omit for none", required = false) List<String> mcpServers,
			@P(value = "Skill names the agent loads; omit for none", required = false) List<String> skills,
			@P(value = "Names of agents this agent may delegate to; omit for none",
				required = false) List<String> delegations,
			ToolContext context) {
		return perform("createAgent()", "Failed to create agent: ", () -> doCreateAgent(name, description,
			systemPrompt, tools, mcpServers, skills, delegations, context.modelSecurity()));
	}

	private OperationResult doCreateAgent(String name, String description, String systemPrompt,
			List<String> tools, List<String> mcpServers, List<String> skills, List<String> delegations,
			ModelSecurity rights) {
		requireName(name, "Agent");
		// The right first: who may not create an agent learns nothing about the names that exist.
		if (!rights.canCreate(TlAiCoreFactory.getAgentType(), null)) {
			throw new ToolSecurityException(SimpleBoundCommandGroup.CREATE,
				TLModelUtil.qualifiedName(TlAiCoreFactory.getAgentType()));
		}
		if (findAgent(name) != null) {
			throw new IllegalArgumentException("An agent named '" + name + "' already exists.");
		}
		List<String> warnings = validateReferences(tools, skills, delegations, rights);
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
			requireInitial(rights, agent, TlAiCoreFactory.getNameAgentAttr(), name);
			requireInitial(rights, agent, TlAiCoreFactory.getDescriptionAgentAttr(), description);
			requireInitial(rights, agent, TlAiCoreFactory.getSystemPromptAgentAttr(), systemPrompt);
			requireInitial(rights, agent, TlAiCoreFactory.getToolsAgentAttr(), tools);
			requireInitial(rights, agent, TlAiCoreFactory.getMcpServersAgentAttr(), mcpServers);
			requireInitial(rights, agent, TlAiCoreFactory.getSkillsAgentAttr(), skills);
			requireInitial(rights, agent, TlAiCoreFactory.getDelegationsAgentAttr(), delegations);
			tx.commit();
		}
		return result("Agent '" + name + "' created.", warnings);
	}

	/**
	 * The commit message of
	 * {@link #updateAgent(String, String, String, List, List, List, List, ToolContext)}.
	 */
	@CalledByReflection
	ResKey updateAgentMessage(String name, String description, String systemPrompt, List<String> tools,
			List<String> mcpServers, List<String> skills, List<String> delegations, ToolContext context) {
		return I18NConstants.AGENT_UPDATED__NAME.fill(name);
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
	 * @param context
	 *        The run, whose model rights decide.
	 * @return JSON-serialized {@link OperationResult} describing the outcome.
	 */
	@ToolMethod(description = "Update an existing application agent. Omitted fields are left unchanged.", group = ToolGroups.AGENT_AUTHORING, marking = AnswerMarking.NONE, commitMessage = "updateAgentMessage")
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
				required = false) List<String> delegations,
			ToolContext context) {
		return perform("updateAgent()", "Failed to update agent: ", () -> doUpdateAgent(name, description,
			systemPrompt, tools, mcpServers, skills, delegations, context.modelSecurity()));
	}

	private OperationResult doUpdateAgent(String name, String description, String systemPrompt,
			List<String> tools, List<String> mcpServers, List<String> skills, List<String> delegations,
			ModelSecurity rights) {
		Agent agent = readable(findAgent(name), rights);
		if (agent == null) {
			throw new IllegalArgumentException("No application agent named '" + name + "'.");
		}
		List<String> warnings = validateReferences(tools, skills, delegations, rights);
		requireWrite(rights, agent);
		requireWrite(rights, agent, TlAiCoreFactory.getDescriptionAgentAttr(), description);
		requireWrite(rights, agent, TlAiCoreFactory.getSystemPromptAgentAttr(), systemPrompt);
		requireWrite(rights, agent, TlAiCoreFactory.getToolsAgentAttr(), tools);
		requireWrite(rights, agent, TlAiCoreFactory.getMcpServersAgentAttr(), mcpServers);
		requireWrite(rights, agent, TlAiCoreFactory.getSkillsAgentAttr(), skills);
		requireWrite(rights, agent, TlAiCoreFactory.getDelegationsAgentAttr(), delegations);
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
	 * The commit message of {@link #deleteAgent(String, ToolContext)}.
	 */
	@CalledByReflection
	ResKey deleteAgentMessage(String name, ToolContext context) {
		return I18NConstants.AGENT_DELETED__NAME.fill(name);
	}

	/**
	 * Deletes a application agent by name.
	 *
	 * @param name
	 *        Name of the agent to delete.
	 * @param context
	 *        The run, whose model rights decide.
	 * @return JSON-serialized {@link OperationResult} describing the outcome.
	 */
	@ToolMethod(description = "Delete a application agent by name", group = ToolGroups.AGENT_AUTHORING, marking = AnswerMarking.NONE, commitMessage = "deleteAgentMessage")
	public String deleteAgent(@P("Name of the agent to delete") String name, ToolContext context) {
		return perform("deleteAgent()", "Failed to delete agent: ",
			() -> doDeleteAgent(name, context.modelSecurity()));
	}

	private OperationResult doDeleteAgent(String name, ModelSecurity rights) {
		Agent agent = readable(findAgent(name), rights);
		if (agent == null) {
			throw new IllegalArgumentException("No application agent named '" + name + "'.");
		}
		requireDelete(rights, agent);
		try (Transaction tx = PersistencyLayer.getKnowledgeBase()
				.beginTransaction(ResKey.text("Delete agent: " + name))) {
			agent.tDelete();
			tx.commit();
		}
		return success("Agent '" + name + "' deleted.");
	}

	// ---- Agent queries ---------------------------------------------------

	/**
	 * Lists all application agents the run may read.
	 *
	 * @param context
	 *        The run, whose model rights decide.
	 * @return JSON-serialized {@link AgentListResult} with one summary per agent.
	 */
	@ToolMethod(description = "List all application agents", mode = ToolMode.READ, group = ToolGroups.AGENT_META)
	public String listAgents(ToolContext context) {
		ModelSecurity rights = context.modelSecurity();
		AgentListResult result = AgentListResult.create();
		for (Agent agent : rights.filterReadable(allKBAgents())) {
			AgentSummary summary = AgentSummary.create();
			summary.setName(agent.getName());
			if (rights.canRead(agent, TlAiCoreFactory.getDescriptionAgentAttr())) {
				summary.setDescription(agent.getDescription());
			}
			result.addAgent(summary);
		}
		return DataClassSerializer.toJson(result, AgentAuthoringTool.class, "listAgents()");
	}

	/**
	 * Gets the full definition of a application agent.
	 *
	 * @param name
	 *        Agent name.
	 * @param context
	 *        The run, whose model rights decide.
	 * @return JSON-serialized {@link AgentDetail}; {@code exists} is {@code false} if the run knows
	 *         no such agent.
	 */
	@ToolMethod(description = "Get the full definition of a application agent", mode = ToolMode.READ, group = ToolGroups.AGENT_META)
	public String getAgent(@P("Agent name") String name, ToolContext context) {
		ModelSecurity rights = context.modelSecurity();
		AgentDetail detail = AgentDetail.create();
		detail.setName(name);
		Agent agent = readable(findAgent(name), rights);
		if (agent == null) {
			detail.setExists(false);
		} else {
			// Each attribute only where the run may read it: an application can hide the instructions
			// of an agent with a <part> grant.
			detail.setExists(true);
			if (rights.canRead(agent, TlAiCoreFactory.getDescriptionAgentAttr())) {
				detail.setDescription(agent.getDescription());
			}
			if (rights.canRead(agent, TlAiCoreFactory.getSystemPromptAgentAttr())) {
				detail.setSystemPrompt(agent.getSystemPrompt());
			}
			if (rights.canRead(agent, TlAiCoreFactory.getToolsAgentAttr())) {
				detail.setTools(new ArrayList<>(agent.getTools()));
			}
			if (rights.canRead(agent, TlAiCoreFactory.getMcpServersAgentAttr())) {
				detail.setMcpServers(new ArrayList<>(agent.getMcpServers()));
			}
			if (rights.canRead(agent, TlAiCoreFactory.getSkillsAgentAttr())) {
				detail.setSkills(new ArrayList<>(agent.getSkills()));
			}
			if (rights.canRead(agent, TlAiCoreFactory.getDelegationsAgentAttr())) {
				detail.setDelegations(new ArrayList<>(agent.getDelegations()));
			}
		}
		return DataClassSerializer.toJson(detail, AgentAuthoringTool.class, "getAgent()");
	}

	/**
	 * Lists all tools available to give to an agent (qualified names and descriptions).
	 *
	 * @return JSON-serialized {@link ToolListResult} with one summary per registered tool.
	 */
	@ToolMethod(description = "List all tools available to give to an agent (qualified names + descriptions)",
		mode = ToolMode.READ, group = ToolGroups.AGENT_META)
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

	/**
	 * Runs the given operation and answers its result, or the reason it failed, as JSON.
	 *
	 * <p>
	 * A refusal by the model rights is logged at INFO and not as an error: an account without a
	 * right is a configured state, not a failure of the application.
	 * </p>
	 *
	 * <p>
	 * A call without a user account, or in a system context, is refused before anything: there the
	 * application checks no rights and records no author, as for the object tools.
	 * </p>
	 */
	private static String perform(String operationName, String failure, Supplier<OperationResult> operation) {
		if (ThreadContext.isSecurityBypassed() || TLContext.currentUser() == null) {
			return DataClassSerializer.errorToJson(failure + "This call runs without a user account, in a system"
				+ " context where the application checks no rights and records no author. Agents and skills are"
				+ " only changed in the name of the user the run acts for. Nothing was changed.",
				AgentAuthoringTool.class, operationName);
		}
		try {
			return DataClassSerializer.toJson(operation.get(), AgentAuthoringTool.class, operationName);
		} catch (ToolSecurityException ex) {
			Logger.info(operationName + " refused: " + ex.getMessage(), AgentAuthoringTool.class);
			return DataClassSerializer.errorToJson(failure + ex.getMessage(), AgentAuthoringTool.class,
				operationName);
		} catch (Exception ex) {
			Logger.error(operationName + " error: " + ex.getMessage(), ex, AgentAuthoringTool.class);
			return DataClassSerializer.errorToJson(failure + ex.getMessage(), AgentAuthoringTool.class,
				operationName);
		}
	}

	/**
	 * The given record, or <code>null</code> if there is none or the run may not read it: for the
	 * run, a record it may not read does not exist.
	 */
	private static <T extends TLObject> T readable(T record, ModelSecurity rights) {
		return record != null && rights.canRead(record) ? record : null;
	}

	/** Refuses unless the run may change the given record. */
	private static void requireWrite(ModelSecurity rights, TLObject record) {
		if (!rights.canWrite(record)) {
			throw new ToolSecurityException(SimpleBoundCommandGroup.WRITE, TLModelUtil.qualifiedName(record.tType()));
		}
	}

	/**
	 * Refuses unless the run may change the given attribute of the record, when the call gives the
	 * attribute a value.
	 */
	private static void requireWrite(ModelSecurity rights, TLObject record, TLStructuredTypePart attribute,
			Object value) {
		if (value != null && !rights.canWrite(record, attribute)) {
			throw attributeRefusal(attribute);
		}
	}

	/**
	 * Refuses unless the run may set the given attribute of the record it creates, when the call
	 * gives the attribute a value. An empty list sets nothing.
	 */
	private static void requireInitial(ModelSecurity rights, TLObject draft, TLStructuredTypePart attribute,
			Object value) {
		if (isEmpty(value)) {
			return;
		}
		if (!rights.canSetInitial(draft, attribute)) {
			throw attributeRefusal(attribute);
		}
	}

	private static boolean isEmpty(Object value) {
		return value == null || (value instanceof List<?> list && list.isEmpty());
	}

	private static ToolSecurityException attributeRefusal(TLStructuredTypePart attribute) {
		return new ToolSecurityException(SimpleBoundCommandGroup.WRITE, TLModelUtil.qualifiedName(attribute));
	}

	/** Refuses unless the run may delete the given record. */
	private static void requireDelete(ModelSecurity rights, TLObject record) {
		if (!rights.canDelete(record)) {
			throw new ToolSecurityException(SimpleBoundCommandGroup.DELETE, TLModelUtil.qualifiedName(record.tType()));
		}
	}

	private static Skill findSkill(String name) {
		return AgentDefinitionQuery.getInstance().findStoredSkill(name);
	}

	private static Collection<Skill> allKBSkills() {
		return WrapperFactory.getWrappersForKOs(Skill.class,
			PersistencyLayer.getKnowledgeBase().getAllKnowledgeObjects(AgentDefinitionQuery.SKILL_TABLE));
	}

	private static Agent findAgent(String name) {
		return AgentDefinitionQuery.getInstance().findStoredAgent(name);
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

	private List<String> validateReferences(List<String> tools, List<String> skills, List<String> delegations,
			ModelSecurity rights) {
		Set<String> validTools = (tools == null || tools.isEmpty())
			? Set.of()
			: ToolRegistryService.getInstance().getToolNames();
		Predicate<String> skillExists =
			skillName -> AgentDefinitionQuery.getInstance().findSkillByName(skillName, rights) != null;
		Predicate<String> agentExists =
			agentName -> AgentDefinitionQuery.getInstance().findAgentByName(agentName, rights) != null;
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
