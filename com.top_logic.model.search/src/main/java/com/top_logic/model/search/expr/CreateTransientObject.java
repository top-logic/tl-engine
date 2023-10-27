/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;

/**
 * {@link SearchExpression} creating a new transient object of a given {@link TLClass} type.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreateTransientObject extends AbstractObjectCreation {

	/**
	 * Creates a {@link CreateTransientObject}.
	 *
	 * @param self
	 *        The expression evaluating to the type to instantiate (usually a model type literal).
	 * @param args
	 *        The optional create context (at most a single argument).
	 */
	CreateTransientObject(String name, SearchExpression self, SearchExpression[] args) {
		super(name, self, args);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new CreateTransientObject(getName(), self, arguments);
	}

	@Override
	protected Object eval(Object self, Object[] arguments, EvalContext definitions) {
		TLClass type = (TLClass) asStructuredTypeNonNull(self, getSelf());
		TLObject context = asTLObject(arguments.length > 0 ? arguments[0] : null);
		return TransientObjectFactory.INSTANCE.createObject(type, context);
	}

	/**
	 * Builder creating a {@link CreateTransientObject} expression.
	 */
	public static class Builder extends AbstractSimpleMethodBuilder<CreateTransientObject> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public CreateTransientObject build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			checkMaxArgs(expr, args, 1);
			return new CreateTransientObject(getName(), self, args);
		}
	}

}
