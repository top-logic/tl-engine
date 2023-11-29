/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template.expr;

import com.top_logic.html.template.TemplateExpression;

/**
 * {@link StrictBinaryExpression} comparing tow values.
 */
public abstract class CompareExpression extends StrictBinaryExpression {

	/**
	 * Creates a {@link CompareExpression}.
	 */
	public CompareExpression(TemplateExpression left, TemplateExpression right) {
		super(left, right);
	}

	@Override
	protected final Object compute(Object leftValue, Object rightValue) {
		boolean result;
		if (isInteger(leftValue) && isInteger(rightValue)) {
			long leftNum = ((Number) leftValue).longValue();
			long rightNum = ((Number) rightValue).longValue();
			result = compareInteger(leftNum, rightNum);
		} else if (leftValue instanceof Number && rightValue instanceof Number) {
			double leftNum = ((Number) leftValue).doubleValue();
			double rightNum = ((Number) rightValue).doubleValue();
			result = compareFloat(leftNum, rightNum);
		} else {
			result = compareObj(leftValue, rightValue);
		}
		return Boolean.valueOf(result);
	}

	private static boolean isInteger(Object value) {
		return value instanceof Integer || value instanceof Long;
	}

	/**
	 * Compares two arbitrary objects.
	 */
	protected abstract boolean compareObj(Object leftValue, Object rightValue);

	/**
	 * Compares two floating point numbers.
	 */
	protected abstract boolean compareFloat(double leftNum, double rightNum);

	/**
	 * Compares two integer numbers.
	 */
	protected abstract boolean compareInteger(long leftNum, long rightNum);

}
