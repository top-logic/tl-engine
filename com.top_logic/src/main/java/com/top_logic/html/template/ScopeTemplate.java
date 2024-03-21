/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

/**
 * Base class for templates defining a local variable.
 */
public abstract class ScopeTemplate implements HTMLTemplateFragment {

	/**
	 * Creates a {@link ScopeTemplate}.
	 */
	public ScopeTemplate(String var, TemplateExpression expression, HTMLTemplateFragment content) {
		_var = var;
		_expression = expression;
		_content = content;
	}

	private final String _var;

	private final TemplateExpression _expression;

	private HTMLTemplateFragment _content;

	/**
	 * The variable to bind.
	 */
	public String getVar() {
		return _var;
	}

	/**
	 * The expression that generates the value of the variable to bind.
	 */
	public TemplateExpression getExpression() {
		return _expression;
	}

	/**
	 * The contents to render with the variable bound to the value computed by the expression.
	 */
	public HTMLTemplateFragment getContent() {
		return _content;
	}

	/**
	 * @see #getContent()
	 */
	public void setContent(HTMLTemplateFragment content) {
		_content = content;
	}

}
