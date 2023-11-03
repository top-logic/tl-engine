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
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.ui.model.exec.ExpressionBuilder;

/**
 * {@link Operator} checking whether a floating-point-number is within a certain range.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(ResourceDisplay.class)
public interface FloatRangeCompare
		extends AbstractFloatCompare<FloatRangeCompare.Impl>, PrimitiveRangeCompare<FloatRangeCompare.Impl> {

	/**
	 * Property name of {@link #getKind()}.
	 */
	String KIND = "kind";

	/**
	 * Kind of comparison.
	 */
	@Name(KIND)
	RangeCompareKind getKind();

	/**
	 * @see #getKind()
	 */
	void setKind(RangeCompareKind value);

	/**
	 * {@link Operator.Impl} for {@link FloatRangeCompare}.
	 */
	public class Impl extends AbstractFloatCompare.Impl<FloatRangeCompare> {

		/**
		 * Creates a {@link FloatRangeCompare.Impl} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public Impl(InstantiationContext context, FloatRangeCompare config) {
			super(context, config);
		}

		@Override
		public SearchExpression build(ExpressionBuilder builder, SearchExpression contextExpr) {
			FloatRangeCompare comparison = getConfig();
			SearchExpression lowerBound = buildExpression(builder, comparison.getLowerBound());
			SearchExpression upperBound = buildExpression(builder, comparison.getUpperBound());
			SearchExpression precision = literal(SearchExpression.toNumber(comparison.getPrecision()));
			switch (comparison.getKind()) {
				case IN_RANGE: {
					return not(isFloatInRange(contextExpr, lowerBound, upperBound, precision));
				}
				case NOT_IN_RANGE: {
					return isFloatInRange(contextExpr, lowerBound, upperBound, precision);
				}
			}
			throw new UnreachableAssertion("Unhandled kind: " + comparison.getKind());
		}

	}

}
