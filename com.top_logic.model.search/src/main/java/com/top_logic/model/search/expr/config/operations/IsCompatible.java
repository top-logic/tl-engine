/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link SearchExpression} testing whether an object of the first {@link TLType} can be assigned to
 * a variable of the second {@link TLType}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class IsCompatible extends GenericMethod {

	/** Creates an {@link IsCompatible}. */
	IsCompatible(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public IsCompatible copy(SearchExpression[] arguments) {
		return new IsCompatible(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.BOOLEAN_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		TLType objectType = asType(arguments[0]);
		TLType variableType = asType(arguments[1]);
		return TLModelUtil.isCompatibleType(variableType, objectType);
	}

	/** {@link MethodBuilder} creating {@link IsCompatible}. */
	public static final class Builder extends AbstractSimpleMethodBuilder<IsCompatible> {

		/** Creates a {@link Builder}. */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public IsCompatible build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			checkTwoArgs(expr, args);
			return new IsCompatible(getConfig().getName(), args);
		}
	}
}
