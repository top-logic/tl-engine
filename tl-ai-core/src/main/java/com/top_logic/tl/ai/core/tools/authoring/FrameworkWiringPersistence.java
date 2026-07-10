/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.authoring;

import java.io.File;
import java.io.IOError;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.RestartException;
import com.top_logic.basic.module.TypedRuntimeModule.ModuleConfiguration;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.layout.admin.component.TLServiceUtils;
import com.top_logic.tl.ai.core.tools.interaction.ToolRegistryReloadFlag;
import com.top_logic.tl.ai.core.agent.DelegationRef;
import com.top_logic.tl.ai.core.agent.SkillRef;
import com.top_logic.tl.ai.core.agent.ToolRef;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions.AgentConfig;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions.SkillConfig;

/**
 * Persists an additive overlay for {@link TopLogicAgentDefinitions}, then reloads
 * {@link ApplicationConfig} and restarts the service so changes take effect immediately.
 *
 * <p>
 * The overlay contains only the delta (added/removed entries). The config system's keyed-list
 * merge applies it on top of the base configuration — existing framework definitions flow
 * through unchanged. A seed overlay file must exist at boot so the path is registered in the
 * webapp resource resolver (see {@code tl-ai-demo} autoconf).
 * </p>
 */
public class FrameworkWiringPersistence {

	/**
	 * Creates a fresh empty overlay config for accumulating delta entries.
	 */
	public static TopLogicAgentDefinitions.Config<?> newOverlay() {
		return TypedConfiguration.newConfigItem(TopLogicAgentDefinitions.Config.class);
	}

	/**
	 * Loads the currently persisted overlay (the delta from {@link #overlayFile()}), or a fresh
	 * empty overlay if none has been written yet.
	 *
	 * <p>
	 * Wiring operations read this, apply their mutation, and write it back, so deltas from earlier
	 * operations accumulate rather than being overwritten.
	 * </p>
	 */
	public static TopLogicAgentDefinitions.Config<?> loadOverlay() {
		return load(overlayFile());
	}

