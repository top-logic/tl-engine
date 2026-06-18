/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.authoring;

import java.util.List;
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
import com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolListResult;
import com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo;
import com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolSummary;
import com.top_logic.tl.ai.core.tools.registry.ScriptToolFactory.ScriptToolConfig;
import com.top_logic.tl.ai.core.tools.registry.ToolDescriptions;
import com.top_logic.tl.ai.core.tools.util.DataClassSerializer;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
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
		+ "Persists to the 'custom' factory and restarts the tool registry; the tool is immediately usable as custom.{name}.")
	public String createScriptTool(
			@P("Unique tool name (referenced as custom.{name}); letters, digits, _ and -") String name,
			@P(value = "Human-readable label", required = false) String label,
			@P("Description telling the model when and how to call the tool") String description,
			@P(value = "Declared parameters. Each parameter object has: name; type (a qualified TL type, e.g. tl.core:String, tl.core:Integer, or myModule:MyType); description; mandatory (boolean); multiple (boolean, accepts a list); and defaultValue (optional TL-Script expression used when an optional parameter is omitted).",
				required = false) List<ScriptToolStore.ParamSpec> parameters,
			@P("TL-Script body; parameters are bound as $name (e.g. \"Hello \" + $name)") String implementation,
			@P(value = "Tool mode: READ, USER_SCOPED, DATA_WRITE (default), or STRUCTURAL",
				required = false) String mode,
			@P(value = "Whether the tool may run in parallel with other concurrency-safe tools (default true)",
				required = false) Boolean concurrencySafe) {
		try {
			ToolRegistryService.Config<?> config = copyOfSource();
			ScriptToolStore store = new ScriptToolStore(config);
			store.create(name, label, description, parameters, implementation, mode,
				concurrencySafe == null || concurrencySafe.booleanValue());
			_persister.accept(config);
			return ok("Script tool '" + name + "' created and activated. "
				+ "Assign 'custom." + name + "' to an agent or skill to use it.", "createScriptTool()");
		} catch (Exception ex) {
			return error("Failed to create script tool: " + ex.getMessage(), "createScriptTool()", ex);
		}
	}

	/**
	 * Updates an existing TL-Script tool; omitted fields are left unchanged.
	 *
	 * @return JSON-serialized {@link OperationResult}.
	 */
	@ToolMethod(description = "Update an existing TL-Script tool in the 'custom' factory. "
		+ "Omitted fields are left unchanged. Changes take effect immediately.")
	public String updateScriptTool(
			@P("Name of the tool to update") String name,
			@P(value = "New label (omit to leave unchanged)", required = false) String label,
			@P(value = "New description (omit to leave unchanged)", required = false) String description,
			@P(value = "Declared parameters. Each parameter object has: name; type (a qualified TL type, e.g. tl.core:String, tl.core:Integer, or myModule:MyType); description; mandatory (boolean); multiple (boolean, accepts a list); and defaultValue (optional TL-Script expression used when an optional parameter is omitted).",
				required = false) List<ScriptToolStore.ParamSpec> parameters,
			@P(value = "New TL-Script body (omit to leave unchanged)", required = false) String implementation,
			@P(value = "New mode (omit to leave unchanged)", required = false) String mode,
			@P(value = "New concurrency-safe flag (omit to leave unchanged)",
				required = false) Boolean concurrencySafe) {
		try {
			ToolRegistryService.Config<?> config = copyOfSource();
			ScriptToolStore store = new ScriptToolStore(config);
			store.update(name, label, description, parameters, implementation, mode, concurrencySafe);
			_persister.accept(config);
			return ok("Script tool '" + name + "' updated and activated.",
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
	@ToolMethod(description = "Delete a TL-Script tool from the 'custom' factory. Takes effect immediately.")
	public String deleteScriptTool(@P("Name of the tool to delete") String name) {
		try {
			ToolRegistryService.Config<?> config = copyOfSource();
			ScriptToolStore store = new ScriptToolStore(config);
			store.delete(name);
			_persister.accept(config);
			return ok("Script tool '" + name + "' deleted.",
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
	@ToolMethod(description = "Get the full definition of a TL-Script tool in the 'custom' factory",
		mode = ToolMode.READ)
	public String getScriptTool(@P("Tool name") String name) {
		ScriptToolDetail detail = ScriptToolDetail.create();
		detail.setName(name);
		try {
			ScriptToolConfig cfg = new ScriptToolStore(copyOfSource()).get(name);
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
	 * Lists all TL-Script tools in the {@code custom} factory.
	 *
	 * @return JSON-serialized {@link ScriptToolListResult}.
	 */
	@ToolMethod(description = "List all TL-Script tools in the 'custom' factory", mode = ToolMode.READ)
	public String listScriptTools() {
		try {
			ScriptToolListResult result = ScriptToolListResult.create();
			for (ScriptToolConfig cfg : new ScriptToolStore(copyOfSource()).list()) {
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
