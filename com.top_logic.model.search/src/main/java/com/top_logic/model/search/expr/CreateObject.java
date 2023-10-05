/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.util.model.ModelService;

/**
 * {@link SearchExpression} creating a new object of a given {@link TLClass} type.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreateObject extends GenericMethod {

	private static final String METHOD_NAME = "new";

	/**
	 * Creates a {@link CreateObject}.
	 *
	 * @param self
	 *        The expression evaluating to the type to instantiate (usually a model type literal).
	 * @param args
	 *        The optional create context (at most a single argument).
	 */
	CreateObject(SearchExpression self, SearchExpression[] args) {
		super(METHOD_NAME, self, args);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new CreateObject(self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		SearchExpression self = getSelf();
		if (self instanceof Literal) {
			return (TLClass) ((Literal) self).getValue();
		} else {
			// No type can be determined without evaluating the self expression.
			return null;
		}
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	@Override
	protected Object eval(Object self, Object[] arguments, EvalContext definitions) {
		TLClass type = (TLClass) asStructuredTypeNonNull(self, getSelf());
		TLObject context = asTLObject(arguments.length > 0 ? arguments[0] : null);
		return ModelService.getInstance().getFactory().createObject(type, context, null);
	}

	/**
	 * Builder creating a {@link CreateObject} expression.
	 */
	public static class Builder extends AbstractSimpleMethodBuilder<CreateObject> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public CreateObject build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			checkMaxArgs(expr, args, 1);
			return new CreateObject(self, args);
		}
	}

}
