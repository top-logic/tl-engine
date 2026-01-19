/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderRequest;
import dev.langchain4j.service.tool.ToolProviderResult;

/**
 * Registry service for managing tools available in the application.
 *
 * <p>
 * Tools are organized by factory. Each factory produces a set of tools, and each tool is
 * identified by a qualified name in the format {@code {factory-name}.{tool-name}}. This allows
 * tools from different factories to have the same simple name without conflicts.
 * </p>
 */
public class ToolRegistry extends ConfiguredManagedClass<ToolRegistry.Config<?>> {

	/**
	 * Configuration for the {@link ToolRegistry} service.
	 */
	public interface Config<I extends ToolRegistry> extends ConfiguredManagedClass.Config<I> {

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

	private Map<String, ToolImplementation> _tools;

	/**
	 * Creates a new {@link ToolRegistry} from the given configuration.
	 */
	public ToolRegistry(InstantiationContext context, Config<?> config) {
		super(context, config);
		_context = context;
	}

	@Override
	protected void startUp() {
		super.startUp();

		_tools = new java.util.HashMap<>();
		for (Map.Entry<String, ToolFactoryConfig> entry : getConfig().getToolFactories().entrySet()) {
			String factoryName = entry.getKey();
			try {
				ToolFactory factory = _context.getInstance(entry.getValue().getImplementation());
				if (factory == null) {
					Logger.warn("Failed to instantiate tool factory '" + factoryName
						+ "': Factory instance is null.", ToolRegistry.class);
					continue;
				}

				// Create tools from this factory and register them with qualified names
				for (ToolImplementation tool : factory.createTools()) {
					String qualifiedName = factoryName + "." + tool.getName();
					_tools.put(qualifiedName, tool);
				}
			} catch (Exception ex) {
				Logger.error("Failed to initialize tool factory '" + factoryName + "': " + ex.getMessage(), ex,
					ToolRegistry.class);
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
	public ToolImplementation getTool(String qualifiedName) {
		return _tools.get(qualifiedName);
	}

	/**
	 * Returns all registered tool names.
	 *
	 * <p>
	 * Tool names are in the qualified format {@code factory-name.tool-name}.
	 * </p>
	 */
	public java.util.Set<String> getToolNames() {
		return _tools.keySet();
	}

	/**
	 * Returns all registered tools.
	 */
	public java.util.Collection<ToolImplementation> getTools() {
		return _tools.values();
	}

	/**
	 * Creates a {@link ToolProvider} that provides access to the specified tools.
	 *
	 * <p>
	 * The returned tool provider can be used with AI services to make the specified tools
	 * available to AI agents during conversation.
	 * </p>
	 *
	 * @param qualifiedToolNames
	 *        Collection of qualified tool names in the format {@code factory-name.tool-name}.
	 * @return A tool provider that provides the requested tools.
	 * @throws IllegalArgumentException
	 *         If any of the specified tool names is not found in the registry.
	 */
	public ToolProvider createToolProvider(Collection<String> qualifiedToolNames) {
		ToolProviderResult.Builder builder = ToolProviderResult.builder();

		for (String qualifiedName : qualifiedToolNames) {
			ToolImplementation tool = _tools.get(qualifiedName);
			if (tool != null) {
				builder.add(tool.getSpecification(), tool.getExecutor());
			} else {
				throw new IllegalArgumentException("Tool '" + qualifiedName + "' not found in registry."
					+ " Available tools: " + _tools.keySet());
			}
		}

		ToolProviderResult result = builder.build();

		// Return a simple ToolProvider that always returns the same result
		return new ToolProvider() {
			@Override
			public ToolProviderResult provideTools(ToolProviderRequest request) {
				return result;
			}
		};
	}

	/**
	 * Returns a list of tool specifications for the specified qualified tool names.
	 *
	 * <p>
	 * This method retrieves the {@link ToolSpecification} objects for the requested tools,
	 * which can be used with chat requests.
	 * </p>
	 *
	 * @param qualifiedToolNames
	 *        Collection of qualified tool names in the format {@code factory-name.tool-name}.
	 * @return A list of tool specifications for the requested tools.
	 * @throws IllegalArgumentException
	 *         If any of the specified tool names is not found in the registry.
	 */
	public List<ToolSpecification> getToolSpecifications(Collection<String> qualifiedToolNames) {
		List<ToolSpecification> specs = new ArrayList<>();

		for (String qualifiedName : qualifiedToolNames) {
			ToolImplementation tool = _tools.get(qualifiedName);
			if (tool != null) {
				specs.add(tool.getSpecification());
			} else {
				throw new IllegalArgumentException("Tool '" + qualifiedName + "' not found in registry."
					+ " Available tools: " + _tools.keySet());
			}
		}

		return specs;
	}

	/**
	 * Returns the singleton {@link ToolRegistry} instance.
	 */
	public static ToolRegistry getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module for the {@link ToolRegistry} service.
	 */
	public static final class Module extends TypedRuntimeModule<ToolRegistry> {

		/**
		 * Singleton instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<ToolRegistry> getImplementation() {
			return ToolRegistry.class;
		}

	}

}
