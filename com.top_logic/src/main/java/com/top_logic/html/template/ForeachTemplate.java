/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.template.WithProperties;

/**
 * A template expanding a {@link #getContent()} multiple times in the context of a bound loop
 * variable.
 */
public class ForeachTemplate implements RawTemplateFragment {

	private final String _var;

	private final TemplateExpression _expression;

	private HTMLTemplateFragment _content;

	/**
	 * Creates a {@link ForeachTemplate}.
	 *
	 * @param var
	 *        See {@link #getVar()}.
	 * @param expression
	 *        See {@link #getExpression()}.
	 * @param inner
	 *        See {@link #getContent()}
	 */
	public ForeachTemplate(String var, TemplateExpression expression, HTMLTemplateFragment inner) {
		_var = var;
		_expression = expression;
		_content = inner;
	}

	/**
	 * The local variable to define.
	 */
	public String getVar() {
		return _var;
	}

	/**
	 * The expression computing a list of elements to iterate.
	 */
	public TemplateExpression getExpression() {
		return _expression;
	}

	/**
	 * The content template to evaluate for each element.
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
		LocalVariable localVariable = new LocalVariable(_var, properties);

		Collection<?> elements = asCollection(_expression.eval(context, properties));
		for (Object x : elements) {
			localVariable.setValue(x);
			_content.write(context, out, localVariable);
		}
	}

	private Collection<?> asCollection(Object obj) {
		return obj == null ? Collections.emptyList()
			: obj instanceof Collection ? (Collection<?>) obj : Collections.singletonList(obj);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}

}
