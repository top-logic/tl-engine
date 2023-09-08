/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.operator;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.template.util.ResourceDisplay;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.model.search.expr.CompareKind;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressions;
import com.top_logic.model.search.ui.model.exec.ExpressionBuilder;

/**
 * {@link Operator} for comparing {@link Comparable} values with another
 * {@link #getComparisonValue()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(ResourceDisplay.class)
public interface ComparableCompare extends PrimitiveValueCompare<ComparableCompare.Impl> {

	/**
	 * Property name of {@link #getKind()}.
	 */
	String KIND = "kind";

	/**
	 * Kind of comparison.
	 */
	@Name(KIND)
	PrimitiveCompareKind getKind();

	/**
	 * {@link Operator.Impl} for {@link ComparableCompare}.
	 */
	class Impl extends PrimitiveValueCompare.Impl<ComparableCompare> {

		/**
		 * Creates a {@link Impl}.
		 */
		public Impl(InstantiationContext context, ComparableCompare config) {
			super(context, config);
		}

		@Override
		public SearchExpression build(ExpressionBuilder builder, SearchExpression contextExpr) {
			ComparableCompare comparison = getConfig();
			SearchExpression rightExpression = buildExpression(builder, comparison.getComparisonValue());
			switch (comparison.getKind()) {
				case EQUALS:
					return SearchExpressions.isEqual(contextExpr, rightExpression);
				case NOT_EQUALS:
					return not(SearchExpressions.isEqual(contextExpr, rightExpression));
				case GE:
					return SearchExpressions.compareOp(CompareKind.GE, contextExpr, rightExpression);
				case GT:
					return SearchExpressions.compareOp(CompareKind.GT, contextExpr, rightExpression);
				case LE:
					return SearchExpressions.compareOp(CompareKind.LE, contextExpr, rightExpression);
				case LT:
					return SearchExpressions.compareOp(CompareKind.LT, contextExpr, rightExpression);
			}
			throw new UnreachableAssertion("Unhandled kind: " + comparison.getKind());
		}
	}
}
