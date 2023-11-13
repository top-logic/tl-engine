/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
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
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

/**
 * {@link GenericMethod} forcing current thread to sleep for a while.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Sleep extends GenericMethod {

	/**
	 * Creates a {@link Sleep}.
	 */
	protected Sleep(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Sleep(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		long sleepTime = asLong(arguments[0]);
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException ex) {
			// Ignore
		}
		return null;
	}

	/**
	 * The side effect of this method is, that the control flow is interrupted. It is undesirable
	 * that the compiler moves this function to another place.
	 * 
	 * @see com.top_logic.model.search.expr.GenericMethod#isSideEffectFree()
	 */
	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	/**
	 * {@link MethodBuilder} creating {@link Sleep}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Sleep> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public Sleep build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			checkSingleArg(expr, args);
			return new Sleep(getConfig().getName(), args);
		}

	}
}
