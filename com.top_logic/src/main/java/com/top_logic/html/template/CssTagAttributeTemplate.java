/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.template.WithProperties;

/**
 * Template for an HTML CSS attribute of a {@link StartTagTemplate}.
 */
public class CssTagAttributeTemplate extends TagAttributeTemplate {

	/**
	 * Creates a {@link CssTagAttributeTemplate}.
	 * 
	 * @param name
	 *        The name of the attribute.
	 * @param content
	 *        The template to create the attribute value from.
	 */
	public CssTagAttributeTemplate(int line, int column, String name, HTMLTemplateFragment content) {
		super(line, column, name, content);
	}

	@Override
	public void write(DisplayContext context, TagWriter out, WithProperties properties) throws IOException {
		out.beginCssClasses(getName());
		getContent().write(context, out, properties);
		out.endCssClasses();
	}

}
