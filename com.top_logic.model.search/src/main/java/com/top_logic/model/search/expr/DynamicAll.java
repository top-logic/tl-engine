/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;

/**
 * {@link SearchExpression} looking up all instances of a type given as argument.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DynamicAll extends GenericMethod implements WithFlatMapSemantics<Void> {

	/**
	 * Creates a {@link DynamicAll}.
	 */
	public DynamicAll(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new DynamicAll(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		if (arguments[0] == null) {
			return Collections.emptyList();
		}

		return evalPotentialFlatMap(definitions, arguments[0], null);
	}

	@Override
	public Object evalDirect(EvalContext definitions, Object singletonValue, Void param) {
		TLStructuredType type = asStructuredTypeNonNull(singletonValue, getArguments()[0]);

		return All.all(this, type);
	}

	@Override
	public boolean isSideEffectFree() {
		// Not really a side-effect, but access to global state, therefore, must not do constant
		// folding.
		return false;
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating {@link DynamicAll}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<DynamicAll> {

		/**
		 * Configuration for the {@link Builder}.
		 */
		public interface Config extends AbstractSimpleMethodBuilder.Config<Builder> {

			@StringDefault("dynamicAll")
			@Override
			String getName();

		}

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public DynamicAll build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			checkSingleArg(expr, args);
			return new DynamicAll(getConfig().getName(), args);
		}

	}

}