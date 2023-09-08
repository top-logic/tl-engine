/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.operator;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressions;
import com.top_logic.model.search.ui.model.AttributeFilter;
import com.top_logic.model.search.ui.model.FilterContainer;
import com.top_logic.model.search.ui.model.exec.ExpressionBuilder;
import com.top_logic.model.search.ui.model.structure.RightHandSide;
import com.top_logic.model.search.ui.model.structure.RightHandSideVisitor;
import com.top_logic.model.search.ui.model.structure.SearchPart;

/**
 * Base model for {@link SearchPart}s describing boolean operators on object properties.
 * 
 * @see AttributeFilter#getComparisons()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface Operator<I extends Operator.Impl<?>> extends SearchPart, PolymorphicConfiguration<I> {

	@Override
	@Hidden
	Class<? extends I> getImplementationClass();

	/**
	 * Algorithm constructing {@link SearchExpression}s for its {@link #getConfig() operator
	 * configuration}.
	 */
	abstract class Impl<O extends Operator<?>> extends AbstractConfiguredInstance<O> {

		/**
		 * Creates a {@link Operator.Impl} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public Impl(InstantiationContext context, O config) {
			super(context, config);
		}

		/**
		 * Actually creates the {@link SearchExpression} from the {@link #getConfig()} representing
		 * the UI of the {@link Operator} expression.
		 *
		 * @param builder
		 *        Builder for common parts.
		 * @param contextExpr
		 *        The left hand side to operate on.
		 * @return The resulting operation.
		 */
		public abstract SearchExpression build(ExpressionBuilder builder, SearchExpression contextExpr);

		private RightHandSideVisitor<SearchExpression, Void> getTransformValue(ExpressionBuilder builder) {
			return builder.getRightHandSideBuilder();
		}

		protected final SearchExpression buildExpression(ExpressionBuilder builder, RightHandSide value) {
			return value.visitRightHandSide(getTransformValue(builder), null);
		}

		protected final SearchExpression filterFunction(ExpressionBuilder builder, FilterContainer filterContainer) {
			return builder.filterFunction(filterContainer);
		}

		/**
		 * Utility for creating a {@link SearchExpression} that tests whether the given general
		 * expression is within the given bounds.
		 *
		 * @param contextExpression
		 *        Expression evaluating to the value to check.
		 * @param lowerBound
		 *        Expression evaluating to the lower bound.
		 * @param upperBound
		 *        Expression evaluating to the upper bound.
		 * @return The testing expression.
		 * 
		 * @see #isDateInRange(SearchExpression, SearchExpression, SearchExpression)
		 * @see #isFloatInRange(SearchExpression, SearchExpression, SearchExpression,
		 *      SearchExpression)
		 * @see #isStringInRange(SearchExpression, SearchExpression, SearchExpression,
		 *      SearchExpression)
		 */
		protected final SearchExpression isInRange(SearchExpression contextExpression, SearchExpression lowerBound,
				SearchExpression upperBound) {
			return or(
				SearchExpressions.isGreater(contextExpression, upperBound),
				SearchExpressions.isGreater(lowerBound, contextExpression));
		}

		/**
		 * Utility for creating a {@link SearchExpression} that tests whether the given float-valued
		 * expression is within the given bounds.
		 *
		 * @param contextExpression
		 *        Expression evaluating to the value to check.
		 * @param lowerBound
		 *        Expression evaluating to the lower bound.
		 * @param upperBound
		 *        Expression evaluating to the upper bound.
		 * @return The testing expression.
		 */
		protected final SearchExpression isFloatInRange(SearchExpression contextExpression, SearchExpression lowerBound,
				SearchExpression upperBound, SearchExpression precision) {
			return or(
				SearchExpressions.isFloatGreater(contextExpression, upperBound, precision),
				SearchExpressions.isFloatGreater(lowerBound, contextExpression, precision));
		}

		/**
		 * Utility for creating a {@link SearchExpression} that tests whether the given date-valued
		 * expression is within the given bounds.
		 *
		 * @param contextExpression
		 *        Expression evaluating to the value to check.
		 * @param lowerBound
		 *        Expression evaluating to the lower bound.
		 * @param upperBound
		 *        Expression evaluating to the upper bound.
		 * @return The testing expression.
		 */
		protected final SearchExpression isDateInRange(SearchExpression contextExpression, SearchExpression lowerBound,
				SearchExpression upperBound) {
			return or(
				SearchExpressions.isDateGreater(contextExpression, upperBound),
				SearchExpressions.isDateGreater(lowerBound, contextExpression));
		}

		/**
		 * Utility for creating a {@link SearchExpression} that tests whether the given expression
		 * is within the given bounds.
		 *
		 * @param contextExpression
		 *        Expression evaluating to the value to check.
		 * @param lowerBound
		 *        Expression evaluating to the lower bound.
		 * @param upperBound
		 *        Expression evaluating to the upper bound.
		 * @param isCaseSensitive
		 *        Expression evaluating to a {@link Boolean} specifying whether the comparision
		 *        should be case-sensitive.
		 * @return The testing expression.
		 */
		protected final SearchExpression isStringInRange(SearchExpression contextExpression,
				SearchExpression lowerBound,
				SearchExpression upperBound, SearchExpression isCaseSensitive) {
			return ifElse(isCaseSensitive,
				or(
					isStringGreater(contextExpression, upperBound, true),
					isStringGreater(lowerBound, contextExpression, true)),
				or(
					isStringGreater(contextExpression, upperBound, false),
					isStringGreater(lowerBound, contextExpression, false)));
		}

	}

}
