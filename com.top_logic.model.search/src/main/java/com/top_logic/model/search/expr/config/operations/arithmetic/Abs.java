/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.arithmetic;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.util.TLModelUtil;

/**
 * Function computing {@link Math#abs(double)}.
 *
 * @author<a href="mailto:jhu@top-logic.com">Jonathan Hüsing</a>
 */
public class Abs extends GenericMethod {

	/**
	 * Creates a {@link Abs}.
	 */
	protected Abs(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Abs(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.DOUBLE_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		return Math.abs(asDouble(arguments[0]));
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating an {@link Abs} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Abs> {

		/** Description of parameters for a {@link Abs}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("value")
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
		public Abs build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new Abs(getConfig().getName(), args);
		}

	}

}