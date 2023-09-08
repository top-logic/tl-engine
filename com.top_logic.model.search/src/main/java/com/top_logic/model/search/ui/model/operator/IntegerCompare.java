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
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressions;
import com.top_logic.model.search.ui.model.exec.ExpressionBuilder;

/**
 * {@link Operator} for comparing integer values with another {@link #getComparisonValue()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(ResourceDisplay.class)
public interface IntegerCompare
		extends AbstractIntegerCompare<IntegerCompare.Impl>, PrimitiveValueCompare<IntegerCompare.Impl> {

	/**
	 * Kind of an {@link IntegerCompare integer comparison}.
	 */
	enum Kind {
		/**
		 * For a match, both values must be equal.
		 */
		EQUALS,

		/**
		 * For a match, the left value must be greater or equal to the right value.
		 */
		GE,

		/**
		 * For a match, the left value must be greater than the right value.
		 */
		GT,

		/**
		 * For a match, the left value must be lower or equal to the right value.
		 */
		LE,

		/**
		 * For a match, the left value must be lower than the right value.
		 */
		LT,

		/**
		 * For a match, both values must be different.
		 */
		NOT_EQUALS,
	}

	/**
	 * Property name of {@link #getKind()}.
	 */
	String KIND = "kind";

	/**
	 * Kind of comparison.
	 */
	@Name(KIND)
	Kind getKind();

	/**
	 * @see #getKind()
	 */
	void setKind(Kind value);

	/**
	 * {@link Operator.Impl} for {@link IntegerCompare}.
	 */
	class Impl extends AbstractIntegerCompare.Impl<IntegerCompare> {

		/**
		 * Creates a {@link Impl}.
		 */
		public Impl(InstantiationContext context, IntegerCompare config) {
			super(context, config);
		}

		@Override
		public SearchExpression build(ExpressionBuilder builder, SearchExpression contextExpr) {
			IntegerCompare comparison = getConfig();
			SearchExpression rightExpression =
				buildExpression(builder, comparison.getComparisonValue());
			switch (comparison.getKind()) {
				case EQUALS: {
					return isEqual(contextExpr, rightExpression);
				}
				case NOT_EQUALS: {
					return not(isEqual(contextExpr, rightExpression));
				}
				case GE: {
					return or(
						SearchExpressions.isGreater(contextExpr, rightExpression),
						isEqual(contextExpr, rightExpression));
				}
				case GT: {
					return SearchExpressions.isGreater(contextExpr, rightExpression);
				}
				case LE: {
					return not(SearchExpressions.isGreater(contextExpr, rightExpression));
				}
				case LT: {
					return not(or(
						SearchExpressions.isGreater(contextExpr, rightExpression),
						isEqual(contextExpr, rightExpression)));
				}
			}
			throw new UnreachableAssertion("Unhandled kind: " + comparison.getKind());
		}

	}

}
