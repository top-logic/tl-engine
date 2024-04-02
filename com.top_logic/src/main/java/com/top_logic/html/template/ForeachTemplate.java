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
public class ForeachTemplate extends ScopeTemplate implements RawTemplateFragment {

	/**
	 * Creates a {@link ForeachTemplate}.
	 *
	 * @param var
	 *        See {@link #getVar()}.
	 * @param expression
	 *        See {@link #getExpression()}.
	 * @param content
	 *        See {@link #getContent()}
	 */
	public ForeachTemplate(String var, TemplateExpression expression, HTMLTemplateFragment content) {
		super(var, expression, content);
	}

	@Override
	public void write(DisplayContext context, TagWriter out, WithProperties properties) throws IOException {
		LocalVariable localVariable = new LocalVariable(getVar(), properties);

		Collection<?> elements = asCollection(getExpression().eval(context, properties));
		for (Object x : elements) {
			localVariable.setValue(x);
			getContent().write(context, out, localVariable);
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
