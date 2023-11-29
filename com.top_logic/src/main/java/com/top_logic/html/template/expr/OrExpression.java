/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template.expr;

import com.top_logic.html.template.TemplateExpression;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.template.WithProperties;

/**
 * Boolean {@link TemplateExpression} combining the result of tow other {@link TemplateExpression}s
 * with <code>or</code>.
 */
public class OrExpression extends BinaryExpression {

	/**
	 * Creates a {@link OrExpression}.
	 */
	public OrExpression(TemplateExpression left, TemplateExpression right) {
		super(left, right);
	}

	@Override
	public Object eval(DisplayContext context, WithProperties properties) {
		// Note: Allow expressions such as "{attr} || '---'" to produce a string constant, if a
		// value is null.
		Object leftValue = getLeft().eval(context, properties);
		return !TestExpression.isTrue(leftValue) ? getRight().eval(context, properties) : leftValue;
	}

	@Override
	public <R, A> R visit(Visitor<R, A> v, A arg) {
		return v.visit(this, arg);
	}
}
