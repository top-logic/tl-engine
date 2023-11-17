/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SimpleGenericMethod;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link GenericMethod} looking up the {@link TLObject#tContainer()} of an object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Container extends SimpleGenericMethod {

	/**
	 * Creates a {@link Container}.
	 */
	protected Container(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Container(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	public Object eval(Object[] arguments) {
		TLObject object = asTLObject(arguments[0]);
		if (object != null) {
			return object.tContainer();
		} else {
			return null;
		}
	}

	/**
	 * {@link Container} can not be evaluated at compile time, because the container of an object
	 * may have been changed.
	 */
	@Override
	public boolean canEvaluateAtCompileTime(Object[] arguments) {
		return arguments[0] == null;
	}

	/**
	 * {@link MethodBuilder} creating {@link Container}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Container> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public Container build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			checkSingleArg(expr, args);
			return new Container(getConfig().getName(), args);
		}

	}
}
