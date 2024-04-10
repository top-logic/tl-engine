/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test for type compatibility with a variable type computed at runtime.
 * 
 * @see InstanceOf
 */
public class DynamicInstanceOf extends GenericMethod {

	/**
	 * Creates a {@link DynamicInstanceOf}.
	 */
	public DynamicInstanceOf(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new DynamicInstanceOf(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.BOOLEAN_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object input = arguments[0];
		SearchExpression inputExpr = getArguments()[0];
		TLStructuredType expectedType = asStructuredTypeNonNull(arguments[1], getArguments()[1]);

		return InstanceOf.isInstanceOf(input, inputExpr, expectedType);
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating {@link DynamicInstanceOf}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<DynamicInstanceOf> {

		/**
		 * Configuration for the {@link Builder}.
		 */
		public interface Config extends AbstractSimpleMethodBuilder.Config<Builder> {

			@StringDefault("dynamicInstanceOf")
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
		public DynamicInstanceOf build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			checkTwoArgs(expr, args);
			return new DynamicInstanceOf(getConfig().getName(), args);
		}

	}

}
