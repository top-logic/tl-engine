/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search.expr;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

/**
 * {@link GenericMethodWithSecurity} that secures a value by applying the current user's Discover
 * right, removing all business objects the user must not discover.
 *
 * <p>
 * Every object source in a secured TL-Script delivers only discoverable objects (see the Discover
 * specification); this function remains as a safety net for values assembled from unsecured
 * sources. It applies {@link SearchExpression#filterSecurity(Object)} to its argument and thereby
 * removes the objects the user must not discover
 * (recursively: a single forbidden object becomes <code>null</code>, forbidden elements are dropped
 * from collections, primitive values are kept).
 * </p>
 *
 * <p>
 * The <em>final result</em> of a script does not need this expression: it is secured by the
 * {@link com.top_logic.model.search.expr.query.QueryExecutor} that executes the script, see
 * {@link com.top_logic.model.search.expr.query.QueryExecutor#executeWith(EvalContext, com.top_logic.model.search.expr.query.Args)}.
 * This expression secures a value <em>within</em> a script, e.g. a partial result that is rendered
 * or handed to another context during the evaluation. It must not be applied to an intermediate
 * result that the script still computes with, since that could drop objects the computation needs
 * (e.g. for a filter) even though the final result would be discoverable.
 * </p>
 *
 * <p>
 * Available as the TL-Script function <code>filterSecurity</code>.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FilterSecurity extends GenericMethodWithSecurity {

	/**
	 * Creates a new {@link FilterSecurity}.
	 */
	protected FilterSecurity(String name, SearchExpression[] arguments, boolean usesSecurity) {
		super(name, arguments, usesSecurity);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new FilterSecurity(getName(), arguments, usesSecurity());
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return argumentTypes.get(0);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object base = arguments[0];
		if (usesSecurity()) {
			return filterSecurity(base);
		} else {
			return base;
		}
	}

	/**
	 * {@link MethodBuilder} creating {@link FilterSecurity}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<FilterSecurity> {

		/** Description of parameters for a {@link FilterSecurity}. */
		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("base")
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
		public FilterSecurity build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new FilterSecurity(getConfig().getName(), args, true);
		}

	}
}

