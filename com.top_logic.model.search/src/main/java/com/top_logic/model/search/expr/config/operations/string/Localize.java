/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.string;

import java.util.List;
import java.util.Locale;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.WithFlatMapSemantics;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * {@link GenericMethod} localizing a {@link ResKey} in a given language.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Localize extends GenericMethod implements WithFlatMapSemantics<Locale> {

	/**
	 * Creates a {@link Localize} expression.
	 */
	protected Localize(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new Localize(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.STRING_TYPE);
	}

	@Override
	protected Object eval(Object self, Object[] arguments, EvalContext definitions) {
		return evalPotentialFlatMap(definitions, self, asLocale(arguments[0]));
	}

	@Override
	public Object evalDirect(EvalContext definitions, Object base, Locale param) {
		ResKey key = asResKeyNotNull(base);

		return Resources.getInstance(param).getString(key);
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating an {@link Localize} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Localize> {

		/** Description of parameters for a {@link Localize}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.optional("lang", () -> SearchExpressionFactory.literal(TLContext.getLocale()))
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
		public Localize build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			return new Localize(getConfig().getName(), self, args);
		}
	}

}
