/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import static com.top_logic.basic.util.NumberUtil.*;

import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link SearchExpression} rounding a floating point value to a given number of digits.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Round extends BinaryOperation {

	/**
	 * Creates a {@link Round}.
	 * 
	 * @param left
	 *        The value to round, see {@link #getLeft()}.
	 * @param right
	 *        The number of digits to round to, see {@link #getRight()}.
	 */
	Round(SearchExpression left, SearchExpression right) {
		super(left, right);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitRound(this, arg);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		Object value = getLeft().evalWith(definitions, args);
		Object precision = getRight().evalWith(definitions, args);

		return compute(value, precision);
	}

	/**
	 * Performs the actual {@link Round} computation with the given values.
	 */
	public final Double compute(Object value, Object precision) {
		if (value == null) {
			return Double.valueOf(0.0);
		}

		int precisionValue = precision == null ? 0 : ((Number) precision).intValue();

		double rounded = round(((Number) value).doubleValue(), precisionValue);
		return Double.valueOf(rounded);
	}

}
