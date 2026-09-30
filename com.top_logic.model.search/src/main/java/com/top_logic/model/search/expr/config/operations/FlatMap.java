/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.Collection;
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
 * {@link GenericMethod} that applies a function to each element of a collection and flattens the
 * results.
 * 
 * <p>
 * The <code>flatMap()</code> function takes a collection and applies a function to each element. If
 * the function returns a collection for an element, all elements of that collection are added to
 * the result. If the function returns a single value, that value is added directly to the result.
 * </p>
 * 
 * <p>
 * This is equivalent to <code>collection.map($func).flatten()</code> but more efficient as it
 * avoids creating intermediate collections.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 * $projects
 * .get('...#milestones')
 * .flatMap(m -> $m.get('...#tasks'))
 * </pre>
 * 
 * @author <a href="mailto:jhu@top-logic.com">jhu</a>
 */
public class FlatMap extends GenericMethod {

	/**
	 * Creates a {@link FlatMap}.
	 */
	protected FlatMap(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new FlatMap(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.OBJECT_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Collection<?> inputCollection = asCollection(arguments[0]);
		SearchExpression function = asSearchExpression(arguments[1]);
		
		List<Object> result = new java.util.ArrayList<>();
		
		// Apply the function to each element and flatten the results
		for (Object element : inputCollection) {
			Object functionResult = function.eval(definitions, element);
			result.addAll(asCollection(functionResult));
		}
		
		return result;
	}

	/**
	 * {@link MethodBuilder} creating {@link FlatMap} expressions.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<FlatMap> {

		/**
		 * Argument descriptor for the <code>flatMap()</code> function: - mandatory "collection":
		 * The input collection to process - mandatory "func": The function to apply to each element
		 */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("collection")
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
		public FlatMap build(Expr expr, SearchExpression[] args) {
			return new FlatMap(getConfig().getName(), args);
		}

	}

}