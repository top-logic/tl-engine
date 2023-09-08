/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template.expr;

import com.top_logic.html.template.I18NConstants;
import com.top_logic.html.template.TemplateExpression;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link BinaryExpression} operating on numbers.
 */
public abstract class NumericExpression extends BinaryExpression {

	/**
	 * Creates a {@link NumericExpression}.
	 */
	public NumericExpression(TemplateExpression left, TemplateExpression right) {
		super(left, right);
	}

	@Override
	protected Object compute(Object leftValue, Object rightValue) {
		Number leftNum = toNumber(leftValue);
		Number rightNum = toNumber(rightValue);
		if (isInteger(leftNum) && isInteger(rightNum)) {
			return computeLong(leftNum.longValue(), rightNum.longValue());
		} else {
			return computeDouble(leftNum.doubleValue(), rightNum.doubleValue());
		}
	}

	private static boolean isInteger(Number num) {
		return num instanceof Integer || num instanceof Long;
	}

	/**
	 * Computes the numeric result.
	 */
	protected abstract Object computeDouble(double leftNum, double rightNum);

	/**
	 * Computes the numeric result.
	 */
	protected abstract Object computeLong(long leftNum, long rightNum);

	static Number toNumber(Object leftValue) {
		if (leftValue instanceof Number) {
			return (Number) leftValue;
		} else {
			throw new TopLogicException(I18NConstants.ERROR_NOT_A_NUMBER__VALUE.fill(leftValue));
		}
	}

}
