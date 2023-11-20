/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.NoArgMethodBuilder;

/**
 * {@link SearchExpression} creating a new object of a given {@link TLClass} type.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DeleteObject extends GenericMethod implements WithFlatMapSemantics<Void> {

	private static final SearchExpression[] NO_ARGS = {};

	/**
	 * Creates a {@link DeleteObject}.
	 *
	 * @param self
	 *        The expression evaluating to the type to instantiate (usually a model type literal).
	 */
	DeleteObject(String name, SearchExpression self) {
		super(name, self, NO_ARGS);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new DeleteObject(getName(), self);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return null;
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	@Override
	protected Object eval(Object self, Object[] arguments, EvalContext definitions) {
		return evalPotentialFlatMap(definitions, self, null);
	}

	@Override
	public Object evalFlatMap(EvalContext definitions, Collection<?> base, Void param) {
		PersistencyLayer.getKnowledgeBase().deleteAll(base.stream().filter(x -> x instanceof TLObject)
			.map(x -> ((TLObject) x).tHandle()).collect(Collectors.toList()));
		return null;
	}

	@Override
	public Object evalDirect(EvalContext definitions, Object singletonValue, Void param) {
		TLObject obj = asTLObject(singletonValue);
		if (obj != null) {
			obj.tDelete();
		}
		return null;
	}

	/**
	 * Builder creating a {@link DeleteObject} expression.
	 */
	public static class Builder extends NoArgMethodBuilder<DeleteObject> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		protected DeleteObject internalBuild(Expr expr, SearchExpression self)
				throws ConfigurationException {
			return new DeleteObject(getName(), self);
		}
	}

}
