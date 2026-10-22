/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.authoring;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.model.search.configured.ScriptParameter;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.tl.ai.core.tools.authoring.messages.OperationResult;
import com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolDetail;
import com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolFactoryListResult;
import com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolListResult;
import com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo;
import com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolSummary;
import com.top_logic.tl.ai.core.tools.registry.ScriptToolFactory.ScriptToolConfig;
import com.top_logic.tl.ai.core.tools.registry.ScriptToolFactory;
import com.top_logic.tl.ai.core.tools.registry.ToolDescriptions;
import com.top_logic.tl.ai.core.tools.util.DataClassSerializer;
import com.top_logic.tl.ai.tool.api.AnswerMarking;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.registry.ToolFactory;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

/**
 * Tool for creating, inspecting, updating, and deleting TL-Script tools in the {@code custom}
 * factory of the {@link ToolRegistryService} configuration.
 *
 * <p>
 * Mutating methods persist a configuration overlay and restart the {@link ToolRegistryService}
 * so that changes take effect immediately; they inherit {@link ToolMode#STRUCTURAL} (approval
 * required, blocked in production). Query methods are {@link ToolMode#READ} and read the
 * persisted configuration.
 * </p>
 */
public class ScriptToolAuthoringTool {

	/**
	 * Configuration for {@link ScriptToolAuthoringTool}.
	 */
	public interface Config extends PolymorphicConfiguration<ScriptToolAuthoringTool> {
		// no additional properties
	}

	private final Supplier<ToolRegistryService.Config<?>> _source;

	private final Consumer<ToolRegistryService.Config<?>> _persister;

	/**
	 * Creates a {@link ScriptToolAuthoringTool} wired to the live application configuration.
	 */
	public ScriptToolAuthoringTool() {
		this(ScriptToolAuthoringTool::liveConfig, ScriptToolConfigPersistence::persist);
	}

	/**
	 * Creates a {@link ScriptToolAuthoringTool} from configuration.
	 */
	@SuppressWarnings("unused")
	@CalledByReflection
	public ScriptToolAuthoringTool(InstantiationContext context, Config config) {
		this();
	}

	/**
	 * Creates a {@link ScriptToolAuthoringTool} with an injected configuration source and
	 * persister (used by tests).
	 */
	public ScriptToolAuthoringTool(Supplier<ToolRegistryService.Config<?>> source,
			Consumer<ToolRegistryService.Config<?>> persister) {
		_source = source;
		_persister = persister;
	}

	/**
	 * Creates a new TL-Script tool.
	 *
	 * @return JSON-serialized {@link OperationResult}.
	 */
	@ToolMethod(description = "Create a TL-Script tool (a tool whose body is a TL-Script expression). "
		+ "Persists to the named factory (default 'custom') and restarts the tool registry; "
		+ "the tool is immediately usable as {factory}.{name}.", group = ToolGroups.AGENT_AUTHORING,
		marking = AnswerMarking.NONE)
	public String createScriptTool(
			@P("Unique tool name; letters, digits, _ and -") String name,
			@P(value = "Human-readable label", required = false) String label,
			@P("Description telling the model when and how to call the tool") String description,
			@P(value = "Declared parameters. Each object has: name (string), type (qualified TL type, e.g. tl.core:String), description (string), mandatory (boolean). Optional fields: multiple (boolean, default false), defaultValue (TL-Script expression, default null).",
				required = false) List<ScriptToolStore.ParamSpec> parameters,
			@P("TL-Script body; parameters are bound as $name (e.g. \"Hello \" + $name)") String implementation,
			@P(value = "Tool mode: READ (only reads data, no side effects), USER_SCOPED (reads user-specific state), DATA_WRITE (modifies data, default), STRUCTURAL (changes system config, requires approval), EXTERNAL (calls a system outside this application, requires approval)",
				required = false) String mode,
			@P(value = "Whether the tool may run in parallel with other concurrency-safe tools (default true)",
				required = false) Boolean concurrencySafe,
			@P(value = "Factory name to group the tool under (default 'custom'); the tool is referenced as {factory}.{name}",
				required = false) String factory) {
		String factoryName = resolveFactory(factory);
		try {
			ToolRegistryService.Config<?> config = copyOfSource();
			ScriptToolStore store = new ScriptToolStore(config, factoryName);
			store.create(name, label, description, parameters, implementation, mode,
				concurrencySafe == null || concurrencySafe.booleanValue());
			_persister.accept(config);
			return ok("Script tool '" + factoryName + "." + name + "' created and activated.",
				"createScriptTool()");
		} catch (Exception ex) {
			return error("Failed to create script tool: " + ex.getMessage(), "createScriptTool()", ex);
		}
	}

