/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.registry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

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
import com.top_logic.tl.ai.core.tools.Tool;
import com.top_logic.tl.ai.core.tools.ToolMode;

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
		ScriptToolConfig.MODE,
		ScriptToolConfig.CONCURRENCY_SAFE,
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
		 * Configuration property name for mode.
		 *
		 * @see #getMode()
		 */
		String MODE = "mode";

		/**
		 * Configuration property name for concurrency-safe.
		 *
		 * @see #getConcurrencySafe()
		 */
		String CONCURRENCY_SAFE = "concurrency-safe";

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
		 * What kind of state change this tool makes.
		 *
		 * <p>
		 * Defaults to {@link ToolMode#GLOBAL}. Set to {@link ToolMode#READ} for tools that only
		 * read data, or {@link ToolMode#USER_SCOPED} for tools that only affect the current user.
		 * </p>
		 *
		 * @see Tool#mode()
		 */
		@Name(MODE)
		ToolMode getMode();

		/**
		 * Whether this tool can safely run in parallel with other concurrency-safe tools.
		 *
		 * <p>
		 * When {@code null} (the default), the value is derived from {@link #getMode()}: {@code
		 * true} for {@link ToolMode#READ} and {@link ToolMode#USER_SCOPED}, {@code false} for
		 * {@link ToolMode#GLOBAL}.
		 * </p>
		 *
		 * @see Tool#isConcurrencySafe()
		 */
		@Name(CONCURRENCY_SAFE)
		Boolean getConcurrencySafe();

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

	private final List<Tool> _tools;

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

			List<String> parameterNames = new ArrayList<>();
			for (ToolParameterConfig paramConfig : toolConfig.getParameters().values()) {
				parameterNames.add(paramConfig.getName());
			}

			String inputSchemaJson = buildInputSchemaJson(toolConfig, parameterNames);

			QueryExecutor executor = QueryExecutor.compile(toolConfig.getImplementation());

			ToolMode mode = toolConfig.getMode();
			if (mode == null) {
				mode = ToolMode.GLOBAL;
			}

			Boolean concurrencySafe = toolConfig.getConcurrencySafe();

			_tools.add(new ScriptToolExecutor(
				toolConfig.getName(),
				toolConfig.getDescription(),
				inputSchemaJson,
				executor,
				parameterNames,
				mode,
				concurrencySafe));
		}
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public List<Tool> createTools() {
		return _tools;
	}

	private static String buildInputSchemaJson(ScriptToolConfig toolConfig, List<String> parameterNames) {
		try {
			StringW buffer = new StringW();
			try (JsonWriter json = new JsonWriter(buffer)) {
				json.beginObject();
				json.name("type").value("object");

				json.name("properties");
				json.beginObject();
				for (ToolParameterConfig paramConfig : toolConfig.getParameters().values()) {
					json.name(paramConfig.getName());
					json.beginObject();
					json.name("type").value(paramConfig.getType());
					String description = paramConfig.getDescription();
					if (description != null && !description.isEmpty()) {
						json.name("description").value(description);
					}
					json.endObject();
				}
				json.endObject();

				json.name("required");
				json.beginArray();
				for (String name : parameterNames) {
					json.value(name);
				}
				json.endArray();

				json.endObject();
			}
			return buffer.toString();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to build input schema JSON", e);
		}
	}

}
