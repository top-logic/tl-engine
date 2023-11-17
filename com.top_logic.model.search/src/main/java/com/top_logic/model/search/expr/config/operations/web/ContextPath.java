/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.web;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.services.ServletContextService;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.util.TLModelUtil;

/**
 * Function retrieving the context path of the running application.
 */
public class ContextPath extends GenericMethod {

	/**
	 * Creates a {@link ContextPath}.
	 */
	protected ContextPath(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ContextPath(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.STRING_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		return ServletContextService.getInstance().getServletContext().getContextPath();
	}

	/**
	 * It may be that the {@link ServletContextService} is not yet started.
	 */
	@Override
	public boolean canEvaluateAtCompileTime(Object[] arguments) {
		return false;
	}

	/**
	 * {@link MethodBuilder} creating {@link ContextPath}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ContextPath> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ContextPath build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			checkNoArguments(expr, args);
			return new ContextPath(getConfig().getName(), args);
		}

	}

}
