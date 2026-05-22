/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.registry;

import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.model.search.expr.ToString;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * A {@link Tool} that executes a TL-Script body compiled with named-parameter bindings.
 *
 * <p>
 * Named arguments arriving from the LLM are translated to the positional {@link Args} array
 * matching the body's {@code Define}-lambda nesting order by {@link ScriptParamCoercion}.
 * </p>
 */
public class ScriptToolExecutor implements Tool {

	private final String _name;

	private final String _description;

	private final ToolSchema _schema;

	private final QueryExecutor _executor;

	private final ScriptParamCoercion _coercion;

	private final ToolMode _mode;

	private final boolean _concurrencySafe;

	/**
	 * Creates a new {@link ScriptToolExecutor}.
	 */
	public ScriptToolExecutor(String name, String description, String inputSchemaJson,
			QueryExecutor executor, ScriptParamCoercion coercion, ToolMode mode,
			boolean concurrencySafe) {
		_name = name;
		_description = description;
		_schema = new ToolSchema(name, description, inputSchemaJson);
		_executor = executor;
		_coercion = coercion;
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
		return _concurrencySafe;
	}

	@Override
	public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
		try {
			Object[] positional = _coercion.coerce(args);
			Object result = _executor.executeWith(Args.some(positional));
			return ToolResult.of(ToString.toString(result));
		} catch (Exception ex) {
			Logger.error("Error executing script tool '" + _name + "': " + ex.getMessage(), ex,
				ScriptToolExecutor.class);
			return ToolResult.of("Error executing tool: " + ex.getMessage());
		}
	}

}
