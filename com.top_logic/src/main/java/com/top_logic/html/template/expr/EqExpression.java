/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template.expr;

import com.top_logic.basic.util.Utils;
import com.top_logic.html.template.TemplateExpression;

/**
 * {@link BinaryExpression} comparing two values.
 */
public class EqExpression extends CompareExpression {

	/**
	 * Creates a {@link EqExpression}.
	 */
	public EqExpression(TemplateExpression left, TemplateExpression right) {
		super(left, right);
	}

	@Override
	protected boolean compareObj(Object leftValue, Object rightValue) {
		return Utils.equals(leftValue, rightValue);
	}

	@Override
	protected boolean compareFloat(double leftNum, double rightNum) {
		return leftNum == rightNum;
	}

	@Override
	protected boolean compareInteger(long leftNum, long rightNum) {
		return leftNum == rightNum;
	}
}
