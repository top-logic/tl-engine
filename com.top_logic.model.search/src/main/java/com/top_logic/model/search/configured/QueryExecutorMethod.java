/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.configured;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link GenericMethod} delegating execution to a given {@link QueryExecutor}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class QueryExecutorMethod extends GenericMethod {

	private final Supplier<QueryExecutor> _executor;

	private Function<List<TLType>, TLType> _typeComputation = argumentTypes -> null;

	
	/**
	 * Creates a {@link QueryExecutorMethod}.
	 *
	 * @param executor
	 *        The {@link QueryExecutor} that is executed during evaluation.
	 * @param name
	 *        See {@link #getName()}.
	 * @param arguments
	 *        See {@link #getArguments()}.
	 */
	QueryExecutorMethod(Supplier<QueryExecutor> executor, String name, SearchExpression[] arguments) {
		super(name, arguments);
		_executor = executor;
	}

	/**
	 * {@link Function} computing the return type of this method from the {@link #getArguments()
	 * argument types}.
	 * 
	 * @see #getType(List)
	 */
	public Function<List<TLType>, TLType> getTypeComputation() {
		return _typeComputation;
	}

	/**
	 * Setter for {@link #getTypeComputation()}.
	 */
	public void setTypeComputation(Function<List<TLType>, TLType> typeComputation) {
		_typeComputation = Objects.requireNonNull(typeComputation);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		QueryExecutorMethod configuredMethod = new QueryExecutorMethod(_executor, getName(), arguments);
		configuredMethod.setTypeComputation(getTypeComputation());
		return configuredMethod;
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return getTypeComputation().apply(argumentTypes);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		return getExecutor().execute(arguments);
	}

	/**
	 * The {@link QueryExecutor} to delegate evaluation to.
	 */
	public QueryExecutor getExecutor() {
		return _executor.get();
	}

	@Override
	public Object getId() {
		return getName();
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	/**
	 * The query on which this method bases may change, therefore this method must not be evaluated
	 * at compile time.
	 */
	@Override
	public boolean canEvaluateAtCompileTime(Object[] arguments) {
		return false;
	}

}