	/**
	 * Updates an existing TL-Script tool; omitted fields are left unchanged.
	 *
	 * @return JSON-serialized {@link OperationResult}.
	 */
	@ToolMethod(description = "Update an existing TL-Script tool. "
		+ "Omitted fields are left unchanged. Changes take effect immediately.", group = ToolGroups.AGENT_AUTHORING,
		marking = AnswerMarking.NONE)
	public String updateScriptTool(
			@P("Name of the tool to update") String name,
			@P(value = "New label (omit to leave unchanged)", required = false) String label,
			@P(value = "New description (omit to leave unchanged)", required = false) String description,
			@P(value = "Declared parameters. Each object has: name (string), type (qualified TL type, e.g. tl.core:String), description (string), mandatory (boolean). Optional fields: multiple (boolean, default false), defaultValue (TL-Script expression, default null).",
				required = false) List<ScriptToolStore.ParamSpec> parameters,
			@P(value = "New TL-Script body (omit to leave unchanged)", required = false) String implementation,
			@P(value = "New mode (omit to leave unchanged)", required = false) String mode,
			@P(value = "New concurrency-safe flag (omit to leave unchanged)",
				required = false) Boolean concurrencySafe,
			@P(value = "Factory name (default 'custom')", required = false) String factory) {
		String factoryName = resolveFactory(factory);
		try {
			ToolRegistryService.Config<?> config = copyOfSource();
			ScriptToolStore store = new ScriptToolStore(config, factoryName);
			store.update(name, label, description, parameters, implementation, mode, concurrencySafe);
			_persister.accept(config);
			return ok("Script tool '" + factoryName + "." + name + "' updated and activated.",
				"updateScriptTool()");
		} catch (Exception ex) {
			return error("Failed to update script tool: " + ex.getMessage(), "updateScriptTool()", ex);
		}
	}

	/**
	 * Deletes a TL-Script tool.
	 *
	 * @return JSON-serialized {@link OperationResult}.
	 */
	@ToolMethod(description = "Delete a TL-Script tool. Takes effect immediately.", group = ToolGroups.AGENT_AUTHORING, marking = AnswerMarking.NONE)
	public String deleteScriptTool(
			@P("Name of the tool to delete") String name,
			@P(value = "Factory name (default 'custom')", required = false) String factory) {
		String factoryName = resolveFactory(factory);
		try {
			ToolRegistryService.Config<?> config = copyOfSource();
			ScriptToolStore store = new ScriptToolStore(config, factoryName);
			store.delete(name);
			_persister.accept(config);
			return ok("Script tool '" + factoryName + "." + name + "' deleted.",
				"deleteScriptTool()");
		} catch (Exception ex) {
			return error("Failed to delete script tool: " + ex.getMessage(), "deleteScriptTool()", ex);
		}
	}

	/**
	 * Gets the full definition of a TL-Script tool.
	 *
	 * @return JSON-serialized {@link ScriptToolDetail}; {@code exists} is {@code false} if absent.
	 */
	@ToolMethod(description = "Get the full definition of a TL-Script tool",
		mode = ToolMode.READ, group = ToolGroups.AGENT_META)
	public String getScriptTool(
			@P("Tool name") String name,
			@P(value = "Factory name (default 'custom')", required = false) String factory) {
		String factoryName = resolveFactory(factory);
		ScriptToolDetail detail = ScriptToolDetail.create();
		detail.setName(name);
		try {
			ScriptToolConfig cfg = new ScriptToolStore(copyOfSource(), factoryName).get(name);
			if (cfg == null) {
				detail.setExists(false);
			} else {
				detail.setExists(true);
				detail.setLabel(cfg.getLabel() == null ? "" : ToolDescriptions.englishPlainText(cfg.getLabel()));
				detail.setDescription(cfg.getDescription() == null ? ""
					: ToolDescriptions.englishPlainText(cfg.getDescription()));
				detail.setImplementation(ExprFormat.INSTANCE.getSpecification(cfg.getImplementation()));
				detail.setMode(cfg.getMode().name());
				detail.setConcurrencySafe(cfg.isConcurrencySafe());
				for (ScriptParameter p : cfg.getParameters()) {
					ScriptToolParamInfo info = ScriptToolParamInfo.create();
					info.setName(p.getName());
					info.setType(p.getType().qualifiedName());
					info.setDescription(p.getDescription() == null ? ""
						: ToolDescriptions.englishPlainText(p.getDescription()));
					info.setMandatory(p.isMandatory());
					info.setMultiple(p.isMultiple());
					info.setDefaultValue(p.getDefaultValue() == null ? ""
						: ExprFormat.INSTANCE.getSpecification(p.getDefaultValue()));
					detail.addParameter(info);
				}
			}
			return DataClassSerializer.toJson(detail, ScriptToolAuthoringTool.class, "getScriptTool()");
		} catch (Exception ex) {
			return error("Failed to read script tool: " + ex.getMessage(), "getScriptTool()", ex);
		}
	}

