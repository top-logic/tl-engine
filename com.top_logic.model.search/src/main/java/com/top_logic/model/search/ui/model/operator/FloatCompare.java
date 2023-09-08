/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.operator;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.CalledByReflection;
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
 * Floating point comparison.
 * 
 * {@link Operator} for comparing a floating-point-number with another {@link #getComparisonValue()}
 * .
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(ResourceDisplay.class)
public interface FloatCompare
		extends AbstractFloatCompare<FloatCompare.Impl>, PrimitiveValueCompare<FloatCompare.Impl> {

	/**
	 * Kind of a {@link FloatCompare}.
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
		 * For a match, the compared values must not be equal.
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
	 * {@link Operator.Impl} for {@link FloatCompare}.
	 */
	class Impl extends AbstractFloatCompare.Impl<FloatCompare> {

		/**
		 * Creates a {@link FloatCompare.Impl} from configuration.
		 */
		@CalledByReflection
		public Impl(InstantiationContext context, FloatCompare config) {
			super(context, config);
		}

		@Override
		public SearchExpression build(ExpressionBuilder builder, SearchExpression contextExpr) {
			SearchExpression rightExpression =
				buildExpression(builder, getConfig().getComparisonValue());
			SearchExpression precision = literal(getConfig().getPrecision());
			switch (getConfig().getKind()) {
				case EQUALS: {
					return SearchExpressions.isFloatEqual(contextExpr, rightExpression, precision);
				}
				case NOT_EQUALS: {
					return not(SearchExpressions.isFloatEqual(contextExpr, rightExpression, precision));
				}
				case GE: {
					return SearchExpressions.compareFloat(CompareKind.GE, contextExpr, rightExpression, precision);
				}
				case GT: {
					return SearchExpressions.compareFloat(CompareKind.GT, contextExpr, rightExpression, precision);
				}
				case LE: {
					return SearchExpressions.compareFloat(CompareKind.LE, contextExpr, rightExpression, precision);
				}
				case LT: {
					return SearchExpressions.compareFloat(CompareKind.LT, contextExpr, rightExpression, precision);
				}
			}
			throw new UnreachableAssertion("Unhandled kind: " + getConfig().getKind());
		}

	}
}
