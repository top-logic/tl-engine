/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.model.search.configured.ConfiguredScript;
import com.top_logic.model.search.configured.ScriptConfiguration;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.registry.ToolFactory;

/**
 * A {@link ToolFactory} that creates tools from TL-Script implementations.
 *
 * <p>
 * Each tool configuration reuses the engine's {@link ScriptConfiguration} contract — the same
 * parameter type-picker, mandatory / multiple flags, default values, and i18n descriptions used
 * by configured TL-Script functions — and adds the AI-specific {@link ToolMode mode} and
 * concurrency flag.
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
		 */
		@Name(TOOLS)
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		Map<String, ScriptToolConfig> getTools();

	}

	/**
	 * Configuration for a single TL-Script tool.
	 *
	 * <p>
	 * Inherits name, parameters (with full {@link com.top_logic.model.TLType} picker), label,
	 * description, and implementation expression from {@link ScriptConfiguration}. Adds the
	 * AI-specific {@link #getMode() mode} and {@link #isConcurrencySafe() concurrency} flags.
	 * </p>
	 */
	@DisplayOrder({
		NamedConfigMandatory.NAME_ATTRIBUTE,
		ScriptConfiguration.LABEL,
		ScriptConfiguration.DESCRIPTION,
		ScriptConfiguration.PARAMETERS,
		ScriptToolConfig.MODE,
		ScriptToolConfig.CONCURRENCY_SAFE,
		ScriptConfiguration.IMPLEMENTATION,
	})
	public interface ScriptToolConfig extends ScriptConfiguration {

		/**
		 * Configuration property name for mode.
		 *
		 * @see #getMode()
		 */
		String MODE = "mode";

		/**
		 * Configuration property name for concurrency-safe.
		 *
		 * @see #isConcurrencySafe()
		 */
		String CONCURRENCY_SAFE = "concurrency-safe";

		/**
		 * What kind of state change this tool makes. Defaults to {@link ToolMode#DATA_WRITE}.
		 *
		 * @see Tool#mode()
		 */
		@Name(MODE)
		@FormattedDefault("DATA_WRITE")
		ToolMode getMode();

		/**
		 * Whether this tool can safely run in parallel with other concurrency-safe tools.
		 *
		 * @see Tool#isConcurrencySafe()
		 */
		@Name(CONCURRENCY_SAFE)
		@BooleanDefault(true)
		boolean isConcurrencySafe();

	}

	private final Config _config;

	private final List<Tool> _tools;

	/**
	 * Creates a new {@link ScriptToolFactory} from the given configuration.
	 */
	public ScriptToolFactory(InstantiationContext context, Config config) {
		_config = config;
		_tools = new ArrayList<>();

		// Map key == tool name (enforced by @Key on getTools()), so values() is sufficient.
		for (ScriptToolConfig toolConfig : config.getTools().values()) {
			ConfiguredScript.Config scriptConfig = asScriptConfig(toolConfig);
			ConfiguredScript script = new ConfiguredScript(context, scriptConfig);
			QueryExecutor executor = script.createExecutor();

			String inputSchema = ToolJsonSchema.build(toolConfig.getParameters());
			String llmDescription = ToolDescriptions.englishPlainText(toolConfig.getDescription());
			ToolMode mode = toolConfig.getMode();

			_tools.add(new ScriptToolExecutor(
				toolConfig.getName(),
				llmDescription,
				inputSchema,
				executor,
				new ScriptParamCoercion(toolConfig.getParameters()),
				mode,
				toolConfig.isConcurrencySafe()));
		}
	}

	/**
	 * Wraps a {@link ScriptToolConfig} in a synthetic
	 * {@link com.top_logic.model.search.configured.ConfiguredScript.Config} so the engine's
	 * executor builder can be reused without our config subtyping the engine's.
	 *
	 * <p>
	 * Copies all properties defined by {@link ScriptConfiguration} as of engine 8.x. If the engine
	 * adds new properties to {@link ScriptConfiguration}, this method must be updated to include
	 * them — there is no compile-time enforcement.
	 * </p>
	 */
	private static ConfiguredScript.Config asScriptConfig(ScriptToolConfig source) {
		ConfiguredScript.Config copy = TypedConfiguration.newConfigItem(ConfiguredScript.Config.class);
		copy.setName(source.getName());
		copy.setLabel(source.getLabel());
		copy.setDescription(source.getDescription());
		copy.setImplementation(source.getImplementation());
		copy.getParameters().addAll(source.getParameters());
		return copy;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public List<Tool> createTools() {
		return _tools;
	}

}
