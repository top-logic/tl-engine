/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template.expr;

import com.top_logic.html.template.TemplateExpression;

/**
 * Base class for unary operations.
 */
public abstract class UnaryExpression implements TemplateExpression {

	private final TemplateExpression _expr;

	/**
	 * Creates a {@link UnaryExpression}.
	 */
	public UnaryExpression(TemplateExpression expr) {
		_expr = expr;
	}

	/**
	 * The expression to operate on.
	 */
	public TemplateExpression getExpr() {
		return _expr;
	}

}
