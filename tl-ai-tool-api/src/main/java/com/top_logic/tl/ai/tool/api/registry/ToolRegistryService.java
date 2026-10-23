/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api.registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.tl.ai.tool.api.AICategory;
import com.top_logic.tl.ai.tool.api.Tool;

/**
 * Registry service for managing tools available in the application.
 *
 * <p>
 * Tools are organized by factory. Each factory produces a set of tools, and each tool is
 * identified by a qualified name in the format {@code {factory-name}.{tool-name}}. This allows
 * tools from different factories to have the same simple name without conflicts.
 * </p>
 */
@ServiceCategory.Of(AICategory.class)
public class ToolRegistryService extends ConfiguredManagedClass<ToolRegistryService.Config<?>> {

	/**
	 * Configuration for the {@link ToolRegistryService} service.
	 */
	public interface Config<I extends ToolRegistryService> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Configuration property name for tool factories.
		 *
		 * @see #getToolFactories()
		 */
		String TOOL_FACTORIES = "tool-factories";

		/**
		 * Map of named tool factories available in the application.
		 *
		 * <p>
		 * Each tool factory is identified by a unique name. The factory name becomes part of the
		 * qualified tool name ({@code factory-name.tool-name}) used to reference tools in
		 * TL-Script functions.
		 * </p>
		 */
		@Name(TOOL_FACTORIES)
		@Key(ToolFactoryConfig.NAME_ATTRIBUTE)
		@EntryTag("factory")
		Map<String, ToolFactoryConfig> getToolFactories();

	}

	/**
	 * Configuration for a named tool factory.
	 */
	public interface ToolFactoryConfig extends NamedConfigMandatory {

		/**
		 * The tool factory implementation.
		 */
		PolymorphicConfiguration<? extends ToolFactory> getImplementation();

		/**
		 * Setter for {@link #getImplementation()}.
		 */
		void setImplementation(PolymorphicConfiguration<? extends ToolFactory> value);

	}

	private final InstantiationContext _context;

	private Map<String, Tool> _tools;

	private Map<String, String> _configNames;

	/**
	 * Creates a new {@link ToolRegistryService} from the given configuration.
	 */
	public ToolRegistryService(InstantiationContext context, Config<?> config) {
		super(context, config);
		_context = context;
	}

	@Override
	protected void startUp() {
		super.startUp();

		_tools = new HashMap<>();
		_configNames = new HashMap<>();
		for (Map.Entry<String, ToolFactoryConfig> entry : getConfig().getToolFactories().entrySet()) {
			String factoryName = entry.getKey();
			try {
				ToolFactory factory = _context.getInstance(entry.getValue().getImplementation());
				if (factory == null) {
					Logger.warn("Failed to instantiate tool factory '" + factoryName
						+ "': Factory instance is null.", ToolRegistryService.class);
					continue;
				}

				for (Tool tool : factory.createTools()) {
					String configName = factoryName + "." + tool.name();
					String qualifiedName = toProviderName(configName);
					_tools.put(qualifiedName, tool);
					_configNames.put(qualifiedName, configName);
				}
			} catch (Exception ex) {
				Logger.error("Failed to initialize tool factory '" + factoryName + "': " + ex.getMessage(), ex,
					ToolRegistryService.class);
			}
		}
	}

	/**
	 * The dot-notation name a tool was configured under, for a provider-safe registry name.
	 *
	 * <p>
	 * {@link #toProviderName(String)} is not invertible - it replaces every character outside
	 * {@code [a-zA-Z0-9_-]}, so {@code factory.tool} and {@code factory_tool} can collapse onto the
	 * same key. The mapping is therefore recorded when the registry is built rather than reconstructed
	 * afterwards. It matters because {@code factory.tool} is the name an administrator writes in
	 * configuration and a user types in TL-Script, so it is the name to match against and the name to
	 * show in a message.
	 * </p>
	 *
	 * @param providerName
	 *        A name as returned by {@link #getToolNames()}.
	 * @return The configured {@code {factory}.{tool}} name, or <code>null</code> if the registry has
	 *         no such tool.
	 */
	public String getConfigName(String providerName) {
		return _configNames.get(providerName);
	}

	/**
	 * Retrieves a tool by its qualified name.
	 *
	 * @param qualifiedName
	 *        The qualified name of the tool in the format {@code factory-name.tool-name}.
	 * @return The tool, or <code>null</code> if no tool with the given name exists.
	 */
	public Tool getTool(String qualifiedName) {
		Tool tool = _tools.get(qualifiedName);
		if (tool == null && qualifiedName.contains(".")) {
			tool = _tools.get(toProviderName(qualifiedName));
		}
		return tool;
	}

	/**
	 * Returns all registered tool names.
	 *
	 * <p>
	 * Tool names are in the qualified format {@code factory-name.tool-name}.
	 * </p>
	 */
	public Set<String> getToolNames() {
		return _tools.keySet();
	}

	/**
	 * Returns all registered tools.
	 */
	public Collection<Tool> getTools() {
		return _tools.values();
	}

	/**
	 * Returns the singleton {@link ToolRegistryService} instance.
	 */
	public static ToolRegistryService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Converts a config-notation qualified name ({@code factory.tool}) to the
	 * provider-safe format ({@code factory__tool}).
	 *
	 * @param configName
	 *        Qualified name in dot notation.
	 * @return The provider-safe name using double-underscore separator.
	 */
	public static String toProviderName(String configName) {
		int dot = configName.indexOf('.');
		if (dot < 0) {
			return sanitize(configName);
		}
		return sanitize(configName.substring(0, dot))
			+ "__" + sanitize(configName.substring(dot + 1));
	}

	/**
	 * Replaces characters outside {@code [a-zA-Z0-9_-]} with underscores.
	 */
	static String sanitize(String name) {
		return name.replaceAll("[^a-zA-Z0-9_-]", "_");
	}

	/**
	 * Module for the {@link ToolRegistryService} service.
	 */
	public static final class Module extends TypedRuntimeModule<ToolRegistryService> {

		/**
		 * Singleton instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<ToolRegistryService> getImplementation() {
			return ToolRegistryService.class;
		}

	}

}
