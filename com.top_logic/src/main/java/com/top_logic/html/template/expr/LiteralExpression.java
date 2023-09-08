/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template.expr;

import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.html.template.TemplateExpression;
import com.top_logic.html.template.TemplateNode;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.template.WithProperties;

/**
 * A literal value in a {@link HTMLTemplateFragment}.
 */
public class LiteralExpression extends TemplateNode implements TemplateExpression {

	private final Object _value;

	/**
	 * Creates a {@link LiteralExpression}.
	 *
	 * @param value
	 *        The literal value.
	 */
	public LiteralExpression(int line, int column, Object value) {
		super(line, column);
		_value = value;
	}

	@Override
	public Object eval(DisplayContext context, WithProperties properties) {
		return _value;
	}

}
