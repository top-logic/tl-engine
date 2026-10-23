/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.order.DisplayOrder;

import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.annotation.LabelPositioning;
import com.top_logic.layout.form.values.edit.annotation.MultiLine;
import com.top_logic.model.annotate.LabelPosition;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.tl.ai.core.agent.environment.AgentFact;
import com.top_logic.tl.ai.core.agent.environment.PromptPlaceholders;
import com.top_logic.tl.ai.tool.api.AICategory;

/**
 * TopLogic-provided agent and skill definitions.
 *
 * <p>
 * These are platform-level definitions shipped with TopLogic. Application-specific agents and
 * skills should be created via the Agent Editor.
 * </p>
 *
 * <p>
 * Application modules may extend a platform agent's keyed lists ({@link AgentConfig#getTools
 * tools}, {@link AgentConfig#getSkills skills}, {@link AgentConfig#getDelegations delegations},
 * {@link AgentConfig#getMcpServers mcp-servers}) via the standard configuration overlay
 * mechanism: an overlay only declares the entries it adds or removes, and the platform's
 * remaining entries flow through unchanged. Scalar properties such as {@code description} and
 * {@code system-prompt} are still atomically replaced when overridden, so overlays should leave
 * them unset unless a true replacement is intended. The two texts a user reads -
 * {@link AgentConfig#getDisplayName display-name} and {@link AgentConfig#getSummary summary} - are
 * overridden the same way; that is how an installation renames an agent this product ships.
 * </p>
 *
 * @see AgentDefinition
 * @see SkillDefinition
 * @see AgentSource#CONFIG
 * @see NameRef
 */
@Label("TopLogic-Agents and Skills")
@ServiceDependencies({})
@ServiceCategory.Of(AICategory.class)
public class TopLogicAgentDefinitions extends ConfiguredManagedClass<TopLogicAgentDefinitions.Config<?>> {

	/**
	 * Configuration for a single agent definition.
	 *
	 * <p>
	 * Contains only identity and capabilities. Tuning parameters belong in
	 * {@link AgentRuntimeService.AgentOverride AgentOverride}.
	 * </p>
	 *
	 * <p>
	 * This is where an installation renames an agent it was shipped, or describes it in its own
	 * words. What is saved here reaches the users when this service is restarted - the restart
	 * button in the list of services, or the next start of the application; saving alone changes
	 * nothing a running agent does.
	 * </p>
	 *
	 * <p>
	 * Saving writes the configuration of every agent as it currently reads, so from then on this
	 * installation carries its own copy of them and a later release's wording, prompt or tool list
	 * no longer flows through. An installation that wants to change two texts and keep receiving
	 * the rest writes the overlay by hand instead, naming only the properties it replaces.
	 * </p>
	 */
	@DisplayOrder({
		AgentConfig.DISPLAY_NAME,
		AgentConfig.SUMMARY,
		AgentConfig.DESCRIPTION,
		AgentConfig.TOOLS,
		AgentConfig.SKILLS,
		AgentConfig.DELEGATIONS,
		AgentConfig.MCP_SERVERS,
		AgentConfig.SYSTEM_PROMPT,
		AgentConfig.FACTS,
	})
	public interface AgentConfig extends NamedConfigMandatory {

		/** Configuration property name for {@link #getDescription()}. */
		String DESCRIPTION = "description";

		/** Configuration property name for {@link #getDisplayName()}. */
		String DISPLAY_NAME = "display-name";

		/** Configuration property name for {@link #getSummary()}. */
		String SUMMARY = "summary";

		/** Configuration property name for {@link #getSystemPrompt()}. */
		String SYSTEM_PROMPT = "system-prompt";

		/** Configuration property name for {@link #isInfrastructure()}. */
		String INFRASTRUCTURE = "infrastructure";

		/** Configuration property name for {@link #getFacts()}. */
		String FACTS = "facts";

		/** Configuration property name for {@link #getTools()}. */
		String TOOLS = "tools";

