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
public class DefineTemplate extends ScopeTemplate implements RawTemplateFragment {

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
		super(var, expression, content);
	}

	@Override
	public void write(DisplayContext context, TagWriter out, WithProperties properties) throws IOException {
		LocalVariable localVariables = new LocalVariable(getVar(), properties);
		Object value = getExpression().eval(context, properties);
		localVariables.setValue(value);
		getContent().write(context, out, localVariables);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}

}
