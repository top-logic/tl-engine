/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.ai.service.scripting.JsonSchemaConverter;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.service.tool.ToolExecutor;

/**
 * A {@link ToolFactory} that creates tools from TL-Script implementations.
 *
 * <p>
 * This factory allows defining tools directly in configuration using TL-Script code. Each tool
 * consists of a specification (name, description, parameters) and a TL-Script expression that
 * implements the tool's logic.
 * </p>
 */
public class ScriptToolFactory implements ToolFactory, ConfiguredInstance<ScriptToolFactory.Config> {

	/**
	 * Configuration for {@link ScriptToolFactory}.
	 */
	@DisplayOrder({
		Config.TOOLS
	})
	public interface Config extends PolymorphicConfiguration<ScriptToolFactory> {

		/**
		 * Configuration property name for tools.
		 *
		 * @see #getTools()
		 */
		String TOOLS = "tools";

		/**
		 * The tools to create.
		 *
		 * <p>
		 * Each tool is defined by its specification (name, description, parameters) and a TL-Script
		 * implementation.
		 * </p>
		 */
		@Name(TOOLS)
		@Key(ScriptToolConfig.NAME)
		Map<String, ScriptToolConfig> getTools();

	}

	/**
	 * Configuration for a single TL-Script tool.
	 */
	@DisplayOrder({
		ScriptToolConfig.NAME,
		ScriptToolConfig.PARAMETERS,
		ScriptToolConfig.DESCRIPTION,
		ScriptToolConfig.IMPLEMENTATION,
	})
	public interface ScriptToolConfig extends PolymorphicConfiguration<ScriptToolConfig> {

		/**
		 * Configuration property name for tool name.
		 *
		 * @see #getName()
		 */
		String NAME = "name";

		/**
		 * Configuration property name for description.
		 *
		 * @see #getDescription()
		 */
		String DESCRIPTION = "description";

		/**
		 * Configuration property name for parameters.
		 *
		 * @see #getParameters()
		 */
		String PARAMETERS = "parameters";

		/**
		 * Configuration property name for implementation.
		 *
		 * @see #getImplementation()
		 */
		String IMPLEMENTATION = "implementation";

		/**
		 * The name of the tool.
		 */
		@Name(NAME)
		@Mandatory
		String getName();

		/**
		 * The description of what the tool does.
		 */
		@Name(DESCRIPTION)
		@Mandatory
		@ControlProvider(MultiLineText.class)
		String getDescription();

		/**
		 * The parameters this tool accepts.
		 *
		 * <p>
		 * Each parameter is defined by its name and configuration (type, description, etc.).
		 * </p>
		 */
		@Name(PARAMETERS)
		@Key(ToolParameterConfig.NAME)
		Map<String, ToolParameterConfig> getParameters();

		/**
		 * The TL-Script implementation of the tool.
		 *
		 * <p>
		 * The script receives the tool parameters as variables and should return the tool's result
		 * as a string.
		 * </p>
		 */
		@Name(IMPLEMENTATION)
		@Mandatory
		Expr getImplementation();

	}

	/**
	 * Configuration for a tool parameter.
	 */
	@DisplayOrder({
		ToolParameterConfig.NAME,
		ToolParameterConfig.TYPE,
		ToolParameterConfig.DESCRIPTION,
	})
	public interface ToolParameterConfig extends PolymorphicConfiguration<ToolParameterConfig> {

		/**
		 * Configuration property name for parameter name.
		 *
		 * @see #getName()
		 */
		String NAME = "name";

		/**
		 * Configuration property name for type.
		 *
		 * @see #getType()
		 */
		String TYPE = "type";

		/**
		 * Configuration property name for description.
		 *
		 * @see #getDescription()
		 */
		String DESCRIPTION = "description";

		/**
		 * The name of the parameter.
		 */
		@Name(NAME)
		@Mandatory
		String getName();

		/**
		 * The JSON schema type of the parameter (e.g., "string", "number", "boolean", "object",
		 * "array").
		 */
		@Name(TYPE)
		@Mandatory
		String getType();

		/**
		 * The description of the parameter.
		 */
		@Name(DESCRIPTION)
		@ControlProvider(MultiLineText.class)
		String getDescription();

	}

	private final Config _config;

	private final List<ToolImplementation> _tools;

	/**
	 * Creates a new {@link ScriptToolFactory} from the given configuration.
	 *
	 * @param context
	 *        The instantiation context for error reporting.
	 * @param config
	 *        The configuration.
	 */
	public ScriptToolFactory(InstantiationContext context, Config config) {
		_config = config;
		_tools = new ArrayList<>();

		for (Map.Entry<String, ScriptToolConfig> entry : config.getTools().entrySet()) {
			ScriptToolConfig toolConfig = entry.getValue();

			// Build parameters as JsonObjectSchema and collect parameter names
			JsonObjectSchema.Builder parametersBuilder = JsonObjectSchema.builder();
			List<String> parameterNames = new ArrayList<>();
			for (ToolParameterConfig paramConfig : toolConfig.getParameters().values()) {
				// Use JsonSchemaConverter to add the property
				JsonSchemaConverter.addProperty(parametersBuilder,
					paramConfig.getName(),
					paramConfig.getType(),
					paramConfig.getDescription());

				// Collect parameter name in order
				parameterNames.add(paramConfig.getName());
			}
			JsonObjectSchema parameters = parametersBuilder.build();

			// Build tool specification
			ToolSpecification specification = ToolSpecification.builder()
				.name(toolConfig.getName())
				.description(toolConfig.getDescription())
				.parameters(parameters)
				.build();

			// Create executor from TL-Script expression
			QueryExecutor executor = QueryExecutor.compile(toolConfig.getImplementation());
			ToolExecutor toolExecutor = new ScriptToolExecutor(executor, parameterNames);

			_tools.add(new ToolImplementation(specification, toolExecutor));
		}
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public List<ToolImplementation> createTools() {
		return _tools;
	}

}
