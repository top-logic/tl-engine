/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template.expr;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.html.template.LiteralTemplate;
import com.top_logic.html.template.RawTemplateFragment;
import com.top_logic.html.template.TemplateExpression;
import com.top_logic.html.template.TemplateNode;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.template.WithProperties;

/**
 * A string literal in a {@link HTMLTemplateFragment}.
 */
public class StringLiteral extends TemplateNode implements RawTemplateFragment, TemplateExpression, LiteralTemplate {

	private String _text;

	/**
	 * Creates a {@link StringLiteral}.
	 *
	 * @param text
	 *        The literal text to render.
	 */
	public StringLiteral(int line, int column, String text) {
		super(line, column);
		_text = text;
	}

	/**
	 * The text to render.
	 */
	public String getText() {
		return _text;
	}

	/**
	 * @see #getText()
	 */
	public void setText(String text) {
		_text = text;
	}

	@Override
	public Object eval(DisplayContext context, WithProperties properties) {
		return _text;
	}

	@Override
	public RawTemplateFragment toFragment() {
		return this;
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		out.write(_text);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}
}