	/**
	 * Loads the persisted overlay from the given file, or a fresh empty overlay if the file does
	 * not exist.
	 */
	public static TopLogicAgentDefinitions.Config<?> load(File file) {
		if (!file.exists()) {
			return newOverlay();
		}
		try {
			ConfigurationDescriptor descriptor =
				TypedConfiguration.getConfigurationDescriptor(ApplicationConfig.Config.class);
			Map<String, ConfigurationDescriptor> descriptors =
				Collections.singletonMap("application", descriptor);

			ConfigurationReader reader = new ConfigurationReader(
				SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, descriptors);
			reader.setSource(BinaryDataFactory.createBinaryData(file));
			ApplicationConfig.Config app = (ApplicationConfig.Config) reader.read();

			ModuleConfiguration module = app.getServices().get(TopLogicAgentDefinitions.class);
			if (module == null || module.getInstance() == null) {
				return newOverlay();
			}
			return (TopLogicAgentDefinitions.Config<?>) module.getInstance();
		} catch (ConfigurationException ex) {
			throw new RuntimeException(
				"Failed to read framework wiring overlay '" + file + "': " + ex.getMessage(), ex);
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
		removeByName(agent.getRemovedTools(), toolName);
	}

	/**
	 * Removes a tool reference from a framework agent's tool list in the overlay.
	 */
	public static void removeToolFromAgent(TopLogicAgentDefinitions.Config<?> overlay,
			String agentName, String toolName) {
		AgentConfig agent = ensureAgent(overlay, agentName);
		removeByName(agent.getTools(), toolName);
		addNameRef(agent.getRemovedTools(), toolName, ToolRef.class);
	}

	/**
	 * Adds a skill reference to a framework agent's skill list in the overlay.
	 */
	public static void addSkillToAgent(TopLogicAgentDefinitions.Config<?> overlay,
			String agentName, String skillName) {
		AgentConfig agent = ensureAgent(overlay, agentName);
		addNameRef(agent.getSkills(), skillName, SkillRef.class);
		removeByName(agent.getRemovedSkills(), skillName);
	}

	/**
	 * Removes a skill reference from a framework agent's skill list in the overlay.
	 */
	public static void removeSkillFromAgent(TopLogicAgentDefinitions.Config<?> overlay,
			String agentName, String skillName) {
		AgentConfig agent = ensureAgent(overlay, agentName);
		removeByName(agent.getSkills(), skillName);
		addNameRef(agent.getRemovedSkills(), skillName, SkillRef.class);
	}

	/**
	 * Adds a delegation reference to a framework agent's delegation list in the overlay.
	 */
	public static void addDelegationToAgent(TopLogicAgentDefinitions.Config<?> overlay,
			String agentName, String delegationName) {
		AgentConfig agent = ensureAgent(overlay, agentName);
		addNameRef(agent.getDelegations(), delegationName, DelegationRef.class);
		removeByName(agent.getRemovedDelegations(), delegationName);
	}

	/**
	 * Removes a delegation reference from a framework agent's delegation list in the overlay.
	 */
	public static void removeDelegationFromAgent(TopLogicAgentDefinitions.Config<?> overlay,
			String agentName, String delegationName) {
		AgentConfig agent = ensureAgent(overlay, agentName);
		removeByName(agent.getDelegations(), delegationName);
		addNameRef(agent.getRemovedDelegations(), delegationName, DelegationRef.class);
	}

	/**
	 * Adds a tool reference to a framework skill's tool list in the overlay.
	 */
	public static void addToolToSkill(TopLogicAgentDefinitions.Config<?> overlay,
			String skillName, String toolName) {
		SkillConfig skill = ensureSkill(overlay, skillName);
		addNameRef(skill.getTools(), toolName, ToolRef.class);
		removeByName(skill.getRemovedTools(), toolName);
	}

	/**
	 * Removes a tool reference from a framework skill's tool list in the overlay.
	 */
	public static void removeToolFromSkill(TopLogicAgentDefinitions.Config<?> overlay,
			String skillName, String toolName) {
		SkillConfig skill = ensureSkill(overlay, skillName);
		removeByName(skill.getTools(), toolName);
		addNameRef(skill.getRemovedTools(), toolName, ToolRef.class);
	}

	/**
	 * Writes the overlay to the autoconf file and restarts the service.
	 *
	 * @param overlay
	 *        Additive overlay containing only the delta entries to merge into the base config.
	 */
	public static void persist(TopLogicAgentDefinitions.Config<?> overlay) {
		try {
			persistTo(overlayFile(), overlay);
			TLServiceUtils.reloadConfigurations();
			restartService();
			ToolRegistryReloadFlag.request();
		} catch (Exception ex) {
			throw new RuntimeException(
				"Failed to persist framework wiring configuration: " + ex.getMessage(), ex);
		} catch (IOError err) {
			throw new RuntimeException(
				"Failed to reload configuration after framework wiring: " + err.getMessage(), err);
		}
	}

	/**
	 * Serializes the overlay as a pretty-printed XML file without reloading or restarting.
	 *
	 * <p>
	 * Prefer {@link #persist(TopLogicAgentDefinitions.Config)} for normal use; call this method
	 * directly only when reload must be suppressed (e.g. in tests).
	 * </p>
	 */
	public static void persistTo(File file, TopLogicAgentDefinitions.Config<?> overlay)
			throws Exception {
		FileUtilities.enforceDirectory(file.getParentFile());

		ApplicationConfig.Config app = TypedConfiguration.newConfigItem(ApplicationConfig.Config.class);
		ModuleConfiguration module = TypedConfiguration.newConfigItem(ModuleConfiguration.class);
		module.setServiceClass(TopLogicAgentDefinitions.class);
		module.setInstance((ServiceConfiguration<?>) overlay);
		app.getServices().put(TopLogicAgentDefinitions.class, module);

		String xml;
		try (StringWriter out = new StringWriter()) {
			try (ConfigurationWriter writer = new ConfigurationWriter(out)) {
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
