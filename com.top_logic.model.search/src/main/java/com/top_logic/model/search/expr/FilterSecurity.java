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
 * {@link GenericMethodWithSecurity} that secures a value by applying the current user's read
 * permissions, removing all business objects the user must not see.
 *
 * <p>
 * Access operations in TL-Script return referenced objects <em>unfiltered</em> (consistent with the
 * user interface, which always shows a referenced object by its label and only secures navigation
 * into it); only the access to the <em>attributes</em> of an object is denied if the user must not
 * read the object. As a consequence, the result of a search expression is not secured
 * automatically. This expression applies {@link SearchExpression#filterSecurity(Object)} to its
 * argument and thereby removes the objects that the current user is not allowed to read (recursively:
 * a single forbidden object becomes <code>null</code>, forbidden elements are dropped from
 * collections, primitive values are kept).
 * </p>
 *
 * <p>
 * It is intended to secure the <em>final result</em> of a search expression and should therefore be
 * applied at the top-level entry points that evaluate a script whose result is handed to a user (so
 * that only readable objects are returned). It must not be applied to intermediate results, since
 * that could drop objects still needed for the computation (e.g. for a filter) even though the final
 * result would be readable.
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

	/**
	 * Wraps a {@link FilterSecurity} around the given base expression, if it is not already a
	 * {@link FilterSecurity} instance.
	 * 
	 * @param base
	 *        The base expression whose result must be filtered for security.
	 * 
	 * @return either the given base expression or a {@link FilterSecurity} with the given
	 *         {@link SearchExpression} as base.
	 */
	public static SearchExpression ensureOnlyAllowedResults(SearchExpression base) {
		if (base instanceof FilterSecurity) {
			// Already secured.
			return base;
		}
		return new FilterSecurity("filterSecurity", new SearchExpression[] { base }, true);
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