		/** Configuration property name for {@link #getMcpServers()}. */
		String MCP_SERVERS = "mcp-servers";

		/** Configuration property name for {@link #getSkills()}. */
		String SKILLS = "skills";

		/** Configuration property name for {@link #getDelegations()}. */
		String DELEGATIONS = "delegations";

		/** Configuration property name for {@link #getRemovedTools()}. */
		String REMOVED_TOOLS = "removed-tools";

		/** Configuration property name for {@link #getRemovedSkills()}. */
		String REMOVED_SKILLS = "removed-skills";

		/** Configuration property name for {@link #getRemovedDelegations()}. */
		String REMOVED_DELEGATIONS = "removed-delegations";

		/**
		 * Human-readable description of the agent.
		 */
		@Name(DESCRIPTION)
		@Nullable
		@LabelPositioning(LabelPosition.ABOVE)
		@MultiLine
		String getDescription();

		/**
		 * What to call this agent in front of a user, in their language.
		 *
		 * <p>
		 * This is the name the chat's agent menu shows, and it is the one field an installation
		 * changes to rename an agent it was shipped. Leave it out and the agent is offered under the
		 * technical name above, which is its identifier: other configuration, the skills and every
		 * conversation already held with this agent refer to that identifier, so it stays as it is
		 * even when it reads badly - this is the field to change instead.
		 * </p>
		 *
		 * <p>
		 * Takes effect when this service is restarted.
		 * </p>
		 */
		@Name(DISPLAY_NAME)
		@Nullable
		ResKey getDisplayName();

		/**
		 * One line saying what this agent is for, in the language of whoever reads it.
		 *
		 * <p>
		 * This is the line under the name in the chat's agent menu, and the second field an
		 * installation changes to describe an agent it was shipped in its own words. Leave it out and
		 * the agent is offered by its name alone - which is better than showing the description
		 * below, because that one is written for a model rather than for a person.
		 * </p>
		 *
		 * <p>
		 * Takes effect when this service is restarted.
		 * </p>
		 */
		@Name(SUMMARY)
		@Nullable
		ResKey getSummary();

		/**
		 * The agent's system prompt text.
		 */
		@Name(SYSTEM_PROMPT)
		@Nullable
		@LabelPositioning(LabelPosition.ABOVE)
		@MultiLine
		String getSystemPrompt();

		/**
		 * Whether the application runs this agent <em>for</em> a user instead of letting them talk to
		 * it.
		 *
		 * <p>
		 * The chat's prompt improver is such an agent: it rewrites the draft a user is about to send
		 * and never answers as a persona. An agent marked here is offered by no menu and is refused
		 * wherever a user's own run would name it, whatever the agent access policy grants - see
		 * the developer article {@code ai/security/run-access}. The product's own call path stays open:
		 * it builds its run with {@code SecurityContext.withoutAgentChoice()}, which is what says in
		 * code that the agent is not the user's choice.
		 * </p>
		 */
		@Name(INFRASTRUCTURE)
		boolean isInfrastructure();

		/**
		 * What this agent tells every model about the situation it runs in.
		 *
		 * <p>
		 * Each fact is TL-Script of no arguments, evaluated in the acting user's context when a run
		 * starts, and rendered into the {@code {{context}}} section of the environment block. These are
		 * the facts that need no page, so they are the same whether the run comes from the chat, from
		 * {@code aiAgentChat} or from the scheduler; a fact that needs what a page is showing is
		 * declared on the chat conversation element instead.
		 * </p>
		 *
		 * @see FactConfig#getStable()
		 */
		@Name(FACTS)
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		@EntryTag("fact")
		List<FactConfig> getFacts();

		/**
		 * Qualified tool names available to this agent.
		 *
		 * <p>
		 * Per-entry keyed list: application overlays may add or remove individual tools without
		 * restating the full platform list. See {@link NameRef}.
		 * </p>
		 */
		@Name(TOOLS)
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		@EntryTag("tool")
		List<ToolRef> getTools();

