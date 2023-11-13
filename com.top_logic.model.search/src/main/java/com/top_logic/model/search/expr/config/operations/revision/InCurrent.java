/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.revision;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.WithFlatMapSemantics;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;

/**
 * Generic method transforming a given {@link TLObject} to current revision.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class InCurrent extends GenericMethod implements WithFlatMapSemantics<Void> {

	/**
	 * Creates a new {@link InCurrent}.
	 */
	protected InCurrent(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new InCurrent(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return argumentTypes.get(0);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		return evalPotentialFlatMap(definitions, arguments[0], null);
	}

	@Override
	public Object evalDirect(EvalContext definitions, Object singletonValue, Void param) {
		TLObject tlObject = asTLObject(singletonValue);
		if (tlObject == null) {
			return null;
		}
		return WrapperHistoryUtils.getCurrent(tlObject);
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating {@link InCurrent}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<InCurrent> {

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public InCurrent build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			checkSingleArg(expr, args);
			return new InCurrent(getConfig().getName(), args);
		}

	}

}

