/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template.expr;

import com.top_logic.html.template.TemplateExpression;

/**
 * {@link NumericExpression} performing a division.
 */
public class DivExpression extends NumericExpression {

	/**
	 * Creates a {@link DivExpression}.
	 */
	public DivExpression(TemplateExpression left, TemplateExpression right) {
		super(left, right);
	}

	@Override
	protected Object computeDouble(double leftNum, double rightNum) {
		return Double.valueOf(leftNum / rightNum);
	}

	@Override
	protected Object computeLong(long leftNum, long rightNum) {
		return Long.valueOf(leftNum / rightNum);
	}

}
