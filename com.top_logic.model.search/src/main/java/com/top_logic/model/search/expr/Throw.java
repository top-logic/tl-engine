/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.util.error.TopLogicException;

/**
 * Throw statement canceling an operation by providing a message to the UI.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Throw extends GenericMethod {

	/**
	 * Creates a {@link Throw}.
	 */
	protected Throw(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new Throw(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return selfType;
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		ResKey message = toResKey(arguments[0]);
		if (message != null) {
			TopLogicException problem = new ScriptAbort(message);
			problem.initSeverity(ErrorSeverity.WARNING);
			if (arguments.length > 1) {
				ResKey details = toResKey(arguments[1]);
				if (details != null) {
					problem.initDetails(details);
				}
			}
			throw problem;
		}
		return null;
	}

	static ResKey toResKey(Object result) {
		if (result == null) {
			return null;
		}
		if (result instanceof ResKey) {
			return (ResKey) result;
		} else {
			return ResKey.text(result.toString());
		}
	}

	/**
	 * {@link MethodBuilder} creating {@link Throw}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Throw> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public Throw build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			checkArgs(expr, args, 1, 2);
			return new Throw(getName(), self, args);
		}

	}
}
