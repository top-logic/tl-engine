/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import static com.top_logic.model.search.expr.Throw.*;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

/**
 * Reports an info message bubble to the UI.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Info extends GenericMethod {

	/**
	 * Creates a {@link Info}.
	 */
	protected Info(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Info(getName(), arguments);
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
			ResKey details;
			if (arguments.length > 1) {
				details = toResKey(arguments[1]);
			} else {
				details = ResKey.NONE;
			}
			InfoService.showInfo(message, details);
		}
		return null;
	}

	/**
	 * {@link MethodBuilder} creating {@link Info}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Info> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public Info build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			checkArgs(expr, args, 1, 2);
			return new Info(getName(), args);
		}

	}
}
