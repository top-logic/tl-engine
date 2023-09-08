/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.model;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.LiteralTemplate;
import com.top_logic.layout.DisplayContext;

/**
 * Template for rendering pre-computed HTML source code.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class HTMLSource implements Template, LiteralTemplate {
	private final String _value;

	HTMLSource(String value) {
		_value = value;
	}

	/**
	 * The literal HTML source code.
	 */
	public String getValue() {
		return _value;
	}

	@Override
	public void write(DisplayContext displayContext, TagWriter out) throws IOException {
		out.writeContent(getValue());
	}

}