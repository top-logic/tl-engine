/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link GenericMethod} checking two {@link TLObject} for unversioned equality.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class EqualsUnversioned extends GenericMethod {

	/**
	 * Creates a {@link EqualsUnversioned}.
	 */
	protected EqualsUnversioned(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new EqualsUnversioned(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.BOOLEAN_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		TLObject first = asTLObject(arguments[0]);
		TLObject second = asTLObject(arguments[1]);
		return WrapperHistoryUtils.equalsUnversioned(first, second);
	}

	/**
	 * {@link MethodBuilder} creating {@link EqualsUnversioned}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<EqualsUnversioned> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public EqualsUnversioned build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			checkTwoArgs(expr, args);
			return new EqualsUnversioned(getConfig().getName(), args);
		}

	}
}
