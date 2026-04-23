/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.registry;

import java.util.List;
import java.util.Map;

import com.top_logic.model.search.expr.ToString;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.provider.ToolSchema;
import com.top_logic.tl.ai.core.tools.Tool;
import com.top_logic.tl.ai.core.tools.ToolMode;

/**
 * A {@link Tool} that executes tools implemented in TL-Script.
 *
 * <p>
 * This executor takes a TL-Script {@link QueryExecutor} and passes tool parameters as script
 * arguments. The script's return value is converted to a string as the tool result.
 * </p>
 */
public class ScriptToolExecutor implements Tool {

	private final String _name;

	private final String _description;

	private final ToolSchema _schema;

	private final QueryExecutor _executor;

	private final List<String> _parameterNames;

	private final ToolMode _mode;

	private final Boolean _concurrencySafe;

	/**
	 * Creates a new {@link ScriptToolExecutor}.
	 *
	 * @param name
	 *        The tool name.
	 * @param description
	 *        The tool description.
	 * @param inputSchemaJson
	 *        The JSON Schema string describing the tool's input parameters.
	 * @param executor
	 *        The TL-Script query executor that implements the tool logic.
	 * @param parameterNames
	 *        The ordered list of parameter names as defined in the tool configuration.
	 * @param mode
	 *        The {@link ToolMode} for this tool.
	 * @param concurrencySafe
	 *        Whether this tool is concurrency-safe, or {@code null} to derive from the mode.
	 */
	public ScriptToolExecutor(String name, String description, String inputSchemaJson,
			QueryExecutor executor, List<String> parameterNames, ToolMode mode, Boolean concurrencySafe) {
		_name = name;
		_description = description;
		_schema = new ToolSchema(name, description, inputSchemaJson);
		_executor = executor;
		_parameterNames = parameterNames;
		_mode = mode;
		_concurrencySafe = concurrencySafe;
	}

	@Override
	public String name() {
		return _name;
	}

	@Override
	public String description() {
		return _description;
	}

	@Override
	public ToolSchema schema() {
		return _schema;
	}

	@Override
	public ToolMode mode() {
		return _mode;
	}

	@Override
	public boolean isConcurrencySafe() {
		if (_concurrencySafe != null) {
			return _concurrencySafe;
		}
		return _mode != ToolMode.GLOBAL;
	}

	@Override
	public String execute(Map<String, Object> args, ToolContext context) {
		try {
			Object[] positionalArgs = new Object[_parameterNames.size()];
			if (args != null) {
				for (int i = 0; i < _parameterNames.size(); i++) {
					positionalArgs[i] = args.get(_parameterNames.get(i));
				}
			}

			return ToString.toString(_executor.execute(positionalArgs));
		} catch (Exception ex) {
			return "Error executing tool: " + ex.getMessage();
		}
	}

}
