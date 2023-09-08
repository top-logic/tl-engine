/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.operator;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.template.util.ResourceDisplay;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.ui.model.FilterContainer;
import com.top_logic.model.search.ui.model.exec.ExpressionBuilder;
import com.top_logic.model.search.ui.model.structure.SearchFilter;

/**
 * Form input model for explicit cast expressions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(ResourceDisplay.class)
public interface TypeCheck extends Operator<TypeCheck.Impl>, TypeCheckBased, SearchFilter, FilterContainer {

	/**
	 * {@link Operator.Impl} for {@link TypeCheck}.
	 */
	class Impl extends Operator.Impl<TypeCheck> {

		/**
		 * Creates a {@link Impl}.
		 */
		public Impl(InstantiationContext context, TypeCheck config) {
			super(context, config);
		}

		@Override
		public SearchExpression build(ExpressionBuilder builder, SearchExpression contextExpr) {
			return builder.toTypeCheckExpression(getConfig(), contextExpr);
		}

	}
}