	/**
	 * Lists all TL-Script tools in the given factory.
	 *
	 * @return JSON-serialized {@link ScriptToolListResult}.
	 */
	@ToolMethod(description = "List all TL-Script tools in a factory. Tools are referenced as "
		+ "{factory}__{name} (double underscore); parse the factory prefix from any known tool name "
		+ "to call this correctly. If the factory is unknown, call listScriptToolFactories first.",
		mode = ToolMode.READ, group = ToolGroups.AGENT_META)
	public String listScriptTools(
			@P(value = "Factory name (default 'custom')", required = false) String factory) {
		String factoryName = resolveFactory(factory);
		try {
			ScriptToolListResult result = ScriptToolListResult.create();
			for (ScriptToolConfig cfg : new ScriptToolStore(copyOfSource(), factoryName).list()) {
				ScriptToolSummary summary = ScriptToolSummary.create();
				summary.setName(cfg.getName());
				summary.setDescription(cfg.getDescription() == null ? ""
					: ToolDescriptions.englishPlainText(cfg.getDescription()));
				summary.setMode(cfg.getMode().name());
				result.addTool(summary);
			}
			return DataClassSerializer.toJson(result, ScriptToolAuthoringTool.class, "listScriptTools()");
		} catch (Exception ex) {
			return error("Failed to list script tools: " + ex.getMessage(), "listScriptTools()", ex);
		}
	}

	/**
	 * Lists the factory names that hold at least one registered TL-Script tool.
	 *
	 * @return JSON-serialized {@link ScriptToolFactoryListResult}.
	 */
	@ToolMethod(
		description = "Returns all distinct factory names that have registered TL-Script tools. "
			+ "Call this before listScriptTools when the factory name is unknown.",
		mode = ToolMode.READ, group = ToolGroups.AGENT_META,
		marking = AnswerMarking.NONE)
	public String listScriptToolFactories() {
		try {
			ScriptToolFactoryListResult result = ScriptToolFactoryListResult.create();
			for (Map.Entry<String, ToolRegistryService.ToolFactoryConfig> entry : copyOfSource()
				.getToolFactories().entrySet()) {
				PolymorphicConfiguration<? extends ToolFactory> impl = entry.getValue().getImplementation();
				if (impl instanceof ScriptToolFactory.Config scriptConfig && !scriptConfig.getTools().isEmpty()) {
					result.addFactoryName(entry.getKey());
				}
			}
			return DataClassSerializer.toJson(result, ScriptToolAuthoringTool.class, "listScriptToolFactories()");
		} catch (Exception ex) {
			return error("Failed to list script tool factories: " + ex.getMessage(),
				"listScriptToolFactories()", ex);
		}
	}

	private ToolRegistryService.Config<?> copyOfSource() {
		return TypedConfiguration.copy(_source.get());
	}

	@SuppressWarnings("unchecked")
	private static ToolRegistryService.Config<?> liveConfig() {
		try {
			return (ToolRegistryService.Config<?>) ApplicationConfig.getInstance()
				.getServiceConfiguration(ToolRegistryService.class);
		} catch (ConfigurationException ex) {
			throw new RuntimeException("Failed to read ToolRegistryService configuration: " + ex.getMessage(), ex);
		}
	}

	private static String resolveFactory(String factory) {
		return (factory == null || factory.isBlank()) ? ScriptToolStore.DEFAULT_FACTORY : factory.trim();
	}

	private static String ok(String message, String operation) {
		OperationResult result = OperationResult.create();
		result.setSuccess(true);
		result.setResultMessage(message);
		return DataClassSerializer.toJson(result, ScriptToolAuthoringTool.class, operation);
	}

	private static String error(String message, String operation, Exception ex) {
		Logger.error(operation + " error: " + ex.getMessage(), ex, ScriptToolAuthoringTool.class);
		return DataClassSerializer.errorToJson(message, ScriptToolAuthoringTool.class, operation);
	}
}
