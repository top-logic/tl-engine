/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.registry;

import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.model.search.expr.SearchExpression;
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

	private final String _group;

	private final boolean _concurrencySafe;

	/**
	 * Creates a new {@link ScriptToolExecutor}.
	 *
	 * @param group
	 *        The tool group this tool declares for itself, or the empty string for an ungrouped
	 *        tool - see {@link Tool#group()}.
	 */
	public ScriptToolExecutor(String name, String description, String inputSchemaJson,
			QueryExecutor executor, ScriptParamCoercion coercion, ToolMode mode, String group,
			boolean concurrencySafe) {
		_name = name;
		_description = description;
		_schema = new ToolSchema(name, description, inputSchemaJson);
		_executor = executor;
		_coercion = coercion;
		_mode = mode;
		_group = group != null ? group : "";
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

	/**
	 * The group configured for this script tool, or the empty string.
	 *
	 * <p>
	 * A TL-Script tool has no annotation to carry a group, so it is ungrouped - and therefore denied
	 * to every role under enforced tool access - unless its configuration classifies it or the
	 * access policy does.
	 * </p>
	 */
	@Override
	public String group() {
		return _group;
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
			return ToolResult.of(SearchExpression.asString(result));
		} catch (Exception ex) {
			Logger.error("Error executing script tool '" + _name + "': " + ex.getMessage(), ex,
				ScriptToolExecutor.class);
			return ToolResult.of("Error executing tool: " + ex.getMessage());
		}
	}

}
