/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.tl.ai.tool.api.ToolSchema;
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
					String qualifiedName = factoryName + "." + tool.name();
					_tools.put(qualifiedName, tool);
				}
			} catch (Exception ex) {
				Logger.error("Failed to initialize tool factory '" + factoryName + "': " + ex.getMessage(), ex,
					ToolRegistryService.class);
			}
		}
	}

	/**
	 * Retrieves a tool by its qualified name.
	 *
	 * @param qualifiedName
	 *        The qualified name of the tool in the format {@code factory-name.tool-name}.
	 * @return The tool, or <code>null</code> if no tool with the given name exists.
	 */
	public Tool getTool(String qualifiedName) {
		return _tools.get(qualifiedName);
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
	 * Returns tool schemas for the specified qualified tool names.
	 *
	 * @param qualifiedToolNames
	 *        Collection of qualified tool names in the format {@code factory-name.tool-name}.
	 * @return A list of tool schemas for the requested tools.
	 * @throws IllegalArgumentException
	 *         If any of the specified tool names is not found in the registry.
	 */
	public List<ToolSchema> getToolSchemas(Collection<String> qualifiedToolNames) {
		List<ToolSchema> schemas = new ArrayList<>();

		for (String qualifiedName : qualifiedToolNames) {
			Tool tool = _tools.get(qualifiedName);
			if (tool != null) {
				schemas.add(tool.schema());
			} else {
				throw new IllegalArgumentException("Tool '" + qualifiedName + "' not found in registry."
					+ " Available tools: " + _tools.keySet());
			}
		}

		return schemas;
	}

	/**
	 * Returns the singleton {@link ToolRegistryService} instance.
	 */
	public static ToolRegistryService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
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
