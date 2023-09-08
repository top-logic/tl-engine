/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template.expr;

import com.top_logic.html.template.TemplateExpression;

/**
 * {@link NumericExpression} for a multiplication.
 */
public class MulExpression extends NumericExpression {

	/**
	 * Creates a {@link MulExpression}.
	 */
	public MulExpression(TemplateExpression left, TemplateExpression right) {
		super(left, right);
	}

	@Override
	protected Object computeDouble(double leftNum, double rightNum) {
		return leftNum * rightNum;
	}

	@Override
	protected Object computeLong(long leftNum, long rightNum) {
		return leftNum * rightNum;
	}


}
