/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link GenericMethod} that applies a function to the entire result of a previous expression.
 * 
 * <p>
 * The <code>with()</code> function takes the result from the previous expression in the chain and
 * passes it completely to a given function. This allows the function to access and use the complete
 * result multiple times, without having to break the chain structure.
 * </p>
 * 
 * <p>
 * Unlike <code>map()</code> or <code>foreach()</code> which apply a function to each element of a
 * collection, <code>with()</code> applies the function to the entire result. The result type is
 * whatever the inner function returns.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 * $projects
 * .get('...#milestones')
 * .filter(m -> $m.get('...#open'))
 * .with(openMilestones -> ... $openMilestones ... $openMilestones)
 * </pre>
 * 
 * @author <a href="mailto:jhu@top-logic.com">jhu</a>
 */
public class With extends GenericMethod {

	/**
	 * Creates a {@link With}.
	 */
	protected With(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new With(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.OBJECT_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object inputValue = arguments[0];
		SearchExpression function = asSearchExpression(arguments[1]);
		
		return function.eval(definitions, inputValue);
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	/**
	 * {@link MethodBuilder} creating {@link With} expressions.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<With> {

		/**
		 * Argument descriptor for the with() function:
		 * - mandatory "input": The input value from the previous expression
		 * - mandatory "function": The function to apply to the input
		 */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("input")
			.mandatory("func")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

		@Override
		public With build(Expr expr, SearchExpression[] args) {
			return new With(getConfig().getName(), args);
		}

	}

}