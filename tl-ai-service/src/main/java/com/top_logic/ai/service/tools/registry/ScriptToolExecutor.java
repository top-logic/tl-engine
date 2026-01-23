/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.registry;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.json.JSON;
import com.top_logic.model.search.expr.ToString;
import com.top_logic.model.search.expr.query.QueryExecutor;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.service.tool.ToolExecutor;

/**
 * A {@link ToolExecutor} that executes tools implemented in TL-Script.
 *
 * <p>
 * This executor takes a TL-Script {@link QueryExecutor} and passes tool parameters as script
 * arguments. The script's return value is converted to a string as the tool result.
 * </p>
 */
public class ScriptToolExecutor implements ToolExecutor {

	private final QueryExecutor _executor;

	private final List<String> _parameterNames;

	/**
	 * Creates a new {@link ScriptToolExecutor}.
	 *
	 * @param executor
	 *        The TL-Script query executor that implements the tool logic.
	 * @param parameterNames
	 *        The ordered list of parameter names as defined in the tool configuration.
	 */
	public ScriptToolExecutor(QueryExecutor executor, List<String> parameterNames) {
		_executor = executor;
		_parameterNames = parameterNames;
	}

	@Override
	public String execute(ToolExecutionRequest request, Object memoryId) {
		String argumentsJson = request.arguments();

		try {
			// Parse JSON arguments into a map
			Map<String, Object> namedArgs = null;
			if (argumentsJson != null && !argumentsJson.isEmpty()) {
				Object parsed = JSON.fromString(argumentsJson);
				if (parsed instanceof Map) {
					@SuppressWarnings("unchecked")
					Map<String, Object> argsMap = (Map<String, Object>) parsed;
					namedArgs = argsMap;
				}
			}

			// Convert named arguments to positional array based on parameter order
			Object[] positionalArgs = new Object[_parameterNames.size()];
			if (namedArgs != null) {
				for (int i = 0; i < _parameterNames.size(); i++) {
					positionalArgs[i] = namedArgs.get(_parameterNames.get(i));
				}
			}

			// Execute the TL-Script with the positional arguments
			return ToString.toString(_executor.execute(positionalArgs));
		} catch (Exception ex) {
			// Return error message as tool result
			return "Error executing tool: " + ex.getMessage();
		}
	}

}