		/**
		 * MCP server names available to this agent.
		 *
		 * <p>
		 * Per-entry keyed list: application overlays may add or remove individual servers without
		 * restating the full platform list. See {@link NameRef}.
		 * </p>
		 */
		@Name(MCP_SERVERS)
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		@EntryTag("mcp-server")
		List<McpServerRef> getMcpServers();

		/**
		 * Skill names available to this agent.
		 *
		 * <p>
		 * Per-entry keyed list: application overlays may add or remove individual skills without
		 * restating the full platform list. See {@link NameRef}.
		 * </p>
		 */
		@Name(SKILLS)
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		@EntryTag("skill")
		List<SkillRef> getSkills();

		/**
		 * Agent names this agent may delegate to.
		 *
		 * <p>
		 * Per-entry keyed list: application overlays may add or remove individual delegates
		 * without restating the full platform list. See {@link NameRef}.
		 * </p>
		 */
		@Name(DELEGATIONS)
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		@EntryTag("delegation")
		List<DelegationRef> getDelegations();

		/**
		 * Tools to suppress from the effective set, even if a lower config layer (the platform
		 * base) declares them.
		 *
		 * <p>
		 * A merge overlay cannot persist a {@code config:operation="remove"} marker on
		 * {@link #getTools()}: such markers are consumed when the overlay file is read back, so
		 * they would not survive the next read-modify-write cycle. Removals are therefore modeled
		 * as plain additive entries here and subtracted from {@link #getTools()} when the running
		 * definitions are computed.
		 * </p>
		 */
		@Name(REMOVED_TOOLS)
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		@EntryTag("removed-tool")
		List<ToolRef> getRemovedTools();

		/**
		 * Skills to suppress from the effective set. See {@link #getRemovedTools()}.
		 */
		@Name(REMOVED_SKILLS)
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		@EntryTag("removed-skill")
		List<SkillRef> getRemovedSkills();

		/**
		 * Delegations to suppress from the effective set. See {@link #getRemovedTools()}.
		 */
		@Name(REMOVED_DELEGATIONS)
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		@EntryTag("removed-delegation")
		List<DelegationRef> getRemovedDelegations();

	}

	/**
	 * One fact an agent states about the situation a run of it happens in.
	 *
	 * @see AgentConfig#getFacts()
	 */
	@TagName("fact")
	public interface FactConfig extends NamedConfigMandatory {

		/** Configuration property name for {@link #getValue()}. */
		String VALUE = "value";

		/** Configuration property name for {@link #getStable()}. */
		String STABLE = "stable";

		@Override
		@Label("Name")
		String getName();

		/**
		 * TL-Script of no arguments computing the value, evaluated in the acting user's context when
		 * a run starts.
		 */
		@Name(VALUE)
		@Label("Value")
		@Mandatory
		@NonNullable
		Expr getValue();

		/**
		 * Whether the value holds for the whole conversation.
		 *
		 * <p>
		 * Only a stable fact is rendered into the environment block: that block sits in front of every
		 * request of the conversation and must stay byte-identical, or the provider's prompt cache
		 * misses on every turn. A fact that changes while the conversation runs is rendered into the
		 * {@link com.top_logic.tl.ai.core.agent.environment.TurnContext per-turn block} instead, which
		 * travels in the turn itself.
		 * </p>
		 */
		@Name(STABLE)
		@Label("Stable")
		@BooleanDefault(true)
		boolean getStable();
	}

	/**
	 * Configuration for a single skill definition.
	 */
	@DisplayOrder({
		SkillConfig.DESCRIPTION,
		SkillConfig.CONTENT,
		SkillConfig.TOOLS,
	})
	public interface SkillConfig extends NamedConfigMandatory {

		/** Configuration property name for {@link #getDescription()}. */
		String DESCRIPTION = "description";

		/** Configuration property name for {@link #getContent()}. */
		String CONTENT = "content";

		/** Configuration property name for {@link #getTools()}. */
		String TOOLS = "tools";

		/** Configuration property name for {@link #getRemovedTools()}. */
		String REMOVED_TOOLS = "removed-tools";

