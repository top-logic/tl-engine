/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.authoring;

import java.io.File;
import java.io.IOError;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.RestartException;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.TypedRuntimeModule.ModuleConfiguration;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.layout.admin.component.OverrideConfigurationWriter;
import com.top_logic.layout.admin.component.TLServiceUtils;
import com.top_logic.tl.ai.core.agent.DelegationRef;
import com.top_logic.tl.ai.core.agent.SkillRef;
import com.top_logic.tl.ai.core.agent.ToolRef;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions.AgentConfig;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions.SkillConfig;

/**
 * Persists a modified {@link TopLogicAgentDefinitions} configuration as an autoconf overlay file,
 * then reloads {@link ApplicationConfig} and restarts the service so that the changed definitions
 * become immediately active.
 *
 * <p>
 * Follows the same full-replacement pattern as {@link ScriptToolConfigPersistence}: the complete
 * live configuration is copied, modified in memory, and written as a full overlay. This ensures
 * the reload picks up the change immediately.
 * </p>
 */
public class FrameworkWiringPersistence {

	/**
	 * Returns a deep copy of the current live {@link TopLogicAgentDefinitions} configuration.
	 */
	@SuppressWarnings("unchecked")
	public static TopLogicAgentDefinitions.Config<?> copyOfLiveConfig() {
		try {
			return TypedConfiguration.copy(
				(TopLogicAgentDefinitions.Config<?>) ApplicationConfig.getInstance()
					.getServiceConfiguration(TopLogicAgentDefinitions.class));
		} catch (ConfigurationException ex) {
			throw new RuntimeException(
				"Failed to read TopLogicAgentDefinitions configuration: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Ensures an {@link AgentConfig} entry exists in the overlay for the given agent name.
	 */
	public static AgentConfig ensureAgent(TopLogicAgentDefinitions.Config<?> overlay, String agentName) {
		AgentConfig agent = overlay.getAgents().get(agentName);
		if (agent == null) {
			agent = TypedConfiguration.newConfigItem(AgentConfig.class);
			agent.setName(agentName);
			overlay.getAgents().put(agentName, agent);
		}
		return agent;
	}

	/**
	 * Ensures a {@link SkillConfig} entry exists in the overlay for the given skill name.
	 */
	public static SkillConfig ensureSkill(TopLogicAgentDefinitions.Config<?> overlay, String skillName) {
		SkillConfig skill = overlay.getSkills().get(skillName);
		if (skill == null) {
			skill = TypedConfiguration.newConfigItem(SkillConfig.class);
			skill.setName(skillName);
			overlay.getSkills().put(skillName, skill);
		}
		return skill;
	}

	/**
	 * Adds a tool reference to a framework agent's tool list in the overlay.
	 */
	public static void addToolToAgent(TopLogicAgentDefinitions.Config<?> overlay,
			String agentName, String toolName) {
		AgentConfig agent = ensureAgent(overlay, agentName);
		addNameRef(agent.getTools(), toolName, ToolRef.class);
	}

	/**
	 * Removes a tool reference from a framework agent's tool list in the overlay.
	 */
	public static void removeToolFromAgent(TopLogicAgentDefinitions.Config<?> overlay,
			String agentName, String toolName) {
		AgentConfig agent = ensureAgent(overlay, agentName);
		removeByName(agent.getTools(), toolName);
	}

	/**
	 * Adds a skill reference to a framework agent's skill list in the overlay.
	 */
	public static void addSkillToAgent(TopLogicAgentDefinitions.Config<?> overlay,
			String agentName, String skillName) {
		AgentConfig agent = ensureAgent(overlay, agentName);
		addNameRef(agent.getSkills(), skillName, SkillRef.class);
	}

	/**
	 * Removes a skill reference from a framework agent's skill list in the overlay.
	 */
	public static void removeSkillFromAgent(TopLogicAgentDefinitions.Config<?> overlay,
			String agentName, String skillName) {
		AgentConfig agent = ensureAgent(overlay, agentName);
		removeByName(agent.getSkills(), skillName);
	}

	/**
	 * Adds a delegation reference to a framework agent's delegation list in the overlay.
	 */
	public static void addDelegationToAgent(TopLogicAgentDefinitions.Config<?> overlay,
			String agentName, String delegationName) {
		AgentConfig agent = ensureAgent(overlay, agentName);
		addNameRef(agent.getDelegations(), delegationName, DelegationRef.class);
	}

	/**
	 * Removes a delegation reference from a framework agent's delegation list in the overlay.
	 */
	public static void removeDelegationFromAgent(TopLogicAgentDefinitions.Config<?> overlay,
			String agentName, String delegationName) {
		AgentConfig agent = ensureAgent(overlay, agentName);
		removeByName(agent.getDelegations(), delegationName);
	}

	/**
	 * Adds a tool reference to a framework skill's tool list in the overlay.
	 */
	public static void addToolToSkill(TopLogicAgentDefinitions.Config<?> overlay,
			String skillName, String toolName) {
		SkillConfig skill = ensureSkill(overlay, skillName);
		addNameRef(skill.getTools(), toolName, ToolRef.class);
	}

	/**
	 * Removes a tool reference from a framework skill's tool list in the overlay.
	 */
	public static void removeToolFromSkill(TopLogicAgentDefinitions.Config<?> overlay,
			String skillName, String toolName) {
		SkillConfig skill = ensureSkill(overlay, skillName);
		removeByName(skill.getTools(), toolName);
	}

	/**
	 * Writes the given configuration to the autoconf overlay file and restarts the service.
	 *
	 * @param config
	 *        The complete {@link TopLogicAgentDefinitions} configuration; it fully replaces the
	 *        existing overlay entry for this service.
	 */
	public static void persist(TopLogicAgentDefinitions.Config<?> config) {
		try {
			persistTo(overlayFile(), config);
			TLServiceUtils.reloadConfigurations();
			restartService();
		} catch (Exception ex) {
			throw new RuntimeException(
				"Failed to persist framework wiring configuration: " + ex.getMessage(), ex);
		} catch (IOError err) {
			throw new RuntimeException(
				"Failed to reload configuration after framework wiring: " + err.getMessage(), err);
		}
	}

	/**
	 * Serializes the given configuration as a pretty-printed XML overlay file.
	 *
	 * <p>
	 * This method handles only the file write; the caller is responsible for reloading the
	 * configuration afterwards. Prefer {@link #persist(TopLogicAgentDefinitions.Config)} for
	 * normal use; call this method directly only when reload must be suppressed (e.g. in tests).
	 * </p>
	 */
	public static void persistTo(File file, TopLogicAgentDefinitions.Config<?> config)
			throws Exception {
		FileUtilities.enforceDirectory(file.getParentFile());

		ApplicationConfig.Config app = TypedConfiguration.newConfigItem(ApplicationConfig.Config.class);
		ModuleConfiguration module = TypedConfiguration.newConfigItem(ModuleConfiguration.class);
		module.setServiceClass(TopLogicAgentDefinitions.class);
		module.setInstance((ServiceConfiguration<?>) config);
		app.getServices().put(TopLogicAgentDefinitions.class, module);

		String xml;
		try (StringWriter out = new StringWriter()) {
			try (ConfigurationWriter writer = new OverrideConfigurationWriter(out,
					Collections.singleton(TypedRuntimeModule.ModuleConfiguration.class))) {
				TypedConfiguration.serialize("application", app, writer);
			}
			xml = out.toString();
		}

		xml = XMLPrettyPrinter.prettyPrint(xml);
		FileUtilities.writeStringToFile(xml, file, StringServices.UTF8);
	}

	private static void restartService() {
		try {
			ModuleUtil.INSTANCE.restart(TopLogicAgentDefinitions.Module.INSTANCE, null);
		} catch (RestartException ex) {
			Logger.error("Failed to restart TopLogicAgentDefinitions: " + ex.getMessage(), ex,
				FrameworkWiringPersistence.class);
		}
	}

	private static <T extends NamedConfigMandatory> void addNameRef(
			List<T> list, String name, Class<T> type) {
		for (T existing : list) {
			if (name.equals(existing.getName())) {
				return; // already present — idempotent
			}
		}
		T ref = TypedConfiguration.newConfigItem(type);
		ref.setName(name);
		list.add(ref);
	}

	private static void removeByName(List<? extends NamedConfigMandatory> list, String name) {
		list.removeIf(ref -> name.equals(ref.getName()));
	}

	private static File overlayFile() {
		return new File(XMLProperties.Setting.resolveAutoconfFolder(),
			TopLogicAgentDefinitions.class.getName() + ".config.xml");
	}
}
