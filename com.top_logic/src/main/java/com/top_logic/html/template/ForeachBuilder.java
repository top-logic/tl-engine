/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

/**
 * {@link TemplateEnhancer} enhancing a template to a looping template.
 */
public class ForeachBuilder implements TemplateEnhancer {

	private final String _var;

	private final String _iteration;

	private final TemplateExpression _expression;

	/**
	 * Creates a {@link ForeachBuilder}.
	 */
	public ForeachBuilder(String var, String iteration, TemplateExpression expression) {
		_var = var;
		_iteration = iteration;
		_expression = expression;
	}

	@Override
	public HTMLTemplateFragment build(HTMLTemplateFragment inner) {
		return new ForeachTemplate(_var, _iteration, _expression, inner);
	}

}
