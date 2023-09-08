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
import com.top_logic.model.search.ui.model.exec.ExpressionBuilder;

/**
 * String comparison.
 * 
 * {@link Operator} for comparing string values with another {@link #getComparisonValue()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(ResourceDisplay.class)
public interface StringCompare
		extends AbstractStringCompare<StringCompare.Impl>, PrimitiveValueCompare<StringCompare.Impl> {

	/**
	 * Kind of {@link StringCompare} comparison.
	 */
	enum Kind {
		/**
		 * For a match, the left string must contain the right one as substring.
		 */
		CONTAINS,

		/**
		 * For a match, both strings must be equal.
		 */
		EQUALS,

		/**
		 * For a match, the left string must have the right one as prefix.
		 */
		STARTS_WITH,

		/**
		 * For a match, the left string must have the right one as suffix.
		 */
		ENDS_WITH,

		/**
		 * For a match, the left string must be greater or equal to the right one in alphabetical
		 * order.
		 */
		GE,

		/**
		 * For a match, the left string must be greater than the right one in alphabetical order.
		 */
		GT,

		/**
		 * For a match, the left string must be lower or equal to the right one in alphabetical
		 * order.
		 */
		LE,

		/**
		 * For a match, the left string must be lower than the right one in alphabetical order.
		 */
		LT,

		/**
		 * For a match, the left string must not contain the right one as subsequence.
		 */
		NOT_CONTAINS,

		/**
		 * For a match, both strings must be different.
		 */
		NOT_EQUALS,

		/**
		 * For a match, the left string must <b>not</b> have the right one as prefix.
		 */
		NOT_STARTS_WITH,

		/**
		 * For a match, the left string must <b>not</b> have the right one as suffix.
		 */
		NOT_ENDS_WITH
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
	 * {@link Operator.Impl} for {@link StringCompare}.
	 */
	class Impl extends AbstractStringCompare.Impl<StringCompare> {

		/**
		 * Creates a {@link Impl}.
		 */
		public Impl(InstantiationContext context, StringCompare config) {
			super(context, config);
		}

		@Override
		public SearchExpression build(ExpressionBuilder builder, SearchExpression contextExpr) {
			StringCompare comparison = getConfig();
			SearchExpression rightExpression = buildExpression(builder, comparison.getComparisonValue());
			boolean isCaseSensitive = comparison.isCaseSensitive();
			switch (comparison.getKind()) {
				case EQUALS: {
					return isStringEqual(contextExpr, rightExpression, isCaseSensitive);
				}
				case NOT_EQUALS: {
					return not(isStringEqual(contextExpr, rightExpression, isCaseSensitive));
				}
				case GE: {
					return or(
						isStringGreater(contextExpr, rightExpression, isCaseSensitive),
						isStringEqual(contextExpr, rightExpression, isCaseSensitive));
				}
				case GT: {
					return isStringGreater(contextExpr, rightExpression, isCaseSensitive);
				}
				case LE: {
					return not(isStringGreater(contextExpr, rightExpression, isCaseSensitive));
				}
				case LT: {
					return not(or(
						isStringGreater(contextExpr, rightExpression, isCaseSensitive),
						isStringEqual(contextExpr, rightExpression, isCaseSensitive)));
				}
				case CONTAINS: {
					return stringContains(contextExpr, rightExpression, isCaseSensitive);
				}
				case NOT_CONTAINS: {
					return not(stringContains(contextExpr, rightExpression, isCaseSensitive));
				}
				case STARTS_WITH: {
					return stringStartsWith(contextExpr, rightExpression, isCaseSensitive);
				}
				case NOT_STARTS_WITH: {
					return not(stringStartsWith(contextExpr, rightExpression, isCaseSensitive));
				}
				case ENDS_WITH: {
					return stringEndsWith(contextExpr, rightExpression, isCaseSensitive);
				}
				case NOT_ENDS_WITH: {
					return not(stringEndsWith(contextExpr, rightExpression, isCaseSensitive));
				}
			}
			throw new UnreachableAssertion("Unhandled comparison kind: " + comparison.getKind());
		}

	}
}
