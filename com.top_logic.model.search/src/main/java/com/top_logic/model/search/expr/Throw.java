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
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
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
	protected Throw(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Throw(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return argumentTypes.get(0);
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		ResKey message = toResKey(arguments[0]);
		if (message != null) {
			ResKey details = arguments.length > 1 ? toResKey(arguments[1]) : null;
			Object value = arguments.length > 2 ? arguments[2] : null;

			TopLogicException problem = new ScriptAbort(message, value);
			problem.initSeverity(ErrorSeverity.WARNING);
			if (details != null) {
				problem.initDetails(details);
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
	 * {@link AbstractSimpleMethodBuilder} creating a {@link Throw} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Throw> {

		/** Description of parameters for a {@link Throw}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("message")
			.optional("details")
			.optional("value")
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
		public Throw build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new Throw(getConfig().getName(), args);
		}
	}
}