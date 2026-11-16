/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.registry;

import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tl.ai.core.security.ReadFilter;
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
 *
 * <p>
 * A configured script body may return anything, model objects included, so the result goes through the
 * run's {@link ReadFilter} rather than straight into
 * {@link SearchExpression#asString(Object)} - which labels a model object, and a label is business
 * data. Object-typed arguments are checked the same way, where their TID is resolved.
 * </p>
 *
 * <p>
 * The marking that comes with that filtering is no longer this path's alone: a Java tool's answer is
 * marked by {@link com.top_logic.tl.ai.tool.api.AnnotatedTools} in the same way, so a tool rewritten
 * from TL-Script in Java keeps saying what its payload is (issue #478).
 * </p>
 *
 * <h2>What a script tool can say about its result, and what it cannot</h2>
 *
 * <p>
 * A script tool answers with the same {@link ToolResult} a Java tool does, and it states the two
 * fields that matter for a long answer: the {@code truncation-hint}, which is how much of the result
 * reaches the model before it is cut, and the {@code summary}, an expression over the result that
 * compaction keeps in place of it. Both are configured beside the implementation - see
 * {@code ScriptToolFactory.ScriptToolConfig} - and both are unset by default, which is what a script
 * tool answered before (Gitea #381).
 * </p>
 *
 * <p>
 * The one thing a script body still cannot do is return
 * {@link com.top_logic.tl.ai.tool.api.ToolContent} - an image or a PDF for the model to perceive -
 * because TL-Script has no value that says "this is a picture". Which run the call belongs to is not
 * missing: {@code aiRunInfo()} answers it inside a script body, from the same record a Java tool reads
 * through {@link ToolExecutionContext#runInfo()}.
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

	private final QueryExecutor _summary;

	private final int _truncationHint;

	private final QueryExecutor _commitMessage;

	private final ResKey _label;

	/**
	 * Creates a new {@link ScriptToolExecutor}.
	 *
	 * @param summary
	 *        Computes what compaction keeps in place of the result, from the result; may be
	 *        <code>null</code>, which keeps the beginning of the result instead.
	 * @param truncationHint
	 *        How many characters of the result reach the model, or a value of zero or less for the
	 *        installation's default.
	 * @param commitMessage
	 *        Computes the commit message of a call from its arguments; may be <code>null</code>.
	 * @param label
	 *        The tool's label, the commit message of a call that has no other; may be
	 *        <code>null</code>.
	 */
	public ScriptToolExecutor(String name, String description, String inputSchemaJson,
			QueryExecutor executor, ScriptParamCoercion coercion, ToolMode mode,
			boolean concurrencySafe, QueryExecutor summary, int truncationHint,
			QueryExecutor commitMessage, ResKey label) {
		_name = name;
		_description = description;
		_schema = new ToolSchema(name, description, inputSchemaJson);
		_executor = executor;
		_coercion = coercion;
		_mode = mode;
		_concurrencySafe = concurrencySafe;
		_summary = summary;
		_truncationHint = truncationHint > 0 ? truncationHint : -1;
		_commitMessage = commitMessage;
		_label = label;
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

	/**
	 * The value of the tool's {@code commit-message} expression for the call's arguments; without
	 * one, or for an empty answer, the tool's label, and without a label the generic message.
	 *
	 * <p>
	 * An internationalized text the expression answers is recorded as it is, so that the log shows
	 * it in the reader's language; any other value as its text.
	 * </p>
	 */
	@Override
	public ResKey commitMessage(Map<String, Object> arguments, ToolExecutionContext context) {
		if (_commitMessage != null) {
			Object[] positional = _coercion.coerce(arguments, ReadFilter.of(context).security());
			Object value = _commitMessage.executeWith(Args.some(positional));
			if (value instanceof ResKey key) {
				return key;
			}
			String text = value == null ? "" : SearchExpression.asString(value);
			if (!text.isBlank()) {
				return ResKey.text(text);
			}
		}
		return _label != null ? _label : Tool.super.commitMessage(arguments, context);
	}

	@Override
	public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
		// Resolved before the script runs, and outside the catch: a call that cannot name its principal
		// is a programming error, not a failed tool call.
		ReadFilter readFilter = ReadFilter.of(context);
		try {
			// Both directions are filtered. An object argument the principal may not read is refused
			// where the TID is resolved, and the result is disclosed through the same filter -
			// SearchExpression.asString labels a model object, and a label is business data. Being
			// business data is also what the rendering is marked as, for the model's benefit.
			Object[] positional = _coercion.coerce(args, readFilter.security());
			Object result = _executor.executeWith(Args.some(positional));
			// The summary is disclosed through the same filter as the result: it is computed from the
			// raw value, so a summary naming an object would otherwise reach the model past the check
			// the result itself passed.
			String summary = _summary == null ? null
				: readFilter.text(_name, _summary.execute(result));
			return new ToolResult(readFilter.text(_name, result), summary, _truncationHint);
		} catch (Exception ex) {
			Logger.error("Error executing script tool '" + _name + "': " + ex.getMessage(), ex,
				ScriptToolExecutor.class);
			// Reported as a failure, not as a result that reads like one: a script tool authored in
			// the running application would otherwise fail invisibly, because only a thrown exception
			// ever set the flag the provider carries beside the text (Gitea #387). It stays a failure
			// the turn is not rolled back for - which is what this catch already decided by returning
			// rather than rethrowing.
			return ToolResult.failure("Error executing tool: " + ex.getMessage());
		}
	}

}
