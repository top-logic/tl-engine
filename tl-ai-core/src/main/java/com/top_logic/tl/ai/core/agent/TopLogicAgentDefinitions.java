/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.order.DisplayOrder;

import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.LabelPositioning;
import com.top_logic.model.annotate.LabelPosition;
import com.top_logic.tl.ai.core.security.AiSecurityMode;

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
 * them unset unless a true replacement is intended.
 * </p>
 *
 * @see AgentDefinition
 * @see SkillDefinition
 * @see AgentSource#CONFIG
 * @see NameRef
 */
@Label("TopLogic-Agents and Skills")
@ServiceDependencies({})
public class TopLogicAgentDefinitions extends ConfiguredManagedClass<TopLogicAgentDefinitions.Config<?>> {

	/**
	 * Configuration for a single agent definition.
	 *
	 * <p>
	 * Contains only identity and capabilities. Tuning parameters belong in
	 * {@link com.top_logic.tl.ai.core.provider.LLMService.AgentOverride AgentOverride}.
	 * </p>
	 */
	@DisplayOrder({
		AgentConfig.DESCRIPTION,
		AgentConfig.TOOLS,
		AgentConfig.SKILLS,
		AgentConfig.DELEGATIONS,
		AgentConfig.MCP_SERVERS,
		AgentConfig.SECURITY_MODE,
		AgentConfig.SYSTEM_PROMPT,
	})
	public interface AgentConfig extends NamedConfigMandatory {

		/** Configuration property name for {@link #getDescription()}. */
		String DESCRIPTION = "description";

		/**
		 * Configuration property name for {@link #getSecurityMode()}.
		 *
		 * @see #getSecurityMode()
		 */
		String SECURITY_MODE = "security-mode";

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
		@ControlProvider(MultiLineText.class)
		String getDescription();

		/**
		 * How strictly security is enforced when this agent runs.
		 *
		 * <p>
		 * Only ever stricter than the application's own setting, never laxer: choosing a mode the
		 * application does not use makes this one agent checked more thoroughly than the rest, while
		 * choosing a laxer one changes nothing. Leave it empty unless this agent needs more than the
		 * application asks for.
		 * </p>
		 */
		/*
		 * Nullable with a null default, both needed and for the same reason as on
		 * ToolAccessPolicy.getSecurityMode(): an enum property otherwise defaults to its first
		 * constant, and "the author of this agent said nothing" has to stay distinguishable from an
		 * explicit LEGACY. Here the distinction is between "no floor at all" and "a floor of LEGACY",
		 * which is a floor that happens to raise nothing.
		 *
		 * A laxer value being silently inert is the one uncomfortable corner of reading this as a
		 * floor, and it is the price of removing the escalation direction: an agent definition is
		 * editable by anyone who may author agents, so it must not be a way around the deployment's
		 * decision. Said out loud in the tooltip above rather than left for an administrator to
		 * discover.
		 */
		@Name(SECURITY_MODE)
		@Nullable
		@NullDefault
		AiSecurityMode getSecurityMode();

		/**
		 * The agent's system prompt text.
		 */
		@Name(SYSTEM_PROMPT)
		@Nullable
		@LabelPositioning(LabelPosition.ABOVE)
		@ControlProvider(MultiLineText.class)
		String getSystemPrompt();

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
		@ControlProvider(MultiLineText.class)
		String getDescription();

		/**
		 * The actual prompt text for this skill.
		 */
		@Name(CONTENT)
		@Nullable
		@LabelPositioning(LabelPosition.ABOVE)
		@ControlProvider(MultiLineText.class)
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
			agents.put(name, new AgentDefinition(
				name,
				cfg.getDescription(),
				cfg.getSystemPrompt(),
				effectiveNames(cfg.getTools(), cfg.getRemovedTools()),
				toNames(cfg.getMcpServers()),
				effectiveNames(cfg.getSkills(), cfg.getRemovedSkills()),
				effectiveNames(cfg.getDelegations(), cfg.getRemovedDelegations()),
				cfg.getSecurityMode(),
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
