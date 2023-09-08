/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.template.model.AttributeTemplate;
import com.top_logic.layout.template.WithProperties;

/**
 * Template for an HTML attribute of a {@link StartTagTemplate}.
 */
public class TagAttributeTemplate extends TemplateNode implements RawTemplateFragment, AttributeTemplate {

	private final String _name;

	private final HTMLTemplateFragment _content;

	/**
	 * Creates a {@link TagAttributeTemplate}.
	 * 
	 * @param name
	 *        The name of the attribute.
	 * @param content
	 *        The template to create the attribute value from.
	 */
	public TagAttributeTemplate(int line, int column, String name, HTMLTemplateFragment content) {
		super(line, column);
		_name = name;
		_content = content;
	}

	/**
	 * The name of this attribute.
	 */
	@Override
	public String getName() {
		return _name;
	}

	/**
	 * The attribute value template.
	 */
	public HTMLTemplateFragment getContent() {
		return _content;
	}

	@Override
	public void write(DisplayContext context, TagWriter out, WithProperties properties) throws IOException {
		out.beginAttribute(_name);
		_content.write(context, out, properties);
		out.endAttribute();
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}
}
