/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * Service holding config-defined (TopLogic-provided) agent and skill definitions.
 *
 * <p>
 * Agent and skill definitions declared in service configuration XML are read-only and
 * upgradeable. Application-specific definitions live in the knowledge base instead.
 * </p>
 *
 * @see AgentDefinition
 * @see SkillDefinition
 * @see AgentSource#CONFIG
 */
@ServiceDependencies({})
public class AgentDefinitionService extends ConfiguredManagedClass<AgentDefinitionService.Config<?>> {

	/**
	 * Configuration for a single agent definition.
	 *
	 * <p>
	 * Contains only identity and capabilities. Tuning parameters belong in
	 * {@link com.top_logic.tl.ai.core.provider.LLMService.AgentModelTuning
	 * AgentModelTuning}.
	 * </p>
	 */
	public interface AgentConfig extends NamedConfigMandatory {

		/** Configuration property name for {@link #getDescription()}. */
		String DESCRIPTION = "description";

		/** Configuration property name for {@link #getSystemPrompt()}. */
		String SYSTEM_PROMPT = "system-prompt";

		/** Configuration property name for {@link #getTools()}. */
		String TOOLS = "tools";

		/** Configuration property name for {@link #getMcpServers()}. */
		String MCP_SERVERS = "mcp-servers";

		/** Configuration property name for {@link #getSkills()}. */
		String SKILLS = "skills";

		/** Configuration property name for {@link #getDelegations()}. */
		String DELEGATIONS = "delegations";

		/**
		 * Human-readable description of the agent.
		 */
		@Name(DESCRIPTION)
		@Nullable
		String getDescription();

		/**
		 * The agent's system prompt text.
		 */
		@Name(SYSTEM_PROMPT)
		@Nullable
		String getSystemPrompt();

		/**
		 * Qualified tool names available to this agent.
		 */
		@Name(TOOLS)
		@Format(CommaSeparatedStrings.class)
		List<String> getTools();

		/**
		 * MCP server names available to this agent.
		 */
		@Name(MCP_SERVERS)
		@Format(CommaSeparatedStrings.class)
		List<String> getMcpServers();

		/**
		 * Skill names available to this agent.
		 */
		@Name(SKILLS)
		@Format(CommaSeparatedStrings.class)
		List<String> getSkills();

		/**
		 * Agent names this agent may delegate to.
		 */
		@Name(DELEGATIONS)
		@Format(CommaSeparatedStrings.class)
		List<String> getDelegations();
	}

	/**
	 * Configuration for a single skill definition.
	 */
	public interface SkillConfig extends NamedConfigMandatory {

		/** Configuration property name for {@link #getDescription()}. */
		String DESCRIPTION = "description";

		/** Configuration property name for {@link #getContent()}. */
		String CONTENT = "content";

		/**
		 * Human-readable description shown in the skill catalog.
		 */
		@Name(DESCRIPTION)
		@Nullable
		String getDescription();

		/**
		 * The actual prompt text for this skill.
		 */
		@Name(CONTENT)
		@Nullable
		String getContent();
	}

	/**
	 * Configuration interface for {@link AgentDefinitionService}.
	 */
	public interface Config<I extends AgentDefinitionService> extends ConfiguredManagedClass.Config<I> {

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

	private static volatile AgentDefinitionService _instance;

	private Map<String, AgentDefinition> _agents;

	private Map<String, SkillDefinition> _skills;

	/**
	 * Creates a new {@link AgentDefinitionService} from configuration.
	 */
	@CalledByReflection
	public AgentDefinitionService(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();
		_instance = this;

		Map<String, AgentDefinition> agents = new LinkedHashMap<>();
		for (Map.Entry<String, AgentConfig> entry : getConfig().getAgents().entrySet()) {
			String name = entry.getKey();
			AgentConfig cfg = entry.getValue();
			agents.put(name, new AgentDefinition(
				name,
				cfg.getDescription(),
				cfg.getSystemPrompt(),
				cfg.getTools(),
				cfg.getMcpServers(),
				cfg.getSkills(),
				cfg.getDelegations(),
				AgentSource.CONFIG));
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
				AgentSource.CONFIG));
		}
		_skills = Collections.unmodifiableMap(skills);

		Logger.info("AgentDefinitionService started with " + _agents.size()
			+ " agent(s) and " + _skills.size() + " skill(s).",
			AgentDefinitionService.class);
	}

	@Override
	protected void shutDown() {
		try {
			_instance = null;
			_agents = null;
			_skills = null;
		} finally {
			super.shutDown();
		}
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
	 * Returns the singleton {@link AgentDefinitionService} instance.
	 */
	public static AgentDefinitionService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module for {@link AgentDefinitionService}.
	 */
	public static final class Module extends TypedRuntimeModule<AgentDefinitionService> {

		/**
		 * Singleton {@link AgentDefinitionService.Module} instance.
		 */
		public static final AgentDefinitionService.Module INSTANCE = new AgentDefinitionService.Module();

		private Module() {
			// Singleton constructor
		}

		@Override
		public Class<AgentDefinitionService> getImplementation() {
			return AgentDefinitionService.class;
		}
	}
}
