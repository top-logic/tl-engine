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
 * Base class for binary operation expressions.
 */
public abstract class BinaryExpression implements TemplateExpression {

	private final TemplateExpression _left;

	private final TemplateExpression _right;

	/**
	 * Creates a {@link BinaryExpression}.
	 *
	 * @param left
	 *        The expression computing the left input to the operation.
	 * @param right
	 *        The expression computing the right input to the operation.
	 */
	public BinaryExpression(TemplateExpression left, TemplateExpression right) {
		_left = left;
		_right = right;
	}

	@Override
	public final Object eval(DisplayContext context, WithProperties properties) {
		Object leftValue = _left.eval(context, properties);
		Object rightValue = _right.eval(context, properties);
		return compute(leftValue, rightValue);
	}

	/**
	 * Combines the input values to the result value.
	 */
	protected abstract Object compute(Object leftValue, Object rightValue);

}
