/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.template.WithProperties;

/**
 * {@link HTMLTemplateFragment} binding a local variable during rendering of it's content.
 */
public class DefineTemplate implements RawTemplateFragment {

	private final String _var;

	private final TemplateExpression _expression;

	private HTMLTemplateFragment _content;

	/**
	 * Creates a {@link DefineTemplate}.
	 * 
	 * @param var
	 *        See {@link #getVar()}.
	 * @param expression
	 *        See {@link #getExpression()}.
	 * @param content
	 *        See {@link #getContent()}.
	 */
	public DefineTemplate(String var, TemplateExpression expression, RawTemplateFragment content) {
		_var = var;
		_expression = expression;
		_content = content;
	}

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

	@Override
	public void write(DisplayContext context, TagWriter out, WithProperties properties) throws IOException {
		LocalVariable localVariables = new LocalVariable(_var, properties);
		Object value = _expression.eval(context, properties);
		localVariables.setValue(value);
		_content.write(context, out, localVariables);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}

}
