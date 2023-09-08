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
 * {@link HTMLTemplateFragment} for a HTML end tag.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class EndTagTemplate extends TemplateNode implements RawTemplateFragment {

	private final String _name;

	/** 
	 * Creates a {@link EndTagTemplate}.
	 * 
	 * @param name Tag name.
	 */
	public EndTagTemplate(int line, int column, String name) {
		super(line, column);
		_name = name;
	}

	/**
	 * The tag name.
	 */
	public String getName() {
		return _name;
	}

	@Override
	public void write(DisplayContext context, TagWriter out, WithProperties properties) throws IOException {
		out.endTag(_name);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}
}
