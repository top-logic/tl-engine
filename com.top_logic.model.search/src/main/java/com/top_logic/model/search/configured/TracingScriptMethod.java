/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.configured;

import java.util.List;

import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.query.Args;

/**
 * {@link GenericMethod} executing the tracing version of the configured script.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TracingScriptMethod extends GenericMethod {

	/**
	 * This constructor creates a new {@link TracingScriptMethod}.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @param arguments
	 *        See {@link #getArguments()}.
	 */
	public TracingScriptMethod(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new TracingScriptMethod(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		return tracingSearch().evalWith(definitions, Args.some(arguments));
	}

	private SearchExpression tracingSearch() {
		return ConfiguredTLScriptFunctions.Module.INSTANCE.getImplementationInstance().getTracingExecutor(getName());
	}

}

