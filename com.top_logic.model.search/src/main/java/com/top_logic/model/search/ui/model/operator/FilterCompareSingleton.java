/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.operator;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.template.util.ResourceDisplay;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.ui.model.exec.ExpressionBuilder;
import com.top_logic.model.search.ui.model.structure.SearchFilter;

/**
 * {@link Operator} that matches by applying {@link SearchFilter}s to attribute values and
 * thereby inspecting attributes of attribute values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(ResourceDisplay.class)
public interface FilterCompareSingleton extends AbstractFilterCompare<FilterCompareSingleton.Impl> {

	/**
	 * {@link Operator.Impl} for {@link FilterCompareSingleton}.
	 */
	class Impl extends AbstractFilterCompare.Impl<FilterCompareSingleton> {

		/**
		 * Creates a {@link Impl}.
		 */
		public Impl(InstantiationContext context, FilterCompareSingleton config) {
			super(context, config);
		}

		@Override
		public SearchExpression build(ExpressionBuilder builder, SearchExpression contextExpr) {
			return call(builder.filterFunction(getConfig()), contextExpr);
		}

	}
}
