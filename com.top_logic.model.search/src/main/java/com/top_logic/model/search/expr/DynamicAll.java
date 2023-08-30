/*
 * Copyright (c) 2013 Business Operation Systems GmbH. All Rights Reserved.
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
import com.top_logic.util.error.TopLogicException;

/**
 * {@link SearchExpression} looking up all instances of a type given as argument.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DynamicAll extends GenericMethod implements WithFlatMapSemantics<Void> {

	/**
	 * Creates a {@link DynamicAll}.
	 */
	public DynamicAll(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new DynamicAll(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object self, Object[] arguments, EvalContext definitions) {
		if (self == null) {
			return Collections.emptyList();
		}

		return evalPotentialFlatMap(definitions, self, null);
	}

	@Override
	public Object evalDirect(EvalContext definitions, Object singletonValue, Void param) {
		if (!(singletonValue instanceof TLStructuredType)) {
			throw new TopLogicException(I18NConstants.ERROR_NOT_A_TYPE__VAL_EXPR.fill(singletonValue, getSelf()));
		}

		return All.all(this, (TLStructuredType) singletonValue);
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
		public DynamicAll build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			checkNoArguments(expr, args);
			return new DynamicAll(getConfig().getName(), self, args);
		}
	}

}