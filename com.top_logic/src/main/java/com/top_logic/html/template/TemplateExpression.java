/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.template.WithProperties;

/**
 * Expression that can be evaluated while rendering a {@link HTMLTemplateFragment}.
 */
public interface TemplateExpression {

	/**
	 * Evaluates this expression in the given context.
	 *
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param properties
	 *        The model object that is currently
	 *        {@link HTMLTemplateFragment#write(DisplayContext, com.top_logic.basic.xml.TagWriter, WithProperties)
	 *        rendered}.
	 * @return The evaluation result.
	 */
	Object eval(DisplayContext context, WithProperties properties);

	/**
	 * Converts this evaluation to a renderer for the evaluated value.
	 */
	default RawTemplateFragment toFragment() {
		return new ExpressionTemplate(this);
	}

}