		/**
		 * Human-readable description shown in the skill catalog.
		 */
		@Name(DESCRIPTION)
		@Nullable
		@LabelPositioning(LabelPosition.ABOVE)
		@MultiLine
		String getDescription();

		/**
		 * The actual prompt text for this skill.
		 */
		@Name(CONTENT)
		@Nullable
		@LabelPositioning(LabelPosition.ABOVE)
		@MultiLine
		String getContent();

		/**
		 * Qualified tool names delivered when this skill is loaded.
		 *
		 * <p>
		 * Per-entry keyed list: application overlays may add or remove individual tools without
		 * restating the full platform list. See {@link NameRef}.
		 * </p>
		 *
		 * @see AgentConfig#getTools()
		 */
		@Name(TOOLS)
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		@EntryTag("tool")
		List<ToolRef> getTools();

		/**
		 * Tools to suppress from this skill's effective set. See
		 * {@link AgentConfig#getRemovedTools()}.
		 */
		@Name(REMOVED_TOOLS)
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		@EntryTag("removed-tool")
		List<ToolRef> getRemovedTools();
	}

	/**
	 * Configuration interface for {@link TopLogicAgentDefinitions}.
	 */
	public interface Config<I extends TopLogicAgentDefinitions> extends ConfiguredManagedClass.Config<I> {

		/** Configuration property name for {@link #getAgents()}. */
		String AGENTS = "agents";

		/** Configuration property name for {@link #getSkills()}. */
		String SKILLS = "skills";

		/**
		 * Map of config-defined agent definitions, keyed by agent name.
		 */
		@Name(AGENTS)
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		@EntryTag("agent")
		Map<String, AgentConfig> getAgents();

		/**
		 * Map of config-defined skill definitions, keyed by skill name.
		 */
		@Name(SKILLS)
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		@EntryTag("skill")
		Map<String, SkillConfig> getSkills();
	}

	private Map<String, AgentDefinition> _agents;

	private Map<String, SkillDefinition> _skills;

	/**
	 * Creates a new {@link TopLogicAgentDefinitions} from configuration.
	 */
	@CalledByReflection
	public TopLogicAgentDefinitions(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();

		Map<String, AgentDefinition> agents = new LinkedHashMap<>();
		for (Map.Entry<String, AgentConfig> entry : getConfig().getAgents().entrySet()) {
			String name = entry.getKey();
			AgentConfig cfg = entry.getValue();
			checkPlaceholders(name, cfg.getSystemPrompt());
			agents.put(name, new AgentDefinition(
				name,
				cfg.getDisplayName(),
				cfg.getDescription(),
				cfg.getSummary(),
				cfg.getSystemPrompt(),
				effectiveNames(cfg.getTools(), cfg.getRemovedTools()),
				toNames(cfg.getMcpServers()),
				effectiveNames(cfg.getSkills(), cfg.getRemovedSkills()),
				effectiveNames(cfg.getDelegations(), cfg.getRemovedDelegations()),
				AgentSource.CONFIG,
				toFacts(cfg.getFacts()),
				cfg.isInfrastructure()));
		}
		_agents = Collections.unmodifiableMap(agents);

		Map<String, SkillDefinition> skills = new LinkedHashMap<>();
		for (Map.Entry<String, SkillConfig> entry : getConfig().getSkills().entrySet()) {
			String name = entry.getKey();
			SkillConfig cfg = entry.getValue();
			skills.put(name, new SkillDefinition(
				name,
				cfg.getDescription(),
				cfg.getContent(),
				effectiveNames(cfg.getTools(), cfg.getRemovedTools()),
				AgentSource.CONFIG));
		}
		_skills = Collections.unmodifiableMap(skills);

		Logger.info("TopLogicAgentDefinitions started with " + _agents.size()
			+ " agent(s) and " + _skills.size() + " skill(s).",
			TopLogicAgentDefinitions.class);
	}

	@Override
	protected void shutDown() {
		try {
			_agents = null;
			_skills = null;
		} finally {
			super.shutDown();
		}
	}

