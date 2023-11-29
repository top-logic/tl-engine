/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template.expr;

import com.top_logic.html.template.TemplateExpression;

/**
 * Base class for binary operations.
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

	/**
	 * The left-hand-side of the expression.
	 */
	public TemplateExpression getLeft() {
		return _left;
	}

	/**
	 * The right-hand-side of the expression.
	 */
	public TemplateExpression getRight() {
		return _right;
	}

}
