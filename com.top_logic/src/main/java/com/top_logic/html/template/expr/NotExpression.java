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
 * Boolean {@link TemplateExpression} negating the result of another {@link TemplateExpression}.
 */
public class NotExpression implements TemplateExpression {

	private final TemplateExpression _expression;

	/**
	 * Creates a {@link NotExpression}.
	 */
	public NotExpression(TemplateExpression expression) {
		_expression = expression;
	}

	@Override
	public Object eval(DisplayContext context, WithProperties properties) {
		return !TestExpression.isTrue(_expression.eval(context, properties));
	}

}