	/**
	 * Refuses an agent whose system prompt names a placeholder the harness cannot fill.
	 *
	 * <p>
	 * A placeholder nobody fills would reach the model verbatim and read as an instruction the
	 * harness failed to honour, and the typo behind it is invisible in the answer. Failing here -
	 * while the definitions are read - names the agent and the placeholder, which is what makes it a
	 * two-second fix rather than a puzzle.
	 * </p>
	 */
	static void checkPlaceholders(String agentName, String systemPrompt) {
		List<String> unknown = PromptPlaceholders.unknownIn(systemPrompt);
		if (unknown.isEmpty()) {
			return;
		}
		String names = String.join(", ", unknown.stream().map(n -> "{{" + n + "}}").toList());
		throw new ConfigurationError(ResKey.text("Agent '" + agentName + "' uses unknown placeholder(s)"
			+ " in its system prompt: " + names + ". Known are: " + PromptPlaceholders.knownNames()));
	}

	private static List<AgentFact> toFacts(List<FactConfig> configs) {
		if (configs == null || configs.isEmpty()) {
			return List.of();
		}
		List<AgentFact> facts = new ArrayList<>(configs.size());
		for (FactConfig config : configs) {
			facts.add(new AgentFact(config.getName(), config.getStable(), config.getValue()));
		}
		return List.copyOf(facts);
	}

	private static List<String> toNames(List<? extends NameRef> refs) {
		if (refs == null || refs.isEmpty()) {
			return List.of();
		}
		return refs.stream().map(NamedConfigMandatory::getName).toList();
	}

	/**
	 * The names present in {@code present} minus the names listed in {@code removed}, preserving
	 * the order of {@code present}.
	 *
	 * <p>
	 * Removals are persisted as additive entries (see {@link AgentConfig#getRemovedTools()}) and
	 * applied here, since the configuration overlay cannot carry a {@code config:operation=remove}
	 * marker that survives a read-modify-write cycle.
	 * </p>
	 */
	public static List<String> effectiveNames(List<? extends NameRef> present,
			List<? extends NameRef> removed) {
		List<String> result = new ArrayList<>(toNames(present));
		result.removeAll(toNames(removed));
		return List.copyOf(result);
	}

	/**
	 * Returns the config-defined agent with the given name, or {@code null} if not found.
	 */
	public AgentDefinition getConfigAgent(String name) {
		return _agents.get(name);
	}

	/**
	 * Returns all config-defined agent definitions.
	 */
	public Map<String, AgentDefinition> getAllConfigAgents() {
		return _agents;
	}

	/**
	 * Returns the config-defined skill with the given name, or {@code null} if not found.
	 */
	public SkillDefinition getConfigSkill(String name) {
		return _skills.get(name);
	}

	/**
	 * Returns all config-defined skill definitions.
	 */
	public Map<String, SkillDefinition> getAllConfigSkills() {
		return _skills;
	}

	/**
	 * Whether a config-defined agent with the given name exists.
	 */
	public boolean hasConfigAgent(String name) {
		return _agents.containsKey(name);
	}

	/**
	 * Whether a config-defined skill with the given name exists.
	 */
	public boolean hasConfigSkill(String name) {
		return _skills.containsKey(name);
	}

	/**
	 * Returns the singleton {@link TopLogicAgentDefinitions} instance.
	 */
	public static TopLogicAgentDefinitions getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module for {@link TopLogicAgentDefinitions}.
	 */
	public static final class Module extends TypedRuntimeModule<TopLogicAgentDefinitions> {

		/**
		 * Singleton {@link TopLogicAgentDefinitions.Module} instance.
		 */
		public static final TopLogicAgentDefinitions.Module INSTANCE = new TopLogicAgentDefinitions.Module();

		private Module() {
			// Singleton constructor
		}

		@Override
		public Class<TopLogicAgentDefinitions> getImplementation() {
			return TopLogicAgentDefinitions.class;
		}
	}
}
