/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template.expr;

import com.top_logic.html.template.I18NConstants;
import com.top_logic.html.template.TemplateExpression;
import com.top_logic.util.Utils;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link CompareExpression} for greater-or-equal.
 */
public class GeExpression extends CompareExpression {

	/**
	 * Creates a {@link GeExpression}.
	 */
	public GeExpression(TemplateExpression left, TemplateExpression right) {
		super(left, right);
	}

	@Override
	protected boolean compareObj(Object leftValue, Object rightValue) {
		Comparable<?> leftComparable = toComparable(leftValue);
		Comparable<?> rightComparable = toComparable(rightValue);
		return Utils.isGreaterOrEqual(leftComparable, rightComparable);
	}

	private static Comparable<?> toComparable(Object value) {
		if (!(value instanceof Comparable)) {
			throw new TopLogicException(I18NConstants.ERROR_NOT_COMPARABLE__VALUE.fill(value));
		}
		return (Comparable<?>) value;
	}

	@Override
	protected boolean compareFloat(double leftNum, double rightNum) {
		return leftNum >= rightNum;
	}

	@Override
	protected boolean compareInteger(long leftNum, long rightNum) {
		return leftNum >= rightNum;
	}

}
